package org.opends.types;

import org.opends.server.types.RDN;
import org.opends.server.core.DirectoryServer;
import static org.opends.server.util.StaticUtils.toLowerCase;
import org.opends.messages.Message;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_DN_END_WITH_ATTR_NAME;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_DN_NO_EQUAL;
import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_DN_INVALID_CHAR;
import org.opends.ldap.DecodeException;
import org.opends.schema.Schema;
import org.opends.schema.AttributeType;

import java.util.LinkedList;


/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 18, 2009 Time:
 * 9:40:42 AM To change this template use File | Settings | File
 * Templates.
 */
public class DN
{
  public String toNormalizedString()
  {
    return null;
  }

    /**
   * Decodes the provided string as a DN.
   *
   * @param  dnString  The string to decode as a DN.
   *
   * @return  The decoded DN.
   *
   * @throws DecodeException  If a problem occurs while trying to
   *                              decode the provided string as a DN.
   */
  public static DN decode(String dnString, Schema schema)
         throws DecodeException
  {
    // A null or empty DN is acceptable.
    if (dnString == null)
    {
      return NULL_DN;
    }

    int length = dnString.length();
    if (length == 0)
    {
      return NULL_DN;
    }


    // Iterate through the DN string.  The first thing to do is to get
    // rid of any leading spaces.
    int pos = 0;
    char c = dnString.charAt(pos);
    while (c == ' ')
    {
      pos++;
      if (pos == length)
      {
        // This means that the DN was completely comprised of spaces
        // and therefore should be considered the same as a null or
        // empty DN.
        return NULL_DN;
      }
      else
      {
        c = dnString.charAt(pos);
      }
    }


    // We know that it's not an empty DN, so we can do the real
    // processing.  Create a loop and iterate through all the RDN
    // components.
    boolean allowExceptions =
         DirectoryServer.allowAttributeNameExceptions();
    LinkedList<RDN> rdnComponents = new LinkedList<RDN>();
    while (true)
    {
      StringBuilder attributeName = new StringBuilder();
      pos = parseAttributeName(dnString, pos, attributeName,
                               allowExceptions);


      // Make sure that we're not at the end of the DN string because
      // that would be invalid.
      if (pos >= length)
      {
        Message message = ERR_ATTR_SYNTAX_DN_END_WITH_ATTR_NAME.get(
            dnString, attributeName.toString());
        throw new DecodeException(message);
      }


      // Skip over any spaces between the attribute name and its
      // value.
      c = dnString.charAt(pos);
      while (c == ' ')
      {
        pos++;
        if (pos >= length)
        {
          // This means that we hit the end of the value before
          // finding a '='.  This is illegal because there is no
          // attribute-value separator.
          Message message = ERR_ATTR_SYNTAX_DN_END_WITH_ATTR_NAME.get(
              dnString, attributeName.toString());
          throw new DecodeException(message);
        }
        else
        {
          c = dnString.charAt(pos);
        }
      }


      // The next character must be an equal sign.  If it is not, then
      // that's an error.
      if (c == '=')
      {
        pos++;
      }
      else
      {
        Message message = ERR_ATTR_SYNTAX_DN_NO_EQUAL.get(
            dnString, attributeName.toString(), c);
        throw new DecodeException(message);
      }


      // Skip over any spaces after the equal sign.
      while ((pos < length) && ((c = dnString.charAt(pos)) == ' '))
      {
        pos++;
      }


      // If we are at the end of the DN string, then that must mean
      // that the attribute value was empty.  This will probably never
      // happen in a real-world environment, but technically isn't
      // illegal.  If it does happen, then go ahead and create the
      // RDN component and return the DN.
      if (pos >= length)
      {
        String        name      = attributeName.toString();
        String        lowerName = toLowerCase(name);
        AttributeType attrType  =
             schema.getAttributeType(lowerName);

        AttributeValue value =
            AttributeValues.create(ByteString.empty(),
                                ByteString.empty());
        rdnComponents.add(new RDN(attrType, name, value));
        return new DN(rdnComponents);
      }


      // Parse the value for this RDN component.
      ByteStringBuilder parsedValue = new ByteStringBuilder(0);
      pos = parseAttributeValue(dnString, pos, parsedValue);


      // Create the new RDN with the provided information.
      String name            = attributeName.toString();
      String lowerName       = toLowerCase(name);
      AttributeType attrType =
           DirectoryServer.getAttributeType(lowerName);
      if (attrType == null)
      {
        // This must be an attribute type that we don't know about.
        // In that case, we'll create a new attribute using the
        // default syntax.  If this is a problem, it will be caught
        // later either by not finding the target entry or by not
        // allowing the entry to be added.
        attrType = DirectoryServer.getDefaultAttributeType(name);
      }

      AttributeValue value =
          AttributeValues.create(attrType,
              parsedValue.toByteString());
      RDN rdn = new RDN(attrType, name, value);


      // Skip over any spaces that might be after the attribute value.
      while ((pos < length) && ((c = dnString.charAt(pos)) == ' '))
      {
        pos++;
      }


      // Most likely, we will be at either the end of the RDN
      // component or the end of the DN.  If so, then handle that
      // appropriately.
      if (pos >= length)
      {
        // We're at the end of the DN string and should have a valid
        // DN so return it.
        rdnComponents.add(rdn);
        return new DN(rdnComponents);
      }
      else if ((c == ',') || (c == ';'))
      {
        // We're at the end of the RDN component, so add it to the
        // list, skip over the comma/semicolon, and start on the next
        // component.
        rdnComponents.add(rdn);
        pos++;
        continue;
      }
      else if (c != '+')
      {
        // This should not happen.  At any rate, it's an illegal
        // character, so throw an exception.
        Message message =
            ERR_ATTR_SYNTAX_DN_INVALID_CHAR.get(dnString, c, pos);
        throw new DirectoryException(ResultCode.INVALID_DN_SYNTAX,
                                     message);
      }


      // If we have gotten here, then this must be a multi-valued RDN.
      // In that case, parse the remaining attribute/value pairs and
      // add them to the RDN that we've already created.
      while (true)
      {
        // Skip over the plus sign and any spaces that may follow it
        // before the next attribute name.
        pos++;
        while ((pos < length) && (dnString.charAt(pos) == ' '))
        {
          pos++;
        }


        // Parse the attribute name from the DN string.
        attributeName = new StringBuilder();
        pos = parseAttributeName(dnString, pos, attributeName,
                                 allowExceptions);


        // Make sure that we're not at the end of the DN string
        // because that would be invalid.
        if (pos >= length)
        {
          Message message = ERR_ATTR_SYNTAX_DN_END_WITH_ATTR_NAME.get(
              dnString, attributeName.toString());
          throw new DirectoryException(ResultCode.INVALID_DN_SYNTAX,
                                       message);
        }


        // Skip over any spaces between the attribute name and its
        // value.
        c = dnString.charAt(pos);
        while (c == ' ')
        {
          pos++;
          if (pos >= length)
          {
            // This means that we hit the end of the value before
            // finding a '='.  This is illegal because there is no
            // attribute-value separator.
            Message message = ERR_ATTR_SYNTAX_DN_END_WITH_ATTR_NAME.
                get(dnString, attributeName.toString());
            throw new DirectoryException(ResultCode.INVALID_DN_SYNTAX,
                                         message);
          }
          else
          {
            c = dnString.charAt(pos);
          }
        }


        // The next character must be an equal sign.  If it is not,
        // then that's an error.
        if (c == '=')
        {
          pos++;
        }
        else
        {
          Message message = ERR_ATTR_SYNTAX_DN_NO_EQUAL.get(
              dnString, attributeName.toString(), c);
          throw new DirectoryException(ResultCode.INVALID_DN_SYNTAX,
                                       message);
        }


        // Skip over any spaces after the equal sign.
        while ((pos < length) && ((c = dnString.charAt(pos)) == ' '))
        {
          pos++;
        }


        // If we are at the end of the DN string, then that must mean
        // that the attribute value was empty.  This will probably
        // never happen in a real-world environment, but technically
        // isn't illegal.  If it does happen, then go ahead and create
        // the RDN component and return the DN.
        if (pos >= length)
        {
          name      = attributeName.toString();
          lowerName = toLowerCase(name);
          attrType  = DirectoryServer.getAttributeType(lowerName);

          if (attrType == null)
          {
            // This must be an attribute type that we don't know
            // about.  In that case, we'll create a new attribute
            // using the default syntax.  If this is a problem, it
            // will be caught later either by not finding the target
            // entry or by not allowing the entry to be added.
            attrType = DirectoryServer.getDefaultAttributeType(name);
          }

          value = AttributeValues.create(ByteString.empty(),
                                     ByteString.empty());
          rdn.addValue(attrType, name, value);
          rdnComponents.add(rdn);
          return new DN(rdnComponents);
        }


        // Parse the value for this RDN component.
        parsedValue.clear();
        pos = parseAttributeValue(dnString, pos, parsedValue);


        // Create the new RDN with the provided information.
        name      = attributeName.toString();
        lowerName = toLowerCase(name);
        attrType  = DirectoryServer.getAttributeType(lowerName);
        if (attrType == null)
        {
          // This must be an attribute type that we don't know about.
          // In that case, we'll create a new attribute using the
          // default syntax.  If this is a problem, it will be caught
          // later either by not finding the target entry or by not
          // allowing the entry to be added.
          attrType = DirectoryServer.getDefaultAttributeType(name);
        }

        value = AttributeValues.create(attrType,
            parsedValue.toByteString());
        rdn.addValue(attrType, name, value);


        // Skip over any spaces that might be after the attribute
        // value.
        while ((pos < length) && ((c = dnString.charAt(pos)) == ' '))
        {
          pos++;
        }


        // Most likely, we will be at either the end of the RDN
        // component or the end of the DN.  If so, then handle that
        // appropriately.
        if (pos >= length)
        {
          // We're at the end of the DN string and should have a valid
          // DN so return it.
          rdnComponents.add(rdn);
          return new DN(rdnComponents);
        }
        else if ((c == ',') || (c == ';'))
        {
          // We're at the end of the RDN component, so add it to the
          // list, skip over the comma/semicolon, and start on the
          // next component.
          rdnComponents.add(rdn);
          pos++;
          break;
        }
        else if (c != '+')
        {
          // This should not happen.  At any rate, it's an illegal
          // character, so throw an exception.
          Message message =
              ERR_ATTR_SYNTAX_DN_INVALID_CHAR.get(dnString, c, pos);
          throw new DirectoryException(ResultCode.INVALID_DN_SYNTAX,
                                       message);
        }
      }
    }
  }
}
