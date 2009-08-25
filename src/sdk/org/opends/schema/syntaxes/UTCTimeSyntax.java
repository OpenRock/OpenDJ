package org.opends.schema.syntaxes;

import static org.opends.messages.SchemaMessages.*;
import static org.opends.server.loggers.debug.DebugLogger.debugEnabled;
import static org.opends.server.loggers.debug.DebugLogger.getTracer;
import static org.opends.server.schema.SchemaConstants.SYNTAX_UTC_TIME_NAME;
import static org.opends.server.util.ServerConstants.DATE_FORMAT_UTC_TIME;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.opends.ldap.DecodeException;
import org.opends.messages.Message;
import org.opends.messages.MessageBuilder;
import org.opends.schema.Schema;
import org.opends.server.loggers.debug.DebugTracer;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.DebugLogLevel;

/**
 * This class implements the UTC time attribute syntax.  This is very similar to
 * the generalized time syntax (and actually has been deprecated in favor of
 * that), but requires that the minute be provided and does not allow for
 * sub-second times.  All matching will be performed using the generalized time
 * matching rules, and equality, ordering, and substring matching will be
 * allowed.
 */
public class UTCTimeSyntax extends AbstractSyntaxImplementation
{
    /**
   * The tracer object for the debug logger.
   */
  private static final DebugTracer TRACER = getTracer();



  /**
   * The lock that will be used to provide threadsafe access to the date
   * formatter.
   */
  private final static Object dateFormatLock;



  /**
   * The date formatter that will be used to convert dates into UTC time values.
   * Note that all interaction with it must be synchronized.
   */
  private final static SimpleDateFormat dateFormat;

  /*
   * Create the date formatter that will be used to construct and parse
   * normalized UTC time values.
   */
  static
  {
    dateFormat = new SimpleDateFormat(DATE_FORMAT_UTC_TIME);
    dateFormat.setLenient(false);
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

    dateFormatLock = new Object();
  }

  public String getName() {
    return SYNTAX_UTC_TIME_NAME;
  }

  public boolean isHumanReadable() {
    return false;
  }



