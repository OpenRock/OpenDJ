package org.opends.ldap.responses;



import java.util.ArrayList;
import java.util.List;

import org.opends.server.types.ByteString;
import org.opends.server.util.Validator;
import org.opends.types.Attribute;
import org.opends.types.DN;
import org.opends.types.Entry;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 25, 2009 Time:
 * 5:49:01 PM To change this template use File | Settings | File
 * Templates.
 */
public final class SearchResultEntry extends Response
{
  private String dn;
  private final List<Attribute> attributes;



  public SearchResultEntry(DN dn)
  {
    Validator.ensureNotNull(dn);
    this.dn = dn.toString();
    this.attributes = new ArrayList<Attribute>();
  }



  public SearchResultEntry(DN dn,
      org.opends.server.types.Attribute... attributes)
  {
    Validator.ensureNotNull(dn);
    this.dn = dn.toString();

    if (attributes != null)
    {
      this.attributes = new ArrayList<Attribute>(attributes.length);
      addAttribute(attributes);
    }
    else
    {
      this.attributes = new ArrayList<Attribute>();
    }
  }



  public SearchResultEntry(Entry entry)
  {
    Validator.ensureNotNull(entry);
    this.dn = entry.getDN().toString();
    this.attributes = new ArrayList<Attribute>(entry.attributeCount());
    for (org.opends.server.types.Attribute attribute : entry
        .getAttributes())
    {
      addAttribute(attribute);
    }
  }



  public SearchResultEntry(String dn)
  {
    Validator.ensureNotNull(dn);
    this.dn = dn;
    this.attributes = new ArrayList<Attribute>();
  }



  /**
   * Adds the provided attribute to the set of raw attributes for this
   * add request.
   * 
   * @param attributes
   *          The attributes to add.
   * @return This raw add request.
   */
  public SearchResultEntry addAttribute(Attribute... attributes)
  {
    if (attributes == null)
    {
      return this;
    }

    for (Attribute attribute : attributes)
    {
      Validator.ensureNotNull(attribute);
      this.attributes.add(attribute);
    }
    return this;
  }



  public SearchResultEntry addAttribute(
      org.opends.server.types.Attribute... attributes)
  {
    if (attributes == null)
    {
      return this;
    }

    for (org.opends.server.types.Attribute attribute : attributes)
    {
      Validator.ensureNotNull(attribute);
      this.attributes.add(new Attribute(attribute));
    }
    return this;
  }



  public SearchResultEntry addAttribute(String attributeDescription,
      ByteString... attributeValues)
  {
    Validator.ensureNotNull(attributeDescription);
    attributes
        .add(new Attribute(attributeDescription, attributeValues));
    return this;
  }



  public int attributeCount()
  {
    return attributes.size();
  }



  public Iterable<ByteString> getAttribute(String attributeDescription)
  {
    for (Attribute attribute : attributes)
    {
      if (attribute.getAttributeDescription().equals(
          attributeDescription))
      {
        return attribute.getAttributeValues();
      }
    }

    return null;
  }



  public Iterable<Attribute> getAttributes()
  {
    return attributes;
  }



  public String getDn()
  {
    return dn;
  }



  public SearchResultEntry setDN(DN dn)
  {
    Validator.ensureNotNull(dn);
    this.dn = dn.toString();
    return this;
  }



  public SearchResultEntry setDN(String dn)
  {
    Validator.ensureNotNull(dn);
    this.dn = dn;
    return this;
  }



  @Override
  public void toString(StringBuilder buffer)
  {
    buffer.append("SearchResultEntry(dn=");
    buffer.append(dn);
    buffer.append(", attributes=");
    buffer.append(attributes);
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}
