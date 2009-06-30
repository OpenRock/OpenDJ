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

package org.opends.common.api;



import static org.opends.server.util.StaticUtils.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opends.server.types.ByteString;
import org.opends.server.util.Validator;



/**
 *
 */
public final class Filter
{
  private static final class AndImpl extends Impl
  {
    private final List<Filter> subFilters;



    public AndImpl(List<Filter> subFilters)
    {
      this.subFilters = subFilters;
    }



    @Override
    public <R, P> R accept(FilterVisitor<R, P> v, P p)
    {
      return v.visitAnd(p, subFilters);
    }



    @Override
    public StringBuilder toString(StringBuilder builder)
    {
      builder.append("(&");
      for (Filter subFilter : subFilters)
      {
        subFilter.toString(builder);
      }
      builder.append(')');
      return builder;
    }
  }

  private static final class ApproxMatchImpl extends Impl
  {

    private final ByteString assertionValue;
    private final String attributeDescription;



    public ApproxMatchImpl(String attributeDescription,
        ByteString assertionValue)
    {
      this.attributeDescription = attributeDescription;
      this.assertionValue = assertionValue;
    }



    @Override
    public <R, P> R accept(FilterVisitor<R, P> v, P p)
    {
      return v
          .visitApproxMatch(p, attributeDescription, assertionValue);
    }



    @Override
    public StringBuilder toString(StringBuilder builder)
    {
      builder.append('(');
      builder.append(attributeDescription);
      builder.append("~=");
      valueToFilterString(builder, assertionValue);
      builder.append(')');
      return builder;
    }

  }

  private static final class EqualityMatchImpl extends Impl
  {

    private final ByteString assertionValue;
    private final String attributeDescription;



    public EqualityMatchImpl(String attributeDescription,
        ByteString assertionValue)
    {
      this.attributeDescription = attributeDescription;
      this.assertionValue = assertionValue;
    }



    @Override
    public <R, P> R accept(FilterVisitor<R, P> v, P p)
    {
      return v.visitEqualityMatch(p, attributeDescription,
          assertionValue);
    }



    @Override
    public StringBuilder toString(StringBuilder builder)
    {
      builder.append('(');
      builder.append(attributeDescription);
      builder.append('=');
      valueToFilterString(builder, assertionValue);
      builder.append(')');
      return builder;
    }

  }

  private static final class ExtensibleMatchImpl extends Impl
  {
    private final String attributeDescription;
    private final boolean dnAttributes;
    private final String matchingRule;
    private final ByteString matchValue;



    public ExtensibleMatchImpl(String matchingRule,
        String attributeDescription, ByteString matchValue,
        boolean dnAttributes)
    {
      this.matchingRule = matchingRule;
      this.attributeDescription = attributeDescription;
      this.matchValue = matchValue;
      this.dnAttributes = dnAttributes;
    }



    @Override
    public <R, P> R accept(FilterVisitor<R, P> v, P p)
    {
      return v.visitExtensibleMatch(p, matchingRule,
          attributeDescription, matchValue, dnAttributes);
    }



    @Override
    public StringBuilder toString(StringBuilder builder)
    {
      builder.append('(');

      if (attributeDescription != null)
      {
        builder.append(attributeDescription);
      }

      if (dnAttributes)
      {
        builder.append(":dn");
      }

      if (matchingRule != null)
      {
        builder.append(':');
        builder.append(matchingRule);
      }

      builder.append(":=");
      valueToFilterString(builder, matchValue);
      builder.append(')');
      return builder;
    }

  }

  private static final class GreaterOrEqualImpl extends Impl
  {

    private final ByteString assertionValue;
    private final String attributeDescription;



    public GreaterOrEqualImpl(String attributeDescription,
        ByteString assertionValue)
    {
      this.attributeDescription = attributeDescription;
      this.assertionValue = assertionValue;
    }



    @Override
    public <R, P> R accept(FilterVisitor<R, P> v, P p)
    {
      return v.visitGreaterOrEqual(p, attributeDescription,
          assertionValue);
    }



