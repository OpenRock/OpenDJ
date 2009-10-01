package org.opends.sdk.schema.syntaxes;

import static org.opends.messages.SchemaMessages.*;
import static org.opends.sdk.schema.SchemaConstants.EMR_AUTH_PASSWORD_EXACT_OID;
import static org.opends.sdk.schema.SchemaConstants.SYNTAX_AUTH_PASSWORD_NAME;

import org.opends.messages.Message;
import org.opends.messages.MessageBuilder;
import org.opends.sdk.DecodeException;
import org.opends.sdk.schema.Schema;
import org.opends.server.types.ByteSequence;

/**
 * This class defines the auth password attribute syntax, which is defined in
 * RFC 3112 and is used to hold authentication information.  Only equality
 * matching will be allowed by default.
 */
public class AuthPasswordSyntax extends AbstractSyntaxImplementation
{
  public String getName()
  {
    return SYNTAX_AUTH_PASSWORD_NAME;
  }

  public boolean isHumanReadable()
  {
    return true;
  }

  public boolean valueIsAcceptable(Schema schema, ByteSequence value,
                                   MessageBuilder invalidReason)
  {
    try
    {
      decodeAuthPassword(value.toString());
      return true;
    }
    catch (DecodeException de)
    {
      invalidReason.append(de.getMessageObject());
      return false;
    }
  }

  @Override
  public String getEqualityMatchingRule()
  {
    return EMR_AUTH_PASSWORD_EXACT_OID;
  }

    /**
   * Decodes the provided authentication password value into its component
   * parts.
   *
   * @param  authPasswordValue  The authentication password value to be decoded.
   *
   * @return  A three-element array, containing the scheme, authInfo, and
   *          authValue components of the given string, in that order.
   *
   * @throws DecodeException  If a problem is encountered while attempting
   *                              to decode the value.
   */
  public static StringBuilder[] decodeAuthPassword(String authPasswordValue)
         throws DecodeException
  {
    // Create placeholders for the values to return.
    StringBuilder scheme    = new StringBuilder();
    StringBuilder authInfo  = new StringBuilder();
    StringBuilder authValue = new StringBuilder();


    // First, ignore any leading whitespace.
    int length = authPasswordValue.length();
    int  pos   = 0;
    while ((pos < length) && (authPasswordValue.charAt(pos) == ' '))
    {
      pos++;
    }


    // The next set of characters will be the scheme, which must consist only
    // of digits, uppercase alphabetic characters, dash, period, slash, and
    // underscore characters.  It must be immediately followed by one or more
    // spaces or a dollar sign.
readScheme:
    while (pos < length)
    {
      char c = authPasswordValue.charAt(pos);

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
        case 'A':
        case 'B':
        case 'C':
        case 'D':
        case 'E':
        case 'F':
        case 'G':
        case 'H':
        case 'I':
        case 'J':
        case 'K':
        case 'L':
        case 'M':
        case 'N':
        case 'O':
        case 'P':
        case 'Q':
        case 'R':
        case 'S':
        case 'T':
        case 'U':
        case 'V':
        case 'W':
        case 'X':
        case 'Y':
        case 'Z':
        case '-':
        case '.':
        case '/':
        case '_':
          scheme.append(c);
          pos++;
          break;
        case ' ':
        case '$':
          break readScheme;
        default:
          Message message = ERR_ATTR_SYNTAX_AUTHPW_INVALID_SCHEME_CHAR.get(pos);
          throw new DecodeException(message);
      }
    }


    // The scheme must consist of at least one character.
    if (scheme.length() == 0)
    {
      Message message = ERR_ATTR_SYNTAX_AUTHPW_NO_SCHEME.get();
      throw new DecodeException(message);
    }


    // Ignore any spaces before the dollar sign separator.  Then read the dollar
    // sign and ignore any trailing spaces.
    while ((pos < length) && (authPasswordValue.charAt(pos) == ' '))
    {
      pos++;
    }

    if ((pos < length) && (authPasswordValue.charAt(pos) == '$'))
    {
      pos++;
    }
    else
    {
      Message message = ERR_ATTR_SYNTAX_AUTHPW_NO_SCHEME_SEPARATOR.get();
      throw new DecodeException(message);
    }

    while ((pos < length) && (authPasswordValue.charAt(pos) == ' '))
    {
      pos++;
    }


    // The next component must be the authInfo element, containing only
    // printable characters other than the dollar sign and space character.
readAuthInfo:
    while (pos < length)
    {
      char c = authPasswordValue.charAt(pos);
      if ((c == ' ') || (c == '$'))
      {
        break readAuthInfo;
      }
      else if (PrintableStringSyntax.isPrintableCharacter(c))
      {
        authInfo.append(c);
        pos++;
      }
      else
      {
        Message message =
            ERR_ATTR_SYNTAX_AUTHPW_INVALID_AUTH_INFO_CHAR.get(pos);
        throw new DecodeException(message);
      }
    }


    // The authInfo element must consist of at least one character.
    if (scheme.length() == 0)
    {
      Message message = ERR_ATTR_SYNTAX_AUTHPW_NO_AUTH_INFO.get();
      throw new DecodeException(message);
    }


    // Ignore any spaces before the dollar sign separator.  Then read the dollar
    // sign and ignore any trailing spaces.
    while ((pos < length) && (authPasswordValue.charAt(pos) == ' '))
    {
      pos++;
    }

    if ((pos < length) && (authPasswordValue.charAt(pos) == '$'))
    {
      pos++;
    }
    else
    {
      Message message = ERR_ATTR_SYNTAX_AUTHPW_NO_AUTH_INFO_SEPARATOR.get();
      throw new DecodeException(message);
    }

    while ((pos < length) && (authPasswordValue.charAt(pos) == ' '))
    {
      pos++;
    }


    // The final component must be the authValue element, containing only
    // printable characters other than the dollar sign and space character.
    while (pos < length)
    {
      char c = authPasswordValue.charAt(pos);
      if ((c == ' ') || (c == '$'))
      {
        break ;
      }
      else if (PrintableStringSyntax.isPrintableCharacter(c))
      {
        authValue.append(c);
        pos++;
      }
      else
      {
        Message message =
            ERR_ATTR_SYNTAX_AUTHPW_INVALID_AUTH_VALUE_CHAR.get(pos);
        throw new DecodeException(message);
      }
    }


    // The authValue element must consist of at least one character.
    if (scheme.length() == 0)
    {
      Message message = ERR_ATTR_SYNTAX_AUTHPW_NO_AUTH_VALUE.get();
      throw new DecodeException(message);
    }


    // The only characters remaining must be whitespace.
    while (pos < length)
    {
      char c = authPasswordValue.charAt(pos);
      if (c == ' ')
      {
        pos++;
      }
      else
      {
        Message message = ERR_ATTR_SYNTAX_AUTHPW_INVALID_TRAILING_CHAR.get(pos);
        throw new DecodeException(message);
      }
    }


    // If we've gotten here, then everything must be OK.
    return new StringBuilder[]
    {
      scheme,
      authInfo,
      authValue
    };
  }



  /**
   * Indicates whether the provided value is encoded using the auth password
   * syntax.
   *
   * @param  value  The value for which to make the determination.
   *
   * @return  <CODE>true</CODE> if the value appears to be encoded using the
   *          auth password syntax, or <CODE>false</CODE> if not.
   */
  public static boolean isEncoded(ByteSequence value)
  {
    // FIXME -- Make this more efficient, and don't use exceptions for flow
    // control.


    try
    {
      decodeAuthPassword(value.toString());
      return true;
    }
    catch (Exception e)
    {
      return false;
    }
  }
}
