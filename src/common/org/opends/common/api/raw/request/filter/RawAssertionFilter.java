package org.opends.common.api.raw.request.filter;

import org.opends.server.types.ByteString;
import org.opends.server.util.Validator;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 10, 2009 Time: 11:54:17
 * AM To change this template use File | Settings | File Templates.
 */
public abstract class RawAssertionFilter extends RawFilter
{
  protected String attributeType;
  protected ByteString assertionValue;

  protected RawAssertionFilter(String attributeType,
                               ByteString assertionValue)
  {
    Validator.ensureNotNull(attributeType, assertionValue);
    this.attributeType = attributeType;
    this.assertionValue = assertionValue;
  }

  public String getAttributeType()
  {
    return attributeType;
  }

  public RawAssertionFilter setAttributeType(String attributeType)
  {
    Validator.ensureNotNull(attributeType);
    this.attributeType = attributeType;
    return this;
  }

  public ByteString getAssertionValue()
  {
    return assertionValue;
  }

  public RawAssertionFilter setAssertionValue(ByteString assertionValue)
  {
    Validator.ensureNotNull(assertionValue);
    this.assertionValue = assertionValue;
    return this;
  }
}
