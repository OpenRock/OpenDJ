package org.opends.schema;

import org.opends.schema.syntaxes.*;
import org.opends.util.SubstringReader;
import org.opends.messages.Message;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_ATTRSYNTAX_EMPTY_VALUE;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_ATTRSYNTAX_EXPECTED_OPEN_PARENTHESIS;
import org.opends.ldap.DecodeException;

import java.util.Map;
import java.util.List;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 15, 2009
 * Time: 5:48:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultSyntaxFactory implements SyntaxFactory
{
  private Map<String, Syntax> syntaxes;
  private Map<String, AttributeType> attributeTypes;
  private Map<String, ObjectClass> objectClasses;

  private DefaultSyntaxFactory()
  {
    syntaxes = new HashMap<String, Syntax>();
  }

  public Syntax registerSyntax(Syntax syntax)
  {
    return syntaxes.put(syntax.getOID(), syntax);
  }

  public void registerAttributeType(AttributeType attributeType,
                                    boolean overwrite)
  {
    if(!attributeTypes.containsKey(attributeType.getOID()))
    {
      attributeTypes.put(attributeType.getOID(), attributeType);
      for(String name : attributeType.getNames())
      {
        attributeTypes.put(name.toLowerCase(), attributeType);
      }
    }
  }

  public Syntax decode(String definition)
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

    String description = null;
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

    for(String property : extraProperties.keySet())
    {
      if(property.equalsIgnoreCase("x-subst"))
      {
        List<String> values = extraProperties.get(property);
        if(!values.isEmpty())
        {
          Syntax substitute = syntaxes.get(values.get(0));
          if(substitute != null)
          {
            return new SubstitutionSyntax(oid, description,
                extraProperties, definition, substitute);
          }
          else
          {
            // TODO: Warn
          }
        }
      }
      else if(property.equalsIgnoreCase("x-pattern"))
      {

      }
    }

    // This is just a static syntax
    Syntax syntax = getSyntax(oid);
    if(syntax == null)
    {
      // Use a default substitute?
    }
    else if(description != null || !extraProperties.isEmpty())
    {
      // There are some customizations.
      // Use a wrapper
    }

    return syntax;
  }

  public Syntax getSyntax(String oid) {
    return syntaxes.get(oid);
  }
}
