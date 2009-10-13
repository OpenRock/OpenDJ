package org.opends.sdk.schema.syntaxes;

import static org.opends.messages.SchemaMessages.*;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.opends.messages.Message;
import org.opends.messages.MessageBuilder;
import org.opends.sdk.DecodeException;
import org.opends.sdk.util.StaticUtils;
import org.opends.sdk.schema.Schema;
import org.opends.server.types.ByteSequence;
import static org.opends.sdk.schema.SchemaConstants.*;

/**
 * This class implements the fax attribute syntax.  This should be restricted to
 * holding only fax message contents, but we will accept any set of bytes.  It
 * will be treated much like the octet string attribute syntax.
 */
public class GeneralizedTimeSyntax extends AbstractSyntaxImplementation
{

  // UTC TimeZone is assumed to never change over JVM lifetime
  private static final TimeZone TIME_ZONE_UTC_OBJ =
      TimeZone.getTimeZone(TIME_ZONE_UTC);

  public String getName() {
    return SYNTAX_GENERALIZED_TIME_NAME;
  }

  public boolean isHumanReadable() {
    return true;
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
    try
    {
      decodeGeneralizedTimeValue(value);
      return true;
    }
    catch (DecodeException de)
    {
      invalidReason.append(de.getMessageObject());
      return false;
    }
  }



  /**
   * Retrieves the generalized time representation of the provided date.
   *
   * @param  d  The date to retrieve in generalized time form.
   *
   * @return  The generalized time representation of the provided date.
   */
  public static String format(Date d)
  {
    return d == null ? null : format(d.getTime());
  }



  /**
   * Retrieves the generalized time representation of the provided date.
   *
   * @param  t  The timestamp to retrieve in generalized time form.
   *
   * @return  The generalized time representation of the provided date.
   */
  public static String format(long t)
  {
    // Generalized time has the format yyyyMMddHHmmss.SSS'Z'

    // Do this in a thread-safe non-synchronized fashion.
    // (Simple)DateFormat is neither fast nor thread-safe.

    StringBuilder sb = new StringBuilder(19);

    GregorianCalendar calendar = new GregorianCalendar(TIME_ZONE_UTC_OBJ);
    calendar.setLenient(false);
    calendar.setTimeInMillis(t);

    // Format the year yyyy.
    int n = calendar.get(Calendar.YEAR);
    if (n < 0)
    {
      IllegalArgumentException e =
          new IllegalArgumentException("Year cannot be < 0:" + n);
      StaticUtils.DEBUG_LOG.throwing(
          "GeneralizedTimeSyntax",  "format", e);
      throw e;
    }
    else if (n < 10)
    {
      sb.append("000");
    }
    else if (n < 100)
    {
      sb.append("00");
    }
    else if (n < 1000)
    {
      sb.append("0");
    }
    sb.append(n);

    // Format the month MM.
    n = calendar.get(Calendar.MONTH) + 1;
    if (n < 10)
    {
      sb.append("0");
    }
    sb.append(n);

    // Format the day dd.
    n = calendar.get(Calendar.DAY_OF_MONTH);
    if (n < 10)
    {
      sb.append("0");
    }
    sb.append(n);

    // Format the hour HH.
    n = calendar.get(Calendar.HOUR_OF_DAY);
    if (n < 10)
    {
      sb.append("0");
    }
    sb.append(n);

    // Format the minute mm.
    n = calendar.get(Calendar.MINUTE);
    if (n < 10)
    {
      sb.append("0");
    }
    sb.append(n);

    // Format the seconds ss.
    n = calendar.get(Calendar.SECOND);
    if (n < 10)
    {
      sb.append("0");
    }
    sb.append(n);

    // Format the milli-seconds.
    sb.append('.');
    n = calendar.get(Calendar.MILLISECOND);
    if (n < 10)
    {
      sb.append("00");
    }
    else if (n < 100)
    {
      sb.append("0");
    }
    sb.append(n);

    // Format the timezone (always Z).
    sb.append('Z');

    return sb.toString();
  }



