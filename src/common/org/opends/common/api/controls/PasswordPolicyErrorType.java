package org.opends.common.api.controls;

import org.opends.messages.Message;
import static org.opends.messages.ProtocolMessages.*;
import static org.opends.messages.CoreMessages.INFO_UNDEFINED_TYPE;

import java.util.List;
import java.util.Arrays;

/**
 * This enumeration defines the set of password policy warnings that may be
 * included in the password policy response control defined in
 * draft-behera-ldap-password-policy.
 */
public class PasswordPolicyErrorType
{
   private static final PasswordPolicyErrorType[] ELEMENTS =
      new PasswordPolicyErrorType[9];

  public static final PasswordPolicyErrorType PASSWORD_EXPIRED =
      register(0, INFO_PWPERRTYPE_DESCRIPTION_PASSWORD_EXPIRED.get());
  public static final PasswordPolicyErrorType ACCOUNT_LOCKED =
      register(1, INFO_PWPERRTYPE_DESCRIPTION_ACCOUNT_LOCKED.get());
  public static final PasswordPolicyErrorType CHANGE_AFTER_RESET =
      register(2, INFO_PWPERRTYPE_DESCRIPTION_CHANGE_AFTER_RESET.get());
  public static final PasswordPolicyErrorType PASSWORD_MOD_NOT_ALLOWED =
      register(3, INFO_PWPERRTYPE_DESCRIPTION_PASSWORD_MOD_NOT_ALLOWED.get());
    public static final PasswordPolicyErrorType MUST_SUPPLY_OLD_PASSWORD =
      register(4, INFO_PWPERRTYPE_DESCRIPTION_MUST_SUPPLY_OLD_PASSWORD.get());
    public static final PasswordPolicyErrorType INSUFFICIENT_PASSWORD_QUALITY =
      register(5, INFO_PWPERRTYPE_DESCRIPTION_INSUFFICIENT_PASSWORD_QUALITY.get());
    public static final PasswordPolicyErrorType PASSWORD_TOO_SHORT =
      register(6, INFO_PWPERRTYPE_DESCRIPTION_PASSWORD_TOO_SHORT.get());
    public static final PasswordPolicyErrorType PASSWORD_TOO_YOUNG =
      register(7, INFO_PWPERRTYPE_DESCRIPTION_PASSWORD_TOO_YOUNG.get());
    public static final PasswordPolicyErrorType PASSWORD_IN_HISTORY =
      register(8, INFO_PWPERRTYPE_DESCRIPTION_PASSWORD_IN_HISTORY.get());

  private int intValue;
  private Message name;

  public static PasswordPolicyErrorType valueOf(int intValue)
  {
    PasswordPolicyErrorType e = ELEMENTS[intValue];
    if(e == null)
    {
      e = new PasswordPolicyErrorType(intValue,
          INFO_UNDEFINED_TYPE.get(intValue));
    }
    return e;
  }

  public static List<PasswordPolicyErrorType> values()
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

  private PasswordPolicyErrorType(int intValue, Message name)
  {
    this.intValue = intValue;
    this.name = name;
  }

  private static PasswordPolicyErrorType register(int intValue,
                                            Message name)
  {
    PasswordPolicyErrorType t =
        new PasswordPolicyErrorType(intValue, name);
    ELEMENTS[intValue] = t;
    return t;
  }

  @Override
  public boolean equals(Object o)
  {
    return this == o || o instanceof PasswordPolicyErrorType &&
        this.intValue == ((PasswordPolicyErrorType) o).intValue;

  }

  @Override
  public int hashCode()
  {
    return intValue;
  }
}
