package org.opends.schema.syntaxes;

import static org.opends.server.schema.SchemaConstants.SYNTAX_OCTET_STRING_OID;
import static org.opends.server.schema.SchemaConstants.SYNTAX_OCTET_STRING_NAME;
import static org.opends.server.schema.SchemaConstants.SYNTAX_OCTET_STRING_DESCRIPTION;
import org.opends.server.types.ByteSequence;
import org.opends.messages.MessageBuilder;
import org.opends.schema.SchemaUtils;

/**
 * This class implements the octet string attribute syntax, which is equivalent
 * to the binary syntax and should be considered a replacement for it.
 * Equality, ordering, and substring matching will be allowed by default.
 */
public class OctetStringSyntax extends SyntaxDescription
{
  public OctetStringSyntax()
  {
    super(SYNTAX_OCTET_STRING_OID, SYNTAX_OCTET_STRING_NAME,
        SYNTAX_OCTET_STRING_DESCRIPTION, SchemaUtils.RFC4512_ORIGIN);
  }

  public boolean isHumanReadable() {
    return true;
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
    // All values will be acceptable for the octet string syntax.
    return true;
  }

}
