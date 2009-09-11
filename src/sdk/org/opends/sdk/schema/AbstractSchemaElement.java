package org.opends.sdk.schema;

import java.util.List;
import java.util.Map;

import org.opends.server.util.Validator;
import org.opends.messages.Message;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 14, 2009
 * Time: 3:24:48 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractSchemaElement
{
  // The description for this definition.
  protected final String description;

  // The set of additional name-value pairs.
  protected final Map<String, List<String>> extraProperties;

  protected AbstractSchemaElement(String description,
                                  Map<String, List<String>> extraProperties)
  {
    Validator.ensureNotNull(description, extraProperties);
    this.description = description;
    this.extraProperties = extraProperties;
  }

  /**
   * Retrieves the description for this schema definition.
   *
   * @return The description for this schema definition.
   */
  public final String getDescription() {

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

    toStringContent(buffer);

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

    buffer.append(" )");

    return buffer.toString();
  }

  /**
   * Appends a string representation of this schema definition's
   * non-generic properties to the provided buffer.
   *
   * @param buffer
   *          The buffer to which the information should be appended.
   */
  protected abstract void toStringContent(StringBuilder buffer);

  protected abstract void validate(List<Message> warnings) throws SchemaException;
}
