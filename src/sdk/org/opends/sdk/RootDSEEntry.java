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



import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.opends.sdk.schema.Schema;
import org.opends.sdk.util.Validator;
import org.opends.server.types.ByteString;

/**
 * Root DSE Entry.
 */
public class RootDSEEntry extends AbstractEntry
{
  private static final String ATTR_ALT_SERVER="1.3.6.1.4.1.1466.101.120.6";
  private static final String ATTR_NAMING_CONTEXTS="1.3.6.1.4.1.1466.101.120.5";
  private static final String ATTR_SUPPORTED_CONTROL="1.3.6.1.4.1.1466.101.120.13";
  private static final String ATTR_SUPPORTED_EXTENSION="1.3.6.1.4.1.1466.101.120.7";
  private static final String ATTR_SUPPORTED_FEATURE="1.3.6.1.4.1.4203.1.3.5";
  private static final String ATTR_SUPPORTED_LDAP_VERSION="1.3.6.1.4.1.1466.101.120.15";
  private static final String ATTR_SUPPORTED_SASL_MECHANISMS="1.3.6.1.4.1.1466.101.120.14";

  private final Map<AttributeDescription, Attribute> attributes;

  private final Set<String> altServers;
  private final Set<DN> namingContexts;
  private final Set<String> supportedControls;
  private final Set<String> supportedExtensions;
  private final Set<String> supportedFeatures;
  private final Set<Integer> supportedLDAPVerions;
  private final Set<String> supportedSASLMechanisms;

  private final Schema schema;

  private RootDSEEntry(AttributeSequence entry, Schema schema)
      throws IllegalArgumentException
  {
    Validator.ensureNotNull(entry, schema);

    this.schema = schema;

    if(entry.hasAttributes())
    {
      Map<AttributeDescription, Attribute> attrs =
          new HashMap<AttributeDescription, Attribute>(
          entry.getAttributeCount());
      Attribute attr;
      for (AttributeValueSequence attribute : entry.getAttributes())
      {
        if(!attribute.isEmpty())
        {
          if (attribute instanceof Attribute)
          {
            attr = (Attribute) attribute;
          }
          else
          {
            attr = Types.newAttribute(attribute, getSchema());
          }
          attrs.put(attr.getAttributeDescription(), attr);
        }
      }
      attributes = Collections.unmodifiableMap(attrs);
    }
    else
    {
      attributes = Collections.emptyMap();
    }


    Attribute attr = getAttribute(ATTR_ALT_SERVER);
    if(attr != null)
    {
      Set<String> uris = new HashSet<String>(attr.size());
      for(ByteString uri : attr)
      {
        uris.add(uri.toString());
      }
      altServers = Collections.unmodifiableSet(uris);
    }
    else
    {
      altServers = Collections.emptySet();
    }
    attr = getAttribute(ATTR_NAMING_CONTEXTS);
    if(attr != null)
    {
      Set<DN> dns = new HashSet<DN>(attr.size());
      for(ByteString dn : attr)
      {
        dns.add(DN.valueOf(dn.toString(), schema));
      }
      namingContexts = Collections.unmodifiableSet(dns);
    }
    else
    {
      namingContexts = Collections.emptySet();
    }
    attr = getAttribute(ATTR_SUPPORTED_CONTROL);
    if(attr != null)
    {
      Set<String> oids = new HashSet<String>(attr.size());
      for(ByteString oid : attr)
      {
        oids.add(oid.toString());
      }
      supportedControls = Collections.unmodifiableSet(oids);
    }
    else
    {
      supportedControls = Collections.emptySet();
    }
    attr = getAttribute(ATTR_SUPPORTED_EXTENSION);
    if(attr != null)
    {
      Set<String> oids = new HashSet<String>(attr.size());
      for(ByteString oid : attr)
      {
        oids.add(oid.toString());
      }
      supportedExtensions = Collections.unmodifiableSet(oids);
    }
    else
    {
      supportedExtensions = Collections.emptySet();
    }
    attr = getAttribute(ATTR_SUPPORTED_FEATURE);
    if(attr != null)
    {
      Set<String> oids = new HashSet<String>(attr.size());
      for(ByteString oid : attr)
      {
        oids.add(oid.toString());
      }
      supportedFeatures = Collections.unmodifiableSet(oids);
    }
    else
    {
      supportedFeatures = Collections.emptySet();
    }
    attr = getAttribute(ATTR_SUPPORTED_LDAP_VERSION);
    if(attr != null)
    {
      Set<Integer> oids = new HashSet<Integer>(attr.size());
      for(ByteString oid : attr)
      {
        oids.add(Integer.valueOf(oid.toString()));
      }
      supportedLDAPVerions = Collections.unmodifiableSet(oids);
    }
    else
    {
      supportedLDAPVerions = Collections.emptySet();
    }
    attr = getAttribute(ATTR_SUPPORTED_SASL_MECHANISMS);
    if(attr != null)
    {
      Set<String> oids = new HashSet<String>(attr.size());
      for(ByteString oid : attr)
      {
        oids.add(oid.toString());
      }
      supportedSASLMechanisms = Collections.unmodifiableSet(oids);
    }
    else
    {
      supportedSASLMechanisms = Collections.emptySet();
    }
  }

