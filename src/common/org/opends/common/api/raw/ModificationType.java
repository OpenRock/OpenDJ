package org.opends.common.api.raw;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import static org.opends.server.protocols.ldap.LDAPConstants.*;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 18, 2009 Time: 11:00:57
 * AM To change this template use File | Settings | File Templates.
 */
public final class ModificationType
{
  private static final ArrayList<ModificationType> ELEMENTS =
      new ArrayList<ModificationType>(4);

  public static final ModificationType ADD =
      register(MOD_TYPE_ADD, "add");
  public static final ModificationType DELETE =
      register(MOD_TYPE_DELETE, "delete");
  public static final ModificationType REPLACE =
      register(MOD_TYPE_REPLACE, "replace");
  public static final ModificationType INCREMENT =
      register(MOD_TYPE_INCREMENT, "increment");

  private int intValue;
  private String name;

  public static ModificationType valueOf(int intValue)
  {
    ModificationType e = ELEMENTS.get(intValue);
    if(e == null)
    {
      e = new ModificationType(intValue, "undefined("+intValue+")");
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
    return name;
  }

  private ModificationType(int intValue, String name)
  {
    this.intValue = intValue;
    this.name = name;
  }

  public final static ModificationType register(int intValue, String name)
  {
    ModificationType t = new ModificationType(intValue, name);
    ELEMENTS.add(intValue, t);
    return t;
  }
}
