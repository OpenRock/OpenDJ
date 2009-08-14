package org.opends.schema.syntaxes;

import static org.opends.server.schema.SchemaConstants.*;

import org.opends.ldap.DecodeException;
import org.opends.messages.MessageBuilder;
import org.opends.schema.Schema;
import org.opends.schema.SchemaUtils;
import org.opends.server.types.ByteSequence;
import org.opends.util.SubstringReader;

/**
 * This class defines the distinguished name attribute syntax, which is used for
 * attributes that hold distinguished names (DNs).  Equality and substring
 * matching will be allowed by default.
 */
public class DistinguishedNameSyntax extends AbstractSyntaxImplementation
{
  public String getName() {
    return SYNTAX_DN_NAME;
  }

  public boolean isHumanReadable() {
    return true;
  }

  public boolean valueIsAcceptable(Schema schema, ByteSequence value,
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
