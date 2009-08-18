package org.opends.schema.syntaxes;

import static org.opends.server.loggers.debug.DebugLogger.debugEnabled;
import static org.opends.server.loggers.debug.DebugLogger.getTracer;
import static org.opends.server.schema.SchemaConstants.SYNTAX_OID_NAME;

import org.opends.ldap.DecodeException;
import org.opends.messages.MessageBuilder;
import org.opends.schema.Schema;
import org.opends.schema.SchemaUtils;
import org.opends.server.loggers.debug.DebugTracer;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.DebugLogLevel;
import org.opends.util.SubstringReader;

/**
 * This class defines the OID syntax, which holds either an identifier name or
 * a numeric OID.  Equality and substring matching will be allowed by default.
 */
public class OIDSyntax extends AbstractSyntaxImplementation
{
  /**
   * The tracer object for the debug logger.
   */
  private static final DebugTracer TRACER = getTracer();
  
  public String getName() {
    return SYNTAX_OID_NAME;
  }

  public boolean isHumanReadable() {
    return true;
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
    try
    {
      SchemaUtils.readOID(new SubstringReader(value.toString()));
      return true;
    }
    catch(DecodeException de)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, de);
      }

      invalidReason.append(de.getMessageObject());
      return false;
    }
  }
}
