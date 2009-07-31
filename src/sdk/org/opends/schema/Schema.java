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

package org.opends.schema;



import java.util.Collections;
import java.util.List;

import org.opends.server.core.DirectoryServer;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.DN;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.RDN;
import org.opends.schema.Syntax;
import org.opends.schema.syntaxes.SyntaxImplementation;
import org.opends.schema.matchingrules.MatchingRuleImplementation;


/**
 * An interface for querying a directory server schema.
 */
public interface Schema
{

  /**
   * Retrieves the attribute syntax definition with the OID.
   * 
   * @param oid
   *          The OID of the attribute syntax to retrieve, formatted in
   *          all lower-case characters.
   * @return The requested attribute syntax, or {@code null} if no
   *         syntax is registered with the provided OID.
   */
  public abstract SyntaxImplementation getSyntax(String oid);



  /**
   * Retrieves the attribute type definition with the specified name or
   * OID.
   * 
   * @param lowerName
   *          The name or OID of the attribute type to retrieve,
   *          formatted in all lower-case characters.
   * @return The requested attribute type, or {@code null} if no type is
   *         registered with the provided name or OID.
   */
  public abstract AttributeType getAttributeType(String lowerName);



  /**
   * Retrieves the DIT content rule definition for the specified object
   * class.
   * 
   * @param lowerName
   *          The object class OID or the name of the DIT content rule to
   *          retrieve.
   * @return The requested DIT content rule, or {@code null} if no DIT
   *         content rule is registered with the provided object class.
   */
  public abstract DITContentRule getDITContentRuleDefinition(
      String lowerName);



  /**
   * Retrieves the DIT structure rule definition with the provided rule
   * ID.
   * 
   * @param ruleID
   *          The rule ID for the DIT structure rule to retrieve.
   * @return The requested DIT structure rule, or {@code null} if no DIT
   *         structure rule is registered with the provided rule ID.
   */
  public abstract DITStructureRule getDITStructureRuleDefinition(int ruleID);



  /**
   * Retrieves the DIT structure rule definition for the provided name
   * form.
   * 
   * @param nameForm
   *          The name form.
   * @return The requested DIT structure rule, or {@code null} if no DIT
   *         structure rule is registered with the provided name form.
   */
  public abstract DITStructureRule getDITStructureRuleDefinition(NameForm nameForm);



  /**
   * Retrieves the matching rule definition with the specified name or
   * OID.
   * 
   * @param lowerName
   *          The name or OID of the matching rule to retrieve,
   *          formatted in all lower-case characters.
   * @return The requested matching rule, or {@code null} if no rule is
   *         registered with the provided name or OID.
   */
  public abstract MatchingRuleImplementation getMatchingRule(String lowerName);



  /**
   * Retrieves the matching rule use definition for the specified
   * matching rule.
   * 
   * @param lowerName
   *          The OID of the matching rule or name of the matching rule use
   *          to retrieve.
   * @return The matching rule use definition, or {@code null} if none
   *         exists for the specified matching rule.
   */
  public abstract MatchingRuleUse getMatchingRuleUseDefinition(String lowerName);



  /**
   * Retrieves the name form definition with the provided name or OID.
   * 
   * @param lowerName
   *          The name or OID of the name form to retrieve, formatted in
   *          all lower-case characters.
   * @return The requested name form, or {@code null} if no name form is
   *         registered with the provided name or OID.
   */
  public abstract NameForm getNameFormDefinition(String lowerName);


  /**
   * Retrieves the object class definition with the specified name or
   * OID.
   * 
   * @param lowerName
   *          The name or OID of the object class to retrieve, formatted
   *          in all lower-case characters.
   * @return The requested object class, or {@code null} if no object
   *         class is registered with the provided name or OID.
   */
  public abstract ObjectClass getObjectClassDefinition(String lowerName);

  public abstract Syntax getDefaultSyntax();



  /**
   * Indicates whether this schema is strict. A strict schema will not
   * create default object classes, attribute types, and syntaxes on
   * demand.
   * 
   * @return {@code true} if this schema is strict.
   */
  public abstract boolean isStrict();
}
