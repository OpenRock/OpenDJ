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

 
}
