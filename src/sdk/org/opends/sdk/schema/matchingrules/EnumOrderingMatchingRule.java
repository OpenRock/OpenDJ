package org.opends.sdk.schema.matchingrules;

import org.opends.sdk.schema.syntaxes.EnumSyntax;
import org.opends.sdk.schema.Schema;
import org.opends.sdk.DecodeException;
import org.opends.sdk.util.Validator;
import org.opends.server.types.ByteString;
import org.opends.server.types.ByteSequence;
import static org.opends.messages.SchemaMessages.WARN_ATTR_SYNTAX_LDAPSYNTAX_ENUM_INVALID_VALUE;

/**
 * This class is the ordering matching rule implementation for an enum
 * syntax implmentation. The ordering is determined by the order of the entries
 * in the X-ENUM extension value.
 */
public final class EnumOrderingMatchingRule
    extends AbstractOrderingMatchingRuleImplementation
{
  private final EnumSyntax syntax;

  public EnumOrderingMatchingRule(EnumSyntax syntax) {
    Validator.ensureNotNull(syntax);
    this.syntax = syntax;
  }

  public ByteString normalizeAttributeValue(Schema schema, ByteSequence value)
      throws DecodeException
  {
    int index = syntax.indexOf(value);
    if(index < 0)
    {
      throw new DecodeException(
          WARN_ATTR_SYNTAX_LDAPSYNTAX_ENUM_INVALID_VALUE.get(value.toString(),
              syntax.getName()));
    }
    return ByteString.valueOf(index);
  }


}
