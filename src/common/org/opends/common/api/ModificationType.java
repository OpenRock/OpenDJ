package org.opends.common.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import static org.opends.server.protocols.ldap.LDAPConstants.*;
import static org.opends.messages.CoreMessages.*;
import org.opends.messages.Message;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 18, 2009 Time: 11:00:57
 * AM To change this template use File | Settings | File Templates.
 */
public final class ModificationType
{
  private static final ArrayList<ModificationType> ELEMENTS =
      new ArrayList<ModificationType>(4);

  public static final ModificationType ADD =
      register(MOD_TYPE_ADD, INFO_MODIFICATION_TYPE_ADD.get());
  public static final ModificationType DELETE =
      register(MOD_TYPE_DELETE, INFO_MODIFICATION_TYPE_DELETE.get());
  public static final ModificationType REPLACE =
      register(MOD_TYPE_REPLACE,
          INFO_MODIFICATION_TYPE_REPLACE.get());
  public static final ModificationType INCREMENT =
      register(MOD_TYPE_INCREMENT,
          INFO_MODIFICATION_TYPE_INCREMENT.get());

  private int intValue;
  private Message name;

  public static ModificationType valueOf(int intValue)
  {
    ModificationType e = ELEMENTS.get(intValue);
    if(e == null)
    {
      e = new ModificationType(intValue,
          INFO_UNDEFINED_TYPE.get(intValue));
    }
    return e;
  }

  public static List<ModificationType> values()
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

  private ModificationType(int intValue, Message name)
  {
    this.intValue = intValue;
    this.name = name;
  }

  public static ModificationType register(int intValue, Message name)
  {
    ModificationType t = new ModificationType(intValue, name);
    ELEMENTS.add(intValue, t);
    return t;
  }

  @Override
  public boolean equals(Object o)
  {
    return this == o || o instanceof ModificationType &&
        this.intValue == ((ModificationType) o).intValue;

  }

  @Override
  public int hashCode()
  {
    return intValue;
  }
}
