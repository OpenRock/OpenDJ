package org.opends.schema.syntaxes;

import static org.opends.server.schema.SchemaConstants.SYNTAX_CERTPAIR_NAME;
import static org.opends.server.schema.SchemaConstants.SYNTAX_CERTPAIR_OID;
import static org.opends.server.schema.SchemaConstants.SYNTAX_CERTPAIR_DESCRIPTION;
import org.opends.server.types.ByteSequence;
import org.opends.messages.MessageBuilder;
import org.opends.schema.SchemaUtils;

/**
 * This class implements the certificate pair attribute syntax.  This should be
 * restricted to holding only X.509 certificate pairs, but we will accept any
 * set of bytes.  It will be treated much like the octet string attribute
 * syntax.
 */
public class CertificatePairSyntax extends SyntaxDescription
{
  /**
   * Creates a new instance of this syntax.
   */
  public CertificatePairSyntax()
  {
    super(SYNTAX_CERTPAIR_OID, SYNTAX_CERTPAIR_NAME,
        SYNTAX_CERTPAIR_DESCRIPTION, SchemaUtils.RFC4512_ORIGIN);
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
    // All values will be acceptable for the certificate pair syntax.
    return true;
  }


  public boolean isHumanReadable() {
    return false;
  }
}
