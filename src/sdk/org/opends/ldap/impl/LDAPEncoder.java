package org.opends.ldap.impl;



import static org.opends.server.protocols.ldap.LDAPConstants.*;

import java.io.IOException;

import org.opends.asn1.ASN1Writer;
import org.opends.ldap.Control;
import org.opends.ldap.Message;
import org.opends.ldap.requests.AbandonRequest;
import org.opends.ldap.requests.AddRequest;
import org.opends.ldap.requests.CompareRequest;
import org.opends.ldap.requests.DeleteRequest;
import org.opends.ldap.requests.ExtendedRequest;
import org.opends.ldap.requests.GenericBindRequest;
import org.opends.ldap.requests.ModifyDNRequest;
import org.opends.ldap.requests.ModifyRequest;
import org.opends.ldap.requests.SearchRequest;
import org.opends.ldap.requests.SimpleBindRequest;
import org.opends.ldap.requests.UnbindRequest;
import org.opends.ldap.responses.AddResponse;
import org.opends.ldap.responses.BindResponse;
import org.opends.ldap.responses.CompareResponse;
import org.opends.ldap.responses.DeleteResponse;
import org.opends.ldap.responses.ExtendedResponse;
import org.opends.ldap.responses.IntermediateResponse;
import org.opends.ldap.responses.ModifyDNResponse;
import org.opends.ldap.responses.ModifyResponse;
import org.opends.ldap.responses.ResultResponse;
import org.opends.ldap.responses.SearchResultDone;
import org.opends.ldap.responses.SearchResultEntry;
import org.opends.ldap.responses.SearchResultReference;
import org.opends.ldap.sasl.SASLBindRequest;
import org.opends.server.types.ByteString;
import org.opends.types.Attribute;
import org.opends.types.Change;



public class LDAPEncoder
{
  public static void encodeAttribute(ASN1Writer writer,
      Attribute attribute) throws IOException
  {
    writer.writeStartSequence();
    writer.writeOctetString(attribute.getAttributeDescription());

    writer.writeStartSet();
    for (ByteString value : attribute.getAttributeValues())
    {
      writer.writeOctetString(value);
    }
    writer.writeEndSequence();

    writer.writeEndSequence();
  }



  public static void encodeControl(ASN1Writer writer, Control control)
      throws IOException
  {
    writer.writeStartSequence();
    writer.writeOctetString(control.getOID());
    if (control.isCritical())
    {
      writer.writeBoolean(control.isCritical());
    }
    if (control.getValue().length() > 0)
    {
      writer.writeOctetString(control.getValue());
    }
    writer.writeEndSequence();
  }



  public static void encodeEntry(ASN1Writer writer,
      SearchResultEntry searchResultEntry) throws IOException
  {
    writer.writeStartSequence(OP_TYPE_SEARCH_RESULT_ENTRY);
    writer.writeOctetString(searchResultEntry.getDn());

    writer.writeStartSequence();
    for (Attribute attr : searchResultEntry.getAttributes())
    {
      encodeAttribute(writer, attr);
    }
    writer.writeEndSequence();
    writer.writeEndSequence();
  }



