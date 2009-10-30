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

package org.opends.sdk.responses;



import java.util.Collection;

import org.opends.sdk.AttributeSequence;
import org.opends.sdk.AttributeValueSequence;
import org.opends.sdk.ResultCode;
import org.opends.sdk.util.Validator;



/**
 * This class contains various methods for creating and manipulating
 * responses.
 * <p>
 * TODO: search reference from LDAP URL.
 * <p>
 * TODO: referral from LDAP URL.
 * <p>
 * TODO: search entry from entry.
 * <p>
 * TODO: unmodifiable requests?
 * <p>
 * TODO: synchronized requests?
 * <p>
 * TODO: copy constructors.
 */
public final class Responses
{
  /**
   * NameAndAttributeSequence -> SearchResultEntry adapter.
   */
  private static final class SearchResultEntryAdapter extends
      AbstractMessage<SearchResultEntry> implements SearchResultEntry
  {
    private final AttributeSequence entry;



    private SearchResultEntryAdapter(AttributeSequence entry)
    {
      this.entry = entry;
    }



    /**
     * {@inheritDoc}
     */
    public SearchResultEntry addAttribute(
        AttributeValueSequence attribute)
        throws UnsupportedOperationException, IllegalArgumentException,
        NullPointerException
    {
      Validator.ensureNotNull(attribute);

      entry.addAttribute(attribute);
      return this;
    }



    public SearchResultEntry addAttribute(String attributeDescription)
        throws UnsupportedOperationException, NullPointerException
    {
      Validator.ensureNotNull(attributeDescription);

      return addAttribute(Attributes.create(attributeDescription));
    }



    /**
     * {@inheritDoc}
     */
    public SearchResultEntry addAttribute(String attributeDescription,
        Object value) throws UnsupportedOperationException,
        NullPointerException
    {
      Validator.ensureNotNull(attributeDescription, value);

      return addAttribute(Attributes
          .create(attributeDescription, value));
    }



    /**
     * {@inheritDoc}
     */
    public SearchResultEntry addAttribute(String attributeDescription,
        Object... values) throws UnsupportedOperationException,
        NullPointerException
    {
      Validator.ensureNotNull(attributeDescription, values);

      return addAttribute(Attributes.create(attributeDescription,
          values));
    }



    /**
     * {@inheritDoc}
     */
    public SearchResultEntry addAttribute(String attributeDescription,
        Collection<?> values) throws UnsupportedOperationException,
        IllegalArgumentException, NullPointerException
    {
      Validator.ensureNotNull(attributeDescription, values);

      return addAttribute(Attributes.create(attributeDescription,
          values));
    }



    /**
     * {@inheritDoc}
     */
    public SearchResultEntry clearAttributes()
        throws UnsupportedOperationException
    {
      entry.clearAttributes();
      return this;
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
    public Iterable<? extends AttributeValueSequence> getAttributes()
    {
      // Need adapter for type-safety.
      return entry.getAttributes();
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
    public SearchResultEntry removeAttribute(String attributeDescription)
        throws UnsupportedOperationException, NullPointerException
    {
      entry.removeAttribute(attributeDescription);
      return this;
    }



    /**
     * {@inheritDoc}
     */
    public SearchResultEntry setName(String dn)
        throws UnsupportedOperationException, NullPointerException
    {
      entry.setName(dn);
      return this;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
      StringBuilder builder = new StringBuilder();
      builder.append("SearchResultEntry(name=");
      builder.append(getName());
      builder.append(", attributes=");
      builder.append(getAttributes());
      builder.append(", controls=");
      builder.append(getControls());
      builder.append(")");
      return builder.toString();
    }

  }



  /**
   * Returns a new search result entry backed by the provided entry.
   * Modifications made to {@code entry} will be reflected in the search
   * result entry. The returned search result entry supports updates to
   * its list of controls, as well as updates to the name and attributes
   * if the underlying entry allows.
   * <p>
   * The method {@link #newSearchResultEntry} provides a deep-copy
   * version of this method.
   *
   * @param entry
   *          The entry to be returned in the search result entry.
   * @return The new search result entry.
   * @throws NullPointerException
   *           If {@code entry} was {@code null}.
   */
  public static SearchResultEntry asSearchResultEntry(
      AttributeSequence entry) throws NullPointerException
  {
    Validator.ensureNotNull(entry);

    return new SearchResultEntryAdapter(entry);
  }



  /**
   * Creates a new bind result using the provided result code.
   *
   * @param resultCode
   *          The result code.
   * @return The new bind result.
   * @throws NullPointerException
   *           If {@code resultCode} was {@code null}.
   */
  public static BindResult newBindResult(ResultCode resultCode)
      throws NullPointerException
  {
    return new BindResultImpl(resultCode);
  }



  /**
   * Creates a new compare result using the provided result code.
   *
   * @param resultCode
   *          The result code.
   * @return The new compare result.
   * @throws NullPointerException
   *           If {@code resultCode} was {@code null}.
   */
  public static CompareResult newCompareResult(ResultCode resultCode)
      throws NullPointerException
  {
    return new CompareResultImpl(resultCode);
  }



  /**
   * Creates a new generic extended result using the provided result
   * code.
   *
   * @param resultCode
   *          The result code.
   * @return The new generic extended result.
   * @throws NullPointerException
   *           If {@code resultCode} was {@code null}.
   */
  public static GenericExtendedResult newGenericExtendedResult(
      ResultCode resultCode) throws NullPointerException
  {
    return new GenericExtendedResultImpl(resultCode);
  }



  /**
   * Creates a new generic intermediate response.
   *
   * @return The new generic intermediate response.
   */
  public static GenericIntermediateResponse newGenericIntermediateResponse()
  {
    return new GenericIntermediateResponseImpl();
  }



  /**
   * Creates a new result using the provided result code.
   *
   * @param resultCode
   *          The result code.
   * @return The new result.
   * @throws NullPointerException
   *           If {@code resultCode} was {@code null}.
   */
  public static Result newResult(ResultCode resultCode)
      throws NullPointerException
  {
    return new ResultImpl<Result>(resultCode);
  }



  /**
   * Creates a new search result using the provided result code.
   *
   * @param resultCode
   *          The result code.
   * @return The new search result.
   * @throws NullPointerException
   *           If {@code resultCode} was {@code null}.
   */
  public static SearchResult newSearchResult(ResultCode resultCode)
      throws NullPointerException
  {
    return new SearchResultImpl(resultCode);
  }



  /**
   * Creates a new search result entry using the provided entry. The
   * content of the provided entry will be copied into the new search
   * result entry such that modifications to the returned search result
   * entry (including attribute values) will not be reflected in the
   * underlying entry.
   * <p>
   * The method {@link #asSearchResultEntry} provides a shallow copy
   * version of this method which should be used in cases where the
   * additional copying performance overhead is to be avoided.
   *
   * @param entry
   *          The entry to be returned in the search result entry.
   * @return The new search result entry.
   * @throws NullPointerException
   *           If {@code entry} was {@code null} .
   */
  public static SearchResultEntry newSearchResultEntry(
      AttributeSequence entry) throws NullPointerException
  {
    SearchResultEntry request =
        new SearchResultEntryImpl(entry.getName());

    for (AttributeValueSequence attribute : entry.getAttributes())
    {
      request.addAttribute(Attributes.copyOf(attribute));
    }
    return request;
  }



  /**
   * Creates a new search result entry using the provided distinguished.
   *
   * @param dn
   *          The distinguished of the search result entry.
   * @return The new search result entry.
   * @throws NullPointerException
   *           If {@code dn} was {@code null}.
   */
  public static SearchResultEntry newSearchResultEntry(String dn)
      throws NullPointerException
  {
    return new SearchResultEntryImpl(dn);
  }



  /**
   * Creates a new search result reference using the provided
   * continuation reference URI.
   *
   * @param uri
   *          The first continuation reference URI to be added to this
   *          search result reference.
   * @return The new search result reference.
   * @throws NullPointerException
   *           If {@code uri} was {@code null}.
   */
  public static SearchResultReference newSearchResultReference(
      String uri) throws NullPointerException
  {
    return new SearchResultReferenceImpl(uri);
  }



  // Private constructor.
  private Responses()
  {
    // Prevent instantiation.
  }
}
