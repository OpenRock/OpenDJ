package org.opends.schema.syntaxes;

import org.opends.server.types.ByteSequence;
import org.opends.server.types.DebugLogLevel;
import static org.opends.server.loggers.debug.DebugLogger.debugEnabled;
import static org.opends.server.loggers.debug.DebugLogger.getTracer;
import org.opends.server.loggers.debug.DebugTracer;
import static org.opends.server.util.ServerConstants.SCHEMA_PROPERTY_ORIGIN;
import static org.opends.server.schema.SchemaConstants.*;
import org.opends.messages.MessageBuilder;
import org.opends.schema.DITStructuralRule;
import org.opends.ldap.DecodeException;

import java.util.List;
import java.util.Map;
import java.util.Collections;

/**
 * This class implements the DIT structure rule description syntax, which is
 * used to hold DIT structure rule definitions in the server schema.  The format
 * of this syntax is defined in RFC 2252.
 */
public class DITStructureRuleSyntax extends SyntaxDescription
{
  /**
   * The tracer object for the debug logger.
   */
  private static final DebugTracer TRACER = getTracer();

  /**
   * Creates a new instance of this syntax.
   */
  public DITStructureRuleSyntax(Map<String, List<String>> extraProperties)
  {
    super(SYNTAX_DIT_STRUCTURE_RULE_OID, SYNTAX_DIT_STRUCTURE_RULE_NAME,
        SYNTAX_DIT_STRUCTURE_RULE_DESCRIPTION, extraProperties);
  }

  public boolean isHumanReadable() {
    return true;
  }

  public boolean valueIsAcceptable(ByteSequence value, MessageBuilder invalidReason)
  {
    // We'll use the decodeDITStructureRule method to determine if the value is
    // acceptable.
    try
    {
      DITStructuralRule.decode(value.toString());
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
