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
 * by brackets "[]" replaced with your own identifying information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Copyright 2009 Sun Microsystems, Inc.
 */

package org.opends.server.core.operations;



import org.opends.server.api.AttributeSyntax;
import org.opends.server.api.MatchingRule;
import org.opends.server.types.AttributeType;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.DITContentRule;
import org.opends.server.types.DITStructureRule;
import org.opends.server.types.DN;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.MatchingRuleUse;
import org.opends.server.types.NameForm;
import org.opends.server.types.ObjectClass;
import org.opends.server.types.RDN;



/**
 *
 */
public interface Schema
{
  DN decodeDN(ByteSequence dn) throws DirectoryException;



  DN decodeDN(String dn) throws DirectoryException;



  RDN decodeRDN(ByteSequence rdn) throws DirectoryException;



  RDN decodeRDN(String rdn) throws DirectoryException;



  /**
   * Retrieves the attribute type definition with the specified name or
   * OID.
   * 
   * @param lowerName
   *          The name or OID of the attribute type to retrieve,
   *          formatted in all lowercase characters.
   * @return The requested attribute type, or <CODE>null</CODE> if no
   *         type is registered with the provided name or OID.
   */
  AttributeType getAttributeType(String lowerName);



  /**
   * Retrieves the DIT content rule definition for the specified
   * objectclass.
   * 
   * @param objectClass
   *          The objectclass for the DIT content rule to retrieve.
   * @return The requested DIT content rule, or <CODE>null</CODE> if no
   *         DIT content rule is registered with the provided
   *         objectclass.
   */
  DITContentRule getDITContentRule(ObjectClass objectClass);



  /**
   * Retrieves the DIT structure rule definition with the provided rule
   * ID.
   * 
   * @param ruleID
   *          The rule ID for the DIT structure rule to retrieve.
   * @return The requested DIT structure rule, or <CODE>null</CODE> if
   *         no DIT structure rule is registered with the provided rule
   *         ID.
   */
  DITStructureRule getDITStructureRule(int ruleID);



  /**
   * Retrieves the DIT structure rule definition for the provided name
   * form.
   * 
   * @param nameForm
   *          The name form for the DIT structure rule to retrieve.
   * @return The requested DIT structure rule, or <CODE>null</CODE> if
   *         no DIT structure rule is registered with the provided name
   *         form.
   */
  DITStructureRule getDITStructureRule(NameForm nameForm);



  /**
   * Retrieves the matching rule definition with the specified name or
   * OID.
   * 
   * @param lowerName
   *          The name or OID of the matching rule to retrieve,
   *          formatted in all lowercase characters.
   * @return The requested matching rule, or <CODE>null</CODE> if no
   *         rule is registered with the provided name or OID.
   */
  MatchingRule getMatchingRule(String lowerName);



  /**
   * Retrieves the matching rule use definition for the specified
   * matching rule.
   * 
   * @param matchingRule
   *          The matching rule for which to retrieve the matching rule
   *          use definition.
   * @return The matching rule use definition, or <CODE>null</CODE> if
   *         none exists for the specified matching rule.
   */
  MatchingRuleUse getMatchingRuleUse(MatchingRule matchingRule);



  /**
   * Retrieves the name form definition for the specified objectclass.
   * 
   * @param objectClass
   *          The objectclass for the name form to retrieve.
   * @return The requested name form, or <CODE>null</CODE> if no name
   *         form is registered with the provided objectClass.
   */
  NameForm getNameForm(ObjectClass objectClass);



  /**
   * Retrieves the name form definition with the provided name or OID.
   * 
   * @param lowerName
   *          The name or OID of the name form to retrieve, formatted in
   *          all lowercase characters.
   * @return The requested name form, or <CODE>null</CODE> if no name
   *         form is registered with the provided name or OID.
   */
  NameForm getNameForm(String lowerName);



  /**
   * Retrieves the objectclass definition with the specified name or
   * OID.
   * 
   * @param lowerName
   *          The name or OID of the objectclass to retrieve, formatted
   *          in all lowercase characters.
   * @return The requested objectclass, or <CODE>null</CODE> if no class
   *         is registered with the provided name or OID.
   */
  ObjectClass getObjectClass(String lowerName);



  /**
   * Retrieves the attribute syntax definition with the OID.
   * 
   * @param lowerName
   *          The OID of the attribute syntax to retrieve, formatted in
   *          all lowercase characters.
   * @return The requested attribute syntax, or <CODE>null</CODE> if no
   *         syntax is registered with the provided OID.
   */
  AttributeSyntax getSyntax(String lowerName);
}
