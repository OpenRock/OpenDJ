package org.opends.types;

import org.opends.schema.Schema;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Aug 17, 2009
 * Time: 4:36:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class EntryImpl implements Entry
{
  private DN dn;

  public EntryImpl(AttributeSequence attrs, Schema schema)
  {
    for(AttributeValueSequence avs : attrs.getAttributes())
    {
    }
  }
  public Entry addAttribute(Attribute attribute)
      throws UnsupportedOperationException, NullPointerException
  {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public Entry addAttribute(AttributeValueSequence attribute)
      throws UnsupportedOperationException, NullPointerException
  {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public Entry clearAttributes() throws UnsupportedOperationException
  {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public boolean containsAttribute(AttributeDescription attributeDescription)
  {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public boolean containsAttribute(String attributeDescription) {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public Iterable<Attribute> findAttributes(AttributeDescription attributeDescription) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public Iterable<Attribute> findAttributes(String attributeDescription) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public Attribute getAttribute(AttributeDescription attributeDescription) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public Attribute getAttribute(String attributeDescription) throws NullPointerException {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public int getAttributeCount() {
    return 0;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public Iterable<Attribute> getAttributes() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public String getName() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public DN getNameDN() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public boolean hasAttributes() {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public Attribute removeAttribute(String attributeDescription) throws UnsupportedOperationException, NullPointerException {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public Attribute removeAttribute(AttributeDescription attributeDescription) throws UnsupportedOperationException, NullPointerException {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public Entry setName(String dn) throws UnsupportedOperationException, NullPointerException {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public Entry setNameDN(DN dn) throws UnsupportedOperationException, NullPointerException {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }
}
