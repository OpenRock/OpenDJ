package org.opends.schema.syntaxes;

import org.opends.server.types.ByteSequence;
import static org.opends.server.schema.SchemaConstants.SYNTAX_DN_DESCRIPTION;
import static org.opends.server.schema.SchemaConstants.SYNTAX_DN_OID;
import static org.opends.server.schema.SchemaConstants.SYNTAX_DN_NAME;
import org.opends.messages.MessageBuilder;
import org.opends.messages.Message;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_DN_END_WITH_ATTR_NAME;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_DN_NO_EQUAL;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_DN_INVALID_CHAR;
import org.opends.util.SubstringReader;
import org.opends.schema.SchemaUtils;
import org.opends.ldap.DecodeException;

/**
 * This class defines the distinguished name attribute syntax, which is used for
 * attributes that hold distinguished names (DNs).  Equality and substring
 * matching will be allowed by default.
 */
public class DistinguishedNameSyntax extends SyntaxImplementation
{
    /**
   * Creates a new instance of this syntax.
   */
  public DistinguishedNameSyntax()
  {
    super(SYNTAX_DN_OID, SYNTAX_DN_NAME,
        SYNTAX_DN_DESCRIPTION, SchemaUtils.RFC4512_ORIGIN);
  }

  public boolean isHumanReadable() {
    return true;
  }

  public boolean valueIsAcceptable(ByteSequence value,
                                   MessageBuilder invalidReason)
  {
    SubstringReader reader = new SubstringReader(value.toString());

    try
    {
      SchemaUtils.readDN(reader);
    }
    catch(DecodeException de)
    {
      invalidReason.append(de.getMessageObject());
      return false;
    }

    return true;
  }
}
