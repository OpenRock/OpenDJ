package org.opends.types;



import static org.opends.messages.CoreMessages.*;
import static org.opends.server.protocols.ldap.LDAPConstants.*;

import java.util.Arrays;
import java.util.List;

import org.opends.messages.Message;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 18, 2009 Time:
 * 5:23:36 PM To change this template use File | Settings | File
 * Templates.
 */
public final class DereferencePolicy
{
  private static final DereferencePolicy[] ELEMENTS =
      new DereferencePolicy[4];

  public static final DereferencePolicy NEVER =
      register(DEREF_NEVER, INFO_DEREFERENCE_POLICY_NEVER.get());
  public static final DereferencePolicy IN_SEARCHING =
      register(DEREF_IN_SEARCHING, INFO_DEREFERENCE_POLICY_IN_SEARCHING
          .get());
  public static final DereferencePolicy FINDING_BASE =
      register(DEREF_FINDING_BASE, INFO_DEREFERENCE_POLICY_FINDING_BASE
          .get());
  public static final DereferencePolicy ALWAYS =
      register(DEREF_ALWAYS, INFO_DEREFERENCE_POLICY_ALWAYS.get());



  public static DereferencePolicy valueOf(int intValue)
  {
    DereferencePolicy e = ELEMENTS[intValue];
    if (e == null)
    {
      e =
          new DereferencePolicy(intValue, INFO_UNDEFINED_TYPE
              .get(intValue));
    }
    return e;
  }



  public static List<DereferencePolicy> values()
  {
    return Arrays.asList(ELEMENTS);
  }



  private static DereferencePolicy register(int intValue, Message name)
  {
    DereferencePolicy t = new DereferencePolicy(intValue, name);
    ELEMENTS[intValue] = t;
    return t;
  }



  private final int intValue;

  private final Message name;



  private DereferencePolicy(int intValue, Message name)
  {
    this.intValue = intValue;
    this.name = name;
  }



  @Override
  public boolean equals(Object o)
  {
    return (this == o)
        || ((o instanceof DereferencePolicy) && (this.intValue == ((DereferencePolicy) o).intValue));

  }



  @Override
  public int hashCode()
  {
    return intValue;
  }



  public int intValue()
  {
    return intValue;
  }



  @Override
  public String toString()
  {
    return name.toString();
  }
}
