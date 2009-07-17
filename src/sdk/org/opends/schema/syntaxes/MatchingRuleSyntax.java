package org.opends.schema.syntaxes;

import static org.opends.server.schema.SchemaConstants.SYNTAX_MATCHING_RULE_OID;
import static org.opends.server.schema.SchemaConstants.SYNTAX_MATCHING_RULE_NAME;
import static org.opends.server.schema.SchemaConstants.SYNTAX_MATCHING_RULE_DESCRIPTION;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.DebugLogLevel;
import static org.opends.server.loggers.debug.DebugLogger.debugEnabled;
import static org.opends.server.loggers.debug.DebugLogger.getTracer;
import org.opends.server.loggers.debug.DebugTracer;
import org.opends.messages.MessageBuilder;
import org.opends.ldap.DecodeException;
import org.opends.schema.SchemaUtils;

/**
 * This class implements the matching rule description syntax, which is used to
 * hold matching rule definitions in the server schema.  The format of this
 * syntax is defined in RFC 2252.
 */
public class MatchingRuleSyntax extends SyntaxImplementation
{
  /**
   * The tracer object for the debug logger.
   */
  private static final DebugTracer TRACER = getTracer();

  public MatchingRuleSyntax()
  {
    super(SYNTAX_MATCHING_RULE_OID, SYNTAX_MATCHING_RULE_NAME,
        SYNTAX_MATCHING_RULE_DESCRIPTION, SchemaUtils.RFC4512_ORIGIN);
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
  @Override
  public boolean valueIsAcceptable(ByteSequence value,
                                   MessageBuilder invalidReason)
  {
    // We'll use the decodeAttributeType method to determine if the value is
    // acceptable.
    try
    {
      //MatchingRule.decode(value.toString());
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
