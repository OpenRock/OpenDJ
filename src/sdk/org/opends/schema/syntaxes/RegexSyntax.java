package org.opends.schema.syntaxes;

import org.opends.server.types.ByteSequence;
import org.opends.server.util.Validator;
import org.opends.messages.MessageBuilder;
import org.opends.messages.Message;
import static org.opends.messages.SchemaMessages.*;
import org.opends.schema.Syntax;

import java.util.regex.Pattern;
import java.util.List;
import java.util.Map;

/**
 * This class provides a regex mechanism where a new syntax and its
 * corresponding matching rules can be created on-the-fly. A regex
 * syntax is an LDAPSyntaxDescriptionSyntax with X-PATTERN extension.
 */
public class RegexSyntax extends SyntaxImplementation
{
  // The Pattern associated with the regex.
  private Pattern pattern;

  /**
   * Construct a new syntax that uses the given regular expression pattern
   * to validate values.
   *
   * @param pattern The pattern used to validate the values.
   */
  public RegexSyntax(String oid, String description,
                     Map<String, List<String>> extraProperties,
                     Pattern pattern)
  {
    super(oid, "Substitution Syntax", description, extraProperties);
    Validator.ensureNotNull(pattern);
    this.pattern = pattern;
  }

  /**
   * Construct a new regular expression syntax implementation for the
   * given syntax with the given pattern.
   *
   * @param syntax The syntax being implemented.
   * @param pattern The pattern used to validate the values.
   */
  public RegexSyntax(Syntax syntax, Pattern pattern)
  {
    super(syntax, "Substitution Syntax");
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
  @Override
  public boolean valueIsAcceptable(ByteSequence value,
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
