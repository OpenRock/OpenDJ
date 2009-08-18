package org.opends.types;



import static org.opends.messages.CoreMessages.INFO_DEREFERENCE_POLICY_ALWAYS;
import static org.opends.messages.CoreMessages.INFO_DEREFERENCE_POLICY_FINDING_BASE;
import static org.opends.messages.CoreMessages.INFO_DEREFERENCE_POLICY_IN_SEARCHING;
import static org.opends.messages.CoreMessages.INFO_DEREFERENCE_POLICY_NEVER;
import static org.opends.messages.CoreMessages.INFO_UNDEFINED_TYPE;
import static org.opends.server.protocols.ldap.LDAPConstants.DEREF_ALWAYS;
import static org.opends.server.protocols.ldap.LDAPConstants.DEREF_FINDING_BASE;
import static org.opends.server.protocols.ldap.LDAPConstants.DEREF_IN_SEARCHING;
import static org.opends.server.protocols.ldap.LDAPConstants.DEREF_NEVER;

import java.util.Arrays;
import java.util.List;

import org.opends.messages.Message;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 18, 2009 Time:
 * 5:23:36 PM To change this template use File | Settings | File
 * Templates.
 */
public final class DereferenceAliasesPolicy
{
  private static final DereferenceAliasesPolicy[] ELEMENTS =
      new DereferenceAliasesPolicy[4];

  public static final DereferenceAliasesPolicy NEVER =
      register(DEREF_NEVER, INFO_DEREFERENCE_POLICY_NEVER.get());
  public static final DereferenceAliasesPolicy IN_SEARCHING =
      register(DEREF_IN_SEARCHING, INFO_DEREFERENCE_POLICY_IN_SEARCHING
          .get());
  public static final DereferenceAliasesPolicy FINDING_BASE =
      register(DEREF_FINDING_BASE, INFO_DEREFERENCE_POLICY_FINDING_BASE
          .get());
  public static final DereferenceAliasesPolicy ALWAYS =
      register(DEREF_ALWAYS, INFO_DEREFERENCE_POLICY_ALWAYS.get());



  public static DereferenceAliasesPolicy valueOf(int intValue)
  {
    DereferenceAliasesPolicy e = ELEMENTS[intValue];
    if (e == null)
    {
      e =
          new DereferenceAliasesPolicy(intValue, INFO_UNDEFINED_TYPE
              .get(intValue));
    }
    return e;
  }



  public static List<DereferenceAliasesPolicy> values()
  {
    return Arrays.asList(ELEMENTS);
  }



  private static DereferenceAliasesPolicy register(int intValue, Message name)
  {
    DereferenceAliasesPolicy t = new DereferenceAliasesPolicy(intValue, name);
    ELEMENTS[intValue] = t;
    return t;
  }



  private final int intValue;

  private final Message name;



  private DereferenceAliasesPolicy(int intValue, Message name)
  {
    this.intValue = intValue;
    this.name = name;
  }



  @Override
  public boolean equals(Object o)
  {
    return (this == o)
        || ((o instanceof DereferenceAliasesPolicy) && (this.intValue == ((DereferenceAliasesPolicy) o).intValue));

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
