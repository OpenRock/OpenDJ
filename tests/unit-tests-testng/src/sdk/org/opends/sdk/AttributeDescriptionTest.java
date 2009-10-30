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



import java.util.Iterator;

import org.opends.sdk.schema.Schema;
import org.opends.sdk.util.LocalizedIllegalArgumentException;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;



/**
 * Test {@code AttributeDescription}.
 */
@Test(groups = { "precommit", "types", "sdk" }, sequential = true)
public final class AttributeDescriptionTest extends OpenDSTestCase
{
  @DataProvider(name = "dataForValueOfNoSchema")
  public Object[][] dataForValueOfNoSchema()
  {
    // Value, type, options, containsOptions("foo")
    return new Object[][] {
        { "cn", "cn", new String[0], false },
        { "CN", "cn", new String[0], false },
        { "objectClass", "objectclass", new String[0], false },
        { "cn;foo", "cn", new String[] { "foo" }, true },
        { "cn;FOO", "cn", new String[] { "FOO" }, true },
        { "cn;bar", "cn", new String[] { "bar" }, false },
        { "cn;BAR", "cn", new String[] { "BAR" }, false },
        { "cn;foo;bar", "cn", new String[] { "foo", "bar" }, true },
        { "cn;FOO;bar", "cn", new String[] { "FOO", "bar" }, true },
        { "cn;foo;BAR", "cn", new String[] { "foo", "BAR" }, true },
        { "cn;FOO;BAR", "cn", new String[] { "FOO", "BAR" }, true },
        { "cn;bar;FOO", "cn", new String[] { "bar", "FOO" }, true },
        { "cn;BAR;foo", "cn", new String[] { "BAR", "foo" }, true },
        { "cn;bar;FOO", "cn", new String[] { "bar", "FOO" }, true },
        { "cn;BAR;FOO", "cn", new String[] { "BAR", "FOO" }, true },
        { "cn;xxx;yyy;zzz", "cn", new String[] { "xxx", "yyy", "zzz" },
            false },
        { "cn;zzz;YYY;xxx", "cn", new String[] { "zzz", "YYY", "xxx" },
            false }, };
  }



  @Test(dataProvider = "dataForValueOfNoSchema")
  public void testValueOfNoSchema(String ad, String at,
      String[] options, boolean containsFoo)
  {
    AttributeDescription attributeDescription =
        AttributeDescription.valueOf(ad, Schema.getEmptySchema());

    Assert.assertEquals(attributeDescription.toString(), ad);

    Assert.assertEquals(attributeDescription.getAttributeType()
        .getNameOrOID(), at);

    Assert.assertFalse(attributeDescription.isObjectClass());

    if (options.length == 0)
    {
      Assert.assertFalse(attributeDescription.hasOptions());
    }
    else
    {
      Assert.assertTrue(attributeDescription.hasOptions());
    }

    Assert.assertFalse(attributeDescription.containsOption("dummy"));
    if (containsFoo)
    {
      Assert.assertTrue(attributeDescription.containsOption("foo"));
      Assert.assertTrue(attributeDescription.containsOption("FOO"));
      Assert.assertTrue(attributeDescription.containsOption("FoO"));
    }
    else
    {
      Assert.assertFalse(attributeDescription.containsOption("foo"));
      Assert.assertFalse(attributeDescription.containsOption("FOO"));
      Assert.assertFalse(attributeDescription.containsOption("FoO"));
    }

    for (String option : options)
    {
      Assert.assertTrue(attributeDescription.containsOption(option));
    }

    Iterator<String> iterator =
        attributeDescription.getOptions().iterator();
    for (int i = 0; i < options.length; i++)
    {
      Assert.assertTrue(iterator.hasNext());
      Assert.assertEquals(iterator.next(), options[i]);
    }
    Assert.assertFalse(iterator.hasNext());
  }



