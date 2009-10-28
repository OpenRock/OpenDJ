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

import org.opends.sdk.responses.SearchResultEntry;
import org.opends.sdk.schema.Schema;
import org.opends.sdk.schema.SchemaNotFoundException;
import org.opends.sdk.util.Functions;
import org.opends.sdk.util.Iterables;
import org.opends.sdk.util.Validator;
import org.opends.server.types.ByteString;

/**
 * Root DSE Entry.
 */
public class RootDSE extends AbstractEntry
{
  private static final String ATTR_ALT_SERVER="altServer";
  private static final String ATTR_NAMING_CONTEXTS="namingContexts";
  private static final String ATTR_SUPPORTED_CONTROL="supportedControl";
  private static final String ATTR_SUPPORTED_EXTENSION="supportedExtension";
  private static final String ATTR_SUPPORTED_FEATURE="supportedFeatures";
  private static final String ATTR_SUPPORTED_LDAP_VERSION=
      "supportedLDAPVersion";
  private static final String ATTR_SUPPORTED_SASL_MECHANISMS=
      "supportedSASLMechanisms";
  private static final String ATTR_SUPPORTED_AUTH_PASSWORD_SCHEMES=
      "supportedAuthPasswordSchemes";
  private static final String ATTR_VENDOR_NAME="vendorName";
  private static final String ATTR_VENDOR_VERSION="vendorVersion";
  private static String[] ROOTDSE_ATTRS=new String[]{ ATTR_ALT_SERVER,
      ATTR_NAMING_CONTEXTS, ATTR_SUPPORTED_CONTROL, ATTR_SUPPORTED_EXTENSION,
      ATTR_SUPPORTED_FEATURE, ATTR_SUPPORTED_LDAP_VERSION,
      ATTR_SUPPORTED_SASL_MECHANISMS, ATTR_VENDOR_NAME, ATTR_VENDOR_VERSION,
      ATTR_SUPPORTED_AUTH_PASSWORD_SCHEMES, "*" };

  private final Entry entry;

  private final Iterable<String> altServers;
  private final Iterable<DN> namingContexts;
  private final Iterable<String> supportedControls;
  private final Iterable<String> supportedExtensions;
  private final Iterable<String> supportedFeatures;
  private final Iterable<Integer> supportedLDAPVerions;
  private final Iterable<String> supportedSASLMechanisms;
  private final Iterable<String> supportedAuthPasswordSchemes;
  private final String vendorName;
  private final String vendorVersion;

  private RootDSE(Entry entry)
      throws IllegalArgumentException
  {
    this.entry = Types.unmodifiableEntry(entry);

    Attribute attr = getAttribute(ATTR_ALT_SERVER);
    if(attr == null)
    {
      altServers = Collections.emptyList();
    }
    else
    {
      altServers = Iterables.unmodifiable(Iterables.transform(
          attr, Functions.valueToString()));
    }

    attr = getAttribute(ATTR_NAMING_CONTEXTS);
    if(attr == null)
    {
      namingContexts = Collections.emptyList();
    }
    else
    {
      namingContexts = Iterables.unmodifiable(Iterables.transform(
          attr, Functions.valueToDN()));
    }

    attr = getAttribute(ATTR_SUPPORTED_CONTROL);
    if(attr == null)
    {
      supportedControls = Collections.emptyList();
    }
    else
    {
      supportedControls = Iterables.unmodifiable(Iterables.transform(
          attr, Functions.valueToString()));
    }

    attr = getAttribute(ATTR_SUPPORTED_EXTENSION);
    if(attr == null)
    {
      supportedExtensions = Collections.emptyList();
    }
    else
    {
      supportedExtensions = Iterables.unmodifiable(Iterables.transform(
          attr, Functions.valueToString()));
    }

    attr = getAttribute(ATTR_SUPPORTED_FEATURE);
    if(attr == null)
    {
      supportedFeatures = Collections.emptyList();
    }
    else
    {
      supportedFeatures = Iterables.unmodifiable(Iterables.transform(
          attr, Functions.valueToString()));
    }

    attr = getAttribute(ATTR_SUPPORTED_LDAP_VERSION);
    if(attr == null)
    {
      supportedLDAPVerions = Collections.emptyList();
    }
    else
    {
      supportedLDAPVerions = Iterables.unmodifiable(Iterables.transform(
          attr, Functions.valueToInteger()));
    }

    attr = getAttribute(ATTR_SUPPORTED_SASL_MECHANISMS);
    if(attr == null)
    {
      supportedSASLMechanisms = Collections.emptyList();
    }
    else
    {
      supportedSASLMechanisms = Iterables.unmodifiable(Iterables.transform(
          attr,
          Functions.valueToString()));
    }

    attr = getAttribute(ATTR_SUPPORTED_AUTH_PASSWORD_SCHEMES);
    if(attr == null)
    {
      supportedAuthPasswordSchemes = Collections.emptyList();
    }
    else
    {
      supportedAuthPasswordSchemes = Iterables.unmodifiable(Iterables.transform(
          attr,
          Functions.valueToString()));
    }

    attr = getAttribute(ATTR_VENDOR_NAME);
    vendorName = attr == null ? "" : attr.firstValueAsString();

    attr = getAttribute(ATTR_VENDOR_VERSION);
    vendorVersion = attr == null ? "" : attr.firstValueAsString();
  }