  /**
   * Decodes the provided normalized value as a generalized time value and
   * retrieves a timestamp containing its representation.
   *
   * @param  value  The normalized value to decode using the generalized time
   *                syntax.
   *
   * @return  The timestamp created from the provided generalized time value.
   *
   * @throws  DecodeException  If the provided value cannot be parsed as a
   *                              valid generalized time string.
   */
  public static long decodeGeneralizedTimeValue(ByteSequence value)
      throws DecodeException
  {
    int year        = 0;
    int month       = 0;
    int day         = 0;
    int hour        = 0;
    int minute      = 0;
    int second      = 0;


    // Get the value as a string and verify that it is at least long enough for
    // "YYYYMMDDhhZ", which is the shortest allowed value.
    String valueString = value.toString().toUpperCase();
    int    length      = valueString.length();
    if (length < 11)
    {
      Message message =
          WARN_ATTR_SYNTAX_GENERALIZED_TIME_TOO_SHORT.get(valueString);
      DecodeException e = new DecodeException(message);
      StaticUtils.DEBUG_LOG.throwing(
          "GeneralizedTimeSyntax",  "valueIsAcceptable", e);
      throw e;
    }


    // The first four characters are the century and year, and they must be
    // numeric digits between 0 and 9.
    for (int i=0; i < 4; i++)
    {
      switch (valueString.charAt(i))
      {
        case '0':
          year = (year * 10);
          break;

        case '1':
          year = (year * 10) + 1;
          break;

        case '2':
          year = (year * 10) + 2;
          break;

        case '3':
          year = (year * 10) + 3;
          break;

        case '4':
          year = (year * 10) + 4;
          break;

        case '5':
          year = (year * 10) + 5;
          break;

        case '6':
          year = (year * 10) + 6;
          break;

        case '7':
          year = (year * 10) + 7;
          break;

        case '8':
          year = (year * 10) + 8;
          break;

        case '9':
          year = (year * 10) + 9;
          break;

        default:
          Message message = WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_YEAR.get(
              valueString, String.valueOf(valueString.charAt(i)));
          DecodeException e = new DecodeException(message);
          StaticUtils.DEBUG_LOG.throwing(
              "GeneralizedTimeSyntax",  "valueIsAcceptable", e);
          throw e;
      }
    }


    // The next two characters are the month, and they must form the string
    // representation of an integer between 01 and 12.
    char m1 = valueString.charAt(4);
    char m2 = valueString.charAt(5);
    switch (m1)
    {
      case '0':
        // m2 must be a digit between 1 and 9.
        switch (m2)
        {
          case '1':
            month = Calendar.JANUARY;
            break;

          case '2':
            month = Calendar.FEBRUARY;
            break;

          case '3':
            month = Calendar.MARCH;
            break;

          case '4':
            month = Calendar.APRIL;
            break;

          case '5':
            month = Calendar.MAY;
            break;

          case '6':
            month = Calendar.JUNE;
            break;

          case '7':
            month = Calendar.JULY;
            break;

          case '8':
            month = Calendar.AUGUST;
            break;

          case '9':
            month = Calendar.SEPTEMBER;
            break;

          default:
            Message message =
                WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_MONTH.get(valueString,
                                                                    valueString.substring(4, 6));
            DecodeException e = new DecodeException(message);
            StaticUtils.DEBUG_LOG.throwing(
                "GeneralizedTimeSyntax",  "valueIsAcceptable", e);
            throw e;
        }
        break;
      case '1':
        // m2 must be a digit between 0 and 2.
        switch (m2)
        {
          case '0':
            month = Calendar.OCTOBER;
            break;

          case '1':
            month = Calendar.NOVEMBER;
            break;

          case '2':
            month = Calendar.DECEMBER;
            break;

          default:
            Message message =
                WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_MONTH.get(valueString,
                                                                    valueString.substring(4, 6));
            DecodeException e = new DecodeException(message);
            StaticUtils.DEBUG_LOG.throwing(
                "GeneralizedTimeSyntax",  "valueIsAcceptable", e);
            throw e;
        }
        break;
      default:
        Message message =
            WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_MONTH.get(valueString,
                                                                valueString.substring(4, 6));
        DecodeException e = new DecodeException(message);
        StaticUtils.DEBUG_LOG.throwing(
            "GeneralizedTimeSyntax",  "valueIsAcceptable", e);
        throw e;
    }


    // The next two characters should be the day of the month, and they must
    // form the string representation of an integer between 01 and 31.
    // This doesn't do any validation against the year or month, so it will
    // allow dates like April 31, or February 29 in a non-leap year, but we'll
    // let those slide.
    char d1 = valueString.charAt(6);
    char d2 = valueString.charAt(7);
    switch (d1)
    {
      case '0':
        // d2 must be a digit between 1 and 9.
        switch (d2)
        {
          case '1':
            day = 1;
            break;

          case '2':
            day = 2;
            break;

          case '3':
            day = 3;
            break;

          case '4':
            day = 4;
            break;

          case '5':
            day = 5;
            break;

          case '6':
            day = 6;
            break;

          case '7':
            day = 7;
            break;

          case '8':
            day = 8;
            break;

          case '9':
            day = 9;
            break;

          default:
            Message message =
                WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_DAY.get(valueString,
                                                                  valueString.substring(6, 8));
            DecodeException e = new DecodeException(message);
            StaticUtils.DEBUG_LOG.throwing(
                "GeneralizedTimeSyntax",  "valueIsAcceptable", e);
            throw e;
        }
        break;

      case '1':
        // d2 must be a digit between 0 and 9.
        switch (d2)
        {
          case '0':
            day = 10;
            break;

          case '1':
            day = 11;
            break;

          case '2':
            day = 12;
            break;

          case '3':
            day = 13;
            break;

          case '4':
            day = 14;
            break;

          case '5':
            day = 15;
            break;

          case '6':
            day = 16;
            break;

          case '7':
            day = 17;
            break;

          case '8':
            day = 18;
            break;

          case '9':
            day = 19;
            break;

          default:
            Message message =
                WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_DAY.get(valueString,
                                                                  valueString.substring(6, 8));
            DecodeException e = new DecodeException(message);
            StaticUtils.DEBUG_LOG.throwing(
                "GeneralizedTimeSyntax",  "valueIsAcceptable", e);
            throw e;
        }
        break;

      case '2':
        // d2 must be a digit between 0 and 9.
        switch (d2)
        {
          case '0':
            day = 20;
            break;

          case '1':
            day = 21;
            break;

          case '2':
            day = 22;
            break;

          case '3':
            day = 23;
            break;

          case '4':
            day = 24;
            break;

          case '5':
            day = 25;
            break;

          case '6':
            day = 26;
            break;

          case '7':
            day = 27;
            break;

          case '8':
            day = 28;
            break;

          case '9':
            day = 29;
            break;

          default:
            Message message =
                WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_DAY.get(valueString,
                                                                  valueString.substring(6, 8));
            DecodeException e = new DecodeException(message);
            StaticUtils.DEBUG_LOG.throwing(
                "GeneralizedTimeSyntax",  "valueIsAcceptable", e);
            throw e;
        }
        break;

      case '3':
        // d2 must be either 0 or 1.
        switch (d2)
        {
          case '0':
            day = 30;
            break;

          case '1':
            day = 31;
            break;

          default:
            Message message =
                WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_DAY.get(valueString,
                                                                  valueString.substring(6, 8));
            DecodeException e = new DecodeException(message);
            StaticUtils.DEBUG_LOG.throwing(
                "GeneralizedTimeSyntax",  "valueIsAcceptable", e);
            throw e;
        }
        break;

      default:
        Message message =
            WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_DAY.get(valueString,
                                                              valueString.substring(6, 8));
        DecodeException e = new DecodeException(message);
        StaticUtils.DEBUG_LOG.throwing(
            "GeneralizedTimeSyntax",  "valueIsAcceptable", e);
        throw e;
    }


    // The next two characters must be the hour, and they must form the string
    // representation of an integer between 00 and 23.
    char h1 = valueString.charAt(8);
    char h2 = valueString.charAt(9);
    switch (h1)
    {
      case '0':
        switch (h2)
        {
          case '0':
            hour = 0;
            break;

          case '1':
            hour = 1;
            break;

          case '2':
            hour = 2;
            break;

          case '3':
            hour = 3;
            break;

          case '4':
            hour = 4;
            break;

          case '5':
            hour = 5;
            break;

          case '6':
            hour = 6;
            break;

          case '7':
            hour = 7;
            break;

          case '8':
            hour = 8;
            break;

          case '9':
            hour = 9;
            break;

          default:
            Message message =
                WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_HOUR.get(valueString,
                                                                   valueString.substring(8, 10));
            DecodeException e = new DecodeException(message);
            StaticUtils.DEBUG_LOG.throwing(
                "GeneralizedTimeSyntax",  "valueIsAcceptable", e);
            throw e;
        }
        break;

      case '1':
        switch (h2)
        {
          case '0':
            hour = 10;
            break;

          case '1':
            hour = 11;
            break;

          case '2':
            hour = 12;
            break;

          case '3':
            hour = 13;
            break;

          case '4':
            hour = 14;
            break;

          case '5':
            hour = 15;
            break;

          case '6':
            hour = 16;
            break;

          case '7':
            hour = 17;
            break;

          case '8':
            hour = 18;
            break;

          case '9':
            hour = 19;
            break;

          default:
            Message message =
                WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_HOUR.get(valueString,
                                                                   valueString.substring(8, 10));
            DecodeException e = new DecodeException(message);
            StaticUtils.DEBUG_LOG.throwing(
                "GeneralizedTimeSyntax",  "valueIsAcceptable", e);
            throw e;
        }
        break;

      case '2':
        switch (h2)
        {
          case '0':
            hour = 20;
            break;

          case '1':
            hour = 21;
            break;

          case '2':
            hour = 22;
            break;

          case '3':
            hour = 23;
            break;

          default:
            Message message =
                WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_HOUR.get(valueString,
                                                                   valueString.substring(8, 10));
            DecodeException e = new DecodeException(message);
            StaticUtils.DEBUG_LOG.throwing(
                "GeneralizedTimeSyntax",  "valueIsAcceptable", e);
            throw e;
        }
        break;

      default:
        Message message =
            WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_HOUR.get(valueString,
                                                               valueString.substring(8, 10));
        DecodeException e = new DecodeException(message);
        StaticUtils.DEBUG_LOG.throwing(
            "GeneralizedTimeSyntax",  "valueIsAcceptable", e);
        throw e;
    }


    // Next, there should be either two digits comprising an integer between 00
    // and 59 (for the minute), a letter 'Z' (for the UTC specifier), a plus
    // or minus sign followed by two or four digits (for the UTC offset), or a
    // period or comma representing the fraction.
    m1 = valueString.charAt(10);
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
        if (length < 13)
        {
          Message message = WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_CHAR.get(
              valueString, String.valueOf(m1), 10);
          DecodeException e = new DecodeException(message);
          StaticUtils.DEBUG_LOG.throwing(
              "GeneralizedTimeSyntax",  "valueIsAcceptable", e);
          throw e;
        }


        minute = 10 * (m1 - '0');

        switch (valueString.charAt(11))
        {
          case '0':
            break;

          case '1':
            minute += 1;
            break;

          case '2':
            minute += 2;
            break;

          case '3':
            minute += 3;
            break;

          case '4':
            minute += 4;
            break;

          case '5':
            minute += 5;
            break;

          case '6':
            minute += 6;
            break;

          case '7':
            minute += 7;
            break;

          case '8':
            minute += 8;
            break;

          case '9':
            minute += 9;
            break;

          default:
            Message message = WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_MINUTE.
                get(valueString,
                    valueString.substring(10, 12));
            DecodeException e = new DecodeException(message);
            StaticUtils.DEBUG_LOG.throwing(
                "GeneralizedTimeSyntax",  "valueIsAcceptable", e);
            throw e;
        }

        break;

      case 'Z':
        // This is fine only if we are at the end of the value.
        if (length == 11)
        {
          try
          {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setLenient(false);
            calendar.setTimeZone(TIME_ZONE_UTC_OBJ);
            calendar.set(year, month, day, hour, minute, second);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTimeInMillis();
          }
          catch (Exception e)
          {
            // This should only happen if the provided date wasn't legal
            // (e.g., September 31).
            Message message = WARN_ATTR_SYNTAX_GENERALIZED_TIME_ILLEGAL_TIME.
                get(valueString, String.valueOf(e));
            DecodeException de = new DecodeException(message, e);
            StaticUtils.DEBUG_LOG.throwing(
                "GeneralizedTimeSyntax",  "valueIsAcceptable", de);
            throw de;
          }
        }
        else
        {
          Message message = WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_CHAR.get(
              valueString, String.valueOf(m1), 10);
          DecodeException e = new DecodeException(message);
          StaticUtils.DEBUG_LOG.throwing(
              "GeneralizedTimeSyntax",  "valueIsAcceptable", e);
          throw e;
        }

      case '+':
      case '-':
        // These are fine only if there are exactly two or four more digits that
        // specify a valid offset.
        if ((length == 13) || (length == 15))
        {
          try
          {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setLenient(false);
            calendar.setTimeZone(getTimeZoneForOffset(valueString, 10));
            calendar.set(year, month, day, hour, minute, second);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTimeInMillis();
          }
          catch (Exception e)
          {

            // This should only happen if the provided date wasn't legal
            // (e.g., September 31).
            Message message = WARN_ATTR_SYNTAX_GENERALIZED_TIME_ILLEGAL_TIME.
                get(valueString, String.valueOf(e));
            DecodeException de = new DecodeException(message, e);
            StaticUtils.DEBUG_LOG.throwing(
                "GeneralizedTimeSyntax",  "valueIsAcceptable", de);
            throw de;
          }
        }
        else
        {
          Message message = WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_CHAR.get(
              valueString, String.valueOf(m1), 10);
          DecodeException e = new DecodeException(message);
          StaticUtils.DEBUG_LOG.throwing(
              "GeneralizedTimeSyntax",  "valueIsAcceptable", e);
          throw e;
        }

      case '.':
      case ',':
        return finishDecodingFraction(valueString, 11, year, month, day, hour,
                                      minute, second, 3600000);

      default:
        Message message = WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_CHAR.get(
            valueString, String.valueOf(m1), 10);
        DecodeException e = new DecodeException(message);
        StaticUtils.DEBUG_LOG.throwing(
            "GeneralizedTimeSyntax",  "valueIsAcceptable", e);
        throw e;
    }


