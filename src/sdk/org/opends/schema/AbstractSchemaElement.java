package org.opends.schema;

import org.opends.server.util.Validator;

import java.util.List;
import java.util.Map;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 14, 2009
 * Time: 3:24:48 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractSchemaElement
{
  // The set of user defined names for this definition.
  protected final List<String> names;

  // The description for this definition.
  protected final String description;

  // Indicates whether this definition is declared "obsolete".
  protected final boolean isObsolete;

  // The set of additional name-value pairs.
  protected final Map<String, List<String>> extraProperties;

  protected AbstractSchemaElement(List<String> names,
                                  String description,
                                  boolean obsolete,
                                  Map<String,
                                  List<String>> extraProperties)
  {
    Validator.ensureNotNull(names, description, obsolete, extraProperties);
    this.names = names;
    this.description = description;
    this.isObsolete = obsolete;
    this.extraProperties = extraProperties;
  }

  /**
   * Retrieves an iterable over the set of user-defined names that may
   * be used to reference this schema definition.
   *
   * @return Returns an iterable over the set of user-defined names
   *         that may be used to reference this schema definition.
   */
  public Iterable<String> getNames() {
    return names;
  }

  /**
   * Indicates whether this schema definition has the specified name.
   *
   * @param name
   *          The name for which to make the determination.
   * @return <code>true</code> if the specified name is assigned to
   *         this schema definition, or <code>false</code> if not.
   */
  public boolean hasName(String name) {
    for(String n : names)
    {
      if(n.equalsIgnoreCase(name))
      {
        return true;
      }
    }
    return false;
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
   * Indicates whether this schema definition is declared "obsolete".
   *
   * @return <code>true</code> if this schema definition is declared
   *         "obsolete", or <code>false</code> if not.
   */
  public final boolean isObsolete()
  {
    return isObsolete;
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

  @Override
  public int hashCode() {
    return getIdentifier().hashCode();
  }

  /**
   * Two schema elements are considered equal if they have the
   * same identifier names.
   *
   * @param   obj   the reference object with which to compare.
   * @return  <code>true</code> if this object is the same as the obj
   *          argument; <code>false</code> otherwise.
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
    {
      return true;
    }

    if ((obj == null) || (! (obj instanceof AbstractSchemaElement)))
    {
      return false;
    }

    AbstractSchemaElement ase = (AbstractSchemaElement) obj;
    if (! getIdentifier().equals(ase.getIdentifier()))
    {
      return false;
    }

    if (names.size() != ase.names.size())
    {
      return false;
    }

    Iterator<String> iterator = names.iterator();
    while (iterator.hasNext())
    {
      if (! ase.names.contains(iterator.next()))
      {
        return false;
      }
    }

    return true;
  }

  /**
   * Retrieves the string representation of this schema definition in
   * the form specified in RFC 2252.
   *
   * @return The string representation of this schema definition in
   *         the form specified in RFC 2252.
   */
  public final String toString()
  {
    return getDefinition();
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
    buffer.append(getIdentifier());

    if (!names.isEmpty()) {
      Iterator<String> iterator = names.iterator();

      String firstName = iterator.next();
      if (iterator.hasNext()) {
        buffer.append(" NAME ( '");
        buffer.append(firstName);

        while (iterator.hasNext()) {
          buffer.append("' '");
          buffer.append(iterator.next());
        }

        buffer.append("' )");
      } else {
        buffer.append(" NAME '");
        buffer.append(firstName);
        buffer.append("'");
      }
    }

    if ((description != null) && (description.length() > 0)) {
      buffer.append(" DESC '");
      buffer.append(description);
      buffer.append("'");
    }

    if (isObsolete) {
      buffer.append(" OBSOLETE");
    }

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

  protected abstract String getDefinition();

  protected abstract String getIdentifier();

  /**
   * Appends a string representation of this schema definition's
   * non-generic properties to the provided buffer.
   *
   * @param buffer
   *          The buffer to which the information should be appended.
   */
  protected abstract void toStringContent(StringBuilder buffer);
}
