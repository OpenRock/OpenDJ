package org.opends.sdk.schema.syntaxes;

import org.opends.sdk.schema.Schema;
import static org.opends.sdk.schema.SchemaConstants.*;
import org.opends.sdk.util.Validator;
import org.opends.server.types.ByteSequence;
import org.opends.messages.MessageBuilder;
import org.opends.messages.Message;
import static org.opends.messages.SchemaMessages.WARN_ATTR_SYNTAX_LDAPSYNTAX_REGEX_INVALID_VALUE;

import java.util.regex.Pattern;

/**
 * This class provides a regex mechanism where a new syntax and its
 * corresponding matching rules can be created on-the-fly. A regex
 * syntax is an LDAPSyntaxDescriptionSyntax with X-PATTERN extension.
 */
public class RegexSyntax extends AbstractSyntaxImplementation
{
  // The Pattern associated with the regex.
  private final Pattern pattern;

  public RegexSyntax(Pattern pattern) {
    Validator.ensureNotNull(pattern);
    this.pattern = pattern;
  }

  public String getName() {
    return "Regex(" + pattern.toString() + ")";
  }

  public boolean isHumanReadable() {
    return true;
  }

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

  @Override
  public String getEqualityMatchingRule() {
    return EMR_CASE_IGNORE_OID;
  }

  @Override
  public String getOrderingMatchingRule() {
    return OMR_CASE_IGNORE_OID;
  }

  @Override
  public String getSubstringMatchingRule() {
    return SMR_CASE_IGNORE_OID;
  }

  @Override
  public String getApproximateMatchingRule() {
    return AMR_DOUBLE_METAPHONE_OID;
  }
}
