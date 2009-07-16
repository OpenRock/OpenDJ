package org.opends.schema.syntaxes;

import static org.opends.server.schema.SchemaConstants.SYNTAX_SUPPORTED_ALGORITHM_OID;
import static org.opends.server.schema.SchemaConstants.SYNTAX_SUPPORTED_ALGORITHM_NAME;
import static org.opends.server.schema.SchemaConstants.SYNTAX_SUPPORTED_ALGORITHM_DESCRIPTION;
import org.opends.server.types.ByteSequence;
import org.opends.messages.MessageBuilder;

import java.util.List;
import java.util.Map;

/**
 * This class implements the supported algorithm attribute syntax.  This should
 * be restricted to holding only X.509 supported algorithms, but we will accept
 * any set of bytes.  It will be treated much like the octet string attribute
 * syntax.
 */
public class SupportedAlgorithmSyntax extends SyntaxDescription
{
  public SupportedAlgorithmSyntax(Map<String, List<String>> extraProperties)
  {
    super(SYNTAX_SUPPORTED_ALGORITHM_OID,
        SYNTAX_SUPPORTED_ALGORITHM_NAME,
        SYNTAX_SUPPORTED_ALGORITHM_DESCRIPTION,
        extraProperties);
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
    // All values will be acceptable for the supported algorithm syntax.
    return true;
  }
}

