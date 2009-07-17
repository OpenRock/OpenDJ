package org.opends.schema;

import org.opends.ldap.DecodeException;
import org.opends.messages.Message;
import static org.opends.messages.SchemaMessages.*;
import static org.opends.server.util.StaticUtils.isDigit;
import static org.opends.server.util.StaticUtils.isAlpha;
import static org.opends.server.util.ServerConstants.SCHEMA_PROPERTY_ORIGIN;
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
      Message message =
          ERR_ATTR_SYNTAX_ILLEGAL_CHAR.
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
}
