package org.opends.schema;

import org.opends.server.util.Validator;
import org.opends.ldap.DecodeException;
import org.opends.util.SubstringReader;
import org.opends.messages.Message;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_ATTRSYNTAX_EMPTY_VALUE;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_ATTRSYNTAX_EXPECTED_OPEN_PARENTHESIS;

import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;

/**
 * This class defines a data structure for storing and interacting
 * with an LDAP syntaxes, which constrain the structure of attribute values
 * stored in an LDAP directory, and determine the representation of attribute
 * and assertion values transferred in the LDAP protocol.
 * <p>
 * Syntax implementations must extend the <code>SyntaxImplementation</code>
 * class so they can be used by OpenDS to validate attribute values.
 * <p>
 * Where ordered sets of names, or extra properties are provided, the
 * ordering will be preserved when the associated fields are accessed
 * via their getters or via the {@link #toString()} methods.
 */
public class Syntax extends AbstractSchemaElement
{
  private final String oid;
  private final String definition;

  public Syntax(String oid, String description,
                Map<String, List<String>> extraProperties)
  {
    super(description, extraProperties);

    Validator.ensureNotNull(oid);
    this.oid = oid;
    this.definition = buildDefinition();
  }

  /**
   * Construct a copy of the provided syntax.
   *
   * @param orginalSyntax The syntax to copy.
   */
  protected Syntax(Syntax orginalSyntax)
  {
    super(orginalSyntax.description, orginalSyntax.extraProperties);

    this.oid = orginalSyntax.oid;
    this.definition = orginalSyntax.definition;
  }

  private Syntax(String oid, String description,
                   Map<String, List<String>> extraProperties,
                   String definition)
  {
    super(description, extraProperties);

    Validator.ensureNotNull(oid);
    this.oid = oid;
    this.definition = definition;
  }



  /**
   * Retrieves the OID for this attribute syntax.
   *
   * @return  The OID for this attribute syntax.
   */
  public final String getOID()
  {
    return oid;
  }


  /**
   * Retrieves the hash code for this attribute syntax.  It will be
   * calculated as the sum of the characters in the OID.
   *
   * @return  The hash code for this attribute syntax.
   */
  public final int hashCode()
  {
    return getOID().hashCode();
  }



  /**
   * Indicates whether the provided object is equal to this attribute
   * syntax. The provided object will be considered equal to this
   * attribute syntax only if it is an attribute syntax with the same
   * OID, description, and extra properties.
   *
   * @param  o  The object for which to make the determination.
   *
   * @return  {@code true} if the provided object is equal to this
   *          attribute syntax, or {@code false} if it is not.
   */
  public final boolean equals(Object o)
  {
    if (o == null)
    {
      return false;
    }

    if (this == o)
    {
      return true;
    }

    if (! (o instanceof Syntax))
    {
      return false;
    }

    Syntax syntax = (Syntax)o;
    return oid.equals(syntax.oid) &&
        description.equals(syntax.description) &&
        extraProperties.equals(syntax.extraProperties);
  }



  /**
   * Retrieves a string representation of this attribute syntax in the
   * format defined in RFC 2252.
   *
   * @return  A string representation of this attribute syntax in the
   *          format defined in RFC 2252.
   */
  public final String toString()
  {
    return definition;
  }

  protected void toStringContent(StringBuilder buffer)
  {
    buffer.append(oid);

    if ((description != null) && (description.length() > 0)) {
      buffer.append(" DESC '");
      buffer.append(description);
      buffer.append("'");
    }
  }

  public static Syntax decode(String definition)
      throws DecodeException
  {
    SubstringReader reader = new SubstringReader(definition);

    // We'll do this a character at a time.  First, skip over any leading
    // whitespace.
    reader.skipWhitespaces();

    if (reader.remaining() <= 0)
    {
      // This means that the value was empty or contained only whitespace.  That
      // is illegal.
      Message message = ERR_ATTR_SYNTAX_ATTRSYNTAX_EMPTY_VALUE.get();
      throw new DecodeException(message);
    }


    // The next character must be an open parenthesis.  If it is not, then that
    // is an error.
    char c = reader.read();
    if (c != '(')
    {
      Message message = ERR_ATTR_SYNTAX_ATTRSYNTAX_EXPECTED_OPEN_PARENTHESIS.
          get(definition, (reader.pos()-1), String.valueOf(c));
      throw new DecodeException(message);
    }


    // Skip over any spaces immediately following the opening parenthesis.
    reader.skipWhitespaces();

    // The next set of characters must be the OID.
    String oid = SchemaUtils.readNumericOID(reader);

    String description = "".intern();
    Map<String, List<String>> extraProperties = Collections.emptyMap();

    // At this point, we should have a pretty specific syntax that describes
    // what may come next, but some of the components are optional and it would
    // be pretty easy to put something in the wrong order, so we will be very
    // flexible about what we can accept.  Just look at the next token, figure
    // out what it is and how to treat what comes after it, then repeat until
    // we get to the end of the value.  But before we start, set default values
    // for everything else we might need to know.
    while (true)
    {
      String tokenName = SchemaUtils.readTokenName(reader);

      if (tokenName == null)
      {
        // No more tokens.
        break;
      }
      else if (tokenName.equalsIgnoreCase("desc"))
      {
        // This specifies the description for the syntax.  It is an
        // arbitrary string of characters enclosed in single quotes.
        description = SchemaUtils.readQuotedString(reader);
      }
      else
      {
        // This must be a non-standard property and it must be followed by
        // either a single value in single quotes or an open parenthesis
        // followed by one or more values in single quotes separated by spaces
        // followed by a close parenthesis.
        if(extraProperties == Collections.emptyList())
        {
          extraProperties = new HashMap<String, List<String>>();
        }
        extraProperties.put(tokenName,
            SchemaUtils.readExtraParameterValues(reader));
      }
    }

    return new Syntax(oid, description, extraProperties, definition);
  }
}