  /**
   * Indicates whether the provided value is acceptable for use in an attribute
   * with this syntax.  If it is not, then the reason may be appended to the
   * provided buffer.
   *
   * @param schema
   *@param  value          The value for which to make the determination.
   * @param  invalidReason  The buffer to which the invalid reason should be
 *                        appended.
 * @return  <CODE>true</CODE> if the provided value is acceptable for use with
   *          this syntax, or <CODE>false</CODE> if not.
   */
  public boolean valueIsAcceptable(Schema schema, ByteSequence value,
                                   MessageBuilder invalidReason)
  {
    // Get the value as a string and verify that it is at least long enough for
    // "YYYYMMDDhhmmZ", which is the shortest allowed value.
    String valueString = value.toString().toUpperCase();
    int    length      = valueString.length();
    if (length < 11)
    {
      Message message = ERR_ATTR_SYNTAX_UTC_TIME_TOO_SHORT.get(valueString);
      invalidReason.append(message);
      return false;
    }


    // The first two characters are the year, and they must be numeric digits
    // between 0 and 9.
    for (int i=0; i < 2; i++)
    {
      switch (valueString.charAt(i))
      {
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
          // These are all fine.
          break;
        default:
          Message message = ERR_ATTR_SYNTAX_UTC_TIME_INVALID_YEAR.get(
                  valueString, String.valueOf(valueString.charAt(i)));
          invalidReason.append(message);
          return false;
      }
    }


    // The next two characters are the month, and they must form the string
    // representation of an integer between 01 and 12.
    char m1 = valueString.charAt(2);
    char m2 = valueString.charAt(3);
    switch (m1)
    {
      case '0':
        // m2 must be a digit between 1 and 9.
        switch (m2)
        {
          case '1':
          case '2':
          case '3':
          case '4':
          case '5':
          case '6':
          case '7':
          case '8':
          case '9':
            // These are all fine.
            break;
          default:
            Message message = ERR_ATTR_SYNTAX_UTC_TIME_INVALID_MONTH.get(
                    valueString, valueString.substring(2, 4));
            invalidReason.append(message);
            return false;
        }
        break;
      case '1':
        // m2 must be a digit between 0 and 2.
        switch (m2)
        {
          case '0':
          case '1':
          case '2':
            // These are all fine.
            break;
          default:
            Message message = ERR_ATTR_SYNTAX_UTC_TIME_INVALID_MONTH.get(
                    valueString, valueString.substring(2, 4));
            invalidReason.append(message);
            return false;
        }
        break;
      default:
        Message message = ERR_ATTR_SYNTAX_UTC_TIME_INVALID_MONTH.get(
                valueString, valueString.substring(2, 4));
        invalidReason.append(message);
        return false;
    }


    // The next two characters should be the day of the month, and they must
    // form the string representation of an integer between 01 and 31.
    // This doesn't do any validation against the year or month, so it will
    // allow dates like April 31, or February 29 in a non-leap year, but we'll
    // let those slide.
    char d1 = valueString.charAt(4);
    char d2 = valueString.charAt(5);
    switch (d1)
    {
      case '0':
        // d2 must be a digit between 1 and 9.
        switch (d2)
        {
          case '1':
          case '2':
          case '3':
          case '4':
          case '5':
          case '6':
          case '7':
          case '8':
          case '9':
            // These are all fine.
            break;
          default:
            Message message = ERR_ATTR_SYNTAX_UTC_TIME_INVALID_DAY.get(
                    valueString, valueString.substring(4, 6));
            invalidReason.append(message);
            return false;
        }
        break;
      case '1':
        // Treated the same as '2'.
      case '2':
        // d2 must be a digit between 0 and 9.
        switch (d2)
        {
          case '0':
          case '1':
          case '2':
          case '3':
          case '4':
          case '5':
          case '6':
          case '7':
          case '8':
          case '9':
            // These are all fine.
            break;
          default:
            Message message = ERR_ATTR_SYNTAX_UTC_TIME_INVALID_DAY.get(
                    valueString, valueString.substring(4, 6));
            invalidReason.append(message);
            return false;
        }
        break;
      case '3':
        // d2 must be either 0 or 1.
        switch (d2)
        {
          case '0':
          case '1':
            // These are all fine.
            break;
          default:
            Message message = ERR_ATTR_SYNTAX_UTC_TIME_INVALID_DAY.get(
                    valueString, valueString.substring(4, 6));
            invalidReason.append(message);
            return false;
        }
        break;
      default:
        Message message = ERR_ATTR_SYNTAX_UTC_TIME_INVALID_DAY.get(valueString,
                                    valueString.substring(4, 6));
        invalidReason.append(message);
        return false;
    }


    // The next two characters must be the hour, and they must form the string
    // representation of an integer between 00 and 23.
    char h1 = valueString.charAt(6);
    char h2 = valueString.charAt(7);
    switch (h1)
    {
      case '0':
        // This is treated the same as '1'.
      case '1':
        switch (h2)
        {
          case '0':
          case '1':
          case '2':
          case '3':
          case '4':
          case '5':
          case '6':
          case '7':
          case '8':
          case '9':
            // These are all fine.
            break;
          default:
            Message message = ERR_ATTR_SYNTAX_UTC_TIME_INVALID_HOUR.get(
                    valueString, valueString.substring(6, 8));
            invalidReason.append(message);
            return false;
        }
        break;
      case '2':
        // This must be a digit between 0 and 3.
        switch (h2)
        {
          case '0':
          case '1':
          case '2':
          case '3':
            // These are all fine.
            break;
          default:
            Message message = ERR_ATTR_SYNTAX_UTC_TIME_INVALID_HOUR.get(
                    valueString, valueString.substring(6, 8));
            invalidReason.append(message);
            return false;
        }
        break;
      default:
        Message message = ERR_ATTR_SYNTAX_UTC_TIME_INVALID_HOUR.get(valueString,
                                    valueString.substring(6, 8));
        invalidReason.append(message);
        return false;
    }


    // Next, there should be two digits comprising an integer between 00 and 59
    // for the minute.
    m1 = valueString.charAt(8);
    switch (m1)
    {
      case '0':
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
        // There must be at least two more characters, and the next one must
        // be a digit between 0 and 9.
        if (length < 11)
        {
          Message message = ERR_ATTR_SYNTAX_UTC_TIME_INVALID_CHAR.get(
                  valueString, String.valueOf(m1), 8);
          invalidReason.append(message);
          return false;
        }

        switch (valueString.charAt(9))
        {
          case '0':
          case '1':
          case '2':
          case '3':
          case '4':
          case '5':
          case '6':
          case '7':
          case '8':
          case '9':
            // These are all fine.
            break;
          default:
            Message message = ERR_ATTR_SYNTAX_UTC_TIME_INVALID_MINUTE.get(
                    valueString, valueString.substring(8, 10));
            invalidReason.append(message);
            return false;
        }

        break;

      default:
        Message message = ERR_ATTR_SYNTAX_UTC_TIME_INVALID_CHAR.get(
                valueString, String.valueOf(m1), 8);
        invalidReason.append(message);
        return false;
    }


    // Next, there should be either two digits comprising an integer between 00
    // and 60 (for the second, including a possible leap second), a letter 'Z'
    // (for the UTC specifier), or a plus or minus sign followed by four digits
    // (for the UTC offset).
    char s1 = valueString.charAt(10);
    switch (s1)
    {
      case '0':
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
        // There must be at least two more characters, and the next one must
        // be a digit between 0 and 9.
        if (length < 13)
        {
          Message message = ERR_ATTR_SYNTAX_UTC_TIME_INVALID_CHAR.get(
                  valueString, String.valueOf(s1), 10);
          invalidReason.append(message);
          return false;
        }

        switch (valueString.charAt(11))
        {
          case '0':
          case '1':
          case '2':
          case '3':
          case '4':
          case '5':
          case '6':
          case '7':
          case '8':
          case '9':
            // These are all fine.
            break;
          default:
            Message message = ERR_ATTR_SYNTAX_UTC_TIME_INVALID_SECOND.get(
                    valueString, valueString.substring(10, 12));
            invalidReason.append(message);
            return false;
        }

        break;
      case '6':
        // There must be at least two more characters and the next one must be
        // a 0.
        if (length < 13)
        {

          Message message = ERR_ATTR_SYNTAX_UTC_TIME_INVALID_CHAR.get(
                  valueString, String.valueOf(s1), 10);
          invalidReason.append(message);
          return false;
        }

        if (valueString.charAt(11) != '0')
        {
          Message message = ERR_ATTR_SYNTAX_UTC_TIME_INVALID_SECOND.get(
                  valueString, valueString.substring(10, 12));
          invalidReason.append(message);
          return false;
        }

        break;
      case 'Z':
        // This is fine only if we are at the end of the value.
        if (length == 11)
        {
          return true;
        }
        else
        {
          Message message = ERR_ATTR_SYNTAX_UTC_TIME_INVALID_CHAR.get(
                  valueString, String.valueOf(s1), 10);
          invalidReason.append(message);
          return false;
        }

      case '+':
      case '-':
        // These are fine only if there are exactly four more digits that
        // specify a valid offset.
        if (length == 15)
        {
          return hasValidOffset(valueString, 11, invalidReason);
        }
        else
        {
          Message message = ERR_ATTR_SYNTAX_UTC_TIME_INVALID_CHAR.get(
                  valueString, String.valueOf(s1), 10);
          invalidReason.append(message);
          return false;
        }

      default:
        Message message = ERR_ATTR_SYNTAX_UTC_TIME_INVALID_CHAR.get(
                valueString, String.valueOf(s1), 10);
        invalidReason.append(message);
        return false;
    }


    // The last element should be either a letter 'Z' (for the UTC specifier),
    // or a plus or minus sign followed by four digits (for the UTC offset).
    switch (valueString.charAt(12))
    {
      case 'Z':
        // This is fine only if we are at the end of the value.
        if (length == 13)
        {
          return true;
        }
        else
        {
          Message message = ERR_ATTR_SYNTAX_UTC_TIME_INVALID_CHAR.get(
                  valueString, String.valueOf(valueString.charAt(12)), 12);
          invalidReason.append(message);
          return false;
        }

      case '+':
      case '-':
        // These are fine only if there are four or two more digits that
        // specify a valid offset.
        if ((length == 17) || (length == 15))
        {
          return hasValidOffset(valueString, 13, invalidReason);
        }
        else
        {
          Message message = ERR_ATTR_SYNTAX_UTC_TIME_INVALID_CHAR.get(
                  valueString, String.valueOf(valueString.charAt(12)), 12);
          invalidReason.append(message);
          return false;
        }

      default:
        Message message = ERR_ATTR_SYNTAX_UTC_TIME_INVALID_CHAR.get(
                valueString, String.valueOf(valueString.charAt(12)), 12);
        invalidReason.append(message);
        return false;
    }
  }



