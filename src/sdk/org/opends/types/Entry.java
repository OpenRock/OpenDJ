package org.opends.types;



import org.opends.server.types.Attribute;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 18, 2009 Time:
 * 9:40:32 AM To change this template use File | Settings | File
 * Templates.
 */
public interface Entry
{
  public int attributeCount();



  public boolean containsAttribute(
      AttributeDescription attributeDescription);



  public boolean containsAttribute(String attributeDescription);



  public Iterable<Attribute> findAttributes(
      AttributeDescription attributeDescription);



  public Iterable<Attribute> findAttributes(String attributeDescription);



  public Attribute getAttribute(
      AttributeDescription attributeDescription);



  public Attribute getAttribute(String attributeDescription);



  public Iterable<Attribute> getAttributes();



  public DN getDN();



  public void putAttribute(AttributeDescription attributeDescription,
      Attribute attribute);



  public void putAttribute(String attributeDescription,
      Attribute attribute);
}
