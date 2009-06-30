package org.opends.common.api.controls;

import org.opends.messages.Message;
import static org.opends.messages.CoreMessages.INFO_UNDEFINED_TYPE;

import java.util.*;

  /**
 * This enumeration defines the set of possible change types that may be used in
 * conjunction with the persistent search control, as defined in
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

  private int intValue;
  private Message name;

  public static PersistentSearchChangeType valueOf(int intValue)
  {
    PersistentSearchChangeType e = ELEMENTS[intValue];
    if(e == null)
    {
      e = new PersistentSearchChangeType(intValue,
          INFO_UNDEFINED_TYPE.get(intValue));
    }
    return e;
  }

  public static List<PersistentSearchChangeType> values()
  {
    return Arrays.asList(ELEMENTS);
  }

  public int intValue()
  {
    return intValue;
  }

  public String toString()
  {
    return name.toString();
  }

  private PersistentSearchChangeType(int intValue, Message name)
  {
    this.intValue = intValue;
    this.name = name;
  }

  private static PersistentSearchChangeType register(int intValue,
                                            Message name)
  {
    PersistentSearchChangeType t =
        new PersistentSearchChangeType(intValue, name);
    ELEMENTS[intValue] = t;
    return t;
  }

  @Override
  public boolean equals(Object o)
  {
    return this == o || o instanceof PersistentSearchChangeType &&
        this.intValue == ((PersistentSearchChangeType) o).intValue;

  }

  @Override
  public int hashCode()
  {
    return intValue;
  }
}
