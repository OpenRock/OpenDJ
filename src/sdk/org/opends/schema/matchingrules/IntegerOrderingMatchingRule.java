package org.opends.schema.matchingrules;

import org.opends.schema.SchemaUtils;
import static org.opends.server.schema.SchemaConstants.OMR_INTEGER_NAME;
import static org.opends.server.schema.SchemaConstants.OMR_INTEGER_OID;
import static org.opends.server.schema.SchemaConstants.SYNTAX_INTEGER_OID;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import org.opends.server.types.DebugLogLevel;
import static org.opends.server.loggers.debug.DebugLogger.debugEnabled;
import static org.opends.server.loggers.debug.DebugLogger.getTracer;
import org.opends.server.loggers.debug.DebugTracer;

import java.util.Collections;

/**
 * This class defines the integerOrderingMatch matching rule defined in X.520
 * and referenced in RFC 4519.
 */
public class IntegerOrderingMatchingRule
    extends OrderingMatchingRuleImplementation
{
  /**
   * The tracer object for the debug logger.
   */
  private static final DebugTracer TRACER = getTracer();

  public IntegerOrderingMatchingRule()
  {
    super(OMR_INTEGER_OID,
        Collections.singletonList(OMR_INTEGER_NAME),
        "",
        false,
        SYNTAX_INTEGER_OID,
        SchemaUtils.RFC4512_ORIGIN);
  }

  public ByteSequence normalizeAttributeValue(ByteSequence value) {
    try
    {
      return ByteString.valueOf(
          Integer.parseInt(value.toString()));
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      return value;
    }
  }
}
