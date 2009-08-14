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


package org.opends.server.schema;

import java.nio.ByteBuffer;
import java.util.*;
import org.opends.messages.Message;
import org.opends.server.backends.index.OrderingIndexKeyFactory;
import org.opends.server.backends.index.IndexQueryFactory;
import org.opends.server.api.MatchingRule;
import org.opends.server.api.MatchingRuleFactory;
import org.opends.server.admin.std.server.MatchingRuleCfg;
import org.opends.server.api.AbstractMatchingRule;
import org.opends.server.api.EqualityMatchingRule;
import org.opends.server.api.OrderingMatchingRule;
import org.opends.server.config.ConfigException;
import org.opends.server.core.DirectoryServer;
import org.opends.server.types.AttributeValue;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import org.opends.server.types.ConditionResult;
import org.opends.server.types.DirectoryException;
import org.opends.server.backends.index.IndexConfig;
import org.opends.server.backends.index.IndexKeyFactory;
import org.opends.server.backends.index.KeySet;
import org.opends.server.types.InitializationException;
import org.opends.server.types.ResultCode;
import static org.opends.server.util.StaticUtils.*;
import static org.opends.server.util.TimeThread.*;
import org.opends.server.backends.index.MatchingRuleIndexProvider;

import org.opends.server.types.Attribute;
import static org.opends.messages.SchemaMessages.*;
import static org.opends.server.loggers.ErrorLogger.*;
import static org.opends.server.schema.SchemaConstants.*;
import static org.opends.server.util.ServerConstants.*;
import static org.opends.server.schema.GeneralizedTimeSyntax.*;
import static org.opends.server.backends.index.MatchingRuleIndexProvider.*;



/**
 * This class acts as a factory for time-based matching rules.
 */
