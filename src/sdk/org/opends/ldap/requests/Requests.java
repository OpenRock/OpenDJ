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

package org.opends.ldap.requests;



import org.opends.server.types.ByteString;
import org.opends.types.SearchScope;
import org.opends.types.filter.Filter;



/**
 * This class contains various methods for creating and manipulating
 * requests.
 * <p>
 * TODO: search request from LDAP URL.
 * <p>
 * TODO: add request from entry.
 * <p>
 * TODO: add request from LDIF.
 * <p>
 * TODO: modify request from LDIF.
 * <p>
 * TODO: modify DN request from LDIF.
 * <p>
 * TODO: delete request from LDIF.
 * <p>
 * TODO: update request from LDIF.
 * <p>
 * TODO: update request from persistent search result.
 * <p>
 * TODO: unmodifiable requests?
 * <p>
 * TODO: synchronized requests?
 * <p>
 * TODO: copy constructors.
 */
public final class Requests
{

  /**
   * Creates a new abandon request using the provided message ID.
   *
   * @param id
   *          The message ID of the request to be abandoned.
   * @return The new abandon request.
   */
  public static AbandonRequest newAbandonRequest(int id)
  {
    return new AbandonRequestImpl(id);
  }



  /**
   * Creates a new add request using the provided DN.
   *
   * @param dn
   *          The name of the entry to be added.
   * @return The new add request.
   * @throws NullPointerException
   *           If {@code dn} was {@code null}.
   */
  public static AddRequest newAddRequest(String dn)
      throws NullPointerException
  {
    return new AddRequestImpl(dn);
  }



  /**
   * Creates a new add request using the provided DN and list of
   * attributes.
   *
   * @param dn
   *          The name of the entry to be added.
   * @param ldifAttributes
   *          Lines of LDIF containing the attributes of the entry to be
   *          added.
   * @return The new add request.
   * @throws IllegalArgumentException
   *           If {@code ldifAttributes} was empty or contained invalid
   *           LDIF.
   * @throws NullPointerException
   *           If {@code dn} or {@code ldifAtttributes} was {@code null}
   *           .
   */
  public static AddRequest newAddRequest(String dn,
      String... ldifAttributes) throws IllegalArgumentException,
      NullPointerException
  {
    AddRequest request = new AddRequestImpl(dn);

    // FIXME: process LDIF.
    return request;
  }



  /**
   * Creates a new compare request using the provided DN, attribute
   * name, and assertion value.
   *
   * @param dn
   *          The name of the entry to be compared.
   * @param attributeDescription
   *          The name of the attribute to be compared.
   * @param assertionValue
   *          The assertion value to be compared.
   * @return The new compare request.
   * @throws NullPointerException
   *           If {@code dn}, {@code attributeDescription}, or {@code
   *           assertionValue} was {@code null}.
   */
  public static CompareRequest newCompareRequest(String dn,
      String attributeDescription, ByteString assertionValue)
      throws NullPointerException
  {
    return new CompareRequestImpl(dn, attributeDescription,
        assertionValue);
  }



  /**
   * Creates a new compare request using the provided DN, attribute
   * name, and assertion value.
   *
   * @param dn
   *          The name of the entry to be compared.
   * @param attributeDescription
   *          The name of the attribute to be compared.
   * @param assertionValue
   *          The assertion value to be compared.
   * @return The new compare request.
   * @throws NullPointerException
   *           If {@code dn}, {@code attributeDescription}, or {@code
   *           assertionValue} was {@code null}.
   */
  public static CompareRequest newCompareRequest(String dn,
      String attributeDescription, String assertionValue)
      throws NullPointerException
  {
    return new CompareRequestImpl(dn, attributeDescription, ByteString
        .valueOf(assertionValue));
  }



  /**
   * Creates a new delete request using the provided DN.
   *
   * @param dn
   *          The name of the entry to be deleted.
   * @return The new delete request.
   * @throws NullPointerException
   *           If {@code dn} was {@code null}.
   */
  public static DeleteRequest newDeleteRequest(String dn)
      throws NullPointerException
  {
    return new DeleteRequestImpl(dn);
  }