    @Override
    public StringBuilder toString(StringBuilder builder)
    {
      builder.append('(');
      builder.append(attributeDescription);
      builder.append(">=");
      valueToFilterString(builder, assertionValue);
      builder.append(')');
      return builder;
    }

  }

  private static abstract class Impl
  {
    protected Impl()
    {
      // Nothing to do.
    }



    public abstract <R, P> R accept(FilterVisitor<R, P> v, P p);



    public abstract StringBuilder toString(StringBuilder builder);
  }

  private static final class LessOrEqualImpl extends Impl
  {

    private final ByteString assertionValue;
    private final String attributeDescription;



    public LessOrEqualImpl(String attributeDescription,
        ByteString assertionValue)
    {
      this.attributeDescription = attributeDescription;
      this.assertionValue = assertionValue;
    }



    @Override
    public <R, P> R accept(FilterVisitor<R, P> v, P p)
    {
      return v
          .visitLessOrEqual(p, attributeDescription, assertionValue);
    }



    @Override
    public StringBuilder toString(StringBuilder builder)
    {
      builder.append('(');
      builder.append(attributeDescription);
      builder.append("<=");
      valueToFilterString(builder, assertionValue);
      builder.append(')');
      return builder;
    }

  }

  private static final class NotImpl extends Impl
  {
    private final Filter subFilter;



    public NotImpl(Filter subFilter)
    {
      this.subFilter = subFilter;
    }



    @Override
    public <R, P> R accept(FilterVisitor<R, P> v, P p)
    {
      return v.visitNot(p, subFilter);
    }



    @Override
    public StringBuilder toString(StringBuilder builder)
    {
      builder.append("(!");
      subFilter.toString(builder);
      builder.append(')');
      return builder;
    }
  }

  private static final class OrImpl extends Impl
  {
    private final List<Filter> subFilters;



    public OrImpl(List<Filter> subFilters)
    {
      this.subFilters = subFilters;
    }



    @Override
    public <R, P> R accept(FilterVisitor<R, P> v, P p)
    {
      return v.visitOr(p, subFilters);
    }



    @Override
    public StringBuilder toString(StringBuilder builder)
    {
      builder.append("(|");
      for (Filter subFilter : subFilters)
      {
        subFilter.toString(builder);
      }
      builder.append(')');
      return builder;
    }
  }

  private static final class PresentImpl extends Impl
  {

    private final String attributeDescription;



    public PresentImpl(String attributeDescription)
    {
      this.attributeDescription = attributeDescription;
    }



    @Override
    public <R, P> R accept(FilterVisitor<R, P> v, P p)
    {
      return v.visitPresent(p, attributeDescription);
    }



    @Override
    public StringBuilder toString(StringBuilder builder)
    {
      builder.append('(');
      builder.append(attributeDescription);
      builder.append("=*)");
      return builder;
    }

  }

  private static final class SubstringsImpl extends Impl
  {

    private final List<ByteString> anyStrings;
    private final String attributeDescription;
    private final ByteString finalString;
    private final ByteString initialString;



    public SubstringsImpl(String attributeDescription,
        ByteString initialString, List<ByteString> anyStrings,
        ByteString finalString)
    {
      this.attributeDescription = attributeDescription;
      this.initialString = initialString;
      this.anyStrings = anyStrings;
      this.finalString = finalString;

    }



    @Override
    public <R, P> R accept(FilterVisitor<R, P> v, P p)
    {
      return v.visitSubstrings(p, attributeDescription, initialString,
          anyStrings, finalString);
    }



    @Override
    public StringBuilder toString(StringBuilder builder)
    {
      builder.append('(');
      builder.append(attributeDescription);
      builder.append("=");
      if (initialString != null)
      {
        valueToFilterString(builder, initialString);
      }
      for (ByteString anyString : anyStrings)
      {
        builder.append('*');
        valueToFilterString(builder, anyString);
      }
      builder.append('*');
      if (finalString != null)
      {
        valueToFilterString(builder, finalString);
      }
      builder.append(')');
      return builder;
    }

  }



  // RFC 4526 - FALSE filter.
  private static final Filter FALSE =
      new Filter(new OrImpl(Collections.<Filter> emptyList()));

