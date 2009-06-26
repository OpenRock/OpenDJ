package org.opends.common.api.filter;

import org.opends.server.types.ByteString;
import org.opends.server.util.Validator;
import org.opends.server.protocols.asn1.ASN1Writer;
import org.opends.common.protocols.ldap.LDAPEncoder;
import org.opends.common.api.AttributeDescription;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 10, 2009 Time: 12:01:12
 * PM To change this template use File | Settings | File Templates.
 */
public final class SubstringFilter extends Filter
{
  private String attributeDescription;
  private ByteString initialString;
  private List<ByteString> anyStrings;
  private ByteString finalString;

  public SubstringFilter(String attributeDescription,
                            ByteString initialString,
                            ByteString finalString,
                            ByteString... anyStrings)
  {
    Validator.ensureNotNull(attributeDescription);
    Validator.ensureTrue(Validator.ensureNotNull(initialString) ||
                         Validator.ensureNotNull(finalString) ||
                         (Validator.ensureNotNull(anyStrings) &&
                          Validator.ensureTrue(anyStrings.length > 0)));
    this.attributeDescription = attributeDescription;
    this.initialString = initialString;
    this.finalString = finalString;

    if(anyStrings == null)
    {
      this.anyStrings = Collections.emptyList(); 
    }
    else
    {
      this.anyStrings = new ArrayList<ByteString>(anyStrings.length);
      for(ByteString anyString : anyStrings)
      {
        Validator.ensureNotNull(anyString);
        this.anyStrings.add(anyString);
      }
    }
  }

  /**
   *
   * @return
   */
  public String getAttributeDescription()
  {
    return attributeDescription;
  }

  public SubstringFilter setAttributeDescription(String attributeDescription)
  {
    Validator.ensureNotNull(attributeDescription);
    this.attributeDescription = attributeDescription;
    return this;
  }

  public SubstringFilter setAttributeDescription(AttributeDescription attributeDescription)
  {
    Validator.ensureNotNull(attributeDescription);
    this.attributeDescription = attributeDescription.toString();
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

  public SubstringFilter setInitialString(ByteString initialString)
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

  public SubstringFilter addAnyString(ByteString... anyStrings)
  {
    if(anyStrings != null)
    {
      if(this.anyStrings == Collections.EMPTY_LIST)
      {
        this.anyStrings = new ArrayList<ByteString>(anyStrings.length);
      }
      for(ByteString anyString : anyStrings)
      {
        Validator.ensureNotNull(anyString);
        this.anyStrings.add(anyString);
      }
    }
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

  public SubstringFilter setFinalString(ByteString finalString)
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
    buffer.append("SubstringFilter(attributeDescription=");
    buffer.append(attributeDescription);
    buffer.append(", initialString=");
    buffer.append(initialString);
    buffer.append(", anyStrings=");
    buffer.append(anyStrings);
    buffer.append(", finalString=");
    buffer.append(finalString);
    buffer.append(")");
  }
}
