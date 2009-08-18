package org.opends.schema.syntaxes;

import static org.opends.server.schema.SchemaConstants.SYNTAX_CERTIFICATE_NAME;

import org.opends.messages.MessageBuilder;
import org.opends.schema.Schema;
import org.opends.server.types.ByteSequence;

/**
 * This class implements the certificate attribute syntax.  This should be
 * restricted to holding only X.509 certificates, but we will accept any set of
 * bytes.  It will be treated much like the octet string attribute syntax.
 */
public class CertificateSyntax extends AbstractSyntaxImplementation
{
  public String getName() {
    return SYNTAX_CERTIFICATE_NAME;
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
    // All values will be acceptable for the certificate syntax.
    return true;
  }

  public boolean isHumanReadable() {
    return false;
  }
}
