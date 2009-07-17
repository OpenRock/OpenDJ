package org.opends.schema.syntaxes;

import static org.opends.server.schema.SchemaConstants.SYNTAX_CERTIFICATE_NAME;
import static org.opends.server.schema.SchemaConstants.SYNTAX_CERTIFICATE_OID;
import static org.opends.server.schema.SchemaConstants.SYNTAX_CERTIFICATE_DESCRIPTION;
import org.opends.server.types.ByteSequence;
import org.opends.messages.MessageBuilder;
import org.opends.schema.SchemaUtils;

/**
 * This class implements the certificate attribute syntax.  This should be
 * restricted to holding only X.509 certificates, but we will accept any set of
 * bytes.  It will be treated much like the octet string attribute syntax.
 */
public class CertificateSyntax extends SyntaxImplementation
{
  /**
   * Creates a new instance of this syntax.
   */
  public CertificateSyntax()
  {
    super(SYNTAX_CERTIFICATE_OID, SYNTAX_CERTIFICATE_NAME,
        SYNTAX_CERTIFICATE_DESCRIPTION, SchemaUtils.RFC4512_ORIGIN);
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
    // All values will be acceptable for the certificate syntax.
    return true;
  }

  public boolean isHumanReadable() {
    return false;
  }
}