  public static RootDSE getRootDSE(Connection connection, Schema schema)
      throws ErrorResultException, InterruptedException, DecodeException,
             SchemaNotFoundException
  {
    SearchResultEntry result = connection.get("", ROOTDSE_ATTRS);
    Entry entry = new SortedEntry(result, schema);

    return new RootDSE(entry);
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
    for(String supported : supportedControls)
    {
      if(supported.equals(oid))
      {
        return true;
      }
    }
    return false;
  }

  public Iterable<String> getSupportedExtendedOperations()
  {
    return supportedExtensions;
  }

  public boolean supportsExtendedOperation(String oid)
  {
    Validator.ensureNotNull(oid);
    for(String supported : supportedExtensions)
    {
      if(supported.equals(oid))
      {
        return true;
      }
    }
    return false;
  }

  public Iterable<String> getSupportedFeatures()
  {
    return supportedFeatures;
  }

  public boolean supportsFeature(String oid)
  {
    Validator.ensureNotNull(oid);
    for(String supported : supportedFeatures)
    {
      if(supported.equals(oid))
      {
        return true;
      }
    }
    return false;
  }

  public Iterable<Integer> getSupportedLDAPVersions()
  {
    return supportedLDAPVerions;
  }

  public boolean supportsLDAPVersion(int version)
  {
    for(int supported : supportedLDAPVerions)
    {
      if(supported == version)
      {
        return true;
      }
    }
    return false;
  }

  public Iterable<String> getSupportedSASLMechanismNames()
  {
    return supportedSASLMechanisms;
  }

  public boolean supportsSASLMechanism(String name)
  {
    Validator.ensureNotNull(name);
    for(String supported : supportedSASLMechanisms)
    {
      if(supported.equals(name))
      {
        return true;
      }
    }
    return false;
  }

  public Iterable<String> getSupportedAuthPasswordSchemes()
  {
    return supportedSASLMechanisms;
  }

  public boolean supportsAuthPasswordScheme(String name)
  {
    Validator.ensureNotNull(name);
    for(String supported : supportedAuthPasswordSchemes)
    {
      if(supported.equals(name))
      {
        return true;
      }
    }
    return false;
  }

  public String getVendorName()
  {
    return vendorName;
  }

  public String getVendorVersion()
  {
    return vendorVersion;
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


  public Entry clearAttributes() throws UnsupportedOperationException
  {
    throw new UnsupportedOperationException();
  }

  public boolean containsAttribute(AttributeDescription attributeDescription)
      throws NullPointerException {
    Validator.ensureNotNull(attributeDescription);

    return entry.containsAttribute(attributeDescription);  }

  public Attribute getAttribute(AttributeDescription attributeDescription)
      throws NullPointerException {
    Validator.ensureNotNull(attributeDescription);

    return entry.getAttribute(attributeDescription);  }

  public int getAttributeCount() {
    return entry.getAttributeCount();
  }

  public Iterable<Attribute> getAttributes() {
    return entry.getAttributes();
  }

  public DN getNameDN() {
    return DN.rootDN();
  }

  public Schema getSchema() {
    return entry.getSchema();
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

  public Entry setNameDN(DN dn) throws UnsupportedOperationException,
      NullPointerException
  {
    throw new UnsupportedOperationException();
  }
}