  /**
   * Creates a new generic bind request using the provided bind DN,
   * authentication type, and authentication information.
   *
   * @param dn
   *          The name of the Directory object that the client wishes to
   *          bind as (may be empty).
   * @param type
   *          The authentication mechanism identifier for this generic
   *          bind request.
   * @param bytes
   *          The authentication information for this generic bind
   *          request in a form defined by the authentication mechanism.
   * @return The new generic bind request.
   * @throws NullPointerException
   *           If {@code dn}, {@code type}, or {@code bytes} was {@code
   *           null}.
   */
  public static GenericBindRequest newGenericBindRequest(String dn,
      byte type, ByteString bytes) throws NullPointerException
  {
    return new GenericBindRequestImpl(dn, type, bytes);
  }



  /**
   * Creates a new generic extended request using the provided name and
   * optional value.
   *
   * @param requestName
   *          The dotted-decimal representation of the unique OID
   *          corresponding to this extended request.
   * @param requestValue
   *          The content of this generic extended request in a form
   *          defined by the extended operation, or {@code null} if
   *          there is no content.
   * @return The new generic extended request.
   * @throws NullPointerException
   *           If {@code requestName} was {@code null}.
   */
  public static GenericExtendedRequest newGenericExtendedRequest(
      String requestName, ByteString requestValue)
      throws NullPointerException
  {
    return new GenericExtendedRequestImpl(requestName, requestValue);
  }



  /**
   * Creates a new modify DN request using the provided entry DN and new
   * RDN.
   *
   * @param dn
   *          The name of the entry to be renamed.
   * @param newRDN
   *          The new RDN of the entry.
   * @return The new modify DN request.
   * @throws NullPointerException
   *           If {@code dn} or {@code newRDN} was {@code null}.
   */
  public static ModifyDNRequest newModifyDNRequest(String dn,
      String newRDN) throws NullPointerException
  {
    return new ModifyDNRequestImpl(dn, newRDN);
  }



  /**
   * Creates a new modify request using the provided DN.
   *
   * @param dn
   *          The the name of the entry to be modified.
   * @return The new modify request.
   * @throws NullPointerException
   *           If {@code dn} was {@code null}.
   */
  public static ModifyRequest newModifyRequest(String dn)
      throws NullPointerException
  {
    return new ModifyRequestImpl(dn);
  }



  /**
   * Creates a new modify request using the provided DN.
   *
   * @param dn
   *          The name of the entry to be modified.
   * @param ldifChanges
   *          Lines of LDIF containing the changes to be made to the
   *          entry.
   * @return The new modify request.
   * @throws IllegalArgumentException
   *           If {@code ldifChanges} was empty or contained invalid
   *           LDIF.
   * @throws NullPointerException
   *           If {@code dn} or {@code ldifChanges} was {@code null}.
   */
  public static ModifyRequest newModifyRequest(String dn,
      String... ldifChanges) throws IllegalArgumentException,
      NullPointerException
  {
    ModifyRequest request = new ModifyRequestImpl(dn);

    // FIXME: process LDIF.
    return request;
  }



  /**
   * Creates a new search request using the provided base DN, scope, and
   * filter.
   *
   * @param baseDN
   *          The name of the base entry relative to which the search is
   *          to be performed.
   * @param scope
   *          The scope of the search.
   * @param filter
   *          The filter that defines the conditions that must be
   *          fulfilled in order for an entry to be returned.
   * @return The new search request.
   * @throws NullPointerException
   *           If the {@code baseDN}, {@code scope}, or {@code filter}
   *           were {@code null}.
   */
  public static SearchRequest newSearchRequest(String baseDN,
      SearchScope scope, Filter filter) throws NullPointerException
  {
    return new SearchRequestImpl(baseDN, scope, filter);
  }



  /**
   * Creates a new search request using the provided base DN, scope, and
   * filter.
   *
   * @param baseDN
   *          The name of the base entry relative to which the search is
   *          to be performed.
   * @param scope
   *          The scope of the search.
   * @param filter
   *          The filter that defines the conditions that must be
   *          fulfilled in order for an entry to be returned.
   * @param attributes
   *          The names of the attributes to be included with each
   *          entry.
   * @return The new search request.
   * @throws NullPointerException
   *           If the {@code baseDN}, {@code scope}, or {@code filter}
   *           were {@code null}.
   */
  public static SearchRequest newSearchRequest(String baseDN,
      SearchScope scope, Filter filter, String... attributes)
      throws NullPointerException
  {
    return new SearchRequestImpl(baseDN, scope, filter)
        .addAttribute(attributes);
  }