public final class TimeBasedMatchingRuleFactory
        extends MatchingRuleFactory<MatchingRuleCfg>
{
  //Greater-than RelativeTimeMatchingRule.
  private MatchingRule greaterThanRTMRule;


  //Less-than RelativeTimeMatchingRule.
  private MatchingRule lessThanRTMRule;


  //PartialDayAndTimeMatchingRule.
  private MatchingRule partialDTMatchingRule;


  //A Collection of matching rules managed by this factory.
  private Set<MatchingRule> matchingRules;


  //Set of matching rule index providers.
  private Set<MatchingRuleIndexProvider> providers;



  private static final TimeZone TIME_ZONE_UTC_OBJ =
      TimeZone.getTimeZone(TIME_ZONE_UTC);


  //Constants for months.
  private static final byte[] JAN = {'j','a','n' };


  private static final byte[] FEB = {'f','e','b'};


  private static final byte[] MAR = {'m','a','r'};


  private static final byte[] APR = {'a','p','r'};


  private static final byte[] MAY = {'m','a','y'};


  private static final byte[] JUN = {'j','u','n'};


  private static final byte[] JUL = {'j','u','l'};


  private static final byte[] AUG = {'a','u','g'};


  private static final byte[] SEP = {'s','e','p'};


  private static final byte[] OCT = {'o','c','t'};


  private static final byte[] NOV = {'n','o','v'};


  private static final byte[] DEC = {'d','e','c'};



  /**
   * {@inheritDoc}
   */
  @Override
  public void initializeMatchingRule(MatchingRuleCfg configuration)
          throws ConfigException, InitializationException
  {
    matchingRules = new HashSet<MatchingRule>();
    providers = new HashSet<MatchingRuleIndexProvider>();
    greaterThanRTMRule = new RelativeTimeGTOrderingMatchingRule();
    matchingRules.add(greaterThanRTMRule);
    providers.add(new RelativeTimeGTOrderingIndexProvider(
            (OrderingMatchingRule)greaterThanRTMRule,
            new RelativeTimeIndexKeyFactory(
            (OrderingMatchingRule)greaterThanRTMRule,ORDERING_INDEX_ID)));
    lessThanRTMRule = new RelativeTimeLTOrderingMatchingRule();
    matchingRules.add(lessThanRTMRule);
    providers.add(new RelativeTimeLTOrderingIndexProvider(
            (OrderingMatchingRule)lessThanRTMRule,
            new RelativeTimeIndexKeyFactory(
            (OrderingMatchingRule)lessThanRTMRule,ORDERING_INDEX_ID)));
    partialDTMatchingRule = new PartialDateAndTimeMatchingRule();
    matchingRules.add(partialDTMatchingRule);
    providers.add(new PartialDateAndTimeIndexProvider(
            (PartialDateAndTimeMatchingRule)partialDTMatchingRule));
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<MatchingRule> getMatchingRules()
  {
    return Collections.unmodifiableCollection(matchingRules);
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<MatchingRuleIndexProvider> getIndexProvider()
  {
    return providers;
  }



 /**
  * This class defines a matching rule which matches  the relative time for
  * time-based searches.
  */
  private abstract class RelativeTimeOrderingMatchingRule
          extends AbstractMatchingRule
          implements OrderingMatchingRule

  {
    /**
     * The serial version identifier required to satisfy the compiler because
     * this class implements the <CODE>java.io.Serializable</CODE> interface.
     * This value was generated using the <CODE>serialver</CODE> command-line
     * utility included with the Java SDK.
     */
    private static final long serialVersionUID = -3501812894473163490L;



     /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription()
    {
      //There is no standard definition.
      return null;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public String getSyntaxOID()
    {
       return SYNTAX_GENERALIZED_TIME_OID;
    }



    /**
      * {@inheritDoc}
      */
    @Override
    public ByteString normalizeValue(ByteSequence value)
            throws DirectoryException
    {
      try
      {
        long timestamp = decodeGeneralizedTimeValue(value);
        return ByteString.valueOf(timestamp);
      }
      catch (DirectoryException de)
      {
        switch (DirectoryServer.getSyntaxEnforcementPolicy())
        {
          case REJECT:
            throw de;

          case WARN:
            logError(de.getMessageObject());
            return value.toByteString();

          default:
            return value.toByteString();
        }
      }
    }



    /**
    * {@inheritDoc}
    */
    @Override
    public ByteString normalizeAssertionValue(ByteSequence value)
            throws DirectoryException
    {
      /**
      An assertion value may contain one of the following:
      s = second
      m = minute
      h = hour
      d = day
      w = week

      An example assertion is OID:=(-)1d, where a '-' means that the user
      intends to search only the expired events. In this example we are
      searching for an event expired 1 day back.

      Use this method to parse, validate and normalize the assertion value
      into a format to be recognized by the valuesMatch routine. This method
      takes the assertion value, adds/substracts it to/from the current time
      and calculates a time which will be used as a relative time by inherited
       rules.
      */

      int index = 0;
      boolean signed = false;
      byte firstByte = value.byteAt(0);

      if(firstByte == '-')
      {
        //Turn the sign on to go back in past.
        signed = true;
        index = 1;
      }
      else if(firstByte == '+')
      {
        //'+" is not required but we won't reject it either.
        index = 1;
      }

      long second = 0;
      long minute = 0;
      long hour = 0;
      long day = 0;
      long week = 0;

      boolean containsTimeUnit = false;
      long number = 0;

      for(; index<value.length(); index++)
      {
        byte b = value.byteAt(index);
        if(isDigit((char)b))
        {
          switch (value.byteAt(index))
          {
            case '0':
              number = (number * 10);
              break;

            case '1':
              number = (number * 10) + 1;
              break;

            case '2':
              number = (number * 10) + 2;
              break;

            case '3':
              number = (number * 10) + 3;
              break;

            case '4':
              number = (number * 10) + 4;
              break;

            case '5':
              number = (number * 10) + 5;
              break;

            case '6':
              number = (number * 10) + 6;
              break;

            case '7':
              number = (number * 10) + 7;
              break;

            case '8':
              number = (number * 10) + 8;
              break;

            case '9':
              number = (number * 10) + 9;
              break;
          }
        }
        else
        {
          Message message = null;
          if(containsTimeUnit)
          {
            //We already have time unit found by now.
            message = WARN_ATTR_CONFLICTING_ASSERTION_FORMAT.
                       get(value.toString());
          }
          else
          {
            switch(value.byteAt(index))
            {
              case 's':
                second = number;
                break;
              case 'm':
                minute = number;
                break;
              case 'h':
                hour = number;
                break;
              case 'd':
                day = number;
                break;
              case 'w':
                week = number;
                break;
              default:
                  message =
                          WARN_ATTR_INVALID_RELATIVE_TIME_ASSERTION_FORMAT.
                          get(value.toString(),(char)value.byteAt(index));
            }
          }
          if(message !=null)
          {
            //Log the message and throw an exception.
            logError(message);
            throw new DirectoryException(
                    ResultCode.INVALID_ATTRIBUTE_SYNTAX, message);
          }
          else
          {
            containsTimeUnit = true;
            number = 0;
          }
        }
      }

      if(!containsTimeUnit)
      {
        //There was no time unit so assume it is seconds.
        second = number;
      }

      long delta = (second + minute*60 +  hour*3600 + day*24*3600 +
              week*7*24*3600)*1000 ;
      long now = getTime();
      return signed?ByteString.valueOf(now-delta):
                            ByteString.valueOf(now+delta);
    }



    /**
     * {@inheritDoc}
     */
    public int compareValues(ByteSequence value1, ByteSequence value2)
    {
      return value1.compareTo(value2);
    }



    /**
      * {@inheritDoc}
      */
    public int compare(byte[] arg0, byte[] arg1)
    {
      return compare(arg0, arg1);
    }
  }



 /**
  * This class defines a matching rule which calculates the "greater-than"
  * relative time for time-based searches.
  */
  private final class RelativeTimeGTOrderingMatchingRule
          extends RelativeTimeOrderingMatchingRule
  {
    //All the names for this matching rule.
    private final List<String> names;



    /**
     * The serial version identifier required to satisfy the compiler because
     * this class implements the <CODE>java.io.Serializable</CODE> interface.
     * This value was generated using the <CODE>serialver</CODE> command-line
     * utility included with the Java SDK.
     */
     private static final long serialVersionUID = 7247241496402474136L;


    RelativeTimeGTOrderingMatchingRule()
    {
      names = new ArrayList<String>();
      names.add(EXT_OMR_RELATIVE_TIME_GT_NAME);
      names.add(EXT_OMR_RELATIVE_TIME_GT_ALT_NAME);
    }


     /**
      * {@inheritDoc}
      */
    @Override
    public String getName()
    {
      return EXT_OMR_RELATIVE_TIME_GT_NAME;
    }



    /**
      * {@inheritDoc}
      */
    @Override
    public Collection<String> getAllNames()
    {
      return Collections.unmodifiableList(names);
    }



    /**
      * {@inheritDoc}
      */
    @Override
    public String getOID()
    {
      return EXT_OMR_RELATIVE_TIME_GT_OID;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public ConditionResult valuesMatch(ByteSequence attributeValue,
        ByteSequence assertionValue)
    {
      int ret = compareValues(attributeValue, assertionValue);

      if (ret > 0)
      {
        return ConditionResult.TRUE;
      }
      else
      {
        return ConditionResult.FALSE;
      }
    }
  }


   /**
   * The index provider for RelativeTimeLTOrderingMatchingRule
   */
  public class RelativeTimeGTOrderingIndexProvider
          extends DefaultOrderingIndexProvider
  {
    /**
     * Creates a new instance of this index provider.
     * @param rule The ordering matching rule.
     * @param factory The index key factory for the matching rule.
     */
    public RelativeTimeGTOrderingIndexProvider(
            OrderingMatchingRule rule, OrderingIndexKeyFactory factory)
    {
      super(rule,factory);
    }




    /**
    * {@inheritDoc}
    */
    @Override
    public <T> T createIndexQuery(ByteSequence assertionValue,
        IndexQueryFactory<T> queryFactory) throws DirectoryException
    {
      return queryFactory.createRangeMatchQuery(factory
          .getIndexID(), matchingRule.normalizeAssertionValue(assertionValue),
          ByteString.empty(), false, false);
    }
  }



  /**
  * This class defines a matching rule which calculates the "less-than"
  * relative time for time-based searches.
  */
  private final class RelativeTimeLTOrderingMatchingRule
          extends RelativeTimeOrderingMatchingRule
  {
    //All the names for this matching rule.
    private final List<String> names;



    /**
     * The serial version identifier required to satisfy the compiler because
     * this class implements the <CODE>java.io.Serializable</CODE> interface.
     * This value was generated using the <CODE>serialver</CODE> command-line
     * utility included with the Java SDK.
     */
   private static final long serialVersionUID = -5122459830973558441L;



    RelativeTimeLTOrderingMatchingRule()
    {
      names = new ArrayList<String>();
      names.add(EXT_OMR_RELATIVE_TIME_LT_NAME);
      names.add(EXT_OMR_RELATIVE_TIME_LT_ALT_NAME);
    }



     /**
      * {@inheritDoc}
      */
    @Override
    public String getName()
    {
      return EXT_OMR_RELATIVE_TIME_LT_NAME;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getAllNames()
    {
      return Collections.unmodifiableList(names);
    }



    /**
      * {@inheritDoc}
      */
    @Override
    public String getOID()
    {
      return EXT_OMR_RELATIVE_TIME_LT_OID;
    }



     /**
     * {@inheritDoc}
     */
    @Override
    public ConditionResult valuesMatch(ByteSequence attributeValue,
        ByteSequence assertionValue)
    {
      int ret = compareValues(attributeValue, assertionValue);

      if (ret < 0)
      {
        return ConditionResult.TRUE;
      }
      else
      {
        return ConditionResult.FALSE;
      }
    }
  }



  /**
   * The index provider for RelativeTimeLTOrderingMatchingRule
   */
  public class RelativeTimeLTOrderingIndexProvider
          extends DefaultOrderingIndexProvider
  {
    /**
     * Creates a new instance of this index provider.
     * @param rule The ordering matching rule.
     * @param factory The index key factory for the matching rule.
     */
    public RelativeTimeLTOrderingIndexProvider(
            OrderingMatchingRule rule, OrderingIndexKeyFactory factory)
    {
      super(rule,factory);
    }




    /**
    * {@inheritDoc}
    */
    @Override
    public <T> T createIndexQuery(ByteSequence assertionValue,
        IndexQueryFactory<T> queryFactory) throws DirectoryException
    {
      return queryFactory.createRangeMatchQuery(factory
          .getIndexID(), ByteString.empty(),
          matchingRule.normalizeAssertionValue(assertionValue),false, false);
    }
  }



  /**
   * Index key factory class for Relative Time Matching rules which share
   * the same index. This index key factory is shared by both greater than
   * and less than Relative Time Matching Rules.
   */
  private final class RelativeTimeIndexKeyFactory extends
      OrderingIndexKeyFactory
  {

    /**
     * Creates a new instance of RelativeTimeIndexKeyFactory.
     *
     * @param matchingRule The relative time Matching Rule.
     */
    private RelativeTimeIndexKeyFactory(
        OrderingMatchingRule matchingRule,String indexID)
    {
      super(matchingRule,indexID);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public String getIndexID()
    {
      return EXTENSIBLE_INDEX_ID;
    }
  }



  /**
   * This class performs the partial date and time matching capabilities.
   */
  private final class PartialDateAndTimeMatchingRule
          extends EqualityMatchingRule
  {
    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription()
    {
      //There is no standard definition.
      return null;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public String getSyntaxOID()
    {
       return SYNTAX_GENERALIZED_TIME_OID;
    }



    /**
      * {@inheritDoc}
      */
    @Override
    public ByteString normalizeValue(ByteSequence value)
            throws DirectoryException
    {
      try
      {
        long timestamp = decodeGeneralizedTimeValue(value);
        return ByteString.valueOf(timestamp);
      }
      catch (DirectoryException de)
      {
        switch (DirectoryServer.getSyntaxEnforcementPolicy())
        {
          case REJECT:
            throw de;

          case WARN:
            logError(de.getMessageObject());
            return value.toByteString();

          default:
            return value.toByteString();
        }
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
      return EXT_PARTIAL_DATE_TIME_NAME;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public String getOID()
    {
      return EXT_PARTIAL_DATE_TIME_OID;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getAllNames()
    {
      return Collections.singleton(getName());
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public ByteString normalizeAssertionValue(ByteSequence value)
        throws DirectoryException
    {
     /**
      An assertion value may contain one or all of the following:
      DD = day
      MM = month
      YYYY = year

      An example assertion is OID:=04MM. In this example we are
      searching for entries corresponding to month of april.

      Use this method to parse, validate and normalize the assertion value
      into a format to be recognized by the compare routine. The normalized
      value is actually the format of : DDMMYYYY.
      */
      int date = 0;
      int year = 0;
      int number = 0;
      int month = -1;

      int length = value.length();
      for(int index=0; index<length; index++)
      {
        byte b = value.byteAt(index);
        if(isDigit((char)b))
        {
          switch (value.byteAt(index))
          {
            case '0':
              number = (number * 10);
              break;

            case '1':
              number = (number * 10) + 1;
              break;

            case '2':
              number = (number * 10) + 2;
              break;

            case '3':
              number = (number * 10) + 3;
              break;

            case '4':
              number = (number * 10) + 4;
              break;

            case '5':
              number = (number * 10) + 5;
              break;

            case '6':
              number = (number * 10) + 6;
              break;

            case '7':
              number = (number * 10) + 7;
              break;

            case '8':
              number = (number * 10) + 8;
              break;

            case '9':
              number = (number * 10) + 9;
              break;
          }
        }
        else
        {
          Message message = null;
          switch(value.byteAt(index))
          {
            case 'D':
              if(!(index < length-1) || value.byteAt(index+1) !='D')
              {
                //the acceptable format is 'DD'.
                message =
                        WARN_ATTR_MISSING_CHAR_PARTIAL_TIME_ASSERTION_FORMAT.
                        get(value.toString(),
                        (char)value.byteAt(index),'D',index+1);
              }
              else if(number == 0)
              {
                message =
                        WARN_ATTR_INVALID_DATE_ASSERTION_FORMAT.get(
                        value.toString(), number);
              }
              else if(date > 0)
              {
                message =
                        WARN_ATTR_DUPLICATE_DATE_ASSERTION_FORMAT.get(
                        value.toString(),date);
              }
              else
              {
                date = number;
                index++;
              }
              break;
            case 'M':
              if(!(index < length-1) || value.byteAt(index+1)!='M')
              {
                //the acceptable value is 'MM'.
                message =
                        WARN_ATTR_MISSING_CHAR_PARTIAL_TIME_ASSERTION_FORMAT.
                        get(value.toString(),
                        (char)value.byteAt(index),'M',index+1);
              }
              else if(number == 0)
              {
                message =
                        WARN_ATTR_INVALID_MONTH_ASSERTION_FORMAT.
                        get(value.toString(),number);
              }
              else if(month > 0)
              {
                message =
                        WARN_ATTR_DUPLICATE_MONTH_ASSERTION_FORMAT.get(
                        value.toString(),month);
              }
              else
              {
                month = number;
                index++;
              }
              break;
            case 'Y':
              if(!(index < length-3))
              {
                //the acceptable value is 'YYYY".
                message =
                        WARN_ATTR_MISSING_YEAR_PARTIAL_TIME_ASSERTION_FORMAT.
                        get(value.toString());
              }
              else
              {
yearLoop: for(int i=index;i<index+3;i++)
                {
                  if(value.byteAt(i) !='Y')
                  {
                    message =
                         WARN_ATTR_MISSING_CHAR_PARTIAL_TIME_ASSERTION_FORMAT.
                        get(value.toString(),(char)value.byteAt(i),'Y',i);
                    break yearLoop;
                  }
                }
                if(message == null)
                {
                  if(number == 0)
                  {
                    message =
                            WARN_ATTR_INVALID_YEAR_ASSERTION_FORMAT.
                            get(value.toString(),number);
                  }
                  else if(year >0)
                  {
                    message = WARN_ATTR_DUPLICATE_YEAR_ASSERTION_FORMAT.
                            get(value.toString(),year);
                  }
                  else
                  {
                    year = number;
                    index+=3;
                  }
                }
              }
              break;
            default:
                message =
                        WARN_ATTR_INVALID_PARTIAL_TIME_ASSERTION_FORMAT.
                        get(value.toString(),(char)value.byteAt(index));
          }
          if(message !=null)
          {
            logError(message);
            throw new DirectoryException(
                    ResultCode.INVALID_ATTRIBUTE_SYNTAX, message);
          }
          else
          {
            number = 0;
          }
        }
      }

      //Validate year, month and date in that order.
      if(year < 0)
      {
        //A future date is allowed.
        Message message =
                WARN_ATTR_INVALID_YEAR_ASSERTION_FORMAT.
                get(value.toString(),year);
        logError(message);
        throw new DirectoryException(
                ResultCode.INVALID_ATTRIBUTE_SYNTAX, message);
      }

      switch(month)
      {
        case -1:
          //just allow this.
          break;
        case 1:
          month = Calendar.JANUARY;
          break;
        case 2:
          month = Calendar.FEBRUARY;
          break;
        case 3:
          month = Calendar.MARCH;
          break;
        case 4:
          month = Calendar.APRIL;
          break;
        case 5:
          month = Calendar.MAY;
          break;
        case 6:
          month = Calendar.JUNE;
          break;
        case 7:
          month = Calendar.JULY;
          break;
        case 8:
          month = Calendar.AUGUST;
          break;
        case 9:
          month = Calendar.SEPTEMBER;
          break;
        case 10:
          month = Calendar.OCTOBER;
          break;
        case 11:
          month = Calendar.NOVEMBER;
          break;
        case 12:
          month = Calendar.DECEMBER;
          break;
        default:
          Message message =
                WARN_ATTR_INVALID_MONTH_ASSERTION_FORMAT.
                get(value.toString(),month);
          logError(message);
           throw new DirectoryException(
                   ResultCode.INVALID_ATTRIBUTE_SYNTAX, message);
      }

      boolean invalidDate = false;
      switch(date)
      {
        case 29:
          if(month == Calendar.FEBRUARY && year%4 !=0)
          {
            invalidDate = true;
          }
          break;
        case 31:
          if(month != -1 && month != Calendar.JANUARY && month!= Calendar.MARCH
                  && month != Calendar.MAY && month != Calendar.JULY
                  && month != Calendar.AUGUST && month != Calendar.OCTOBER
                  && month != Calendar.DECEMBER)
          {
            invalidDate = true;
          }
          break;
        default:
          if(!(date >=0 && date <=31))
          {
            invalidDate = true;
          }
      }
      if(invalidDate)
      {
        Message message =
                WARN_ATTR_INVALID_DATE_ASSERTION_FORMAT.
                get(value.toString(),date);
        logError(message);
        throw new DirectoryException(
                ResultCode.INVALID_ATTRIBUTE_SYNTAX, message);
      }

      /**
       * Since we reached here we have a valid assertion value. Construct
       * a normalized value in the order: DATE MONTH YEAR.
       */
      ByteBuffer bb = ByteBuffer.allocate(3*4);
      bb.putInt(date);
      bb.putInt(month);
      bb.putInt(year);
      return ByteString.wrap(bb.array());
    }



     /**
     * {@inheritDoc}
     */
    @Override
    public ConditionResult valuesMatch(ByteSequence attributeValue,
        ByteSequence assertionValue)
    {
      long timeInMS = ((ByteString)attributeValue).toLong();
      //Build the information from the attribute value.
      GregorianCalendar cal = new GregorianCalendar(TIME_ZONE_UTC_OBJ);
      cal.setLenient(false);
      cal.setTimeInMillis(timeInMS);
      int date = cal.get(Calendar.DATE);
      int month = cal.get(Calendar.MONTH);
      int year = cal.get(Calendar.YEAR);

      //Build the information from the assertion value.
      ByteBuffer bb = ByteBuffer.wrap(assertionValue.toByteArray());
      int assertDate = bb.getInt(0);
      int assertMonth = bb.getInt(4);
      int assertYear = bb.getInt(8);

      //All the non-zero values should match.
      if(assertDate !=0 && assertDate != date)
      {
        return ConditionResult.FALSE;
      }

      if(assertMonth !=-1 && assertMonth != month)
      {
        return ConditionResult.FALSE;
      }

      if(assertYear !=0 && assertYear != year)
      {
        return ConditionResult.FALSE;
      }

     return ConditionResult.TRUE;
    }



    /**
     * Decomposes an attribute value into a set of partial date and time index
     * keys.
     *
     * @param attValue
     *          The normalized attribute value
     * @param set
     *          A set into which the keys will be inserted.
     */
    private void timeKeys(ByteString attributeValue, KeySet keySet)
    {
      long timeInMS = 0L;
      try
      {
        timeInMS = decodeGeneralizedTimeValue(attributeValue);
      }
      catch(DirectoryException de)
      {
        //If the schema check is on this should never reach here. If not then we
        //would return from here.
        return;
      }
      //Build the information from the attribute value.
      GregorianCalendar cal = new GregorianCalendar(TIME_ZONE_UTC_OBJ);
      cal.setTimeInMillis(timeInMS);
      int date = cal.get(Calendar.DATE);
      int month = cal.get(Calendar.MONTH);
      int year = cal.get(Calendar.YEAR);

      //Insert date.
      if(date > 0)
      {
        keySet.addKey(ByteString.valueOf(date).toByteArray());
      }

      //Insert month.
      if(month >=0)
      {
        keySet.addKey(getMonthKey(month));
      }

      if(year > 0)
      {
        keySet.addKey(ByteString.valueOf(year).toByteArray());
      }
    }



    //Returns a byte array of for the corresponding month.
    public byte[] getMonthKey(int month)
    {
      byte[] key = null;
      switch(month)
      {
        case Calendar.JANUARY:
          key = JAN;
          break;
        case Calendar.FEBRUARY:
          key = FEB;
          break;
        case Calendar.MARCH:
          key = MAR;
          break;
        case Calendar.APRIL:
          key = APR;
          break;
        case Calendar.MAY:
          key = MAY;
          break;
        case Calendar.JUNE:
          key = JUN;
          break;
        case Calendar.JULY:
          key = JUL;
          break;
        case Calendar.AUGUST:
          key = AUG;
          break;
        case Calendar.SEPTEMBER:
          key = SEP;
          break;
        case Calendar.OCTOBER:
          key = OCT;
          break;
        case Calendar.NOVEMBER:
          key = NOV;
          break;
        case Calendar.DECEMBER:
          key = DEC;
          break;
        default:
          key = new byte[0];
       }
      return key;
    }
  }



  /**
   * Extensible Indexer class for Partial Date and Time Matching rules.
   */
  private final class PartialDateAndTimeIndexKeyFactory extends IndexKeyFactory
  {
    //The equality matching rule to be used for indexing.
    private PartialDateAndTimeMatchingRule matchingRule;



    //The byte-by-byte comparator for comparing the keys.
    private Comparator<byte[]> comparator;



    /**
     * Creates a new instance of PartialDateAndTimeIndexKeyFactory.
     *
     * @param matchingRule
     *          The PartialDateAndTime Rule.
     */
    private PartialDateAndTimeIndexKeyFactory(
        PartialDateAndTimeMatchingRule matchingRule)
    {
      this.matchingRule = matchingRule;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public Comparator<byte[]> getComparator()
    {
      return comparator;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void getKeys(List<Attribute> attrList, KeySet keySet)
    {
      for (Attribute attr : attrList)
      {
        for(AttributeValue value : attr)
        {
          matchingRule.timeKeys(value.getValue(), keySet);
        }
      }
    }



    /**
     * {@inheritDoc}
     */
    public String getPreferredIndexName()
    {
      return PARTIAL_DATE_TIME_INDEX_NAME;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public String getIndexID()
    {
      return EXTENSIBLE_INDEX_ID;
    }
  }



  /**
   * The index provider for partial date and time matching rule.
   */
  public class PartialDateAndTimeIndexProvider
          extends MatchingRuleIndexProvider
  {
    //Matching rule.
    private PartialDateAndTimeMatchingRule rule;



    //Index key factory.
    private IndexKeyFactory factory;


    /**
     * Creates a new instance of this index provider.
     * @param rule The ordering matching rule.
     */
    private PartialDateAndTimeIndexProvider(
            PartialDateAndTimeMatchingRule rule)
    {
      this.rule = rule;
      this.factory = new PartialDateAndTimeIndexKeyFactory(rule);
    }



    /**
     * {@inheritDoc}
     */
    public <T> T createIndexQuery(ByteSequence assertionValue,
            IndexQueryFactory<T> queryFactory) throws DirectoryException
    {
      //Build the information from the assertion value
      byte[] arr = rule.normalizeAssertionValue(
              assertionValue).toByteArray();
      ByteBuffer bb = ByteBuffer.wrap(arr);

      int assertDate = bb.getInt(0);
      int assertMonth = bb.getInt(4);
      int assertYear = bb.getInt(8);
      List<T> queries = new ArrayList<T>();

      if(assertDate >0)
      {
        queries.add(queryFactory.createExactMatchQuery(
                factory.getIndexID(),
                ByteString.valueOf(assertDate)));
      }

      if(assertMonth >=0)
      {
        queries.add(queryFactory.createExactMatchQuery(
                factory.getIndexID(),
                ByteString.wrap(rule.getMonthKey(assertMonth))));
      }

      if(assertYear > 0)
      {
        queries.add(queryFactory.createExactMatchQuery(
                factory.getIndexID(),
                ByteString.valueOf(assertYear)));
      }
      return queryFactory.createIntersectionQuery(queries);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IndexKeyFactory> getIndexKeyFactory(IndexConfig config)
    {
      return Collections.singleton(factory);
    }

    @Override
    public MatchingRule getMatchingRule() 
    {
      return rule;
    }
  }
}
