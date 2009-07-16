package org.opends.schema.syntaxes;

import org.opends.server.loggers.debug.DebugTracer;
import static org.opends.server.loggers.debug.DebugLogger.getTracer;
import static org.opends.server.loggers.debug.DebugLogger.debugEnabled;
import static org.opends.server.schema.SchemaConstants.*;
import static org.opends.server.schema.SchemaConstants.SYNTAX_DIT_CONTENT_RULE_DESCRIPTION;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.DebugLogLevel;
import static org.opends.server.util.ServerConstants.SCHEMA_PROPERTY_ORIGIN;
import org.opends.messages.MessageBuilder;
import org.opends.schema.DITContentRule;
import org.opends.ldap.DecodeException;

import java.util.List;
import java.util.Map;
import java.util.Collections;

/**
 * This class implements the DIT content rule description syntax, which is used
 * to hold DIT content rule definitions in the server schema.  The format of
 * this syntax is defined in RFC 2252.
 */
public class DITContentRuleSyntax extends SyntaxDescription
{
  /**
   * Creates a new instance of this syntax.
   */
  public DITContentRuleSyntax(Map<String, List<String>> extraProperties)
  {
    super(SYNTAX_DIT_CONTENT_RULE_OID, SYNTAX_DIT_CONTENT_RULE_NAME,
        SYNTAX_DIT_CONTENT_RULE_DESCRIPTION, extraProperties);
  }

  /**
   * The tracer object for the debug logger.
   */
  private static final DebugTracer TRACER = getTracer();


  /**
   * {@inheritDoc}
   */
  public boolean valueIsAcceptable(ByteSequence value,
                                   MessageBuilder invalidReason)
  {
    // We'll use the decodeDITContentRule method to determine if the value is
    // acceptable.
    try
    {
      DITContentRule.decode(value.toString());
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

  public boolean isHumanReadable() {
    return true;
  }
}


