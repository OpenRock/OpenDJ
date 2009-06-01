package org.opends.common.api.raw;

import org.opends.server.types.ByteString;
import org.opends.server.util.Validator;

import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;


/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 26, 2009 Time: 11:44:53
 * PM To change this template use File | Settings | File Templates.
 */
public class RawPartialAttribute
{
  private String attributeDescription;
  private List<ByteString> attributeValues;

  public RawPartialAttribute(String attributeDescription)
  {
    this.attributeDescription = attributeDescription;
    this.attributeValues = new LinkedList<ByteString>();
  }

  public String getAttributeDescription()
  {
    return attributeDescription;
  }

  public RawPartialAttribute setAttirbuteDescription(
      String attributeDescription)
  {
    Validator.ensureNotNull(attributeDescription);
    this.attributeDescription = attributeDescription;
    return this;
  }

  public Iterable<ByteString> getAttributeValues()
  {
    return attributeValues;
  }

  public RawPartialAttribute addAttributeValue(ByteString attributeValue)
  {
    Validator.ensureNotNull(attributeValue);
    attributeValues.add(attributeValue);
    return this;
  }

    /**
   * Returns a string representation of this attribute.
   *
   * @return A string representation of this attribute.
   */
  @Override
  public final String toString()
  {
    StringBuilder builder = new StringBuilder();
    toString(builder);
    return builder.toString();
  }



  /**
   * Appends a string representation of this attribute to the provided
   * buffer.
   *
   * @param buffer
   *          The buffer into which a string representation of this
   *          request should be appended.
   */
  public void toString(StringBuilder buffer)
  {
    buffer.append("LDAPAttribute(type=");
    buffer.append(attributeDescription);
    buffer.append(", values={");

    if (! attributeValues.isEmpty())
    {
      Iterator<ByteString> iterator = attributeValues.iterator();
      buffer.append(iterator.next());
      while (iterator.hasNext())
      {
        buffer.append(", ");
        buffer.append(iterator.next());
      }
    }

    buffer.append("})");
  }
}
