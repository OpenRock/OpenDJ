package org.opends.common.api.raw;

import static org.opends.server.protocols.ldap.LDAPConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 18, 2009 Time: 5:23:36
 * PM To change this template use File | Settings | File Templates.
 */
public final class DereferencePolicy
{
  private static final ArrayList<DereferencePolicy> ELEMENTS =
      new ArrayList<DereferencePolicy>(4);

  public static final DereferencePolicy NEVER =
      register(DEREF_NEVER, "never");
  public static final DereferencePolicy IN_SEARCHING =
      register(DEREF_IN_SEARCHING, "inSearching");
  public static final DereferencePolicy FINDING_BASE =
      register(DEREF_FINDING_BASE, "findingBase");
  public static final DereferencePolicy ALWAYS =
      register(DEREF_ALWAYS, "always");

  private int intValue;
  private String name;

  public static DereferencePolicy valueOf(int intValue)
  {
    DereferencePolicy e = ELEMENTS.get(intValue);
    if(e == null)
    {
      e = new DereferencePolicy(intValue, "undefinded("+intValue+")");
    }
    return e;
  }

  public static List<DereferencePolicy> values()
  {
    return Collections.unmodifiableList(ELEMENTS);
  }

  public int intValue()
  {
    return intValue;
  }

  public String toString()
  {
    return name;
  }

  private DereferencePolicy(int intValue, String name)
  {
    this.intValue = intValue;
    this.name = name;
  }

  private final static DereferencePolicy register(int intValue, String name)
  {
    DereferencePolicy t = new DereferencePolicy(intValue, name);
    ELEMENTS.add(intValue, t);
    return t;
  }

  @Override
  public boolean equals(Object o)
  {
    if(this == o)
    {
      return true;
    }

    if(o instanceof DereferencePolicy)
    {
      return this.intValue == ((DereferencePolicy)o).intValue;  
    }

    return false;
  }

  @Override
  public int hashCode()
  {
    return intValue;
  }
}
