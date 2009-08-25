package org.opends.schema.syntaxes;

import static org.opends.messages.SchemaMessages.*;
import static org.opends.server.util.StaticUtils.isValidSchemaElement;
import static org.opends.server.util.StaticUtils.toLowerCase;

import org.opends.messages.MessageBuilder;
import org.opends.schema.Schema;
import org.opends.server.types.ByteSequence;
import static org.opends.server.schema.SchemaConstants.*;

/**
 * This class implements the guide attribute syntax, which may be used to
 * provide criteria for generating search filters for entries, optionally tied
 * to a specified objectclass.
 */
public class GuideSyntax extends AbstractSyntaxImplementation
{
  public String getName() {
    return SYNTAX_GUIDE_NAME;
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
    // Get a lowercase string version of the provided value.
    String valueStr = toLowerCase(value.toString());


    // Find the position of the octothorpe.  If there isn't one, then the entire
    // value should be the criteria.
    int sharpPos = valueStr.indexOf('#');
    if (sharpPos < 0)
    {
      return criteriaIsValid(valueStr, valueStr, invalidReason);
    }


    // Get the objectclass and see if it is a valid name or OID.
    String ocName   = valueStr.substring(0, sharpPos).trim();
    int    ocLength = ocName.length();
    if (ocLength == 0)
    {

      invalidReason.append(ERR_ATTR_SYNTAX_GUIDE_NO_OC.get(valueStr));
      return false;
    }

    if (! isValidSchemaElement(ocName, 0, ocLength, invalidReason))
    {
      return false;
    }


    // The rest of the value must be the criteria.
    return criteriaIsValid(valueStr.substring(sharpPos+1), valueStr,
            invalidReason);
  }



