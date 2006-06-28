/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE
 * or https://OpenDS.dev.java.net/OpenDS.LICENSE.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE.  If applicable,
 * add the following below this CDDL HEADER, with the fields enclosed
 * by brackets "[]" replaced with your own identifying * information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Portions Copyright 2006 Sun Microsystems, Inc.
 */
package org.opends.server.protocols.ldap;



import java.util.ArrayList;
import java.util.Iterator;

import org.opends.server.core.DirectoryException;
import org.opends.server.core.ModifyOperation;
import org.opends.server.protocols.asn1.ASN1Element;
import org.opends.server.protocols.asn1.ASN1OctetString;
import org.opends.server.protocols.asn1.ASN1Sequence;

import static org.opends.server.loggers.Debug.*;
import static org.opends.server.messages.MessageHandler.*;
import static org.opends.server.messages.ProtocolMessages.*;
import static org.opends.server.protocols.ldap.LDAPConstants.*;
import static org.opends.server.protocols.ldap.LDAPResultCode.*;
import static org.opends.server.util.ServerConstants.*;



/**
 * This class defines the structures and methods for an LDAP modify request
 * protocol op, which is used to alter the contents of an entry in the Directory
 * Server.
 */
public class ModifyRequestProtocolOp
       extends ProtocolOp
{
  /**
   * The fully-qualified name of this class to use for debugging purposes.
   */
  private static final String CLASS_NAME =
       "org.opends.server.protocols.ldap.ModifyRequestProtocolOp";



  // The set of modifications for this modify request.
  private ArrayList<LDAPModification> modifications;

  // The DN for this modify request.
  private ASN1OctetString dn;



  /**
   * Creates a new LDAP modify request protocol op with the specified DN and no
   * modifications.
   *
   * @param  dn  The DN for this modify request.
   */
  public ModifyRequestProtocolOp(ASN1OctetString dn)
  {
    assert debugConstructor(CLASS_NAME, String.valueOf(dn));

    this.dn            = dn;
    this.modifications = new ArrayList<LDAPModification>();
  }



  /**
   * Creates a new LDAP modify request protocol op with the specified DN and set
   * of modifications.
   *
   * @param  dn             The DN for this modify request.
   * @param  modifications  The set of modifications for this modify request.
   */
  public ModifyRequestProtocolOp(ASN1OctetString dn,
                                 ArrayList<LDAPModification> modifications)
  {
    assert debugConstructor(CLASS_NAME, String.valueOf(dn),
                            String.valueOf(modifications));

    this.dn = dn;

    if (modifications == null)
    {
      this.modifications = new ArrayList<LDAPModification>();
    }
    else
    {
      this.modifications = modifications;
    }
  }



  /**
   * Retrieves the DN for this modify request.
   *
   * @return  The DN for this modify request.
   */
  public ASN1OctetString getDN()
  {
    assert debugEnter(CLASS_NAME, "getDN");

    return dn;
  }



  /**
   * Specifies the DN for this modify request.
   *
   * @param  dn  The DN for this modify request.
   */
  public void setDN(ASN1OctetString dn)
  {
    assert debugEnter(CLASS_NAME, "setDN", String.valueOf(dn));

    this.dn = dn;
  }



  /**
   * Retrieves the set of modifications for this modify request.  The returned
   * list may be altered by the caller.
   *
   * @return  The set of modifications for this modify request.
   */
  public ArrayList<LDAPModification> getModifications()
  {
    assert debugEnter(CLASS_NAME, "getModifications");

    return modifications;
  }



  /**
   * Retrieves the BER type for this protocol op.
   *
   * @return  The BER type for this protocol op.
   */
  public byte getType()
  {
    assert debugEnter(CLASS_NAME, "getType");

    return OP_TYPE_MODIFY_REQUEST;
  }



  /**
   * Retrieves the name for this protocol op type.
   *
   * @return  The name for this protocol op type.
   */
  public String getProtocolOpName()
  {
    assert debugEnter(CLASS_NAME, "getProtocolOpName");

    return "Modify Request";
  }



  /**
   * Encodes this protocol op to an ASN.1 element suitable for including in an
   * LDAP message.
   *
   * @return  The ASN.1 element containing the encoded protocol op.
   */
  public ASN1Element encode()
  {
    assert debugEnter(CLASS_NAME, "encode");

    ArrayList<ASN1Element> elements = new ArrayList<ASN1Element>(2);
    elements.add(dn);


    ArrayList<ASN1Element> modElements =
         new ArrayList<ASN1Element>(modifications.size());
    for (LDAPModification mod : modifications)
    {
      modElements.add(mod.encode());
    }
    elements.add(new ASN1Sequence(modElements));


    return new ASN1Sequence(OP_TYPE_MODIFY_REQUEST, elements);
  }



  /**
   * Decodes the provided ASN.1 element as an LDAP modify request protocol op.
   *
   * @param  element  The ASN.1 element to be decoded.
   *
   * @return  The decoded modify request protocol op.
   *
   * @throws  LDAPException  If a problem occurs while decoding the provided
   *                         ASN.1 element as an LDAP modify request protocol
   *                         op.
   */
  public static ModifyRequestProtocolOp decodeModifyRequest(ASN1Element element)
         throws LDAPException
  {
    assert debugEnter(CLASS_NAME, "decodeModifyRequest",
                      String.valueOf(element));

    ArrayList<ASN1Element> elements;
    try
    {
      elements = element.decodeAsSequence().elements();
    }
    catch (Exception e)
    {
      assert debugException(CLASS_NAME, "decodeModifyRequest", e);

      int    msgID   = MSGID_LDAP_MODIFY_REQUEST_DECODE_SEQUENCE;
      String message = getMessage(msgID, String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, msgID, message, e);
    }


    int numElements = elements.size();
    if (numElements != 2)
    {
      int    msgID   = MSGID_LDAP_MODIFY_REQUEST_DECODE_INVALID_ELEMENT_COUNT;
      String message = getMessage(msgID, numElements);
      throw new LDAPException(PROTOCOL_ERROR, msgID, message);
    }


    ASN1OctetString dn;
    try
    {
      dn = elements.get(0).decodeAsOctetString();
    }
    catch (Exception e)
    {
      assert debugException(CLASS_NAME, "decodeModifyRequest", e);

      int    msgID   = MSGID_LDAP_MODIFY_REQUEST_DECODE_DN;
      String message = getMessage(msgID, String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, msgID, message, e);
    }



    ArrayList<LDAPModification> modifications;
    try
    {
      ArrayList<ASN1Element> modElements =
           elements.get(1).decodeAsSequence().elements();
      modifications = new ArrayList<LDAPModification>(modElements.size());
      for (ASN1Element e : modElements)
      {
        modifications.add(LDAPModification.decode(e));
      }
    }
    catch (Exception e)
    {
      assert debugException(CLASS_NAME, "decodeModifyRequest", e);

      int    msgID   = MSGID_LDAP_MODIFY_REQUEST_DECODE_MODS;
      String message = getMessage(msgID, String.valueOf(e));
      throw new LDAPException(PROTOCOL_ERROR, msgID, message, e);
    }


    return new ModifyRequestProtocolOp(dn, modifications);
  }



  /**
   * Converts the provided LDAP message containing a modify request protocol op
   * to a <CODE>ModifyOperation</CODE> object that may be processed by the core
   * server.
   *
   * @param  requestMessage    The LDAP message containing the modify request
   *                           protocol op.
   * @param  clientConnection  The client connection from which the request was
   *                           read.
   *
   * @return  The modify operation created from the provided request message.
   *
   * @throws  DirectoryException  If the provided LDAP message cannot be decoded
   *                              as a modify operation.
   */
  public static ModifyOperation messageToModifyOperation(
                                     LDAPMessage requestMessage,
                                     LDAPClientConnection clientConnection)
         throws DirectoryException
  {
    assert debugEnter(CLASS_NAME, "messageToModifyOperation",
                      String.valueOf(requestMessage),
                      String.valueOf(clientConnection));

    // NYI
    return null;
  }



  /**
   * Appends a string representation of this LDAP protocol op to the provided
   * buffer.
   *
   * @param  buffer  The buffer to which the string should be appended.
   */
  public void toString(StringBuilder buffer)
  {
    assert debugEnter(CLASS_NAME, "toString", "java.lang.StringBuilder");

    buffer.append("ModifyRequest(dn=");
    dn.toString(buffer);
    buffer.append(", mods={");

    if (! modifications.isEmpty())
    {
      Iterator<LDAPModification> iterator = modifications.iterator();
      iterator.next().toString(buffer);

      while (iterator.hasNext())
      {
        buffer.append(", ");
        iterator.next().toString(buffer);
      }
    }

    buffer.append("})");
  }



  /**
   * Appends a multi-line string representation of this LDAP protocol op to the
   * provided buffer.
   *
   * @param  buffer  The buffer to which the information should be appended.
   * @param  indent  The number of spaces from the margin that the lines should
   *                 be indented.
   */
  public void toString(StringBuilder buffer, int indent)
  {
    assert debugEnter(CLASS_NAME, "toString", "java.lang.StringBuilder",
                      String.valueOf(indent));

    StringBuilder indentBuf = new StringBuilder(indent);
    for (int i=0 ; i < indent; i++)
    {
      indentBuf.append(' ');
    }

    buffer.append(indentBuf);
    buffer.append("Modify Request");
    buffer.append(EOL);

    buffer.append(indentBuf);
    buffer.append("  DN:  ");
    dn.toString(buffer);
    buffer.append(EOL);

    buffer.append("  Modifications:");
    buffer.append(EOL);

    for (LDAPModification mod : modifications)
    {
      mod.toString(buffer, indent+4);
    }
  }
}

