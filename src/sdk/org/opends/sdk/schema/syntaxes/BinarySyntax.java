package org.opends.sdk.schema.syntaxes;

import org.opends.messages.MessageBuilder;
import org.opends.sdk.schema.Schema;
import org.opends.server.types.ByteSequence;
import static org.opends.sdk.schema.SchemaConstants.*;

/**
 * This class defines the binary attribute syntax, which is essentially a byte
 * array using very strict matching.  Equality, ordering, and substring matching
 * will be allowed by default.
 */
public class BinarySyntax extends AbstractSyntaxImplementation
{
  public String getName() {
    return SYNTAX_BINARY_NAME;
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
    // All values will be acceptable for the binary syntax.
    return true;
  }

  public boolean isHumanReadable()
  {
    return false;
  }

  @Override
  public String getEqualityMatchingRule() {
    return EMR_OCTET_STRING_OID;
  }

  @Override
  public String getOrderingMatchingRule() {
    return OMR_OCTET_STRING_OID;
  }
}
