package org.opends.ldap.impl;



import static org.opends.messages.ProtocolMessages.ERR_LDAP_SEARCH_REQUEST_DECODE_INVALID_DEREF;
import static org.opends.server.protocols.asn1.ASN1Constants.UNIVERSAL_BOOLEAN_TYPE;
import static org.opends.server.protocols.asn1.ASN1Constants.UNIVERSAL_OCTET_STRING_TYPE;
import static org.opends.server.protocols.ldap.LDAPConstants.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.opends.asn1.ASN1Reader;
import org.opends.ldap.ProtocolException;
import org.opends.ldap.controls.GenericControl;
import org.opends.ldap.requests.AbandonRequest;
import org.opends.ldap.requests.AddRequest;
import org.opends.ldap.requests.CompareRequest;
import org.opends.ldap.requests.DeleteRequest;
import org.opends.ldap.requests.GenericBindRequest;
import org.opends.ldap.requests.GenericExtendedRequest;
import org.opends.ldap.requests.ModifyDNRequest;
import org.opends.ldap.requests.ModifyRequest;
import org.opends.ldap.requests.Request;
import org.opends.ldap.requests.Requests;
import org.opends.ldap.requests.SearchRequest;
import org.opends.ldap.requests.SimpleBindRequest;
import org.opends.ldap.requests.UnbindRequest;
import org.opends.ldap.responses.BindResult;
import org.opends.ldap.responses.CompareResult;
import org.opends.ldap.responses.GenericExtendedResult;
import org.opends.ldap.responses.GenericIntermediateResponse;
import org.opends.ldap.responses.Response;
import org.opends.ldap.responses.Responses;
import org.opends.ldap.responses.Result;
import org.opends.ldap.responses.SearchResult;
import org.opends.ldap.responses.SearchResultEntry;
import org.opends.ldap.responses.SearchResultReference;
import org.opends.ldap.sasl.GenericSASLBindRequest;
import org.opends.server.types.ByteString;
import org.opends.types.DereferenceAliasesPolicy;
import org.opends.types.ModificationType;
import org.opends.types.ResultCode;
import org.opends.types.SearchScope;
import org.opends.types.filter.Filter;



public class LDAPDecoder
{

  /**
   * Decodes the elements from the provided ASN.1 reader as an LDAP
   * message.
   *
   * @param reader
   *          The ASN.1 reader.
   * @param handler
   *          The <code>LDAPMessageHandler</code> that will handle a
   *          decoded message.
   * @throws IOException
   *           If an error occurred while reading bytes to decode.
   */
  public static void decode(ASN1Reader reader,
      LDAPMessageHandler handler) throws IOException
  {
    reader.readStartSequence();
    try
    {
      int messageID = (int) reader.readInteger();
      decodeProtocolOp(reader, messageID, handler);
    }
    finally
    {
      reader.readEndSequence();
    }
  }



  public static SearchResultEntry decodeEntry(ASN1Reader reader)
      throws IOException
  {
    reader.readStartSequence(OP_TYPE_SEARCH_RESULT_ENTRY);
    String dn = reader.readOctetStringAsString();
    SearchResultEntry rawMessage = Responses.newSearchResultEntry(dn);
    reader.readStartSequence();
    while (reader.hasNextElement())
    {
      reader.readStartSequence();
      String attributeDescription = reader.readOctetStringAsString();
      reader.readStartSet();

      ByteString singleValue = null;
      if (reader.hasNextElement())
      {
        singleValue = reader.readOctetString();

        if (reader.hasNextElement())
        {
          List<ByteString> vlist = new LinkedList<ByteString>();
          vlist.add(singleValue);
          singleValue = null;
          do
          {
            vlist.add(reader.readOctetString());
          }
          while (reader.hasNextElement());
          rawMessage.addAttribute(attributeDescription, vlist);
        }
        else
        {
          rawMessage.addAttribute(attributeDescription, singleValue);
        }
      }
      else
      {
        rawMessage.addAttribute(attributeDescription);
      }

      reader.readEndSet();
      reader.readEndSequence();
    }
    reader.readEndSequence();
    reader.readEndSequence();

    return rawMessage;
  }



