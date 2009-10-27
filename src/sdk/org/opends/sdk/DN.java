/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE
 * or https://OpenDS.dev.java.net/OpenDS.LICENSE.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE.  If applicable,
 * add the following below this CDDL HEADER, with the fields enclosed
 * by brackets "[]" replaced with your own identifying information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Copyright 2009 Sun Microsystems, Inc.
 */

package org.opends.sdk;



import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.opends.sdk.schema.Schema;
import org.opends.sdk.schema.SchemaLocal;
import org.opends.sdk.util.LocalizedIllegalArgumentException;
import org.opends.sdk.util.SubstringReader;
import org.opends.sdk.util.Validator;



/**
 * A distinguished name (DN).
 */
public final class DN implements Iterable<RDN>
{
  private static final DN ROOT_DN = new DN(null, null, "");
  private static final int DN_CACHE_SIZE = 100;
  private static final DecodeCache CACHE = new DecodeCache();



  // FIXME: needs synchronization or use thread locals.
  private static class DecodeCache extends
      SchemaLocal<Map<String, DN>>
  {
    public DN getCachedDN(Schema schema, String dn)
    {
      return get(schema).get(dn);
    }



    public void putCachedDN(Schema schema, String dnString, DN dn)
    {
      get(schema).put(dnString, dn);
    }



    @SuppressWarnings("serial")
    @Override
    protected Map<String, DN> initialValue()
    {
      return new LinkedHashMap<String, DN>(DN_CACHE_SIZE, 0.75f, true)
      {
        @Override
        protected boolean removeEldestEntry(
            Map.Entry<String, DN> stringDNEntry)
        {
          return size() > DN_CACHE_SIZE;
        }
      };
    }
  }

  private final RDN rdn;
  private final DN parent;
  private String normalizedString;



  private DN(RDN rdn, DN parent, String normalizedString)
  {
    this.rdn = rdn;
    this.parent = parent;
    this.normalizedString = normalizedString;
  }



  @Override
  public String toString()
  {
    if (normalizedString == null)
    {
      StringBuilder builder = new StringBuilder();
      rdn.toString(builder);
      if (!parent.isRootDN())
      {
        builder.append(",");
        builder.append(parent.toString());
      }
      normalizedString = builder.toString();
    }

    return normalizedString;
  }



  public Iterator<RDN> iterator()
  {
    return new Iterator<RDN>()
    {
      DN dn = DN.this;



      public boolean hasNext()
      {
        return dn.parent != null && !dn.parent.isRootDN();
      }



      public RDN next()
      {
        if (!hasNext())
        {
          throw new NoSuchElementException();
        }
        RDN rdn = dn.rdn;
        dn = dn.parent;
        return rdn;
      }



      public void remove()
      {
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
    return new DN(rdn, this, null);
  }



  public DN child(RDN... rdns)
  {
    DN parent = this;
    for (int i = rdns.length - 1; i >= 0; i--)
    {
      parent = new DN(rdns[i], parent, null);
    }
    return parent;
  }



  public DN parent()
  {
    return parent;
  }



  public ConditionResult matches(DN dn)
  {
    if (rdn == null && dn.rdn == null)
    {
      return ConditionResult.TRUE;
    }
    else if (rdn != null && dn.rdn != null)
    {
      ConditionResult result = rdn.matches(dn.rdn);
      if (result != ConditionResult.TRUE)
      {
        return result;
      }
      return parent.matches(dn.parent);
    }
    return ConditionResult.FALSE;
  }



  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
    {
      return true;
    }

    if (obj instanceof DN)
    {
      DN that = (DN) obj;
      return matches(that) == ConditionResult.TRUE;
    }

    return false;
  }



  public static DN rootDN()
  {
    return ROOT_DN;
  }



  public static DN valueOf(String dnString, Schema schema)
      throws LocalizedIllegalArgumentException
  {
    if (dnString.length() == 0)
      return ROOT_DN;

    DN dn = CACHE.getCachedDN(schema, dnString);
    if (dn == null)
    {
      SubstringReader reader = new SubstringReader(dnString);
      dn = decode(reader, schema);
    }
    return dn;
  }



  static DN decode(SubstringReader reader, Schema schema)
      throws LocalizedIllegalArgumentException
  {
    reader.skipWhitespaces();
    if (reader.remaining() == 0)
    {
      return ROOT_DN;
    }

    RDN rdn = RDN.decode(reader, schema);

    DN parent;
    if (reader.remaining() > 0 && reader.read() == ',')
    {
      reader.mark();
      String parentString = reader.read(reader.remaining());
      parent = CACHE.getCachedDN(schema, parentString);
      if (parent == null)
      {
        reader.reset();
        parent = decode(reader, schema);
        CACHE.putCachedDN(schema, parentString, parent);
      }
    }
    else
    {
      parent = ROOT_DN;
    }

    return parent.child(rdn);
  }



  public boolean isAncestorOf(DN dn)
  {
    Validator.ensureNotNull(dn);

    // We could optimize this if we kept track of the number of RDN
    // components in DN by fast-forwarding to the parent of dn having
    // the number of RDNs as this DN.
    for (DN tmp = dn; tmp != null; tmp = tmp.parent)
    {
      if (equals(tmp))
      {
        return true;
      }
    }
    return false;
  }



  public boolean isDescendantOf(DN dn)
  {
    Validator.ensureNotNull(dn);

    // We could optimize this if we kept track of the number of RDN
    // components in DN by fast-forwarding to the parent of this DN
    // having the number of RDNs as dn.
    for (DN tmp = this; tmp != null; tmp = tmp.parent)
    {
      if (equals(dn))
      {
        return true;
      }
    }
    return false;
  }

}
