package org.opends.common.api.response;

import org.opends.common.api.RawMessage;
import org.opends.common.api.DN;
import org.opends.common.api.Entry;
import org.opends.common.api.RawPartialAttribute;
import org.opends.server.types.ByteString;
import org.opends.server.types.Attribute;
import org.opends.server.types.AttributeValue;
import org.opends.server.util.Validator;

import java.util.*;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 25, 2009 Time: 5:49:01
 * PM To change this template use File | Settings | File Templates.
 */
public final class RawSearchResultEntry extends RawMessage
    implements RawResponse
{
  private String dn;
  private final List<RawPartialAttribute> attributes;

  public RawSearchResultEntry(String dn)
  {
    Validator.ensureNotNull(dn);
    this.dn = dn;
    this.attributes = new ArrayList<RawPartialAttribute>();
  }

  public RawSearchResultEntry(DN dn)
  {
    Validator.ensureNotNull(dn);
    this.dn = dn.toString();
    this.attributes = new ArrayList<RawPartialAttribute>();
  }

  public RawSearchResultEntry(DN dn, Attribute... attributes)
  {
    Validator.ensureNotNull(dn);
    this.dn = dn.toString();

    if(attributes != null)
    {
      this.attributes =
          new ArrayList<RawPartialAttribute>(attributes.length);
      addAttribute(attributes);
    }
    else
    {
      this.attributes = new ArrayList<RawPartialAttribute>();
    }
  }

  public RawSearchResultEntry(Entry entry)
  {
    Validator.ensureNotNull(entry);
    this.dn = entry.getDN().toString();
    this.attributes =
          new ArrayList<RawPartialAttribute>(entry.attributeCount());
    for(Attribute attribute : entry.getAttributes())
    {
      addAttribute(attribute);
    }
  }

  public String getDn()
  {
    return dn;
  }

  public RawSearchResultEntry setDN(String dn)
  {
    Validator.ensureNotNull(dn);
    this.dn = dn;
    return this;
  }

  public RawSearchResultEntry setDN(DN dn)
  {
    Validator.ensureNotNull(dn);
    this.dn = dn.toString();
    return this;
  }

  public RawSearchResultEntry addAttribute(String attributeDescription,
                                    ByteString... attributeValues)
  {
    Validator.ensureNotNull(attributeDescription);
    attributes.add(new RawPartialAttribute(attributeDescription,
        attributeValues));
    return this;
  }

  public RawSearchResultEntry addAttribute(Attribute... attributes)
  {
    if(attributes == null)
    {
      return this;
    }

    for(Attribute attribute : attributes)
    {
      Validator.ensureNotNull(attribute);
      this.attributes.add(new RawPartialAttribute(attribute));
    }
    return this;
  }

  /**
   * Adds the provided attribute to the set of raw attributes for this
   * add request.
   *
   * @param attributes The attributes to add.
   * @return This raw add request.
   */
  public RawSearchResultEntry addAttribute(
      RawPartialAttribute... attributes)
  {
    if(attributes == null)
    {
      return this;
    }

    for(RawPartialAttribute attribute : attributes)
    {
      Validator.ensureNotNull(attribute);
      this.attributes.add(attribute);
    }
    return this;
  }


  public Iterable<RawPartialAttribute> getAttributes()
  {
    return attributes;
  }

  public Iterable<ByteString> getAttribute(
      String attributeDescription)
  {
    for(RawPartialAttribute attribute : attributes)
    {
      if(attribute.getAttributeDescription().equals(
          attributeDescription))
      {
        return attribute.getAttributeValues();
      }
    }

    return null;
  }

  public int attributeCount()
  {
    return attributes.size();
  }

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