  public static RootDSEEntry getRootDSE(Connection connection)
  {
    return null;
  }

  public Iterable<String> getAltServers()
  {
    return altServers;
  }

  public Iterable<DN> getNamingContexts()
  {
    return namingContexts;
  }

  public Iterable<String> getSupportedControls()
  {
      return supportedControls;
  }

  public boolean supportsControl(String oid)
  {
    Validator.ensureNotNull(oid);
    return supportedControls.contains(oid);
  }

  public Iterable<String> getSupportedExtendedOperations()
  {
    return supportedExtensions;
  }

  public boolean supportsExtendedOperation(String oid)
  {
    Validator.ensureNotNull(oid);
    return supportedExtensions.contains(oid);
  }

  public Iterable<String> getSupportedFeatures()
  {
    return supportedFeatures;
  }

  public boolean supportsFeature(String oid)
  {
    Validator.ensureNotNull(oid);
    return supportedFeatures.contains(oid);
  }

  public Iterable<Integer> getSupportedLDAPVersions()
  {
    return supportedLDAPVerions;
  }

  public boolean supportsLDAPVersion(int version)
  {
    return supportedLDAPVerions.contains(version);
  }

  public Iterable<String> getSupportedSASLMechanismNames()
  {
    return supportedSASLMechanisms;
  }

  public boolean supportsSASLMechanism(String oid)
  {
    Validator.ensureNotNull(oid);
    return supportedSASLMechanisms.contains(oid);
  }

  /**
   * {@inheritDoc}
   */
  public boolean addAttribute(Attribute attribute,
                              Collection<ByteString> duplicateValues)
      throws UnsupportedOperationException, NullPointerException
  {
    throw new UnsupportedOperationException();
  }



  /**
   * {@inheritDoc}
   */
  public Entry addAttribute(AttributeValueSequence attribute)
      throws LocalizedIllegalArgumentException,
      UnsupportedOperationException, NullPointerException
  {
    throw new UnsupportedOperationException();
  }



  /**
   * {@inheritDoc}
   */
  public Entry addAttribute(String attributeDescription,
                            Object... values) throws LocalizedIllegalArgumentException,
      UnsupportedOperationException, NullPointerException
  {
    throw new UnsupportedOperationException();
  }



  public Entry clearAttributes() throws UnsupportedOperationException
  {
    throw new UnsupportedOperationException();
  }

  public boolean containsAttribute(AttributeDescription attributeDescription)
      throws NullPointerException {
    Validator.ensureNotNull(attributeDescription);

    return attributes.containsKey(attributeDescription);  }

  public Attribute getAttribute(AttributeDescription attributeDescription)
      throws NullPointerException {
    Validator.ensureNotNull(attributeDescription);

    return attributes.get(attributeDescription);  }

  public int getAttributeCount() {
    return attributes.size();
  }

  public Iterable<Attribute> getAttributes() {
    return attributes.values();
  }

  public DN getNameDN() {
    return DN.rootDN();
  }

  public Schema getSchema() {
    return schema;
  }

  /**
   * {@inheritDoc}
   */
  public boolean removeAttribute(Attribute attribute,
                                 Collection<ByteString> missingValues)
      throws UnsupportedOperationException, NullPointerException
  {
    throw new UnsupportedOperationException();
  }



  public boolean removeAttribute(
      AttributeDescription attributeDescription)
      throws UnsupportedOperationException, NullPointerException
  {
    throw new UnsupportedOperationException();
  }



  /**
   * {@inheritDoc}
   */
  public Entry removeAttribute(String attributeDescription)
      throws LocalizedIllegalArgumentException,
      UnsupportedOperationException, NullPointerException
  {
    throw new UnsupportedOperationException();
  }



  /**
   * {@inheritDoc}
   */
  public Entry removeAttribute(String attributeDescription,
                               Object... values) throws LocalizedIllegalArgumentException,
      UnsupportedOperationException, NullPointerException
  {
    throw new UnsupportedOperationException();
  }



  /**
   * {@inheritDoc}
   */
  public boolean replaceAttribute(Attribute attribute)
      throws UnsupportedOperationException, NullPointerException
  {
    throw new UnsupportedOperationException();
  }



  /**
   * {@inheritDoc}
   */
  public Entry replaceAttribute(String attributeDescription,
                                Object... values) throws LocalizedIllegalArgumentException,
      UnsupportedOperationException, NullPointerException
  {
    throw new UnsupportedOperationException();
  }



  /**
   * {@inheritDoc}
   */
  public Entry setName(String dn)
      throws LocalizedIllegalArgumentException,
      UnsupportedOperationException, NullPointerException
  {
    throw new UnsupportedOperationException();
  }



  public Entry setNameDN(DN dn) throws UnsupportedOperationException,
      NullPointerException
  {
    throw new UnsupportedOperationException();
  }
}
