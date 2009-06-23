package org.opends.common.protocols.ldap;

import org.opends.common.api.raw.RawPartialAttribute;
import org.opends.common.api.raw.RawControl;
import org.opends.common.api.raw.RawMessage;
import org.opends.common.api.raw.request.*;
import org.opends.common.api.raw.request.extended.RawExtendedRequest;
import org.opends.common.api.raw.request.filter.*;
import org.opends.common.api.raw.response.*;
import org.opends.server.protocols.asn1.ASN1Writer;
import static org.opends.server.protocols.ldap.LDAPConstants.*;
import org.opends.server.types.ByteString;

import java.io.IOException;
import java.util.Map;
import java.util.Set;


public class LDAPEncoder
{
  public static void encodeRequest(ASN1Writer writer, int messageID,
                                   RawAbandonRequest abandonRequest)
      throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeInteger(OP_TYPE_ABANDON_REQUEST, abandonRequest.getMessageID());
    encodeMessageFooter(writer, abandonRequest);
  }

  public static void encodeRequest(ASN1Writer writer, int messageID,
                                   RawAddRequest addRequest)
      throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_ADD_REQUEST);
    writer.writeOctetString(addRequest.getDN());

    // Write the attributes
    writer.writeStartSequence();
    for(Set<Map.Entry<String, >>: addRequest.getAttributes())
    {
      writer.writeStartSequence();
      writer.writeOctetString(attribute.getAttributeDescription());

      writer.writeStartSet();
      for(ByteString value : attribute.getAttributeValues())
      {
        writer.writeOctetString(value);
      }
      writer.writeEndSequence();

      writer.writeEndSequence();
    }
    writer.writeEndSequence();

    writer.writeEndSequence();
    encodeMessageFooter(writer, addRequest);
  }

  public static void encodeResponse(ASN1Writer writer, int messageID,
                                    RawAddResponse addResponse)
      throws IOException
  {
    encodeMessageHeader(writer, messageID);
    encodeResultResponseHeader(writer, OP_TYPE_ADD_RESPONSE, addResponse);
    encodeResultResponseFooter(writer);
    encodeMessageFooter(writer, addResponse);
  }

  public static void encodeRequest(ASN1Writer writer, int messageID,
                                   int version,
                                   RawSimpleBindRequest bindRequest)
      throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_BIND_REQUEST);

    writer.writeInteger(version);
    writer.writeOctetString(bindRequest.getBindDN());
    writer.writeOctetString(TYPE_AUTHENTICATION_SIMPLE,
        bindRequest.getSimplePassword());

    writer.writeEndSequence();
    encodeMessageFooter(writer, bindRequest);
  }

  public static void encodeRequest(ASN1Writer writer, int messageID,
                                   int version,
                                   RawSASLBindRequest bindRequest)
      throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_BIND_REQUEST);

    writer.writeInteger(version);
    writer.writeOctetString(bindRequest.getBindDN());

    writer.writeStartSequence(TYPE_AUTHENTICATION_SASL);
    writer.writeOctetString(bindRequest.getSASLMechanism());
    if(bindRequest.getSASLCredentials().length() > 0)
    {
      writer.writeOctetString(bindRequest.getSASLCredentials());
    }
    writer.writeEndSequence();

    writer.writeEndSequence();
    encodeMessageFooter(writer, bindRequest);
  }

  public static void encodeRequest(ASN1Writer writer, int messageID,
                                   int version,
                                   RawUnknownBindRequest bindRequest)
      throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_BIND_REQUEST);

    writer.writeInteger(version);
    writer.writeOctetString(bindRequest.getBindDN());

    writer.writeOctetString(bindRequest.getAuthenticationType(),
                            bindRequest.getAuthenticationBytes());

    writer.writeEndSequence();
    encodeMessageFooter(writer, bindRequest);
  }

  public static void encodeResponse(ASN1Writer writer, int messageID,
                                    RawBindResponse bindResponse)
      throws IOException
  {
    encodeMessageHeader(writer, messageID);
    encodeResultResponseHeader(writer, OP_TYPE_BIND_RESPONSE, bindResponse);

    if (bindResponse.getServerSASLCreds().length() > 0)
    {
      writer.writeOctetString(TYPE_SERVER_SASL_CREDENTIALS,
          bindResponse.getServerSASLCreds());
    }

    encodeResultResponseFooter(writer);
    encodeMessageFooter(writer, bindResponse);
  }

  public static void encodeRequest(ASN1Writer writer, int messageID,
                                   RawCompareRequest compareRequest)
      throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_COMPARE_REQUEST);
    writer.writeOctetString(compareRequest.getDN());

    writer.writeStartSequence();
    writer.writeOctetString(compareRequest.getAttributeDescription());
    writer.writeOctetString(compareRequest.getAssertionValue());
    writer.writeEndSequence();

    writer.writeEndSequence();
    encodeMessageFooter(writer, compareRequest);
  }

  public static void encodeResponse(ASN1Writer writer, int messageID,
                                    RawCompareResponse compareResponse)
      throws IOException
  {
    encodeMessageHeader(writer, messageID);
    encodeResultResponseHeader(writer, OP_TYPE_COMPARE_RESPONSE,
        compareResponse);
    encodeResultResponseFooter(writer);
    encodeMessageFooter(writer, compareResponse);
  }

  public static void encodeRequest(ASN1Writer writer, int messageID,
                                   RawDeleteRequest deleteRequest)
      throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeOctetString(OP_TYPE_DELETE_REQUEST, deleteRequest.getDN());
    encodeMessageFooter(writer, deleteRequest);
  }

  public static void encodeResponse(ASN1Writer writer, int messageID,
                                    RawDeleteResponse deleteResponse)
      throws IOException
  {
    encodeMessageHeader(writer, messageID);
    encodeResultResponseHeader(writer, OP_TYPE_DELETE_RESPONSE, deleteResponse);
    encodeResultResponseFooter(writer);
    encodeMessageFooter(writer, deleteResponse);
  }

  public static void encodeRequest(ASN1Writer writer, int messageID,
                                   RawExtendedRequest extendedRequest)
      throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_EXTENDED_REQUEST);
    writer.writeOctetString(TYPE_EXTENDED_REQUEST_OID,
        extendedRequest.getRequestName());

    if(extendedRequest.getRequestValue().length() > 0)
    {
      writer.writeOctetString(TYPE_EXTENDED_REQUEST_VALUE,
          extendedRequest.getRequestValue());
    }

    writer.writeEndSequence();
    encodeMessageFooter(writer, extendedRequest);
  }

  public static void encodeResponse(ASN1Writer writer, int messageID,
                                    RawExtendedResponse extendedResponse)
      throws IOException
  {
    encodeMessageHeader(writer, messageID);
    encodeResultResponseHeader(writer, OP_TYPE_EXTENDED_RESPONSE,
        extendedResponse);

    if (extendedResponse.getResponseName().length() > 0)
    {
      writer.writeOctetString(TYPE_EXTENDED_RESPONSE_OID,
          extendedResponse.getResponseName());
    }

    if (extendedResponse.getResponseValue().length() > 0)
    {
      writer.writeOctetString(TYPE_EXTENDED_RESPONSE_VALUE,
          extendedResponse.getResponseValue());
    }

    encodeResultResponseFooter(writer);
    encodeMessageFooter(writer, extendedResponse);
  }

  public static void encodeRequest(ASN1Writer writer, int messageID,
                                   RawModifyDNRequest modifyDNRequest)
      throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_MODIFY_DN_REQUEST);
    writer.writeOctetString(modifyDNRequest.getDN());
    writer.writeOctetString(modifyDNRequest.getNewRDN());
    writer.writeBoolean(modifyDNRequest.isDeleteOldRDN());

    if(modifyDNRequest.getNewSuperior().length() > 0)
    {
      writer.writeOctetString(TYPE_MODIFY_DN_NEW_SUPERIOR,
          modifyDNRequest.getNewSuperior());
    }

    writer.writeEndSequence();
    encodeMessageFooter(writer, modifyDNRequest);
  }

  public static void encodeResponse(ASN1Writer writer, int messageID,
                                    RawModifyDNResponse modifyDNResponse)
      throws IOException
  {
    encodeMessageHeader(writer, messageID);
    encodeResultResponseHeader(writer, OP_TYPE_MODIFY_DN_RESPONSE,
        modifyDNResponse);
    encodeResultResponseFooter(writer);
    encodeMessageFooter(writer, modifyDNResponse);
  }

  public static void encodeRequest(ASN1Writer writer, int messageID,
                                   RawModifyRequest modifyRequest)
      throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_MODIFY_REQUEST);
    writer.writeOctetString(modifyRequest.getDN());

    writer.writeStartSequence();
    for(RawModifyRequest.Change change : modifyRequest.getChanges())
    {
      encodeChange(writer, change);
    }
    writer.writeEndSequence();

    writer.writeEndSequence();
    encodeMessageFooter(writer, modifyRequest);
  }

  public static void encodeResponse(ASN1Writer writer, int messageID,
                                    RawModifyResponse modifyResponse)
      throws IOException
  {
    encodeMessageHeader(writer, messageID);
    encodeResultResponseHeader(writer, OP_TYPE_MODIFY_RESPONSE, modifyResponse);
    encodeResultResponseFooter(writer);
    encodeMessageFooter(writer, modifyResponse);
  }

  public static void encodeRequest(ASN1Writer writer, int messageID,
                                   RawSearchRequest searchRequest)
      throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_SEARCH_REQUEST);
    writer.writeOctetString(searchRequest.getBaseDN());
    writer.writeEnumerated(searchRequest.getScope());
    writer.writeEnumerated(searchRequest.getDereferencePolicy());
    writer.writeInteger(searchRequest.getSizeLimit());
    writer.writeInteger(searchRequest.getTimeLimit());
    writer.writeBoolean(searchRequest.isTypesOnly());
    searchRequest.getFilter().encodeLDAP(writer);

    writer.writeStartSequence();
    for(String attribute : searchRequest.getAttributes())
    {
      writer.writeOctetString(attribute);
    }
    writer.writeEndSequence();

    writer.writeEndSequence();
    encodeMessageFooter(writer, searchRequest);
  }

  public static void encodeResponse(ASN1Writer writer, int messageID,
                                    RawSearchResultEntry searchResultEntry)
      throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_SEARCH_RESULT_ENTRY);
    writer.writeOctetString(searchResultEntry.getDn());

    writer.writeStartSequence();
    for(RawPartialAttribute attr : searchResultEntry.getPartialAttributeList())
    {
      encodeAttribute(writer, attr);
    }
    writer.writeEndSequence();

    writer.writeEndSequence();
    encodeMessageFooter(writer, searchResultEntry);
  }

  public static void encodeResponse(ASN1Writer writer, int messageID,
                                 RawSearchResultReference searchResultReference)
      throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_SEARCH_RESULT_REFERENCE);
    for(String url : searchResultReference.getURIs())
    {
      writer.writeOctetString(url);
    }
    writer.writeEndSequence();
    encodeMessageFooter(writer, searchResultReference);
  }

  public static void encodeResponse(ASN1Writer writer, int messageID,
                                    RawSearchResultDone searchResultDone)
      throws IOException
  {
    encodeMessageHeader(writer, messageID);
    encodeResultResponseHeader(writer, OP_TYPE_SEARCH_RESULT_DONE,
        searchResultDone);
    encodeResultResponseFooter(writer);
    encodeMessageFooter(writer, searchResultDone);
  }

  public static void encodeRequest(ASN1Writer writer, int messageID,
                                   RawUnbindRequest unbindRequest)
      throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeNull(OP_TYPE_UNBIND_REQUEST);
    encodeMessageFooter(writer, unbindRequest);
  }

  public static void encodeResponse(ASN1Writer writer, int messageID,
                                   RawIntermediateResponse intermediateResponse)
      throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_INTERMEDIATE_RESPONSE);

    if (intermediateResponse.getResponseName().length() > 0)
    {
      writer.writeOctetString(TYPE_INTERMEDIATE_RESPONSE_OID,
          intermediateResponse.getResponseName());
    }

    if (intermediateResponse.getResponseValue().length() > 0)
    {
      writer.writeOctetString(TYPE_INTERMEDIATE_RESPONSE_VALUE,
          intermediateResponse.getResponseValue());
    }

    writer.writeEndSequence();
    encodeMessageFooter(writer, intermediateResponse);
  }

  private static void encodeMessageHeader(ASN1Writer writer, int messageID)
      throws IOException
  {
    writer.writeStartSequence();
    writer.writeInteger(messageID);
  }

  private static void encodeMessageFooter(ASN1Writer writer, RawMessage message)
      throws IOException
  {
    if(message.hasControls())
    {
      writer.writeStartSequence(TYPE_CONTROL_SEQUENCE);
      for(RawControl control : message.getControls())
      {
        encodeControl(writer, control);
      }
      writer.writeEndSequence();
    }

    writer.writeEndSequence();
  }

  private static void encodeControl(ASN1Writer writer, RawControl control)
      throws IOException
  {
    writer.writeStartSequence();
    writer.writeOctetString(control.getOID());
    if(control.isCritical())
    {
      writer.writeBoolean(control.isCritical());
    }
    if (control.getValue().length() > 0)
    {
      writer.writeOctetString(control.getValue());
    }
    writer.writeEndSequence();
  }

  public static void encodeFilter(ASN1Writer writer, RawOrFilter filter)
      throws IOException
  {
    writer.writeStartSequence(TYPE_FILTER_OR);
    for(RawFilter f : filter.getComponents())
    {
      f.encodeLDAP(writer);
    }
    writer.writeEndSequence();
    return;
  }

  public static void encodeFilter(ASN1Writer writer, RawAndFilter filter)
      throws IOException
  {
    writer.writeStartSequence(TYPE_FILTER_AND);
    for(RawFilter f : filter.getComponents())
    {
      f.encodeLDAP(writer);
    }
    writer.writeEndSequence();
    return;
  }

  public static void encodeFilter(ASN1Writer writer, RawNotFilter filter)
      throws IOException
  {
    writer.writeStartSequence(TYPE_FILTER_NOT);
    filter.getFilter().encodeLDAP(writer);
    writer.writeEndSequence();
  }

  public static void encodeFilter(ASN1Writer writer, RawEqualFilter filter)
      throws IOException
  {
    writer.writeStartSequence(TYPE_FILTER_EQUALITY);
    writer.writeOctetString(filter.getAttributeType());
    writer.writeOctetString(filter.getAssertionValue());
    writer.writeEndSequence();
  }

  public static void encodeFilter(ASN1Writer writer, RawGreaterOrEqualFilter filter)
      throws IOException
  {
    writer.writeStartSequence(TYPE_FILTER_GREATER_OR_EQUAL);
    writer.writeOctetString(filter.getAttributeType());
    writer.writeOctetString(filter.getAssertionValue());
    writer.writeEndSequence();
  }

  public static void encodeFilter(ASN1Writer writer, RawLessOrEqualFilter filter)
      throws IOException
  {
    writer.writeStartSequence(TYPE_FILTER_LESS_OR_EQUAL);
    writer.writeOctetString(filter.getAttributeType());
    writer.writeOctetString(filter.getAssertionValue());
    writer.writeEndSequence();
  }

  public static void encodeFilter(ASN1Writer writer, RawApproximateFilter filter)
      throws IOException
  {
    writer.writeStartSequence(TYPE_FILTER_APPROXIMATE);
    writer.writeOctetString(filter.getAttributeType());
    writer.writeOctetString(filter.getAssertionValue());
    writer.writeEndSequence();
  }

  public static void encodeFilter(ASN1Writer writer, RawSubstringFilter filter)
      throws IOException
  {
    writer.writeStartSequence(TYPE_FILTER_APPROXIMATE);
    writer.writeOctetString(filter.getAttributeType());

    writer.writeStartSequence();
    ByteString subInitialElement = filter.getInitialString();
    if (subInitialElement != null)
    {
      writer.writeOctetString(TYPE_SUBINITIAL, subInitialElement);
    }

    for (ByteString s : filter.getAnyStrings())
    {
      writer.writeOctetString(TYPE_SUBANY, s);
    }

    ByteString subFinalElement = filter.getFinalString();
    if (subFinalElement != null)
    {
      writer.writeOctetString(TYPE_SUBFINAL, subFinalElement);
    }
    writer.writeEndSequence();

    writer.writeEndSequence();
  }

  public static void encodeFilter(ASN1Writer writer, RawPresenceFilter filter)
      throws IOException
  {
    writer.writeOctetString(TYPE_FILTER_PRESENCE, filter.getAttributeType());
  }

  public static void encodeFilter(ASN1Writer writer, RawExtensibleFilter filter)
      throws IOException
  {
    writer.writeStartSequence(TYPE_FILTER_EXTENSIBLE_MATCH);

    String matchingRuleID = filter.getMatchingRule();
    if (matchingRuleID.length() > 0)
    {
      writer.writeOctetString(TYPE_MATCHING_RULE_ID,
                              matchingRuleID);
    }

    String attributeType = filter.getAttributeType();
    if (attributeType.length() > 0)
    {
      writer.writeOctetString(TYPE_MATCHING_RULE_TYPE,
                              attributeType);
    }

    writer.writeOctetString(TYPE_MATCHING_RULE_VALUE,
                            filter.getMatchValue());

    if (filter.isDnAttributes())
    {
      writer.writeBoolean(TYPE_MATCHING_RULE_DN_ATTRIBUTES, true);
    }

    writer.writeEndSequence();
  }

  public static void encodeFilter(ASN1Writer writer, RawUnknownFilter filter)
      throws IOException
  {
    writer.writeOctetString(filter.getFilterTag(), filter.getFilterBytes());
  }

  private static void encodeResultResponseHeader(ASN1Writer writer,
                                                 byte typeTag,
                                                 RawResultResponse rawMessage)
      throws IOException
  {
    writer.writeStartSequence(typeTag);
    writer.writeEnumerated(rawMessage.getResultCode());
    writer.writeOctetString(rawMessage.getMatchedDN());
    writer.writeOctetString(rawMessage.getDiagnosticMessage());

    if (rawMessage.hasReferrals())
    {
      writer.writeStartSequence(TYPE_REFERRAL_SEQUENCE);
      for (String s : rawMessage.getReferrals())
      {
        writer.writeOctetString(s);
      }
      writer.writeEndSequence();
    }
  }

  private static void encodeResultResponseFooter(ASN1Writer writer)
      throws IOException
  {
    writer.writeEndSequence();
  }

  private static void encodeAttribute(ASN1Writer writer,
                                      RawPartialAttribute attribute)
      throws IOException
  {
    writer.writeStartSequence();
    writer.writeOctetString(attribute.getAttributeDescription());

    writer.writeStartSet();
    for(ByteString value : attribute.getAttributeValues())
    {
      writer.writeOctetString(value);
    }
    writer.writeEndSequence();

    writer.writeEndSequence();
  }

  private static void encodeChange(ASN1Writer writer, RawModifyRequest.Change change)
      throws IOException
  {
    writer.writeStartSequence();
    writer.writeEnumerated(change.getModificationType().intValue());
    writer.writeStartSequence();
    writer.writeOctetString(change.getAttributeDescription());
    writer.writeStartSet();
    for(ByteString value : change.getAttributeValues())
    {
      writer.writeOctetString(value);
    }
    writer.writeEndSequence();
    writer.writeEndSequence();
    writer.writeEndSequence();
  }
}
