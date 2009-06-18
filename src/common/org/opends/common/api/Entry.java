package org.opends.common.api;

import org.opends.server.types.Attribute;
import org.opends.common.api.AttributeDescription;

import java.util.Set;
import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 18, 2009 Time: 9:40:32
 * AM To change this template use File | Settings | File Templates.
 */
public interface Entry
{
  public DN getDN();

  public boolean containsAttribute(String attributeDescription);
  public boolean containsAttribute(AttributeDescription attributeDescription);

  public Attribute getAttribute(String attributeDescription);
  public Attribute getAttribute(AttributeDescription attributeDescription);

  public void putAttribute(AttributeDescription attributeDescription,
                           Attribute attribute);
  public void putAttribute(String attributeDescription, Attribute attribute);

  public Iterable<Attribute> findAttributes(AttributeDescription attributeDescription);
  public Iterable<Attribute> findAttributes(String attributeDescription);

  public Iterable<Attribute> getAttributes();
}
