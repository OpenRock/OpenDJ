package org.opends.common.api.raw.request.filter;

import org.opends.server.types.ByteString;
import org.opends.server.util.Validator;
import org.opends.server.protocols.asn1.ASN1Writer;
import org.opends.common.protocols.ldap.LDAPEncoder;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 10, 2009 Time: 12:01:12
 * PM To change this template use File | Settings | File Templates.
 */
public final class RawSubstringFilter extends RawFilter
{
  private String attributeType;
  private ByteString initialString;
  private List<ByteString> anyStrings;
  private ByteString finalString;

  public RawSubstringFilter(String attributeType,
                            ByteString anyString)
  {
    Validator.ensureNotNull(attributeType, anyString);
    this.attributeType = attributeType;
    this.initialString = null;
    this.finalString = null;
    this.anyStrings = new ArrayList<ByteString>(1);
    this.anyStrings.add(anyString);
  }

  public RawSubstringFilter(String attributeType,
                            ByteString initialString,
                            ByteString finalString)
  {
    Validator.ensureNotNull(attributeType);
    Validator.ensureTrue(Validator.ensureNotNull(initialString) ||
                         Validator.ensureNotNull(finalString));
    this.attributeType = attributeType;
    this.initialString = initialString;
    this.finalString = finalString;
    this.anyStrings = Collections.emptyList();
  }

  /**
   *
   * @return
   */
  public String getAttributeType()
  {
    return attributeType;
  }

  public RawSubstringFilter setAttributeType(String attributeType)
  {
    Validator.ensureNotNull(attributeType);
    this.attributeType = attributeType;
    return this;
  }

  /**
   *
   * @return The initial substring to match or <code>null</code> if initial
   * substring matching is not required.
   */
  public ByteString getInitialString()
  {
    return initialString;
  }

  public RawSubstringFilter setInitialString(ByteString initialString)
  {
    this.initialString = initialString;
    return this;
  }

  /**
   *
   * @return The any substrings to match. 
   */
  public Iterable<ByteString> getAnyStrings()
  {
    return anyStrings;
  }

  public RawSubstringFilter addAnyString(ByteString anyString)
  {
    Validator.ensureNotNull(anyString);
    if(anyStrings == Collections.EMPTY_LIST)
    {
      anyStrings = new ArrayList<ByteString>(1);
    }
    anyStrings.add(anyString);
    return this;
  }

  /**
   *
   * @return The final substring to match or <code>null</code> if final
   * substring matching is not required.
   */
  public ByteString getFinalString()
  {
    return finalString;
  }

  public RawSubstringFilter setFinalString(ByteString finalString)
  {
    this.finalString = finalString;
    return this;
  }

  public void encodeLDAP(ASN1Writer writer) throws IOException
  {
    LDAPEncoder.encodeFilter(writer, this);
  }

  public void toString(StringBuilder buffer)
  {
    buffer.append("SubstringFilter(attributeType=");
    buffer.append(attributeType);
    buffer.append(", initialString=");
    buffer.append(initialString);
    buffer.append(", anyStrings=");
    buffer.append(anyStrings);
    buffer.append(", finalString=");
    buffer.append(finalString);
    buffer.append(")");
  }
}
