package org.opends.sdk.ldap;



import static org.opends.server.protocols.ldap.LDAPConstants.*;

import java.io.IOException;

import org.opends.sdk.AbandonRequest;
import org.opends.sdk.AddRequest;
import org.opends.sdk.AttributeValueSequence;
import org.opends.sdk.BindResult;
import org.opends.sdk.Change;
import org.opends.sdk.CompareRequest;
import org.opends.sdk.CompareResult;
import org.opends.sdk.DeleteRequest;
import org.opends.sdk.ExtendedRequest;
import org.opends.sdk.ExtendedResult;
import org.opends.sdk.GenericBindRequest;
import org.opends.sdk.IntermediateResponse;
import org.opends.sdk.ModifyDNRequest;
import org.opends.sdk.ModifyRequest;
import org.opends.sdk.Request;
import org.opends.sdk.Response;
import org.opends.sdk.Result;
import org.opends.sdk.SearchRequest;
import org.opends.sdk.SearchResult;
import org.opends.sdk.SearchResultEntry;
import org.opends.sdk.SearchResultReference;
import org.opends.sdk.SimpleBindRequest;
import org.opends.sdk.UnbindRequest;
import org.opends.sdk.asn1.ASN1Writer;
import org.opends.sdk.controls.Control;
import org.opends.sdk.sasl.SASLBindRequest;
import org.opends.server.types.ByteString;



public class LDAPEncoder
{
  public static void encodeAttribute(ASN1Writer writer,
      AttributeValueSequence attribute) throws IOException
  {
    writer.writeStartSequence();
    writer
        .writeOctetString(attribute.getAttributeDescriptionAsString());

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
    writer.writeOctetString(searchResultEntry.getName());

    writer.writeStartSequence();
    for (AttributeValueSequence attr : searchResultEntry
        .getAttributes())
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
    writer.writeOctetString(request.getName());

    // Write the attributes
    writer.writeStartSequence();
    for (AttributeValueSequence attr : request.getAttributes())
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
    writer.writeOctetString(request.getName());

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
    writer.writeOctetString(OP_TYPE_DELETE_REQUEST, request.getName());
    encodeMessageFooter(writer, request);
  }



  public static void encodeExtendedRequest(ASN1Writer writer,
      int messageID, ExtendedRequest<?> request) throws IOException
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
    writer.writeOctetString(request.getName());

    writer.writeOctetString(request.getAuthenticationType(), request
        .getAuthenticationValue());

    writer.writeEndSequence();
    encodeMessageFooter(writer, request);
  }



  public static void encodeBindRequest(ASN1Writer writer,
      int messageID, int version, SASLBindRequest<?> request)
      throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_BIND_REQUEST);

    writer.writeInteger(version);
    writer.writeOctetString(request.getName());

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
    writer.writeOctetString(request.getName());
    writer.writeOctetString(TYPE_AUTHENTICATION_SIMPLE, request
        .getPassword());

    writer.writeEndSequence();
    encodeMessageFooter(writer, request);
  }



  public static void encodeModifyDNRequest(ASN1Writer writer,
      int messageID, ModifyDNRequest request) throws IOException
  {
    encodeMessageHeader(writer, messageID);
    writer.writeStartSequence(OP_TYPE_MODIFY_DN_REQUEST);
    writer.writeOctetString(request.getName());
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
    writer.writeOctetString(request.getName());

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
    writer.writeOctetString(request.getName());
    writer.writeEnumerated(request.getScope().intValue());
    writer.writeEnumerated(request.getDereferenceAliasesPolicy()
        .intValue());
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
      int messageID, ExtendedResult result) throws IOException
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
    encodeAttribute(writer, change);
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
