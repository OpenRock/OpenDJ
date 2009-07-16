package org.opends.schema.syntaxes;

import static org.opends.server.util.ServerConstants.SCHEMA_PROPERTY_ORIGIN;
import static org.opends.server.schema.SchemaConstants.SYNTAX_FAX_OID;
import static org.opends.server.schema.SchemaConstants.SYNTAX_FAX_NAME;
import static org.opends.server.schema.SchemaConstants.SYNTAX_FAX_DESCRIPTION;
import org.opends.server.types.ByteSequence;
import org.opends.messages.MessageBuilder;

import java.util.List;
import java.util.Map;
import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 15, 2009
 * Time: 3:00:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class FaxSyntax extends SyntaxDescription
{
  public FaxSyntax(Map<String, List<String>> extraProperties)
  {
    super(SYNTAX_FAX_OID, SYNTAX_FAX_NAME,
        SYNTAX_FAX_DESCRIPTION, extraProperties);
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