  /**
   * Indicates whether the provided string contains a valid set of two or four
   * UTC offset digits.  The provided string must have either two or four
   * characters from the provided start position to the end of the value.
   *
   * @param  value          The whole value, including the offset.
   * @param  startPos       The position of the first character that is
   *                        contained in the offset.
   * @param  invalidReason  The buffer to which the invalid reason may be
   *                        appended if the string does not contain a valid set
   *                        of UTC offset digits.
   *
   * @return  <CODE>true</CODE> if the provided offset string is valid, or
   *          <CODE>false</CODE> if it is not.
   */
  private boolean hasValidOffset(String value, int startPos,
                                 MessageBuilder invalidReason)
  {
    int offsetLength = value.length() - startPos;
    if (offsetLength < 2)
    {
      Message message = ERR_ATTR_SYNTAX_UTC_TIME_TOO_SHORT.get(value);
      invalidReason.append(message);
      return false;
    }

    // The first two characters must be an integer between 00 and 23.
    switch (value.charAt(startPos))
    {
      case '0':
      case '1':
        switch (value.charAt(startPos+1))
        {
          case '0':
          case '1':
          case '2':
          case '3':
          case '4':
          case '5':
          case '6':
          case '7':
          case '8':
          case '9':
            // These are all fine.
            break;
          default:
            Message message = ERR_ATTR_SYNTAX_UTC_TIME_INVALID_OFFSET.get(value,
                                        value.substring(startPos,
                                                        startPos+offsetLength));
            invalidReason.append(message);
            return false;
        }
        break;
      case '2':
        switch (value.charAt(startPos+1))
        {
          case '0':
          case '1':
          case '2':
          case '3':
            // These are all fine.
            break;
          default:
            Message message = ERR_ATTR_SYNTAX_UTC_TIME_INVALID_OFFSET.get(value,
                                        value.substring(startPos,
                                                        startPos+offsetLength));
            invalidReason.append(message);
            return false;
        }
        break;
      default:
        Message message = ERR_ATTR_SYNTAX_UTC_TIME_INVALID_OFFSET.get(value,
                                    value.substring(startPos,
                                                    startPos+offsetLength));
        invalidReason.append(message);
        return false;
    }


    // If there are two more characters, then they must be an integer between
    // 00 and 59.
    if (offsetLength == 4)
    {
      switch (value.charAt(startPos+2))
      {
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
          switch (value.charAt(startPos+3))
          {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
              // These are all fine.
              break;
            default:
              Message message =
                   ERR_ATTR_SYNTAX_UTC_TIME_INVALID_OFFSET.get(
                           value,value.substring(startPos,
                           startPos+offsetLength));
              invalidReason.append(message);
              return false;
          }
          break;
        default:
          Message message = ERR_ATTR_SYNTAX_UTC_TIME_INVALID_OFFSET.get(value,
                                      value.substring(startPos,
                                                      startPos+offsetLength));
          invalidReason.append(message);
          return false;
      }
    }

    return true;
  }


  /**
   * Retrieves an string containing a UTC time representation of the
   * provided date.
   *
   * @param  d  The date for which to retrieve the UTC time value.
   *
   * @return  The attribute value created from the date.
   */
  public static String createUTCTimeValue(Date d)
  {
    synchronized (dateFormatLock)
    {
      return dateFormat.format(d);
    }
  }



  /**
   * Decodes the provided normalized value as a UTC time value and
   * retrieves a Java <CODE>Date</CODE> object containing its representation.
   *
   * @param  valueString  The normalized UTC time value to decode to a
   *                          Java <CODE>Date</CODE>.
   *
   * @return  The Java <CODE>Date</CODE> created from the provided UTC time
   *          value.
   *
   * @throws DecodeException  If the provided value cannot be parsed as a
   *                              valid UTC time string.
   */
  public static Date decodeUTCTimeValue(String valueString)
         throws DecodeException
  {
    try
    {
      synchronized (dateFormatLock)
      {
        return dateFormat.parse(valueString);
      }
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message = ERR_ATTR_SYNTAX_UTC_TIME_CANNOT_PARSE.get(
          valueString, String.valueOf(e));
      throw new DecodeException(message, e);
    }
  }

}
