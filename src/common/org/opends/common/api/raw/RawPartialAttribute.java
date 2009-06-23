package org.opends.common.api.raw;

import org.opends.server.types.ByteString;
import org.opends.server.types.Attribute;
import org.opends.server.types.AttributeValue;
import org.opends.server.util.Validator;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 19, 2009 Time: 6:08:54
 * PM To change this template use File | Settings | File Templates.
 */
public class RawPartialAttribute
{
  private String attributeDescription;
  private List<ByteString> attributeValues;

  public RawPartialAttribute(String attributeDescription,
                             ByteString... attributeValues)
  {
    Validator.ensureNotNull(attributeDescription);
    this.attributeDescription = attributeDescription;

    if(attributeValues != null)
    {
      this.attributeValues =
          new ArrayList<ByteString>(attributeValues.length);
      for(ByteString value : attributeValues)
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

  public RawPartialAttribute(Attribute attribute)
  {
    Validator.ensureNotNull(attribute);
    this.attributeDescription = attribute.getNameWithOptions();
    if(attribute.size() > 0)
    {
      attributeValues = new ArrayList<ByteString>(attribute.size());
    }
    else
    {
      attributeValues = Collections.emptyList();
    }

    for(AttributeValue attributeValue : attribute)
    {
      attributeValues.add(attributeValue.getValue());
    }
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
