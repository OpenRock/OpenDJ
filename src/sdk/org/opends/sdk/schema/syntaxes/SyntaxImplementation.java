package org.opends.sdk.schema.syntaxes;

import org.opends.messages.MessageBuilder;
import org.opends.sdk.schema.Schema;
import org.opends.server.types.ByteSequence;

/**
 * This class defines the set of methods and structures that must be
 * implemented to define a new attribute syntax.
 */
public interface SyntaxImplementation
{
  /**
   * Retrieves the common name for this attribute syntax.
   *
   * @return  The common name for this attribute syntax.
   */
  public String getName();

  /**
   * Retrieves the default equality matching rule that will be used
   * for attributes with this syntax.
   *
   * @return  The default equality matching rule that will be used for
   *          attributes with this syntax, or {@code null} if equality
   *          matches will not be allowed for this type by default.
   */
  public String getEqualityMatchingRule();

  /**
   * Retrieves the default ordering matching rule that will be used
   * for attributes with this syntax.
   *
   * @return  The default ordering matching rule that will be used for
   *          attributes with this syntax, or {@code null} if ordering
   *          matches will not be allowed for this type by default.
   */
  public String getOrderingMatchingRule();

  /**
   * Retrieves the default substring matching rule that will be used
   * for attributes with this syntax.
   *
   * @return  The default substring matching rule that will be used
   *          for attributes with this syntax, or {@code null} if
   *          substring matches will not be allowed for this type by
   *          default.
   */
  public String getSubstringMatchingRule();

  /**
   * Retrieves the default approximate matching rule that will be used
   * for attributes with this syntax.
   *
   * @return  The default approximate matching rule that will be used
   *          for attributes with this syntax, or {@code null} if
   *          approximate matches will not be allowed for this type by
   *          default.
   */
  public String getApproximateMatchingRule();

  /**
   * Indicates whether this attribute syntax would likely be a
   * human readable string.
   * @return {@code true} if this attribute syntax would likely be a
   * human readable string or {@code false} if not.
   */
  boolean isHumanReadable();



  /**
   * Indicates whether this attribute syntax requires that values must
   * be encoded using the Basic Encoding Rules (BER) used by X.500
   * directories and always include the {@code binary} attribute
   * description option.
   *
   * @return {@code true} this attribute syntax requires that values
   *         must be BER encoded and always include the {@code binary}
   *         attribute description option, or {@code false} if not.
   * @see <a href="http://tools.ietf.org/html/rfc4522">RFC 4522 -
   *      Lightweight Directory Access Protocol (LDAP): The Binary
   *      Encoding Option </a>
   */
  public boolean isBEREncodingRequired();

  /**
   * Indicates whether the provided value is acceptable for use in an
   * attribute with this syntax.  If it is not, then the reason may be
   * appended to the provided buffer.
   *
   * @param schema The schema in which this syntax is defined.
   *@param  value          The value for which to make the
   *                        determination.
   * @param  invalidReason  The buffer to which the invalid reason
 *                        should be appended.
 * @return  {@code true} if the provided value is acceptable for use
   *          with this syntax, or {@code false} if not.
   */
  boolean valueIsAcceptable(Schema schema, ByteSequence value,
                            MessageBuilder invalidReason);
}
