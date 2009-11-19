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
package org.opends.sdk.schema;



import org.opends.sdk.Assertion;
import org.opends.sdk.ConditionResult;
import org.opends.sdk.DN;
import org.opends.sdk.DecodeException;
import org.opends.sdk.util.ByteSequence;
import org.opends.sdk.util.ByteString;
import org.opends.sdk.util.LocalizedIllegalArgumentException;



/**
 * This class defines the distinguishedNameMatch matching rule defined
 * in X.520 and referenced in RFC 2252.
 */
final class DistinguishedNameEqualityMatchingRuleImpl extends
    AbstractMatchingRuleImpl
{
  @Override
  public Assertion getAssertion(final Schema schema, ByteSequence value)
      throws DecodeException
  {
    DN assertion;
    try
    {
      assertion = DN.valueOf(value.toString(), schema);
    }
    catch (LocalizedIllegalArgumentException e)
    {
      throw DecodeException.error(e.getMessageObject());
    }

    final DN finalAssertion = assertion;
    return new Assertion()
    {
      public ConditionResult matches(ByteSequence attributeValue)
      {
        try
        {
          DN attribute = DN.valueOf(attributeValue.toString(), schema);
          return attribute.matches(finalAssertion);
        }
        catch (LocalizedIllegalArgumentException e)
        {
          return ConditionResult.UNDEFINED;
        }
      }
    };
  }



  public ByteString normalizeAttributeValue(Schema schema,
      ByteSequence value) throws DecodeException
  {
    try
    {
      return ByteString.valueOf(DN.valueOf(value.toString(), schema)
          .toString());
    }
    catch (LocalizedIllegalArgumentException e)
    {
      throw DecodeException.error(e.getMessageObject());
    }
  }
}
