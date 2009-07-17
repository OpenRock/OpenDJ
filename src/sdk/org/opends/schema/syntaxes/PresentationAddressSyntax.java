package org.opends.schema.syntaxes;

import static org.opends.server.schema.SchemaConstants.SYNTAX_PRESENTATION_ADDRESS_OID;
import static org.opends.server.schema.SchemaConstants.SYNTAX_PRESENTATION_ADDRESS_NAME;
import static org.opends.server.schema.SchemaConstants.SYNTAX_PRESENTATION_ADDRESS_DESCRIPTION;
import org.opends.server.types.ByteSequence;
import org.opends.messages.MessageBuilder;
import org.opends.schema.SchemaUtils;

/**
 * This class implements the presentation address attribute syntax, which is
 * defined in RFC 1278.  However, because this LDAP syntax is being deprecated,
 * this implementation behaves exactly like the directory string syntax.
 */
public class PresentationAddressSyntax extends SyntaxDescription
{
  public PresentationAddressSyntax()
  {
    super(SYNTAX_PRESENTATION_ADDRESS_OID,
        SYNTAX_PRESENTATION_ADDRESS_NAME,
        SYNTAX_PRESENTATION_ADDRESS_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN);
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
    // We will accept any value for this syntax.
    return true;
  }
}