    // Next, there should be either two digits comprising an integer between 00
    // and 60 (for the second, including a possible leap second), a letter 'Z'
    // (for the UTC specifier), a plus or minus sign followed by two or four
    // digits (for the UTC offset), or a period or comma to start the fraction.
    char s1 = valueString.charAt(12);
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
        if (length < 15)
        {
          Message message = WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_CHAR.get(
              valueString, String.valueOf(s1), 12);
          DecodeException e = new DecodeException(message);
          StaticUtils.DEBUG_LOG.throwing(
              "GeneralizedTimeSyntax",  "valueIsAcceptable", e);
          throw e;
        }


        second = 10 * (s1 - '0');

        switch (valueString.charAt(13))
        {
          case '0':
            break;

          case '1':
            second += 1;
            break;

          case '2':
            second += 2;
            break;

          case '3':
            second += 3;
            break;

          case '4':
            second += 4;
            break;

          case '5':
            second += 5;
            break;

          case '6':
            second += 6;
            break;

          case '7':
            second += 7;
            break;

          case '8':
            second += 8;
            break;

          case '9':
            second += 9;
            break;

          default:
            Message message = WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_MINUTE.
                get(valueString,
                    valueString.substring(12, 14));
            DecodeException e = new DecodeException(message);
            StaticUtils.DEBUG_LOG.throwing(
                "GeneralizedTimeSyntax",  "valueIsAcceptable", e);
            throw e;
        }

