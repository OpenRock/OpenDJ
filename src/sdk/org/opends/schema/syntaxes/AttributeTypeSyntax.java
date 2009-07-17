package org.opends.schema.syntaxes;

import static org.opends.server.schema.SchemaConstants.*;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.DebugLogLevel;
import static org.opends.server.loggers.debug.DebugLogger.debugEnabled;
import static org.opends.server.loggers.debug.DebugLogger.getTracer;
import org.opends.server.loggers.debug.DebugTracer;
import org.opends.schema.AttributeType;
import org.opends.schema.SchemaUtils;
import org.opends.messages.MessageBuilder;
import org.opends.ldap.DecodeException;

/**
 * This class defines the attribute type description syntax, which is used to
 * hold attribute type definitions in the server schema.  The format of this
 * syntax is defined in RFC 2252.
 */
public class AttributeTypeSyntax extends SyntaxDescription
{
  /**
   * The tracer object for the debug logger.
   */
  private static final DebugTracer TRACER = getTracer();

  /**
   * Creates a new instance of this syntax.
   */
  public AttributeTypeSyntax()
  {
    super(SYNTAX_ATTRIBUTE_TYPE_OID, SYNTAX_ATTRIBUTE_TYPE_NAME,
        SYNTAX_ATTRIBUTE_TYPE_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN);
  }

  public boolean isHumanReadable() {
    return true;
  }

  public boolean valueIsAcceptable(ByteSequence value,
                                   MessageBuilder invalidReason)
  {
    // We'll use the decodeAttributeType method to determine if the value is
    // acceptable.
    try
    {
      AttributeType.decode(value.toString());
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
