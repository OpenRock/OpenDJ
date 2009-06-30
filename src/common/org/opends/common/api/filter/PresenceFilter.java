package org.opends.common.api.filter;

import org.opends.server.util.Validator;
import org.opends.common.protocols.ldap.LDAPEncoder;
import org.opends.common.protocols.asn1.ASN1Writer;
import org.opends.common.api.AttributeDescription;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 10, 2009 Time: 12:16:56
 * PM To change this template use File | Settings | File Templates.
 */
public final class PresenceFilter extends Filter
{
  private String attributeDescription;

  public PresenceFilter(String attributeDescription)
  {
    Validator.ensureNotNull(attributeDescription);
    this.attributeDescription = attributeDescription;
  }

  public PresenceFilter(AttributeDescription attributeDescription)
  {
    Validator.ensureNotNull(attributeDescription);
    this.attributeDescription = attributeDescription.toString();
  }

  public String getAttributeDescription()
  {
    return attributeDescription;
  }

  public PresenceFilter setAttributeDescription(String attributeDescription)
  {
    Validator.ensureNotNull(attributeDescription);
    this.attributeDescription = attributeDescription;
    return this;
  }

  public PresenceFilter setAttributeDescription(AttributeDescription attributeDescription)
  {
    Validator.ensureNotNull(attributeDescription);
    this.attributeDescription = attributeDescription.toString();
    return this;
  }

  public void encodeLDAP(ASN1Writer writer) throws IOException
  {
    LDAPEncoder.encodeFilter(writer, this);
  }

  public void toString(StringBuilder buffer)
  {
    buffer.append("PresentFilter(attributeDescription=");
    buffer.append(attributeDescription);
    buffer.append(")");
  }
}