  // Heavily used (objectClass=*) filter.
  private static final Filter OBJECT_CLASS_PRESENT =
      new Filter(new PresentImpl("objectClass"));

  // RFC 4526 - TRUE filter.
  private static final Filter TRUE =
      new Filter(new AndImpl(Collections.<Filter> emptyList()));



  public static Filter alwaysFalse()
  {
    return FALSE;
  }



  public static Filter alwaysTrue()
  {
    return TRUE;
  }



  public static Filter and(Filter... subFilters)
  {
    if (subFilters == null || subFilters.length == 0)
    {
      // RFC 4526 - TRUE filter.
      return alwaysTrue();
    }

    List<Filter> subFiltersList =
        new ArrayList<Filter>(subFilters.length);
    for (Filter subFilter : subFilters)
    {
      Validator.ensureNotNull(subFilter);
      subFiltersList.add(subFilter);
    }

    return new Filter(new AndImpl(Collections
        .unmodifiableList(subFiltersList)));
  }



  public static Filter approxMatch(
      AttributeDescription attributeDescription,
      ByteString assertionValue)
  {
    return approxMatch(attributeDescription.toString(), assertionValue);
  }



  public static Filter approxMatch(String attributeDescription,
      ByteString assertionValue)
  {
    Validator.ensureNotNull(attributeDescription);
    Validator.ensureNotNull(assertionValue);
    return new Filter(new ApproxMatchImpl(attributeDescription,
        assertionValue));
  }



  public static Filter equalityMatch(
      AttributeDescription attributeDescription,
      ByteString assertionValue)
  {
    return equalityMatch(attributeDescription.toString(),
        assertionValue);
  }



  public static Filter equalityMatch(String attributeDescription,
      ByteString assertionValue)
  {
    Validator.ensureNotNull(attributeDescription);
    Validator.ensureNotNull(assertionValue);
    return new Filter(new EqualityMatchImpl(attributeDescription,
        assertionValue));
  }



  public static Filter extensibleMatch(String matchingRule,
      AttributeDescription attributeDescription, ByteString matchValue,
      boolean dnAttributes)
  {
    return extensibleMatch(matchingRule, attributeDescription,
        matchValue, dnAttributes);
  }



  public static Filter extensibleMatch(String matchingRule,
      String attributeDescription, ByteString matchValue,
      boolean dnAttributes)
  {
    Validator.ensureTrue(matchingRule != null
        || attributeDescription != null);
    Validator.ensureNotNull(matchValue);
    return new Filter(new ExtensibleMatchImpl(matchingRule,
        attributeDescription, matchValue, dnAttributes));
  }



  public static Filter greaterOrEqual(
      AttributeDescription attributeDescription,
      ByteString assertionValue)
  {
    return greaterOrEqual(attributeDescription.toString(),
        assertionValue);
  }



  public static Filter greaterOrEqual(String attributeDescription,
      ByteString assertionValue)
  {
    Validator.ensureNotNull(attributeDescription);
    Validator.ensureNotNull(assertionValue);
    return new Filter(new GreaterOrEqualImpl(attributeDescription,
        assertionValue));
  }



  public static Filter lessOrEqual(
      AttributeDescription attributeDescription,
      ByteString assertionValue)
  {
    return lessOrEqual(attributeDescription.toString(), assertionValue);
  }



  public static Filter lessOrEqual(String attributeDescription,
      ByteString assertionValue)
  {
    Validator.ensureNotNull(attributeDescription);
    Validator.ensureNotNull(assertionValue);
    return new Filter(new LessOrEqualImpl(attributeDescription,
        assertionValue));
  }



  public static Filter not(Filter subFilter)
  {
    Validator.ensureNotNull(subFilter);
    return new Filter(new NotImpl(subFilter));
  }



  public static Filter objectClassPresent()
  {
    return OBJECT_CLASS_PRESENT;
  }



  public static Filter or(Filter... subFilters)
  {
    if (subFilters == null || subFilters.length == 0)
    {
      // RFC 4526 - FALSE filter.
      return alwaysFalse();
    }

    List<Filter> subFiltersList =
        new ArrayList<Filter>(subFilters.length);
    for (Filter subFilter : subFilters)
    {
      Validator.ensureNotNull(subFilter);
      subFiltersList.add(subFilter);
    }

    return new Filter(new OrImpl(Collections
        .unmodifiableList(subFiltersList)));
  }



