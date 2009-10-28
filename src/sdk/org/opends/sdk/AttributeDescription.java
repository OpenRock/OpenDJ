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

package org.opends.sdk;



import static org.opends.messages.SchemaMessages.ERR_ATTRIBUTE_DESCRIPTION_TYPE_NOT_FOUND;
import static org.opends.sdk.util.StaticUtils.toLowerCase;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.opends.messages.Message;
import org.opends.sdk.schema.AttributeType;
import org.opends.sdk.schema.Schema;
import org.opends.sdk.schema.UnknownSchemaElementException;
import org.opends.sdk.util.Iterators;
import org.opends.sdk.util.LocalizedIllegalArgumentException;
import org.opends.sdk.util.Validator;



/**
 * An attribute description as defined in RFC 4512 section 2.5.
 * Attribute descriptions are used to identify an attribute in an entry
 * and are composed of an attribute type and a set of zero or more
 * attribute options.
 *
 * @see <a href="http://tools.ietf.org/html/rfc4512#section-2.5">RFC
 *      4512 - Lightweight Directory Access Protocol (LDAP): Directory
 *      Information Models </a>
 */
public final class AttributeDescription implements
    Comparable<AttributeDescription>
{
  // TODO: we could use a per thread/schema cache.

  private static abstract class Impl implements Iterable<String>
  {
    protected Impl()
    {
      // Nothing to do.
    }



    public abstract int compareTo(Impl other);



    public abstract boolean containsOption(String normalizedOption);



    public abstract boolean equals(Impl other);



    public abstract String firstNormalizedOption();



    @Override
    public abstract int hashCode();



    public abstract boolean hasOptions();



    public abstract boolean isSubTypeOf(Impl other);



    public abstract boolean isSuperTypeOf(Impl other);



    public abstract int size();

  }



  private static final class MultiOptionImpl extends Impl
  {

    private final String[] normalizedOptions;
    private final String[] options;



    private MultiOptionImpl(String[] options, String[] normalizedOptions)
    {
      this.options = options;
      this.normalizedOptions = normalizedOptions;
    }



    @Override
    public int compareTo(Impl other)
    {
      if (other == ZERO_OPTION_IMPL)
      {
        // If other has zero options then this sorts after.
        return 1;
      }
      else if (other.size() == 1)
      {
        return firstNormalizedOption().compareTo(
            other.firstNormalizedOption());
      }
      else
      {
        // Need to compare all options in order.
        MultiOptionImpl impl2 = (MultiOptionImpl) other;

        int i1 = 0;
        int i2 = 0;
        int sz1 = normalizedOptions.length;
        int sz2 = impl2.normalizedOptions.length;

        while ((i1 < sz1) && (i2 < sz2))
        {
          String o1 = normalizedOptions[i1++];
          String o2 = impl2.normalizedOptions[i2++];
          int result = o1.compareTo(o2);
          if (result != 0)
          {
            return result;
          }
        }

        if (i1 < sz1)
        {
          return 1;
        }
        else if (i2 < sz2)
        {
          return -1;
        }
        else
        {
          return 0;
        }
      }
    }



    @Override
    public boolean containsOption(String normalizedOption)
    {
      int sz = normalizedOptions.length;
      for (int i = 0; i < sz; i++)
      {
        if (normalizedOptions[i].equals(normalizedOption))
        {
          return true;
        }
      }
      return false;
    }



    @Override
    public boolean equals(Impl other)
    {
      if (other instanceof MultiOptionImpl)
      {
        MultiOptionImpl tmp = (MultiOptionImpl) other;
        return Arrays.equals(normalizedOptions, tmp.normalizedOptions);
      }
      else
      {
        return false;
      }
    }



    @Override
    public String firstNormalizedOption()
    {
      return normalizedOptions[0];
    }



    @Override
    public int hashCode()
    {
      return Arrays.hashCode(normalizedOptions);
    }



    @Override
    public boolean hasOptions()
    {
      return true;
    }



    @Override
    public boolean isSubTypeOf(Impl other)
    {
      // Must contain a super-set of other's options.
      if (other == ZERO_OPTION_IMPL)
      {
        return true;
      }
      else if (other.size() == 1)
      {
        return containsOption(other.firstNormalizedOption());
      }
      else if (other.size() > size())
      {
        return false;
      }
      else
      {
        // Check this contains other's options.
        //
        // This could be optimized more if required, but it's probably
        // not worth it.
        MultiOptionImpl tmp = (MultiOptionImpl) other;
        for (String normalizedOption : tmp.normalizedOptions)
        {
          if (!containsOption(normalizedOption))
          {
            return false;
          }
        }
        return true;
      }
    }



    @Override
    public boolean isSuperTypeOf(Impl other)
    {
      // Must contain a sub-set of other's options.
      for (String normalizedOption : normalizedOptions)
      {
        if (!other.containsOption(normalizedOption))
        {
          return false;
        }
      }
      return true;
    }



    public Iterator<String> iterator()
    {
      return Iterators.arrayIterator(options);
    }



    @Override
    public int size()
    {
      return normalizedOptions.length;
    }

  }



  private static final class SingleOptionImpl extends Impl
  {

    private final String normalizedOption;
    private final String option;



    private SingleOptionImpl(String option, String normalizedOption)
    {
      this.option = option;
      this.normalizedOption = normalizedOption;
    }



    @Override
    public int compareTo(Impl other)
    {
      if (other == ZERO_OPTION_IMPL)
      {
        // If other has zero options then this sorts after.
        return 1;
      }
      else
      {
        int result =
            normalizedOption.compareTo(other.firstNormalizedOption());
        if (result == 0)
        {
          if (other.size() > 1)
          {
            return -1;
          }
        }
        return result;
      }
    }



    @Override
    public boolean containsOption(String normalizedOption)
    {
      return this.normalizedOption.equals(normalizedOption);
    }



    @Override
    public boolean equals(Impl other)
    {
      return (other.size() == 1)
          && other.containsOption(normalizedOption);
    }



    @Override
    public String firstNormalizedOption()
    {
      return normalizedOption;
    }



    @Override
    public int hashCode()
    {
      return normalizedOption.hashCode();
    }



    @Override
    public boolean hasOptions()
    {
      return true;
    }



    @Override
    public boolean isSubTypeOf(Impl other)
    {
      // Other must have no options or the same option.
      if (other == ZERO_OPTION_IMPL)
      {
        return true;
      }
      else
      {
        return equals(other);
      }
    }



    @Override
    public boolean isSuperTypeOf(Impl other)
    {
      // Other must have this option.
      return other.containsOption(normalizedOption);
    }



    public Iterator<String> iterator()
    {
      return Iterators.singleton(option);
    }



    @Override
    public int size()
    {
      return 1;
    }

  }



  private static final class ZeroOptionImpl extends Impl
  {
    private ZeroOptionImpl()
    {
      // Nothing to do.
    }



    @Override
    public int compareTo(Impl other)
    {
      // If other has options then this sorts before.
      return (this == other) ? 0 : -1;
    }



    @Override
    public boolean containsOption(String normalizedOption)
    {
      return false;
    }



    @Override
    public boolean equals(Impl other)
    {
      return (this == other);
    }



    @Override
    public String firstNormalizedOption()
    {
      // No first option.
      return null;
    }



    @Override
    public int hashCode()
    {
      // Use attribute type hash code.
      return 0;
    }



    @Override
    public boolean hasOptions()
    {
      return false;
    }



    @Override
    public boolean isSubTypeOf(Impl other)
    {
      // Can only be a sub-type if other has no options.
      return (this == other);
    }



    @Override
    public boolean isSuperTypeOf(Impl other)
    {
      // Will always be a super-type.
      return true;
    }



    public Iterator<String> iterator()
    {
      return Iterators.empty();
    }



    @Override
    public int size()
    {
      return 0;
    }

  }

  // Object class attribute description.
  private static final ZeroOptionImpl ZERO_OPTION_IMPL =
      new ZeroOptionImpl();

  private static final AttributeDescription OBJECT_CLASS;
  static
  {
    AttributeType attributeType =
        Schema.getCoreSchema().getAttributeType("2.5.4.0");
    OBJECT_CLASS =
        new AttributeDescription(attributeType.getNameOrOID(),
            attributeType, ZERO_OPTION_IMPL);
  }



  /**
   * Creates an attribute description having the provided attribute type
   * and no options.
   *
   * @param attributeType
   *          The attribute type.
   * @return The attribute description.
   * @throws NullPointerException
   *           If {@code attributeType} was {@code null}.
   */
  public static AttributeDescription create(AttributeType attributeType)
      throws NullPointerException
  {
    Validator.ensureNotNull(attributeType);

    // Use object identity in case attribute type does not come from
    // core schema.
    if (attributeType == OBJECT_CLASS.getAttributeType())
    {
      return OBJECT_CLASS;
    }
    else
    {
      return new AttributeDescription(attributeType.getNameOrOID(),
          attributeType, ZERO_OPTION_IMPL);
    }
  }



  /**
   * Creates an attribute description having the same attribute type and
   * options as the provided attribute description and, in addition, the
   * provided list of options.
   *
   * @param attributeDescription
   *          The attribute description.
   * @param options
   *          The attribute options.
   * @return The new attribute description containing {@code options}.
   * @throws NullPointerException
   *           If {@code attributeDescription} or {@code options} was
   *           {@code null}.
   */
  public static AttributeDescription create(
      AttributeDescription attributeDescription, String... options)
      throws NullPointerException
  {
    Validator.ensureNotNull(attributeDescription, options);

    // This should not be called very often, so don't optimize.
    AttributeDescription newAttributeDescription = attributeDescription;
    for (String option : options)
    {
      newAttributeDescription = create(newAttributeDescription, option);
    }
    return newAttributeDescription;
  }



  /**
   * Creates an attribute description having the same attribute type and
   * options as the provided attribute description and, in addition, the
   * provided new option.
   *
   * @param attributeDescription
   *          The attribute description.
   * @param option
   *          The attribute option.
   * @return The new attribute description containing {@code option}.
   * @throws NullPointerException
   *           If {@code attributeDescription} or {@code option} was
   *           {@code null}.
   */
  public static AttributeDescription create(
      AttributeDescription attributeDescription, String option)
      throws NullPointerException
  {
    Validator.ensureNotNull(attributeDescription, option);

    String normalizedOption = toLowerCase(option);
    if (attributeDescription.pimpl.containsOption(normalizedOption))
    {
      return attributeDescription;
    }

    String oldAttributeDescription =
        attributeDescription.attributeDescription;
    StringBuilder builder =
        new StringBuilder(oldAttributeDescription.length()
            + option.length() + 1);
    builder.append(oldAttributeDescription);
    builder.append(';');
    builder.append(option);
    String newAttributeDescription = builder.toString();

    Impl impl = attributeDescription.pimpl;
    if (impl instanceof ZeroOptionImpl)
    {
      return new AttributeDescription(newAttributeDescription,
          attributeDescription.attributeType, new SingleOptionImpl(
              option, normalizedOption));

    }
    else if (impl instanceof SingleOptionImpl)
    {
      SingleOptionImpl simpl = (SingleOptionImpl) impl;

      String[] newOptions = new String[2];
      newOptions[0] = simpl.option;
      newOptions[1] = option;

      String[] newNormalizedOptions = new String[2];
      if (normalizedOption.compareTo(simpl.normalizedOption) < 0)
      {
        newNormalizedOptions[0] = normalizedOption;
        newNormalizedOptions[1] = simpl.normalizedOption;
      }

      return new AttributeDescription(newAttributeDescription,
          attributeDescription.attributeType, new MultiOptionImpl(
              newOptions, newNormalizedOptions));
    }
    else
    {
      MultiOptionImpl mimpl = (MultiOptionImpl) impl;

      int sz1 = mimpl.options.length;
      String[] newOptions = new String[sz1 + 1];
      for (int i = 0; i < sz1; i++)
      {
        newOptions[i] = mimpl.options[i];
      }
      newOptions[sz1] = option;

      int sz2 = mimpl.normalizedOptions.length;
      String[] newNormalizedOptions = new String[sz2 + 1];
      boolean inserted = false;
      for (int i = 0; i < sz2; i++)
      {
        if (!inserted)
        {
          String s = mimpl.normalizedOptions[i];
          if (normalizedOption.compareTo(s) < 0)
          {
            newNormalizedOptions[i] = normalizedOption;
            newNormalizedOptions[i + 1] = s;
            inserted = true;
          }
          else
          {
            newNormalizedOptions[i] = s;
          }
        }
        else
        {
          newNormalizedOptions[i + 1] = mimpl.normalizedOptions[i];
        }
      }

      if (!inserted)
      {
        newNormalizedOptions[sz2] = normalizedOption;
      }

      return new AttributeDescription(newAttributeDescription,
          attributeDescription.attributeType, new MultiOptionImpl(
              newOptions, newNormalizedOptions));
    }
  }



  /**
   * Creates an attribute description having the provided attribute type
   * and single option.
   *
   * @param attributeType
   *          The attribute type.
   * @param option
   *          The attribute option.
   * @return The attribute description.
   * @throws NullPointerException
   *           If {@code attributeType} or {@code option} was {@code
   *           null}.
   */
  public static AttributeDescription create(
      AttributeType attributeType, String option)
      throws NullPointerException
  {
    Validator.ensureNotNull(attributeType, option);

    String oid = attributeType.getNameOrOID();
    StringBuilder builder =
        new StringBuilder(oid.length() + option.length() + 1);
    builder.append(oid);
    builder.append(';');
    builder.append(option);
    String attributeDescription = builder.toString();
    String normalizedOption = toLowerCase(option);

    return new AttributeDescription(attributeDescription,
        attributeType, new SingleOptionImpl(option, normalizedOption));
  }



  /**
   * Creates an attribute description having the provided attribute type
   * and options.
   *
   * @param attributeType
   *          The attribute type.
   * @param options
   *          The attribute options.
   * @return The attribute description.
   * @throws NullPointerException
   *           If {@code attributeType} or {@code options} was {@code
   *           null}.
   */
  public static AttributeDescription create(
      AttributeType attributeType, String... options)
      throws NullPointerException
  {
    Validator.ensureNotNull(attributeType, options);

    switch (options.length)
    {
    case 0:
      return create(attributeType);
    case 1:
      return create(attributeType, options[0]);
    default:
      String[] optionsList = new String[options.length];
      String[] normalizedOptions = new String[options.length];

      String oid = attributeType.getNameOrOID();
      StringBuilder builder =
          new StringBuilder(oid.length() + options[0].length()
              + options[1].length() + 2);
      builder.append(oid);

      int i = 0;
      for (String option : options)
      {
        builder.append(';');
        builder.append(option);
        optionsList[i] = option;
        String normalizedOption = toLowerCase(option);
        normalizedOptions[i++] = normalizedOption;
      }
      Arrays.sort(normalizedOptions);

      String attributeDescription = builder.toString();
      return new AttributeDescription(attributeDescription,
          attributeType, new MultiOptionImpl(optionsList,
              normalizedOptions));
    }

  }



  /**
   * Returns an attribute description representing the object class
   * attribute type with no options.
   *
   * @return The object class attribute description.
   */
  public static AttributeDescription objectClass()
  {
    return OBJECT_CLASS;
  }



  /**
   * Parses the provided LDAP string representation of an attribute
   * description using the provided schema.
   *
   * @param attributeDescription
   *          The LDAP string representation of an attribute
   *          description.
   * @param schema
   *          The schema to use when parsing the attribute description.
   * @return The parsed attribute description.
   * @throws LocalizedIllegalArgumentException
   *           If {@code attributeDescription} is not a valid LDAP
   *           string representation of an attribute description.
   * @throws NullPointerException
   *           If {@code attributeDescription} or {@code schema} was
   *           {@code null}.
   */
  public static AttributeDescription valueOf(
      String attributeDescription, Schema schema)
      throws LocalizedIllegalArgumentException, NullPointerException
  {
    Validator.ensureNotNull(attributeDescription, schema);

    // The normalized description will always be needed.
    String normalizedAttributeDescription =
        toLowerCase(attributeDescription);

    // Determine if the attribute description has any options.
    int semicolon = normalizedAttributeDescription.indexOf(';');
    if (semicolon < 0)
    {
      // No options.
      String oid = normalizedAttributeDescription;
      AttributeType attributeType;
      try
      {
        attributeType = schema.getAttributeType(oid);
      }
      catch (UnknownSchemaElementException e)
      {
        Message message =
            ERR_ATTRIBUTE_DESCRIPTION_TYPE_NOT_FOUND.get(
                attributeDescription, e.getMessageObject());
        throw new LocalizedIllegalArgumentException(message);
      }

      // Use object identity in case attribute type does not come from
      // core schema.
      if (attributeType == OBJECT_CLASS.getAttributeType()
          && attributeDescription.equals(OBJECT_CLASS.toString()))
      {
        return OBJECT_CLASS;
      }
      else
      {
        return new AttributeDescription(attributeDescription,
            attributeType, ZERO_OPTION_IMPL);
      }
    }

    String oid = normalizedAttributeDescription.substring(0, semicolon);
    AttributeType attributeType;
    try
    {
      attributeType = schema.getAttributeType(oid);
    }
    catch (UnknownSchemaElementException e)
    {
      Message message =
          ERR_ATTRIBUTE_DESCRIPTION_TYPE_NOT_FOUND.get(
              attributeDescription, e.getMessageObject());
      throw new LocalizedIllegalArgumentException(message);
    }

    int nextSemicolon =
        normalizedAttributeDescription.indexOf(';', semicolon + 1);
    if (nextSemicolon < 0)
    {
      // Single option.
      String option = attributeDescription.substring(semicolon + 1);
      String normalizedOption =
          normalizedAttributeDescription.substring(semicolon + 1);
      return new AttributeDescription(attributeDescription,
          attributeType, new SingleOptionImpl(option, normalizedOption));
    }

    // Multiple options need sorting and duplicates removed - we could
    // optimize a bit further here for 2 option attribute descriptions.
    List<String> options = new LinkedList<String>();
    String firstOption =
        attributeDescription.substring(semicolon + 1, nextSemicolon);
    options.add(firstOption);

    SortedSet<String> normalizedOptions = new TreeSet<String>();
    String firstNormalizedOption =
        normalizedAttributeDescription.substring(semicolon + 1,
            nextSemicolon);
    normalizedOptions.add(firstNormalizedOption);

    semicolon = nextSemicolon;
    nextSemicolon =
        normalizedAttributeDescription.indexOf(';', semicolon + 1);
    while (nextSemicolon > 0)
    {
      String option =
          attributeDescription.substring(semicolon + 1, nextSemicolon);
      options.add(option);

      String normalizedOption =
          normalizedAttributeDescription.substring(semicolon + 1,
              nextSemicolon);
      normalizedOptions.add(normalizedOption);

      semicolon = nextSemicolon;
      nextSemicolon =
          normalizedAttributeDescription.indexOf(';', semicolon + 1);
    }

    String finalOption = attributeDescription.substring(semicolon + 1);
    options.add(finalOption);

    String finalNormalizedOption =
        normalizedAttributeDescription.substring(semicolon + 1);
    normalizedOptions.add(finalNormalizedOption);

    return new AttributeDescription(attributeDescription,
        attributeType, new MultiOptionImpl(options
            .toArray(new String[options.size()]), normalizedOptions
            .toArray(new String[normalizedOptions.size()])));
  }

  private final String attributeDescription;

  private final AttributeType attributeType;

  private final Impl pimpl;



  // Private constructor.
  private AttributeDescription(String attributeDescription,
      AttributeType attributeType, Impl pimpl)
  {
    this.attributeDescription = attributeDescription;
    this.attributeType = attributeType;
    this.pimpl = pimpl;
  }



  /**
   * Compares this attribute description to the provided attribute
   * description. The attribute types are compared first and then, if
   * equal, the options are normalized, sorted, and compared.
   *
   * @param other
   *          The attribute description to be compared.
   * @return A negative integer, zero, or a positive integer as this
   *         attribute description is less than, equal to, or greater
   *         than the specified attribute description.
   * @throws NullPointerException
   *           If {@code name} was {@code null}.
   */
  public int compareTo(AttributeDescription other)
      throws NullPointerException
  {
    int result = attributeType.compareTo(other.attributeType);
    if (result != 0)
    {
      return result;
    }
    else
    {
      // Attribute type is the same, so compare options.
      return pimpl.compareTo(other.pimpl);
    }
  }



  /**
   * Indicates whether or not this attribute description contains the
   * provided option.
   *
   * @param option
   *          The option for which to make the determination.
   * @return {@code true} if this attribute description has the provided
   *         option, or {@code false} if not.
   * @throws NullPointerException
   *           If {@code option} was {@code null}.
   */
  public boolean containsOption(String option)
      throws NullPointerException
  {
    String normalizedOption = toLowerCase(option);
    return pimpl.containsOption(normalizedOption);
  }



  /**
   * Indicates whether the provided object is an attribute description
   * which is equal to this attribute description. It will be considered
   * equal if the attribute type and normalized sorted list of options
   * are identical.
   *
   * @param o
   *          The object for which to make the determination.
   * @return {@code true} if the provided object is an attribute
   *         description that is equal to this attribute description, or
   *         {@code false} if not.
   */
  @Override
  public boolean equals(Object o)
  {
    if (this == o)
    {
      return true;
    }

    if (!(o instanceof AttributeDescription))
    {
      return false;
    }

    AttributeDescription other = (AttributeDescription) o;
    if (!attributeType.equals(other.attributeType))
    {
      return false;
    }

    // Attribute type is the same, compare options.
    return pimpl.equals(other.pimpl);
  }



  /**
   * Returns the attribute type associated with this attribute
   * description.
   *
   * @return The attribute type associated with this attribute
   *         description.
   */
  public AttributeType getAttributeType()
  {
    return attributeType;
  }



  /**
   * Returns an {@code Iterable} containing the options contained in
   * this attribute description. Attempts to remove options using an
   * iterator's {@code remove()} method are not permitted and will
   * result in an {@code UnsupportedOperationException} being thrown.
   *
   * @return An {@code Iterable} containing the options.
   */
  public Iterable<String> getOptions()
  {
    return pimpl;
  }



  /**
   * Returns the hash code for this attribute description. It will be
   * calculated as the sum of the hash codes of the attribute type and
   * normalized sorted list of options.
   *
   * @return The hash code for this attribute description.
   */
  @Override
  public int hashCode()
  {
    return attributeType.hashCode() + pimpl.hashCode();
  }



  /**
   * Indicates whether or not this attribute description has any
   * options.
   *
   * @return {@code true} if this attribute description has any options,
   *         or {@code false} if not.
   */
  public boolean hasOptions()
  {
    return pimpl.hasOptions();
  }



  /**
   * Indicates whether or not this attribute description is the {@code
   * objectClass} attribute description with no options.
   *
   * @return {@code true} if this attribute description is the {@code
   *         objectClass} attribute description with no options, or
   *         {@code false} if not.
   */
  public boolean isObjectClass()
  {
    return attributeType.isObjectClass() && !hasOptions();
  }



  /**
   * Indicates whether or not this attribute description is a sub-type
   * of the provided attribute description as defined in RFC 4512
   * section 2.5. Specifically, this method will return {@code true} if
   * and only if the following conditions are both {@code true}:
   * <ul>
   * <li>This attribute description has an attribute type which is equal
   * to, or is a sub-type of, the attribute type in the provided
   * attribute description.
   * <li>This attribute description contains all of the options
   * contained in the provided attribute description.
   * </ul>
   * Note that this method will return {@code true} if this attribute
   * description is equal to the provided attribute description.
   *
   * @param other
   *          The attribute description for which to make the
   *          determination.
   * @return {@code true} if this attribute description is a sub-type of
   *         the provided attribute description, or {@code false} if
   *         not.
   * @throws NullPointerException
   *           If {@code name} was {@code null}.
   */
  public boolean isSubTypeOf(AttributeDescription other)
      throws NullPointerException
  {
    if (!attributeType.isSubTypeOf(other.attributeType))
    {
      return false;
    }
    else
    {
      return pimpl.isSubTypeOf(other.pimpl);
    }
  }



  /**
   * Indicates whether or not this attribute description is a super-type
   * of the provided attribute description as defined in RFC 4512
   * section 2.5. Specifically, this method will return {@code true} if
   * and only if the following conditions are both {@code true}:
   * <ul>
   * <li>This attribute description has an attribute type which is equal
   * to, or is a super-type of, the attribute type in the provided
   * attribute description.
   * <li>This attribute description contains a sub-set of the options
   * contained in the provided attribute description.
   * </ul>
   * Note that this method will return {@code true} if this attribute
   * description is equal to the provided attribute description.
   *
   * @param other
   *          The attribute description for which to make the
   *          determination.
   * @return {@code true} if this attribute description is a super-type
   *         of the provided attribute description, or {@code false} if
   *         not.
   * @throws NullPointerException
   *           If {@code name} was {@code null}.
   */
  public boolean isSuperTypeOf(AttributeDescription other)
      throws NullPointerException
  {
    if (!other.attributeType.isSubTypeOf(attributeType))
    {
      return false;
    }
    else
    {
      return pimpl.isSuperTypeOf(other.pimpl);
    }
  }



  /**
   * Returns the string representation of this attribute description as
   * defined in RFC4512 section 2.5.
   *
   * @return The string representation of this attribute description.
   */
  @Override
  public String toString()
  {
    return attributeDescription;
  }
}
