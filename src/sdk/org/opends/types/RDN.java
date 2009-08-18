package org.opends.types;

import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_DN_ATTR_ILLEGAL_CHAR;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_DN_ATTR_VALUE_DECODE_FAILURE;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_DN_END_WITH_ATTR_NAME;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_DN_ESCAPED_HEX_VALUE_INVALID;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_DN_HEX_VALUE_TOO_SHORT;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_DN_INVALID_HEX_DIGIT;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_DN_INVALID_REQUIRES_ESCAPE_CHAR;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_DN_NO_EQUAL;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_OID_CONSECUTIVE_PERIODS;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_OID_ILLEGAL_CHARACTER;
import static org.opends.server.util.StaticUtils.hexStringToByteArray;
import static org.opends.server.util.StaticUtils.isAlpha;
import static org.opends.server.util.StaticUtils.isDigit;
import static org.opends.server.util.StaticUtils.isHexDigit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.opends.ldap.DecodeException;
import org.opends.messages.Message;
import org.opends.schema.AttributeType;
import org.opends.schema.MatchingRule;
import org.opends.schema.Schema;
import org.opends.schema.Syntax;
import org.opends.server.types.ByteString;
import org.opends.server.types.ByteStringBuilder;
import org.opends.server.util.StaticUtils;
import org.opends.util.SubstringReader;
import org.opends.util.Validator;


/**
 * This class defines a data structure for storing and interacting
 * with the relative distinguished names associated with entries in
 * the Directory Server.
 */
public abstract class RDN implements Iterable<RDN.AttributeTypeAndValue>
{
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

    public AttributeType attributeType() {
      return attributeType;
    }

    public ByteString attributeValue() {
      return attributeValue;
    }

    public ConditionResult matches(AttributeTypeAndValue atv)
    {
      if(!attributeType.equals(atv.attributeType))
      {
        return ConditionResult.FALSE;
      }

      MatchingRule matchingRule = attributeType.getEqualityMatchingRule();
      if(matchingRule != null)
      {
        return matchingRule.valuesMatch(attributeValue, atv.attributeValue);
      }

      return ConditionResult.UNDEFINED;
    }