  /**
   * Decodes the elements from the provided ASN.1 read as an LDAP
   * abandon request protocol op.
   *
   * @param reader
   *          The ASN.1 reader.
   * @param messageID
   *          The decoded message ID for this message.
   * @param handler
   *          The <code>LDAPMessageHandler</code> that will handle this
   *          decoded message.
   * @throws IOException
   *           If an error occurred while reading bytes to decode.
   */
  private static void decodeAbandonRequest(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws IOException
  {
    int msgToAbandon =
        (int) reader.readInteger(OP_TYPE_ABANDON_REQUEST);
    AbandonRequest rawMessage =
        Requests.newAbandonRequest(msgToAbandon);

    decodeControls(reader, rawMessage);
    handler.handleAbandonRequest(messageID, rawMessage);
  }



  /**
   * Decodes the elements from the provided ASN.1 reader as an LDAP add
   * request protocol op.
   *
   * @param reader
   *          The ASN.1 reader.
   * @param messageID
   *          The decoded message ID for this message.
   * @param handler
   *          The <code>LDAPMessageHandler</code> that will handle this
   *          decoded message.
   * @throws IOException
   *           If an error occurred while reading bytes to decode.
   */
  private static void decodeAddRequest(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws IOException
  {
    reader.readStartSequence(OP_TYPE_ADD_REQUEST);
    String dn = reader.readOctetStringAsString();
    AddRequest rawMessage = Requests.newAddRequest(dn);
    reader.readStartSequence();
    while (reader.hasNextElement())
    {
      reader.readStartSequence();
      String attributeDescription = reader.readOctetStringAsString();
      reader.readStartSet();

      ByteString singleValue = reader.readOctetString();

      if (reader.hasNextElement())
      {
        List<ByteString> vlist = new LinkedList<ByteString>();
        vlist.add(singleValue);
        singleValue = null;
        do
        {
          vlist.add(reader.readOctetString());
        }
        while (reader.hasNextElement());

        rawMessage.addAttribute(attributeDescription, vlist);
      }
      else
      {
        rawMessage.addAttribute(attributeDescription, singleValue);
      }

      reader.readEndSet();
      reader.readEndSequence();
    }

    reader.readEndSequence();
    reader.readEndSequence();

    decodeControls(reader, rawMessage);
    handler.handleAddRequest(messageID, rawMessage);
  }



  /**
   * Decodes the elements from the provided ASN.1 reader as an add
   * response protocol op.
   *
   * @param reader
   *          The ASN.1 reader.
   * @param messageID
   *          The decoded message ID for this message.
   * @param handler
   *          The <code>LDAPMessageHandler</code> that will handle this
   *          decoded message.
   * @throws IOException
   *           If an error occurred while reading bytes to decode.
   */
  private static void decodeAddResult(ASN1Reader reader, int messageID,
      LDAPMessageHandler handler) throws IOException
  {
    reader.readStartSequence(OP_TYPE_ADD_RESPONSE);
    ResultCode resultCode = ResultCode.valueOf(reader.readEnumerated());
    String matchedDN = reader.readOctetStringAsString();
    String diagnosticMessage = reader.readOctetStringAsString();
    Result rawMessage =
        Responses.newResult(resultCode).setMatchedDN(matchedDN)
            .setDiagnosticMessage(diagnosticMessage);
    decodeResponseReferrals(reader, rawMessage);
    reader.readEndSequence();

    decodeControls(reader, rawMessage);
    handler.handleAddResult(messageID, rawMessage);
  }



  /**
   * Decodes the elements from the provided ASN.1 read as an LDAP bind
   * request protocol op.
   *
   * @param reader
   *          The ASN.1 reader.
   * @param messageID
   *          The decoded message ID for this message.
   * @param handler
   *          The <code>LDAPMessageHandler</code> that will handle this
   *          decoded message.
   * @throws IOException
   *           If an error occurred while reading bytes to decode.
   */
  private static void decodeBindRequest(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws IOException
  {
    reader.readStartSequence(OP_TYPE_BIND_REQUEST);
    int protocolVersion;
    String dn;
    byte type;
    try
    {
      protocolVersion = (int) reader.readInteger();
      dn = reader.readOctetStringAsString();
      type = reader.peekType();
      switch (type)
      {
      case TYPE_AUTHENTICATION_SIMPLE:
        ByteString simplePassword =
            reader.readOctetString(TYPE_AUTHENTICATION_SIMPLE);

        SimpleBindRequest simpleBindMessage =
            Requests.newSimpleBindRequest(dn, simplePassword);

        decodeControls(reader, simpleBindMessage);
        handler.handleBindRequest(messageID, protocolVersion,
            simpleBindMessage);
        break;
      case TYPE_AUTHENTICATION_SASL:
        reader.readStartSequence(TYPE_AUTHENTICATION_SASL);
        String saslMechanism = reader.readOctetStringAsString();
        ByteString saslCredentials;
        if (reader.hasNextElement()
            && (reader.peekType() == UNIVERSAL_OCTET_STRING_TYPE))
        {
          saslCredentials = reader.readOctetString();
        }
        else
        {
          saslCredentials = ByteString.empty();
        }
        reader.readEndSequence();

        GenericSASLBindRequest rawSASLBindMessage =
            new GenericSASLBindRequest(saslMechanism, saslCredentials);
        rawSASLBindMessage.setName(dn);

        decodeControls(reader, rawSASLBindMessage);
        handler.handleBindRequest(messageID, protocolVersion,
            rawSASLBindMessage);
        break;
      default:
        ByteString unknownAuthBytes = reader.readOctetString(type);

        GenericBindRequest rawUnknownBindMessage =
            Requests.newGenericBindRequest(dn, type, unknownAuthBytes);

        decodeControls(reader, rawUnknownBindMessage);
        handler.handleBindRequest(messageID, protocolVersion,
            rawUnknownBindMessage);
      }
    }
    finally
    {
      reader.readEndSequence();
    }
  }



  /**
   * Decodes the elements from the provided ASN.1 reader as a bind
   * response protocol op.
   *
   * @param reader
   *          The ASN.1 reader.
   * @param messageID
   *          The decoded message ID for this message.
   * @param handler
   *          The <code>LDAPMessageHandler</code> that will handle this
   *          decoded message.
   * @throws IOException
   *           If an error occurred while reading bytes to decode.
   */
  private static void decodeBindResult(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws IOException
  {
    reader.readStartSequence(OP_TYPE_BIND_RESPONSE);
    ResultCode resultCode = ResultCode.valueOf(reader.readEnumerated());
    String matchedDN = reader.readOctetStringAsString();
    String diagnosticMessage = reader.readOctetStringAsString();
    BindResult rawMessage =
        Responses.newBindResult(resultCode).setMatchedDN(matchedDN)
            .setDiagnosticMessage(diagnosticMessage);
    decodeResponseReferrals(reader, rawMessage);
    if (reader.hasNextElement()
        && (reader.peekType() == TYPE_SERVER_SASL_CREDENTIALS))
    {
      rawMessage.setServerSASLCredentials(reader
          .readOctetString(TYPE_SERVER_SASL_CREDENTIALS));
    }
    reader.readEndSequence();

    decodeControls(reader, rawMessage);
    handler.handleBindResult(messageID, rawMessage);
  }



  /**
   * Decodes the elements from the provided ASN.1 reader as an LDAP
   * compare request protocol op.
   *
   * @param reader
   *          The ASN.1 reader.
   * @param messageID
   *          The decoded message ID for this message.
   * @param handler
   *          The <code>LDAPMessageHandler</code> that will handle this
   *          decoded message.
   * @throws IOException
   *           If an error occurred while reading bytes to decode.
   */
  private static void decodeCompareRequest(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws IOException
  {
    reader.readStartSequence(OP_TYPE_COMPARE_REQUEST);
    String dn = reader.readOctetStringAsString();
    reader.readStartSequence();
    String attributeType = reader.readOctetStringAsString();
    ByteString assertionValue = reader.readOctetString();
    reader.readEndSequence();
    reader.readEndSequence();
    CompareRequest rawMessage =
        Requests.newCompareRequest(dn, attributeType, assertionValue);
    decodeControls(reader, rawMessage);
    handler.handleCompareRequest(messageID, rawMessage);
  }



  /**
   * Decodes the elements from the provided ASN.1 reader as a compare
   * response protocol op.
   *
   * @param reader
   *          The ASN.1 reader.
   * @param messageID
   *          The decoded message ID for this message.
   * @param handler
   *          The <code>LDAPMessageHandler</code> that will handle this
   *          decoded message.
   * @throws IOException
   *           If an error occurred while reading bytes to decode.
   */
  private static void decodeCompareResult(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws IOException
  {
    reader.readStartSequence(OP_TYPE_COMPARE_RESPONSE);
    ResultCode resultCode = ResultCode.valueOf(reader.readEnumerated());
    String matchedDN = reader.readOctetStringAsString();
    String diagnosticMessage = reader.readOctetStringAsString();
    CompareResult rawMessage =
        Responses.newCompareResult(resultCode).setMatchedDN(matchedDN)
            .setDiagnosticMessage(diagnosticMessage);
    decodeResponseReferrals(reader, rawMessage);
    reader.readEndSequence();

    decodeControls(reader, rawMessage);
    handler.handleCompareResult(messageID, rawMessage);
  }



  /**
   * Decodes the elements from the provided ASN.1 reader as an LDAP
   * control.
   *
   * @param reader
   *          The ASN.1 reader.
   * @param rawRequest
   *          The decoded request to decode controls for.
   * @throws IOException
   *           If an error occurred while reading bytes to decode.
   */
  private static void decodeControl(ASN1Reader reader,
      Request rawRequest) throws IOException
  {
    reader.readStartSequence();
    String oid = reader.readOctetStringAsString();
    boolean isCritical = false;
    ByteString value = null;
    if (reader.hasNextElement()
        && (reader.peekType() == UNIVERSAL_BOOLEAN_TYPE))
    {
      isCritical = reader.readBoolean();
    }
    if (reader.hasNextElement()
        && (reader.peekType() == UNIVERSAL_OCTET_STRING_TYPE))
    {
      value = reader.readOctetString();
    }
    reader.readEndSequence();

    rawRequest.addControl(new GenericControl(oid, isCritical, value));
  }



  /**
   * Decodes the elements from the provided ASN.1 reader as an LDAP
   * control.
   *
   * @param reader
   *          The ASN.1 reader.
   * @param response
   *          The decoded message to decode controls for.
   * @throws IOException
   *           If an error occurred while reading bytes to decode.
   */
  private static void decodeControl(ASN1Reader reader, Response response)
      throws IOException
  {
    reader.readStartSequence();
    String oid = reader.readOctetStringAsString();
    boolean isCritical = false;
    ByteString value = null;
    if (reader.hasNextElement()
        && (reader.peekType() == UNIVERSAL_BOOLEAN_TYPE))
    {
      isCritical = reader.readBoolean();
    }
    if (reader.hasNextElement()
        && (reader.peekType() == UNIVERSAL_OCTET_STRING_TYPE))
    {
      value = reader.readOctetString();
    }
    reader.readEndSequence();

    response.addControl(new GenericControl(oid, isCritical, value));
  }



  /**
   * Decodes the elements from the provided ASN.1 reader as a set of
   * controls.
   *
   * @param reader
   *          The ASN.1 reader.
   * @param rawMessage
   *          The decoded message to decode controls for.
   * @throws IOException
   *           If an error occurred while reading bytes to decode.
   */
  private static void decodeControls(ASN1Reader reader,
      Request rawRequest) throws IOException
  {
    if (reader.hasNextElement()
        && (reader.peekType() == TYPE_CONTROL_SEQUENCE))
    {
      reader.readStartSequence(TYPE_CONTROL_SEQUENCE);
      while (reader.hasNextElement())
      {
        decodeControl(reader, rawRequest);
      }
      reader.readEndSequence();
    }
  }



  /**
   * Decodes the elements from the provided ASN.1 reader as a set of
   * controls.
   *
   * @param reader
   *          The ASN.1 reader.
   * @param response
   *          The decoded message to decode controls for.
   * @throws IOException
   *           If an error occurred while reading bytes to decode.
   */
  private static void decodeControls(ASN1Reader reader,
      Response response) throws IOException
  {
    if (reader.hasNextElement()
        && (reader.peekType() == TYPE_CONTROL_SEQUENCE))
    {
      reader.readStartSequence(TYPE_CONTROL_SEQUENCE);
      while (reader.hasNextElement())
      {
        decodeControl(reader, response);
      }
      reader.readEndSequence();
    }
  }



  /**
   * Decodes the elements from the provided ASN.1 reader as an LDAP
   * delete request protocol op.
   *
   * @param reader
   *          The ASN.1 reader.
   * @param messageID
   *          The decoded message ID for this message.
   * @param handler
   *          The <code>LDAPMessageHandler</code> that will handle this
   *          decoded message.
   * @throws IOException
   *           If an error occurred while reading bytes to decode.
   */
  private static void decodeDeleteRequest(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws IOException
  {
    String dn = reader.readOctetStringAsString(OP_TYPE_DELETE_REQUEST);
    DeleteRequest rawMessage = Requests.newDeleteRequest(dn);

    decodeControls(reader, rawMessage);
    handler.handleDeleteRequest(messageID, rawMessage);
  }



  /**
   * Decodes the elements from the provided ASN.1 reader as a delete
   * response protocol op.
   *
   * @param reader
   *          The ASN.1 reader.
   * @param messageID
   *          The decoded message ID for this message.
   * @param handler
   *          The <code>LDAPMessageHandler</code> that will handle this
   *          decoded message.
   * @throws IOException
   *           If an error occurred while reading bytes to decode.
   */
  private static void decodeDeleteResult(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws IOException
  {
    reader.readStartSequence(OP_TYPE_DELETE_RESPONSE);
    ResultCode resultCode = ResultCode.valueOf(reader.readEnumerated());
    String matchedDN = reader.readOctetStringAsString();
    String diagnosticMessage = reader.readOctetStringAsString();
    Result rawMessage =
        Responses.newResult(resultCode).setMatchedDN(matchedDN)
            .setDiagnosticMessage(diagnosticMessage);
    decodeResponseReferrals(reader, rawMessage);
    reader.readEndSequence();

    decodeControls(reader, rawMessage);
    handler.handleDeleteResult(messageID, rawMessage);
  }



  /**
   * Decodes the elements from the provided ASN.1 reader as an LDAP
   * extended request protocol op.
   *
   * @param reader
   *          The ASN.1 reader.
   * @param messageID
   *          The decoded message ID for this message.
   * @param handler
   *          The <code>LDAPMessageHandler</code> that will handle this
   *          decoded message.
   * @throws IOException
   *           If an error occurred while reading bytes to decode.
   */
  private static void decodeExtendedRequest(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws IOException
  {
    reader.readStartSequence(OP_TYPE_EXTENDED_REQUEST);
    String oid =
        reader.readOctetStringAsString(TYPE_EXTENDED_REQUEST_OID);
    ByteString value = null;
    if (reader.hasNextElement()
        && (reader.peekType() == TYPE_EXTENDED_REQUEST_VALUE))
    {
      value = reader.readOctetString(TYPE_EXTENDED_REQUEST_VALUE);
    }
    reader.readEndSequence();

    GenericExtendedRequest rawMessage =
        Requests.newGenericExtendedRequest(oid, value);

    decodeControls(reader, rawMessage);
    handler.handleExtendedRequest(messageID, rawMessage);
  }



  /**
   * Decodes the elements from the provided ASN.1 reader as a extended
   * response protocol op.
   *
   * @param reader
   *          The ASN.1 reader.
   * @param messageID
   *          The decoded message ID for this message.
   * @param handler
   *          The <code>LDAPMessageHandler</code> that will handle this
   *          decoded message.
   * @throws IOException
   *           If an error occurred while reading bytes to decode.
   */
  private static void decodeExtendedResult(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws IOException
  {
    reader.readStartSequence(OP_TYPE_EXTENDED_RESPONSE);

    ResultCode resultCode = ResultCode.valueOf(reader.readEnumerated());
    String matchedDN = reader.readOctetStringAsString();
    String diagnosticMessage = reader.readOctetStringAsString();
    GenericExtendedResult rawMessage =
        Responses.newGenericExtendedResult(resultCode).setMatchedDN(
            matchedDN).setDiagnosticMessage(diagnosticMessage);
    decodeResponseReferrals(reader, rawMessage);
    if (reader.hasNextElement()
        && (reader.peekType() == TYPE_EXTENDED_RESPONSE_OID))
    {
      rawMessage.setResponseName(reader
          .readOctetStringAsString(TYPE_EXTENDED_RESPONSE_OID));
    }
    if (reader.hasNextElement()
        && (reader.peekType() == TYPE_EXTENDED_RESPONSE_VALUE))
    {
      rawMessage.setResponseValue(reader
          .readOctetString(TYPE_EXTENDED_RESPONSE_VALUE));
    }
    reader.readEndSequence();

    decodeControls(reader, rawMessage);
    handler.handleExtendedResult(messageID, rawMessage);
  }



  /**
   * Decodes the elements from the provided ASN.1 reader as an LDAP
   * intermediate response protocol op.
   *
   * @param reader
   *          The ASN.1 reader.
   * @param messageID
   *          The decoded message ID for this message.
   * @param handler
   *          The <code>LDAPMessageHandler</code> that will handle this
   *          decoded message.
   * @throws IOException
   *           If an error occurred while reading bytes to decode.
   */
  private static void decodeIntermediateResponse(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws IOException
  {
    reader.readStartSequence(OP_TYPE_INTERMEDIATE_RESPONSE);
    GenericIntermediateResponse rawMessage =
        Responses.newGenericIntermediateResponse();
    if (reader.hasNextElement()
        && (reader.peekType() == TYPE_INTERMEDIATE_RESPONSE_OID))
    {
      rawMessage.setResponseName(reader
          .readOctetStringAsString(TYPE_INTERMEDIATE_RESPONSE_OID));
    }
    if (reader.hasNextElement()
        && (reader.peekType() == TYPE_INTERMEDIATE_RESPONSE_VALUE))
    {
      rawMessage.setResponseValue(reader
          .readOctetString(TYPE_INTERMEDIATE_RESPONSE_VALUE));
    }
    reader.readEndSequence();

    decodeControls(reader, rawMessage);
    handler.handleIntermediateResponse(messageID, rawMessage);
  }



  /**
   * Decodes the elements from the provided ASN.1 reader as a modify DN
   * request protocol op.
   *
   * @param reader
   *          The ASN.1 reader.
   * @param messageID
   *          The decoded message ID for this message.
   * @param handler
   *          The <code>LDAPMessageHandler</code> that will handle this
   *          decoded message.
   * @throws IOException
   *           If an error occurred while reading bytes to decode.
   */
  private static void decodeModifyDNRequest(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws IOException
  {
    reader.readStartSequence(OP_TYPE_MODIFY_DN_REQUEST);
    String entryDN = reader.readOctetStringAsString();
    String newRDN = reader.readOctetStringAsString();
    ModifyDNRequest rawMessage =
        Requests.newModifyDNRequest(entryDN, newRDN);
    rawMessage.setDeleteOldRDN(reader.readBoolean());
    if (reader.hasNextElement()
        && (reader.peekType() == TYPE_MODIFY_DN_NEW_SUPERIOR))
    {
      rawMessage.setNewSuperior(reader
          .readOctetStringAsString(TYPE_MODIFY_DN_NEW_SUPERIOR));
    }
    reader.readEndSequence();

    decodeControls(reader, rawMessage);
    handler.handleModifyDNRequest(messageID, rawMessage);
  }



  /**
   * Decodes the elements from the provided ASN.1 reader as a modify DN
   * response protocol op.
   *
   * @param reader
   *          The ASN.1 reader.
   * @param messageID
   *          The decoded message ID for this message.
   * @param handler
   *          The <code>LDAPMessageHandler</code> that will handle this
   *          decoded message.
   * @throws IOException
   *           If an error occurred while reading bytes to decode.
   */
  private static void decodeModifyDNResult(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws IOException
  {
    reader.readStartSequence(OP_TYPE_MODIFY_DN_RESPONSE);

    ResultCode resultCode = ResultCode.valueOf(reader.readEnumerated());
    String matchedDN = reader.readOctetStringAsString();
    String diagnosticMessage = reader.readOctetStringAsString();
    Result rawMessage =
        Responses.newResult(resultCode).setMatchedDN(matchedDN)
            .setDiagnosticMessage(diagnosticMessage);
    decodeResponseReferrals(reader, rawMessage);
    reader.readEndSequence();

    decodeControls(reader, rawMessage);
    handler.handleModifyDNResult(messageID, rawMessage);
  }



  /**
   * Decodes the elements from the provided ASN.1 reader as an LDAP
   * modify request protocol op.
   *
   * @param reader
   *          The ASN.1 reader.
   * @param messageID
   *          The decoded message ID for this message.
   * @param handler
   *          The <code>LDAPMessageHandler</code> that will handle this
   *          decoded message.
   * @throws IOException
   *           If an error occurred while reading bytes to decode.
   */
  private static void decodeModifyRequest(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws IOException
  {
    reader.readStartSequence(OP_TYPE_MODIFY_REQUEST);
    String dn = reader.readOctetStringAsString();
    ModifyRequest rawMessage = Requests.newModifyRequest(dn);
    reader.readStartSequence();
    try
    {
      while (reader.hasNextElement())
      {
        reader.readStartSequence();
        try
        {
          ModificationType type =
              ModificationType.valueOf(reader.readEnumerated());

          reader.readStartSequence();
          String attributeDescription =
              reader.readOctetStringAsString();
          reader.readStartSet();

          ByteString singleValue = null;
          if (reader.hasNextElement())
          {
            singleValue = reader.readOctetString();

            if (reader.hasNextElement())
            {
              List<ByteString> vlist = new LinkedList<ByteString>();
              vlist.add(singleValue);
              singleValue = null;
              do
              {
                vlist.add(reader.readOctetString());
              }
              while (reader.hasNextElement());
              rawMessage.addChange(type, attributeDescription, vlist);
            }
            else
            {
              rawMessage.addChange(type, attributeDescription,
                  singleValue);
            }
          }
          else
          {
            rawMessage.addChange(type, attributeDescription);
          }

          reader.readEndSet();
          reader.readEndSequence();
        }
        finally
        {
          reader.readEndSequence();
        }
      }
    }
    finally
    {
      reader.readEndSequence();
      reader.readEndSequence();
    }

    decodeControls(reader, rawMessage);
    handler.handleModifyRequest(messageID, rawMessage);
  }



  /**
   * Decodes the elements from the provided ASN.1 reader as a modify
   * response protocol op.
   *
   * @param reader
   *          The ASN.1 reader.
   * @param messageID
   *          The decoded message ID for this message.
   * @param handler
   *          The <code>LDAPMessageHandler</code> that will handle this
   *          decoded message.
   * @throws IOException
   *           If an error occurred while reading bytes to decode.
   */
  private static void decodeModifyResult(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws IOException
  {
    reader.readStartSequence(OP_TYPE_MODIFY_RESPONSE);

    ResultCode resultCode = ResultCode.valueOf(reader.readEnumerated());
    String matchedDN = reader.readOctetStringAsString();
    String diagnosticMessage = reader.readOctetStringAsString();
    Result rawMessage =
        Responses.newResult(resultCode).setMatchedDN(matchedDN)
            .setDiagnosticMessage(diagnosticMessage);
    decodeResponseReferrals(reader, rawMessage);
    reader.readEndSequence();

    decodeControls(reader, rawMessage);
    handler.handleModifyResult(messageID, rawMessage);
  }



  /**
   * Decodes the elements from the provided ASN.1 reader as an LDAP
   * protocol op.
   *
   * @param reader
   *          The ASN.1 reader.
   * @param messageID
   *          The decoded message ID for this message.
   * @param handler
   *          The <code>LDAPMessageHandler</code> that will handle this
   *          decoded message.
   * @throws IOException
   *           If an error occurred while reading bytes to decode.
   */
  private static void decodeProtocolOp(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws IOException
  {
    byte type = reader.peekType();

    switch (type)
    {
    case OP_TYPE_UNBIND_REQUEST: // 0x42
      decodeUnbindRequest(reader, messageID, handler);
      break;
    case 0x43: // 0x43
    case 0x44: // 0x44
    case 0x45: // 0x45
    case 0x46: // 0x46
    case 0x47: // 0x47
    case 0x48: // 0x48
    case 0x49: // 0x49
      handler.handleUnrecognizedMessage(messageID, type, reader
          .readOctetString(type));
      break;
    case OP_TYPE_DELETE_REQUEST: // 0x4A
      decodeDeleteRequest(reader, messageID, handler);
      break;
    case 0x4B: // 0x4B
    case 0x4C: // 0x4C
    case 0x4D: // 0x4D
    case 0x4E: // 0x4E
    case 0x4F: // 0x4F
      handler.handleUnrecognizedMessage(messageID, type, reader
          .readOctetString(type));
      break;
    case OP_TYPE_ABANDON_REQUEST: // 0x50
      decodeAbandonRequest(reader, messageID, handler);
      break;
    case 0x51: // 0x51
    case 0x52: // 0x52
    case 0x53: // 0x53
    case 0x54: // 0x54
    case 0x55: // 0x55
    case 0x56: // 0x56
    case 0x57: // 0x57
    case 0x58: // 0x58
    case 0x59: // 0x59
    case 0x5A: // 0x5A
    case 0x5B: // 0x5B
    case 0x5C: // 0x5C
    case 0x5D: // 0x5D
    case 0x5E: // 0x5E
    case 0x5F: // 0x5F
      handler.handleUnrecognizedMessage(messageID, type, reader
          .readOctetString(type));
      break;
    case OP_TYPE_BIND_REQUEST: // 0x60
      decodeBindRequest(reader, messageID, handler);
      break;
    case OP_TYPE_BIND_RESPONSE: // 0x61
      decodeBindResult(reader, messageID, handler);
      break;
    case 0x62: // 0x62
      handler.handleUnrecognizedMessage(messageID, type, reader
          .readOctetString(type));
      break;
    case OP_TYPE_SEARCH_REQUEST: // 0x63
      decodeSearchRequest(reader, messageID, handler);
      break;
    case OP_TYPE_SEARCH_RESULT_ENTRY: // 0x64
      decodeSearchResultEntry(reader, messageID, handler);
      break;
    case OP_TYPE_SEARCH_RESULT_DONE: // 0x65
      decodeSearchResult(reader, messageID, handler);
      break;
    case OP_TYPE_MODIFY_REQUEST: // 0x66
      decodeModifyRequest(reader, messageID, handler);
      break;
    case OP_TYPE_MODIFY_RESPONSE: // 0x67
      decodeModifyResult(reader, messageID, handler);
      break;
    case OP_TYPE_ADD_REQUEST: // 0x68
      decodeAddRequest(reader, messageID, handler);
      break;
    case OP_TYPE_ADD_RESPONSE: // 0x69
      decodeAddResult(reader, messageID, handler);
      break;
    case 0x6A: // 0x6A
      handler.handleUnrecognizedMessage(messageID, type, reader
          .readOctetString(type));
      break;
    case OP_TYPE_DELETE_RESPONSE: // 0x6B
      decodeDeleteResult(reader, messageID, handler);
      break;
    case OP_TYPE_MODIFY_DN_REQUEST: // 0x6C
      decodeModifyDNRequest(reader, messageID, handler);
      break;
    case OP_TYPE_MODIFY_DN_RESPONSE: // 0x6D
      decodeModifyDNResult(reader, messageID, handler);
      break;
    case OP_TYPE_COMPARE_REQUEST: // 0x6E
      decodeCompareRequest(reader, messageID, handler);
      break;
    case OP_TYPE_COMPARE_RESPONSE: // 0x6F
      decodeCompareResult(reader, messageID, handler);
      break;
    case 0x70: // 0x70
    case 0x71: // 0x71
    case 0x72: // 0x72
      handler.handleUnrecognizedMessage(messageID, type, reader
          .readOctetString(type));
      break;
    case OP_TYPE_SEARCH_RESULT_REFERENCE: // 0x73
      decodeSearchResultReference(reader, messageID, handler);
      break;
    case 0x74: // 0x74
    case 0x75: // 0x75
    case 0x76: // 0x76
      handler.handleUnrecognizedMessage(messageID, type, reader
          .readOctetString(type));
      break;
    case OP_TYPE_EXTENDED_REQUEST: // 0x77
      decodeExtendedRequest(reader, messageID, handler);
      break;
    case OP_TYPE_EXTENDED_RESPONSE: // 0x78
      decodeExtendedResult(reader, messageID, handler);
      break;
    case OP_TYPE_INTERMEDIATE_RESPONSE: // 0x79
      decodeIntermediateResponse(reader, messageID, handler);
      break;
    default:
      handler.handleUnrecognizedMessage(messageID, type, reader
          .readOctetString(type));
      break;
    }
  }



  private static void decodeResponseReferrals(ASN1Reader reader,
      Result rawMessage) throws IOException
  {
    if (reader.hasNextElement()
        && (reader.peekType() == TYPE_REFERRAL_SEQUENCE))
    {
      reader.readStartSequence(TYPE_REFERRAL_SEQUENCE);
      // Should have at least 1.
      do
      {
        rawMessage.addReferralURI((reader.readOctetStringAsString()));
      }
      while (reader.hasNextElement());
      reader.readEndSequence();
    }
  }



  /**
   * Decodes the elements from the provided ASN.1 reader as a search
   * result done protocol op.
   *
   * @param reader
   *          The ASN.1 reader.
   * @param messageID
   *          The decoded message ID for this message.
   * @param handler
   *          The <code>LDAPMessageHandler</code> that will handle this
   *          decoded message.
   * @throws IOException
   *           If an error occurred while reading bytes to decode.
   */
  private static void decodeSearchResult(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws IOException
  {
    reader.readStartSequence(OP_TYPE_SEARCH_RESULT_DONE);

    ResultCode resultCode = ResultCode.valueOf(reader.readEnumerated());
    String matchedDN = reader.readOctetStringAsString();
    String diagnosticMessage = reader.readOctetStringAsString();
    SearchResult rawMessage =
        Responses.newSearchResult(resultCode).setMatchedDN(matchedDN)
            .setDiagnosticMessage(diagnosticMessage);
    decodeResponseReferrals(reader, rawMessage);
    reader.readEndSequence();

    decodeControls(reader, rawMessage);
    handler.handleSearchResult(messageID, rawMessage);
  }



  /**
   * Decodes the elements from the provided ASN.1 reader as an LDAP
   * search result entry protocol op.
   *
   * @param reader
   *          The ASN.1 reader.
   * @param messageID
   *          The decoded message ID for this message.
   * @param handler
   *          The <code>LDAPMessageHandler</code> that will handle this
   *          decoded message.
   * @throws IOException
   *           If an error occurred while reading bytes to decode.
   */
  private static void decodeSearchResultEntry(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws IOException
  {
    SearchResultEntry rawMessage = decodeEntry(reader);
    decodeControls(reader, rawMessage);
    handler.handleSearchResultEntry(messageID, rawMessage);
  }



  /**
   * Decodes the elements from the provided ASN.1 reader as a search
   * result reference protocol op.
   *
   * @param reader
   *          The ASN.1 reader.
   * @param messageID
   *          The decoded message ID for this message.
   * @param handler
   *          The <code>LDAPMessageHandler</code> that will handle this
   *          decoded message.
   * @throws IOException
   *           If an error occurred while reading bytes to decode.
   */
  private static void decodeSearchResultReference(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws IOException
  {
    reader.readStartSequence(OP_TYPE_SEARCH_RESULT_REFERENCE);
    SearchResultReference rawMessage =
        Responses.newSearchResultReference(reader
            .readOctetStringAsString());
    while (reader.hasNextElement())
    {
      rawMessage.addURI(reader.readOctetStringAsString());
    }
    reader.readEndSequence();

    decodeControls(reader, rawMessage);
    handler.handleSearchResultReference(messageID, rawMessage);
  }



  /**
   * Decodes the elements from the provided ASN.1 reader as an LDAP
   * search request protocol op.
   *
   * @param reader
   *          The ASN.1 reader.
   * @param messageID
   *          The decoded message ID for this message.
   * @param handler
   *          The <code>LDAPMessageHandler</code> that will handle this
   *          decoded message.
   * @throws IOException
   *           If an error occurred while reading bytes to decode.
   */
  private static void decodeSearchRequest(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws IOException
  {
    reader.readStartSequence(OP_TYPE_SEARCH_REQUEST);
    String baseDN;
    SearchScope scope;
    DereferenceAliasesPolicy dereferencePolicy;
    int sizeLimit;
    int timeLimit;
    boolean typesOnly;
    Filter filter;
    SearchRequest rawMessage;
    try
    {
      baseDN = reader.readOctetStringAsString();
      scope = SearchScope.valueOf(reader.readEnumerated());
      int dereferencePolicyIntValue = reader.readEnumerated();
      if (dereferencePolicyIntValue < 0
          || dereferencePolicyIntValue > 3)
      {
        throw new ProtocolException(
            ERR_LDAP_SEARCH_REQUEST_DECODE_INVALID_DEREF
                .get(dereferencePolicyIntValue));
      }
      dereferencePolicy =
          DereferenceAliasesPolicy.valueOf(dereferencePolicyIntValue);
      sizeLimit = (int) reader.readInteger();
      timeLimit = (int) reader.readInteger();
      typesOnly = reader.readBoolean();
      filter = Filter.decode(reader);
      rawMessage = Requests.newSearchRequest(baseDN, scope, filter);
      rawMessage.setDereferenceAliasesPolicy(dereferencePolicy);
      rawMessage.setTimeLimit(timeLimit);
      rawMessage.setSizeLimit(sizeLimit);
      rawMessage.setTypesOnly(typesOnly);
      reader.readStartSequence();
      while (reader.hasNextElement())
      {
        rawMessage.addAttribute(reader.readOctetStringAsString());
      }
      reader.readEndSequence();
    }
    finally
    {
      reader.readEndSequence();
    }

    decodeControls(reader, rawMessage);
    handler.handleSearchRequest(messageID, rawMessage);
  }



  /**
   * Decodes the elements from the provided ASN.1 read as an LDAP unbind
   * request protocol op.
   *
   * @param reader
   *          The ASN.1 reader.
   * @param messageID
   *          The decoded message ID for this message.
   * @param handler
   *          The <code>LDAPMessageHandler</code> that will handle this
   *          decoded message.
   * @throws IOException
   *           If an error occurred while reading bytes to decode.
   */
  private static void decodeUnbindRequest(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws IOException
  {
    UnbindRequest rawMessage;
    reader.readNull(OP_TYPE_UNBIND_REQUEST);
    rawMessage = Requests.newUnbindRequest();

    decodeControls(reader, rawMessage);
    handler.handleUnbindRequest(messageID, rawMessage);
  }
}
