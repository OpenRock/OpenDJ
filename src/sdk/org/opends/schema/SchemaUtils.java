package org.opends.schema;

import org.opends.ldap.DecodeException;
import org.opends.messages.Message;
import static org.opends.messages.SchemaMessages.*;
import static org.opends.server.util.StaticUtils.*;
import static org.opends.server.util.ServerConstants.SCHEMA_PROPERTY_ORIGIN;
import org.opends.server.util.StaticUtils;
import org.opends.server.types.ByteString;
import org.opends.server.types.ByteStringBuilder;
import static org.opends.server.schema.SchemaConstants.*;
import static org.opends.server.schema.SchemaConstants.SYNTAX_UUID_OID;
import static org.opends.server.schema.SchemaConstants.SYNTAX_UUID_DESCRIPTION;
import org.opends.util.SubstringReader;
import org.opends.schema.syntaxes.*;
import org.opends.schema.matchingrules.*;

import java.util.*;

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

  public static final Map<String, List<String>> RFC4517_ORIGIN =
      Collections.singletonMap(SCHEMA_PROPERTY_ORIGIN,
          Collections.singletonList("RFC 4517"));

  public static final Map<String, List<String>> RFC4530_ORIGIN =
      Collections.singletonMap(SCHEMA_PROPERTY_ORIGIN,
          Collections.singletonList("RFC 4530"));
  private static final String EMPTY_STRING = "".intern();
  private static final Set<String> EMPTY_STRING_SET =
      Collections.emptySet();

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

  public static SortedSet<String> readNameDescriptors(SubstringReader reader)
      throws DecodeException
  {
    int length = 0;
    SortedSet<String> values;

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
      values = singletonSortedSet(reader.read(length));
    }
    else if (c == '(')
    {
      // Skip over any leading spaces;
      reader.skipWhitespaces();
      reader.mark();

      c = reader.read();
      if(c == ')')
      {
        values = emptySortedSet();
      }
      else
      {
        values = new TreeSet<String>();
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

  public static Set<String> readOIDs(SubstringReader reader)
      throws DecodeException
  {
    Set<String> values;

    // Skip over any spaces at the beginning of the value.
    reader.skipWhitespaces();
    reader.mark();

    char c = reader.read();
    if (c == '(')
    {
      values = new HashSet<String>();

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
      values = Collections.singleton(readOID(reader));
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

  public static Set<Integer> readRuleIDs(SubstringReader reader)
      throws DecodeException
  {
    Set<Integer> values;

    // Skip over any spaces at the beginning of the value.
    reader.skipWhitespaces();
    reader.mark();

    char c = reader.read();
    if (c == '(')
    {
      values = new HashSet<Integer>();

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
      values = Collections.singleton(readRuleID(reader));
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

  private final static SortedSet<Object> EMPTY_SORTED_SET =
      new EmptySortedSet();

  public static <T> SortedSet<T> emptySortedSet()
  {
    return (SortedSet<T>)EMPTY_SORTED_SET;
  }

  private static class EmptySortedSet extends AbstractSet<Object>
      implements SortedSet<Object>
  {
    public Iterator<Object> iterator() {
      return new Iterator<Object>() {
        public boolean hasNext() {
          return false;
        }
        public Object next() {
          throw new NoSuchElementException();
        }
        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }

    public int size() {return 0;}

    public boolean contains(Object obj) {return false;}

    public Comparator<Object> comparator() {
      return null;
    }

    public SortedSet<Object> subSet(Object fromElement, Object toElement) {
      return EMPTY_SORTED_SET;
    }

    public SortedSet<Object> headSet(Object toElement) {
      return EMPTY_SORTED_SET;
    }

    public SortedSet<Object> tailSet(Object fromElement) {
      return null;
    }

    public Object first() {
      return null;
    }

    public Object last() {
      return null;
    }
  }

  public static <T> SortedSet<T> singletonSortedSet(T element)
  {
    return new SingletonSortedSet<T>(element);
  }

  private static class SingletonSortedSet<E> extends AbstractSet<E>
      implements SortedSet<E>
  {
    final private E element;

    SingletonSortedSet(E e) {element = e;}

    public Iterator<E> iterator() {
      return new Iterator<E>() {
        private boolean hasNext = true;
        public boolean hasNext() {
          return hasNext;
        }
        public E next() {
          if (hasNext) {
            hasNext = false;
            return element;
          }
          throw new NoSuchElementException();
        }
        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }

    public int size() {return 1;}

    public boolean contains(Object o)
    {
      return (o==null ? element==null : o.equals(element));
    }

    public Comparator<? super E> comparator() {
      return null;
    }

    public SortedSet<E> subSet(E fromElement, E toElement) {
      Comparable<E> e = (Comparable<E>)element;
      if(e.compareTo(fromElement) >= 0 && e.compareTo(toElement) < 0)
      {
        return this;
      }
      return emptySortedSet();
    }

    public SortedSet<E> headSet(E toElement) {
      Comparable<E> e = (Comparable<E>)element;
      if(e.compareTo(toElement) < 0)
      {
        return this;
      }
      return emptySortedSet();
    }

    public SortedSet<E> tailSet(E fromElement) {
      Comparable<E> e = (Comparable<E>)element;
      if(e.compareTo(fromElement) >= 0)
      {
        return this;
      }
      return emptySortedSet();
    }

    public E first() {
      return element;
    }

    public E last() {
      return element;
    }
  }

  static Schema generateDefaultSchema()
  {
    SchemaBuilder builder = new SchemaBuilder();
    try
    {
      defaultSyntaxes(builder);
      defaultMatchingRules(builder);
      defaultAttributeTypes(builder);
      defaultObjectClasses(builder);
      return builder.toSchema();
    }
    catch(Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  private static void defaultSyntaxes(SchemaBuilder builder)
      throws SchemaException
  {
    // All RFC 4512 / 4517
    builder.addSyntax(SYNTAX_ATTRIBUTE_TYPE_OID,
        SYNTAX_ATTRIBUTE_TYPE_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new AttributeTypeSyntax(), false);
    builder.addSyntax(SYNTAX_BINARY_OID, SYNTAX_BINARY_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new BinarySyntax(), false);
    builder.addSyntax(SYNTAX_BIT_STRING_OID, SYNTAX_ATTRIBUTE_TYPE_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new BitStringSyntax(), false);
    builder.addSyntax(SYNTAX_BOOLEAN_OID, SYNTAX_BOOLEAN_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new BooleanSyntax(), false);
    builder.addSyntax(SYNTAX_CERTLIST_OID, SYNTAX_CERTLIST_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new CertificateListSyntax(), false);
    builder.addSyntax(SYNTAX_CERTPAIR_OID, SYNTAX_CERTPAIR_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new CertificatePairSyntax(), false);
    builder.addSyntax(SYNTAX_CERTIFICATE_OID, SYNTAX_CERTIFICATE_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new CertificateSyntax(), false);
    builder.addSyntax(SYNTAX_COUNTRY_STRING_OID,
        SYNTAX_COUNTRY_STRING_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new CountryStringSyntax(), false);
    builder.addSyntax(SYNTAX_DELIVERY_METHOD_OID,
        SYNTAX_DELIVERY_METHOD_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new DeliveryMethodSyntax(), false);
    builder.addSyntax(SYNTAX_DIRECTORY_STRING_OID,
        SYNTAX_DIRECTORY_STRING_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new DirectoryStringSyntax(false), false);
    builder.addSyntax(SYNTAX_DIT_CONTENT_RULE_OID,
        SYNTAX_DIT_CONTENT_RULE_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new DITContentRuleSyntax(), false);
    builder.addSyntax(SYNTAX_DIT_STRUCTURE_RULE_OID,
        SYNTAX_DIT_STRUCTURE_RULE_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new DITStructureRuleSyntax(), false);
    builder.addSyntax(SYNTAX_DN_OID, SYNTAX_DN_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new DistinguishedNameSyntax(), false);
    builder.addSyntax(SYNTAX_ENHANCED_GUIDE_OID,
        SYNTAX_ENHANCED_GUIDE_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new EnhancedGuideSyntax(), false);
    builder.addSyntax(SYNTAX_FAXNUMBER_OID, SYNTAX_FAXNUMBER_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new FacsimileNumberSyntax(), false);
    builder.addSyntax(SYNTAX_FAX_OID, SYNTAX_FAX_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new FaxSyntax(), false);
    builder.addSyntax(SYNTAX_GENERALIZED_TIME_OID,
        SYNTAX_GENERALIZED_TIME_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new GeneralizedTimeSyntax(), false);
    builder.addSyntax(SYNTAX_GUIDE_OID, SYNTAX_GUIDE_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new GuideSyntax(), false);
    builder.addSyntax(SYNTAX_IA5_STRING_OID, SYNTAX_IA5_STRING_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new IA5StringSyntax(), false);
    builder.addSyntax(SYNTAX_INTEGER_OID, SYNTAX_INTEGER_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new IntegerSyntax(), false);
    builder.addSyntax(SYNTAX_JPEG_OID, SYNTAX_JPEG_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new JPEGSyntax(), false);
    builder.addSyntax(SYNTAX_MATCHING_RULE_OID,
        SYNTAX_MATCHING_RULE_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new MatchingRuleSyntax(), false);
    builder.addSyntax(SYNTAX_MATCHING_RULE_USE_OID,
        SYNTAX_MATCHING_RULE_USE_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new MatchingRuleUseSyntax(), false);
    builder.addSyntax(SYNTAX_LDAP_SYNTAX_OID, SYNTAX_LDAP_SYNTAX_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new LDAPSyntaxDescriptionSyntax(), false);
    builder.addSyntax(SYNTAX_NAME_AND_OPTIONAL_UID_OID,
        SYNTAX_NAME_AND_OPTIONAL_UID_DESCRIPTION,
        SchemaUtils.RFC4517_ORIGIN, new NameAndOptionalUIDSyntax(), false);
    builder.addSyntax(SYNTAX_NAME_FORM_OID, SYNTAX_NAME_FORM_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new NameFormSyntax(), false);
    builder.addSyntax(SYNTAX_NUMERIC_STRING_OID,
        SYNTAX_NUMERIC_STRING_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new NumericStringSyntax(), false);
    builder.addSyntax(SYNTAX_OBJECTCLASS_OID, SYNTAX_OBJECTCLASS_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new ObjectClassSyntax(), false);
    builder.addSyntax(SYNTAX_OCTET_STRING_OID, SYNTAX_OCTET_STRING_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new OctetStringSyntax(), false);
    builder.addSyntax(SYNTAX_OID_OID, SYNTAX_OID_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new OIDSyntax(), false);
    builder.addSyntax(SYNTAX_OTHER_MAILBOX_OID,
        SYNTAX_OTHER_MAILBOX_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new OtherMailboxSyntax(), false);
    builder.addSyntax(SYNTAX_POSTAL_ADDRESS_OID,
        SYNTAX_POSTAL_ADDRESS_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new PostalAddressSyntax(), false);
    builder.addSyntax(SYNTAX_PRESENTATION_ADDRESS_OID,
        SYNTAX_PRESENTATION_ADDRESS_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new PresentationAddressSyntax(), false);
    builder.addSyntax(SYNTAX_PRINTABLE_STRING_OID,
        SYNTAX_PRINTABLE_STRING_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new PrintableStringSyntax(), false);
    builder.addSyntax(SYNTAX_PROTOCOL_INFORMATION_OID,
        SYNTAX_PROTOCOL_INFORMATION_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new ProtocolInformationSyntax(), false);
    builder.addSyntax(SYNTAX_SUBSTRING_ASSERTION_OID,
        SYNTAX_SUBSTRING_ASSERTION_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new SubstringAssertionSyntax(), false);
    builder.addSyntax(SYNTAX_SUPPORTED_ALGORITHM_OID,
        SYNTAX_SUPPORTED_ALGORITHM_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new SupportedAlgorithmSyntax(), false);
    builder.addSyntax(SYNTAX_TELEPHONE_OID, SYNTAX_TELEPHONE_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new TelephoneNumberSyntax(false), false);
    builder.addSyntax(SYNTAX_TELETEX_TERM_ID_OID,
        SYNTAX_TELETEX_TERM_ID_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new TeletexTerminalIdentifierSyntax(),
        false);
    builder.addSyntax(SYNTAX_TELEX_OID, SYNTAX_TELEX_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new TelexNumberSyntax(), false);
    builder.addSyntax(SYNTAX_UTC_TIME_OID, SYNTAX_UTC_TIME_DESCRIPTION,
        SchemaUtils.RFC4512_ORIGIN, new UTCTimeSyntax(), false);

    // Extras
    builder.addSyntax(SYNTAX_UUID_OID, SYNTAX_UUID_DESCRIPTION,
        SchemaUtils.RFC4530_ORIGIN, new UUIDSyntax(), false);
  }

  private static void defaultMatchingRules(SchemaBuilder builder)
      throws SchemaException
  {
    builder.addMatchingRule(EMR_BIT_STRING_OID,
        SchemaUtils.singletonSortedSet(EMR_BIT_STRING_NAME),
        EMPTY_STRING, false, SYNTAX_BIT_STRING_OID, SchemaUtils.RFC4512_ORIGIN,
        new BitStringEqualityMatchingRule(), false);
    builder.addMatchingRule(EMR_BOOLEAN_OID,
        SchemaUtils.singletonSortedSet(EMR_BOOLEAN_NAME),
        EMPTY_STRING, false, SYNTAX_BOOLEAN_OID, SchemaUtils.RFC4512_ORIGIN,
        new BooleanEqualityMatchingRule(), false);
    builder.addMatchingRule(EMR_CASE_EXACT_IA5_OID,
        SchemaUtils.singletonSortedSet(EMR_CASE_EXACT_IA5_NAME),
        EMPTY_STRING, false, SYNTAX_IA5_STRING_OID, SchemaUtils.RFC4512_ORIGIN,
        new CaseExactIA5EqualityMatchingRule(), false);
    builder.addMatchingRule(EMR_CASE_EXACT_OID,
        SchemaUtils.singletonSortedSet(EMR_CASE_EXACT_NAME),
        EMPTY_STRING, false, SYNTAX_DIRECTORY_STRING_OID,
        SchemaUtils.RFC4512_ORIGIN, new CaseExactEqualityMatchingRule(), false);
    builder.addMatchingRule(OMR_CASE_EXACT_OID,
        SchemaUtils.singletonSortedSet(OMR_CASE_EXACT_NAME),
        EMPTY_STRING, false, SYNTAX_DIRECTORY_STRING_OID,
        SchemaUtils.RFC4512_ORIGIN, new CaseExactOrderingMatchingRule(), false);
    builder.addMatchingRule(SMR_CASE_EXACT_OID,
        SchemaUtils.singletonSortedSet(SMR_CASE_EXACT_NAME),
        EMPTY_STRING, false, SYNTAX_SUBSTRING_ASSERTION_OID,
        SchemaUtils.RFC4512_ORIGIN, new CaseExactSubstringMatchingRule(),
        false);
    builder.addMatchingRule(EMR_CASE_IGNORE_IA5_OID,
        SchemaUtils.singletonSortedSet(EMR_CASE_IGNORE_IA5_NAME),
        EMPTY_STRING, false, SYNTAX_IA5_STRING_OID,
        SchemaUtils.RFC4512_ORIGIN, new CaseIgnoreIA5EqualityMatchingRule(),
        false);
    builder.addMatchingRule(SMR_CASE_IGNORE_IA5_OID,
        SchemaUtils.singletonSortedSet(SMR_CASE_IGNORE_IA5_NAME),
        EMPTY_STRING, false, SYNTAX_SUBSTRING_ASSERTION_OID,
        SchemaUtils.RFC4512_ORIGIN, new CaseIgnoreIA5SubstringMatchingRule(),
        false);
    builder.addMatchingRule(EMR_CASE_IGNORE_LIST_OID,
        SchemaUtils.singletonSortedSet(EMR_CASE_IGNORE_LIST_NAME),
        EMPTY_STRING, false, SYNTAX_POSTAL_ADDRESS_OID,
        SchemaUtils.RFC4512_ORIGIN, new CaseIgnoreListEqualityMatchingRule(),
        false);
    builder.addMatchingRule(SMR_CASE_IGNORE_LIST_OID,
        SchemaUtils.singletonSortedSet(SMR_CASE_IGNORE_LIST_NAME),
        EMPTY_STRING, false, SYNTAX_SUBSTRING_ASSERTION_OID,
        SchemaUtils.RFC4512_ORIGIN, new CaseIgnoreListSubstringMatchingRule(),
        false);
    builder.addMatchingRule(EMR_CASE_IGNORE_OID,
        SchemaUtils.singletonSortedSet(EMR_CASE_IGNORE_NAME),
        EMPTY_STRING, false, SYNTAX_DIRECTORY_STRING_OID,
        SchemaUtils.RFC4512_ORIGIN, new CaseIgnoreEqualityMatchingRule(),
        false);
    builder.addMatchingRule(OMR_CASE_IGNORE_OID,
        SchemaUtils.singletonSortedSet(OMR_CASE_IGNORE_NAME),
        EMPTY_STRING, false,  SYNTAX_DIRECTORY_STRING_OID,
        SchemaUtils.RFC4512_ORIGIN, new CaseIgnoreOrderingMatchingRule(),
        false);
    builder.addMatchingRule(SMR_CASE_IGNORE_OID,
        SchemaUtils.singletonSortedSet(SMR_CASE_IGNORE_NAME),
        EMPTY_STRING, false, SYNTAX_SUBSTRING_ASSERTION_OID,
        SchemaUtils.RFC4512_ORIGIN, new CaseIgnoreSubstringMatchingRule(),
        false);
    builder.addMatchingRule(EMR_DIRECTORY_STRING_FIRST_COMPONENT_OID,
        SchemaUtils.singletonSortedSet(
            EMR_DIRECTORY_STRING_FIRST_COMPONENT_NAME), EMPTY_STRING, false,
        SYNTAX_DIRECTORY_STRING_OID,  SchemaUtils.RFC4512_ORIGIN,
        new DirectoryStringFirstComponentEqualityMatchingRule(), false);
    builder.addMatchingRule(EMR_DIRECTORY_STRING_FIRST_COMPONENT_OID,
        SchemaUtils.singletonSortedSet(
            EMR_DIRECTORY_STRING_FIRST_COMPONENT_NAME), EMPTY_STRING, false,
        SYNTAX_DIRECTORY_STRING_OID, SchemaUtils.RFC4512_ORIGIN,
        new DistinguishedNameEqualityMatchingRule(), false);
    builder.addMatchingRule(EMR_GENERALIZED_TIME_OID,
        SchemaUtils.singletonSortedSet(EMR_GENERALIZED_TIME_NAME),
        EMPTY_STRING, false, SYNTAX_GENERALIZED_TIME_OID,
        SchemaUtils.RFC4512_ORIGIN, new GeneralizedTimeEqualityMatchingRule(),
        false);
    builder.addMatchingRule(OMR_GENERALIZED_TIME_OID,
        SchemaUtils.singletonSortedSet(OMR_GENERALIZED_TIME_NAME),
        EMPTY_STRING, false, SYNTAX_GENERALIZED_TIME_OID,
        SchemaUtils.RFC4512_ORIGIN, new GeneralizedTimeOrderingMatchingRule(),
        false);
    builder.addMatchingRule(EMR_INTEGER_FIRST_COMPONENT_OID,
        SchemaUtils.singletonSortedSet(EMR_INTEGER_FIRST_COMPONENT_NAME),
        EMPTY_STRING, false, SYNTAX_INTEGER_OID, SchemaUtils.RFC4512_ORIGIN,
        new IntegerFirstComponentEqualityMatchingRule(), false);
    builder.addMatchingRule(EMR_INTEGER_OID,
        SchemaUtils.singletonSortedSet(EMR_INTEGER_NAME),
        EMPTY_STRING, false, SYNTAX_INTEGER_OID, SchemaUtils.RFC4512_ORIGIN,
        new IntegerEqualityMatchingRule(), false);
    builder.addMatchingRule(OMR_INTEGER_OID,
        SchemaUtils.singletonSortedSet(OMR_INTEGER_NAME),
        EMPTY_STRING, false, SYNTAX_INTEGER_OID, SchemaUtils.RFC4512_ORIGIN,
        new IntegerOrderingMatchingRule(), false);
    builder.addMatchingRule(EMR_KEYWORD_OID,
        SchemaUtils.singletonSortedSet(EMR_KEYWORD_NAME),
        EMPTY_STRING, false, SYNTAX_DIRECTORY_STRING_OID,
        SchemaUtils.RFC4512_ORIGIN, new KeywordEqualityMatchingRule(), false);
    builder.addMatchingRule(EMR_NUMERIC_STRING_OID,
        SchemaUtils.singletonSortedSet(EMR_NUMERIC_STRING_NAME),
        EMPTY_STRING, false, SYNTAX_NUMERIC_STRING_OID,
        SchemaUtils.RFC4512_ORIGIN, new NumericStringEqualityMatchingRule(),
        false);
    builder.addMatchingRule(OMR_NUMERIC_STRING_OID,
        SchemaUtils.singletonSortedSet(OMR_NUMERIC_STRING_NAME),
        EMPTY_STRING, false, SYNTAX_NUMERIC_STRING_OID,
        SchemaUtils.RFC4512_ORIGIN, new NumericStringOrderingMatchingRule(),
        false);
    builder.addMatchingRule(SMR_NUMERIC_STRING_OID,
        SchemaUtils.singletonSortedSet(SMR_NUMERIC_STRING_NAME),
        EMPTY_STRING, false, SYNTAX_SUBSTRING_ASSERTION_OID,
        SchemaUtils.RFC4512_ORIGIN, new NumericStringSubstringMatchingRule(),
        false);
    builder.addMatchingRule(EMR_OID_FIRST_COMPONENT_OID,
        SchemaUtils.singletonSortedSet(EMR_OID_FIRST_COMPONENT_NAME),
        EMPTY_STRING, false, SYNTAX_OID_OID, SchemaUtils.RFC4512_ORIGIN,
        new ObjectIdentifierFirstComponentEqualityMatchingRule(), false);
    builder.addMatchingRule(EMR_OID_OID,
        SchemaUtils.singletonSortedSet(EMR_OID_NAME),
        EMPTY_STRING, false, SYNTAX_OID_OID, SchemaUtils.RFC4512_ORIGIN,
        new ObjectIdentifierEqualityMatchingRule(), false);
    builder.addMatchingRule(EMR_OCTET_STRING_OID,
        SchemaUtils.singletonSortedSet(EMR_OCTET_STRING_NAME),
        EMPTY_STRING, false, SYNTAX_OCTET_STRING_OID,
        SchemaUtils.RFC4512_ORIGIN, new OctetStringEqualityMatchingRule(),
        false);
    builder.addMatchingRule(OMR_OCTET_STRING_OID,
        SchemaUtils.singletonSortedSet(OMR_OCTET_STRING_NAME),
        EMPTY_STRING, false, SYNTAX_OCTET_STRING_OID,
        SchemaUtils.RFC4512_ORIGIN, new OctetStringOrderingMatchingRule(),
        false);
    builder.addMatchingRule(EMR_TELEPHONE_OID,
        SchemaUtils.singletonSortedSet(EMR_TELEPHONE_NAME),
        EMPTY_STRING, false, SYNTAX_TELEPHONE_OID, SchemaUtils.RFC4512_ORIGIN,
        new TelephoneNumberEqualityMatchingRule(), false);
    builder.addMatchingRule(SMR_TELEPHONE_OID,
        SchemaUtils.singletonSortedSet(SMR_TELEPHONE_NAME),
        EMPTY_STRING, false, SYNTAX_SUBSTRING_ASSERTION_OID,
        SchemaUtils.RFC4512_ORIGIN, new TelephoneNumberSubstringMatchingRule(),
        false);
    builder.addMatchingRule(EMR_UNIQUE_MEMBER_OID,
        SchemaUtils.singletonSortedSet(EMR_UNIQUE_MEMBER_NAME),
        EMPTY_STRING, false, SYNTAX_NAME_AND_OPTIONAL_UID_OID,
        SchemaUtils.RFC4512_ORIGIN, new UniqueMemberEqualityMatchingRule(),
        false);
    builder.addMatchingRule(EMR_WORD_OID,
        SchemaUtils.singletonSortedSet(EMR_WORD_NAME),
        EMPTY_STRING, false, SYNTAX_DIRECTORY_STRING_OID,
        SchemaUtils.RFC4512_ORIGIN, new WordEqualityMatchingRule(), false);
  }

  private static void defaultAttributeTypes(SchemaBuilder builder)
      throws SchemaException
  {
    builder.addAttributeType("2.5.4.0",
            SchemaUtils.singletonSortedSet("objectClass"),
            EMPTY_STRING,
            false,
            null,
            "objectIdentifierMatch",
            null,
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.38",
            false,
            false,
            false,
            AttributeUsage.USER_APPLICATIONS,
            SchemaUtils.RFC4512_ORIGIN, false);

    builder.addAttributeType("2.5.4.1",
            SchemaUtils.singletonSortedSet("aliasedObjectName"),
            EMPTY_STRING,
            false,
            null,
            "distinguishedNameMatch",
            null,
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.12",
            true,
            false,
            false,
            AttributeUsage.DIRECTORY_OPERATION,
            SchemaUtils.RFC4512_ORIGIN, false);

    builder.addAttributeType("2.5.18.1",
            SchemaUtils.singletonSortedSet("createTimestamp"),
            EMPTY_STRING,
            false,
            null,
            "generalizedTimeMatch",
            "generalizedTimeOrderingMatch",
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.24",
            true,
            false,
            true,
            AttributeUsage.DIRECTORY_OPERATION,
            SchemaUtils.RFC4512_ORIGIN, false);

    builder.addAttributeType("2.5.18.2",
            SchemaUtils.singletonSortedSet("modifyTimestamp"),
            EMPTY_STRING,
            false,
            null,
            "generalizedTimeMatch",
            "generalizedTimeOrderingMatch",
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.24",
            true,
            false,
            true,
            AttributeUsage.DIRECTORY_OPERATION,
            SchemaUtils.RFC4512_ORIGIN, false);

    builder.addAttributeType("2.5.18.3",
            SchemaUtils.singletonSortedSet("creatorsName"),
            EMPTY_STRING,
            false,
            null,
            "distinguishedNameMatch",
            null,
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.12",
            true,
            false,
            true,
            AttributeUsage.DIRECTORY_OPERATION,
            SchemaUtils.RFC4512_ORIGIN, false);

    builder.addAttributeType("2.5.18.4",
            SchemaUtils.singletonSortedSet("modifiersName"),
            EMPTY_STRING,
            false,
            null,
            "distinguishedNameMatch",
            null,
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.12",
            true,
            false,
            true,
            AttributeUsage.DIRECTORY_OPERATION,
            SchemaUtils.RFC4512_ORIGIN, false);

    builder.addAttributeType("2.5.18.10",
            SchemaUtils.singletonSortedSet("subschemaSubentry"),
            EMPTY_STRING,
            false,
            null,
            "distinguishedNameMatch",
            null,
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.12",
            true,
            false,
            true,
            AttributeUsage.DIRECTORY_OPERATION,
            SchemaUtils.RFC4512_ORIGIN, false);

    builder.addAttributeType("2.5.21.5",
            SchemaUtils.singletonSortedSet("attributeTypes"),
            EMPTY_STRING,
            false,
            null,
            "objectIdentifierFirstComponentMatch",
            null,
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.3",
            false,
            false,
            false,
            AttributeUsage.DIRECTORY_OPERATION,
            SchemaUtils.RFC4512_ORIGIN, false);

    builder.addAttributeType("2.5.21.6",
            SchemaUtils.singletonSortedSet("objectClasses"),
            EMPTY_STRING,
            false,
            null,
            "objectIdentifierFirstComponentMatch",
            null,
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.37",
            false,
            false,
            false,
            AttributeUsage.DIRECTORY_OPERATION,
            SchemaUtils.RFC4512_ORIGIN, false);

    builder.addAttributeType("2.5.21.4",
            SchemaUtils.singletonSortedSet("matchingRules"),
            EMPTY_STRING,
            false,
            null,
            "objectIdentifierFirstComponentMatch",
            null,
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.30",
            false,
            false,
            false,
            AttributeUsage.DIRECTORY_OPERATION,
            SchemaUtils.RFC4512_ORIGIN, false);

    builder.addAttributeType("2.5.21.8",
            SchemaUtils.singletonSortedSet("matchingRuleUse"),
            EMPTY_STRING,
            false,
            null,
            "objectIdentifierFirstComponentMatch",
            null,
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.31",
            false,
            false,
            false,
            AttributeUsage.DIRECTORY_OPERATION,
            SchemaUtils.RFC4512_ORIGIN, false);

    builder.addAttributeType("2.5.21.9",
            SchemaUtils.singletonSortedSet("structuralObjectClass"),
            EMPTY_STRING,
            false,
            null,
            "objectIdentifierMatch",
            null,
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.38",
            true,
            false,
            true,
            AttributeUsage.DIRECTORY_OPERATION,
            SchemaUtils.RFC4512_ORIGIN, false);

    builder.addAttributeType("2.5.21.10",
            SchemaUtils.singletonSortedSet("governingStructureRule"),
            EMPTY_STRING,
            false,
            null,
            "integerMatch",
            null,
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.27",
            true,
            false,
            true,
            AttributeUsage.DIRECTORY_OPERATION,
            SchemaUtils.RFC4512_ORIGIN, false);

    builder.addAttributeType("1.3.6.1.4.1.1466.101.120.5",
            SchemaUtils.singletonSortedSet("namingContexts"),
            EMPTY_STRING,
            false,
            null,
            null,
            null,
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.12",
            false,
            false,
            false,
            AttributeUsage.DSA_OPERATION,
            SchemaUtils.RFC4512_ORIGIN, false);

    builder.addAttributeType("1.3.6.1.4.1.1466.101.120.6",
            SchemaUtils.singletonSortedSet("altServer"),
            EMPTY_STRING,
            false,
            null,
            null,
            null,
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.26",
            false,
            false,
            false,
            AttributeUsage.DSA_OPERATION,
            SchemaUtils.RFC4512_ORIGIN, false);

    builder.addAttributeType("1.3.6.1.4.1.1466.101.120.7",
            SchemaUtils.singletonSortedSet("supportedExtension"),
            EMPTY_STRING,
            false,
            null,
            null,
            null,
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.38",
            false,
            false,
            false,
            AttributeUsage.DSA_OPERATION,
            SchemaUtils.RFC4512_ORIGIN, false);

    builder.addAttributeType("1.3.6.1.4.1.1466.101.120.13",
            SchemaUtils.singletonSortedSet("supportedControl"),
            EMPTY_STRING,
            false,
            null,
            null,
            null,
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.38",
            false,
            false,
            false,
            AttributeUsage.DSA_OPERATION,
            SchemaUtils.RFC4512_ORIGIN, false);

    builder.addAttributeType("1.3.6.1.4.1.1466.101.120.14",
            SchemaUtils.singletonSortedSet("supportedSASLMechanisms"),
            EMPTY_STRING,
            false,
            null,
            null,
            null,
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.15",
            false,
            false,
            false,
            AttributeUsage.DSA_OPERATION,
            SchemaUtils.RFC4512_ORIGIN, false);

    builder.addAttributeType("1.3.6.1.4.1.4203.1.3.5",
            SchemaUtils.singletonSortedSet("supportedFeatures"),
            EMPTY_STRING,
            false,
            null,
            "objectIdentifierMatch",
            null,
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.38",
            false,
            false,
            false,
            AttributeUsage.DSA_OPERATION,
            SchemaUtils.RFC4512_ORIGIN, false);

    builder.addAttributeType("1.3.6.1.4.1.1466.101.120.15",
            SchemaUtils.singletonSortedSet("supportedLDAPVersion"),
            EMPTY_STRING,
            false,
            null,
            null,
            null,
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.27",
            false,
            false,
            false,
            AttributeUsage.DSA_OPERATION,
            SchemaUtils.RFC4512_ORIGIN, false);

    builder.addAttributeType("1.3.6.1.4.1.1466.101.120.16",
            SchemaUtils.singletonSortedSet("ldapSyntaxes"),
            EMPTY_STRING,
            false,
            null,
            "objectIdentifierFirstComponentMatch",
            null,
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.54",
            false,
            false,
            false,
            AttributeUsage.DIRECTORY_OPERATION,
            SchemaUtils.RFC4512_ORIGIN, false);

    builder.addAttributeType("2.5.21.1",
            SchemaUtils.singletonSortedSet("dITStructureRules"),
            EMPTY_STRING,
            false,
            null,
            "integerFirstComponentMatch",
            null,
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.17",
            false,
            false,
            false,
            AttributeUsage.DIRECTORY_OPERATION,
            SchemaUtils.RFC4512_ORIGIN, false);

    builder.addAttributeType("2.5.21.7",
            SchemaUtils.singletonSortedSet("nameForms"),
            EMPTY_STRING,
            false,
            null,
            "objectIdentifierFirstComponentMatch",
            null,
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.35",
            false,
            false,
            false,
            AttributeUsage.DIRECTORY_OPERATION,
            SchemaUtils.RFC4512_ORIGIN, false);

    builder.addAttributeType("2.5.21.2",
            SchemaUtils.singletonSortedSet("dITContentRules"),
            EMPTY_STRING,
            false,
            null,
            "objectIdentifierFirstComponentMatch",
            null,
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.16",
            false,
            false,
            false,
            AttributeUsage.DIRECTORY_OPERATION,
            SchemaUtils.RFC4512_ORIGIN, false);
  }

  private static void defaultObjectClasses(SchemaBuilder builder)
      throws SchemaException
  {
    builder.addObjectClass("2.5.6.0",
        SchemaUtils.singletonSortedSet("top"),
        EMPTY_STRING,
        false,
        EMPTY_STRING_SET,
        SchemaUtils.singletonSortedSet("objectClass"),
        EMPTY_STRING_SET,
        ObjectClassType.ABSTRACT,
        SchemaUtils.RFC4512_ORIGIN, false);

    builder.addObjectClass("2.5.6.1",
        SchemaUtils.singletonSortedSet("alias"),
        EMPTY_STRING,
        false,
        SchemaUtils.singletonSortedSet("top"),
        SchemaUtils.singletonSortedSet("aliasedObjectName"),
            EMPTY_STRING_SET,
        ObjectClassType.STRUCTURAL,
        SchemaUtils.RFC4512_ORIGIN, false);

    builder.addObjectClass("1.3.6.1.4.1.1466.101.120.111",
        SchemaUtils.singletonSortedSet("extensibleObject"),
        EMPTY_STRING,
        false,
        SchemaUtils.singletonSortedSet("top"),
        SchemaUtils.singletonSortedSet("aliasedObjectName"),
            EMPTY_STRING_SET,
        ObjectClassType.AUXILIARY,
        SchemaUtils.RFC4512_ORIGIN, false);

    Set<String> subschemaAttrs = new HashSet<String>();
    subschemaAttrs.add("dITStructureRules");
    subschemaAttrs.add("nameForms");
    subschemaAttrs.add("ditContentRules");
    subschemaAttrs.add("objectClasses");
    subschemaAttrs.add("attributeTypes");
    subschemaAttrs.add("matchingRules");
    subschemaAttrs.add("matchingRuleUse");

    builder.addObjectClass("2.5.20.1",
        SchemaUtils.singletonSortedSet("subschema"),
        EMPTY_STRING,
        false,
        SchemaUtils.singletonSortedSet("top"),
        EMPTY_STRING_SET,
        subschemaAttrs,
        ObjectClassType.AUXILIARY,
        SchemaUtils.RFC4512_ORIGIN, false);
  }
}
