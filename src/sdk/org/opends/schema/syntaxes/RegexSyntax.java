package org.opends.schema.syntaxes;

import static org.opends.messages.SchemaMessages.WARN_ATTR_SYNTAX_LDAPSYNTAX_REGEX_INVALID_VALUE;

import java.util.regex.Pattern;

import org.opends.messages.Message;
import org.opends.messages.MessageBuilder;
import org.opends.schema.Schema;
import org.opends.server.types.ByteSequence;
import org.opends.server.util.Validator;

/**
 * This class provides a regex mechanism where a new syntax and its
 * corresponding matching rules can be created on-the-fly. A regex
 * syntax is an LDAPSyntaxDescriptionSyntax with X-PATTERN extension.
 */
public class RegexSyntax extends AbstractSyntaxImplementation
{
  // The Pattern associated with the regex.
  private Pattern pattern;

  public String getName() {
    return "Substitution Syntax";
  }

  /**
   * Construct a new regular expression syntax implementation for the
   * given syntax with the given pattern.
   *
   * @param pattern The pattern used to validate the values.
   */
  public RegexSyntax(Pattern pattern)
  {
    Validator.ensureNotNull(pattern);
    this.pattern = pattern;
  }

  public boolean isHumanReadable()
  {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  public boolean valueIsAcceptable(Schema schema, ByteSequence value,
                                   MessageBuilder invalidReason)
  {
    String strValue = value.toString();
    boolean matches = pattern.matcher(strValue).matches();
    if(!matches)
    {
      Message message = WARN_ATTR_SYNTAX_LDAPSYNTAX_REGEX_INVALID_VALUE.get(
          strValue, pattern.pattern());
      invalidReason.append(message);
    }
    return matches;
  }
}
