package org.opends.schema;

import org.opends.schema.syntaxes.SyntaxDescription;
import org.opends.ldap.DecodeException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 15, 2009
 * Time: 5:45:01 PM
 * To change this template use File | Settings | File Templates.
 */
public interface SyntaxFactory
{
  public SyntaxDescription decode(String definition) 
      throws DecodeException;

  public SyntaxDescription getSyntax(String oid);
}
