package org.opends.common.api.controls;

import org.opends.common.api.filter.Filter;
import org.opends.common.api.AttributeDescription;
import org.opends.common.protocols.asn1.ASN1Writer;
import org.opends.server.types.ByteString;
import org.opends.server.util.Validator;
import static org.opends.server.protocols.ldap.LDAPConstants.*;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jun 29, 2009
 * Time: 5:45:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleMatchingFilter extends Filter
{
  private String matchingRule;
  private String attributeDescription;
  private ByteString matchValue;

  /**
   * Either matchingRule or attributeDescription must NOT be
   * <code>null</code>.
   * @param matchingRule
   * @param attributeDescription
   * @param matchValue
   */
  public SimpleMatchingFilter(String matchingRule,
                              String attributeDescription,
                              ByteString matchValue)
  {
    Validator.ensureTrue((Validator.ensureNotNull(matchingRule) &&
        Validator.ensureTrue(matchingRule.length() > 0)) ||
        (Validator.ensureNotNull(attributeDescription) &&
            Validator.ensureTrue(matchingRule.length() > 0)));
    Validator.ensureNotNull(matchValue);
    if(matchingRule == null)
    {
      this.matchingRule = "".intern();
    }
    else
    {
      this.matchingRule = matchingRule;
    }
    if(attributeDescription == null)
    {
      this.attributeDescription = "".intern();
    }
    else
    {
      this.attributeDescription = attributeDescription;
    }
    this.matchValue = matchValue;
  }

  public SimpleMatchingFilter(String matchingRule,
                             AttributeDescription attributeDescription,
                             ByteString matchValue)
  {
    Validator.ensureTrue((Validator.ensureNotNull(matchingRule) &&
        Validator.ensureTrue(matchingRule.length() > 0)) ||
        (Validator.ensureNotNull(attributeDescription) &&
            Validator.ensureTrue(matchingRule.length() > 0)));
    Validator.ensureNotNull(matchValue);
    if(matchingRule == null)
    {
      this.matchingRule = "".intern();
    }
    else
    {
      this.matchingRule = matchingRule;
    }
    if(attributeDescription == null)
    {
      this.attributeDescription = "".intern();
    }
    else
    {
      this.attributeDescription = attributeDescription.toString();
    }
    this.matchValue = matchValue;
  }

  public String getMatchingRule()
  {
    return matchingRule;
  }

  public SimpleMatchingFilter setMatchingRule(String matchingRule)
  {
    Validator.ensureNotNull(matchingRule);
    this.matchingRule = matchingRule;
    return this;
  }

  public String getAttributeDescription()
  {
    return attributeDescription;
  }

  public SimpleMatchingFilter setAttributeDescription(String attributeDescription)
  {
    Validator.ensureNotNull(attributeDescription);
    this.attributeDescription = attributeDescription;
    return this;
  }

  public SimpleMatchingFilter setAttributeDescription(AttributeDescription attributeDescription)
  {
    Validator.ensureNotNull(attributeDescription);
    this.attributeDescription = attributeDescription.toString();
    return this;
  }

  public ByteString getMatchValue()
  {
    return matchValue;
  }

  public SimpleMatchingFilter setMatchValue(ByteString matchValue)
  {
    Validator.ensureNotNull(matchValue);
    this.matchValue = matchValue;
    return this;
  }

  public void encodeLDAP(ASN1Writer writer) throws IOException
  {
    writer.writeStartSequence(TYPE_FILTER_EXTENSIBLE_MATCH);

    String matchingRuleID = getMatchingRule();
    if (matchingRuleID.length() > 0)
    {
      writer.writeOctetString(TYPE_MATCHING_RULE_ID,
                              matchingRuleID);
    }

    String attributeType = getAttributeDescription();
    if (attributeType.length() > 0)
    {
      writer.writeOctetString(TYPE_MATCHING_RULE_TYPE,
                              attributeType);
    }

    writer.writeOctetString(TYPE_MATCHING_RULE_VALUE,
                            getMatchValue());

    writer.writeEndSequence();
  }

  public void toString(StringBuilder buffer)
  {
    buffer.append("SimpleMatchingFilter(matchingRule=");
    buffer.append(matchingRule);
    buffer.append(", attributeDescription=");
    buffer.append(attributeDescription);
    buffer.append(", matchValue=");
    buffer.append(matchValue);
    buffer.append(")");
  }
}
