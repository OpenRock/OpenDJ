package org.opends.sdk.schema.matchingrules;

import static org.opends.server.util.StaticUtils.toLowerCase;

import org.opends.sdk.Assertion;
import org.opends.sdk.DecodeException;
import org.opends.sdk.schema.*;
import org.opends.sdk.util.SubstringReader;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import org.opends.messages.Message;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_EMPTY_VALUE;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_EXPECTED_OPEN_PARENTHESIS;

/**
 * This class implements the objectIdentifierFirstComponentMatch matching rule
 * defined in X.520 and referenced in RFC 2252.  This rule is intended for use
 * with attributes whose values contain a set of parentheses enclosing a
 * space-delimited set of names and/or name-value pairs (like attribute type or
 * objectclass descriptions) in which the "first component" is the first item
 * after the opening parenthesis.
 */
public class ObjectIdentifierFirstComponentEqualityMatchingRule
    extends AbstractMatchingRuleImplementation
{
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


    // The next character must be an open parenthesis.  If it is not, then
    // that is an error.
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
    String normalized =
        ObjectIdentifierEqualityMatchingRule.resolveNames(schema,
            SchemaUtils.readOID(reader));
    return ByteString.valueOf(normalized);
  }

  @Override
  public Assertion getAssertion(Schema schema, ByteSequence value)
      throws DecodeException {
    String definition = value.toString();
    SubstringReader reader = new SubstringReader(definition);
    String normalized =
        ObjectIdentifierEqualityMatchingRule.resolveNames(schema,
            SchemaUtils.readOID(reader));

    return new ObjectIdentifierEqualityMatchingRule.OIDAssertion(normalized);
  }
}