  @DataProvider(name = "dataForCompareNoSchema")
  public Object[][] dataForCompareNoSchema()
  {
    // AD1, AD2, compare result, isSubtype, isSuperType
    return new Object[][] { { "cn", "cn", 0, true, true },
        { "cn", "CN", 0, true, true }, { "CN", "cn", 0, true, true },
        { "CN", "CN", 0, true, true },
        { "cn", "commonName", -1, false, false },
        { "commonName", "cn", 1, false, false },
        { "commonName", "commonName", 0, true, true },
        { "cn", "cn;foo", -1, false, true },
        { "cn;foo", "cn", 1, true, false },
        { "cn;foo", "cn;foo", 0, true, true },
        { "CN;FOO", "cn;foo", 0, true, true },
        { "cn;foo", "CN;FOO", 0, true, true },
        { "CN;FOO", "CN;FOO", 0, true, true },
        { "cn;foo", "cn;bar", 1, false, false },
        { "cn;bar", "cn;foo", -1, false, false },

        { "cn;xxx;yyy", "cn", 1, true, false },
        { "cn;xxx;yyy", "cn;yyy", -1, true, false },
        { "cn;xxx;yyy", "cn;xxx", 1, true, false },
        { "cn;xxx;yyy", "cn;xxx;yyy", 0, true, true },
        { "cn;xxx;yyy", "cn;yyy;xxx", 0, true, true },

        { "cn", "cn;xxx;yyy", -1, false, true },
        { "cn;yyy", "cn;xxx;yyy", 1, false, true },
        { "cn;xxx", "cn;xxx;yyy", -1, false, true },
        { "cn;xxx;yyy", "cn;xxx;yyy", 0, true, true },
        { "cn;yyy;xxx", "cn;xxx;yyy", 0, true, true }, };
  }



  @Test(dataProvider = "dataForCompareNoSchema")
  public void testCompareNoSchema(String ad1, String ad2, int compare,
      boolean isSubType, boolean isSuperType)
  {
    AttributeDescription attributeDescription1 =
        AttributeDescription.valueOf(ad1, Schema.getEmptySchema());

    AttributeDescription attributeDescription2 =
        AttributeDescription.valueOf(ad2, Schema.getEmptySchema());

    // Identity.
    Assert.assertTrue(attributeDescription1
        .equals(attributeDescription1));
    Assert.assertTrue(attributeDescription1
        .compareTo(attributeDescription1) == 0);
    Assert.assertTrue(attributeDescription1
        .isSubTypeOf(attributeDescription1));
    Assert.assertTrue(attributeDescription1
        .isSuperTypeOf(attributeDescription1));

    if (compare == 0)
    {
      Assert.assertTrue(attributeDescription1
          .equals(attributeDescription2));
      Assert.assertTrue(attributeDescription2
          .equals(attributeDescription1));
      Assert.assertTrue(attributeDescription1
          .compareTo(attributeDescription2) == 0);
      Assert.assertTrue(attributeDescription2
          .compareTo(attributeDescription1) == 0);

      Assert.assertTrue(attributeDescription1
          .isSubTypeOf(attributeDescription2));
      Assert.assertTrue(attributeDescription1
          .isSuperTypeOf(attributeDescription2));
      Assert.assertTrue(attributeDescription2
          .isSubTypeOf(attributeDescription1));
      Assert.assertTrue(attributeDescription2
          .isSuperTypeOf(attributeDescription1));
    }
    else
    {
      Assert.assertFalse(attributeDescription1
          .equals(attributeDescription2));
      Assert.assertFalse(attributeDescription2
          .equals(attributeDescription1));

      if (compare < 0)
      {
        Assert.assertTrue(attributeDescription1
            .compareTo(attributeDescription2) < 0);
        Assert.assertTrue(attributeDescription2
            .compareTo(attributeDescription1) > 0);
      }
      else
      {
        Assert.assertTrue(attributeDescription1
            .compareTo(attributeDescription2) > 0);
        Assert.assertTrue(attributeDescription2
            .compareTo(attributeDescription1) < 0);
      }

      Assert.assertEquals(attributeDescription1
          .isSubTypeOf(attributeDescription2), isSubType);

      Assert.assertEquals(attributeDescription1
          .isSuperTypeOf(attributeDescription2), isSuperType);
    }
  }



  @DataProvider(name = "dataForValueOfInvalidAttributeDescriptions")
  public Object[][] dataForValueOfInvalidAttributeDescriptions()
  {
    return new Object[][] { { "" }, { " " }, { ";" }, { " ; " },
        { ";foo" }, { "cn;" }, { "cn; " }, { "cn;;foo" },
        { "cn; ;foo" }, { "cn;foo;" }, { "cn;foo; " },
        { "cn;foo;;bar" }, { "cn;foo; ;bar" }, { "cn;foo;bar;;" }, };
  }



  // FIXME: none of these pass! The valueOf method is far to lenient.
  @Test(dataProvider = "dataForValueOfInvalidAttributeDescriptions", expectedExceptions = LocalizedIllegalArgumentException.class, enabled = false)
  public void testValueOfInvalidAttributeDescriptions(String ad)
  {
    AttributeDescription.valueOf(ad, Schema.getEmptySchema());
  }
}
