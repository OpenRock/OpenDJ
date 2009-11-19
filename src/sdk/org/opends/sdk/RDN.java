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



import static org.opends.messages.SchemaMessages.*;
import static org.opends.sdk.util.StaticUtils.hexStringToByteArray;
import static org.opends.sdk.util.StaticUtils.isAlpha;
import static org.opends.sdk.util.StaticUtils.isDigit;
import static org.opends.sdk.util.StaticUtils.isHexDigit;

import java.util.*;

import org.opends.messages.Message;
import org.opends.sdk.schema.*;
import org.opends.sdk.util.*;



/**
 * A relative distinguished name (RDN).
 */
public abstract class RDN implements
    Iterable<RDN.AttributeTypeAndValue>
{
  private static final char[] SPECIAL_CHARS =
      new char[] { '\"', '+', ',', ';', '<', '>', ' ', '#', '=', '\\' };
  private static final char[] DELIMITER_CHARS =
      new char[] { '+', ',', ';' };
  private static final char[] DQUOTE_CHAR = new char[] { '\"' };
  private static final Comparator<RDN.AttributeTypeAndValue> ATV_COMPARATOR =
      new Comparator<RDN.AttributeTypeAndValue>()
      {
        public int compare(RDN.AttributeTypeAndValue o1,
            RDN.AttributeTypeAndValue o2)
        {
          return o1.attributeType().getOID().compareTo(
              o2.attributeType().getOID());
        }
      };



  public static final class AttributeTypeAndValue
  {
    private final AttributeType attributeType;
    private final ByteString attributeValue;



    public AttributeTypeAndValue(AttributeType attributeType,
        ByteString attributeValue)
    {
      Validator.ensureNotNull(attributeType, attributeValue);
      this.attributeType = attributeType;
      this.attributeValue = attributeValue;
    }



    public AttributeType attributeType()
    {
      return attributeType;
    }



    public ByteString attributeValue()
    {
      return attributeValue;
    }



    public ConditionResult matches(AttributeTypeAndValue atv)
    {
      if (!attributeType.equals(atv.attributeType))
      {
        return ConditionResult.FALSE;
      }

      MatchingRule matchingRule =
          attributeType.getEqualityMatchingRule();
      if (matchingRule != null)
      {
        try
        {
          return matchingRule.getAssertion(attributeValue).matches(
              matchingRule.normalizeAttributeValue(atv.attributeValue));
        }
        catch (DecodeException de)
        {
          return ConditionResult.UNDEFINED;
        }
      }

      return ConditionResult.UNDEFINED;
    }



    public void toString(StringBuilder buffer)
    {
      if (!attributeType.getNames().iterator().hasNext())
      {
        buffer.append(attributeType.getOID());
        buffer.append("=#");
        StaticUtils.toHex(attributeValue, buffer);
      }
      else
      {
        buffer.append(attributeType.getNameOrOID());
        buffer.append("=");
        Syntax syntax = attributeType.getSyntax();
        if (!syntax.isHumanReadable())
        {
          buffer.append("#");
          StaticUtils.toHex(attributeValue, buffer);
        }
        else
        {
          String str = attributeValue.toString();
          char c;
          for (int si = 0; si < str.length(); si++)
          {
            c = str.charAt(si);
            if (c == ' ' || c == '#' || c == '"' || c == '+'
                || c == ',' || c == ';' || c == '<' || c == '='
                || c == '>' || c == '\\' || c == '\u0000')
            {
              buffer.append('\\');
            }
            buffer.append(c);
          }
        }
      }
    }
  }



  private static final class SingleValuedRDN extends RDN
  {
    private final AttributeTypeAndValue atv;



    private SingleValuedRDN(AttributeTypeAndValue atv)
    {
      this.atv = atv;
    }



    public ByteString getAttributeValue(AttributeType attributeType)
    {
      return atv.attributeType().equals(attributeType) ? atv
          .attributeValue() : null;
    }



    public int numAttributeTypeAndValues()
    {
      return 1;
    }



    public Iterator<AttributeTypeAndValue> iterator()
    {
      return new Iterator<AttributeTypeAndValue>()
      {
        private boolean visited = false;



        public boolean hasNext()
        {
          return !visited;
        }



        public AttributeTypeAndValue next()
        {
          if (visited)
          {
            throw new NoSuchElementException();
          }
          return atv;
        }



        public void remove()
        {
          throw new UnsupportedOperationException();
        }
      };
    }



    public ConditionResult matches(RDN rdn)
    {
      if (rdn instanceof SingleValuedRDN)
      {
        return atv.matches(((SingleValuedRDN) rdn).atv);
      }
      return ConditionResult.FALSE;
    }



    public void toString(StringBuilder buffer)
    {
      atv.toString(buffer);
    }
  }



  private static final class MultiValuedRDN extends RDN
  {
    private final AttributeTypeAndValue[] atvs;



    private MultiValuedRDN(AttributeTypeAndValue[] atvs)
    {
      this.atvs = atvs;
    }



    public ByteString getAttributeValue(AttributeType attributeType)
    {
      for (AttributeTypeAndValue atv : atvs)
      {
        if (atv.attributeType().equals(attributeType))
        {
          return atv.attributeValue();
        }
      }
      return null;
    }



    public int numAttributeTypeAndValues()
    {
      return atvs.length;
    }



    public Iterator<AttributeTypeAndValue> iterator()
    {
      return new Iterator<AttributeTypeAndValue>()
      {
        private int i = 0;



        public boolean hasNext()
        {
          return i < atvs.length;
        }



        public AttributeTypeAndValue next()
        {
          if (i >= atvs.length)
          {
            throw new NoSuchElementException();
          }
          return atvs[i++];
        }



        public void remove()
        {
          throw new UnsupportedOperationException();
        }
      };
    }



    public ConditionResult matches(RDN rdn)
    {
      if (rdn instanceof MultiValuedRDN)
      {
        ConditionResult result;
        MultiValuedRDN thatRDN = (MultiValuedRDN) rdn;
        for (int i = 0; i < atvs.length; i++)
        {
          result = atvs[i].matches(thatRDN.atvs[i]);
          if (result != ConditionResult.TRUE)
          {
            return result;
          }
        }
        return ConditionResult.TRUE;
      }
      return ConditionResult.FALSE;
    }



    public void toString(StringBuilder buffer)
    {
      for (int i = 0; i < atvs.length - 1; i++)
      {
        atvs[i].toString(buffer);
        buffer.append("+");
      }
      atvs[atvs.length - 1].toString(buffer);
    }
  }



  public abstract ByteString getAttributeValue(
      AttributeType attributeType);



  public abstract int numAttributeTypeAndValues();



  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
    {
      return true;
    }

    if (obj instanceof RDN)
    {
      RDN that = (RDN) obj;
      return matches(that) == ConditionResult.TRUE;
    }

    return false;
  }



  public abstract ConditionResult matches(RDN rdn);



  public abstract void toString(StringBuilder buffer);



  public static RDN newRDN(AttributeType attributeType, ByteString value)
  {
    return new SingleValuedRDN(new AttributeTypeAndValue(attributeType,
        value));
  }



  public static RDN newRDN(AttributeTypeAndValue atv)
  {
    Validator.ensureNotNull(atv);
    return new SingleValuedRDN(atv);
  }



  public static RDN newRDN(
      AttributeTypeAndValue... attributeTypeAndValues)
  {
    Validator.ensureNotNull((Object[]) attributeTypeAndValues);
    Arrays.sort(attributeTypeAndValues, ATV_COMPARATOR);
    return new MultiValuedRDN(attributeTypeAndValues);
  }



  public static RDN valueOf(String rdnString)
      throws LocalizedIllegalArgumentException
  {
    return valueOf(rdnString, Schema.getDefaultSchema());
  }



  public static RDN valueOf(String rdnString, Schema schema)
      throws LocalizedIllegalArgumentException
  {
    SubstringReader reader = new SubstringReader(rdnString);
    try
    {
      return decode(reader, schema);
    }
    catch (UnknownSchemaElementException e)
    {
      Message message =
          ERR_RDN_TYPE_NOT_FOUND.get(rdnString, e.getMessageObject());
      throw new LocalizedIllegalArgumentException(message);
    }
  }



  static RDN decode(SubstringReader reader, Schema schema)
      throws LocalizedIllegalArgumentException,
      UnknownSchemaElementException
  {
    AttributeTypeAndValue firstAVA =
        readAttributeTypeAndValue(reader, schema);

    // Skip over any spaces that might be after the attribute value.
    reader.skipWhitespaces();

    reader.mark();
    if (reader.remaining() > 0 && reader.read() == '+')
    {
      List<AttributeTypeAndValue> avas =
          new ArrayList<AttributeTypeAndValue>();
      avas.add(firstAVA);

      do
      {
        avas.add(readAttributeTypeAndValue(reader, schema));

        // Skip over any spaces that might be after the attribute value.
        reader.skipWhitespaces();

        reader.mark();
      }
      while (reader.read() == '+');

      reader.reset();
      return newRDN(avas
          .toArray(new AttributeTypeAndValue[avas.size()]));
    }
    else
    {
      reader.reset();
      return newRDN(firstAVA);
    }
  }



  private static AttributeTypeAndValue readAttributeTypeAndValue(
      SubstringReader reader, Schema schema)
      throws LocalizedIllegalArgumentException,
      UnknownSchemaElementException
  {
    // Skip over any spaces at the beginning.
    reader.skipWhitespaces();

    AttributeType attribute = readDNAttributeName(reader, schema);

    // Make sure that we're not at the end of the DN string because
    // that would be invalid.
    if (reader.remaining() == 0)
    {
      Message message =
          ERR_ATTR_SYNTAX_DN_END_WITH_ATTR_NAME.get(reader.getString(),
              attribute.getNameOrOID());
      throw new LocalizedIllegalArgumentException(message);
    }

    // The next character must be an equal sign. If it is not, then
    // that's an error.
    char c;
    if ((c = reader.read()) != '=')
    {
      Message message =
          ERR_ATTR_SYNTAX_DN_NO_EQUAL.get(reader.getString(), attribute
              .getNameOrOID(), c);
      throw new LocalizedIllegalArgumentException(message);
    }

    // Skip over any spaces after the equal sign.
    reader.skipWhitespaces();

    // Parse the value for this RDN component.
    ByteString value = readDNAttributeValue(reader);

    return new AttributeTypeAndValue(attribute, value);
  }



  private static AttributeType readDNAttributeName(
      SubstringReader reader, Schema schema)
      throws LocalizedIllegalArgumentException,
      UnknownSchemaElementException
  {
    int length = 1;
    reader.mark();

    // The next character must be either numeric (for an OID) or
    // alphabetic (for
    // an attribute description).
    char c = reader.read();
    if (isDigit(c))
    {
      boolean lastWasPeriod = false;
      do
      {
        if (c == '.')
        {
          if (lastWasPeriod)
          {
            Message message =
                ERR_ATTR_SYNTAX_OID_CONSECUTIVE_PERIODS.get(reader
                    .getString(), reader.pos() - 1);
            throw new LocalizedIllegalArgumentException(message);
          }
          else
          {
            lastWasPeriod = true;
          }
        }
        else if (!isDigit(c))
        {
          // This must have been an illegal character.
          Message message =
              ERR_ATTR_SYNTAX_OID_ILLEGAL_CHARACTER.get(reader
                  .getString(), reader.pos() - 1);
          throw new LocalizedIllegalArgumentException(message);
        }
        else
        {
          lastWasPeriod = false;
        }
        length++;
      }
      while ((c = reader.read()) != '=');
    }
    if (isAlpha(c))
    {
      // This must be an attribute description. In this case, we will
      // only
      // accept alphabetic characters, numeric digits, and the hyphen.
      while ((c = reader.read()) != '=')
      {
        if (length == 0 && !isAlpha(c))
        {
          // This is an illegal character.
          Message message =
              ERR_ATTR_SYNTAX_DN_ATTR_ILLEGAL_CHAR.get(reader
                  .getString(), c, reader.pos() - 1);
          throw new LocalizedIllegalArgumentException(message);
        }

        if (!isAlpha(c) && !isDigit(c) && c != '-')
        {
          // This is an illegal character.
          Message message =
              ERR_ATTR_SYNTAX_DN_ATTR_ILLEGAL_CHAR.get(reader
                  .getString(), c, reader.pos() - 1);
          throw new LocalizedIllegalArgumentException(message);
        }

        length++;
      }
    }
    else
    {
      Message message =
          ERR_ATTR_SYNTAX_DN_ATTR_ILLEGAL_CHAR.get(reader.getString(),
              c, reader.pos() - 1);
      throw new LocalizedIllegalArgumentException(message);
    }

    reader.reset();

    // Return the position of the first non-space character after the
    // token.

    return schema.getAttributeType(reader.read(length));
  }



  private static ByteString readDNAttributeValue(SubstringReader reader)
      throws LocalizedIllegalArgumentException
  {
    // All leading spaces have already been stripped so we can start
    // reading the value. However, it may be empty so check for that.
    if (reader.remaining() == 0)
    {
      return ByteString.empty();
    }

    reader.mark();

    // Look at the first character. If it is an octothorpe (#), then
    // that means that the value should be a hex string.
    char c = reader.read();
    int length = 0;
    if (c == '#')
    {
      // The first two characters must be hex characters.
      reader.mark();
      if (reader.remaining() < 2)
      {
        Message message =
            ERR_ATTR_SYNTAX_DN_HEX_VALUE_TOO_SHORT.get(reader
                .getString());
        throw new LocalizedIllegalArgumentException(message);
      }

      for (int i = 0; i < 2; i++)
      {
        c = reader.read();
        if (isHexDigit(c))
        {
          length++;
        }
        else
        {
          Message message =
              ERR_ATTR_SYNTAX_DN_INVALID_HEX_DIGIT.get(reader
                  .getString(), c);
          throw new LocalizedIllegalArgumentException(message);
        }
      }

      // The rest of the value must be a multiple of two hex
      // characters. The end of the value may be designated by the
      // end of the DN, a comma or semicolon, or a space.
      while (reader.remaining() > 0)
      {
        c = reader.read();
        if (isHexDigit(c))
        {
          length++;

          if (reader.remaining() > 0)
          {
            c = reader.read();
            if (isHexDigit(c))
            {
              length++;
            }
            else
            {
              Message message =
                  ERR_ATTR_SYNTAX_DN_INVALID_HEX_DIGIT.get(reader
                      .getString(), c);
              throw new LocalizedIllegalArgumentException(message);
            }
          }
          else
          {
            Message message =
                ERR_ATTR_SYNTAX_DN_HEX_VALUE_TOO_SHORT.get(reader
                    .getString());
            throw new LocalizedIllegalArgumentException(message);
          }
        }
        else if ((c == ' ') || (c == ',') || (c == ';'))
        {
          // This denotes the end of the value.
          break;
        }
        else
        {
          Message message =
              ERR_ATTR_SYNTAX_DN_INVALID_HEX_DIGIT.get(reader
                  .getString(), c);
          throw new LocalizedIllegalArgumentException(message);
        }
      }

      // At this point, we should have a valid hex string. Convert it
      // to a byte array and set that as the value of the provided
      // octet string.
      try
      {
        reader.reset();
        return ByteString
            .wrap(hexStringToByteArray(reader.read(length)));
      }
      catch (Exception e)
      {
        Message message =
            ERR_ATTR_SYNTAX_DN_ATTR_VALUE_DECODE_FAILURE.get(reader
                .getString(), String.valueOf(e));
        throw new LocalizedIllegalArgumentException(message);
      }
    }

    // If the first character is a quotation mark, then the value
    // should continue until the corresponding closing quotation mark.
    else if (c == '"')
    {
      try
      {
        return StaticUtils.evaluateEscapes(reader, DQUOTE_CHAR, false);
      }
      catch (DecodeException e)
      {
        throw new LocalizedIllegalArgumentException(e
            .getMessageObject());
      }
    }

    // Otherwise, use general parsing to find the end of the value.
    else
    {
      reader.reset();
      ByteString bytes;
      try
      {
        bytes =
            StaticUtils.evaluateEscapes(reader, SPECIAL_CHARS,
                DELIMITER_CHARS, true);
      }
      catch (DecodeException e)
      {
        throw new LocalizedIllegalArgumentException(e
            .getMessageObject());
      }
      if (bytes.length() == 0)
      {
        // We don't allow an empty attribute value.
        Message message =
            ERR_ATTR_SYNTAX_DN_INVALID_REQUIRES_ESCAPE_CHAR.get(reader
                .getString(), reader.pos());
        throw new LocalizedIllegalArgumentException(message);
      }
      return bytes;
    }
  }



  // Prevent instantiation outside of this class.
  private RDN()
  {
    // No implementation required.
  }
}
