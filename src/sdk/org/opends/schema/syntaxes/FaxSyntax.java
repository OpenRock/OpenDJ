package org.opends.schema.syntaxes;

import static org.opends.server.schema.SchemaConstants.SYNTAX_FAX_OID;
import static org.opends.server.schema.SchemaConstants.SYNTAX_FAX_NAME;
import static org.opends.server.schema.SchemaConstants.SYNTAX_FAX_DESCRIPTION;
import org.opends.server.types.ByteSequence;
import org.opends.messages.MessageBuilder;
import org.opends.schema.SchemaUtils;

/**
 * This class implements the fax attribute syntax.  This should be restricted to
 * holding only fax message contents, but we will accept any set of bytes.  It
 * will be treated much like the octet string attribute syntax.
 */
public class FaxSyntax extends SyntaxImplementation
{
  public FaxSyntax()
  {
    super(SYNTAX_FAX_OID, SYNTAX_FAX_NAME,
        SYNTAX_FAX_DESCRIPTION, SchemaUtils.RFC4512_ORIGIN);
  }

  public boolean isHumanReadable() {
    return false;
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
    // All values will be acceptable for the fax syntax.
    return true;
  }
}