    public void toString(StringBuilder buffer)
    {
      if(!attributeType.getNames().iterator().hasNext())
      {
        buffer.append(attributeType.getOID());
        buffer.append("=#");
        buffer.append(attributeValue.toHex());
      }
      else
      {
        buffer.append(attributeType.getNameOrOID());
        buffer.append("=");
        Syntax syntax = attributeType.getSyntax();
        if(!syntax.isHumanReadable())
        {
          buffer.append("#");
          buffer.append(attributeValue.toHex());
        }
        else
        {
          String str = attributeValue.toString();
          char c;
          for(int si = 0; si < str.length(); si++)
          {
            c = str.charAt(si);
            if(c == ' ' || c == '#' || c == '"' || c == '+' ||
                c == ',' || c == ';' || c == '<' || c == '=' ||
                c == '>' || c == '\\' || c == '\u0000')
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

    private SingleValuedRDN(AttributeTypeAndValue atv) {
      this.atv = atv;
    }

    public ByteString getAttributeValue(AttributeType attributeType)
    {
      return atv.attributeType().equals(attributeType) ?
          atv.attributeValue() : null;
    }

    public int numAttributeTypeAndValues()
    {
      return 1;
    }

    public Iterator<AttributeTypeAndValue> iterator() {
      return new Iterator<AttributeTypeAndValue>()
      {
        private boolean visited = false;
        public boolean hasNext() {
          return !visited;
        }

        public AttributeTypeAndValue next() {
          if(visited)
          {
            throw new NoSuchElementException();
          }
          return atv;
        }

        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }

    public ConditionResult matches(RDN rdn)
    {
      if(rdn instanceof SingleValuedRDN)
      {
        return atv.matches(((SingleValuedRDN)rdn).atv);
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

    private MultiValuedRDN(AttributeTypeAndValue[] atvs) {
      this.atvs = atvs;
    }

    public ByteString getAttributeValue(AttributeType attributeType)
    {
      for (AttributeTypeAndValue atv : atvs) {
        if (atv.attributeType().equals(attributeType)) {
          return atv.attributeValue();
        }
      }
      return null;
    }

    public int numAttributeTypeAndValues()
    {
      return atvs.length;
    }

    public Iterator<AttributeTypeAndValue> iterator() {
      return new Iterator<AttributeTypeAndValue>()
      {
        private int i = 0;
        public boolean hasNext() {
          return i < atvs.length;
        }

        public AttributeTypeAndValue next() {
          if(i >= atvs.length)
          {
            throw new NoSuchElementException();
          }
          return atvs[i++];
        }

        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }

    public ConditionResult matches(RDN rdn)
    {
      if(rdn instanceof MultiValuedRDN)
      {
        ConditionResult result;
        MultiValuedRDN thatRDN = (MultiValuedRDN)rdn;
        for(int i = 0; i < atvs.length; i++)
        {
          result = atvs[i].matches(thatRDN.atvs[i]);
          if(result != ConditionResult.TRUE)
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
      for(int i = 0; i < atvs.length - 1; i++)
      {
        atvs[i].toString(buffer);
        buffer.append("+");
      }
      atvs[atvs.length - 1].toString(buffer);
    }
  }

  public abstract ByteString getAttributeValue(AttributeType attributeType);

  public abstract int numAttributeTypeAndValues();

  @Override
  public boolean equals(Object obj) {
    if(this == obj)
    {
      return true;
    }

    if(obj instanceof RDN)
    {
      RDN that = (RDN)obj;
      return matches(that) == ConditionResult.TRUE;
    }

    return false;
  }

  public abstract ConditionResult matches(RDN rdn);

  public abstract void toString(StringBuilder buffer);

  public static RDN create(AttributeType attributeType, ByteString value)
  {
    return new SingleValuedRDN(new AttributeTypeAndValue(attributeType, value));
  }
  public static RDN create(AttributeTypeAndValue atv)
  {
    Validator.ensureNotNull(atv);
    return new SingleValuedRDN(atv);
  }

  public static RDN create(AttributeTypeAndValue... attributeTypeAndValues)
  {
    Validator.ensureNotNull((Object[])attributeTypeAndValues);
    Arrays.sort(attributeTypeAndValues, ATV_COMPARATOR);
    return new MultiValuedRDN(attributeTypeAndValues);
  }

  public static RDN readRDN(SubstringReader reader, Schema schema)
    throws DecodeException
  {
    AttributeTypeAndValue firstAVA = readAttributeTypeAndValue(reader, schema);

    // Skip over any spaces that might be after the attribute value.
    reader.skipWhitespaces();

    reader.mark();
    if(reader.remaining() > 0 && reader.read() == '+')
    {
      List<AttributeTypeAndValue> avas = new ArrayList<AttributeTypeAndValue>();
      avas.add(firstAVA);

      do
      {
        avas.add(readAttributeTypeAndValue(reader, schema));

        // Skip over any spaces that might be after the attribute value.
        reader.skipWhitespaces();

        reader.mark();
      }
      while(reader.read() == '+');

      reader.reset();
      return create(avas.toArray(new AttributeTypeAndValue[avas.size()]));
    }
    else
    {
      reader.reset();
      return create(firstAVA);
    }
  }

  private static AttributeTypeAndValue readAttributeTypeAndValue(
      SubstringReader reader, Schema schema) throws DecodeException
  {
    // Skip over any spaces at the beginning.
    reader.skipWhitespaces();

    AttributeType attribute = readDNAttributeName(reader, schema);

    // Make sure that we're not at the end of the DN string because
    // that would be invalid.
    if (reader.remaining() == 0)
    {
      Message message = ERR_ATTR_SYNTAX_DN_END_WITH_ATTR_NAME.get(
          reader.getString(), attribute.getNameOrOID());
      throw new DecodeException(message);
    }

    // The next character must be an equal sign.  If it is not, then
    // that's an error.
    char c;
    if ((c = reader.read()) != '=')
    {
      Message message = ERR_ATTR_SYNTAX_DN_NO_EQUAL.get(
          reader.getString(), attribute.getNameOrOID(), c);
      throw new DecodeException(message);
    }


    // Skip over any spaces after the equal sign.
    reader.skipWhitespaces();

    // Parse the value for this RDN component.
    ByteString value = readDNAttributeValue(reader);

    return new AttributeTypeAndValue(attribute, value);
  }

  private static AttributeType readDNAttributeName(SubstringReader reader,
                                                   Schema schema)
      throws DecodeException
  {
    int length = 1;
    reader.mark();

    // The next character must be either numeric (for an OID) or alphabetic (for
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
                ERR_ATTR_SYNTAX_OID_CONSECUTIVE_PERIODS.
                    get(reader.getString(), reader.pos()-1);
            throw new DecodeException(message);
          }
          else
          {
            lastWasPeriod = true;
          }
        }
        else if (! isDigit(c))
        {
          // This must have been an illegal character.
          Message message =
              ERR_ATTR_SYNTAX_OID_ILLEGAL_CHARACTER.
                  get(reader.getString(), reader.pos()-1);
          throw new DecodeException(message);
        }
        else
        {
          lastWasPeriod = false;
        }
        length ++;
      }
      while((c = reader.read()) != '=');
    }
    if (isAlpha(c))
    {
      // This must be an attribute description.  In this case, we will only
      // accept alphabetic characters, numeric digits, and the hyphen.
      while((c = reader.read()) != '=')
      {
        if(length == 0 && !isAlpha(c))
        {
          // This is an illegal character.
          Message message = ERR_ATTR_SYNTAX_DN_ATTR_ILLEGAL_CHAR.
              get(reader.getString(), c, reader.pos() - 1);
          throw new DecodeException(message);
        }

        if(!isAlpha(c) && !isDigit(c) && c != '-')
        {
          // This is an illegal character.
          Message message = ERR_ATTR_SYNTAX_DN_ATTR_ILLEGAL_CHAR.
              get(reader.getString(), c, reader.pos() - 1);
          throw new DecodeException(message);
        }

        length ++;
      }
    }
    else
    {
      Message message = ERR_ATTR_SYNTAX_DN_ATTR_ILLEGAL_CHAR.
              get(reader.getString(), c, reader.pos() - 1);
      throw new DecodeException(message);
    }

    reader.reset();

    // Return the position of the first non-space character after the token.
    AttributeType attribute = schema.getAttributeType(reader.read(length));
    if(attribute == null)
    {
      // need to do something....
    }

    return attribute;
  }

  private static ByteString readDNAttributeValue(SubstringReader reader)
      throws DecodeException
  {
    // All leading spaces have already been stripped so we can start
    // reading the value.  However, it may be empty so check for that.
    if (reader.remaining() == 0)
    {
      return ByteString.empty();
    }

    reader.mark();

    // Look at the first character.  If it is an octothorpe (#), then
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
            ERR_ATTR_SYNTAX_DN_HEX_VALUE_TOO_SHORT.get(reader.getString());
        throw new DecodeException(message);
      }

      for (int i=0; i < 2; i++)
      {
        c = reader.read();
        if (isHexDigit(c))
        {
          length++;
        }
        else
        {
          Message message =
              ERR_ATTR_SYNTAX_DN_INVALID_HEX_DIGIT.get(reader.getString(), c);
          throw new DecodeException(message);
        }
      }


      // The rest of the value must be a multiple of two hex
      // characters.  The end of the value may be designated by the
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
              Message message = ERR_ATTR_SYNTAX_DN_INVALID_HEX_DIGIT.
                  get(reader.getString(), c);
              throw new DecodeException(message);
            }
          }
          else
          {
            Message message =
                ERR_ATTR_SYNTAX_DN_HEX_VALUE_TOO_SHORT.get(reader.getString());
            throw new DecodeException(message);
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
              ERR_ATTR_SYNTAX_DN_INVALID_HEX_DIGIT.get(reader.getString(), c);
          throw new DecodeException(message);
        }
      }


      // At this point, we should have a valid hex string.  Convert it
      // to a byte array and set that as the value of the provided
      // octet string.
      try
      {
        reader.reset();
        return ByteString.wrap(hexStringToByteArray(reader.read(length)));
      }
      catch (Exception e)
      {
        Message message =
            ERR_ATTR_SYNTAX_DN_ATTR_VALUE_DECODE_FAILURE.
              get(reader.getString(), String.valueOf(e));
        throw new DecodeException(message);
      }
    }


    // If the first character is a quotation mark, then the value
    // should continue until the corresponding closing quotation mark.
    else if (c == '"')
    {
      reader.mark();
      c = reader.read();
      ByteStringBuilder builder = null;

      if(c == '\\')
      {
        reader.mark();
        c = reader.read();
        if(isHexDigit(c))
        {
          builder = new ByteStringBuilder();
          char c2 = reader.read();
          if (isHexDigit(c2))
          {
            try
            {
              builder.append(StaticUtils.hexToByte(c, c2));
            }
            catch(Exception e)
            {
              Message message =
                  ERR_ATTR_SYNTAX_DN_ATTR_VALUE_DECODE_FAILURE.
                      get(reader.getString(), String.valueOf(e));
              throw new DecodeException(message);
            }
          }
          else
          {
            Message message =
                ERR_ATTR_SYNTAX_DN_ESCAPED_HEX_VALUE_INVALID.
                    get(reader.toString());
            throw new DecodeException(message);
          }
          reader.mark();
        }
        else
        {
          length++;
        }
      }
      else
      {
        length++;
      }

      while(reader.remaining() > 0)
      {
        c = reader.read();
        if(c == '\\')
        {
          // The previous character was an escape, so we'll take this
          // one.  However, this could be a hex digit, and if that's
          // the case then the escape would actually be in front of
          // two hex digits that should be treated as a special
          // character.
          if(builder == null)
          {
            builder = new ByteStringBuilder();
          }
          reader.reset();
          builder.append(reader.read(length));
          length = 0;
          reader.read();
          reader.mark();
          c = reader.read();
          if(isHexDigit(c))
          {
            // It is a hexadecimal digit, so the next digit must be
            // one too.  However, this could be just one in a series
            // of escaped hex pairs that is used in a string
            // containing one or more multi-byte UTF-8 characters so
            // we can't just treat this byte in isolation.  Collect
            // all the bytes together and make sure to take care of
            // these hex bytes before appending anything else to the
            // value.
            char c2 = reader.read();
            if (isHexDigit(c2))
            {
              try
              {
                builder.append(StaticUtils.hexToByte(c, c2));
              }
              catch(Exception e)
              {
                Message message =
                    ERR_ATTR_SYNTAX_DN_ATTR_VALUE_DECODE_FAILURE.
                        get(reader.getString(), String.valueOf(e));
                throw new DecodeException(message);
              }
            }
            else
            {
              Message message =
                  ERR_ATTR_SYNTAX_DN_ESCAPED_HEX_VALUE_INVALID.
                      get(reader.toString());
              throw new DecodeException(message);
            }
            reader.mark();
          }
          else
          {
            length++;
          }
        }
        else if (c == '"')
        {
          break;
        }
        else
        {
          length++;
        }
      }

      if(length > 0)
      {
        reader.reset();
        String last = reader.read(length);
        reader.read();
        if(builder != null)
        {
          builder.append(last);
          return builder.toByteString();
        }

        return ByteString.valueOf(last);
      }
      else if(builder != null)
      {
        return builder.toByteString();
      }
      else
      {
        return ByteString.empty();
      }
    }
    else if(c == '+' || c == ',' || (c == ';'))
    {
      //We don't allow an empty attribute value. So do not allow the
      // first character to be a '+' or ',' since it is not escaped
      // by the user.
      Message message =
             ERR_ATTR_SYNTAX_DN_INVALID_REQUIRES_ESCAPE_CHAR.get(
                      reader.getString(), reader.pos());
          throw new DecodeException(message);
    }


    // Otherwise, use general parsing to find the end of the value.
    else
    {
      ByteStringBuilder builder = null;
      int lengthWithoutSp = 0;

      if(c == '\\')
      {
        reader.mark();
        c = reader.read();
        if(isHexDigit(c))
        {
          builder = new ByteStringBuilder();
          char c2 = reader.read();
          if (isHexDigit(c2))
          {
            try
            {
              builder.append(StaticUtils.hexToByte(c, c2));
            }
            catch(Exception e)
            {
              Message message =
                  ERR_ATTR_SYNTAX_DN_ATTR_VALUE_DECODE_FAILURE.
                      get(reader.getString(), String.valueOf(e));
              throw new DecodeException(message);
            }
          }
          else
          {
            Message message =
                ERR_ATTR_SYNTAX_DN_ESCAPED_HEX_VALUE_INVALID.
                    get(reader.toString());
            throw new DecodeException(message);
          }
          reader.mark();
        }
        else
        {
          length++;
          if(c != ' ')
          {
            lengthWithoutSp++;
          }
        }
      }
      else
      {
        length++;
        if(c != ' ')
        {
          lengthWithoutSp++;
        }
      }

      while(reader.remaining() > 0)
      {
        c = reader.read();
        if(c == '\\')
        {
          // The previous character was an escape, so we'll take this
          // one.  However, this could be a hex digit, and if that's
          // the case then the escape would actually be in front of
          // two hex digits that should be treated as a special
          // character.
          if(builder == null)
          {
            builder = new ByteStringBuilder();
          }
          reader.reset();
          builder.append(reader.read(length));
          length = 0;
          lengthWithoutSp = 0;
          reader.read();
          reader.mark();
          c = reader.read();
          if(isHexDigit(c))
          {
            // It is a hexadecimal digit, so the next digit must be
            // one too.  However, this could be just one in a series
            // of escaped hex pairs that is used in a string
            // containing one or more multi-byte UTF-8 characters so
            // we can't just treat this byte in isolation.  Collect
            // all the bytes together and make sure to take care of
            // these hex bytes before appending anything else to the
            // value.
            char c2 = reader.read();
            if (isHexDigit(c2))
            {
              try
              {
                builder.append(StaticUtils.hexToByte(c, c2));
              }
              catch(Exception e)
              {
                Message message =
                    ERR_ATTR_SYNTAX_DN_ATTR_VALUE_DECODE_FAILURE.
                        get(reader.getString(), String.valueOf(e));
                throw new DecodeException(message);
              }
            }
            else
            {
              Message message =
                  ERR_ATTR_SYNTAX_DN_ESCAPED_HEX_VALUE_INVALID.
                      get(reader.toString());
              throw new DecodeException(message);
            }
            reader.mark();
          }
          else
          {
            length++;
            if(c != ' ')
            {
              lengthWithoutSp++;
            }
          }
        }
        else if ((c == ',') || (c == '+') || (c == ';'))
        {
          break;
        }
        else
        {
          length++;
          if(c != ' ')
          {
            lengthWithoutSp++;
          }
        }
      }

      reader.reset();
      if(lengthWithoutSp > 0)
      {
        String last = reader.read(lengthWithoutSp);
        if(builder != null)
        {
          builder.append(last);
          return builder.toByteString();
        }

        return ByteString.valueOf(last);
      }
      else if(builder != null)
      {
        return builder.toByteString();
      }
      else
      {
        return ByteString.empty();
      }
    }
  }



  // Prevent instantiation outside of this class.
  private RDN() {
    // No implementation required.
  }
}