        break;

      case '6':
        // There must be at least two more characters and the next one must be
        // a 0.
        if (length < 15)
        {
          Message message = WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_CHAR.get(
              valueString, String.valueOf(s1), 12);
          DecodeException e = new DecodeException(message);
          StaticUtils.DEBUG_LOG.throwing(
              "GeneralizedTimeSyntax",  "valueIsAcceptable", e);
          throw e;
        }

        if (valueString.charAt(13) != '0')
        {
          Message message =
              WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_SECOND.get(valueString,
                                                                   valueString.substring(12, 14));
          DecodeException e = new DecodeException(message);
          StaticUtils.DEBUG_LOG.throwing(
              "GeneralizedTimeSyntax",  "valueIsAcceptable", e);
          throw e;
        }

        second = 60;
        break;

      case 'Z':
        // This is fine only if we are at the end of the value.
        if (length == 13)
        {
          try
          {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setLenient(false);
            calendar.setTimeZone(TIME_ZONE_UTC_OBJ);
            calendar.set(year, month, day, hour, minute, second);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTimeInMillis();
          }
          catch (Exception e)
          {

            // This should only happen if the provided date wasn't legal
            // (e.g., September 31).
            Message message = WARN_ATTR_SYNTAX_GENERALIZED_TIME_ILLEGAL_TIME.
                get(valueString, String.valueOf(e));
            DecodeException de = new DecodeException(message, e);
            StaticUtils.DEBUG_LOG.throwing(
                "GeneralizedTimeSyntax",  "valueIsAcceptable", de);
            throw de;
          }
        }
        else
        {
          Message message = WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_CHAR.get(
              valueString, String.valueOf(s1), 12);
          DecodeException e = new DecodeException(message);
          StaticUtils.DEBUG_LOG.throwing(
              "GeneralizedTimeSyntax",  "valueIsAcceptable", e);
          throw e;
        }

