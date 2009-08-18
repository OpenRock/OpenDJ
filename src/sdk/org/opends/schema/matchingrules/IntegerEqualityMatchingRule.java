package org.opends.schema.matchingrules;

import static org.opends.server.loggers.debug.DebugLogger.debugEnabled;
import static org.opends.server.loggers.debug.DebugLogger.getTracer;

import org.opends.schema.Schema;
import org.opends.server.loggers.debug.DebugTracer;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import org.opends.server.types.DebugLogLevel;

/**
 * This class defines the integerMatch matching rule defined in X.520 and
 * referenced in RFC 2252.
 */
public class IntegerEqualityMatchingRule
    extends AbstractEqualityMatchingRuleImplementation
{
  /**
   * The tracer object for the debug logger.
   */
  private static final DebugTracer TRACER = getTracer();

  public ByteSequence normalizeAttributeValue(Schema schema, ByteSequence value)
  {
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
