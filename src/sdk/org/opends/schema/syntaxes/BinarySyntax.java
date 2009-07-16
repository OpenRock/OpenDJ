package org.opends.schema.syntaxes;

import org.opends.server.types.ByteSequence;
import static org.opends.server.schema.SchemaConstants.*;
import static org.opends.server.util.ServerConstants.SCHEMA_PROPERTY_ORIGIN;
import org.opends.messages.MessageBuilder;

import java.util.List;
import java.util.Map;
import java.util.Collections;

/**
 * This class defines the binary attribute syntax, which is essentially a byte
 * array using very strict matching.  Equality, ordering, and substring matching
 * will be allowed by default.
 */
public class BinarySyntax extends SyntaxDescription
{
  /**
   * Creates a new instance of this syntax.
   */
  public BinarySyntax(Map<String, List<String>> extraProperties)
  {
    super(SYNTAX_BINARY_OID, SYNTAX_BINARY_NAME,
        SYNTAX_BINARY_DESCRIPTION, extraProperties);
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
    // All values will be acceptable for the binary syntax.
    return true;
  }

  public boolean isHumanReadable()
  {
    return false;
  }
}
