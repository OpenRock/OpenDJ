package org.opends.schema;

import org.opends.ldap.DecodeException;
import org.opends.messages.Message;
import static org.opends.messages.SchemaMessages.*;
import static org.opends.server.util.StaticUtils.*;
import static org.opends.server.util.ServerConstants.SCHEMA_PROPERTY_ORIGIN;
import org.opends.server.util.StaticUtils;
import org.opends.server.types.ByteString;
import org.opends.server.types.ByteStringBuilder;
import org.opends.util.SubstringReader;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 13, 2009
 * Time: 10:15:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class SchemaUtils
{
  public static final Map<String, List<String>> RFC4512_ORIGIN =
      Collections.singletonMap(SCHEMA_PROPERTY_ORIGIN,
          Collections.singletonList("RFC 4512"));

  public static final Map<String, List<String>> RFC4530_ORIGIN =
      Collections.singletonMap(SCHEMA_PROPERTY_ORIGIN,
          Collections.singletonList("RFC 4530"));

  /**
   * Reads the next OID from the definition, skipping over
   * any leading spaces.
   *
   * @param  reader The string representation of the definition.
   *
   * @return  The OID read from the definition.
   *
   * @throws  DecodeException  If a problem is encountered while reading the
   *                              token name.
   */
  public static String readNumericOID(SubstringReader reader)
      throws DecodeException
  {
    // This must be a numeric OID.  In that case, we will accept
    // only digits  and periods, but not consecutive periods.
    boolean lastWasPeriod = false;
    int length = 0;

    // Skip over any spaces at the beginning of the value.
    reader.skipWhitespaces();
    reader.mark();

    char c = reader.read();
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
        // Technically, this must be an illegal character.  However, it is
        // possible that someone just got sloppy and did not include a space
        // between the name/OID and a closing parenthesis.  In that case,
        // we'll assume it's the end of the value.
        if (c == ')')
        {
          break;
        }

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
    while((c = reader.read()) != ' ');

    if(length == 0)
    {
      Message message =
          ERR_ATTR_SYNTAX_OID_NO_VALUE.get();
      throw new DecodeException(message);
    }

    reader.reset();

    return reader.read(length);
  }

  /**
   * Reads the next OID from the definition, skipping over
   * any leading spaces.
   *
   * @param  reader The string representation of the definition.
   *
   * @return  The OID read from the definition.
   *
   * @throws  DecodeException  If a problem is encountered while reading the
   *                              token name.
   */
  public static String readNumericOIDLen(SubstringReader reader)
      throws DecodeException
  {
    // This must be a numeric OID.  In that case, we will accept
    // only digits  and periods, but not consecutive periods.
    boolean lastWasPeriod = false;
    int length = 0;

    // Skip over any spaces at the beginning of the value.
    reader.skipWhitespaces();
    reader.mark();

    char c = reader.read();
    while(c != ' ' && c != '{')
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
        // Technically, this must be an illegal character.  However, it is
        // possible that someone just got sloppy and did not include a space
        // between the name/OID and a closing parenthesis.  In that case,
        // we'll assume it's the end of the value.
        if (c == ')')
        {
          break;
        }

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
      c = reader.read();
    }

    if(length == 0)
    {
      Message message =
          ERR_ATTR_SYNTAX_OID_NO_VALUE.get();
      throw new DecodeException(message);
    }

    reader.reset();
    String oid = reader.read(length);

    if(reader.read() == '{')
    {
      reader.mark();
      // The only thing we'll allow here will be numeric digits and the
      // closing curly brace.
      while((c = reader.read()) != '}')
      {
        if(!isDigit(c))
        {
          Message message =
              ERR_ATTR_SYNTAX_OID_ILLEGAL_CHARACTER.
                  get(reader.getString(), reader.pos()-1);
          throw new DecodeException(message);
        }
      }
    }

    return oid;
  }

  public static List<String> readNameDescriptors(SubstringReader reader)
      throws DecodeException
  {
    int length = 0;
    List<String> values;

    // Skip over any spaces at the beginning of the value.
    reader.skipWhitespaces();

    char c = reader.read();
    if (c == '\'')
    {
      reader.mark();
      // Parse until the closing quote.
      do
      {
        length ++;
      }
      while(reader.read() != '\'');

      reader.reset();
      values = Collections.singletonList(reader.read(length));
    }
    else if (c == '(')
    {
      // Skip over any leading spaces;
      reader.skipWhitespaces();
      reader.mark();

      c = reader.read();
      if(c == ')')
      {
        values = Collections.emptyList();
      }
      else
      {
        values = new ArrayList<String>();
        do
        {
          reader.reset();
          values.add(readQuotedDescriptor(reader));
          reader.mark();
        }
        while(reader.read() != ')');
      }
    }
    else
    {
      Message message = ERR_ATTR_SYNTAX_ILLEGAL_CHAR_IN_STRING_OID.
          get(String.valueOf(c), reader.pos() - 1);
      throw new DecodeException(message);
    }

    return values;
  }

  /**
   * Reads the next token name from the definition, skipping over
   * any leading or trailing spaces or <code>null</code> if there
   * are no moretokens to read.
   *
   * @param  reader The string representation of the definition.
   *
   * @return  The token name read from the definition or
   * <code>null</code>.
   *
   * @throws  DecodeException  If a problem is encountered while reading the
   *                              token name.
   */
  public static String readTokenName(SubstringReader reader)
          throws DecodeException
  {
    String token = null;
    int length = 0;
    // Skip over any spaces at the beginning of the value.
    reader.skipWhitespaces();
    reader.mark();

    // Read until we find the next space.
    char c = reader.read();
    while(c != ' ' && c != ')')
    {
      length ++;
    }

    if(length > 0)
    {
      reader.reset();
      token = reader.read(length);
    }

    // Skip over any trailing spaces after the value.
    reader.skipWhitespaces();

    if(token == null && reader.remaining() > 0)
    {
      reader.reset();
      Message message =
          ERR_ATTR_SYNTAX_UNEXPECTED_CLOSE_PARENTHESIS.
              get(length);
      throw new DecodeException(message);
    }

    return token;
  }

    /**
   * Reads the value of a string enclosed in single quotes, skipping over the
   * quotes and any leading spaces.
   *
   * @param  reader The string representation of the definition.
   *
   * @return The string value read from the definition.
   *
   * @throws DecodeException  If a problem is encountered while reading the
   *                              quoted string.
   */
  public static String readQuotedString(SubstringReader reader)
          throws DecodeException
  {
    int length = 0;

    // Skip over any spaces at the beginning of the value.
    reader.skipWhitespaces();

    // The next character must be a single quote.
    char c = reader.read();
    if (c != '\'')
    {
      Message message = WARN_ATTR_SYNTAX_EXPECTED_QUOTE_AT_POS.get(
          reader.pos()-1, String.valueOf(c));
      throw new DecodeException(message);
    }


    // Read until we find the closing quote.
    reader.mark();
    while(reader.read() != '\'')
    {
      length ++;
    }

    reader.reset();

    return reader.read(length);
  }

  /**
   * Reads the value of a string enclosed in single quotes, skipping over the
   * quotes and any leading spaces.
   *
   * @param  reader The string representation of the definition.
   *
   * @return The string value read from the definition.
   *
   * @throws DecodeException  If a problem is encountered while reading the
   *                              quoted string.
   */
  public static String readQuotedDescriptor(SubstringReader reader)
          throws DecodeException
  {
    int length = 0;

    // Skip over any spaces at the beginning of the value.
    reader.skipWhitespaces();

    // The next character must be a single quote.
    char c = reader.read();
    if (c != '\'')
    {
      Message message = WARN_ATTR_SYNTAX_EXPECTED_QUOTE_AT_POS.get(
          reader.pos()-1, String.valueOf(c));
      throw new DecodeException(message);
    }

    // Read until we find the closing quote.
    reader.mark();
    while((c = reader.read()) != '\'')
    {
      if(length == 0 && !isAlpha(c))
      {
        // This is an illegal character.
        Message message = ERR_ATTR_SYNTAX_ILLEGAL_CHAR_IN_STRING_OID.
            get(String.valueOf(c), reader.pos() - 1);
        throw new DecodeException(message);
      }

      if(!isAlpha(c) && !isDigit(c) && c != '-')
      {
        // This is an illegal character.
        Message message = ERR_ATTR_SYNTAX_ILLEGAL_CHAR_IN_STRING_OID.
            get(String.valueOf(c), reader.pos() - 1);
        throw new DecodeException(message);
      }
      
      length ++;
    }

    reader.reset();

    return reader.read(length);
  }

  public static List<String> readOIDs(SubstringReader reader)
      throws DecodeException
  {
    List<String> values;

    // Skip over any spaces at the beginning of the value.
    reader.skipWhitespaces();
    reader.mark();

    char c = reader.read();
    if (c == '(')
    {
      values = new ArrayList<String>();

      do
      {
        values.add(readOID(reader));

        // Skip over any trailing spaces;
        reader.skipWhitespaces();
      }
      while(reader.read() != ')');
    }
    else
    {
      reader.reset();
      values = Collections.singletonList(readOID(reader));
    }

    return values;
  }

  /**
   * Reads the attribute description or numeric OID, skipping over
   * any leading or trailing spaces.
   *
   * @param reader The string representation of the definition.
   *
   * @return The attribute description or numeric OID read from
   *         the definition.
   *
   * @throws  DecodeException  If a problem is encountered while reading the
   *                              name or OID.
   */
  public static String readOID(SubstringReader reader)
          throws DecodeException
  {
    int length = 1;

    // Skip over any spaces at the beginning of the value.
    reader.skipWhitespaces();
    reader.mark();

    // The next character must be either numeric (for an OID) or alphabetic (for
    // an attribute description).
    char c = reader.read();
    if (isDigit(c))
    {
      reader.reset();
      return readNumericOID(reader);
    }

    if (isAlpha(c))
    {
      // This must be an attribute description.  In this case, we will only
      // accept alphabetic characters, numeric digits, and the hyphen.
      c = reader.read();
      while(c != ' ' && c != ')')
      {
        if(length == 0 && !isAlpha(c))
        {
          // This is an illegal character.
          Message message = ERR_ATTR_SYNTAX_ILLEGAL_CHAR_IN_STRING_OID.
              get(String.valueOf(c), reader.pos() - 1);
          throw new DecodeException(message);
        }

        if(!isAlpha(c) && !isDigit(c) && c != '-')
        {
          // This is an illegal character.
          Message message = ERR_ATTR_SYNTAX_ILLEGAL_CHAR_IN_STRING_OID.
              get(String.valueOf(c), reader.pos() - 1);
          throw new DecodeException(message);
        }

        length ++;
      }
    }
    else
    {
      Message message = ERR_ATTR_SYNTAX_ILLEGAL_CHAR_IN_STRING_OID.
                  get(String.valueOf(c), reader.pos() - 1);
      throw new DecodeException(message);
    }

    reader.reset();

    // Return the position of the first non-space character after the token.
    return reader.read(length);
  }



  /**
   * Reads the value for an "extra" parameter.  It will handle a single unquoted
   * word (which is technically illegal, but we'll allow it), a single quoted
   * string, or an open parenthesis followed by a space-delimited set of quoted
   * strings or unquoted words followed by a close parenthesis.
   *
   * @param  reader The string representation of the definition.
   *
   * @return  The "extra" parameter value that was read.
   *
   * @throws  DecodeException  If a problem occurs while attempting to read
   *                              the value.
   */
  public static List<String> readExtraParameterValues(SubstringReader reader)
          throws DecodeException
  {
    int length = 0;
    List<String> values;

    // Skip over any leading spaces.
    reader.skipWhitespaces();
    reader.mark();


    // Look at the next character.  If it is a quote, then parse until the next
    // quote and end.  If it is an open parenthesis, then parse individual
    // values until the close parenthesis and end.  Otherwise, parse until the
    // next space and end.
    char c = reader.read();
    if (c == '\'')
    {
      reader.mark();
      // Parse until the closing quote.
      do
      {
        length ++;
      }
      while(reader.read() != '\'');

      reader.reset();
      values = Collections.singletonList(reader.read(length));
    }
    else if (c == '(')
    {
      // Skip over any leading spaces;
      reader.skipWhitespaces();
      reader.mark();

      c = reader.read();
      if(c == ')')
      {
        values = Collections.emptyList();
      }
      else
      {
        values = new ArrayList<String>();
        do
        {
          reader.reset();
          values.add(readQuotedString(reader));
          reader.mark();
        }
        while(reader.read() != ')');
      }
    }
    else
    {
      // Parse until the next space.
      do
      {
        length ++;
      }
      while(reader.read() != ' ');

      reader.reset();
      values = Collections.singletonList(reader.read(length));
    }

    return values;
  }

  /**
   * Reads the next ruleid from the definition, skipping over
   * any leading spaces.
   *
   * @param  reader The string representation of the definition.
   *
   * @return  The ruleid read from the definition.
   *
   * @throws  DecodeException  If a problem is encountered while reading the
   *                              token name.
   */
  public static Integer readRuleID(SubstringReader reader)
      throws DecodeException
  {
    // This must be a ruleid.  In that case, we will accept
    // only digits.
    int length = 0;

    // Skip over any spaces at the beginning of the value.
    reader.skipWhitespaces();
    reader.mark();

    char c = reader.read();
    do
    {
      if (! isDigit(c))
      {
        // Technically, this must be an illegal character.  However, it is
        // possible that someone just got sloppy and did not include a space
        // between the ruleid and a closing parenthesis.  In that case,
        // we'll assume it's the end of the value.
        if (c == ')')
        {
          break;
        }

        // This must have been an illegal character.
        Message message =
            ERR_ATTR_SYNTAX_RULE_ID_ILLEGAL_CHARACTER.
                get(reader.getString(), reader.pos()-1);
        throw new DecodeException(message);
      }
      length ++;
    }
    while((c = reader.read()) != ' ');

    if(length == 0)
    {
      Message message =
          ERR_ATTR_SYNTAX_RULE_ID_NO_VALUE.get();
      throw new DecodeException(message);
    }

    reader.reset();

    return Integer.valueOf(reader.read(length));
  }

  public static List<Integer> readRuleIDs(SubstringReader reader)
      throws DecodeException
  {
    List<Integer> values;

    // Skip over any spaces at the beginning of the value.
    reader.skipWhitespaces();
    reader.mark();

    char c = reader.read();
    if (c == '(')
    {
      values = new ArrayList<Integer>();

      do
      {
        values.add(readRuleID(reader));

        // Skip over any trailing spaces;
        reader.skipWhitespaces();
      }
      while(reader.read() != ')');
    }
    else
    {
      reader.reset();
      values = Collections.singletonList(readRuleID(reader));
    }

    return values;
  }

  public static class AttributeTypeAndValue
  {
    private String attributeType;
    private ByteString attributeValue;

    public AttributeTypeAndValue(String attributeType,
                                 ByteString attributeValue)
    {
      this.attributeType = attributeType;
      this.attributeValue = attributeValue;
    }

    public String getAttributeType() {
      return attributeType;
    }

    public ByteString getAttributeValue() {
      return attributeValue;
    }
  }

  public static List<List<AttributeTypeAndValue>> readDN(SubstringReader reader)
      throws DecodeException
  {
     // Skip over any spaces at the beginning of the value.
    reader.skipWhitespaces();

    if(reader.remaining() == 0)
    {
      // This means that the DN was completely comprised of spaces
      // and therefore should be considered the same as a null or
      // empty DN.
      return Collections.emptyList();
    }

    List<AttributeTypeAndValue> firstRDN = readRDN(reader);

    // Skip over any spaces that might be after the RDN.
    reader.skipWhitespaces();

    reader.mark();
    if(reader.read() == ',')
    {
      List<List<AttributeTypeAndValue>> rdns =
          new ArrayList<List<AttributeTypeAndValue>>();
      rdns.add(firstRDN);

      do
      {
        rdns.add(readRDN(reader));

        // Skip over any spaces that might be after the attribute value.
        reader.skipWhitespaces();

        reader.mark();
      }
      while(reader.read() == ',');

      reader.reset();
      return rdns;
    }
    else
    {
      reader.reset();
      return Collections.singletonList(firstRDN);
    }
  }

  public static List<AttributeTypeAndValue> readRDN(SubstringReader reader)
    throws DecodeException
  {
    AttributeTypeAndValue firstAVA = readAttributeTypeAndValue(reader);

    // Skip over any spaces that might be after the attribute value.
    reader.skipWhitespaces();

    reader.mark();
    if(reader.read() == '+')
    {
      List<AttributeTypeAndValue> avas = new ArrayList<AttributeTypeAndValue>();
      avas.add(firstAVA);

      do
      {
        avas.add(readAttributeTypeAndValue(reader));

        // Skip over any spaces that might be after the attribute value.
        reader.skipWhitespaces();

        reader.mark();
      }
      while(reader.read() == '+');

      reader.reset();
      return avas;
    }
    else
    {
      reader.reset();
      return Collections.singletonList(firstAVA);
    }
  }

  public static AttributeTypeAndValue readAttributeTypeAndValue(
      SubstringReader reader) throws DecodeException
  {
    // Skip over any spaces at the beginning.
    reader.skipWhitespaces();

    String attributeName = SchemaUtils.readDNAttributeName(reader);

    // Make sure that we're not at the end of the DN string because
    // that would be invalid.
    if (reader.remaining() == 0)
    {
      Message message = ERR_ATTR_SYNTAX_DN_END_WITH_ATTR_NAME.get(
          reader.getString(), attributeName);
      throw new DecodeException(message);
    }

    // The next character must be an equal sign.  If it is not, then
    // that's an error.
    char c;
    if ((c = reader.read()) != '=')
    {
      Message message = ERR_ATTR_SYNTAX_DN_NO_EQUAL.get(
          reader.getString(), attributeName, c);
      throw new DecodeException(message);
    }


    // Skip over any spaces after the equal sign.
    reader.skipWhitespaces();

    // Parse the value for this RDN component.
    ByteString value = SchemaUtils.readDNAttributeValue(reader);

    return new AttributeTypeAndValue(attributeName, value);
  }

  public static String readDNAttributeName(SubstringReader reader)
      throws DecodeException
  {
    int length = 1;

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
      c = reader.read();
      while(c != '=')
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
    return reader.read(length);

  }

  public static ByteString readDNAttributeValue(SubstringReader reader)
      throws DecodeException
  {
    // All leading spaces have already been stripped so we can start
    // reading the value.  However, it may be empty so check for that.
    if (reader.remaining() == 0)
    {
      return ByteString.empty();
    }


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
      // Keep reading until we find an unescaped closing quotation
      // mark.
      boolean escaped = false;
      StringBuilder valueString = new StringBuilder();
      while (true)
      {
        if (reader.remaining() == 0)
        {
          // We hit the end of the DN before the closing quote.
          // That's an error.
          Message message =
              ERR_ATTR_SYNTAX_DN_UNMATCHED_QUOTE.get(reader.getString());
          throw new DecodeException(message);
        }

        c = reader.read();
        if (escaped)
        {
          // The previous character was an escape, so we'll take this
          // one no matter what.
          valueString.append(c);
          escaped = false;
        }
        else if (c == '\\')
        {
          // The next character is escaped.  Set a flag to denote
          // this, but don't include the backslash.
          escaped = true;
        }
        else if (c == '"')
        {
          // This is the end of the value.
          break;
        }
        else
        {
          // This is just a regular character that should be in the
          // value.
          valueString.append(c);
        }
      }

      return ByteString.valueOf(valueString.toString());
    }
    else if(c == '+' || c == ',')
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
      boolean escaped;
      ByteStringBuilder hexBuffer = new ByteStringBuilder();
      ByteStringBuilder builder = new ByteStringBuilder();

      if (c == '\\')
      {
        escaped = true;
      }
      else
      {
        escaped = false;
        builder.append(c);
        if(c != ' ')
        {
          length = builder.length();
        }
      }


      // Keep reading until we find an unescaped comma or plus sign or
      // the end of the DN.
      while (true)
      {
        if (reader.remaining() == 0)
        {
          // This is the end of the DN and therefore the end of the
          // value.  If there are any hex characters, then we need to
          // deal with them accordingly.
          break;
        }

        reader.mark();
        c = reader.read();
        if (escaped)
        {
          // The previous character was an escape, so we'll take this
          // one.  However, this could be a hex digit, and if that's
          // the case then the escape would actually be in front of
          // two hex digits that should be treated as a special
          // character.
          if (isHexDigit(c))
          {
            // It is a hexadecimal digit, so the next digit must be
            // one too.  However, this could be just one in a series
            // of escaped hex pairs that is used in a string
            // containing one or more multi-byte UTF-8 characters so
            // we can't just treat this byte in isolation.  Collect
            // all the bytes together and make sure to take care of
            // these hex bytes before appending anything else to the
            // value.
            if (reader.remaining() == 0)
            {
              Message message =
                ERR_ATTR_SYNTAX_DN_ESCAPED_HEX_VALUE_INVALID.
                    get(reader.toString());
              throw new DecodeException(message);
            }
            else
            {
              char c2 = reader.read();
              if (isHexDigit(c2))
              {
                try
                {
                hexBuffer.append(StaticUtils.hexToByte(c, c2));
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
            }
          }
          else
          {
            if(hexBuffer.length() > 0)
            {
              hexBuffer.toString();
              builder.append(hexBuffer);
              hexBuffer.clear();
            }
            builder.append(c);
            length = builder.length();
          }

          escaped = false;
        }
        else if (c == '\\')
        {
          escaped = true;
        }
        else if ((c == ',') || (c == ';'))
        {
          reader.reset();
          break;
        }
        else if (c == '+')
        {
          reader.reset();
          break;
        }
        else
        {
          if(hexBuffer.length() > 0)
          {
            hexBuffer.toString();
            builder.append(hexBuffer);
            hexBuffer.clear();
          }
          builder.append(c);
          if(c != ' ')
          {
            length = builder.length();
          }
        }
      }

      if(hexBuffer.length() > 0)
      {
        hexBuffer.toString();
        builder.append(hexBuffer);
        hexBuffer.clear();
      }

      return builder.subSequence(0, length).toByteString();
    }
  }
}
