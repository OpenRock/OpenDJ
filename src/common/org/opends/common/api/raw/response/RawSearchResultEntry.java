package org.opends.common.api.raw.response;

import org.opends.common.api.raw.RawMessage;
import org.opends.common.api.DN;
import org.opends.common.api.Entry;
import org.opends.server.core.operations.Response;
import org.opends.server.core.operations.Schema;
import org.opends.server.types.DirectoryException;
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
  private final Map<String, List<ByteString>> attributes =
      new HashMap<String, List<ByteString>>();

  public RawSearchResultEntry(String dn)
  {
    Validator.ensureNotNull(dn);
    this.dn = dn;
  }

  public RawSearchResultEntry(DN dn)
  {
    Validator.ensureNotNull(dn);
    this.dn = dn.toString();
  }

  public RawSearchResultEntry(DN dn, Attribute... attributes)
  {
    Validator.ensureNotNull(dn);
    this.dn = dn.toString();
    addAttribute(attributes);
  }

  public RawSearchResultEntry(Entry entry)
  {
    Validator.ensureNotNull(entry);
    this.dn = entry.getDN().toString();
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
                                    ByteString attributeValue,
                                    ByteString... attributeValues)
  {
    Validator.ensureNotNull(attributeDescription, attributeValue);
    List<ByteString> values = attributes.get(attributeDescription);
    if(values == null)
    {
      values = new ArrayList<ByteString>(1);
    }
    values.add(attributeValue);

    if(attributeValues != null)
    {
      for(ByteString value : attributeValues)
      {
        Validator.ensureNotNull(attributeValue);
        values.add(value);
      }
    }

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
      List<ByteString> values =
          this.attributes.get(attribute.getNameWithOptions());
      if(values == null)
      {
        if(attribute.size() > 0)
        {
          values = new ArrayList<ByteString>(attribute.size());
        }
        else
        {
          values = Collections.emptyList();
        }
      }

      for(AttributeValue attributeValue : attribute)
      {
        values.add(attributeValue.getValue());
      }
    }
    return this;
  }



  public Set<Map.Entry<String, List<ByteString>>> getAttributes()
  {
    return attributes.entrySet();
  }

  public Iterable<ByteString> getAttribute(String attributeDescription)
  {
    return attributes.get(attributeDescription);
  }

  public Response toResponse(Schema schema) throws DirectoryException
  {
    // TODO: not yet implemented.
    return null;
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
