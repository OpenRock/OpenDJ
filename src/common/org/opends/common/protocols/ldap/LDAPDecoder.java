package org.opends.common.protocols.ldap;


import org.opends.common.api.raw.RawAttribute;
import org.opends.common.api.raw.RawControl;
import org.opends.common.api.raw.RawMessage;
import org.opends.common.api.raw.RawPartialAttribute;
import org.opends.common.api.raw.request.*;
import org.opends.common.api.raw.response.*;
import org.opends.common.protocols.ldap.asn1.ASN1StreamReader;
import org.opends.messages.Message;
import static org.opends.messages.ProtocolMessages.*;
import static org.opends.server.loggers.debug.DebugLogger.debugEnabled;
import static org.opends.server.loggers.debug.DebugLogger.getTracer;
import org.opends.server.loggers.debug.DebugTracer;
import static org.opends.server.protocols.asn1.ASN1Constants.*;
import org.opends.server.protocols.asn1.ASN1Exception;
import static org.opends.server.protocols.ldap.LDAPConstants.*;
import org.opends.server.types.*;

import java.io.IOException;


public class LDAPDecoder
{
  /**
   * The tracer object for the debug logger.
   */
  private static final DebugTracer TRACER = getTracer();



  /**
   * Decodes the elements from the provided ASN.1 reader as an LDAP
   * message.
   *
   * @param reader
   *          The ASN.1 reader.
   * @param handler
   *          The <code>LDAPMessageHandler</code> that will handle a
   *          decoded message.
   * @throws LDAPException
   *           If a problem occurs while attempting to decode the LDAP
   *           message.
   */
  public static void decode(ASN1StreamReader reader, LDAPMessageHandler handler)
      throws LDAPProtocolException, ASN1Exception, IOException
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
   * @throws LDAPException
   *           If the provided ASN.1 element cannot be decoded as an
   *           abandon request protocol op.
   */
  private static void decodeAbandonRequest(ASN1StreamReader reader, int messageID,
                                           LDAPMessageHandler handler)
      throws LDAPProtocolException, ASN1Exception, IOException
  {
    int msgToAbandon = (int) reader.readInteger(OP_TYPE_ABANDON_REQUEST);
    RawAbandonRequest rawMessage = new RawAbandonRequest(msgToAbandon);

    decodeControls(reader, rawMessage);
    handler.handleRequest(messageID, rawMessage);
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
   * @throws LDAPException
   *           If a problem occurs while decoding the provided ASN.1
   *           element as an LDAP add request protocol op.
   */
  private static void decodeAddRequest(ASN1StreamReader reader, int messageID,
                                       LDAPMessageHandler handler)
      throws LDAPProtocolException, ASN1Exception, IOException
  {
    reader.readStartSequence(OP_TYPE_ADD_REQUEST);
    String dn = reader.readOctetStringAsString();
    RawAddRequest rawMessage = new RawAddRequest(dn);
    reader.readStartSequence();
    while (reader.hasNextElement())
    {
      rawMessage.addAttribute(decodeAttribute(reader));
    }
    reader.readEndSequence();
    reader.readEndSequence();

    decodeControls(reader, rawMessage);
    handler.handleRequest(messageID, rawMessage);
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
   * @throws LDAPException
   *           If a problem occurs while attempting to decode the ASN.1
   *           element to a protocol op.
   */
  private static void decodeAddResponse(ASN1StreamReader reader, int messageID,
                                        LDAPMessageHandler handler)
      throws LDAPProtocolException, ASN1Exception, IOException
  {
    reader.readStartSequence(OP_TYPE_ADD_RESPONSE);
    ResultCode resultCode = ResultCode.valueOf(reader.readEnumerated());
    String matchedDN = reader.readOctetStringAsString();
    String diagnosticMessage = reader.readOctetStringAsString();
    RawAddResponse rawMessage =
        new RawAddResponse(resultCode, matchedDN, diagnosticMessage);
    decodeResponseReferrals(reader, rawMessage);
    reader.readEndSequence();

    decodeControls(reader, rawMessage);
    handler.handleResponse(messageID, rawMessage);
  }



  private static RawAttribute decodeAttribute(ASN1StreamReader reader)
      throws LDAPProtocolException, ASN1Exception, IOException
  {
    reader.readStartSequence();
    String attributeType = reader.readOctetStringAsString();
    RawAttribute rawAttribute;
    reader.readStartSet();
    // Should contain at least one value.
    rawAttribute = new RawAttribute(attributeType, reader.readOctetString());
    while(reader.hasNextElement())
    {
      rawAttribute.addAttributeValue(reader.readOctetString());
    }
    reader.readEndSequence();
    reader.readEndSequence();

    return rawAttribute;
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
   * @throws LDAPException
   *           If a problem occurs while trying to decode the provided
   *           ASN.1 element as an LDAP bind request.
   */
  private static void decodeBindRequest(ASN1StreamReader reader, int messageID,
                                        LDAPMessageHandler handler)
      throws LDAPProtocolException, ASN1Exception, IOException
  {
    reader.readStartSequence(OP_TYPE_BIND_REQUEST);
    int protocolVersion;
    String dn;
    byte type;
    ByteString simplePassword = null;
    String saslMechanism = null;
    ByteString saslCredentials = null;
    try
    {
      protocolVersion = (int) reader.readInteger();
      dn = reader.readOctetStringAsString();
      type = reader.peekType();
      switch (type)
      {
        case TYPE_AUTHENTICATION_SIMPLE:
          simplePassword = reader.readOctetString(TYPE_AUTHENTICATION_SIMPLE);
          break;
        case TYPE_AUTHENTICATION_SASL:
          reader.readStartSequence(TYPE_AUTHENTICATION_SASL);
          saslMechanism = reader.readOctetStringAsString();
          if (reader.hasNextElement() &&
              reader.peekType() == UNIVERSAL_OCTET_STRING_TYPE)
          {
            saslCredentials = reader.readOctetString();
          }
          reader.readEndSequence();
          break;
        default:
          // We don't support this authentication type. We MUST leave the
          // ASN.1 reader in a state so its ready to read more messages before
          // throwing the exception!
          Message message =
              ERR_LDAP_BIND_REQUEST_DECODE_INVALID_CRED_TYPE.get(type);
          throw new LDAPProtocolException(message,
                                          ResultCode.AUTH_METHOD_NOT_SUPPORTED,
                                          false);
      }
    }
    finally
    {
      reader.readEndSequence();
    }

    if (type == TYPE_AUTHENTICATION_SIMPLE)
    {
      RawSimpleBindRequest rawMessage =
          new RawSimpleBindRequest(dn, simplePassword);
      decodeControls(reader, rawMessage);
      handler.handleRequest(messageID, protocolVersion, rawMessage);
    }
    else
    {
      RawSASLBindRequest rawMessage =
          new RawSASLBindRequest(saslMechanism, saslCredentials);
      rawMessage.setBindDN(dn);
      decodeControls(reader, rawMessage);
      handler.handleRequest(messageID, protocolVersion, rawMessage);
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
   * @throws LDAPException
   *           If a problem occurs while attempting to decode the ASN.1
   *           element to a protocol op.
   */
  private static void decodeBindResponse(ASN1StreamReader reader, int messageID,
                                         LDAPMessageHandler handler)
      throws LDAPProtocolException, ASN1Exception, IOException
  {
    reader.readStartSequence(OP_TYPE_BIND_RESPONSE);
    ResultCode resultCode = ResultCode.valueOf(reader.readEnumerated());
    String matchedDN = reader.readOctetStringAsString();
    String diagnosticMessage = reader.readOctetStringAsString();
    RawBindResponse rawMessage =
        new RawBindResponse(resultCode, matchedDN, diagnosticMessage);
    decodeResponseReferrals(reader, rawMessage);
    if (reader.hasNextElement()
        && reader.peekType() == TYPE_SERVER_SASL_CREDENTIALS)
    {
      rawMessage.setServerSASLCreds(
          reader.readOctetString(TYPE_SERVER_SASL_CREDENTIALS));
    }
    reader.readEndSequence();

    decodeControls(reader, rawMessage);
    handler.handleResponse(messageID, rawMessage);
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
   * @throws LDAPException
   *           If a problem occurs while attempting to decode the ASN.1
   *           element as a compare request protocol op.
   */
  private static void decodeCompareRequest(ASN1StreamReader reader, int messageID,
                                           LDAPMessageHandler handler)
      throws LDAPProtocolException, ASN1Exception, IOException
  {
    reader.readStartSequence(OP_TYPE_COMPARE_REQUEST);
    String dn = reader.readOctetStringAsString();
    reader.readStartSequence();
    String attributeType = reader.readOctetStringAsString();
    ByteString assertionValue = reader.readOctetString();
    reader.readEndSequence();
    reader.readEndSequence();
    RawCompareRequest rawMessage =
        new RawCompareRequest(dn, attributeType, assertionValue);
    decodeControls(reader, rawMessage);
    handler.handleRequest(messageID, rawMessage);
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
   * @throws LDAPException
   *           If a problem occurs while attempting to decode the ASN.1
   *           element to a protocol op.
   */
  private static void decodeCompareResponse(ASN1StreamReader reader, int messageID,
                                            LDAPMessageHandler handler)
      throws LDAPProtocolException, ASN1Exception, IOException
  {
    reader.readStartSequence(OP_TYPE_COMPARE_RESPONSE);
    ResultCode resultCode = ResultCode.valueOf(reader.readEnumerated());
    String matchedDN = reader.readOctetStringAsString();
    String diagnosticMessage = reader.readOctetStringAsString();
    RawCompareResponse rawMessage =
        new RawCompareResponse(resultCode, matchedDN, diagnosticMessage);
    decodeResponseReferrals(reader, rawMessage);
    reader.readEndSequence();

    decodeControls(reader, rawMessage);
    handler.handleResponse(messageID, rawMessage);
  }



  /**
   * Decodes the elements from the provided ASN.1 reader as an LDAP
   * control.
   *
   * @param reader
   *          The ASN.1 reader.
   * @param rawMessage
   *          The decoded message to decode controls for.
   * @throws LDAPException
   *           If a problem occurs while attempting to decode the
   *           provided ASN.1 element as an LDAP control.
   */
  private static void decodeControl(ASN1StreamReader reader, RawMessage rawMessage)
      throws LDAPProtocolException, ASN1Exception, IOException
  {
    reader.readStartSequence();
    String oid = reader.readOctetStringAsString();
    boolean isCritical = false;
    ByteString value = null;
    if(reader.hasNextElement() &&
       reader.peekType() == UNIVERSAL_BOOLEAN_TYPE)
    {
      isCritical = reader.readBoolean();
    }
    if(reader.hasNextElement() &&
       reader.peekType() == UNIVERSAL_OCTET_STRING_TYPE)
    {
      value = reader.readOctetString();
    }
    reader.readEndSequence();

    rawMessage.addControl(new RawControl(oid, isCritical, value));
  }



  /**
   * Decodes the elements from the provided ASN.1 reader as a set of
   * controls.
   *
   * @param reader
   *          The ASN.1 reader.
   * @param rawMessage
   *          The decoded message to decode controls for.
   * @throws LDAPException
   *           If a problem occurs while attempting to decode the
   *           controls.
   */
  private static void decodeControls(ASN1StreamReader reader, RawMessage rawMessage)
      throws LDAPProtocolException, ASN1Exception, IOException
  {
    if (reader.hasNextElement()
        && reader.peekType() == TYPE_CONTROL_SEQUENCE)
    {
      reader.readStartSequence(TYPE_CONTROL_SEQUENCE);
      while(reader.hasNextElement())
      {
        decodeControl(reader, rawMessage);
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
   * @throws LDAPException
   *           If the provided ASN.1 element cannot be decoded as an
   *           unbind request protocol op.
   */
  private static void decodeDeleteRequest(ASN1StreamReader reader, int messageID,
                                          LDAPMessageHandler handler)
      throws LDAPProtocolException, ASN1Exception, IOException
  {
    String dn = reader.readOctetStringAsString(OP_TYPE_DELETE_REQUEST);
    RawDeleteRequest rawMessage = new RawDeleteRequest(dn);

    decodeControls(reader, rawMessage);
    handler.handleRequest(messageID, rawMessage);
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
   * @throws LDAPException
   *           If a problem occurs while attempting to decode the ASN.1
   *           element to a protocol op.
   */
  private static void decodeDeleteResponse(ASN1StreamReader reader, int messageID,
                                           LDAPMessageHandler handler)
      throws LDAPProtocolException, ASN1Exception, IOException
  {
    reader.readStartSequence(OP_TYPE_DELETE_RESPONSE);
    ResultCode resultCode = ResultCode.valueOf(reader.readEnumerated());
    String matchedDN = reader.readOctetStringAsString();
    String diagnosticMessage = reader.readOctetStringAsString();
    RawDeleteResponse rawMessage =
        new RawDeleteResponse(resultCode, matchedDN, diagnosticMessage);
    decodeResponseReferrals(reader, rawMessage);
    reader.readEndSequence();

    decodeControls(reader, rawMessage);
    handler.handleResponse(messageID, rawMessage);
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
   * @throws LDAPException
   *           If a problem occurs while attempting to decode the
   *           provided ASN.1 element as an LDAP extended request
   *           protocol op.
   */
  private static void decodeExtendedRequest(ASN1StreamReader reader, int messageID,
                                            LDAPMessageHandler handler)
      throws LDAPProtocolException, ASN1Exception, IOException
  {
    reader.readStartSequence(OP_TYPE_EXTENDED_REQUEST);
    String oid = reader.readOctetStringAsString(TYPE_EXTENDED_REQUEST_OID);
    RawExtendedRequest rawMessage = new RawExtendedRequest(oid);
    if (reader.hasNextElement()
        && reader.peekType() == TYPE_EXTENDED_REQUEST_VALUE)
    {
      rawMessage.setRequestValue(
          reader.readOctetString(TYPE_EXTENDED_REQUEST_VALUE));
    }
    reader.readEndSequence();

    decodeControls(reader, rawMessage);
    handler.handleRequest(messageID, rawMessage);
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
   * @throws LDAPException
   *           If a problem occurs while attempting to decode the ASN.1
   *           element to a protocol op.
   */
  private static void decodeExtendedResponse(ASN1StreamReader reader, int messageID,
                                             LDAPMessageHandler handler)
      throws LDAPProtocolException, ASN1Exception, IOException
  {
    reader.readStartSequence(OP_TYPE_EXTENDED_RESPONSE);

    ResultCode resultCode = ResultCode.valueOf(reader.readEnumerated());
    String matchedDN = reader.readOctetStringAsString();
    String diagnosticMessage = reader.readOctetStringAsString();
    RawExtendedResponse rawMessage =
        new RawExtendedResponse(resultCode, matchedDN,
                                diagnosticMessage);
    decodeResponseReferrals(reader, rawMessage);
    if (reader.hasNextElement()
        && reader.peekType() == TYPE_EXTENDED_RESPONSE_OID)
    {
      rawMessage.setResponseName(reader.readOctetStringAsString());
    }
    if (reader.hasNextElement()
        && reader.peekType() == TYPE_EXTENDED_RESPONSE_VALUE)
    {
      rawMessage.setResponseValue(reader.readOctetString());
    }
    reader.readEndSequence();

    decodeControls(reader, rawMessage);
    handler.handleResponse(messageID, rawMessage);
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
   * @throws LDAPException
   *           If a problem occurs while attempting to decode the
   *           provided ASN.1 element as an LDAP intermediate response
   *           protocol op.
   */
  private static void decodeIntermediateResponse(ASN1StreamReader reader,
                                                 int messageID,
                                                 LDAPMessageHandler handler)
      throws LDAPProtocolException, ASN1Exception, IOException
  {
    reader.readStartSequence(OP_TYPE_INTERMEDIATE_RESPONSE);
    RawIntermediateResponse rawMessage = new RawIntermediateResponse();
    if (reader.hasNextElement()
        && reader.peekType() == TYPE_INTERMEDIATE_RESPONSE_OID)
    {
      rawMessage.setResponseName(reader.readOctetStringAsString());
    }
    if (reader.hasNextElement()
        && reader.peekType() == TYPE_INTERMEDIATE_RESPONSE_VALUE)
    {
      rawMessage.setResponseValue(reader.readOctetString());
    }
    reader.readEndSequence();

    decodeControls(reader, rawMessage);
    handler.handleResponse(messageID, rawMessage);
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
   * @throws LDAPException
   *           If a problem occurs while trying to decode the provided
   *           ASN.1 element as an LDAP modify DN request protocol op.
   */
  private static void decodeModifyDNRequest(ASN1StreamReader reader, int messageID,
                                            LDAPMessageHandler handler)
      throws LDAPProtocolException, ASN1Exception, IOException
  {
    reader.readStartSequence(OP_TYPE_MODIFY_DN_REQUEST);
    String entryDN = reader.readOctetStringAsString();
    String newRDN = reader.readOctetStringAsString();
    RawModifyDNRequest rawMessage =
        new RawModifyDNRequest(entryDN, newRDN);
    rawMessage.setDeleteOldRDN(reader.readBoolean());
    if (reader.hasNextElement()
        && reader.peekType() == TYPE_MODIFY_DN_NEW_SUPERIOR)
    {
      rawMessage.setNewSuperior(
          reader.readOctetStringAsString(TYPE_MODIFY_DN_NEW_SUPERIOR));
    }
    reader.readEndSequence();

    decodeControls(reader, rawMessage);
    handler.handleRequest(messageID, rawMessage);
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
   * @throws LDAPException
   *           If a problem occurs while attempting to decode the ASN.1
   *           element to a protocol op.
   */
  private static void decodeModifyDNResponse(ASN1StreamReader reader, int messageID,
                                             LDAPMessageHandler handler)
      throws LDAPProtocolException, ASN1Exception, IOException
  {
    reader.readStartSequence(OP_TYPE_MODIFY_DN_RESPONSE);
    ResultCode resultCode = ResultCode.valueOf(reader.readEnumerated());
    String matchedDN = reader.readOctetStringAsString();
    String diagnosticMessage = reader.readOctetStringAsString();
    RawModifyDNResponse rawMessage =
        new RawModifyDNResponse(resultCode, matchedDN,
                                diagnosticMessage);
    decodeResponseReferrals(reader, rawMessage);
    reader.readEndSequence();

    decodeControls(reader, rawMessage);
    handler.handleResponse(messageID, rawMessage);
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
   * @throws LDAPException
   *           If a problem occurs while decoding the provided ASN.1
   *           element as an LDAP modify request protocol op.
   */
  private static void decodeModifyRequest(ASN1StreamReader reader, int messageID,
                                          LDAPMessageHandler handler)
      throws LDAPProtocolException, ASN1Exception, IOException
  {
    reader.readStartSequence(OP_TYPE_MODIFY_REQUEST);
    String dn = reader.readOctetStringAsString();
    RawModifyRequest rawMessage = new RawModifyRequest(dn);
    reader.readStartSequence();
    try
    {
      while (reader.hasNextElement())
      {
        reader.readStartSequence();
        try
        {
          ModificationType modificationType;
          int type = (int)reader.readInteger();
          switch (type)
          {
            case MOD_TYPE_ADD:
              modificationType = ModificationType.ADD;
              break;
            case MOD_TYPE_DELETE:
              modificationType = ModificationType.DELETE;
              break;
            case MOD_TYPE_REPLACE:
              modificationType = ModificationType.REPLACE;
              break;
            case MOD_TYPE_INCREMENT:
              modificationType = ModificationType.INCREMENT;
              break;
            default:
              Message message =
                  ERR_LDAP_MODIFICATION_DECODE_INVALID_MOD_TYPE.
                      get(type);
              throw new LDAPProtocolException(message, false);
          }
          RawPartialAttribute attribute = decodePartialAttribute(reader);
          rawMessage.addChange(new RawChange(modificationType, attribute));
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
    handler.handleRequest(messageID, rawMessage);
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
   * @throws LDAPException
   *           If a problem occurs while attempting to decode the ASN.1
   *           element to a protocol op.
   */
  private static void decodeModifyResponse(ASN1StreamReader reader, int messageID,
                                           LDAPMessageHandler handler)
      throws LDAPProtocolException, ASN1Exception, IOException
  {
    reader.readStartSequence(OP_TYPE_MODIFY_RESPONSE);
    ResultCode resultCode = ResultCode.valueOf(reader.readEnumerated());
    String matchedDN = reader.readOctetStringAsString();
    String diagnosticMessage = reader.readOctetStringAsString();
    RawModifyResponse rawMessage =
        new RawModifyResponse(resultCode, matchedDN, diagnosticMessage);
    decodeResponseReferrals(reader, rawMessage);
    reader.readEndSequence();

    decodeControls(reader, rawMessage);
    handler.handleResponse(messageID, rawMessage);
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
   * @throws LDAPProtocolException
   *           If a problem occurs while trying to decode the provided
   *           ASN.1 elements as an LDAP protocol op.
   */
  private static void decodeProtocolOp(ASN1StreamReader reader, int messageID,
                                       LDAPMessageHandler handler)
      throws LDAPProtocolException, ASN1Exception, IOException
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
        Message message =
            ERR_LDAP_PROTOCOL_OP_DECODE_INVALID_TYPE.get(type);
        throw new LDAPProtocolException(message, true);
      case OP_TYPE_DELETE_REQUEST: // 0x4A
        decodeDeleteRequest(reader, messageID, handler);
        break;
      case 0x4B: // 0x4B
      case 0x4C: // 0x4C
      case 0x4D: // 0x4D
      case 0x4E: // 0x4E
      case 0x4F: // 0x4F
        message = ERR_LDAP_PROTOCOL_OP_DECODE_INVALID_TYPE.get(type);
        throw new LDAPProtocolException(message, true);
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
        message = ERR_LDAP_PROTOCOL_OP_DECODE_INVALID_TYPE.get(type);
        throw new LDAPProtocolException(message, true);
      case OP_TYPE_BIND_REQUEST: // 0x60
        decodeBindRequest(reader, messageID, handler);
        break;
      case OP_TYPE_BIND_RESPONSE: // 0x61
        decodeBindResponse(reader, messageID, handler);
        break;
      case 0x62: // 0x62
        message = ERR_LDAP_PROTOCOL_OP_DECODE_INVALID_TYPE.get(type);
        throw new LDAPProtocolException(message, true);
      case OP_TYPE_SEARCH_REQUEST: // 0x63
        decodeSearchRequest(reader, messageID, handler);
        break;
      case OP_TYPE_SEARCH_RESULT_ENTRY: // 0x64
        decodeSearchEntry(reader, messageID, handler);
        break;
      case OP_TYPE_SEARCH_RESULT_DONE: // 0x65
        decodeSearchDone(reader, messageID, handler);
        break;
      case OP_TYPE_MODIFY_REQUEST: // 0x66
        decodeModifyRequest(reader, messageID, handler);
        break;
      case OP_TYPE_MODIFY_RESPONSE: // 0x67
        decodeModifyResponse(reader, messageID, handler);
        break;
      case OP_TYPE_ADD_REQUEST: // 0x68
        decodeAddRequest(reader, messageID, handler);
        break;
      case OP_TYPE_ADD_RESPONSE: // 0x69
        decodeAddResponse(reader, messageID, handler);
        break;
      case 0x6A: // 0x6A
        message = ERR_LDAP_PROTOCOL_OP_DECODE_INVALID_TYPE.get(type);
        throw new LDAPProtocolException(message, true);
      case OP_TYPE_DELETE_RESPONSE: // 0x6B
        decodeDeleteResponse(reader, messageID, handler);
        break;
      case OP_TYPE_MODIFY_DN_REQUEST: // 0x6C
        decodeModifyDNRequest(reader, messageID, handler);
        break;
      case OP_TYPE_MODIFY_DN_RESPONSE: // 0x6D
        decodeModifyDNResponse(reader, messageID, handler);
        break;
      case OP_TYPE_COMPARE_REQUEST: // 0x6E
        decodeCompareRequest(reader, messageID, handler);
        break;
      case OP_TYPE_COMPARE_RESPONSE: // 0x6F
        decodeCompareResponse(reader, messageID, handler);
        break;
      case 0x70: // 0x70
      case 0x71: // 0x71
      case 0x72: // 0x72
        message = ERR_LDAP_PROTOCOL_OP_DECODE_INVALID_TYPE.get(type);
        throw new LDAPProtocolException(message, true);
      case OP_TYPE_SEARCH_RESULT_REFERENCE: // 0x73
        decodeSearchReference(reader, messageID, handler);
        break;
      case 0x74: // 0x74
      case 0x75: // 0x75
      case 0x76: // 0x76
        message = ERR_LDAP_PROTOCOL_OP_DECODE_INVALID_TYPE.get(type);
        throw new LDAPProtocolException(message, true);
      case OP_TYPE_EXTENDED_REQUEST: // 0x77
        decodeExtendedRequest(reader, messageID, handler);
        break;
      case OP_TYPE_EXTENDED_RESPONSE: // 0x78
        decodeExtendedResponse(reader, messageID, handler);
        break;
      case OP_TYPE_INTERMEDIATE_RESPONSE: // 0x79
        decodeIntermediateResponse(reader, messageID, handler);
        break;
      default:
        message = ERR_LDAP_PROTOCOL_OP_DECODE_INVALID_TYPE.get(type);
        throw new LDAPProtocolException(message, true);
    }
  }



  private static RawPartialAttribute decodePartialAttribute(ASN1StreamReader reader)
      throws LDAPProtocolException, ASN1Exception, IOException
  {
    reader.readStartSequence();
    String attributeType = reader.readOctetStringAsString();
    RawPartialAttribute rawAttribute = new RawPartialAttribute(attributeType);
    reader.readStartSet();
    while(reader.hasNextElement())
    {
      rawAttribute.addAttributeValue(reader.readOctetString());
    }
    reader.readEndSequence();
    reader.readEndSequence();
    return rawAttribute;
  }



  private static void decodeResponseReferrals(ASN1StreamReader reader,
                                              RawResultResponse rawMessage)
      throws LDAPProtocolException, ASN1Exception, IOException
  {
    if (reader.hasNextElement()
        && reader.peekType() == TYPE_REFERRAL_SEQUENCE)
    {
      reader.readStartSequence(TYPE_REFERRAL_SEQUENCE);
      // Should have at least 1.
      do
      {
        rawMessage.addReferral((reader.readOctetStringAsString()));
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
   * @throws LDAPException
   *           If a problem occurs while attempting to decode the ASN.1
   *           element to a protocol op.
   */
  private static void decodeSearchDone(ASN1StreamReader reader, int messageID,
                                       LDAPMessageHandler handler)
      throws LDAPProtocolException, ASN1Exception, IOException
  {
    reader.readStartSequence(OP_TYPE_SEARCH_RESULT_DONE);
    ResultCode resultCode = ResultCode.valueOf(reader.readEnumerated());
    String matchedDN = reader.readOctetStringAsString();
    String diagnosticMessage = reader.readOctetStringAsString();
    RawSearchResultDone rawMessage =
        new RawSearchResultDone(resultCode, matchedDN,
                                diagnosticMessage);
    decodeResponseReferrals(reader, rawMessage);
    reader.readEndSequence();

    decodeControls(reader, rawMessage);
    handler.handleResponse(messageID, rawMessage);
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
   * @throws LDAPException
   *           If a problem occurs while decoding the provided ASN.1
   *           element as an LDAP search result entry protocol op.
   */
  private static void decodeSearchEntry(ASN1StreamReader reader, int messageID,
                                        LDAPMessageHandler handler)
      throws LDAPProtocolException, ASN1Exception, IOException
  {
    reader.readStartSequence(OP_TYPE_SEARCH_RESULT_ENTRY);
    String dn = reader.readOctetStringAsString();
    RawSearchResultEntry rawMessage = new RawSearchResultEntry(dn);
    reader.readStartSequence();
    while (reader.hasNextElement())
    {
      rawMessage.addPartialAttribute(decodePartialAttribute(reader));
    }
    reader.readEndSequence();
    reader.readEndSequence();

    decodeControls(reader, rawMessage);
    handler.handleResponse(messageID, rawMessage);
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
   * @throws LDAPException
   *           If a problem occurs while decoding the provided ASN.1
   *           element as an LDAP search result reference protocol op.
   */
  private static void decodeSearchReference(ASN1StreamReader reader, int messageID,
                                            LDAPMessageHandler handler)
      throws LDAPProtocolException, ASN1Exception, IOException
  {
    reader.readStartSequence(OP_TYPE_SEARCH_RESULT_REFERENCE);
    RawSearchResultReference rawMessage =
        new RawSearchResultReference(reader.readOctetStringAsString());
    while (reader.hasNextElement())
    {
      rawMessage.addURI(reader.readOctetStringAsString());
    }
    reader.readEndSequence();

    decodeControls(reader, rawMessage);
    handler.handleResponse(messageID, rawMessage);
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
   * @throws LDAPException
   *           If a problem occurs while decoding the provided ASN.1
   *           element as an LDAP search request protocol op.
   */
  private static void decodeSearchRequest(ASN1StreamReader reader, int messageID,
                                          LDAPMessageHandler handler)
      throws LDAPProtocolException, ASN1Exception, IOException
  {
    reader.readStartSequence(OP_TYPE_SEARCH_REQUEST);
    String baseDN;
    SearchScope scope;
    DereferencePolicy dereferencePolicy;
    int sizeLimit;
    int timeLimit;
    boolean typesOnly;
    RawFilter filter;
    RawSearchRequest rawMessage;
    try
    {
      baseDN = reader.readOctetStringAsString();
      int scopeValue = (int) reader.readInteger();
      switch (scopeValue)
      {
        case SCOPE_BASE_OBJECT:
          scope = SearchScope.BASE_OBJECT;
          break;
        case SCOPE_SINGLE_LEVEL:
          scope = SearchScope.SINGLE_LEVEL;
          break;
        case SCOPE_WHOLE_SUBTREE:
          scope = SearchScope.WHOLE_SUBTREE;
          break;
        case SCOPE_SUBORDINATE_SUBTREE:
          scope = SearchScope.SUBORDINATE_SUBTREE;
          break;
        default:
          Message message =
              ERR_LDAP_SEARCH_REQUEST_DECODE_INVALID_SCOPE
                  .get(scopeValue);
          throw new LDAPProtocolException(message, false);
      }
      int derefValue = (int) reader.readInteger();
      switch (derefValue)
      {
        case DEREF_NEVER:
          dereferencePolicy = DereferencePolicy.NEVER_DEREF_ALIASES;
          break;
        case DEREF_IN_SEARCHING:
          dereferencePolicy = DereferencePolicy.DEREF_IN_SEARCHING;
          break;
        case DEREF_FINDING_BASE:
          dereferencePolicy = DereferencePolicy.DEREF_FINDING_BASE_OBJECT;
          break;
        case DEREF_ALWAYS:
          dereferencePolicy = DereferencePolicy.DEREF_ALWAYS;
          break;
        default:
          Message message =
              ERR_LDAP_SEARCH_REQUEST_DECODE_INVALID_DEREF
                  .get(derefValue);
          throw new LDAPProtocolException(message, false);
      }
      sizeLimit = (int) reader.readInteger();
      timeLimit = (int) reader.readInteger();
      typesOnly = reader.readBoolean();
      // TODO: Redo filter decodeing
      try
      {
        filter = RawFilter.decode(reader);
      }
      catch (Exception e)
      {
        if (debugEnabled())
        {
          TRACER.debugCaught(DebugLogLevel.ERROR, e);
        }

        Message message =
            ERR_LDAP_SEARCH_REQUEST_DECODE_FILTER.get(String.valueOf(e));
        throw new LDAPProtocolException(message, e, false);
      }
      rawMessage = new RawSearchRequest(baseDN, scope, filter);
      rawMessage.setDereferencePolicy(dereferencePolicy);
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
    handler.handleRequest(messageID, rawMessage);
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
   * @throws LDAPException
   *           If the provided ASN.1 element cannot be decoded as an
   *           unbind request protocol op.
   */
  private static void decodeUnbindRequest(ASN1StreamReader reader,
                                          int messageID, LDAPMessageHandler handler)
      throws LDAPProtocolException, ASN1Exception, IOException
  {
    RawUnbindRequest rawMessage;
    reader.readNull(OP_TYPE_UNBIND_REQUEST);
    rawMessage = new RawUnbindRequest();

    decodeControls(reader, rawMessage);
    handler.handleRequest(messageID, rawMessage);
  }
}
