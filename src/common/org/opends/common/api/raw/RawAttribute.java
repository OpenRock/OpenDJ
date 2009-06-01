package org.opends.common.api.raw;

import org.opends.server.types.ByteString;
import org.opends.server.util.Validator;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 26, 2009 Time: 11:52:41
 * PM To change this template use File | Settings | File Templates.
 */
public final class RawAttribute extends RawPartialAttribute
{
  public RawAttribute(String attributeDescription, ByteString attributeValue)
  {
    super(attributeDescription);
    Validator.ensureNotNull(attributeValue);
    addAttributeValue(attributeValue);
  }
}
