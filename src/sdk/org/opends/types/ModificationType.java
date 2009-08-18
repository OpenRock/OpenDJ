package org.opends.types;



import static org.opends.messages.CoreMessages.INFO_MODIFICATION_TYPE_ADD;
import static org.opends.messages.CoreMessages.INFO_MODIFICATION_TYPE_DELETE;
import static org.opends.messages.CoreMessages.INFO_MODIFICATION_TYPE_INCREMENT;
import static org.opends.messages.CoreMessages.INFO_MODIFICATION_TYPE_REPLACE;
import static org.opends.messages.CoreMessages.INFO_UNDEFINED_TYPE;
import static org.opends.server.protocols.ldap.LDAPConstants.MOD_TYPE_ADD;
import static org.opends.server.protocols.ldap.LDAPConstants.MOD_TYPE_DELETE;
import static org.opends.server.protocols.ldap.LDAPConstants.MOD_TYPE_INCREMENT;
import static org.opends.server.protocols.ldap.LDAPConstants.MOD_TYPE_REPLACE;

import java.util.Arrays;
import java.util.List;

import org.opends.messages.Message;



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
      register(MOD_TYPE_ADD, INFO_MODIFICATION_TYPE_ADD.get());
  public static final ModificationType DELETE =
      register(MOD_TYPE_DELETE, INFO_MODIFICATION_TYPE_DELETE.get());
  public static final ModificationType REPLACE =
      register(MOD_TYPE_REPLACE, INFO_MODIFICATION_TYPE_REPLACE.get());
  public static final ModificationType INCREMENT =
      register(MOD_TYPE_INCREMENT, INFO_MODIFICATION_TYPE_INCREMENT
          .get());



  public static ModificationType register(int intValue, Message name)
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
          new ModificationType(intValue, INFO_UNDEFINED_TYPE
              .get(intValue));
    }
    return e;
  }



  public static List<ModificationType> values()
  {
    return Arrays.asList(ELEMENTS);
  }



  private final int intValue;

  private final Message name;



  private ModificationType(int intValue, Message name)
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
    return name.toString();
  }
}
