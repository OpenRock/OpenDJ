package org.opends.common.api;

import static org.opends.server.protocols.ldap.LDAPConstants.*;
import static org.opends.messages.CoreMessages.*;
import org.opends.messages.Message;

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
      register(DEREF_NEVER, INFO_DEREFERENCE_POLICY_NEVER.get());
  public static final DereferencePolicy IN_SEARCHING =
      register(DEREF_IN_SEARCHING,
          INFO_DEREFERENCE_POLICY_IN_SEARCHING.get());
  public static final DereferencePolicy FINDING_BASE =
      register(DEREF_FINDING_BASE,
          INFO_DEREFERENCE_POLICY_FINDING_BASE.get());
  public static final DereferencePolicy ALWAYS =
      register(DEREF_ALWAYS, INFO_DEREFERENCE_POLICY_ALWAYS.get());

  private int intValue;
  private Message name;

  public static DereferencePolicy valueOf(int intValue)
  {
    DereferencePolicy e = ELEMENTS.get(intValue);
    if(e == null)
    {
      e = new DereferencePolicy(intValue,
          INFO_UNDEFINED_TYPE.get(intValue));
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
    return name.toString();
  }

  private DereferencePolicy(int intValue, Message name)
  {
    this.intValue = intValue;
    this.name = name;
  }

  private static DereferencePolicy register(int intValue,
                                            Message name)
  {
    DereferencePolicy t = new DereferencePolicy(intValue, name);
    ELEMENTS.add(intValue, t);
    return t;
  }

  @Override
  public boolean equals(Object o)
  {
    return this == o || o instanceof DereferencePolicy &&
        this.intValue == ((DereferencePolicy) o).intValue;

  }

  @Override
  public int hashCode()
  {
    return intValue;
  }
}
