package org.opends.common.api.filter;

import org.opends.server.types.ByteString;
import org.opends.server.util.Validator;
import org.opends.server.protocols.asn1.ASN1Writer;
import org.opends.common.protocols.ldap.LDAPEncoder;
import org.opends.common.api.AttributeDescription;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 10, 2009 Time: 12:24:54
 * PM To change this template use File | Settings | File Templates.
 */
public final class ExtensibleFilter extends Filter
{
  private String matchingRule;
  private String attributeDescription;
  private ByteString matchValue;
  private boolean dnAttributes;

  public ExtensibleFilter(String matchingRule, String attributeDescription,
                             ByteString matchValue)
  {
    Validator.ensureNotNull(matchingRule, attributeDescription, matchValue);
    this.matchingRule = matchingRule;
    this.attributeDescription = attributeDescription;
    this.matchValue = matchValue;
    this.dnAttributes = false;
  }

  public ExtensibleFilter(String matchingRule,
                             AttributeDescription attributeDescription,
                             ByteString matchValue)
  {
    Validator.ensureNotNull(matchingRule, attributeDescription, matchValue);
    this.matchingRule = matchingRule;
    this.attributeDescription = attributeDescription.toString();
    this.matchValue = matchValue;
    this.dnAttributes = false;
  }

  public ExtensibleFilter(ByteString matchValue)
  {
    Validator.ensureNotNull(matchValue);
    this.matchingRule = "".intern();
    this.attributeDescription = "".intern();
    this.matchValue = matchValue;
    this.dnAttributes = false;
  }

  public String getMatchingRule()
  {
    return matchingRule;
  }

  public ExtensibleFilter setMatchingRule(String matchingRule)
  {
    Validator.ensureNotNull(matchingRule);
    this.matchingRule = matchingRule;
    return this;
  }

  public String getAttributeDescription()
  {
    return attributeDescription;
  }

  public ExtensibleFilter setAttributeDescription(String attributeDescription)
  {
    Validator.ensureNotNull(attributeDescription);
    this.attributeDescription = attributeDescription;
    return this;
  }

  public ExtensibleFilter setAttributeDescription(AttributeDescription attributeDescription)
  {
    Validator.ensureNotNull(attributeDescription);
    this.attributeDescription = attributeDescription.toString();
    return this;
  }

  public ByteString getMatchValue()
  {
    return matchValue;
  }

  public ExtensibleFilter setMatchValue(ByteString matchValue)
  {
    Validator.ensureNotNull(matchValue);
    this.matchValue = matchValue;
    return this;
  }

  public boolean isDnAttributes()
  {
    return dnAttributes;
  }

  public ExtensibleFilter setDnAttributes(boolean dnAttributes)
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
    buffer.append(", attributeDescription=");
    buffer.append(attributeDescription);
    buffer.append(", matchValue=");
    buffer.append(matchValue);
    buffer.append(", dnAttributes=");
    buffer.append(dnAttributes);
    buffer.append(")");
  }
}