  public static Filter present(
      AttributeDescription attributeDescription)
  {
    return present(attributeDescription.toString());
  }



  public static Filter present(String attributeDescription)
  {
    Validator.ensureNotNull(attributeDescription);

    if (toLowerCase(attributeDescription).equals("objectclass"))
    {
      return OBJECT_CLASS_PRESENT;
    }

    return new Filter(new PresentImpl(attributeDescription));
  }



  public static Filter substrings(
      AttributeDescription attributeDescription,
      ByteString initialString, ByteString finalString,
      ByteString... anyStrings)
  {
    return substrings(attributeDescription.toString(), initialString,
        finalString, anyStrings);
  }



  public static Filter substrings(String attributeDescription,
      ByteString initialString, ByteString finalString,
      ByteString... anyStrings)
  {
    Validator.ensureNotNull(attributeDescription);

    List<ByteString> anyStringList;
    if (anyStrings == null || anyStrings.length == 0)
    {
      anyStringList = Collections.emptyList();
    }
    else if (anyStrings.length == 1)
    {
      Validator.ensureNotNull(anyStrings[0]);
      anyStringList = Collections.singletonList(anyStrings[0]);
    }
    else
    {
      anyStringList = new ArrayList<ByteString>(anyStrings.length);
      for (ByteString anyString : anyStrings)
      {
        Validator.ensureNotNull(anyString);
        anyStringList.add(anyString);
      }
      anyStringList = Collections.unmodifiableList(anyStringList);
    }

    return new Filter(new SubstringsImpl(attributeDescription,
        initialString, anyStringList, finalString));
  }



  public static Filter valueOf(String string)
      throws IllegalFilterException
  {
    // TODO
    Validator.ensureNotNull(string);
    return null;
  }



  /**
   * Appends a properly-cleaned version of the provided value to the
   * given builder so that it can be safely used in string
   * representations of this search filter. The formatting changes that
   * may be performed will be in compliance with the specification in
   * RFC 2254.
   *
   * @param builder
   *          The builder to which the "safe" version of the value will
   *          be appended.
   * @param value
   *          The value to be appended to the builder.
   */
  private static void valueToFilterString(StringBuilder builder,
      ByteString value)
  {
    // Get the binary representation of the value and iterate through
    // it to see if there are any unsafe characters. If there are,
    // then escape them and replace them with a two-digit hex
    // equivalent.
    builder.ensureCapacity(builder.length() + value.length());
    for (int i = 0; i < value.length(); i++)
    {
      // FIXME: this is a bit overkill - it will escape all non-ascii
      // chars!
      byte b = value.byteAt(i);
      if (((b & 0x7F) != b) || // Not 7-bit clean
          (b <= 0x1F) || // Below the printable character range
          (b == 0x28) || // Open parenthesis
          (b == 0x29) || // Close parenthesis
          (b == 0x2A) || // Asterisk
          (b == 0x5C) || // Backslash
          (b == 0x7F)) // Delete character
      {
        builder.append('\\');
        builder.append(byteToHex(b));
      }
      else
      {
        builder.append((char) b);
      }
    }
  }



  private final Impl pimpl;



  private Filter(Impl pimpl)
  {
    this.pimpl = pimpl;
  }



  /**
   * Apply a visitor to this raw filter.
   *
   * @param <R>
   *          The return type of the visitor's methods.
   * @param <P>
   *          The type of the additional parameters to the visitor's
   *          methods.
   * @param v
   *          The raw filter visitor.
   * @param p
   *          Optional additional visitor parameter.
   * @return Returns a result as specified by the visitor.
   */
  public <R, P> R accept(FilterVisitor<R, P> v, P p)
  {
    return pimpl.accept(v, p);
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public final String toString()
  {
    StringBuilder builder = new StringBuilder();
    return toString(builder).toString();
  }



  public StringBuilder toString(StringBuilder builder)
  {
    return pimpl.toString(builder);
  }
}