      case '+':
      case '-':
        // These are fine only if there are exactly two or four more digits that
        // specify a valid offset.
        if ((length == 15) || (length == 17))
        {
          try
          {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setLenient(false);
            calendar.setTimeZone(getTimeZoneForOffset(valueString, 12));
            calendar.set(year, month, day, hour, minute, second);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTimeInMillis();
          }
          catch (Exception e)
          {

            // This should only happen if the provided date wasn't legal
            // (e.g., September 31).
            Message message = WARN_ATTR_SYNTAX_GENERALIZED_TIME_ILLEGAL_TIME.
                get(valueString, String.valueOf(e));
            DecodeException de = new DecodeException(message, e);
            StaticUtils.DEBUG_LOG.throwing(
                "GeneralizedTimeSyntax",  "valueIsAcceptable", de);
            throw de;
          }
        }
        else
        {
          Message message = WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_CHAR.get(
              valueString, String.valueOf(s1), 12);
          DecodeException e = new DecodeException(message);
          StaticUtils.DEBUG_LOG.throwing(
              "GeneralizedTimeSyntax",  "valueIsAcceptable", e);
          throw e;
        }

      case '.':
      case ',':
        return finishDecodingFraction(valueString, 13, year, month, day, hour,
                                      minute, second, 60000);