  public static void encodeRequest(ASN1Writer writer, int messageID,
      AbandonRequest abandonRequest) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeInteger(OP_TYPE_ABANDON_REQUEST, abandonRequest
        .getMessageID());
    encodeMessageFooter(writer, abandonRequest);
  }



  public static void encodeRequest(ASN1Writer writer, int messageID,
      AddRequest addRequest) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_ADD_REQUEST);
    writer.writeOctetString(addRequest.getDN());

    // Write the attributes
    writer.writeStartSequence();
    for (Attribute attr : addRequest.getAttributes())
    {
      encodeAttribute(writer, attr);
    }
    writer.writeEndSequence();

    writer.writeEndSequence();
    encodeMessageFooter(writer, addRequest);
  }



  public static void encodeRequest(ASN1Writer writer, int messageID,
      CompareRequest compareRequest) throws IOException
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



  public static void encodeRequest(ASN1Writer writer, int messageID,
      DeleteRequest deleteRequest) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeOctetString(OP_TYPE_DELETE_REQUEST, deleteRequest
        .getDN());
    encodeMessageFooter(writer, deleteRequest);
  }



  public static void encodeRequest(ASN1Writer writer, int messageID,
      ExtendedRequest extendedRequest) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_EXTENDED_REQUEST);
    writer.writeOctetString(TYPE_EXTENDED_REQUEST_OID, extendedRequest
        .getRequestName());

    ByteString requestValue = extendedRequest.getRequestValue();
    if (requestValue != null)
    {
      writer
          .writeOctetString(TYPE_EXTENDED_REQUEST_VALUE, requestValue);
    }

    writer.writeEndSequence();
    encodeMessageFooter(writer, extendedRequest);
  }



  public static void encodeRequest(ASN1Writer writer, int messageID,
      int version, GenericBindRequest bindRequest) throws IOException
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



  public static void encodeRequest(ASN1Writer writer, int messageID,
      int version, SASLBindRequest bindRequest) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_BIND_REQUEST);

    writer.writeInteger(version);
    writer.writeOctetString(bindRequest.getBindDN());

    writer.writeStartSequence(TYPE_AUTHENTICATION_SASL);
    writer.writeOctetString(bindRequest.getSASLMechanism());
    if (bindRequest.getSASLCredentials() != null)
    {
      writer.writeOctetString(bindRequest.getSASLCredentials());
    }
    writer.writeEndSequence();

    writer.writeEndSequence();
    encodeMessageFooter(writer, bindRequest);
  }



  public static void encodeRequest(ASN1Writer writer, int messageID,
      int version, SimpleBindRequest bindRequest) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_BIND_REQUEST);

    writer.writeInteger(version);
    writer.writeOctetString(bindRequest.getBindDN());
    writer.writeOctetString(TYPE_AUTHENTICATION_SIMPLE, bindRequest
        .getSimplePassword());

    writer.writeEndSequence();
    encodeMessageFooter(writer, bindRequest);
  }



  public static void encodeRequest(ASN1Writer writer, int messageID,
      ModifyDNRequest modifyDNRequest) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_MODIFY_DN_REQUEST);
    writer.writeOctetString(modifyDNRequest.getDN());
    writer.writeOctetString(modifyDNRequest.getNewRDN());
    writer.writeBoolean(modifyDNRequest.isDeleteOldRDN());

    if (modifyDNRequest.getNewSuperior().length() > 0)
    {
      writer.writeOctetString(TYPE_MODIFY_DN_NEW_SUPERIOR,
          modifyDNRequest.getNewSuperior());
    }

    writer.writeEndSequence();
    encodeMessageFooter(writer, modifyDNRequest);
  }



  public static void encodeRequest(ASN1Writer writer, int messageID,
      ModifyRequest modifyRequest) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_MODIFY_REQUEST);
    writer.writeOctetString(modifyRequest.getDN());

    writer.writeStartSequence();
    for (Change change : modifyRequest.getChanges())
    {
      encodeChange(writer, change);
    }
    writer.writeEndSequence();

    writer.writeEndSequence();
    encodeMessageFooter(writer, modifyRequest);
  }



  public static void encodeRequest(ASN1Writer writer, int messageID,
      SearchRequest searchRequest) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_SEARCH_REQUEST);
    writer.writeOctetString(searchRequest.getBaseDN());
    writer.writeEnumerated(searchRequest.getScope().intValue());
    writer.writeEnumerated(searchRequest.getDereferencePolicy()
        .intValue());
    writer.writeInteger(searchRequest.getSizeLimit());
    writer.writeInteger(searchRequest.getTimeLimit());
    writer.writeBoolean(searchRequest.isTypesOnly());
    searchRequest.getFilter().encode(writer);

    writer.writeStartSequence();
    for (String attribute : searchRequest.getAttributes())
    {
      writer.writeOctetString(attribute);
    }
    writer.writeEndSequence();

    writer.writeEndSequence();
    encodeMessageFooter(writer, searchRequest);
  }



  public static void encodeRequest(ASN1Writer writer, int messageID,
      UnbindRequest unbindRequest) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeNull(OP_TYPE_UNBIND_REQUEST);
    encodeMessageFooter(writer, unbindRequest);
  }



  public static void encodeResponse(ASN1Writer writer, int messageID,
      AddResponse addResponse) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    encodeResultResponseHeader(writer, OP_TYPE_ADD_RESPONSE,
        addResponse);
    encodeResultResponseFooter(writer);
    encodeMessageFooter(writer, addResponse);
  }



  public static void encodeResponse(ASN1Writer writer, int messageID,
      BindResponse bindResponse) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    encodeResultResponseHeader(writer, OP_TYPE_BIND_RESPONSE,
        bindResponse);

    if (bindResponse.getServerSASLCreds().length() > 0)
    {
      writer.writeOctetString(TYPE_SERVER_SASL_CREDENTIALS,
          bindResponse.getServerSASLCreds());
    }

    encodeResultResponseFooter(writer);
    encodeMessageFooter(writer, bindResponse);
  }



  public static void encodeResponse(ASN1Writer writer, int messageID,
      CompareResponse compareResponse) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    encodeResultResponseHeader(writer, OP_TYPE_COMPARE_RESPONSE,
        compareResponse);
    encodeResultResponseFooter(writer);
    encodeMessageFooter(writer, compareResponse);
  }



  public static void encodeResponse(ASN1Writer writer, int messageID,
      DeleteResponse deleteResponse) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    encodeResultResponseHeader(writer, OP_TYPE_DELETE_RESPONSE,
        deleteResponse);
    encodeResultResponseFooter(writer);
    encodeMessageFooter(writer, deleteResponse);
  }



  public static void encodeResponse(ASN1Writer writer, int messageID,
      ExtendedResponse extendedResponse) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    encodeResultResponseHeader(writer, OP_TYPE_EXTENDED_RESPONSE,
        extendedResponse);

    String responseName = extendedResponse.getResponseName();
    ByteString responseValue = extendedResponse.getResponseValue();

    if (responseName != null)
    {
      writer.writeOctetString(TYPE_EXTENDED_RESPONSE_OID, responseName);
    }

    if (responseValue != null)
    {
      writer.writeOctetString(TYPE_EXTENDED_RESPONSE_VALUE,
          responseValue);
    }

    encodeResultResponseFooter(writer);
    encodeMessageFooter(writer, extendedResponse);
  }



  public static void encodeResponse(ASN1Writer writer, int messageID,
      IntermediateResponse intermediateResponse) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_INTERMEDIATE_RESPONSE);

    String responseName = intermediateResponse.getResponseName();
    ByteString responseValue = intermediateResponse.getResponseValue();

    if (responseName != null)
    {
      writer.writeOctetString(TYPE_INTERMEDIATE_RESPONSE_OID,
          intermediateResponse.getResponseName());
    }

    if (responseValue != null)
    {
      writer.writeOctetString(TYPE_INTERMEDIATE_RESPONSE_VALUE,
          intermediateResponse.getResponseValue());
    }

    writer.writeEndSequence();
    encodeMessageFooter(writer, intermediateResponse);
  }



  public static void encodeResponse(ASN1Writer writer, int messageID,
      ModifyDNResponse modifyDNResponse) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    encodeResultResponseHeader(writer, OP_TYPE_MODIFY_DN_RESPONSE,
        modifyDNResponse);
    encodeResultResponseFooter(writer);
    encodeMessageFooter(writer, modifyDNResponse);
  }



  public static void encodeResponse(ASN1Writer writer, int messageID,
      ModifyResponse modifyResponse) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    encodeResultResponseHeader(writer, OP_TYPE_MODIFY_RESPONSE,
        modifyResponse);
    encodeResultResponseFooter(writer);
    encodeMessageFooter(writer, modifyResponse);
  }



  public static void encodeResponse(ASN1Writer writer, int messageID,
      SearchResultDone searchResultDone) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    encodeResultResponseHeader(writer, OP_TYPE_SEARCH_RESULT_DONE,
        searchResultDone);
    encodeResultResponseFooter(writer);
    encodeMessageFooter(writer, searchResultDone);
  }



  public static void encodeResponse(ASN1Writer writer, int messageID,
      SearchResultEntry searchResultEntry) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    encodeEntry(writer, searchResultEntry);
    encodeMessageFooter(writer, searchResultEntry);
  }



  public static void encodeResponse(ASN1Writer writer, int messageID,
      SearchResultReference searchResultReference) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_SEARCH_RESULT_REFERENCE);
    for (String url : searchResultReference.getURIs())
    {
      writer.writeOctetString(url);
    }
    writer.writeEndSequence();
    encodeMessageFooter(writer, searchResultReference);
  }



  private static void encodeChange(ASN1Writer writer, Change change)
      throws IOException
  {
    writer.writeStartSequence();
    writer.writeEnumerated(change.getModificationType().intValue());
    encodeAttribute(writer, change.getModification());
    writer.writeEndSequence();
  }



  private static void encodeMessageFooter(ASN1Writer writer,
      Message message) throws IOException
  {
    if (message.hasControls())
    {
      writer.writeStartSequence(TYPE_CONTROL_SEQUENCE);
      for (Control control : message.getControls())
      {
        encodeControl(writer, control);
      }
      writer.writeEndSequence();
    }

    writer.writeEndSequence();
  }



  private static void encodeMessageHeader(ASN1Writer writer,
      int messageID) throws IOException
  {
    writer.writeStartSequence();
    writer.writeInteger(messageID);
  }



  private static void encodeResultResponseFooter(ASN1Writer writer)
      throws IOException
  {
    writer.writeEndSequence();
  }



  private static void encodeResultResponseHeader(ASN1Writer writer,
      byte typeTag, ResultResponse rawMessage) throws IOException
  {
    writer.writeStartSequence(typeTag);
    writer.writeEnumerated(rawMessage.getResultCode().intValue());
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
}
