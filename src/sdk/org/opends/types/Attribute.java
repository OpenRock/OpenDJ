package org.opends.types;



import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opends.server.types.AttributeValue;
import org.opends.server.types.ByteString;
import org.opends.server.util.Validator;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 19, 2009 Time:
 * 6:08:54 PM To change this template use File | Settings | File
 * Templates.
 */
public class Attribute
{
  private final String attributeDescription;
  private List<ByteString> attributeValues;



  public Attribute(org.opends.server.types.Attribute attribute)
  {
    Validator.ensureNotNull(attribute);
    this.attributeDescription = attribute.getNameWithOptions();
    if (attribute.size() > 0)
    {
      attributeValues = new ArrayList<ByteString>(attribute.size());
    }
    else
    {
      attributeValues = Collections.emptyList();
    }

    for (AttributeValue attributeValue : attribute)
    {
      attributeValues.add(attributeValue.getValue());
    }
  }



  public Attribute(String attributeDescription,
      ByteString... attributeValues)
  {
    Validator.ensureNotNull(attributeDescription);
    this.attributeDescription = attributeDescription;

    if (attributeValues != null)
    {
      this.attributeValues =
          new ArrayList<ByteString>(attributeValues.length);
      for (ByteString value : attributeValues)
      {
        Validator.ensureNotNull(value);
        this.attributeValues.add(value);
      }
    }
    else
    {
      this.attributeValues = Collections.emptyList();
    }
  }



  public Attribute(String attributeDescription,
      ByteString attributeValue, ByteString... attributeValues)
  {
    Validator.ensureNotNull(attributeDescription, attributeValue);
    this.attributeDescription = attributeDescription;

    if (attributeValues != null)
    {
      this.attributeValues =
          new ArrayList<ByteString>(attributeValues.length + 1);
      this.attributeValues.add(attributeValue);
      for (ByteString value : attributeValues)
      {
        Validator.ensureNotNull(value);
        this.attributeValues.add(value);
      }
    }
    else
    {
      this.attributeValues = new ArrayList<ByteString>(1);
      this.attributeValues.add(attributeValue);
    }
  }



  public void addAttributeValue(ByteString attributeValue)
  {
    Validator.ensureNotNull(attributeValue);
    attributeValues.add(attributeValue);
  }



  public String getAttributeDescription()
  {
    return attributeDescription;
  }



  public Iterable<ByteString> getAttributeValues()
  {
    return attributeValues;
  }



  /**
   * Returns a string representation of this request.
   * 
   * @return A string representation of this request.
   */
  @Override
  public final String toString()
  {
    StringBuilder builder = new StringBuilder();
    toString(builder);
    return builder.toString();
  }



  /**
   * Appends a string representation of this request to the provided
   * buffer.
   * 
   * @param buffer
   *          The buffer into which a string representation of this
   *          request should be appended.
   */
  public void toString(StringBuilder buffer)
  {
    buffer.append("Attribute(attributeDescription=");
    buffer.append(attributeDescription);
    buffer.append(", attributeValues=");
    buffer.append(attributeValues);
    buffer.append(")");
  }
}
