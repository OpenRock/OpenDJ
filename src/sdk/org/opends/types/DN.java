package org.opends.types;

import org.opends.server.types.ByteString;
import org.opends.schema.AttributeType;
import org.opends.schema.Schema;
import org.opends.util.Validator;
import org.opends.util.SubstringReader;
import org.opends.ldap.DecodeException;

import java.util.*;


/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 18, 2009 Time:
 * 9:40:42 AM To change this template use File | Settings | File
 * Templates.
 */
public final class DN implements Iterable<RDN>
{
  private static final DN ROOT_DN = new DN(null, null);


  private final RDN rdn;
  private final DN parent;
  private String normalizedString;

  private DN(RDN rdn, DN parent) {
    this.rdn = rdn;
    this.parent = parent;
  }

  public String toNormalizedString()
  {
    if(normalizedString == null)
    {
      StringBuilder builder = new StringBuilder();
      rdn.toString(builder);
      if(parent != null)
      {
        builder.append(",");
        builder.append(parent.toNormalizedString());
      }
      normalizedString = builder.toString();
    }

    return normalizedString;
  }

  public Iterator<RDN> iterator() {
    return new Iterator<RDN>()
    {
      DN dn = DN.this;
      public boolean hasNext() {
        return dn.parent != null && !dn.parent.isRootDN();
      }

      public RDN next() {
        if(!hasNext())
        {
          throw new NoSuchElementException();
        }
        RDN rdn = dn.rdn;
        dn = dn.parent;
        return rdn;
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  public boolean isRootDN()
  {
    return parent == null;
  }

  public DN child(RDN rdn)
  {
    return new DN(rdn, this);
  }

  public DN child(RDN... rdns)
  {
    DN parent = this;
    for(int i = rdns.length - 1; i >=0; i--)
    {
      parent = new DN(rdns[i], parent);
    }
    return parent;
  }

  public DN parent()
  {
    return parent;
  }

  public static DN rootDN()
  {
    return ROOT_DN;
  }

  private static LinkedHashMap<String, DN> dnCache;

  public static DN valueOf(String dnString, Schema schema)
      throws DecodeException
  {
    DN dn = dnCache.get(dnString);
    if(dn == null)
    {
      SubstringReader reader = new SubstringReader(dnString);
      dn = decode(reader, schema);
      dnCache.put(dnString, dn);
    }
    return dn;
  }

  private static DN decode(SubstringReader reader, Schema schema)
      throws DecodeException
  {
    RDN rdn = RDN.readRDN(reader, schema);

    DN parent;
    if(reader.remaining() > 0 && reader.read() == ',')
    {
      reader.mark();
      String parentString = reader.read(reader.remaining());
      synchronized(dnCache)
      {
        parent = dnCache.get(parentString);
      }
      if(parent == null)
      {
        reader.reset();
        parent = decode(reader, schema);
        synchronized(dnCache)
        {
          dnCache.put(parentString, parent);
        }
      }
    }
    else
    {
      parent = ROOT_DN;
    }

    return parent.child(rdn);
  }


}
