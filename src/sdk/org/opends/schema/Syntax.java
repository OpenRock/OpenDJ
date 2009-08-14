package org.opends.schema;

import java.util.List;
import java.util.Map;

import org.opends.messages.MessageBuilder;
import org.opends.server.types.ByteSequence;
import org.opends.server.util.Validator;

/**
 * This class defines a data structure for storing and interacting
 * with an LDAP syntaxes, which constrain the structure of attribute values
 * stored in an LDAP directory, and determine the representation of attribute
 * and assertion values transferred in the LDAP protocol.
 * <p>
 * Syntax implementations must extend the <code>SyntaxImplementation</code>
 * class so they can be used by OpenDS to validate attribute values.
 * <p>
 * Where ordered sets of names, or extra properties are provided, the
 * ordering will be preserved when the associated fields are accessed
 * via their getters or via the {@link #toString()} methods.
 */
public abstract class Syntax extends AbstractSchemaElement
{
  protected final String oid;
  protected final String definition;

  protected Syntax(String oid, String description,
                   Map<String, List<String>> extraProperties,
                   String definition)
  {
    super(description, extraProperties);

    Validator.ensureNotNull(oid);
    this.oid = oid;

    if(definition != null)
    {
      this.definition = definition;
    }
    else
    {
      this.definition = buildDefinition();
    }
  }

  /**
   * Retrieves the OID for this attribute syntax.
   *
   * @return  The OID for this attribute syntax.
   */
  public final String getOID()
  {
    return oid;
  }

  /**
   * Retrieves the default equality matching rule that will be used
   * for attributes with this syntax.
   *
   * @return  The default equality matching rule that will be used for
   *          attributes with this syntax, or {@code null} if equality
   *          matches will not be allowed for this type by default.
   */
  public abstract EqualityMatchingRule getEqualityMatchingRule();



  /**
   * Retrieves the default ordering matching rule that will be used
   * for attributes with this syntax.
   *
   * @return  The default ordering matching rule that will be used for
   *          attributes with this syntax, or {@code null} if ordering
   *          matches will not be allowed for this type by default.
   */
  public abstract OrderingMatchingRule getOrderingMatchingRule();



  /**
   * Retrieves the default substring matching rule that will be used
   * for attributes with this syntax.
   *
   * @return  The default substring matching rule that will be used
   *          for attributes with this syntax, or {@code null} if
   *          substring matches will not be allowed for this type by
   *          default.
   */
  public abstract SubstringMatchingRule getSubstringMatchingRule();



  /**
   * Retrieves the default approximate matching rule that will be used
   * for attributes with this syntax.
   *
   * @return  The default approximate matching rule that will be used
   *          for attributes with this syntax, or {@code null} if
   *          approximate matches will not be allowed for this type by
   *          default.
   */
  public abstract ApproximateMatchingRule getApproximateMatchingRule();


  
  /**
   * Indicates whether this attribute syntax would likely be a
   * human readable string.
   * @return {@code true} if this attribute syntax would likely be a
   * human readable string or {@code false} if not.
   */
  public abstract boolean isHumanReadable();

  /**
   * Indicates whether the provided value is acceptable for use in an
   * attribute with this syntax.  If it is not, then the reason may be
   * appended to the provided buffer.
   *
   * @param  value          The value for which to make the
   *                        determination.
   * @param  invalidReason  The buffer to which the invalid reason
   *                        should be appended.
   *
   * @return  {@code true} if the provided value is acceptable for use
   *          with this syntax, or {@code false} if not.
   */
  public abstract boolean valueIsAcceptable(ByteSequence value,
                                            MessageBuilder invalidReason);


  /**
   * Retrieves the hash code for this attribute syntax.  It will be
   * calculated as the sum of the characters in the OID.
   *
   * @return  The hash code for this attribute syntax.
   */
  public final int hashCode()
  {
    return getOID().hashCode();
  }



  /**
   * Retrieves a string representation of this attribute syntax in the
   * format defined in RFC 2252.
   *
   * @return  A string representation of this attribute syntax in the
   *          format defined in RFC 2252.
   */
  public final String toString()
  {
    return definition;
  }

  protected void toStringContent(StringBuilder buffer)
  {
    buffer.append(oid);

    if ((description != null) && (description.length() > 0)) {
      buffer.append(" DESC '");
      buffer.append(description);
      buffer.append("'");
    }
  }

  protected abstract Syntax duplicate();
}
