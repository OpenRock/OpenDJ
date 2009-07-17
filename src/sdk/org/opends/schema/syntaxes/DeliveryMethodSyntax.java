package org.opends.schema.syntaxes;

import org.opends.server.types.ByteSequence;
import static org.opends.server.util.StaticUtils.toLowerCase;
import static org.opends.server.schema.SchemaConstants.*;
import org.opends.messages.MessageBuilder;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_DELIVERY_METHOD_NO_ELEMENTS;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_DELIVERY_METHOD_INVALID_ELEMENT;
import org.opends.schema.SchemaUtils;

import java.util.*;

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
public class DeliveryMethodSyntax extends SyntaxImplementation
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

  /**
   * Creates a new instance of this syntax.
   */
  public DeliveryMethodSyntax()
  {
    super(SYNTAX_DELIVERY_METHOD_OID, SYNTAX_DELIVERY_METHOD_NAME,
        SYNTAX_DELIVERY_METHOD_DESCRIPTION, SchemaUtils.RFC4512_ORIGIN);
  }


  /**
   * Indicates whether the provided value is acceptable for use in an attribute
   * with this syntax.  If it is not, then the reason may be appended to the
   * provided buffer.
   *
   * @param  value          The value for which to make the determination.
   * @param  invalidReason  The buffer to which the invalid reason should be
   *                        appended.
   *
   * @return  <CODE>true</CODE> if the provided value is acceptable for use with
   *          this syntax, or <CODE>false</CODE> if not.
   */
  public boolean valueIsAcceptable(ByteSequence value,
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
}
