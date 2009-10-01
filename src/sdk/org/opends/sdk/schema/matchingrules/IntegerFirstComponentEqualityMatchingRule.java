package org.opends.sdk.schema.matchingrules;

import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_EMPTY_VALUE;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_EXPECTED_OPEN_PARENTHESIS;
import static org.opends.messages.SchemaMessages.ERR_EMR_INTFIRSTCOMP_FIRST_COMPONENT_NOT_INT;
import static org.opends.server.loggers.debug.DebugLogger.debugEnabled;
import static org.opends.server.loggers.debug.DebugLogger.getTracer;

import org.opends.messages.Message;
import org.opends.sdk.Assertion;
import org.opends.sdk.ConditionResult;
import org.opends.sdk.DecodeException;
import org.opends.sdk.schema.Schema;
import org.opends.sdk.schema.SchemaUtils;
import org.opends.sdk.util.SubstringReader;
import org.opends.server.loggers.debug.DebugTracer;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import org.opends.server.types.DebugLogLevel;

/**
 * This class implements the integerFirstComponentMatch matching rule defined in
 * X.520 and referenced in RFC 2252.  This rule is intended for use with
 * attributes whose values contain a set of parentheses enclosing a
 * space-delimited set of names and/or name-value pairs (like attribute type or
 * objectclass descriptions) in which the "first component" is the first item
 * after the opening parenthesis.
 */
public class IntegerFirstComponentEqualityMatchingRule
    extends AbstractMatchingRuleImplementation
{
  /**
   * The tracer object for the debug logger.
   */
  private static final DebugTracer TRACER = getTracer();

  public ByteString normalizeAttributeValue(Schema schema, ByteSequence value)
      throws DecodeException
  {
    String definition = value.toString();
    SubstringReader reader = new SubstringReader(definition);

    // We'll do this a character at a time.  First, skip over any leading
    // whitespace.
    reader.skipWhitespaces();

    if (reader.remaining() <= 0)
    {
      // This means that the value was empty or contained only whitespace.
      // That is illegal.
      Message message = ERR_ATTR_SYNTAX_EMPTY_VALUE.get();
      throw new DecodeException(message);
    }


    // The next character must be an open parenthesis.  If it is not,
    // then that is an error.
    char c = reader.read();
    if (c != '(')
    {
      Message message = ERR_ATTR_SYNTAX_EXPECTED_OPEN_PARENTHESIS.
          get(definition, (reader.pos()-1), String.valueOf(c));
      throw new DecodeException(message);
    }


    // Skip over any spaces immediately following the opening parenthesis.
    reader.skipWhitespaces();

    // The next set of characters must be the OID.
    return ByteString.valueOf(SchemaUtils.readRuleID(reader));
  }

  @Override
  public Assertion getAssertion(Schema schema, ByteSequence value)
      throws DecodeException
  {
    try
    {
      String definition = value.toString();
      SubstringReader reader = new SubstringReader(definition);
      final int intValue = SchemaUtils.readRuleID(reader);

      return new Assertion()
      {
        public ConditionResult matches(ByteString attributeValue) {
          return intValue == attributeValue.toInt() ?
              ConditionResult.TRUE : ConditionResult.FALSE;
        }
      };
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message = ERR_EMR_INTFIRSTCOMP_FIRST_COMPONENT_NOT_INT.get(
          value.toString());
      throw new DecodeException(message);
    }

  }
}
