package org.opends.examples;

import org.opends.schema.Schema;
import org.opends.types.DN;
import org.opends.types.RDN;
import org.opends.types.SearchScope;
import org.opends.ldap.DecodeException;
import org.opends.ldap.Connection;
import org.opends.ldap.Connections;
import org.opends.ldap.responses.SearchResultFuture;
import org.opends.ldap.responses.SearchResult;
import org.opends.ldap.impl.LDAPConnectionFactory;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Aug 13, 2009 Time: 3:44:34
 * PM To change this template use File | Settings | File Templates.
 */
public class SchemaTest
{
  public static final void main(String[] args)
  {
    Schema defaultSchema = Schema.DEFAULT_SCHEMA;

    try
    {
    DN dn = DN.valueOf("  attributetypes=\\0dLu\\C4\\8Di\\C4\\87                    ,nameforms=\"Before\\0d\\\"After\\\" , +\"+  objectclasses= test  ,nameforms=com  ", defaultSchema);
    DN dn2 = DN.valueOf(dn.toString(), defaultSchema);
      System.out.println(dn);
      System.out.println(dn2);
      System.out.println(dn.toString().equals(dn2.toString()));
    }
    catch(DecodeException de)
    {
      System.out.println(de);
    }


  }
}
