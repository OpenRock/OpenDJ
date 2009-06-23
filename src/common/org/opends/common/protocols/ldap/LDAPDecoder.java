package org.opends.common.protocols.ldap;


import org.opends.common.api.raw.*;
import org.opends.common.api.raw.request.*;
import org.opends.common.api.raw.request.extended.RawExtendedRequest;
import org.opends.common.api.raw.request.filter.*;
import org.opends.common.api.raw.response.*;
import org.opends.common.protocols.asn1.ASN1StreamReader;
import static org.opends.server.loggers.debug.DebugLogger.getTracer;
import org.opends.server.loggers.debug.DebugTracer;
import static org.opends.server.protocols.asn1.ASN1Constants.*;
import static org.opends.server.protocols.ldap.LDAPConstants.*;
import org.opends.server.types.ByteString;

import java.io.IOException;


public class LDAPDecoder
{
  /**
   * The tracer object for the debug logger.
   */
  private static final DebugTracer TRACER = getTracer();
  private static final String EMPTY_STRING = "".intern();


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
   *           If an error occured while reading bytes to decode.
   */
  public static void decode(ASN1StreamReader reader, LDAPMessageHandler handler)
      throws IOException, UnsupportedMessageException
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
   * @throws IOException
   *           If an error occured while reading bytes to decode.
   */
  private static void decodeAbandonRequest(ASN1StreamReader reader, int messageID,
                                           LDAPMessageHandler handler)
      throws IOException, UnsupportedMessageException
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
   * @throws IOException
   *           If an error occured while reading bytes to decode.
   */
  private static void decodeAddRequest(ASN1StreamReader reader, int messageID,
                                       LDAPMessageHandler handler)
      throws IOException, UnsupportedMessageException
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
   * @throws IOException
   *           If an error occured while reading bytes to decode.
   */
  private static void decodeAddResponse(ASN1StreamReader reader, int messageID,
                                        LDAPMessageHandler handler)
      throws IOException, UnsupportedMessageException
  {
    reader.readStartSequence(OP_TYPE_ADD_RESPONSE);
    int resultCode = reader.readEnumerated();
    String matchedDN = reader.readOctetStringAsString();
    String diagnosticMessage = reader.readOctetStringAsString();
    RawAddResponse rawMessage =
        new RawAddResponse(resultCode, matchedDN, diagnosticMessage);
    decodeResponseReferrals(reader, rawMessage);
    reader.readEndSequence();

    decodeControls(reader, rawMessage);
    handler.handleResponse(messageID, rawMessage);
  }



