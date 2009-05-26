package org.opends.common.protocols.ldap;



import org.opends.server.loggers.debug.DebugTracer;
import static org.opends.server.loggers.debug.DebugLogger.getTracer;
import static org.opends.server.loggers.debug.DebugLogger.debugEnabled;
import org.opends.server.protocols.ldap.*;
import static org.opends.server.protocols.ldap.LDAPConstants.*;
import static org.opends.server.protocols.ldap.LDAPResultCode.PROTOCOL_ERROR;
import static org.opends.server.protocols.ldap.LDAPResultCode.AUTH_METHOD_NOT_SUPPORTED;
import org.opends.server.protocols.asn1.ASN1Reader;
import org.opends.server.protocols.asn1.ASN1Exception;
import static org.opends.server.protocols.asn1.ASN1Constants.UNIVERSAL_BOOLEAN_TYPE;
import static org.opends.server.protocols.asn1.ASN1Constants.UNIVERSAL_OCTET_STRING_TYPE;
import org.opends.server.types.*;
import org.opends.messages.Message;
import static org.opends.messages.ProtocolMessages.*;
import org.opends.common.api.raw.*;
import org.opends.common.api.raw.response.*;
import org.opends.common.api.raw.request.*;



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
  public static void decode(ASN1Reader reader,
      LDAPMessageHandler handler) throws LDAPException
  {
    try
    {
      reader.readStartSequence();
    }
    catch (Exception e)
    {
      Message message = ERR_LDAP_MESSAGE_DECODE_NULL.get();
      throw new LDAPException(PROTOCOL_ERROR, message);
    }

    int messageID;
    try
    {
      messageID = (int) reader.readInteger();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_MESSAGE_DECODE_MESSAGE_ID.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    try
    {
      decodeProtocolOp(reader, messageID, handler);
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_MESSAGE_DECODE_PROTOCOL_OP.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
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
  private static void decodeAbandonRequest(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws LDAPException
  {
    RawAbandonRequest rawMessage;
    try
    {
      rawMessage = new RawAbandonRequest((int) reader.readInteger());
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_ABANDON_REQUEST_DECODE_ID.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

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
  private static void decodeAddRequest(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws LDAPException
  {
    try
    {
      reader.readStartSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_ADD_REQUEST_DECODE_SEQUENCE.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    String dn;
    try
    {
      dn = reader.readOctetStringAsString();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_ADD_REQUEST_DECODE_DN.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    RawAddRequest rawMessage = new RawAddRequest(dn);
    try
    {
      reader.readStartSequence();
      while (reader.hasNextElement())
      {
        rawMessage.addAttribute(LDAPAttribute.decode(reader));
      }
      reader.readEndSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_ADD_REQUEST_DECODE_ATTRS.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    try
    {
      reader.readEndSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_ADD_REQUEST_DECODE_SEQUENCE.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

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
  private static void decodeAddResponse(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws LDAPException
  {
    try
    {
      reader.readStartSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_RESULT_DECODE_SEQUENCE.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    ResultCode resultCode = decodeResponseResultCode(reader);
    String matchedDN = decodeResponseMatchedDN(reader);
    String diagnosticMessage = decodeResponseDiagMessage(reader);

    RawAddResponse rawMessage =
        new RawAddResponse(resultCode, matchedDN, diagnosticMessage);

    decodeResponseReferrals(reader, rawMessage);

    try
    {
      reader.readEndSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_RESULT_DECODE_SEQUENCE.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    decodeControls(reader, rawMessage);
    handler.handleResponse(messageID, rawMessage);
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
  private static void decodeBindRequest(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws LDAPException
  {
    try
    {
      reader.readStartSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_BIND_REQUEST_DECODE_SEQUENCE.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    int protocolVersion;
    try
    {
      protocolVersion = (int) reader.readInteger();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_BIND_REQUEST_DECODE_VERSION.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    String dn;
    try
    {
      dn = reader.readOctetStringAsString();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_BIND_REQUEST_DECODE_DN.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    byte type;
    try
    {
      type = reader.peekType();

    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_BIND_REQUEST_DECODE_CREDENTIALS.get(String
              .valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    ByteString simplePassword = null;
    String saslMechanism = null;
    ByteString saslCredentials = null;
    switch (type)
    {
    case TYPE_AUTHENTICATION_SIMPLE:
      try
      {
        simplePassword = reader.readOctetString();
      }
      catch (Exception e)
      {
        Message message =
            ERR_LDAP_BIND_REQUEST_DECODE_PASSWORD
                .get(String.valueOf(e));
        throw new LDAPException(PROTOCOL_ERROR, message, e);
      }
      break;
    case TYPE_AUTHENTICATION_SASL:
      try
      {
        reader.readStartSequence();
        saslMechanism = reader.readOctetStringAsString();
        if (reader.hasNextElement())
        {
          saslCredentials = reader.readOctetString();
        }
        reader.readEndSequence();
      }
      catch (Exception e)
      {
        Message message =
            ERR_LDAP_BIND_REQUEST_DECODE_SASL_INFO.get(String
                .valueOf(e));
        throw new LDAPException(PROTOCOL_ERROR, message, e);
      }
      break;
    default:
      Message message =
          ERR_LDAP_BIND_REQUEST_DECODE_INVALID_CRED_TYPE.get(type);
      throw new LDAPException(AUTH_METHOD_NOT_SUPPORTED, message);
    }

    try
    {
      reader.readEndSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_BIND_REQUEST_DECODE_SEQUENCE.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
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
      rawMessage.setName(dn);
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
  private static void decodeBindResponse(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws LDAPException
  {
    try
    {
      reader.readStartSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_RESULT_DECODE_SEQUENCE.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    ResultCode resultCode = decodeResponseResultCode(reader);
    String matchedDN = decodeResponseMatchedDN(reader);
    String diagnosticMessage = decodeResponseDiagMessage(reader);

    RawBindResponse rawMessage =
        new RawBindResponse(resultCode, matchedDN, diagnosticMessage);

    decodeResponseReferrals(reader, rawMessage);

    try
    {
      if (reader.hasNextElement()
          && reader.peekType() == TYPE_SERVER_SASL_CREDENTIALS)
      {
        rawMessage.setServerSASLCreds(reader.readOctetString());
      }
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_BIND_RESULT_DECODE_SERVER_SASL_CREDENTIALS
              .get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    try
    {
      reader.readEndSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_RESULT_DECODE_SEQUENCE.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

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
  private static void decodeCompareRequest(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws LDAPException
  {
    try
    {
      reader.readStartSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_COMPARE_REQUEST_DECODE_SEQUENCE.get(String
              .valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    String dn;
    try
    {
      dn = reader.readOctetStringAsString();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_COMPARE_REQUEST_DECODE_DN.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    try
    {
      reader.readStartSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_COMPARE_REQUEST_DECODE_AVA.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    String attributeType;
    try
    {
      attributeType = reader.readOctetStringAsString();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_COMPARE_REQUEST_DECODE_TYPE.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    ByteString assertionValue;
    try
    {
      assertionValue = reader.readOctetString();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_COMPARE_REQUEST_DECODE_VALUE.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    try
    {
      reader.readEndSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_COMPARE_REQUEST_DECODE_AVA.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    try
    {
      reader.readEndSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_COMPARE_REQUEST_DECODE_SEQUENCE.get(String
              .valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

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
  private static void decodeCompareResponse(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws LDAPException
  {
    try
    {
      reader.readStartSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_RESULT_DECODE_SEQUENCE.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    ResultCode resultCode = decodeResponseResultCode(reader);
    String matchedDN = decodeResponseMatchedDN(reader);
    String diagnosticMessage = decodeResponseDiagMessage(reader);

    RawCompareResponse rawMessage =
        new RawCompareResponse(resultCode, matchedDN, diagnosticMessage);

    decodeResponseReferrals(reader, rawMessage);

    try
    {
      reader.readEndSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_RESULT_DECODE_SEQUENCE.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

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
  private static void decodeControl(ASN1Reader reader,
      RawMessage rawMessage) throws LDAPException
  {
    try
    {
      reader.readStartSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_CONTROL_DECODE_SEQUENCE.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    String oid;
    try
    {
      oid = reader.readOctetStringAsString();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_CONTROL_DECODE_OID.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    boolean isCritical = false;
    ByteString value = null;
    try
    {
      while (reader.hasNextElement())
      {
        switch (reader.peekType())
        {
        case UNIVERSAL_BOOLEAN_TYPE:
          try
          {
            isCritical = reader.readBoolean();
          }
          catch (Exception e2)
          {
            if (debugEnabled())
            {
              TRACER.debugCaught(DebugLogLevel.ERROR, e2);
            }

            Message message =
                ERR_LDAP_CONTROL_DECODE_CRITICALITY.get(String
                    .valueOf(e2));
            throw new LDAPException(PROTOCOL_ERROR, message, e2);
          }
          break;
        case UNIVERSAL_OCTET_STRING_TYPE:
          try
          {
            value = reader.readOctetString();
          }
          catch (Exception e2)
          {
            if (debugEnabled())
            {
              TRACER.debugCaught(DebugLogLevel.ERROR, e2);
            }

            Message message =
                ERR_LDAP_CONTROL_DECODE_VALUE.get(String.valueOf(e2));
            throw new LDAPException(PROTOCOL_ERROR, message, e2);
          }
          break;
        default:
          Message message =
              ERR_LDAP_CONTROL_DECODE_INVALID_TYPE.get(reader
                  .peekType());
          throw new LDAPException(PROTOCOL_ERROR, message);
        }
      }
    }
    catch (ASN1Exception asn1e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, asn1e);
      }
    }

    try
    {
      reader.readEndSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_CONTROL_DECODE_SEQUENCE.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

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
  private static void decodeControls(ASN1Reader reader,
      RawMessage rawMessage) throws LDAPException
  {
    try
    {
      if (reader.hasNextElement()
          && reader.peekType() == TYPE_CONTROL_SEQUENCE)
      {
        decodeControl(reader, rawMessage);
      }
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_MESSAGE_DECODE_CONTROLS.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    try
    {
      reader.readEndSequence();
    }
    catch (Exception e)
    {
      Message message = ERR_LDAP_MESSAGE_DECODE_NULL.get();
      throw new LDAPException(PROTOCOL_ERROR, message);
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
  private static void decodeDeleteRequest(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws LDAPException
  {
    RawDeleteRequest rawMessage;
    try
    {
      rawMessage =
          new RawDeleteRequest(reader.readOctetStringAsString());
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_DELETE_REQUEST_DECODE_DN.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

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
  private static void decodeDeleteResponse(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws LDAPException
  {
    try
    {
      reader.readStartSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_RESULT_DECODE_SEQUENCE.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    ResultCode resultCode = decodeResponseResultCode(reader);
    String matchedDN = decodeResponseMatchedDN(reader);
    String diagnosticMessage = decodeResponseDiagMessage(reader);

    RawDeleteResponse rawMessage =
        new RawDeleteResponse(resultCode, matchedDN, diagnosticMessage);

    decodeResponseReferrals(reader, rawMessage);

    try
    {
      reader.readEndSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_RESULT_DECODE_SEQUENCE.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

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
  private static void decodeExtendedRequest(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws LDAPException
  {
    try
    {
      reader.readStartSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_EXTENDED_REQUEST_DECODE_SEQUENCE.get(String
              .valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    String oid;
    try
    {
      oid = reader.readOctetStringAsString();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_EXTENDED_REQUEST_DECODE_OID.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    RawExtendedRequest rawMessage = new RawExtendedRequest(oid);
    try
    {
      if (reader.hasNextElement()
          && reader.peekType() == TYPE_EXTENDED_REQUEST_VALUE)
      {
        rawMessage.setRequestValue(reader.readOctetString());
      }
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_EXTENDED_REQUEST_DECODE_VALUE.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    try
    {
      reader.readEndSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_EXTENDED_REQUEST_DECODE_SEQUENCE.get(String
              .valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

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
  private static void decodeExtendedResponse(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws LDAPException
  {
    try
    {
      reader.readStartSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_RESULT_DECODE_SEQUENCE.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    ResultCode resultCode = decodeResponseResultCode(reader);
    String matchedDN = decodeResponseMatchedDN(reader);
    String diagnosticMessage = decodeResponseDiagMessage(reader);

    RawExtendedResponse rawMessage =
        new RawExtendedResponse(resultCode, matchedDN,
            diagnosticMessage);

    decodeResponseReferrals(reader, rawMessage);

    try
    {
      if (reader.hasNextElement()
          && reader.peekType() == TYPE_EXTENDED_RESPONSE_OID)
      {
        rawMessage.setResponseName(reader.readOctetStringAsString());
      }
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_EXTENDED_RESULT_DECODE_OID.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    try
    {
      if (reader.hasNextElement()
          && reader.peekType() == TYPE_EXTENDED_RESPONSE_VALUE)
      {
        rawMessage.setResponseValue(reader.readOctetString());
      }
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_EXTENDED_RESULT_DECODE_VALUE.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    try
    {
      reader.readEndSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_RESULT_DECODE_SEQUENCE.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

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
  private static void decodeIntermediateResponse(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws LDAPException
  {
    try
    {
      reader.readStartSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_INTERMEDIATE_RESPONSE_DECODE_SEQUENCE.get(String
              .valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    RawIntermediateResponse rawMessage = new RawIntermediateResponse();
    try
    {
      if (reader.hasNextElement()
          && reader.peekType() == TYPE_INTERMEDIATE_RESPONSE_OID)
      {
        rawMessage.setResponseName(reader.readOctetStringAsString());
      }
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_INTERMEDIATE_RESPONSE_CANNOT_DECODE_OID.get(String
              .valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    try
    {
      if (reader.hasNextElement()
          && reader.peekType() == TYPE_INTERMEDIATE_RESPONSE_VALUE)
      {
        rawMessage.setResponseValue(reader.readOctetString());
      }
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_INTERMEDIATE_RESPONSE_CANNOT_DECODE_VALUE.get(String
              .valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    try
    {
      reader.readEndSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_INTERMEDIATE_RESPONSE_DECODE_SEQUENCE.get(String
              .valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

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
  private static void decodeModifyDNRequest(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws LDAPException
  {
    try
    {
      reader.readStartSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_MODIFY_DN_REQUEST_DECODE_SEQUENCE.get(String
              .valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    String entryDN;
    try
    {
      entryDN = reader.readOctetStringAsString();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_MODIFY_DN_REQUEST_DECODE_DN.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    String newRDN;
    try
    {
      newRDN = reader.readOctetStringAsString();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_MODIFY_DN_REQUEST_DECODE_NEW_RDN.get(String
              .valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    RawModifyDNRequest rawMessage =
        new RawModifyDNRequest(entryDN, newRDN);
    try
    {
      rawMessage.setDeleteOldRDN(reader.readBoolean());
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_MODIFY_DN_REQUEST_DECODE_DELETE_OLD_RDN.get(String
              .valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    try
    {
      if (reader.hasNextElement()
          && reader.peekType() == TYPE_MODIFY_DN_NEW_SUPERIOR)
      {
        rawMessage.setNewSuperior(reader.readOctetStringAsString());
      }
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_MODIFY_DN_REQUEST_DECODE_NEW_SUPERIOR.get(String
              .valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    try
    {
      reader.readEndSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_MODIFY_DN_REQUEST_DECODE_SEQUENCE.get(String
              .valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

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
  private static void decodeModifyDNResponse(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws LDAPException
  {
    try
    {
      reader.readStartSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_RESULT_DECODE_SEQUENCE.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    ResultCode resultCode = decodeResponseResultCode(reader);
    String matchedDN = decodeResponseMatchedDN(reader);
    String diagnosticMessage = decodeResponseDiagMessage(reader);

    RawModifyDNResponse rawMessage =
        new RawModifyDNResponse(resultCode, matchedDN,
            diagnosticMessage);

    decodeResponseReferrals(reader, rawMessage);

    try
    {
      reader.readEndSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_RESULT_DECODE_SEQUENCE.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

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
  private static void decodeModifyRequest(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws LDAPException
  {
    try
    {
      reader.readStartSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_MODIFY_REQUEST_DECODE_SEQUENCE
              .get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    String dn;
    try
    {
      dn = reader.readOctetStringAsString();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_MODIFY_REQUEST_DECODE_DN.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    RawModifyRequest rawMessage = new RawModifyRequest(dn);
    try
    {
      reader.readStartSequence();
      while (reader.hasNextElement())
      {
        rawMessage.addModification(LDAPModification.decode(reader));
      }
      reader.readEndSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_MODIFY_REQUEST_DECODE_MODS.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    try
    {
      reader.readEndSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_MODIFY_REQUEST_DECODE_SEQUENCE
              .get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
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
  private static void decodeModifyResponse(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws LDAPException
  {
    try
    {
      reader.readStartSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_RESULT_DECODE_SEQUENCE.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    ResultCode resultCode = decodeResponseResultCode(reader);
    String matchedDN = decodeResponseMatchedDN(reader);
    String diagnosticMessage = decodeResponseDiagMessage(reader);

    RawModifyResponse rawMessage =
        new RawModifyResponse(resultCode, matchedDN, diagnosticMessage);

    decodeResponseReferrals(reader, rawMessage);

    try
    {
      reader.readEndSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_RESULT_DECODE_SEQUENCE.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

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
   * @throws LDAPException
   *           If a problem occurs while trying to decode the provided
   *           ASN.1 elements as an LDAP protocol op.
   */
  private static void decodeProtocolOp(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws LDAPException
  {
    byte type;
    try
    {
      type = reader.peekType();
    }
    catch (Exception e)
    {
      Message message = ERR_LDAP_PROTOCOL_OP_DECODE_NULL.get();
      throw new LDAPException(PROTOCOL_ERROR, message);
    }

    switch (type)
    {
    case OP_TYPE_UNBIND_REQUEST: // 0x42
      decodeUnbindRequest(reader, messageID, handler);
    case 0x43: // 0x43
    case 0x44: // 0x44
    case 0x45: // 0x45
    case 0x46: // 0x46
    case 0x47: // 0x47
    case 0x48: // 0x48
    case 0x49: // 0x49
      Message message =
          ERR_LDAP_PROTOCOL_OP_DECODE_INVALID_TYPE.get(type);
      throw new LDAPException(PROTOCOL_ERROR, message);
    case OP_TYPE_DELETE_REQUEST: // 0x4A
      decodeDeleteRequest(reader, messageID, handler);
    case 0x4B: // 0x4B
    case 0x4C: // 0x4C
    case 0x4D: // 0x4D
    case 0x4E: // 0x4E
    case 0x4F: // 0x4F
      message = ERR_LDAP_PROTOCOL_OP_DECODE_INVALID_TYPE.get(type);
      throw new LDAPException(PROTOCOL_ERROR, message);
    case OP_TYPE_ABANDON_REQUEST: // 0x50
      decodeAbandonRequest(reader, messageID, handler);
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
      throw new LDAPException(PROTOCOL_ERROR, message);
    case OP_TYPE_BIND_REQUEST: // 0x60
      decodeBindRequest(reader, messageID, handler);
    case OP_TYPE_BIND_RESPONSE: // 0x61
      decodeBindResponse(reader, messageID, handler);
    case 0x62: // 0x62
      message = ERR_LDAP_PROTOCOL_OP_DECODE_INVALID_TYPE.get(type);
      throw new LDAPException(PROTOCOL_ERROR, message);
    case OP_TYPE_SEARCH_REQUEST: // 0x63
      decodeSearchRequest(reader, messageID, handler);
    case OP_TYPE_SEARCH_RESULT_ENTRY: // 0x64
      decodeSearchEntry(reader, messageID, handler);
    case OP_TYPE_SEARCH_RESULT_DONE: // 0x65
      decodeSearchDone(reader, messageID, handler);
    case OP_TYPE_MODIFY_REQUEST: // 0x66
      decodeModifyRequest(reader, messageID, handler);
    case OP_TYPE_MODIFY_RESPONSE: // 0x67
      decodeModifyResponse(reader, messageID, handler);
    case OP_TYPE_ADD_REQUEST: // 0x68
      decodeAddRequest(reader, messageID, handler);
    case OP_TYPE_ADD_RESPONSE: // 0x69
      decodeAddResponse(reader, messageID, handler);
    case 0x6A: // 0x6A
      message = ERR_LDAP_PROTOCOL_OP_DECODE_INVALID_TYPE.get(type);
      throw new LDAPException(PROTOCOL_ERROR, message);
    case OP_TYPE_DELETE_RESPONSE: // 0x6B
      decodeDeleteResponse(reader, messageID, handler);
    case OP_TYPE_MODIFY_DN_REQUEST: // 0x6C
      decodeModifyDNRequest(reader, messageID, handler);
    case OP_TYPE_MODIFY_DN_RESPONSE: // 0x6D
      decodeModifyDNResponse(reader, messageID, handler);
    case OP_TYPE_COMPARE_REQUEST: // 0x6E
      decodeCompareRequest(reader, messageID, handler);
    case OP_TYPE_COMPARE_RESPONSE: // 0x6F
      decodeCompareResponse(reader, messageID, handler);
    case 0x70: // 0x70
    case 0x71: // 0x71
    case 0x72: // 0x72
      message = ERR_LDAP_PROTOCOL_OP_DECODE_INVALID_TYPE.get(type);
      throw new LDAPException(PROTOCOL_ERROR, message);
    case OP_TYPE_SEARCH_RESULT_REFERENCE: // 0x73
      decodeSearchReference(reader, messageID, handler);
    case 0x74: // 0x74
    case 0x75: // 0x75
    case 0x76: // 0x76
      message = ERR_LDAP_PROTOCOL_OP_DECODE_INVALID_TYPE.get(type);
      throw new LDAPException(PROTOCOL_ERROR, message);
    case OP_TYPE_EXTENDED_REQUEST: // 0x77
      decodeExtendedRequest(reader, messageID, handler);
    case OP_TYPE_EXTENDED_RESPONSE: // 0x78
      decodeExtendedResponse(reader, messageID, handler);
    case OP_TYPE_INTERMEDIATE_RESPONSE: // 0x79
      decodeIntermediateResponse(reader, messageID, handler);
    default:
      message = ERR_LDAP_PROTOCOL_OP_DECODE_INVALID_TYPE.get(type);
      throw new LDAPException(PROTOCOL_ERROR, message);
    }
  }



  private static String decodeResponseDiagMessage(ASN1Reader reader)
      throws LDAPException
  {
    try
    {
      return reader.readOctetStringAsString();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_RESULT_DECODE_ERROR_MESSAGE.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }
  }



  private static String decodeResponseMatchedDN(ASN1Reader reader)
      throws LDAPException
  {
    try
    {
      return reader.readOctetStringAsString();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_RESULT_DECODE_MATCHED_DN.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }
  }



  private static void decodeResponseReferrals(ASN1Reader reader,
      RawResultResponse rawMessage) throws LDAPException
  {
    try
    {
      if (reader.hasNextElement()
          && reader.peekType() == TYPE_REFERRAL_SEQUENCE)
      {
        reader.readStartSequence();
        // Should have at least 1.
        do
        {
          rawMessage.addReferral((reader.readOctetStringAsString()));
        }
        while (reader.hasNextElement());
        reader.readEndSequence();
      }
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_RESULT_DECODE_REFERRALS.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }
  }



  private static ResultCode decodeResponseResultCode(ASN1Reader reader)
      throws LDAPException
  {
    try
    {
      return ResultCode.valueOf((int) reader.readInteger());
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_RESULT_DECODE_RESULT_CODE.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
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
  private static void decodeSearchDone(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws LDAPException
  {
    try
    {
      reader.readStartSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_RESULT_DECODE_SEQUENCE.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    ResultCode resultCode = decodeResponseResultCode(reader);
    String matchedDN = decodeResponseMatchedDN(reader);
    String diagnosticMessage = decodeResponseDiagMessage(reader);

    RawSearchResultDone rawMessage =
        new RawSearchResultDone(resultCode, matchedDN,
            diagnosticMessage);

    decodeResponseReferrals(reader, rawMessage);

    try
    {
      reader.readEndSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_RESULT_DECODE_SEQUENCE.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

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
  private static void decodeSearchEntry(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws LDAPException
  {
    try
    {
      reader.readStartSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_SEARCH_ENTRY_DECODE_SEQUENCE.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    String dn;
    try
    {
      dn = reader.readOctetStringAsString();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_SEARCH_ENTRY_DECODE_DN.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    RawSearchResultEntry rawMessage = new RawSearchResultEntry(dn);
    try
    {
      reader.readStartSequence();
      while (reader.hasNextElement())
      {
        rawMessage.addPartialAttribute(LDAPAttribute.decode(reader));
      }
      reader.readEndSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_SEARCH_ENTRY_DECODE_ATTRS.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    try
    {
      reader.readEndSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_SEARCH_ENTRY_DECODE_SEQUENCE.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

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
  private static void decodeSearchReference(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws LDAPException
  {
    try
    {
      reader.readStartSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_SEARCH_REFERENCE_DECODE_SEQUENCE.get(String
              .valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    RawSearchResultReference rawMessage;
    try
    {
      // Should have at least one uri.
      rawMessage =
          new RawSearchResultReference(reader.readOctetStringAsString());
      while (reader.hasNextElement())
      {
        rawMessage.addURI(reader.readOctetStringAsString());
      }
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_SEARCH_REFERENCE_DECODE_URLS.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    try
    {
      reader.readEndSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_SEARCH_REFERENCE_DECODE_SEQUENCE.get(String
              .valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

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
  private static void decodeSearchRequest(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws LDAPException
  {
    try
    {
      reader.readStartSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_SEARCH_REQUEST_DECODE_SEQUENCE
              .get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    String baseDN;
    try
    {
      baseDN = reader.readOctetStringAsString();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_SEARCH_REQUEST_DECODE_BASE.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    SearchScope scope;
    try
    {
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
        throw new LDAPException(PROTOCOL_ERROR, message);
      }
    }
    catch (LDAPException le)
    {
      throw le;
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_SEARCH_REQUEST_DECODE_SCOPE.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    DereferencePolicy dereferencePolicy;
    try
    {
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
        throw new LDAPException(PROTOCOL_ERROR, message);
      }
    }
    catch (LDAPException le)
    {
      throw le;
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_SEARCH_REQUEST_DECODE_DEREF.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    int sizeLimit;
    try
    {
      sizeLimit = (int) reader.readInteger();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_SEARCH_REQUEST_DECODE_SIZE_LIMIT.get(String
              .valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    int timeLimit;
    try
    {
      timeLimit = (int) reader.readInteger();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_SEARCH_REQUEST_DECODE_TIME_LIMIT.get(String
              .valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    boolean typesOnly;
    try
    {
      typesOnly = reader.readBoolean();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_SEARCH_REQUEST_DECODE_TYPES_ONLY.get(String
              .valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    RawFilter filter;
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
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    RawSearchRequest rawMessage =
        new RawSearchRequest(baseDN, scope, filter);
    rawMessage.setDereferencePolicy(dereferencePolicy);
    rawMessage.setTimeLimit(timeLimit);
    rawMessage.setSizeLimit(sizeLimit);
    rawMessage.setTypesOnly(typesOnly);

    try
    {
      reader.readStartSequence();
      while (reader.hasNextElement())
      {
        rawMessage.addAttribute(reader.readOctetStringAsString());
      }
      reader.readEndSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_SEARCH_REQUEST_DECODE_ATTRIBUTES.get(String
              .valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    try
    {
      reader.readEndSequence();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_LDAP_SEARCH_REQUEST_DECODE_SEQUENCE
              .get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
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
  private static void decodeUnbindRequest(ASN1Reader reader,
      int messageID, LDAPMessageHandler handler) throws LDAPException
  {
    RawUnbindRequest rawMessage;
    try
    {
      reader.readNull();
      rawMessage = new RawUnbindRequest();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message = ERR_LDAP_UNBIND_DECODE.get(String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, message, e);
    }

    decodeControls(reader, rawMessage);
    handler.handleRequest(messageID, rawMessage);
  }
}
