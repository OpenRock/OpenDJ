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



import java.util.Collection;
import java.util.Iterator;

import org.opends.server.types.ByteString;
import org.opends.spi.AbstractMessage;
import org.opends.types.AttributeValueSequence;
import org.opends.types.NameAndAttributeSequence;
import org.opends.types.SearchScope;
import org.opends.types.filter.Filter;
import org.opends.util.Validator;



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
   * @param messageID
   *          The message ID of the request to be abandoned.
   * @return The new abandon request.
   */
  public static AbandonRequest newAbandonRequest(int messageID)
  {
    return new AbandonRequestImpl(messageID);
  }



  /**
   * Creates a new add request using the provided distinguished name.
   *
   * @param name
   *          The distinguished name of the entry to be added.
   * @return The new add request.
   * @throws NullPointerException
   *           If {@code name} was {@code null}.
   */
  public static AddRequest newAddRequest(String name)
      throws NullPointerException
  {
    return new AddRequestImpl(name);
  }



  /**
   * Creates a new add request using the provided distinguished name and
   * list of attributes.
   *
   * @param name
   *          The distinguished name of the entry to be added.
   * @param ldifAttributes
   *          Lines of LDIF containing the attributes of the entry to be
   *          added.
   * @return The new add request.
   * @throws IllegalArgumentException
   *           If {@code ldifAttributes} was empty or contained invalid
   *           LDIF.
   * @throws NullPointerException
   *           If {@code name} or {@code ldifAtttributes} was {@code
   *           null} .
   */
  public static AddRequest newAddRequest(String name,
      String... ldifAttributes) throws IllegalArgumentException,
      NullPointerException
  {
    AddRequest request = new AddRequestImpl(name);

    // FIXME: process LDIF.
    return request;
  }



  /**
   * NameAndAttributeSequence -> AddRequest adapter.
   */
  private static final class NameAndAttributeSequenceAddRequest extends
      AbstractMessage<AddRequest> implements AddRequest
  {
    private final NameAndAttributeSequence entry;



    private NameAndAttributeSequenceAddRequest(
        NameAndAttributeSequence entry)
    {
      this.entry = entry;
    }



    /**
     * {@inheritDoc}
     */
    public String toString()
    {
      StringBuilder builder = new StringBuilder();
      builder.append("AddRequest(name=");
      builder.append(getName());
      builder.append(", attributes=");
      builder.append(getAttributes());
      builder.append(", controls=");
      builder.append(getControls());
      builder.append(")");
      return builder.toString();
    }



    /**
     * {@inheritDoc}
     */
    public AddRequest addAttribute(AttributeValueSequence attribute)
        throws UnsupportedOperationException, IllegalArgumentException,
        NullPointerException
    {
      throw new UnsupportedOperationException();
    }



    /**
     * {@inheritDoc}
     */
    public AddRequest addAttribute(String attributeDescription,
        ByteString value) throws UnsupportedOperationException,
        NullPointerException
    {
      throw new UnsupportedOperationException();
    }



    /**
     * {@inheritDoc}
     */
    public AddRequest addAttribute(String attributeDescription,
        ByteString firstValue, ByteString... remainingValues)
        throws UnsupportedOperationException, NullPointerException
    {
      throw new UnsupportedOperationException();
    }



    /**
     * {@inheritDoc}
     */
    public AddRequest addAttribute(String attributeDescription,
        Collection<ByteString> values)
        throws UnsupportedOperationException, IllegalArgumentException,
        NullPointerException
    {
      throw new UnsupportedOperationException();
    }



    /**
     * {@inheritDoc}
     */
    public AddRequest addAttribute(String attributeDescription,
        String value) throws UnsupportedOperationException,
        NullPointerException
    {
      throw new UnsupportedOperationException();
    }



    /**
     * {@inheritDoc}
     */
    public AddRequest addAttribute(String attributeDescription,
        String firstValue, String... remainingValues)
        throws UnsupportedOperationException, NullPointerException
    {
      throw new UnsupportedOperationException();
    }



    /**
     * {@inheritDoc}
     */
    public AddRequest clearAttributes()
        throws UnsupportedOperationException
    {
      throw new UnsupportedOperationException();
    }



    /**
     * {@inheritDoc}
     */
    public AttributeValueSequence getAttribute(
        String attributeDescription) throws NullPointerException
    {
      return entry.getAttribute(attributeDescription);
    }



    /**
     * {@inheritDoc}
     */
    public int getAttributeCount()
    {
      return entry.getAttributeCount();
    }



    /**
     * {@inheritDoc}
     */
    public Iterable<AttributeValueSequence> getAttributes()
    {
      return new Iterable<AttributeValueSequence>()
      {

        public Iterator<AttributeValueSequence> iterator()
        {
          return new Iterator<AttributeValueSequence>()
          {

            public boolean hasNext()
            {
              return iterator.hasNext();
            }



            public AttributeValueSequence next()
            {
              // Ensure that the return sequence is unmodifiable.
              return Attributes.unmodifiable(iterator.next());
            }



            public void remove()
            {
              // Prevent modifications.
              throw new UnsupportedOperationException();
            }



            private final Iterator<AttributeValueSequence> iterator =
                entry.getAttributes().iterator();

          };
        }

      };

    }



    /**
     * {@inheritDoc}
     */
    public String getName()
    {
      return entry.getName();
    }



    /**
     * {@inheritDoc}
     */
    public boolean hasAttributes()
    {
      return entry.hasAttributes();
    }



    /**
     * {@inheritDoc}
     */
    public AttributeValueSequence removeAttribute(
        String attributeDescription)
        throws UnsupportedOperationException, NullPointerException
    {
      throw new UnsupportedOperationException();
    }



    /**
     * {@inheritDoc}
     */
    public AddRequest setName(String dn)
        throws UnsupportedOperationException, NullPointerException
    {
      throw new UnsupportedOperationException();
    }

  }



  /**
   * Returns an unmodifiable add request backed by the provided entry.
   * Modifications made to {@code entry} will be reflected in the
   * returned add request. The returned add request supports updates to
   * its list of controls, but any attempts to modify the name,
   * attributes, or their values will result in an {@code
   * UnsupportedOperationException} being thrown.
   * <p>
   * The method {@link #newAddRequest} provides a deep-copy version of
   * this method.
   *
   * @param entry
   *          The entry to be added.
   * @return The new add request.
   * @throws NullPointerException
   *           If {@code entry} was {@code null}.
   */
  public static AddRequest asAddRequest(NameAndAttributeSequence entry)
      throws NullPointerException
  {
    Validator.ensureNotNull(entry);

    return new NameAndAttributeSequenceAddRequest(entry);
  }



  /**
   * Creates a new add request using the provided entry. The content of
   * the provided entry will be copied into the new add request such
   * that modifications to the returned add request (including attribute
   * values) will not be reflected in the underlying entry.
   * <p>
   * The method {@link #asAddRequest} provides a shallow copy read-only
   * version of this method which should be used in cases where the
   * additional copying performance overhead is to be avoided.
   *
   * @param entry
   *          The entry to be added.
   * @return The new add request.
   * @throws NullPointerException
   *           If {@code entry} was {@code null} .
   */
  public static AddRequest newAddRequest(NameAndAttributeSequence entry)
      throws NullPointerException
  {
    AddRequest request = new AddRequestImpl(entry.getName());

    for (AttributeValueSequence attribute : entry.getAttributes())
    {
      request.addAttribute(Attributes.copyOf(attribute));
    }
    return request;
  }



  /**
   * Creates a new compare request using the provided distinguished
   * name, attribute name, and assertion value.
   *
   * @param name
   *          The distinguished name of the entry to be compared.
   * @param attributeDescription
   *          The name of the attribute to be compared.
   * @param assertionValue
   *          The assertion value to be compared.
   * @return The new compare request.
   * @throws NullPointerException
   *           If {@code name}, {@code attributeDescription}, or {@code
   *           assertionValue} was {@code null}.
   */
  public static CompareRequest newCompareRequest(String name,
      String attributeDescription, ByteString assertionValue)
      throws NullPointerException
  {
    return new CompareRequestImpl(name, attributeDescription,
        assertionValue);
  }



  /**
   * Creates a new compare request using the provided distinguished
   * name, attribute name, and assertion value.
   *
   * @param name
   *          The distinguished name of the entry to be compared.
   * @param attributeDescription
   *          The name of the attribute to be compared.
   * @param assertionValue
   *          The assertion value to be compared.
   * @return The new compare request.
   * @throws NullPointerException
   *           If {@code name}, {@code attributeDescription}, or {@code
   *           assertionValue} was {@code null}.
   */
  public static CompareRequest newCompareRequest(String name,
      String attributeDescription, String assertionValue)
      throws NullPointerException
  {
    return new CompareRequestImpl(name, attributeDescription,
        ByteString.valueOf(assertionValue));
  }



  /**
   * Creates a new delete request using the provided distinguished name.
   *
   * @param name
   *          The distinguished name of the entry to be deleted.
   * @return The new delete request.
   * @throws NullPointerException
   *           If {@code name} was {@code null}.
   */
  public static DeleteRequest newDeleteRequest(String name)
      throws NullPointerException
  {
    return new DeleteRequestImpl(name);
  }



  /**
   * Creates a new generic bind request using the provided distinguished
   * name, authentication type, and authentication information.
   *
   * @param name
   *          The distinguished name of the Directory object that the
   *          client wishes to bind as (may be empty).
   * @param authenticationType
   *          The authentication mechanism identifier for this generic
   *          bind request.
   * @param authenticationValue
   *          The authentication information for this generic bind
   *          request in a form defined by the authentication mechanism.
   * @return The new generic bind request.
   * @throws NullPointerException
   *           If {@code name}, {@code authenticationType}, or {@code
   *           authenticationValue} was {@code null}.
   */
  public static GenericBindRequest newGenericBindRequest(String name,
      byte authenticationType, ByteString authenticationValue)
      throws NullPointerException
  {
    return new GenericBindRequestImpl(name, authenticationType,
        authenticationValue);
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
   * Creates a new modify DN request using the provided distinguished
   * name and new RDN.
   *
   * @param name
   *          The distinguished name of the entry to be renamed.
   * @param newRDN
   *          The new RDN of the entry.
   * @return The new modify DN request.
   * @throws NullPointerException
   *           If {@code name} or {@code newRDN} was {@code null}.
   */
  public static ModifyDNRequest newModifyDNRequest(String name,
      String newRDN) throws NullPointerException
  {
    return new ModifyDNRequestImpl(name, newRDN);
  }



  /**
   * Creates a new modify request using the provided distinguished name.
   *
   * @param name
   *          The the distinguished name of the entry to be modified.
   * @return The new modify request.
   * @throws NullPointerException
   *           If {@code name} was {@code null}.
   */
  public static ModifyRequest newModifyRequest(String name)
      throws NullPointerException
  {
    return new ModifyRequestImpl(name);
  }



  /**
   * Creates a new modify request using the provided distinguished name.
   *
   * @param name
   *          The distinguished name of the entry to be modified.
   * @param ldifChanges
   *          Lines of LDIF containing the changes to be made to the
   *          entry.
   * @return The new modify request.
   * @throws IllegalArgumentException
   *           If {@code ldifChanges} was empty or contained invalid
   *           LDIF.
   * @throws NullPointerException
   *           If {@code name} or {@code ldifChanges} was {@code null}.
   */
  public static ModifyRequest newModifyRequest(String name,
      String... ldifChanges) throws IllegalArgumentException,
      NullPointerException
  {
    ModifyRequest request = new ModifyRequestImpl(name);

    // FIXME: process LDIF.
    return request;
  }



  /**
   * Creates a new search request using the provided distinguished name,
   * scope, and filter.
   *
   * @param name
   *          The distinguished name of the base entry relative to which
   *          the search is to be performed.
   * @param scope
   *          The scope of the search.
   * @param filter
   *          The filter that defines the conditions that must be
   *          fulfilled in order for an entry to be returned.
   * @return The new search request.
   * @throws NullPointerException
   *           If the {@code name}, {@code scope}, or {@code filter}
   *           were {@code null}.
   */
  public static SearchRequest newSearchRequest(String name,
      SearchScope scope, Filter filter) throws NullPointerException
  {
    return new SearchRequestImpl(name, scope, filter);
  }



  /**
   * Creates a new search request using the provided distinguished name,
   * scope, and filter.
   *
   * @param name
   *          The distinguished name of the base entry relative to which
   *          the search is to be performed.
   * @param scope
   *          The scope of the search.
   * @param filter
   *          The filter that defines the conditions that must be
   *          fulfilled in order for an entry to be returned.
   * @param attributeDescriptions
   *          The names of the attributes to be included with each
   *          entry.
   * @return The new search request.
   * @throws NullPointerException
   *           If the {@code name}, {@code scope}, or {@code filter}
   *           were {@code null}.
   */
  public static SearchRequest newSearchRequest(String name,
      SearchScope scope, Filter filter, String... attributeDescriptions)
      throws NullPointerException
  {
    return new SearchRequestImpl(name, scope, filter)
        .addAttribute(attributeDescriptions);
  }



  /**
   * Creates a new search request using the provided distinguished name,
   * scope, and filter.
   *
   * @param name
   *          The distinguished name of the base entry relative to which
   *          the search is to be performed.
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
   *           If the {@code name}, {@code scope}, or {@code filter}
   *           were {@code null}.
   */
  public static SearchRequest newSearchRequest(String name,
      SearchScope scope, String filter)
      throws IllegalArgumentException, NullPointerException
  {
    return new SearchRequestImpl(name, scope, Filter.valueOf(filter));
  }



  /**
   * Creates a new search request using the provided distinguished name,
   * scope, and filter.
   *
   * @param name
   *          The distinguished name of the base entry relative to which
   *          the search is to be performed.
   * @param scope
   *          The scope of the search.
   * @param filter
   *          The filter that defines the conditions that must be
   *          fulfilled in order for an entry to be returned.
   * @param attributeDescriptions
   *          The names of the attributes to be included with each
   *          entry.
   * @return The new search request.
   * @throws IllegalArgumentException
   *           If {@code filter} is not a valid LDAP string
   *           representation of a filter.
   * @throws NullPointerException
   *           If the {@code name}, {@code scope}, or {@code filter}
   *           were {@code null}.
   */
  public static SearchRequest newSearchRequest(String name,
      SearchScope scope, String filter, String... attributeDescriptions)
      throws IllegalArgumentException, NullPointerException
  {
    return new SearchRequestImpl(name, scope, Filter.valueOf(filter))
        .addAttribute(attributeDescriptions);
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
   *          The distinguished name of the Directory object that the
   *          client wishes to bind as.
   * @return The new simple bind request.
   * @throws NullPointerException
   *           If {@code name} was {@code null}.
   */
  public static SimpleBindRequest newSimpleBindRequest(String name)
      throws NullPointerException
  {
    return new SimpleBindRequestImpl().setName(name);
  }



  /**
   * Creates a new simple bind request having the provided name and
   * password suitable for name/password authentication.
   *
   * @param name
   *          The distinguished name of the Directory object that the
   *          client wishes to bind as, which may be empty..
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
    return new SimpleBindRequestImpl().setName(name).setPassword(
        password);
  }



  /**
   * Creates a new simple bind request having the provided name and
   * password suitable for name/password authentication.
   *
   * @param name
   *          The distinguished name of the Directory object that the
   *          client wishes to bind as, which may be empty.
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
    return new SimpleBindRequestImpl().setName(name).setPassword(
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
