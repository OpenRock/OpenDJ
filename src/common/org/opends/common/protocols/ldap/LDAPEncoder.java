package org.opends.common.protocols.ldap;

import org.opends.common.api.raw.request.*;
import org.opends.common.api.raw.response.*;
import org.opends.common.api.raw.RawMessage;
import org.opends.common.api.raw.RawControl;
import org.opends.server.protocols.asn1.ASN1Writer;
import static org.opends.server.protocols.ldap.LDAPConstants.*;
import org.opends.server.types.RawAttribute;
import org.opends.server.types.RawModification;

import java.io.IOException;


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
    for(RawAttribute attr : addRequest.getAttributes())
    {
      attr.write(writer);
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
    writer.writeOctetString(bindRequest.getName());
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
    writer.writeOctetString(bindRequest.getName());

    writer.writeStartSequence(TYPE_AUTHENTICATION_SASL);
    writer.writeOctetString(bindRequest.getSaslMechanism());
    if(bindRequest.getSaslCredentials().length() > 0)
    {
      writer.writeOctetString(bindRequest.getSaslCredentials());
    }
    writer.writeEndSequence();

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
    for(RawModification mod : modifyRequest.getModifications())
    {
      mod.write(writer);
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
    writer.writeEnumerated(searchRequest.getScope().intValue());
    writer.writeEnumerated(searchRequest.getDereferencePolicy().intValue());
    writer.writeInteger(searchRequest.getSizeLimit());
    writer.writeInteger(searchRequest.getTimeLimit());
    writer.writeBoolean(searchRequest.isTypesOnly());
    searchRequest.getFilter().write(writer);

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
    writer.writeOctetString(searchResultEntry.getObjectName());

    writer.writeStartSequence();
    for(RawAttribute attr : searchResultEntry.getPartialAttributeList())
    {
      attr.write(writer);
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
    writer.writeStartSequence(TYPE_CONTROL_SEQUENCE);
    for(RawControl control : message.getControls())
    {
      encodeControl(writer, control);
    }
    writer.writeEndSequence();

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

  private static void encodeResultResponseHeader(ASN1Writer writer,
                                                 byte typeTag,
                                                 RawResultResponse rawMessage)
      throws IOException
  {
    writer.writeStartSequence(typeTag);
    writer.writeEnumerated(rawMessage.getResultCode().getIntValue());
    writer.writeOctetString(rawMessage.getMatchedDN());
    writer.writeOctetString(rawMessage.getDiagnosticMessage());

    if (!rawMessage.getReferrals().isEmpty())
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
}
