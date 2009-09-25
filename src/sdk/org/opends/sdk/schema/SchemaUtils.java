package org.opends.sdk.schema;

import static org.opends.messages.SchemaMessages.*;
import static org.opends.sdk.util.StaticUtils.isAlpha;
import static org.opends.sdk.util.StaticUtils.isDigit;

import java.util.*;

import org.opends.messages.Message;
import org.opends.sdk.DecodeException;
import org.opends.sdk.util.SubstringReader;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 13, 2009
 * Time: 10:15:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class SchemaUtils
{
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

    try
    {
      char c;
      while((c = reader.read()) != ' ')
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

      if(length == 0)
      {
        Message message =
            ERR_ATTR_SYNTAX_OID_NO_VALUE.get();
        throw new DecodeException(message);
      }

      reader.reset();

      return reader.read(length);
    }
    catch(StringIndexOutOfBoundsException e)
    {
      Message message = ERR_ATTR_SYNTAX_TRUNCATED_VALUE.get();
      throw new DecodeException(message);
    }
  }

  public static List<String> readNameDescriptors(SubstringReader reader)
      throws DecodeException
  {
    int length = 0;
    List<String> values;

    // Skip over any spaces at the beginning of the value.
    reader.skipWhitespaces();

    try
    {
      char c = reader.read();
      if (c == '\'')
      {
        reader.mark();
        // Parse until the closing quote.
        while(reader.read() != '\'')
        {
          length ++;
        }


        reader.reset();
        values = Collections.singletonList(reader.read(length));
        reader.read();
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
          values = new LinkedList<String>();
          do
          {
            reader.reset();
            values.add(readQuotedDescriptor(reader));
            reader.skipWhitespaces();
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
    catch(StringIndexOutOfBoundsException e)
    {
      Message message = ERR_ATTR_SYNTAX_TRUNCATED_VALUE.get();
      throw new DecodeException(message);
    }
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

    try
    {
      // Read until we find the next space.
      char c;
      while((c = reader.read()) != ' ' && c != ')')
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
    catch(StringIndexOutOfBoundsException e)
    {
      Message message = ERR_ATTR_SYNTAX_TRUNCATED_VALUE.get();
      throw new DecodeException(message);
    }
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

    try
    {
      // The next character must be a single quote.
      char c = reader.read();
      if (c != '\'')
      {
        Message message = ERR_ATTR_SYNTAX_EXPECTED_QUOTE_AT_POS.get(
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

      String str = reader.read(length);
      reader.read();
      return str;
    }
    catch(StringIndexOutOfBoundsException e)
    {
      Message message = ERR_ATTR_SYNTAX_TRUNCATED_VALUE.get();
      throw new DecodeException(message);
    }
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

    try
    {
      // The next character must be a single quote.
      char c = reader.read();
      if (c != '\'')
      {
        Message message = ERR_ATTR_SYNTAX_EXPECTED_QUOTE_AT_POS.get(
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

        if(!isAlpha(c) && !isDigit(c) && c != '-' && c != '_' && c != '.')
        {
          // This is an illegal character.
          Message message = ERR_ATTR_SYNTAX_ILLEGAL_CHAR_IN_STRING_OID.
              get(String.valueOf(c), reader.pos() - 1);
          throw new DecodeException(message);
        }

        length ++;
      }

      reader.reset();

      String descr = reader.read(length);
      reader.read();
      return descr;
    }
    catch(StringIndexOutOfBoundsException e)
    {
      Message message = ERR_ATTR_SYNTAX_TRUNCATED_VALUE.get();
      throw new DecodeException(message);
    }
  }

  public static Set<String> readOIDs(SubstringReader reader)
      throws DecodeException
  {
    Set<String> values;

    // Skip over any spaces at the beginning of the value.
    reader.skipWhitespaces();
    reader.mark();

    try
    {
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
    catch(StringIndexOutOfBoundsException e)
    {
      Message message = ERR_ATTR_SYNTAX_TRUNCATED_VALUE.get();
      throw new DecodeException(message);
    }
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

    try
    {
      // The next character must be either numeric (for an OID) or alphabetic
      // (for an attribute description).
      char c = reader.read();
      if (isDigit(c))
      {
        reader.reset();
        return readNumericOID(reader);
      }

      else if (isAlpha(c))
      {
        // This must be an attribute description.  In this case, we will only
        // accept alphabetic characters, numeric digits, and the hyphen.
        while(reader.remaining() > 0 && (c = reader.read()) != ' ' && c != ')')
        {
          if(length == 0 && !isAlpha(c))
          {
            // This is an illegal character.
            Message message = ERR_ATTR_SYNTAX_ILLEGAL_CHAR_IN_STRING_OID.
                get(String.valueOf(c), reader.pos() - 1);
            throw new DecodeException(message);
          }

          if(!isAlpha(c) && !isDigit(c) && c != '-' && c != '.' && c != '_')
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
    catch(StringIndexOutOfBoundsException e)
    {
      Message message = ERR_ATTR_SYNTAX_TRUNCATED_VALUE.get();
      throw new DecodeException(message);
    }
  }

  /**
   * Reads the next OID from the definition, skipping over
   * any leading spaces. The OID may be followed by a integer length in
   * brackets.
   *
   * @param  reader The string representation of the definition.
   *
   * @return  The OID read from the definition.
   *
   * @throws  DecodeException  If a problem is encountered while reading the
   *                              token name.
   */
  public static String readOIDLen(SubstringReader reader)
          throws DecodeException
  {
    int length = 1;

    // Skip over any spaces at the beginning of the value.
    reader.skipWhitespaces();
    reader.mark();

    try
    {
      // The next character must be either numeric (for an OID) or alphabetic
      // (for an attribute description).
      char c = reader.read();
      if (isDigit(c))
      {
        boolean lastWasPeriod = false;
        while((c = reader.read()) != ' ' && c != '{')
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

        if(length == 0)
        {
          Message message =
              ERR_ATTR_SYNTAX_OID_NO_VALUE.get();
          throw new DecodeException(message);
        }
      }

      else if (isAlpha(c))
      {
        // This must be an attribute description.  In this case, we will only
        // accept alphabetic characters, numeric digits, and the hyphen.
        while((c = reader.read()) != ' ' && c != ')' && c != '{')
        {
          if(length == 0 && !isAlpha(c))
          {
            // This is an illegal character.
            Message message = ERR_ATTR_SYNTAX_ILLEGAL_CHAR_IN_STRING_OID.
                get(String.valueOf(c), reader.pos() - 1);
            throw new DecodeException(message);
          }

          if(!isAlpha(c) && !isDigit(c) && c != '-' && c != '.' && c != '_')
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
      String oid = reader.read(length);

      reader.mark();
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
      else
      {
        reader.reset();
      }

      return oid;
    }
    catch(StringIndexOutOfBoundsException e)
    {
      Message message = ERR_ATTR_SYNTAX_TRUNCATED_VALUE.get();
      throw new DecodeException(message);
    }
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
  public static List<String> readExtensions(SubstringReader reader)
          throws DecodeException
  {
    int length = 0;
    List<String> values;

    // Skip over any leading spaces.
    reader.skipWhitespaces();
    reader.mark();

    try
    {
      // Look at the next character.  If it is a quote, then parse until the next
      // quote and end.  If it is an open parenthesis, then parse individual
      // values until the close parenthesis and end.  Otherwise, parse until the
      // next space and end.
      char c = reader.read();
      if (c == '\'')
      {
        reader.mark();
        // Parse until the closing quote.
        while(reader.read() != '\'')
        {
          length ++;
        }


        reader.reset();
        values = Collections.singletonList(reader.read(length));
        reader.read();
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
            reader.skipWhitespaces();
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
    catch(StringIndexOutOfBoundsException e)
    {
      Message message = ERR_ATTR_SYNTAX_TRUNCATED_VALUE.get();
      throw new DecodeException(message);
    }
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

    try
    {
      while(reader.read() != ' ')
      {
        length ++;
      }

      if(length == 0)
      {
        Message message =
            ERR_ATTR_SYNTAX_RULE_ID_NO_VALUE.get();
        throw new DecodeException(message);
      }

      reader.reset();
      String ruleID = reader.read(length);

      try
      {
        return Integer.valueOf(ruleID);
      }
      catch(NumberFormatException e)
      {
        Message message = ERR_ATTR_SYNTAX_RULE_ID_INVALID.get(ruleID);
        throw new DecodeException(message);
      }
    }
    catch(StringIndexOutOfBoundsException e)
    {
      Message message = ERR_ATTR_SYNTAX_TRUNCATED_VALUE.get();
      throw new DecodeException(message);
    }
  }

  public static Set<Integer> readRuleIDs(SubstringReader reader)
      throws DecodeException
  {
    Set<Integer> values;

    // Skip over any spaces at the beginning of the value.
    reader.skipWhitespaces();
    reader.mark();

    try
    {
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
    catch(StringIndexOutOfBoundsException e)
    {
      Message message = ERR_ATTR_SYNTAX_TRUNCATED_VALUE.get();
      throw new DecodeException(message);
    }
  }
}