      default:
        Message message = WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_CHAR.get(
            valueString, String.valueOf(s1), 12);
        DecodeException e = new DecodeException(message);
        StaticUtils.DEBUG_LOG.throwing(
            "GeneralizedTimeSyntax",  "valueIsAcceptable", e);
        throw e;
    }


    // Next, there should be either a period or comma followed by between one
    // and three digits (to specify the sub-second), a letter 'Z' (for the UTC
    // specifier), or a plus or minus sign followed by two our four digits (for
    // the UTC offset).
    switch (valueString.charAt(14))
    {
      case '.':
      case ',':
        return finishDecodingFraction(valueString, 15, year, month, day, hour,
                                      minute, second, 1000);

      case 'Z':
        // This is fine only if we are at the end of the value.
        if (length == 15)
        {
          try
          {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setLenient(false);
            calendar.setTimeZone(TIME_ZONE_UTC_OBJ);
            calendar.set(year, month, day, hour, minute, second);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTimeInMillis();
          }
          catch (Exception e)
          {
            // This should only happen if the provided date wasn't legal
            // (e.g., September 31).
            Message message = WARN_ATTR_SYNTAX_GENERALIZED_TIME_ILLEGAL_TIME.
                get(valueString, String.valueOf(e));
            DecodeException de = new DecodeException(message, e);
            StaticUtils.DEBUG_LOG.throwing(
                "GeneralizedTimeSyntax",  "valueIsAcceptable", de);
            throw de;
          }
        }
        else
        {
          Message message = WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_CHAR.get(
              valueString, String.valueOf(valueString.charAt(14)), 14);
          DecodeException e = new DecodeException(message);
          StaticUtils.DEBUG_LOG.throwing(
              "GeneralizedTimeSyntax",  "valueIsAcceptable", e);
          throw e;
        }

      case '+':
      case '-':
        // These are fine only if there are exactly two or four more digits that
        // specify a valid offset.
        if ((length == 17) || (length == 19))
        {
          try
          {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setLenient(false);
            calendar.setTimeZone(getTimeZoneForOffset(valueString, 14));
            calendar.set(year, month, day, hour, minute, second);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTimeInMillis();
          }
          catch (Exception e)
          {
            // This should only happen if the provided date wasn't legal
            // (e.g., September 31).
            Message message = WARN_ATTR_SYNTAX_GENERALIZED_TIME_ILLEGAL_TIME.
                get(valueString, String.valueOf(e));
            DecodeException de = new DecodeException(message, e);
            StaticUtils.DEBUG_LOG.throwing(
                "GeneralizedTimeSyntax",  "valueIsAcceptable", de);
            throw de;
          }
        }
        else
        {
          Message message = WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_CHAR.get(
              valueString, String.valueOf(valueString.charAt(14)), 14);
          DecodeException e = new DecodeException(message);
          StaticUtils.DEBUG_LOG.throwing(
              "GeneralizedTimeSyntax",  "valueIsAcceptable", e);
          throw e;
        }

      default:
        Message message = WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_CHAR.get(
            valueString, String.valueOf(valueString.charAt(14)), 14);
        DecodeException e = new DecodeException(message);
        StaticUtils.DEBUG_LOG.throwing(
            "GeneralizedTimeSyntax",  "valueIsAcceptable", e);
        throw e;
    }
  }



  /**
   * Completes decoding the generalized time value containing a fractional
   * component.  It will also decode the trailing 'Z' or offset.
   *
   * @param  value       The whole value, including the fractional component and
   *                     time zone information.
   * @param  startPos    The position of the first character after the period
   *                     in the value string.
   * @param  year        The year decoded from the provided value.
   * @param  month       The month decoded from the provided value.
   * @param  day         The day decoded from the provided value.
   * @param  hour        The hour decoded from the provided value.
   * @param  minute      The minute decoded from the provided value.
   * @param  second      The second decoded from the provided value.
   * @param  multiplier  The multiplier value that should be used to scale the
   *                     fraction appropriately.  If it's a fraction of an hour,
   *                     then it should be 3600000 (60*60*1000).  If it's a
   *                     fraction of a minute, then it should be 60000.  If it's
   *                     a fraction of a second, then it should be 1000.
   *
   * @return  The timestamp created from the provided generalized time value
   *          including the fractional element.
   *
   * @throws  DecodeException  If the provided value cannot be parsed as a
   *                              valid generalized time string.
   */
  private static long finishDecodingFraction(String value, int startPos,
                                             int year, int month, int day,
                                             int hour, int minute, int second,
                                             int multiplier)
      throws DecodeException
  {
    int length = value.length();
    StringBuilder fractionBuffer = new StringBuilder(2 + length - startPos);
    fractionBuffer.append("0.");

    TimeZone timeZone = null;

    outerLoop:
    for (int i=startPos; i < length; i++)
    {
      char c = value.charAt(i);
      switch (c)
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
          fractionBuffer.append(c);
          break;

        case 'Z':
          // This is only acceptable if we're at the end of the value.
          if (i != (value.length() - 1))
          {
            Message message =
                WARN_ATTR_SYNTAX_GENERALIZED_TIME_ILLEGAL_FRACTION_CHAR.
                    get(value, String.valueOf(c));
            DecodeException e = new DecodeException(message);
            StaticUtils.DEBUG_LOG.throwing(
                "GeneralizedTimeSyntax",  "finishDecodingFraction", e);
            throw e;
          }

          timeZone = TIME_ZONE_UTC_OBJ;
          break outerLoop;

        case '+':
        case '-':
          timeZone = getTimeZoneForOffset(value, i);
          break outerLoop;

        default:
          Message message =
              WARN_ATTR_SYNTAX_GENERALIZED_TIME_ILLEGAL_FRACTION_CHAR.
                  get(value, String.valueOf(c));
          DecodeException e = new DecodeException(message);
          StaticUtils.DEBUG_LOG.throwing(
              "GeneralizedTimeSyntax",  "finishDecodingFraction", e);
          throw e;
      }
    }

    if (fractionBuffer.length() == 2)
    {
      Message message =
          WARN_ATTR_SYNTAX_GENERALIZED_TIME_EMPTY_FRACTION.get(value);
      DecodeException e = new DecodeException(message);
      StaticUtils.DEBUG_LOG.throwing(
          "GeneralizedTimeSyntax",  "finishDecodingFraction", e);
      throw e;
    }

    if (timeZone == null)
    {
      Message message =
          WARN_ATTR_SYNTAX_GENERALIZED_TIME_NO_TIME_ZONE_INFO.get(value);
      DecodeException e = new DecodeException(message);
      StaticUtils.DEBUG_LOG.throwing(
          "GeneralizedTimeSyntax",  "finishDecodingFraction", e);
      throw e;
    }

    Double fractionValue = Double.parseDouble(fractionBuffer.toString());
    long additionalMilliseconds = Math.round(fractionValue * multiplier);

    try
    {
      GregorianCalendar calendar = new GregorianCalendar();
      calendar.setLenient(false);
      calendar.setTimeZone(timeZone);
      calendar.set(year, month, day, hour, minute, second);
      calendar.set(Calendar.MILLISECOND, 0);
      return calendar.getTimeInMillis() + additionalMilliseconds;
    }
    catch (Exception e)
    {

      // This should only happen if the provided date wasn't legal
      // (e.g., September 31).
      Message message = WARN_ATTR_SYNTAX_GENERALIZED_TIME_ILLEGAL_TIME.get(
          value, String.valueOf(e));
      DecodeException de = new DecodeException(message, e);
      StaticUtils.DEBUG_LOG.throwing(
          "GeneralizedTimeSyntax",  "valueIsAcceptable", de);
      throw de;
    }
  }



  /**
   * Decodes a time zone offset from the provided value.
   *
   * @param  value          The whole value, including the offset.
   * @param  startPos       The position of the first character that is
   *                        contained in the offset.  This should be the
   *                        position of the plus or minus character.
   *
   * @return  The {@code TimeZone} object representing the decoded time zone.
   *
   * @throws  DecodeException  If the provided value does not contain a valid
   *                              offset.
   */
  private static TimeZone getTimeZoneForOffset(String value, int startPos)
      throws DecodeException
  {
    String offSetStr = value.substring(startPos);
    if ((offSetStr.length() != 3) && (offSetStr.length() != 5))
    {
      Message message = WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_OFFSET.get(
          value, offSetStr);
      DecodeException e = new DecodeException(message);
      StaticUtils.DEBUG_LOG.throwing(
          "GeneralizedTimeSyntax",  "getTimeZoneForOffset", e);
      throw e;
    }


    // The first character must be either a plus or minus.
    switch (offSetStr.charAt(0))
    {
      case '+':
      case '-':
        // These are OK.
        break;

      default:
        Message message = WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_OFFSET.get(
            value, offSetStr);
        DecodeException e = new DecodeException(message);
        StaticUtils.DEBUG_LOG.throwing(
            "GeneralizedTimeSyntax",  "getTimeZoneForOffset", e);
        throw e;
    }


    // The first two characters must be an integer between 00 and 23.
    switch (offSetStr.charAt(1))
    {
      case '0':
      case '1':
        switch (offSetStr.charAt(2))
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
            Message message = WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_OFFSET.
                get(value, offSetStr);
            DecodeException e = new DecodeException(message);
            StaticUtils.DEBUG_LOG.throwing(
                "GeneralizedTimeSyntax",  "getTimeZoneForOffset", e);
            throw e;
        }
        break;

      case '2':
        switch (offSetStr.charAt(2))
        {
          case '0':
          case '1':
          case '2':
          case '3':
            // These are all fine.
            break;

          default:
            Message message = WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_OFFSET.
                get(value, offSetStr);
            DecodeException e = new DecodeException(message);
            StaticUtils.DEBUG_LOG.throwing(
                "GeneralizedTimeSyntax",  "getTimeZoneForOffset", e);
            throw e;
        }
        break;

      default:
        Message message = WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_OFFSET.get(
            value, offSetStr);
        DecodeException e = new DecodeException(message);
        StaticUtils.DEBUG_LOG.throwing(
            "GeneralizedTimeSyntax",  "getTimeZoneForOffset", e);
        throw e;
    }


    // If there are two more characters, then they must be an integer between
    // 00 and 59.
    if (offSetStr.length() == 5)
    {
      switch (offSetStr.charAt(3))
      {
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
          switch (offSetStr.charAt(4))
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
                  WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_OFFSET.
                      get(value, offSetStr);
              DecodeException e = new DecodeException(message);
              StaticUtils.DEBUG_LOG.throwing(
                  "GeneralizedTimeSyntax",  "getTimeZoneForOffset", e);
              throw e;
          }
          break;

        default:
          Message message = WARN_ATTR_SYNTAX_GENERALIZED_TIME_INVALID_OFFSET.
              get(value, offSetStr);
          DecodeException e = new DecodeException(message);
          StaticUtils.DEBUG_LOG.throwing(
              "GeneralizedTimeSyntax",  "getTimeZoneForOffset", e);
          throw e;
      }
    }


    // If we've gotten here, then it looks like a valid offset.  We can create a
    // time zone by using "GMT" followed by the offset.
    return TimeZone.getTimeZone("GMT" + offSetStr);
  }

  @Override
  public String getEqualityMatchingRule() {
    return EMR_GENERALIZED_TIME_OID;
  }

  @Override
  public String getOrderingMatchingRule() {
    return OMR_GENERALIZED_TIME_OID;
  }

  @Override
  public String getSubstringMatchingRule() {
    return SMR_CASE_IGNORE_OID;
  }
}
