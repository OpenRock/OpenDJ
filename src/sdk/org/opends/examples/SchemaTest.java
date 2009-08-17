package org.opends.examples;

import org.opends.schema.Schema;
import org.opends.types.DN;
import org.opends.types.RDN;
import org.opends.ldap.DecodeException;

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
    DN dn = DN.valueOf("nameforms=example,nameforms=com", defaultSchema);
      System.out.println(dn);
    }
    catch(DecodeException de)
    {
      System.out.println(de);
    }

  }
}
