package org.opends.ldap.controls;



import static org.opends.messages.CoreMessages.INFO_UNDEFINED_TYPE;

import java.util.Arrays;
import java.util.List;

import org.opends.messages.Message;



/**
 * This enumeration defines the set of possible change types that may be
 * used in conjunction with the persistent search control, as defined in
 * draft-ietf-ldapext-psearch.
 */
public final class PersistentSearchChangeType
{
  private static final PersistentSearchChangeType[] ELEMENTS =
      new PersistentSearchChangeType[4];

  public static final PersistentSearchChangeType ADD =
      register(1, Message.raw("add"));
  public static final PersistentSearchChangeType DELETE =
      register(2, Message.raw("delete"));
  public static final PersistentSearchChangeType MODIFY =
      register(4, Message.raw("modify"));
  public static final PersistentSearchChangeType MODIFY_DN =
      register(8, Message.raw("modify DN"));



  public static PersistentSearchChangeType valueOf(int intValue)
  {
    PersistentSearchChangeType e = ELEMENTS[intValue];
    if (e == null)
    {
      e =
          new PersistentSearchChangeType(intValue, INFO_UNDEFINED_TYPE
              .get(intValue));
    }
    return e;
  }



  public static List<PersistentSearchChangeType> values()
  {
    return Arrays.asList(ELEMENTS);
  }



  private static PersistentSearchChangeType register(int intValue,
      Message name)
  {
    PersistentSearchChangeType t =
        new PersistentSearchChangeType(intValue, name);
    ELEMENTS[intValue] = t;
    return t;
  }



  private final int intValue;

  private final Message name;



  private PersistentSearchChangeType(int intValue, Message name)
  {
    this.intValue = intValue;
    this.name = name;
  }



  @Override
  public boolean equals(Object o)
  {
    return (this == o)
        || ((o instanceof PersistentSearchChangeType) && (this.intValue == ((PersistentSearchChangeType) o).intValue));

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
