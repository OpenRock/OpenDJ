package org.opends.schema.syntaxes;

import org.opends.server.loggers.debug.DebugTracer;
import static org.opends.server.loggers.debug.DebugLogger.getTracer;
import static org.opends.server.loggers.debug.DebugLogger.debugEnabled;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.DebugLogLevel;
import static org.opends.server.schema.SchemaConstants.*;
import org.opends.schema.SchemaUtils;
import org.opends.schema.Syntax;
import org.opends.messages.MessageBuilder;
import org.opends.ldap.DecodeException;

/**
 * This class defines the LDAP syntax description syntax, which is used to
 * hold attribute syntax definitions in the schema.  The format of this
 * syntax is defined in RFC 2252.
 */
public class LDAPSyntaxDescriptionSyntax extends SyntaxImplementation
{
  /**
   * The tracer object for the debug logger.
   */
  private static final DebugTracer TRACER = getTracer();

  public LDAPSyntaxDescriptionSyntax()
  {
    super(SYNTAX_LDAP_SYNTAX_OID, SYNTAX_LDAP_SYNTAX_NAME,
        SYNTAX_LDAP_SYNTAX_DESCRIPTION, SchemaUtils.RFC4512_ORIGIN);
  }

  public boolean isHumanReadable() {
    return true;
  }

  public boolean valueIsAcceptable(ByteSequence value,
                                   MessageBuilder invalidReason)
  {
    // We'll use the decodeNameForm method to determine if the value is
    // acceptable.
    try
    {
      Syntax.decode(value.toString());
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
