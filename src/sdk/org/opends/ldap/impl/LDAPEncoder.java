package org.opends.ldap.impl;



import static org.opends.server.protocols.ldap.LDAPConstants.*;

import java.io.IOException;

import org.opends.asn1.ASN1Writer;
import org.opends.ldap.controls.Control;
import org.opends.ldap.requests.AbandonRequest;
import org.opends.ldap.requests.AddRequest;
import org.opends.ldap.requests.CompareRequest;
import org.opends.ldap.requests.DeleteRequest;
import org.opends.ldap.requests.ExtendedRequest;
import org.opends.ldap.requests.GenericBindRequest;
import org.opends.ldap.requests.ModifyDNRequest;
import org.opends.ldap.requests.ModifyRequest;
import org.opends.ldap.requests.Request;
import org.opends.ldap.requests.SearchRequest;
import org.opends.ldap.requests.SimpleBindRequest;
import org.opends.ldap.requests.UnbindRequest;
import org.opends.ldap.responses.BindResult;
import org.opends.ldap.responses.CompareResult;
import org.opends.ldap.responses.IntermediateResponse;
import org.opends.ldap.responses.Response;
import org.opends.ldap.responses.Result;
import org.opends.ldap.responses.SearchResult;
import org.opends.ldap.responses.SearchResultEntry;
import org.opends.ldap.responses.SearchResultReference;
import org.opends.ldap.sasl.SASLBindRequest;
import org.opends.server.types.ByteString;
import org.opends.spi.AbstractExtendedResult;
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
    for (ByteString value : attribute)
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
    writer.writeOctetString(searchResultEntry.getDN());

    writer.writeStartSequence();
    for (Attribute attr : searchResultEntry.getAttributes())
    {
      encodeAttribute(writer, attr);
    }
    writer.writeEndSequence();
    writer.writeEndSequence();
  }



  public static void encodeAbandonRequest(ASN1Writer writer,
      int messageID, AbandonRequest request) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer
        .writeInteger(OP_TYPE_ABANDON_REQUEST, request.getMessageID());
    encodeMessageFooter(writer, request);
  }



  public static void encodeAddRequest(ASN1Writer writer, int messageID,
      AddRequest request) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_ADD_REQUEST);
    writer.writeOctetString(request.getDN());

    // Write the attributes
    writer.writeStartSequence();
    for (Attribute attr : request.getAttributes())
    {
      encodeAttribute(writer, attr);
    }
    writer.writeEndSequence();

    writer.writeEndSequence();
    encodeMessageFooter(writer, request);
  }



  public static void encodeCompareRequest(ASN1Writer writer,
      int messageID, CompareRequest request) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_COMPARE_REQUEST);
    writer.writeOctetString(request.getDN());

    writer.writeStartSequence();
    writer.writeOctetString(request.getAttributeDescription());
    writer.writeOctetString(request.getAssertionValue());
    writer.writeEndSequence();

    writer.writeEndSequence();
    encodeMessageFooter(writer, request);
  }



  public static void encodeDeleteRequest(ASN1Writer writer,
      int messageID, DeleteRequest request) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeOctetString(OP_TYPE_DELETE_REQUEST, request.getDN());
    encodeMessageFooter(writer, request);
  }



  public static void encodeExtendedRequest(ASN1Writer writer,
      int messageID, ExtendedRequest request) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_EXTENDED_REQUEST);
    writer.writeOctetString(TYPE_EXTENDED_REQUEST_OID, request
        .getRequestName());

    ByteString requestValue = request.getRequestValue();
    if (requestValue != null)
    {
      writer
          .writeOctetString(TYPE_EXTENDED_REQUEST_VALUE, requestValue);
    }

    writer.writeEndSequence();
    encodeMessageFooter(writer, request);
  }



  public static void encodeBindRequest(ASN1Writer writer,
      int messageID, int version, GenericBindRequest request)
      throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_BIND_REQUEST);

    writer.writeInteger(version);
    writer.writeOctetString(request.getBindDN());

    writer.writeOctetString(request.getAuthenticationType(), request
        .getAuthenticationBytes());

    writer.writeEndSequence();
    encodeMessageFooter(writer, request);
  }



  public static void encodeBindRequest(ASN1Writer writer,
      int messageID, int version, SASLBindRequest request)
      throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_BIND_REQUEST);

    writer.writeInteger(version);
    writer.writeOctetString(request.getBindDN());

    writer.writeStartSequence(TYPE_AUTHENTICATION_SASL);
    writer.writeOctetString(request.getSASLMechanism());
    if (request.getSASLCredentials() != null)
    {
      writer.writeOctetString(request.getSASLCredentials());
    }
    writer.writeEndSequence();

    writer.writeEndSequence();
    encodeMessageFooter(writer, request);
  }



  public static void encodeBindRequest(ASN1Writer writer,
      int messageID, int version, SimpleBindRequest request)
      throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_BIND_REQUEST);

    writer.writeInteger(version);
    writer.writeOctetString(request.getBindDN());
    writer.writeOctetString(TYPE_AUTHENTICATION_SIMPLE, request
        .getSimplePassword());

    writer.writeEndSequence();
    encodeMessageFooter(writer, request);
  }



  public static void encodeModifyDNRequest(ASN1Writer writer,
      int messageID, ModifyDNRequest request) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_MODIFY_DN_REQUEST);
    writer.writeOctetString(request.getDN());
    writer.writeOctetString(request.getNewRDN());
    writer.writeBoolean(request.isDeleteOldRDN());

    if (request.getNewSuperior().length() > 0)
    {
      writer.writeOctetString(TYPE_MODIFY_DN_NEW_SUPERIOR, request
          .getNewSuperior());
    }

    writer.writeEndSequence();
    encodeMessageFooter(writer, request);
  }



  public static void encodeModifyRequest(ASN1Writer writer,
      int messageID, ModifyRequest request) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_MODIFY_REQUEST);
    writer.writeOctetString(request.getDN());

    writer.writeStartSequence();
    for (Change change : request.getChanges())
    {
      encodeChange(writer, change);
    }
    writer.writeEndSequence();

    writer.writeEndSequence();
    encodeMessageFooter(writer, request);
  }



  public static void encodeSearchRequest(ASN1Writer writer,
      int messageID, SearchRequest request) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_SEARCH_REQUEST);
    writer.writeOctetString(request.getBaseDN());
    writer.writeEnumerated(request.getScope().intValue());
    writer.writeEnumerated(request.getDereferencePolicy().intValue());
    writer.writeInteger(request.getSizeLimit());
    writer.writeInteger(request.getTimeLimit());
    writer.writeBoolean(request.isTypesOnly());
    request.getFilter().encode(writer);

    writer.writeStartSequence();
    for (String attribute : request.getAttributes())
    {
      writer.writeOctetString(attribute);
    }
    writer.writeEndSequence();

    writer.writeEndSequence();
    encodeMessageFooter(writer, request);
  }



  public static void encodeUnbindRequest(ASN1Writer writer,
      int messageID, UnbindRequest request) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeNull(OP_TYPE_UNBIND_REQUEST);
    encodeMessageFooter(writer, request);
  }



  public static void encodeAddResult(ASN1Writer writer, int messageID,
      Result result) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    encodeResultHeader(writer, OP_TYPE_ADD_RESPONSE, result);
    encodeResultFooter(writer);
    encodeMessageFooter(writer, result);
  }



  public static void encodeBindResult(ASN1Writer writer, int messageID,
      BindResult result) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    encodeResultHeader(writer, OP_TYPE_BIND_RESPONSE, result);

    if (result.getServerSASLCredentials().length() > 0)
    {
      writer.writeOctetString(TYPE_SERVER_SASL_CREDENTIALS, result
          .getServerSASLCredentials());
    }

    encodeResultFooter(writer);
    encodeMessageFooter(writer, result);
  }



  public static void encodeCompareResult(ASN1Writer writer,
      int messageID, CompareResult result) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    encodeResultHeader(writer, OP_TYPE_COMPARE_RESPONSE, result);
    encodeResultFooter(writer);
    encodeMessageFooter(writer, result);
  }



  public static void encodeDeleteResult(ASN1Writer writer,
      int messageID, Result result) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    encodeResultHeader(writer, OP_TYPE_DELETE_RESPONSE, result);
    encodeResultFooter(writer);
    encodeMessageFooter(writer, result);
  }



  public static void encodeExtendedResult(ASN1Writer writer,
      int messageID, AbstractExtendedResult result) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    encodeResultHeader(writer, OP_TYPE_EXTENDED_RESPONSE, result);

    String responseName = result.getResponseName();
    ByteString responseValue = result.getResponseValue();

    if (responseName != null)
    {
      writer.writeOctetString(TYPE_EXTENDED_RESPONSE_OID, responseName);
    }

    if (responseValue != null)
    {
      writer.writeOctetString(TYPE_EXTENDED_RESPONSE_VALUE,
          responseValue);
    }

    encodeResultFooter(writer);
    encodeMessageFooter(writer, result);
  }



  public static void encodeIntermediateResponse(ASN1Writer writer,
      int messageID, IntermediateResponse response) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_INTERMEDIATE_RESPONSE);

    String responseName = response.getResponseName();
    ByteString responseValue = response.getResponseValue();

    if (responseName != null)
    {
      writer.writeOctetString(TYPE_INTERMEDIATE_RESPONSE_OID, response
          .getResponseName());
    }

    if (responseValue != null)
    {
      writer.writeOctetString(TYPE_INTERMEDIATE_RESPONSE_VALUE,
          response.getResponseValue());
    }

    writer.writeEndSequence();
    encodeMessageFooter(writer, response);
  }



  public static void encodeModifyDNResult(ASN1Writer writer,
      int messageID, Result result) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    encodeResultHeader(writer, OP_TYPE_MODIFY_DN_RESPONSE, result);
    encodeResultFooter(writer);
    encodeMessageFooter(writer, result);
  }



  public static void encodeModifyResult(ASN1Writer writer,
      int messageID, Result result) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    encodeResultHeader(writer, OP_TYPE_MODIFY_RESPONSE, result);
    encodeResultFooter(writer);
    encodeMessageFooter(writer, result);
  }



  public static void encodeSearchResult(ASN1Writer writer,
      int messageID, SearchResult result) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    encodeResultHeader(writer, OP_TYPE_SEARCH_RESULT_DONE, result);
    encodeResultFooter(writer);
    encodeMessageFooter(writer, result);
  }



  public static void encodeSearchResultEntry(ASN1Writer writer,
      int messageID, SearchResultEntry entry) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    encodeEntry(writer, entry);
    encodeMessageFooter(writer, entry);
  }



  public static void encodeSearchResultReference(ASN1Writer writer,
      int messageID, SearchResultReference reference)
      throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_SEARCH_RESULT_REFERENCE);
    for (String url : reference.getURIs())
    {
      writer.writeOctetString(url);
    }
    writer.writeEndSequence();
    encodeMessageFooter(writer, reference);
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
      Request request) throws IOException
  {
    if (request.hasControls())
    {
      writer.writeStartSequence(TYPE_CONTROL_SEQUENCE);
      for (Control control : request.getControls())
      {
        encodeControl(writer, control);
      }
      writer.writeEndSequence();
    }

    writer.writeEndSequence();
  }



  private static void encodeMessageFooter(ASN1Writer writer,
      Response response) throws IOException
  {
    if (response.hasControls())
    {
      writer.writeStartSequence(TYPE_CONTROL_SEQUENCE);
      for (Control control : response.getControls())
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



  private static void encodeResultFooter(ASN1Writer writer)
      throws IOException
  {
    writer.writeEndSequence();
  }



  private static void encodeResultHeader(ASN1Writer writer,
      byte typeTag, Result rawMessage) throws IOException
  {
    writer.writeStartSequence(typeTag);
    writer.writeEnumerated(rawMessage.getResultCode().intValue());
    writer.writeOctetString(rawMessage.getMatchedDN());
    writer.writeOctetString(rawMessage.getDiagnosticMessage());

    if (rawMessage.hasReferralURIs())
    {
      writer.writeStartSequence(TYPE_REFERRAL_SEQUENCE);
      for (String s : rawMessage.getReferralURIs())
      {
        writer.writeOctetString(s);
      }
      writer.writeEndSequence();
    }
  }
}
