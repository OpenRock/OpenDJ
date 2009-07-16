package org.opends.schema.syntaxes;

import org.opends.server.types.ByteSequence;
import org.opends.server.util.Validator;
import org.opends.messages.MessageBuilder;
import org.opends.schema.MatchingRule;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 9, 2009
 * Time: 11:44:55 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class SyntaxDescription
{
  private final String oid;
  private final String name;
  private final String description;
  private final Map<String, List<String>> extraProperties;
  private final String definition;

  protected SyntaxDescription(String oid, String name,
                              String description,
                              Map<String, List<String>> extraProperties)
  {
    Validator.ensureNotNull(oid, name, description, extraProperties);
    this.oid = oid;
    this.name = name;
    this.description = description;
    this.extraProperties = extraProperties;
    this.definition = buildDefinition();
  }

  protected SyntaxDescription(String oid, String name,
                              String description,
                              Map<String, List<String>> extraProperties,
                              String definition)
  {
    Validator.ensureNotNull(oid, name, description, extraProperties);
    this.oid = oid;
    this.name = name;
    this.description = description;
    this.extraProperties = extraProperties;

    if(definition == null)
    {
      definition = buildDefinition();
    }
    this.definition = definition;
  }

  /**
   * Retrieves the common name for this attribute syntax.
   *
   * @return  The common name for this attribute syntax.
   */
  public final String getSyntaxName()
  {
    return name;
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
   * Retrieves a description for this attribute syntax.
   *
   * @return  A description for this attribute syntax.
   */
  public final String getDescription()
  {
    return description;
  }

  /**
   * Retrieves an iterable over the names of "extra" properties
   * associated with this schema definition.
   *
   * @return Returns an iterable over the names of "extra" properties
   *         associated with this schema definition.
   */
  public final Iterable<String> getExtraPropertyNames() {

    return extraProperties.keySet();
  }



  /**
   * Retrieves an iterable over the value(s) of the specified "extra"
   * property for this schema definition.
   *
   * @param name
   *          The name of the "extra" property for which to retrieve
   *          the value(s).
   * @return Returns an iterable over the value(s) of the specified
   *         "extra" property for this schema definition, or
   *         <code>null</code> if no such property is defined.
   */
  public final Iterable<String> getExtraProperty(String name) {

    return extraProperties.get(name);
  }

  public abstract SyntaxDescription customInstance(
      String description, Map<String, List<String>> extraProperties,
      String definition);

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
   * Indicates whether the provided object is equal to this attribute
   * syntax. The provided object will be considered equal to this
   * attribute syntax only if it is an attribute syntax with the same
   * OID.
   *
   * @param  o  The object for which to make the determination.
   *
   * @return  {@code true} if the provided object is equal to this
   *          attribute syntax, or {@code false} if it is not.
   */
  public final boolean equals(Object o)
  {
    if (o == null)
    {
      return false;
    }

    if (this == o)
    {
      return true;
    }

    if (! (o instanceof SyntaxDescription))
    {
      return false;
    }

    return getOID().equals(((SyntaxDescription) o).getOID());
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



  /**
   * Builds a string representation of this schema definition in the
   * form specified in RFC 2252.
   *
   * @return The string representation of this schema definition in
   *         the form specified in RFC 2252.
   */
  protected String buildDefinition()
  {
    StringBuilder buffer = new StringBuilder();

    buffer.append("( ");
    buffer.append(oid);

    if ((description == null) || (description.length() == 0))
    {
      buffer.append(" )");
    }
    else
    {
      buffer.append(" DESC '");
      buffer.append(description);
      buffer.append("' )");
    }

    if (!extraProperties.isEmpty()) {
      for (Map.Entry<String, List<String>> e : extraProperties
          .entrySet()) {

        String property = e.getKey();

        List<String> valueList = e.getValue();

        buffer.append(" ");
        buffer.append(property);

        if (valueList.size() == 1) {
          buffer.append(" '");
          buffer.append(valueList.get(0));
          buffer.append("'");
        } else {
          buffer.append(" ( ");

          for (String value : valueList) {
            buffer.append("'");
            buffer.append(value);
            buffer.append("' ");
          }

          buffer.append(")");
        }
      }
    }

    return buffer.toString();
  }
}