  private static RawPartialAttribute decodeAttribute(ASN1StreamReader reader)
      throws IOException
  {
    reader.readStartSequence();
    String attributeType = reader.readOctetStringAsString();
    RawPartialAttribute rawAttribute;
    reader.readStartSet();
    // Should contain at least one value.
    rawAttribute = new RawPartialAttribute(attributeType, reader.readOctetString());
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
   * @throws IOException
   *           If an error occured while reading bytes to decode.
   */
  private static void decodeBindRequest(ASN1StreamReader reader, int messageID,
                                        LDAPMessageHandler handler)
      throws IOException, UnsupportedMessageException
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

          RawSimpleBindRequest simpleBindMessage =
              new RawSimpleBindRequest(dn, simplePassword);

          decodeControls(reader, simpleBindMessage);
          handler.handleRequest(messageID, protocolVersion, simpleBindMessage);
          break;
        case TYPE_AUTHENTICATION_SASL:
          reader.readStartSequence(TYPE_AUTHENTICATION_SASL);
          String saslMechanism = reader.readOctetStringAsString();
          ByteString saslCredentials;
          if (reader.hasNextElement() &&
              reader.peekType() == UNIVERSAL_OCTET_STRING_TYPE)
          {
            saslCredentials = reader.readOctetString();
          }
          else
          {
            saslCredentials = ByteString.empty();
          }
          reader.readEndSequence();

          RawSASLBindRequest rawSASLBindMessage =
              new RawSASLBindRequest(saslMechanism, saslCredentials);
          rawSASLBindMessage.setBindDN(dn);

          decodeControls(reader, rawSASLBindMessage);
          handler.handleRequest(messageID, protocolVersion, rawSASLBindMessage);
          break;
        default:
          ByteString unknownAuthBytes = reader.readOctetString(type);

          RawUnknownBindRequest rawUnknownBindMessage =
              new RawUnknownBindRequest(dn, type, unknownAuthBytes);

          decodeControls(reader, rawUnknownBindMessage);
          handler.handleRequest(messageID, protocolVersion,
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
   *           If an error occured while reading bytes to decode.
   */
  private static void decodeBindResponse(ASN1StreamReader reader, int messageID,
                                         LDAPMessageHandler handler)
      throws IOException, UnsupportedMessageException
  {
    reader.readStartSequence(OP_TYPE_BIND_RESPONSE);
    int resultCode = reader.readEnumerated();
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
   * @throws IOException
   *           If an error occured while reading bytes to decode.
   */
  private static void decodeCompareRequest(ASN1StreamReader reader,
                                           int messageID,
                                           LDAPMessageHandler handler)
      throws IOException, UnsupportedMessageException
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
   * @throws IOException
   *           If an error occured while reading bytes to decode.
   */
  private static void decodeCompareResponse(ASN1StreamReader reader,
                                            int messageID,
                                            LDAPMessageHandler handler)
      throws IOException, UnsupportedMessageException
  {
    reader.readStartSequence(OP_TYPE_COMPARE_RESPONSE);
    int resultCode = reader.readEnumerated();
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
   * @throws IOException
   *           If an error occured while reading bytes to decode.
   */
  private static void decodeControl(ASN1StreamReader reader,
                                    RawMessage rawMessage)
      throws IOException, UnsupportedMessageException
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
   * @throws IOException
   *           If an error occured while reading bytes to decode.
   */
  private static void decodeControls(ASN1StreamReader reader,
                                     RawMessage rawMessage)
      throws IOException, UnsupportedMessageException
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
   * @throws IOException
   *           If an error occured while reading bytes to decode.
   */
  private static void decodeDeleteRequest(ASN1StreamReader reader,
                                          int messageID,
                                          LDAPMessageHandler handler)
      throws IOException, UnsupportedMessageException
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
   * @throws IOException
   *           If an error occured while reading bytes to decode.
   */
  private static void decodeDeleteResponse(ASN1StreamReader reader,
                                           int messageID,
                                           LDAPMessageHandler handler)
      throws IOException, UnsupportedMessageException
  {
    reader.readStartSequence(OP_TYPE_DELETE_RESPONSE);

    int resultCode = reader.readEnumerated();
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
   * @throws IOException
   *           If an error occured while reading bytes to decode.
   */
  private static void decodeExtendedRequest(ASN1StreamReader reader,
                                            int messageID,
                                            LDAPMessageHandler handler)
      throws IOException, UnsupportedMessageException
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
   * @throws IOException
   *           If an error occured while reading bytes to decode.
   */
  private static void decodeExtendedResponse(ASN1StreamReader reader,
                                             int messageID,
                                             LDAPMessageHandler handler)
      throws IOException, UnsupportedMessageException
  {
    reader.readStartSequence(OP_TYPE_EXTENDED_RESPONSE);

    int resultCode = reader.readEnumerated();
    String matchedDN = reader.readOctetStringAsString();
    String diagnosticMessage = reader.readOctetStringAsString();
    RawExtendedResponse rawMessage =
        new RawExtendedResponse(resultCode, matchedDN,
                                diagnosticMessage);
    decodeResponseReferrals(reader, rawMessage);
    if (reader.hasNextElement()
        && reader.peekType() == TYPE_EXTENDED_RESPONSE_OID)
    {
      rawMessage.setResponseName(reader.readOctetStringAsString(
          TYPE_EXTENDED_RESPONSE_OID));
    }
    if (reader.hasNextElement()
        && reader.peekType() == TYPE_EXTENDED_RESPONSE_VALUE)
    {
      rawMessage.setResponseValue(reader.readOctetString(
          TYPE_EXTENDED_RESPONSE_VALUE));
    }
    reader.readEndSequence();

    decodeControls(reader, rawMessage);
    handler.handleResponse(messageID, rawMessage);
  }


  private static RawFilter decodeFilter(ASN1StreamReader reader)
      throws IOException
  {
    byte type = reader.peekType();

    switch (type)
    {
      case TYPE_FILTER_AND:
        reader.readStartSequence(type);
        RawAndFilter andFilter = new RawAndFilter(decodeFilter(reader));
        while(reader.hasNextElement())
        {
          andFilter.addComponent(decodeFilter(reader));
        }
        reader.readEndSequence();
        return andFilter;

      case TYPE_FILTER_OR:
        reader.readStartSequence(type);
        RawOrFilter orFilter = new RawOrFilter(decodeFilter(reader));
        while(reader.hasNextElement())
        {
          orFilter.addComponent(decodeFilter(reader));
        }
        reader.readEndSequence();
        return orFilter;


      case TYPE_FILTER_NOT:
        reader.readStartSequence(type);
        RawNotFilter notFilter = new RawNotFilter(decodeFilter(reader));
        reader.readEndSequence();
        return notFilter;

      case TYPE_FILTER_EQUALITY:
        reader.readStartSequence(type);
        RawEqualFilter equalFilter =
            new RawEqualFilter(reader.readOctetStringAsString(),
                               reader.readOctetString());
        reader.readEndSequence();
        return equalFilter;

      case TYPE_FILTER_GREATER_OR_EQUAL:
        reader.readStartSequence(type);
        RawGreaterOrEqualFilter geFilter =
            new RawGreaterOrEqualFilter(reader.readOctetStringAsString(),
                               reader.readOctetString());
        reader.readEndSequence();
        return geFilter;

      case TYPE_FILTER_LESS_OR_EQUAL:
        reader.readStartSequence(type);
        RawLessOrEqualFilter leFilter =
            new RawLessOrEqualFilter(reader.readOctetStringAsString(),
                               reader.readOctetString());
        reader.readEndSequence();
        return leFilter;

      case TYPE_FILTER_APPROXIMATE:
        reader.readStartSequence(type);
        RawApproximateFilter approxFilter =
            new RawApproximateFilter(reader.readOctetStringAsString(),
                               reader.readOctetString());
        reader.readEndSequence();
        return approxFilter;

      case TYPE_FILTER_SUBSTRING:
        RawSubstringFilter substringFilter = null;
        ByteString initialSubstring = null;
        ByteString finalSubstring = null;

        reader.readStartSequence(type);
        String substringType = reader.readOctetStringAsString();
        reader.readStartSequence();

        // There should be at least one element in this substring filter
        // sequence.
        if(reader.peekType() == TYPE_SUBINITIAL)
        {
          initialSubstring = reader.readOctetString(TYPE_SUBINITIAL);
        }
        if(reader.hasNextElement() && reader.peekType() == TYPE_SUBANY)
        {
          substringFilter =
              new RawSubstringFilter(substringType,
                                     reader.readOctetString(TYPE_SUBANY));
          while(reader.hasNextElement() && reader.peekType() == TYPE_SUBANY)
          {
            substringFilter.addAnyString(reader.readOctetString(TYPE_SUBANY));
          }

        }
        if(reader.hasNextElement() && reader.peekType() == TYPE_SUBFINAL)
        {
          finalSubstring = reader.readOctetString(TYPE_SUBFINAL);
        }

        reader.readEndSequence();
        reader.readEndSequence();
        return substringFilter == null ?
               new RawSubstringFilter(substringType, initialSubstring,
                                      finalSubstring) :
               substringFilter.setInitialString(initialSubstring).
                   setFinalString(finalSubstring);

      case TYPE_FILTER_PRESENCE:
        return new RawPresenceFilter(reader.readOctetStringAsString(type));

      case TYPE_FILTER_EXTENSIBLE_MATCH:
        String extensibleType = EMPTY_STRING;
        String matchingRuleID = EMPTY_STRING;

        reader.readStartSequence(type);
        if(reader.peekType() == TYPE_MATCHING_RULE_ID)
        {
          matchingRuleID =
              reader.readOctetStringAsString(TYPE_MATCHING_RULE_ID);
        }
        if(reader.peekType() == TYPE_MATCHING_RULE_TYPE)
        {
          extensibleType =
              reader.readOctetStringAsString(TYPE_MATCHING_RULE_TYPE);
        }
        RawExtensibleFilter extensibleFilter =
            new RawExtensibleFilter(reader.readOctetString(
                TYPE_MATCHING_RULE_VALUE));
        if(reader.hasNextElement() &&
           reader.peekType() == TYPE_MATCHING_RULE_DN_ATTRIBUTES)
        {
          extensibleFilter.setDnAttributes(reader.readBoolean());
        }
        reader.readEndSequence();
        extensibleFilter.setAttributeType(extensibleType);
        extensibleFilter.setMatchingRule(matchingRuleID);
        return extensibleFilter;

      default:
        return new RawUnknownFilter(type, reader.readOctetString(type));
    }
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
   *           If an error occured while reading bytes to decode.
   */
  private static void decodeIntermediateResponse(ASN1StreamReader reader,
                                                 int messageID,
                                                 LDAPMessageHandler handler)
      throws IOException, UnsupportedMessageException
  {
    reader.readStartSequence(OP_TYPE_INTERMEDIATE_RESPONSE);
    RawIntermediateResponse rawMessage = new RawIntermediateResponse();
    if (reader.hasNextElement()
        && reader.peekType() == TYPE_INTERMEDIATE_RESPONSE_OID)
    {
      rawMessage.setResponseName(
          reader.readOctetStringAsString(TYPE_INTERMEDIATE_RESPONSE_OID));
    }
    if (reader.hasNextElement()
        && reader.peekType() == TYPE_INTERMEDIATE_RESPONSE_VALUE)
    {
      rawMessage.setResponseValue(
          reader.readOctetString(TYPE_INTERMEDIATE_RESPONSE_VALUE));
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
   * @throws IOException
   *           If an error occured while reading bytes to decode.
   */
  private static void decodeModifyDNRequest(ASN1StreamReader reader,
                                            int messageID,
                                            LDAPMessageHandler handler)
      throws IOException, UnsupportedMessageException
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
   * @throws IOException
   *           If an error occured while reading bytes to decode.
   */
  private static void decodeModifyDNResponse(ASN1StreamReader reader,
                                             int messageID,
                                             LDAPMessageHandler handler)
      throws IOException, UnsupportedMessageException
  {
    reader.readStartSequence(OP_TYPE_MODIFY_DN_RESPONSE);

    int resultCode = reader.readEnumerated();
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
   * @throws IOException
   *           If an error occured while reading bytes to decode.
   */
  private static void decodeModifyRequest(ASN1StreamReader reader,
                                          int messageID,
                                          LDAPMessageHandler handler)
      throws IOException, UnsupportedMessageException
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
          int type = (int)reader.readInteger();
          RawPartialAttribute attribute = decodePartialAttribute(reader);
          rawMessage.addChange(new RawModifyRequest.Change(type, attribute));
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
   * @throws IOException
   *           If an error occured while reading bytes to decode.
   */
  private static void decodeModifyResponse(ASN1StreamReader reader,
                                           int messageID,
                                           LDAPMessageHandler handler)
      throws IOException, UnsupportedMessageException
  {
    reader.readStartSequence(OP_TYPE_MODIFY_RESPONSE);

    int resultCode = reader.readEnumerated();
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
   * @throws IOException
   *           If an error occured while reading bytes to decode.
   */
  private static void decodeProtocolOp(ASN1StreamReader reader, int messageID,
                                       LDAPMessageHandler handler)
      throws IOException, UnsupportedMessageException
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
        handler.handleMessage(messageID, new RawUnknownMessage(
            type, reader.readOctetString(type)));
        break;
      case OP_TYPE_DELETE_REQUEST: // 0x4A
        decodeDeleteRequest(reader, messageID, handler);
        break;
      case 0x4B: // 0x4B
      case 0x4C: // 0x4C
      case 0x4D: // 0x4D
      case 0x4E: // 0x4E
      case 0x4F: // 0x4F
        handler.handleMessage(messageID, new RawUnknownMessage(
            type, reader.readOctetString(type)));
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
        handler.handleMessage(messageID, new RawUnknownMessage(
            type, reader.readOctetString(type)));
        break;
      case OP_TYPE_BIND_REQUEST: // 0x60
        decodeBindRequest(reader, messageID, handler);
        break;
      case OP_TYPE_BIND_RESPONSE: // 0x61
        decodeBindResponse(reader, messageID, handler);
        break;
      case 0x62: // 0x62
        handler.handleMessage(messageID, new RawUnknownMessage(
            type, reader.readOctetString(type)));
        break;
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
        handler.handleMessage(messageID, new RawUnknownMessage(
            type, reader.readOctetString(type)));
        break;
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
        handler.handleMessage(messageID, new RawUnknownMessage(
            type, reader.readOctetString(type)));
        break;
      case OP_TYPE_SEARCH_RESULT_REFERENCE: // 0x73
        decodeSearchReference(reader, messageID, handler);
        break;
      case 0x74: // 0x74
      case 0x75: // 0x75
      case 0x76: // 0x76
        handler.handleMessage(messageID, new RawUnknownMessage(
            type, reader.readOctetString(type)));
        break;
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
        handler.handleMessage(messageID, new RawUnknownMessage(
            type, reader.readOctetString(type)));
        break;
    }
  }



  private static RawPartialAttribute
    decodePartialAttribute(ASN1StreamReader reader)
      throws IOException
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
      throws IOException
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
   * @throws IOException
   *           If an error occured while reading bytes to decode.
   */
  private static void decodeSearchDone(ASN1StreamReader reader, int messageID,
                                       LDAPMessageHandler handler)
      throws IOException, UnsupportedMessageException
  {
    reader.readStartSequence(OP_TYPE_SEARCH_RESULT_DONE);

    int resultCode = reader.readEnumerated();
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
   * @throws IOException
   *           If an error occured while reading bytes to decode.
   */
  private static void decodeSearchEntry(ASN1StreamReader reader,
                                        int messageID,
                                        LDAPMessageHandler handler)
      throws IOException, UnsupportedMessageException
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
   * @throws IOException
   *           If an error occured while reading bytes to decode.
   */
  private static void decodeSearchReference(ASN1StreamReader reader,
                                            int messageID,
                                            LDAPMessageHandler handler)
      throws IOException, UnsupportedMessageException
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
   * @throws IOException
   *           If an error occured while reading bytes to decode.
   */
  private static void decodeSearchRequest(ASN1StreamReader reader,
                                          int messageID,
                                          LDAPMessageHandler handler)
      throws IOException, UnsupportedMessageException
  {
    reader.readStartSequence(OP_TYPE_SEARCH_REQUEST);
    String baseDN;
    int scope;
    int dereferencePolicy;
    int sizeLimit;
    int timeLimit;
    boolean typesOnly;
    RawFilter filter;
    RawSearchRequest rawMessage;
    try
    {
      baseDN = reader.readOctetStringAsString();
      scope = (int) reader.readInteger();
      dereferencePolicy = (int) reader.readInteger();
      sizeLimit = (int) reader.readInteger();
      timeLimit = (int) reader.readInteger();
      typesOnly = reader.readBoolean();
      filter = decodeFilter(reader);
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
   * @throws IOException
   *           If an error occured while reading bytes to decode.
   */
  private static void decodeUnbindRequest(ASN1StreamReader reader,
                                          int messageID, LDAPMessageHandler handler)
      throws IOException, UnsupportedMessageException
  {
    RawUnbindRequest rawMessage;
    reader.readNull(OP_TYPE_UNBIND_REQUEST);
    rawMessage = new RawUnbindRequest();

    decodeControls(reader, rawMessage);
    handler.handleRequest(messageID, rawMessage);
  }
}
