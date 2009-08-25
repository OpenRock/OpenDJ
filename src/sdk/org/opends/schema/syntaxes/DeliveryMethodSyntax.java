package org.opends.schema.syntaxes;

import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_DELIVERY_METHOD_INVALID_ELEMENT;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_DELIVERY_METHOD_NO_ELEMENTS;
import static org.opends.server.util.StaticUtils.toLowerCase;

import java.util.HashSet;
import java.util.StringTokenizer;

import org.opends.messages.MessageBuilder;
import org.opends.schema.Schema;
import org.opends.server.types.ByteSequence;
import static org.opends.server.schema.SchemaConstants.*;
import static org.opends.server.schema.SchemaConstants.AMR_DOUBLE_METAPHONE_OID;

/**
 * This class defines the delivery method attribute syntax.  This contains one
 * or more of a fixed set of values.  If there are multiple values, then they
 * are separated by spaces with a dollar sign between them.  The allowed values
 * include:
 *
 * <UL>
 *   <LI>any</LI>
 *   <LI>mhs</LI>
 *   <LI>physical</LI>
 *   <LI>telex</LI>
 *   <LI>teletex</LI>
 *   <LI>g3fax</LI>
 *   <LI>g4fax</LI>
 *   <LI>ia5</LI>
 *   <LI>videotex</LI>
 *   <LI>telephone</LI>
 * </UL>
 */
public class DeliveryMethodSyntax extends AbstractSyntaxImplementation
{
  /**
   * The set of values that may be used as delivery methods.
   */
  private static final HashSet<String> ALLOWED_VALUES =
        new HashSet<String>();
  {
    ALLOWED_VALUES.add("any");
    ALLOWED_VALUES.add("mhs");
    ALLOWED_VALUES.add("physical");
    ALLOWED_VALUES.add("telex");
    ALLOWED_VALUES.add("teletex");
    ALLOWED_VALUES.add("g3fax");
    ALLOWED_VALUES.add("g4fax");
    ALLOWED_VALUES.add("ia5");
    ALLOWED_VALUES.add("videotex");
    ALLOWED_VALUES.add("telephone");
  }

  public String getName() {
    return SYNTAX_DELIVERY_METHOD_NAME;
  }

  /**
   * Indicates whether the provided value is acceptable for use in an attribute
   * with this syntax.  If it is not, then the reason may be appended to the
   * provided buffer.
   *
   * @param schema
   *@param  value          The value for which to make the determination.
   * @param  invalidReason  The buffer to which the invalid reason should be
 *                        appended.
 * @return  <CODE>true</CODE> if the provided value is acceptable for use with
   *          this syntax, or <CODE>false</CODE> if not.
   */
  public boolean valueIsAcceptable(Schema schema, ByteSequence value,
                                   MessageBuilder invalidReason)
  {
    String stringValue = toLowerCase(value.toString());
    StringTokenizer tokenizer = new StringTokenizer(stringValue, " $");
    if (! tokenizer.hasMoreTokens())
    {
      invalidReason.append(ERR_ATTR_SYNTAX_DELIVERY_METHOD_NO_ELEMENTS.get(
              value.toString()));
      return false;
    }

    while (tokenizer.hasMoreTokens())
    {
      String token = tokenizer.nextToken();
      if (! ALLOWED_VALUES.contains(token))
      {
        invalidReason.append(ERR_ATTR_SYNTAX_DELIVERY_METHOD_INVALID_ELEMENT
                .get(value.toString(), token));
        return false;
      }
    }

    return true;
  }

  public boolean isHumanReadable() {
    return true;
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
