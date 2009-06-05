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



import java.util.Collections;
import java.util.List;

import org.opends.server.api.AttributeSyntax;
import org.opends.server.api.MatchingRule;
import org.opends.server.core.DirectoryServer;
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
 * An interface for querying a directory server schema.
 */
public abstract class Schema
{
  // Default schema.
  private static final Schema DEFAULT_SCHEMA = new Schema()
  {

    /**
     * {@inheritDoc}
     */
    @Override
    public DN decodeDN(ByteSequence dn) throws DirectoryException
    {
      return DN.decode(dn);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public DN decodeDN(String dn) throws DirectoryException
    {
      return DN.decode(dn);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public RDN decodeRDN(ByteSequence rdn) throws DirectoryException
    {
      return decodeRDN(rdn.toString());
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public RDN decodeRDN(String rdn) throws DirectoryException
    {
      return RDN.decode(rdn);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public AttributeSyntax getAttributeSyntax(String oid)
    {
      return DirectoryServer.getAttributeSyntax(oid, true);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public AttributeType getAttributeType(String lowerName)
    {
      return DirectoryServer.getAttributeType(lowerName, true);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public DITContentRule getDITContentRule(ObjectClass objectClass)
    {
      return DirectoryServer.getDITContentRule(objectClass);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public DITStructureRule getDITStructureRule(int ruleID)
    {
      return DirectoryServer.getDITStructureRule(ruleID);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public DITStructureRule getDITStructureRule(NameForm nameForm)
    {
      return DirectoryServer.getDITStructureRule(nameForm);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public MatchingRule getMatchingRule(String lowerName)
    {
      return DirectoryServer.getMatchingRule(lowerName);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public MatchingRuleUse getMatchingRuleUse(MatchingRule matchingRule)
    {
      return DirectoryServer.getMatchingRuleUse(matchingRule);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public List<NameForm> getNameFormsForObjectClass(
        ObjectClass objectClass)
    {
      List<NameForm> nameForms =
          DirectoryServer.getNameForm(objectClass);
      if (nameForms == null)
      {
        return Collections.emptyList();
      }
      else
      {
        return Collections.unmodifiableList(nameForms);
      }
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public NameForm getNameForm(String lowerName)
    {
      return DirectoryServer.getNameForm(lowerName);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectClass getObjectClass(String lowerName)
    {
      return DirectoryServer.getObjectClass(lowerName, true);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStrict()
    {
      return false;
    }

  };

  // Strict default schema.
  private static final Schema STRICT_DEFAULT_SCHEMA = new Schema()
  {

    /**
     * {@inheritDoc}
     */
    @Override
    public DN decodeDN(ByteSequence dn) throws DirectoryException
    {
      return DN.decode(dn);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public DN decodeDN(String dn) throws DirectoryException
    {
      return DN.decode(dn);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public RDN decodeRDN(ByteSequence rdn) throws DirectoryException
    {
      return decodeRDN(rdn.toString());
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public RDN decodeRDN(String rdn) throws DirectoryException
    {
      return RDN.decode(rdn);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public AttributeSyntax getAttributeSyntax(String oid)
    {
      return DirectoryServer.getAttributeSyntax(oid, false);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public AttributeType getAttributeType(String lowerName)
    {
      return DirectoryServer.getAttributeType(lowerName);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public DITContentRule getDITContentRule(ObjectClass objectClass)
    {
      return DirectoryServer.getDITContentRule(objectClass);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public DITStructureRule getDITStructureRule(int ruleID)
    {
      return DirectoryServer.getDITStructureRule(ruleID);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public DITStructureRule getDITStructureRule(NameForm nameForm)
    {
      return DirectoryServer.getDITStructureRule(nameForm);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public MatchingRule getMatchingRule(String lowerName)
    {
      return DirectoryServer.getMatchingRule(lowerName);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public MatchingRuleUse getMatchingRuleUse(MatchingRule matchingRule)
    {
      return DirectoryServer.getMatchingRuleUse(matchingRule);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public List<NameForm> getNameFormsForObjectClass(
        ObjectClass objectClass)
    {
      List<NameForm> nameForms =
          DirectoryServer.getNameForm(objectClass);
      if (nameForms == null)
      {
        return Collections.emptyList();
      }
      else
      {
        return Collections.unmodifiableList(nameForms);
      }
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public NameForm getNameForm(String lowerName)
    {
      return DirectoryServer.getNameForm(lowerName);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectClass getObjectClass(String lowerName)
    {
      return DirectoryServer.getObjectClass(lowerName);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStrict()
    {
      return false;
    }

  };



  /**
   * Returns the default schema which will create new object classes,
   * attribute types and syntaxes on demand.
   *
   * @return The default schema.
   */
  static Schema getDefaultSchema()
  {
    return DEFAULT_SCHEMA;
  }



  /**
   * Returns the default schema which will not create new object
   * classes, attribute types and syntaxes on demand.
   *
   * @return The strict default schema.
   */
  static Schema getStrictDefaultSchema()
  {
    return STRICT_DEFAULT_SCHEMA;
  }



  /**
   * Creates a new schema.
   */
  protected Schema()
  {
    // No implementation required.
  }



  /**
   * Decodes the provided byte sequence as a DN using this schema.
   *
   * @param dn
   *          The byte sequence containing the UTF-8 string
   *          representation of the DN to be decoded.
   * @return The decoded DN.
   * @throws DirectoryException
   *           If a problem occurs while trying to decode the provided
   *           byte sequence as a DN.
   */
  public abstract DN decodeDN(ByteSequence dn)
      throws DirectoryException;



  /**
   * Decodes the provided string as a DN using this schema.
   *
   * @param dn
   *          The string representation of the DN to be decoded.
   * @return The decoded DN.
   * @throws DirectoryException
   *           If a problem occurs while trying to decode the provided
   *           byte sequence as a DN.
   */
  public abstract DN decodeDN(String dn) throws DirectoryException;



  /**
   * Decodes the provided byte sequence as an RDN using this schema.
   *
   * @param rdn
   *          The byte sequence containing the UTF-8 string
   *          representation of the RDN to be decoded.
   * @return The decoded RDN.
   * @throws DirectoryException
   *           If a problem occurs while trying to decode the provided
   *           byte sequence as an RDN.
   */
  public abstract RDN decodeRDN(ByteSequence rdn)
      throws DirectoryException;



  /**
   * Decodes the provided string as an RDN using this schema.
   *
   * @param rdn
   *          The string representation of the RDN to be decoded.
   * @return The decoded RDN.
   * @throws DirectoryException
   *           If a problem occurs while trying to decode the provided
   *           byte sequence as an RDN.
   */
  public abstract RDN decodeRDN(String rdn) throws DirectoryException;



  /**
   * Retrieves the attribute syntax definition with the OID.
   *
   * @param oid
   *          The OID of the attribute syntax to retrieve, formatted in
   *          all lower-case characters.
   * @return The requested attribute syntax, or {@code null} if no
   *         syntax is registered with the provided OID.
   */
  public abstract AttributeSyntax getAttributeSyntax(String oid);



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
   * @param objectClass
   *          The object class.
   * @return The requested DIT content rule, or {@code null} if no DIT
   *         content rule is registered with the provided object class.
   */
  public abstract DITContentRule getDITContentRule(
      ObjectClass objectClass);



  /**
   * Retrieves the DIT structure rule definition with the provided rule
   * ID.
   *
   * @param ruleID
   *          The rule ID for the DIT structure rule to retrieve.
   * @return The requested DIT structure rule, or {@code null} if no DIT
   *         structure rule is registered with the provided rule ID.
   */
  public abstract DITStructureRule getDITStructureRule(int ruleID);



  /**
   * Retrieves the DIT structure rule definition for the provided name
   * form.
   *
   * @param nameForm
   *          The name form.
   * @return The requested DIT structure rule, or {@code null} if no DIT
   *         structure rule is registered with the provided name form.
   */
  public abstract DITStructureRule getDITStructureRule(NameForm nameForm);



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
  public abstract MatchingRule getMatchingRule(String lowerName);



  /**
   * Retrieves the matching rule use definition for the specified
   * matching rule.
   *
   * @param matchingRule
   *          The matching rule.
   * @return The matching rule use definition, or {@code null} if none
   *         exists for the specified matching rule.
   */
  public abstract MatchingRuleUse getMatchingRuleUse(
      MatchingRule matchingRule);



  /**
   * Retrieves the name forms defined for the specified object class.
   *
   * @param objectClass
   *          The object class.
   * @return An unmodifiable list containing the name forms associated
   *         with the object class. The list will be empty if no name
   *         forms are registered with the provided object class.
   */
  public abstract List<NameForm> getNameFormsForObjectClass(
      ObjectClass objectClass);



  /**
   * Retrieves the name form definition with the provided name or OID.
   *
   * @param lowerName
   *          The name or OID of the name form to retrieve, formatted in
   *          all lower-case characters.
   * @return The requested name form, or {@code null} if no name form is
   *         registered with the provided name or OID.
   */
  public abstract NameForm getNameForm(String lowerName);



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
  public abstract ObjectClass getObjectClass(String lowerName);



  /**
   * Indicates whether this schema is strict. A strict schema will not
   * create default object classes, attribute types, and syntaxes on
   * demand.
   *
   * @return {@code true} if this schema is strict.
   */
  public abstract boolean isStrict();
}
