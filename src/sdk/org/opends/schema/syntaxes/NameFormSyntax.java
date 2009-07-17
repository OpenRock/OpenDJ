package org.opends.schema.syntaxes;

import org.opends.server.loggers.debug.DebugTracer;
import static org.opends.server.loggers.debug.DebugLogger.getTracer;
import static org.opends.server.loggers.debug.DebugLogger.debugEnabled;
import static org.opends.server.schema.SchemaConstants.SYNTAX_NAME_FORM_OID;
import static org.opends.server.schema.SchemaConstants.SYNTAX_NAME_FORM_NAME;
import static org.opends.server.schema.SchemaConstants.SYNTAX_NAME_FORM_DESCRIPTION;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.DebugLogLevel;
import org.opends.messages.MessageBuilder;
import org.opends.schema.NameForm;
import org.opends.schema.SchemaUtils;
import org.opends.ldap.DecodeException;

/**
 * Creates a new instance of this syntax.  Note that the only thing that
 * should be done here is to invoke the default constructor for the
 * superclass.  All initialization should be performed in the
 * <CODE>initializeSyntax</CODE> method.
 */
public class NameFormSyntax extends SyntaxDescription
{
  /**
   * The tracer object for the debug logger.
   */
  private static final DebugTracer TRACER = getTracer();

  public NameFormSyntax()
  {
    super(SYNTAX_NAME_FORM_OID, SYNTAX_NAME_FORM_NAME,
        SYNTAX_NAME_FORM_DESCRIPTION, SchemaUtils.RFC4512_ORIGIN);
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
      NameForm.decode(value.toString());
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
