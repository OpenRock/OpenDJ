package org.opends.schema.syntaxes;

import org.opends.server.loggers.debug.DebugTracer;
import static org.opends.server.loggers.debug.DebugLogger.getTracer;
import static org.opends.server.loggers.debug.DebugLogger.debugEnabled;
import static org.opends.server.schema.SchemaConstants.SYNTAX_OBJECTCLASS_OID;
import static org.opends.server.schema.SchemaConstants.SYNTAX_OBJECTCLASS_NAME;
import static org.opends.server.schema.SchemaConstants.SYNTAX_OBJECTCLASS_DESCRIPTION;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.DebugLogLevel;
import org.opends.messages.MessageBuilder;
import org.opends.schema.ObjectClass;
import org.opends.schema.SchemaUtils;
import org.opends.ldap.DecodeException;

/**
 * This class implements the object class description syntax, which is used to
 * hold objectclass definitions in the server schema.  The format of this
 * syntax is defined in RFC 2252.
 */
public class ObjectClassSyntax extends SyntaxImplementation
{
  /**
   * The tracer object for the debug logger.
   */
  private static final DebugTracer TRACER = getTracer();

  public ObjectClassSyntax()
  {
    super(SYNTAX_OBJECTCLASS_OID, SYNTAX_OBJECTCLASS_NAME,
        SYNTAX_OBJECTCLASS_DESCRIPTION, SchemaUtils.RFC4512_ORIGIN);
  }

  public boolean isHumanReadable() {
    return true;
  }

  public boolean valueIsAcceptable(ByteSequence value,
                                   MessageBuilder invalidReason)
  {
    // We'll use the decodeObjectClass method to determine if the value is
    // acceptable.
    try
    {
      ObjectClass.decode(value.toString());
      return true;
    }
    catch (DecodeException de)
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
