package org.opends.sdk;



import static org.opends.sdk.ldap.LDAPConstants.MOD_TYPE_ADD;
import static org.opends.sdk.ldap.LDAPConstants.MOD_TYPE_DELETE;
import static org.opends.sdk.ldap.LDAPConstants.MOD_TYPE_INCREMENT;
import static org.opends.sdk.ldap.LDAPConstants.MOD_TYPE_REPLACE;

import java.util.Arrays;
import java.util.List;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 18, 2009 Time:
 * 11:00:57 AM To change this template use File | Settings | File
 * Templates.
 */
public final class ModificationType
{
  private static final ModificationType[] ELEMENTS =
      new ModificationType[4];

  public static final ModificationType ADD =
      register(MOD_TYPE_ADD, "add");
  public static final ModificationType DELETE =
      register(MOD_TYPE_DELETE, "delete");
  public static final ModificationType REPLACE =
      register(MOD_TYPE_REPLACE, "replace");
  public static final ModificationType INCREMENT =
      register(MOD_TYPE_INCREMENT, "increment");



  public static ModificationType register(int intValue, String name)
  {
    ModificationType t = new ModificationType(intValue, name);
    ELEMENTS[intValue] = t;
    return t;
  }



  public static ModificationType valueOf(int intValue)
  {
    ModificationType e = ELEMENTS[intValue];
    if (e == null)
    {
      e =
          new ModificationType(intValue, String.format("undefined%d",
              intValue));
    }
    return e;
  }



  public static List<ModificationType> values()
  {
    return Arrays.asList(ELEMENTS);
  }

  private final int intValue;

  private final String name;



  private ModificationType(int intValue, String name)
  {
    this.intValue = intValue;
    this.name = name;
  }



  @Override
  public boolean equals(Object o)
  {
    return (this == o)
        || ((o instanceof ModificationType) && (this.intValue == ((ModificationType) o).intValue));

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
    return name;
  }
}
