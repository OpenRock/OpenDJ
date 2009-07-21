package org.opends.schema;

import org.opends.server.util.Validator;
import org.opends.ldap.DecodeException;
import org.opends.util.SubstringReader;
import org.opends.messages.Message;
import static org.opends.messages.SchemaMessages.*;

import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;

/**
 * This class defines a data structure for storing and interacting
 * with matching rules, which are used by servers to compare 
 * attribute values against assertion values when performing Search
 * and Compare operations.  They are also used to identify the value
 * to be added or deleted when modifying entries, and are used when
 * comparing a purported distinguished name with the name of an entry.
 * <p>
 * Matching rule implementations must extend the
 * <code>MatchingRuleImplementation</code> class so they can be used by
 * OpenDS.
 * <p>
 * Where ordered sets of names, or extra properties are provided, the
 * ordering will be preserved when the associated fields are accessed
 * via their getters or via the {@link #toString()} methods.
 */
public class MatchingRule extends AbstractSchemaElement
{
  // The OID that may be used to reference this definition.
  private final String oid;

  private final String syntax;

  // The definition string used to create this objectclass.
  private final String definition;

  protected MatchingRule(String oid,
                     List<String> names,
                     String description,
                     boolean obsolete,
                     String syntax,
                     Map<String, List<String>> extraProperties)
  {
    super(names, description, obsolete, extraProperties);

    Validator.ensureNotNull(oid, syntax);
    this.oid = oid;
    this.syntax = syntax;
    this.definition = buildDefinition();
  }

  /**
   * Construct a copy of the provided matching rule.
   *
   * @param orginalMatchingRule The matching rule to copy.
   */
  protected MatchingRule(MatchingRule orginalMatchingRule)
  {
    super(orginalMatchingRule.names, orginalMatchingRule.description,
        orginalMatchingRule.isObsolete, orginalMatchingRule.extraProperties);

    this.oid = orginalMatchingRule.oid;
    this.syntax = orginalMatchingRule.syntax;
    this.definition = orginalMatchingRule.definition;
  }

  private MatchingRule(String oid,
                     List<String> names,
                     String description,
                     boolean obsolete,
                     String syntax,
                     Map<String, List<String>> extraProperties,
                     String definition)
  {
    super(names, description, obsolete, extraProperties);

    Validator.ensureNotNull(oid, syntax, definition);
    this.oid = oid;
    this.syntax = syntax;
    this.definition = definition;
  }

      /**
   * Retrieves the OID for this schema definition.
   *
   * @return The OID for this schema definition.
   */
  public final String getOID() {

    return oid;
  }


  /**
   * Retrieves the name or OID for this schema definition. If it has
   * one or more names, then the primary name will be returned. If it
   * does not have any names, then the OID will be returned.
   *
   * @return The name or OID for this schema definition.
   */
  public String getNameOrOID() {
    if(names.isEmpty())
    {
      return oid;
    }
    return names.get(0);
  }

  /**
   * Indicates whether this schema definition has the specified name
   * or OID.
   *
   * @param value
   *          The value for which to make the determination.
   * @return <code>true</code> if the provided value matches the OID
   *         or one of the names assigned to this schema definition,
   *         or <code>false</code> if not.
   */
  public boolean hasNameOrOID(String value) {
    return hasName(value) ||
        getOID().equals(value);
  }

  /**
   * Retrieves the OID of the assertion value syntax with which this matching
   * rule is associated.
   *
   * @return The OID of the assertion value syntax with which this matching
   *         rule is associated.
   */
  String getSyntax()
  {
    return syntax;
  }

  protected String getDefinition() {
    return definition;
  }

  protected String getIdentifier() {
    return oid;
  }

  protected void toStringContent(StringBuilder buffer) {
    buffer.append(" SYNTAX ");
    buffer.append(syntax);
  }

  public static MatchingRule decode(String definition)
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
      Message message = ERR_ATTR_SYNTAX_MR_EMPTY_VALUE.get();
      throw new DecodeException(message);
    }


    // The next character must be an open parenthesis.  If it is not, then that
    // is an error.
    char c = reader.read();
    if (c != '(')
    {
      Message message = ERR_ATTR_SYNTAX_MR_EXPECTED_OPEN_PARENTHESIS.
          get(definition, (reader.pos()-1), String.valueOf(c));
      throw new DecodeException(message);
    }


    // Skip over any spaces immediately following the opening parenthesis.
    reader.skipWhitespaces();

    // The next set of characters must be the OID.
    String oid = SchemaUtils.readNumericOID(reader);

    List<String> names = Collections.emptyList();
    String description = "".intern();
    boolean isObsolete = false;
    String syntax = null;
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
      else if (tokenName.equalsIgnoreCase("name"))
      {
        names = SchemaUtils.readNameDescriptors(reader);
      }
      else if (tokenName.equalsIgnoreCase("desc"))
      {
        // This specifies the description for the matching rule.  It is an
        // arbitrary string of characters enclosed in single quotes.
        description = SchemaUtils.readQuotedString(reader);
      }
      else if (tokenName.equalsIgnoreCase("obsolete"))
      {
        // This indicates whether the matching rule should be considered
        // obsolete.  We do not need to do any more parsing for this token.
        isObsolete = true;
      }
      else if (tokenName.equalsIgnoreCase("syntax"))
      {
        syntax = SchemaUtils.readNumericOID(reader);
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

    // Make sure that a syntax was specified.
    if (syntax == null)
    {
      Message message = ERR_ATTR_SYNTAX_MR_NO_SYNTAX.get(definition);
      throw new DecodeException(message);
    }

    return new MatchingRule(oid, names, description, isObsolete, syntax,
        extraProperties, definition);
  }
}
