package org.opends.schema.syntaxes;

import static org.opends.server.util.ServerConstants.SCHEMA_PROPERTY_ORIGIN;
import static org.opends.server.schema.SchemaConstants.SYNTAX_PROTOCOL_INFORMATION_OID;
import static org.opends.server.schema.SchemaConstants.SYNTAX_PROTOCOL_INFORMATION_NAME;
import static org.opends.server.schema.SchemaConstants.SYNTAX_PROTOCOL_INFORMATION_DESCRIPTION;
import org.opends.server.types.ByteSequence;
import org.opends.messages.MessageBuilder;

import java.util.List;
import java.util.Map;
import java.util.Collections;

/**
 * This class implements the protocol information attribute syntax, which is
 * being deprecated.  As such, this implementation behaves exactly like the
 * directory string syntax.
 */
public class ProtocolInformationSyntax extends SyntaxDescription
{
  public ProtocolInformationSyntax(Map<String, List<String>> extraProperties)
  {
    super(SYNTAX_PROTOCOL_INFORMATION_OID,
        SYNTAX_PROTOCOL_INFORMATION_NAME,
        SYNTAX_PROTOCOL_INFORMATION_DESCRIPTION,
        extraProperties);
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
