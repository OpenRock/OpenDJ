package org.opends.common.api.raw.request.filter;

import org.opends.server.types.ByteString;
import org.opends.server.util.Validator;
import org.opends.server.protocols.asn1.ASN1Writer;
import org.opends.common.protocols.ldap.LDAPEncoder;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 10, 2009 Time: 12:24:54
 * PM To change this template use File | Settings | File Templates.
 */
public final class RawExtensibleFilter extends RawFilter
{
  private String matchingRule;
  private String attributeType;
  private ByteString matchValue;
  private boolean dnAttributes;

  public RawExtensibleFilter(String matchingRule, String attributeType,
                             ByteString matchValue)
  {
    Validator.ensureNotNull(matchingRule, attributeType, matchValue);
    this.matchingRule = matchingRule;
    this.attributeType = attributeType;
    this.matchValue = matchValue;
    this.dnAttributes = false;
  }

  public RawExtensibleFilter(ByteString matchValue)
  {
    Validator.ensureNotNull(matchValue);
    this.matchingRule = "".intern();
    this.attributeType = "".intern();
    this.matchValue = matchValue;
    this.dnAttributes = false;
  }

  public String getMatchingRule()
  {
    return matchingRule;
  }

  public RawExtensibleFilter setMatchingRule(String matchingRule)
  {
    Validator.ensureNotNull(matchingRule);
    this.matchingRule = matchingRule;
    return this;
  }

  public String getAttributeType()
  {
    return attributeType;
  }

  public RawExtensibleFilter setAttributeType(String attributeType)
  {
    Validator.ensureNotNull(attributeType);
    this.attributeType = attributeType;
    return this;
  }

  public ByteString getMatchValue()
  {
    return matchValue;
  }

  public RawExtensibleFilter setMatchValue(ByteString matchValue)
  {
    Validator.ensureNotNull(matchValue);
    this.matchValue = matchValue;
    return this;
  }

  public boolean isDnAttributes()
  {
    return dnAttributes;
  }

  public RawExtensibleFilter setDnAttributes(boolean dnAttributes)
  {
    this.dnAttributes = dnAttributes;
    return this;
  }

  public void encodeLDAP(ASN1Writer writer) throws IOException
  {
    LDAPEncoder.encodeFilter(writer, this);
  }

  public void toString(StringBuilder buffer)
  {
    buffer.append("ExtensibleFilter(matchingRule=");
    buffer.append(matchingRule);
    buffer.append(", attributeType=");
    buffer.append(attributeType);
    buffer.append(", matchValue=");
    buffer.append(matchValue);
    buffer.append(", dnAttributes=");
    buffer.append(dnAttributes);
    buffer.append(")");
  }
}
