package org.opends.sdk.schema.syntaxes;

import static org.opends.sdk.schema.SchemaConstants.EMR_OCTET_STRING_OID;
import static org.opends.sdk.schema.SchemaConstants.OMR_OCTET_STRING_OID;
import static org.opends.sdk.schema.SchemaConstants.SYNTAX_SUPPORTED_ALGORITHM_NAME;

import org.opends.messages.MessageBuilder;
import org.opends.sdk.schema.Schema;
import org.opends.server.types.ByteSequence;

/**
 * This class implements the supported algorithm attribute syntax.  This should
 * be restricted to holding only X.509 supported algorithms, but we will accept
 * any set of bytes.  It will be treated much like the octet string attribute
 * syntax.
 */
public class SupportedAlgorithmSyntax extends AbstractSyntaxImplementation
{

  public String getName() {
    return SYNTAX_SUPPORTED_ALGORITHM_NAME;
  }

  public boolean isHumanReadable() {
    return false;
  }

  public boolean isBEREncodingRequired() {
    return true;
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
    // All values will be acceptable for the supported algorithm syntax.
    return true;
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