  /**
   * Determines whether the provided string represents a valid criteria
   * according to the guide syntax.
   *
   * @param  criteria       The portion of the criteria for which to make the
   *                        determination.
   * @param  valueStr       The complete guide value provided by the client.
   * @param  invalidReason  The buffer to which to append the reason that the
   *                        criteria is invalid if a problem is found.
   *
   * @return  <CODE>true</CODE> if the provided string does contain a valid
   *          criteria, or <CODE>false</CODE> if not.
   */
  public static boolean criteriaIsValid(String criteria, String valueStr,
                                        MessageBuilder invalidReason)
  {
    // See if the criteria starts with a '!'.  If so, then just evaluate
    // everything after that as a criteria.
    char c = criteria.charAt(0);
    if (c == '!')
    {
      return criteriaIsValid(criteria.substring(1), valueStr, invalidReason);
    }


    // See if the criteria starts with a '('.  If so, then find the
    // corresponding ')' and parse what's in between as a criteria.
    if (c == '(')
    {
      int length = criteria.length();
      int depth  = 1;

      for (int i=1; i < length; i++)
      {
        c = criteria.charAt(i);
        if (c == ')')
        {
          depth--;
          if (depth == 0)
          {
            String subCriteria = criteria.substring(1, i);
            if (! criteriaIsValid(subCriteria, valueStr, invalidReason))
            {
              return false;
            }

            // If we are at the end of the value, then it was valid.  Otherwise,
            // the next character must be a pipe or an ampersand followed by
            // another set of criteria.
            if (i == (length-1))
            {
              return true;
            }
            else
            {
              c = criteria.charAt(i+1);
              if ((c == '|') || (c == '&'))
              {
                return criteriaIsValid(criteria.substring(i+2), valueStr,
                                       invalidReason);
              }
              else
              {

                invalidReason.append(
                        ERR_ATTR_SYNTAX_GUIDE_ILLEGAL_CHAR.get(
                                valueStr, criteria, c, (i+1)));
                return false;
              }
            }
          }
        }
        else if (c == '(')
        {
          depth++;
        }
      }


      // If we've gotten here, then we went through the entire value without
      // finding the appropriate closing parenthesis.

      invalidReason.append(ERR_ATTR_SYNTAX_GUIDE_MISSING_CLOSE_PAREN.get(
              valueStr, criteria));
      return false;
    }


    // See if the criteria starts with a '?'.  If so, then it must be either
    // "?true" or "?false".
    if (c == '?')
    {
      if (criteria.startsWith("?true"))
      {
        if (criteria.length() == 5)
        {
          return true;
        }
        else
        {
          // The only characters allowed next are a pipe or an ampersand.
          c = criteria.charAt(5);
          if ((c == '|') || (c == '&'))
          {
            return criteriaIsValid(criteria.substring(6), valueStr,
                                   invalidReason);
          }
          else
          {
            invalidReason.append(ERR_ATTR_SYNTAX_GUIDE_ILLEGAL_CHAR.get(
                    valueStr, criteria, c, 5));
            return false;
          }
        }
      }
      else if (criteria.startsWith("?false"))
      {
        if (criteria.length() == 6)
        {
          return true;
        }
        else
        {
          // The only characters allowed next are a pipe or an ampersand.
          c = criteria.charAt(6);
          if ((c == '|') || (c == '&'))
          {
            return criteriaIsValid(criteria.substring(7), valueStr,
                                   invalidReason);
          }
          else
          {
            invalidReason.append(ERR_ATTR_SYNTAX_GUIDE_ILLEGAL_CHAR.get(
                    valueStr, criteria, c, 6));
            return false;
          }
        }
      }
      else
      {
        invalidReason.append(ERR_ATTR_SYNTAX_GUIDE_INVALID_QUESTION_MARK.get(
                valueStr, criteria));
        return false;
      }
    }


    // See if the criteria is either "true" or "false".  If so, then it is
    // valid.
    if (criteria.equals("true") || criteria.equals("false"))
    {
      return true;
    }


    // The only thing that will be allowed is an attribute type name or OID
    // followed by a dollar sign and a match type.  Find the dollar sign and
    // verify whether the value before it is a valid attribute type name or OID.
    int dollarPos = criteria.indexOf('$');
    if (dollarPos < 0)
    {
      invalidReason.append(ERR_ATTR_SYNTAX_GUIDE_NO_DOLLAR.get(
              valueStr, criteria));
      return false;
    }
    else if (dollarPos == 0)
    {
      invalidReason.append(ERR_ATTR_SYNTAX_GUIDE_NO_ATTR.get(
              valueStr, criteria));
      return false;
    }
    else if (dollarPos == (criteria.length()-1))
    {
      invalidReason.append(ERR_ATTR_SYNTAX_GUIDE_NO_MATCH_TYPE.get(
              valueStr, criteria));
      return false;
    }
    else
    {
      if (! isValidSchemaElement(criteria, 0, dollarPos, invalidReason))
      {
        return false;
      }
    }


    // The substring immediately after the dollar sign must be one of "eq",
    // "substr", "ge", "le", or "approx".  It may be followed by the end of the
    // value, a pipe, or an ampersand.
    int endPos;
    c = criteria.charAt(dollarPos+1);
    switch (c)
    {
      case 'e':
        if (criteria.startsWith("eq", dollarPos+1))
        {
          endPos = dollarPos + 3;
          break;
        }
        else
        {
          invalidReason.append(ERR_ATTR_SYNTAX_GUIDE_INVALID_MATCH_TYPE.get(
                  valueStr, criteria, dollarPos+1));
          return false;
        }

      case 's':
        if (criteria.startsWith("substr", dollarPos+1))
        {
          endPos = dollarPos + 7;
          break;
        }
        else
        {
          invalidReason.append(ERR_ATTR_SYNTAX_GUIDE_INVALID_MATCH_TYPE.get(
                  valueStr, criteria, dollarPos+1));
          return false;
        }

      case 'g':
        if (criteria.startsWith("ge", dollarPos+1))
        {
          endPos = dollarPos + 3;
          break;
        }
        else
        {
          invalidReason.append(ERR_ATTR_SYNTAX_GUIDE_INVALID_MATCH_TYPE.get(
                  valueStr, criteria, dollarPos+1));
          return false;
        }

      case 'l':
        if (criteria.startsWith("le", dollarPos+1))
        {
          endPos = dollarPos + 3;
          break;
        }
        else
        {
          invalidReason.append(ERR_ATTR_SYNTAX_GUIDE_INVALID_MATCH_TYPE.get(
                  valueStr, criteria, dollarPos+1));
          return false;
        }

      case 'a':
        if (criteria.startsWith("approx", dollarPos+1))
        {
          endPos = dollarPos + 7;
          break;
        }
        else
        {
          invalidReason.append(ERR_ATTR_SYNTAX_GUIDE_INVALID_MATCH_TYPE.get(
                  valueStr, criteria, dollarPos+1));
          return false;
        }

      default:
        invalidReason.append(ERR_ATTR_SYNTAX_GUIDE_INVALID_MATCH_TYPE.get(
                valueStr, criteria, dollarPos+1));
        return false;
    }


    // See if we are at the end of the value.  If so, then it is valid.
    // Otherwise, the next character must be a pipe or an ampersand.
    if (endPos >= criteria.length())
    {
      return true;
    }
    else
    {
      c = criteria.charAt(endPos);
      if ((c == '|') || (c == '&'))
      {
        return criteriaIsValid(criteria.substring(endPos+1), valueStr,
                               invalidReason);
      }
      else
      {
        invalidReason.append(ERR_ATTR_SYNTAX_GUIDE_ILLEGAL_CHAR.get(
                valueStr, criteria, c, endPos));
        return false;
      }
    }
  }

  @Override
  public String getEqualityMatchingRule() {
    return EMR_OCTET_STRING_OID;
  }

  @Override
  public String getOrderingMatchingRule() {
    return OMR_OCTET_STRING_OID;
  }
}
