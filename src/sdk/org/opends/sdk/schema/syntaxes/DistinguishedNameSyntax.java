package org.opends.sdk.schema.syntaxes;

import static org.opends.server.schema.SchemaConstants.SYNTAX_DN_NAME;
import static org.opends.server.schema.SchemaConstants.EMR_DN_OID;
import static org.opends.server.schema.SchemaConstants.SMR_CASE_IGNORE_OID;

import org.opends.messages.MessageBuilder;
import org.opends.sdk.DecodeException;
import org.opends.sdk.schema.Schema;
import org.opends.sdk.schema.SchemaUtils;
import org.opends.sdk.util.SubstringReader;
import org.opends.server.types.ByteSequence;

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

  @Override
  public String getEqualityMatchingRule() {
    return EMR_DN_OID;
  }

  @Override
  public String getSubstringMatchingRule() {
    return SMR_CASE_IGNORE_OID;
  }
}