  /**
   * Creates a new search request using the provided base DN, scope, and
   * filter.
   *
   * @param baseDN
   *          The name of the base entry relative to which the search is
   *          to be performed.
   * @param scope
   *          The scope of the search.
   * @param filter
   *          The filter that defines the conditions that must be
   *          fulfilled in order for an entry to be returned.
   * @return The new search request.
   * @throws IllegalArgumentException
   *           If {@code filter} is not a valid LDAP string
   *           representation of a filter.
   * @throws NullPointerException
   *           If the {@code baseDN}, {@code scope}, or {@code filter}
   *           were {@code null}.
   */
  public static SearchRequest newSearchRequest(String baseDN,
      SearchScope scope, String filter)
      throws IllegalArgumentException, NullPointerException
  {
    return new SearchRequestImpl(baseDN, scope, Filter.valueOf(filter));
  }



  /**
   * Creates a new search request using the provided base DN, scope, and
   * filter.
   *
   * @param baseDN
   *          The name of the base entry relative to which the search is
   *          to be performed.
   * @param scope
   *          The scope of the search.
   * @param filter
   *          The filter that defines the conditions that must be
   *          fulfilled in order for an entry to be returned.
   * @param attributes
   *          The names of the attributes to be included with each
   *          entry.
   * @return The new search request.
   * @throws IllegalArgumentException
   *           If {@code filter} is not a valid LDAP string
   *           representation of a filter.
   * @throws NullPointerException
   *           If the {@code baseDN}, {@code scope}, or {@code filter}
   *           were {@code null}.
   */
  public static SearchRequest newSearchRequest(String baseDN,
      SearchScope scope, String filter, String... attributes)
      throws IllegalArgumentException, NullPointerException
  {
    return new SearchRequestImpl(baseDN, scope, Filter.valueOf(filter))
        .addAttribute(attributes);
  }



  /**
   * Creates a new simple bind request having an empty name and password
   * suitable for anonymous authentication.
   *
   * @return The new simple bind request.
   */
  public static SimpleBindRequest newSimpleBindRequest()
  {
    return new SimpleBindRequestImpl();
  }



  /**
   * Creates a new simple bind request having the provided name and an
   * empty password suitable for unauthenticated authentication.
   *
   * @param name
   *          The DN of the Directory object that the client wishes to
   *          bind as.
   * @return The new simple bind request.
   * @throws NullPointerException
   *           If {@code name} was {@code null}.
   */
  public static SimpleBindRequest newSimpleBindRequest(String name)
      throws NullPointerException
  {
    return new SimpleBindRequestImpl().setBindDN(name);
  }



  /**
   * Creates a new simple bind request having the provided name and
   * password suitable for name/password authentication.
   *
   * @param name
   *          The DN of the Directory object that the client wishes to
   *          bind as, which may be empty..
   * @param password
   *          The password of the Directory object that the client
   *          wishes to bind as, which may be empty.
   * @return The new simple bind request.
   * @throws NullPointerException
   *           If {@code name} or {@code password} was {@code null}.
   */
  public static SimpleBindRequest newSimpleBindRequest(String name,
      String password) throws NullPointerException
  {
    return new SimpleBindRequestImpl().setBindDN(name).setPassword(
        password);
  }



  /**
   * Creates a new simple bind request having the provided name and
   * password suitable for name/password authentication.
   *
   * @param name
   *          The DN of the Directory object that the client wishes to
   *          bind as, which may be empty..
   * @param password
   *          The password of the Directory object that the client
   *          wishes to bind as, which may be empty.
   * @return The new simple bind request.
   * @throws NullPointerException
   *           If {@code name} or {@code password} was {@code null}.
   */
  public static SimpleBindRequest newSimpleBindRequest(String name,
      ByteString password) throws NullPointerException
  {
    return new SimpleBindRequestImpl().setBindDN(name).setPassword(
        password);
  }



  /**
   * Creates a new unbind request.
   *
   * @return The new unbind request.
   */
  public static UnbindRequest newUnbindRequest()
  {
    return new UnbindRequestImpl();
  }



  // Private constructor.
  private Requests()
  {
    // Prevent instantiation.
  }
}
