package org.opends.common.protocols.ldap;


import org.opends.common.api.request.*;
import org.opends.common.api.filter.*;
import org.opends.common.api.response.*;
import org.opends.common.api.*;
import org.opends.common.api.controls.Control;
import org.opends.common.api.extended.GenericExtendedRequest;
import org.opends.common.api.extended.GenericExtendedResponse;
import org.opends.common.api.extended.GenericIntermediateResponse;
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
  public static void decode(ASN1StreamReader reader,
                            LDAPMessageHandler handler)
      throws IOException
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
      throws IOException
  {
    int msgToAbandon = (int) reader.readInteger(OP_TYPE_ABANDON_REQUEST);
    AbandonRequest rawMessage = new AbandonRequest(msgToAbandon);

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
      throws IOException
  {
    reader.readStartSequence(OP_TYPE_ADD_REQUEST);
    String dn = reader.readOctetStringAsString();
    AddRequest rawMessage = new AddRequest(dn);
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
      throws IOException
  {
    reader.readStartSequence(OP_TYPE_ADD_RESPONSE);
    ResultCode resultCode =
        ResultCode.valueOf(reader.readEnumerated());
    String matchedDN = reader.readOctetStringAsString();
    String diagnosticMessage = reader.readOctetStringAsString();
    AddResponse rawMessage =
        new AddResponse(resultCode, matchedDN, diagnosticMessage);
    decodeResponseReferrals(reader, rawMessage);
    reader.readEndSequence();

    decodeControls(reader, rawMessage);
    handler.handleResponse(messageID, rawMessage);
  }



  private static Attribute decodeAttribute(ASN1StreamReader reader)
      throws IOException
  {
    reader.readStartSequence();
    String attributeType = reader.readOctetStringAsString();
    Attribute rawAttribute;
    reader.readStartSet();
    // Should contain at least one value.
    rawAttribute = new Attribute(attributeType,
        reader.readOctetString(), null);
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
      throws IOException
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
              new SimpleBindRequest(dn, simplePassword);

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

          SASLBindRequest rawSASLBindMessage =
              new SASLBindRequest(saslMechanism, saslCredentials);
          rawSASLBindMessage.setBindDN(dn);

          decodeControls(reader, rawSASLBindMessage);
          handler.handleRequest(messageID, protocolVersion, rawSASLBindMessage);
          break;
        default:
          ByteString unknownAuthBytes = reader.readOctetString(type);

          GenericBindRequest rawUnknownBindMessage =
              new GenericBindRequest(dn, type, unknownAuthBytes);

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
      throws IOException
  {
    reader.readStartSequence(OP_TYPE_BIND_RESPONSE);
    ResultCode resultCode =
        ResultCode.valueOf(reader.readEnumerated());
    String matchedDN = reader.readOctetStringAsString();
    String diagnosticMessage = reader.readOctetStringAsString();
    BindResponse rawMessage =
        new BindResponse(resultCode, matchedDN, diagnosticMessage);
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
      throws IOException
  {
    reader.readStartSequence(OP_TYPE_COMPARE_REQUEST);
    String dn = reader.readOctetStringAsString();
    reader.readStartSequence();
    String attributeType = reader.readOctetStringAsString();
    ByteString assertionValue = reader.readOctetString();
    reader.readEndSequence();
    reader.readEndSequence();
    CompareRequest rawMessage =
        new CompareRequest(dn, attributeType, assertionValue);
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
      throws IOException
  {
    reader.readStartSequence(OP_TYPE_COMPARE_RESPONSE);
    ResultCode resultCode =
        ResultCode.valueOf(reader.readEnumerated());
    String matchedDN = reader.readOctetStringAsString();
    String diagnosticMessage = reader.readOctetStringAsString();
    CompareResponse rawMessage =
        new CompareResponse(resultCode, matchedDN, diagnosticMessage);
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
                                    Message rawMessage)
      throws IOException
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

    rawMessage.addControl(new Control(oid, isCritical, value));
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
                                     Message rawMessage)
      throws IOException
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
      throws IOException
  {
    String dn = reader.readOctetStringAsString(OP_TYPE_DELETE_REQUEST);
    DeleteRequest rawMessage = new DeleteRequest(dn);

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
      throws IOException
  {
    reader.readStartSequence(OP_TYPE_DELETE_RESPONSE);
    ResultCode resultCode =
        ResultCode.valueOf(reader.readEnumerated());
    String matchedDN = reader.readOctetStringAsString();
    String diagnosticMessage = reader.readOctetStringAsString();
    DeleteResponse rawMessage =
        new DeleteResponse(resultCode, matchedDN, diagnosticMessage);
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
      throws IOException
  {
    reader.readStartSequence(OP_TYPE_EXTENDED_REQUEST);
    String oid = reader.readOctetStringAsString(TYPE_EXTENDED_REQUEST_OID);
    GenericExtendedRequest rawMessage =
        new GenericExtendedRequest(oid);
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
      throws IOException
  {
    reader.readStartSequence(OP_TYPE_EXTENDED_RESPONSE);

    ResultCode resultCode =
        ResultCode.valueOf(reader.readEnumerated());
    String matchedDN = reader.readOctetStringAsString();
    String diagnosticMessage = reader.readOctetStringAsString();
    GenericExtendedResponse rawMessage =
        new GenericExtendedResponse(resultCode, matchedDN,
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


  private static Filter decodeFilter(ASN1StreamReader reader)
      throws IOException
  {
    byte type = reader.peekType();

    switch (type)
    {
      case TYPE_FILTER_AND:
        reader.readStartSequence(type);
        AndFilter andFilter = new AndFilter(decodeFilter(reader));
        while(reader.hasNextElement())
        {
          andFilter.addComponent(decodeFilter(reader));
        }
        reader.readEndSequence();
        return andFilter;

      case TYPE_FILTER_OR:
        reader.readStartSequence(type);
        OrFilter orFilter = new OrFilter(decodeFilter(reader));
        while(reader.hasNextElement())
        {
          orFilter.addComponent(decodeFilter(reader));
        }
        reader.readEndSequence();
        return orFilter;


      case TYPE_FILTER_NOT:
        reader.readStartSequence(type);
        NotFilter notFilter = new NotFilter(decodeFilter(reader));
        reader.readEndSequence();
        return notFilter;

      case TYPE_FILTER_EQUALITY:
        reader.readStartSequence(type);
        EqualFilter equalFilter =
            new EqualFilter(reader.readOctetStringAsString(),
                               reader.readOctetString());
        reader.readEndSequence();
        return equalFilter;

      case TYPE_FILTER_GREATER_OR_EQUAL:
        reader.readStartSequence(type);
        GreaterOrEqualFilter geFilter =
            new GreaterOrEqualFilter(reader.readOctetStringAsString(),
                               reader.readOctetString());
        reader.readEndSequence();
        return geFilter;

      case TYPE_FILTER_LESS_OR_EQUAL:
        reader.readStartSequence(type);
        LessOrEqualFilter leFilter =
            new LessOrEqualFilter(reader.readOctetStringAsString(),
                               reader.readOctetString());
        reader.readEndSequence();
        return leFilter;

      case TYPE_FILTER_APPROXIMATE:
        reader.readStartSequence(type);
        ApproximateFilter approxFilter =
            new ApproximateFilter(reader.readOctetStringAsString(),
                               reader.readOctetString());
        reader.readEndSequence();
        return approxFilter;

      case TYPE_FILTER_SUBSTRING:
        SubstringFilter substringFilter = null;
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
              new SubstringFilter(substringType, initialSubstring,
                  null, reader.readOctetString(TYPE_SUBANY));
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
               new SubstringFilter(substringType, initialSubstring,
                                      finalSubstring) :
               substringFilter.setFinalString(finalSubstring);

      case TYPE_FILTER_PRESENCE:
        return new PresenceFilter(reader.readOctetStringAsString(type));

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
        ExtensibleFilter extensibleFilter =
            new ExtensibleFilter(reader.readOctetString(
                TYPE_MATCHING_RULE_VALUE));
        if(reader.hasNextElement() &&
           reader.peekType() == TYPE_MATCHING_RULE_DN_ATTRIBUTES)
        {
          extensibleFilter.setDnAttributes(reader.readBoolean());
        }
        reader.readEndSequence();
        extensibleFilter.setAttributeDescription(extensibleType);
        extensibleFilter.setMatchingRule(matchingRuleID);
        return extensibleFilter;

      default:
        return new GenericFilter(type, reader.readOctetString(type));
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
      throws IOException
  {
    reader.readStartSequence(OP_TYPE_INTERMEDIATE_RESPONSE);
    GenericIntermediateResponse rawMessage =
        new GenericIntermediateResponse();
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
      throws IOException
  {
    reader.readStartSequence(OP_TYPE_MODIFY_DN_REQUEST);
    String entryDN = reader.readOctetStringAsString();
    String newRDN = reader.readOctetStringAsString();
    ModifyDNRequest rawMessage =
        new ModifyDNRequest(entryDN, newRDN);
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
      throws IOException
  {
    reader.readStartSequence(OP_TYPE_MODIFY_DN_RESPONSE);

    ResultCode resultCode =
        ResultCode.valueOf(reader.readEnumerated());
    String matchedDN = reader.readOctetStringAsString();
    String diagnosticMessage = reader.readOctetStringAsString();
    ModifyDNResponse rawMessage =
        new ModifyDNResponse(resultCode, matchedDN,
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
      throws IOException
  {
    reader.readStartSequence(OP_TYPE_MODIFY_REQUEST);
    String dn = reader.readOctetStringAsString();
    ModifyRequest rawMessage = new ModifyRequest(dn);
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
          Attribute attribute =
              decodePartialAttribute(reader);
          rawMessage.addChange(new Change(type, attribute));
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
      throws IOException
  {
    reader.readStartSequence(OP_TYPE_MODIFY_RESPONSE);

    ResultCode resultCode =
        ResultCode.valueOf(reader.readEnumerated());
    String matchedDN = reader.readOctetStringAsString();
    String diagnosticMessage = reader.readOctetStringAsString();
    ModifyResponse rawMessage =
        new ModifyResponse(resultCode, matchedDN, diagnosticMessage);
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
      throws IOException
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
        handler.handleMessage(messageID, new GenericMessage(
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
        handler.handleMessage(messageID, new GenericMessage(
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
        handler.handleMessage(messageID, new GenericMessage(
            type, reader.readOctetString(type)));
        break;
      case OP_TYPE_BIND_REQUEST: // 0x60
        decodeBindRequest(reader, messageID, handler);
        break;
      case OP_TYPE_BIND_RESPONSE: // 0x61
        decodeBindResponse(reader, messageID, handler);
        break;
      case 0x62: // 0x62
        handler.handleMessage(messageID, new GenericMessage(
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
        handler.handleMessage(messageID, new GenericMessage(
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
        handler.handleMessage(messageID, new GenericMessage(
            type, reader.readOctetString(type)));
        break;
      case OP_TYPE_SEARCH_RESULT_REFERENCE: // 0x73
        decodeSearchReference(reader, messageID, handler);
        break;
      case 0x74: // 0x74
      case 0x75: // 0x75
      case 0x76: // 0x76
        handler.handleMessage(messageID, new GenericMessage(
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
        handler.handleMessage(messageID, new GenericMessage(
            type, reader.readOctetString(type)));
        break;
    }
  }



  private static Attribute
    decodePartialAttribute(ASN1StreamReader reader)
      throws IOException
  {
    reader.readStartSequence();
    String attributeType = reader.readOctetStringAsString();
    Attribute rawAttribute = new Attribute(attributeType);
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
                                              ResultResponse rawMessage)
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
      throws IOException
  {
    reader.readStartSequence(OP_TYPE_SEARCH_RESULT_DONE);

    ResultCode resultCode =
        ResultCode.valueOf(reader.readEnumerated());
    String matchedDN = reader.readOctetStringAsString();
    String diagnosticMessage = reader.readOctetStringAsString();
    SearchResultDone rawMessage =
        new SearchResultDone(resultCode, matchedDN,
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
      throws IOException
  {
    reader.readStartSequence(OP_TYPE_SEARCH_RESULT_ENTRY);
    String dn = reader.readOctetStringAsString();
    SearchResultEntry rawMessage = new SearchResultEntry(dn);
    reader.readStartSequence();
    while (reader.hasNextElement())
    {
      rawMessage.addAttribute(decodePartialAttribute(reader));
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
      throws IOException
  {
    reader.readStartSequence(OP_TYPE_SEARCH_RESULT_REFERENCE);
    SearchResultReference rawMessage =
        new SearchResultReference(reader.readOctetStringAsString());
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
      throws IOException
  {
    reader.readStartSequence(OP_TYPE_SEARCH_REQUEST);
    String baseDN;
    SearchScope scope;
    DereferencePolicy dereferencePolicy;
    int sizeLimit;
    int timeLimit;
    boolean typesOnly;
    Filter filter;
    SearchRequest rawMessage;
    try
    {
      baseDN = reader.readOctetStringAsString();
      scope = SearchScope.valueOf(reader.readEnumerated());
      dereferencePolicy = DereferencePolicy.valueOf(
          reader.readEnumerated());
      sizeLimit = (int) reader.readInteger();
      timeLimit = (int) reader.readInteger();
      typesOnly = reader.readBoolean();
      filter = decodeFilter(reader);
      rawMessage = new SearchRequest(baseDN, scope, filter);
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
      throws IOException
  {
    UnbindRequest rawMessage;
    reader.readNull(OP_TYPE_UNBIND_REQUEST);
    rawMessage = new UnbindRequest();

    decodeControls(reader, rawMessage);
    handler.handleRequest(messageID, rawMessage);
  }
}
