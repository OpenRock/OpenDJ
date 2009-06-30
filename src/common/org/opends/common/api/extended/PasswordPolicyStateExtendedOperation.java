package org.opends.common.api.extended;

import org.opends.server.loggers.debug.DebugTracer;
import static org.opends.server.loggers.debug.DebugLogger.getTracer;
import static org.opends.server.util.ServerConstants.OID_PASSWORD_POLICY_STATE_EXTOP;
import static org.opends.server.util.StaticUtils.getExceptionMessage;
import org.opends.server.util.Validator;
import org.opends.server.types.ByteString;
import org.opends.server.types.ByteStringBuilder;
import org.opends.server.schema.GeneralizedTimeSyntax;
import org.opends.common.api.ResultCode;
import org.opends.common.api.DecodeException;
import org.opends.common.api.DN;
import org.opends.common.protocols.asn1.ASN1Reader;
import org.opends.common.protocols.asn1.ASN1;
import org.opends.common.protocols.asn1.ASN1Writer;
import org.opends.messages.Message;
import static org.opends.messages.ExtensionMessages.*;

import java.io.IOException;
import java.util.*;

/**
 * This class implements an LDAP extended operation that can be used to query
 * and update elements of the Directory Server password policy state for a given
 * user.  The ASN.1 definition for the value of the extended request is:
 * <BR>
 * <PRE>
 * PasswordPolicyStateValue ::= SEQUENCE {
 *      targetUser     LDAPDN
 *      operations     SEQUENCE OF PasswordPolicyStateOperation OPTIONAL }
 *
 * PasswordPolicyStateOperation ::= SEQUENCE {
 *      opType       ENUMERATED {
 *           getPasswordPolicyDN                          (0),
 *           getAccountDisabledState                      (1),
 *           setAccountDisabledState                      (2),
 *           clearAccountDisabledState                    (3),
 *           getAccountExpirationTime                     (4),
 *           setAccountExpirationTime                     (5),
 *           clearAccountExpirationTime                   (6),
 *           getSecondsUntilAccountExpiration             (7),
 *           getPasswordChangedTime                       (8),
 *           setPasswordChangedTime                       (9),
 *           clearPasswordChangedTime                     (10),
 *           getPasswordExpirationWarnedTime              (11),
 *           setPasswordExpirationWarnedTime              (12),
 *           clearPasswordExpirationWarnedTime            (13),
 *           getSecondsUntilPasswordExpiration            (14),
 *           getSecondsUntilPasswordExpirationWarning     (15),
 *           getAuthenticationFailureTimes                (16),
 *           addAuthenticationFailureTime                 (17),
 *           setAuthenticationFailureTimes                (18),
 *           clearAuthenticationFailureTimes              (19),
 *           getSecondsUntilAuthenticationFailureUnlock   (20),
 *           getRemainingAuthenticationFailureCount       (21),
 *           getLastLoginTime                             (22),
 *           setLastLoginTime                             (23),
 *           clearLastLoginTime                           (24),
 *           getSecondsUntilIdleLockout                   (25),
 *           getPasswordResetState                        (26),
 *           setPasswordResetState                        (27),
 *           clearPasswordResetState                      (28),
 *           getSecondsUntilPasswordResetLockout          (29),
 *           getGraceLoginUseTimes                        (30),
 *           addGraceLoginUseTime                         (31),
 *           setGraceLoginUseTimes                        (32),
 *           clearGraceLoginUseTimes                      (33),
 *           getRemainingGraceLoginCount                  (34),
 *           getPasswordChangedByRequiredTime             (35),
 *           setPasswordChangedByRequiredTime             (36),
 *           clearPasswordChangedByRequiredTime           (37),
 *           getSecondsUntilRequiredChangeTime            (38),
 *           getPasswordHistory                           (39),
 *           clearPasswordHistory                         (40),
 *           ... },
 *      opValues     SEQUENCE OF OCTET STRING OPTIONAL }
 * </PRE>
 * <BR>
 * Both the request and response values use the same encoded form, and they both
 * use the same OID of "1.3.6.1.4.1.26027.1.6.1".  The response value will only
 * include get* elements.  If the request did not include any operations, then
 * the response will include all get* elements; otherwise, the response will
 * only include the get* elements that correspond to the state fields referenced
 * in the request (regardless of whether that operation was included in a get*,
 * set*, add*, remove*, or clear* operation).
 */
public final class PasswordPolicyStateExtendedOperation
    extends AbstractExtendedOperation
{
  /**
   * The tracer object for the debug logger.
   */
  private static final DebugTracer TRACER = getTracer();

  private static final PasswordPolicyStateExtendedOperation SINGLETON =
      new PasswordPolicyStateExtendedOperation();

  private static final String PASSWORD_POLICY_DN_NAME =
      "Password Policy DN";
  private static final String ACCOUNT_DISABLED_STATE_NAME =
      "Account Disabled State";
  private static final String ACCOUNT_EXPIRATION_TIME_NAME =
      "Account Expiration Time";
  private static final String SECONDS_UNTIL_ACCOUNT_EXPIRATION_NAME =
      "Seconds Until Account Expiration";
  private static final String PASSWORD_CHANGED_TIME_NAME =
      "Password Changed Time";
  private static final String PASSWORD_EXPIRATION_WARNED_TIME_NAME =
      "Password Expiration Warned Time";
  private static final String SECONDS_UNTIL_PASSWORD_EXPIRATION_NAME =
      "Seconds Until Password Expiration";
  private static final String
      SECONDS_UNTIL_PASSWORD_EXPIRATION_WARNING_NAME =
      "Seconds Until Password Expiration Warning";
  private static final String AUTHENTICATION_FAILURE_TIMES_NAME =
      "Authentication Failure Times";
  private static final String
      SECONDS_UNTIL_AUTHENTICATION_FAILURE_UNLOCK_NAME =
      "Seconds Until Authentication Failure Unlock";
  private static final String
      REMAINING_AUTHENTICATION_FAILURE_COUNT_NAME =
      "Remaining Authentication Failure Count";
  private static final String LAST_LOGIN_TIME_NAME =
      "Last Login Time";
  private static final String SECONDS_UNTIL_IDLE_LOCKOUT_NAME =
      "Seconds Until Idle Lockout";
  private static final String PASSWORD_RESET_STATE_NAME =
      "Password Reset State";
  private static final String
      SECONDS_UNTIL_PASSWORD_RESET_LOCKOUT_NAME =
      "Seconds Until Password Reset Lockout";
  private static final String GRACE_LOGIN_USE_TIMES_NAME =
      "Grace Login Use Times";
  private static final String REMAINING_GRACE_LOGIN_COUNT_NAME =
      "Remaining Grace Login Count";
  private static final String PASSWORD_CHANGED_BY_REQUIRED_TIME_NAME =
      "Password Changed By Required Time";
  private static final String
      SECONDS_UNTIL_REQUIRED_CHANGE_TIME_NAME =
      "Seconds Until Required Change Time";
  private static final String PASSWORD_HISTORY_NAME =
      "Password History";

  public static enum OperationType implements Operation
  {
    GET_PASSWORD_POLICY_DN(PASSWORD_POLICY_DN_NAME),

    GET_ACCOUNT_DISABLED_STATE(ACCOUNT_DISABLED_STATE_NAME),
    SET_ACCOUNT_DISABLED_STATE(ACCOUNT_DISABLED_STATE_NAME),
    CLEAR_ACCOUNT_DISABLED_STATE(ACCOUNT_DISABLED_STATE_NAME),

    GET_ACCOUNT_EXPIRATION_TIME(ACCOUNT_EXPIRATION_TIME_NAME),
    SET_ACCOUNT_EXPIRATION_TIME(ACCOUNT_EXPIRATION_TIME_NAME),
    CLEAR_ACCOUNT_EXPIRATION_TIME(ACCOUNT_EXPIRATION_TIME_NAME),

    GET_SECONDS_UNTIL_ACCOUNT_EXPIRATION(
        SECONDS_UNTIL_ACCOUNT_EXPIRATION_NAME),

    GET_PASSWORD_CHANGED_TIME(PASSWORD_CHANGED_TIME_NAME),
    SET_PASSWORD_CHANGED_TIME(PASSWORD_CHANGED_TIME_NAME),
    CLEAR_PASSWORD_CHANGED_TIME(PASSWORD_CHANGED_TIME_NAME),

    GET_PASSWORD_EXPIRATION_WARNED_TIME(
        PASSWORD_EXPIRATION_WARNED_TIME_NAME),
    SET_PASSWORD_EXPIRATION_WARNED_TIME(
        PASSWORD_EXPIRATION_WARNED_TIME_NAME),
    CLEAR_PASSWORD_EXPIRATION_WARNED_TIME(
        PASSWORD_EXPIRATION_WARNED_TIME_NAME),

    GET_SECONDS_UNTIL_PASSWORD_EXPIRATION(
        SECONDS_UNTIL_PASSWORD_EXPIRATION_NAME),

    GET_SECONDS_UNTIL_PASSWORD_EXPIRATION_WARNING(
        SECONDS_UNTIL_PASSWORD_EXPIRATION_WARNING_NAME),

    GET_AUTHENTICATION_FAILURE_TIMES(
        AUTHENTICATION_FAILURE_TIMES_NAME),
    ADD_AUTHENTICATION_FAILURE_TIMES(
        AUTHENTICATION_FAILURE_TIMES_NAME),
    SET_AUTHENTICATION_FAILURE_TIMES(
        AUTHENTICATION_FAILURE_TIMES_NAME),
    CLEAR_AUTHENTICATION_FAILURE_TIMES(
        AUTHENTICATION_FAILURE_TIMES_NAME),

    GET_SECONDS_UNTIL_AUTHENTICATION_FAILURE_UNLOCK(
        SECONDS_UNTIL_AUTHENTICATION_FAILURE_UNLOCK_NAME),

    GET_REMAINING_AUTHENTICATION_FAILURE_COUNT(
        REMAINING_AUTHENTICATION_FAILURE_COUNT_NAME),

    GET_LAST_LOGIN_TIME(LAST_LOGIN_TIME_NAME),
    SET_LAST_LOGIN_TIME(LAST_LOGIN_TIME_NAME),
    CLEAR_LAST_LOGIN_TIME(LAST_LOGIN_TIME_NAME),

    GET_SECONDS_UNTIL_IDLE_LOCKOUT(
        SECONDS_UNTIL_IDLE_LOCKOUT_NAME),

    GET_PASSWORD_RESET_STATE(PASSWORD_RESET_STATE_NAME),
    SET_PASSWORD_RESET_STATE(PASSWORD_RESET_STATE_NAME),
    CLEAR_PASSWORD_RESET_STATE(PASSWORD_RESET_STATE_NAME),

    GET_SECONDS_UNTIL_PASSWORD_RESET_LOCKOUT(
        SECONDS_UNTIL_PASSWORD_RESET_LOCKOUT_NAME),

    GET_GRACE_LOGIN_USE_TIMES(GRACE_LOGIN_USE_TIMES_NAME),
    ADD_GRACE_LOGIN_USE_TIME(GRACE_LOGIN_USE_TIMES_NAME),
    SET_GRACE_LOGIN_USE_TIMES(GRACE_LOGIN_USE_TIMES_NAME),
    CLEAR_GRACE_LOGIN_USE_TIMES(GRACE_LOGIN_USE_TIMES_NAME),

    GET_REMAINING_GRACE_LOGIN_COUNT(
        REMAINING_GRACE_LOGIN_COUNT_NAME),

    GET_PASSWORD_CHANGED_BY_REQUIRED_TIME(
        PASSWORD_CHANGED_BY_REQUIRED_TIME_NAME),
    SET_PASSWORD_CHANGED_BY_REQUIRED_TIME(
        PASSWORD_CHANGED_BY_REQUIRED_TIME_NAME),
    CLEAR_PASSWORD_CHANGED_BY_REQUIRED_TIME(
        PASSWORD_CHANGED_BY_REQUIRED_TIME_NAME),

    GET_SECONDS_UNTIL_REQUIRED_CHANGE_TIME(
        SECONDS_UNTIL_REQUIRED_CHANGE_TIME_NAME),

    GET_PASSWORD_HISTORY(PASSWORD_HISTORY_NAME),
    CLEAR_PASSWORD_HISTORY(PASSWORD_HISTORY_NAME);

    private String propertyName;

    OperationType(String propertyName) {
      this.propertyName = propertyName;
    }

    public String getPropertyName() {
      return propertyName;
    }


    public OperationType getOperationType() {
      return this;
    }

    public Iterable<ByteString> getValues() {
      return null;
    }

    @Override
    public String toString() {
      return propertyName;
    }
  }

  public interface Operation
  {
    public OperationType getOperationType();
    public Iterable<ByteString> getValues();
  }

  private interface OperationContainer
  {
    public void addOperation(Operation operation);

    public Iterable<Operation> getOperations();
  }

  private static class MultiValueOperation implements Operation
  {
    OperationType property;
    List<ByteString> values;

    private MultiValueOperation(OperationType property,
                                List<ByteString> values) {
      this.property = property;
      this.values = values;
    }

    private MultiValueOperation(OperationType property,
                                ByteString value)
    {
      this.property = property;
      this.values = Collections.singletonList(value);
    }

    public OperationType getOperationType() {
      return property;
    }

    public Iterable<ByteString> getValues() {
      return values;
    }

    @Override
    public String toString() {
      return property.getPropertyName() + ": " + values;
    }
  }

  private PasswordPolicyStateExtendedOperation() {
    super();
    // We could register the result codes here if they are not
    // already included in the default set.
  }

  public static class Request extends
    ExtendedRequest<PasswordPolicyStateExtendedOperation>
    implements OperationContainer
  {
    String targetUser;
    List<Operation> operations = new ArrayList<Operation>();

    public Request(String targetUser)
    {
      super(OID_PASSWORD_POLICY_STATE_EXTOP);
      Validator.ensureNotNull(targetUser);
      this.targetUser = targetUser;
    }

    public Request(DN targetUser)
    {
      super(OID_PASSWORD_POLICY_STATE_EXTOP);
      Validator.ensureNotNull(targetUser);
      this.targetUser = targetUser.toString();
    }

    public PasswordPolicyStateExtendedOperation getExtendedOperation()
    {
      return SINGLETON;
    }

    public void requestPasswordPolicyDN()
    {
      operations.add(OperationType.GET_PASSWORD_POLICY_DN);
    }

    public void requestAccountDisabledState()
    {
      operations.add(OperationType.GET_ACCOUNT_DISABLED_STATE);
    }

    public void setAccountDisabledState(boolean state)
    {
      operations.add(new MultiValueOperation(
          OperationType.SET_ACCOUNT_DISABLED_STATE,
          ByteString.valueOf(String.valueOf(state))));
    }

    public void clearAccountDisabledState()
    {
      operations.add(OperationType.CLEAR_ACCOUNT_DISABLED_STATE);
    }

    public void requestAccountExpirationTime()
    {
      operations.add(OperationType.GET_ACCOUNT_EXPIRATION_TIME);
    }

    public void setAccountExpirationTime(Date date)
    {
      if(date == null)
      {
        operations.add(OperationType.SET_ACCOUNT_EXPIRATION_TIME);
      }
      else
      {
        operations.add(new MultiValueOperation(
            OperationType.SET_ACCOUNT_EXPIRATION_TIME,
            ByteString.valueOf(GeneralizedTimeSyntax.format(date))));
      }
    }

    public void clearAccountExpirationTime()
    {
      operations.add(OperationType.CLEAR_ACCOUNT_EXPIRATION_TIME);
    }

    public void requestSecondsUntilAccountExpiration()
    {
      operations.add(
          OperationType.GET_SECONDS_UNTIL_ACCOUNT_EXPIRATION);
    }

    public void requestPasswordChangedTime()
    {
      operations.add(OperationType.GET_PASSWORD_CHANGED_TIME);
    }

    public void setPasswordChangedTime(Date date)
    {
      if(date == null)
      {
        operations.add(OperationType.SET_PASSWORD_CHANGED_TIME);
      }
      else
      {
        operations.add(new MultiValueOperation(
            OperationType.SET_PASSWORD_CHANGED_TIME,
            ByteString.valueOf(GeneralizedTimeSyntax.format(date))));
      }
    }

    public void clearPasswordChangedTime()
    {
      operations.add(OperationType.CLEAR_PASSWORD_CHANGED_TIME);
    }

    public void requestPasswordExpirationWarnedTime()
    {
      operations.add(
          OperationType.GET_PASSWORD_EXPIRATION_WARNED_TIME);
    }

    public void setPasswordExpirationWarnedTime(Date date)
    {
      if(date == null)
      {
        operations.add(
            OperationType.SET_PASSWORD_EXPIRATION_WARNED_TIME);

      }
      else
      {
        operations.add(new MultiValueOperation(
            OperationType.SET_PASSWORD_EXPIRATION_WARNED_TIME,
            ByteString.valueOf(GeneralizedTimeSyntax.format(date))));
      }
    }

    public void clearPasswordExpirationWarnedTime()
    {
      operations.add(
          OperationType.CLEAR_PASSWORD_EXPIRATION_WARNED_TIME);
    }

    public void requestSecondsUntilPasswordExpiration()
    {
      operations.add(
          OperationType.GET_SECONDS_UNTIL_PASSWORD_EXPIRATION);
    }

    public void requestSecondsUntilPasswordExpirationWarning()
    {
      operations.add(
          OperationType.GET_SECONDS_UNTIL_PASSWORD_EXPIRATION_WARNING);
    }

    public void requestAuthenticationFailureTimes()
    {
      operations.add(OperationType.GET_AUTHENTICATION_FAILURE_TIMES);
    }

    public void addAuthenticationFailureTime(Date date)
    {
      if(date == null)
      {
        operations.add(
            OperationType.ADD_AUTHENTICATION_FAILURE_TIMES);
      }
      else
      {
        operations.add(new MultiValueOperation(
            OperationType.ADD_AUTHENTICATION_FAILURE_TIMES,
            ByteString.valueOf(GeneralizedTimeSyntax.format(date))));
      }
    }

    public void setAuthenticationFailureTimes(Date... dates)
    {
      if(dates == null)
      {
        operations.add(OperationType.SET_AUTHENTICATION_FAILURE_TIMES);
      }
      else
      {
        ArrayList<ByteString> times =
            new ArrayList<ByteString>(dates.length);
        for(Date date : dates)
        {
          times.add(ByteString.valueOf(
              GeneralizedTimeSyntax.format(date)));
        }
        operations.add(new MultiValueOperation(
            OperationType.SET_AUTHENTICATION_FAILURE_TIMES, times));
      }
    }

    public void clearAuthenticationFailureTimes()
    {
      operations.add(
          OperationType.CLEAR_AUTHENTICATION_FAILURE_TIMES);
    }

    public void requestSecondsUntilAuthenticationFailureUnlock()
    {
      operations.add(
          OperationType.GET_SECONDS_UNTIL_AUTHENTICATION_FAILURE_UNLOCK);
    }

    public void requestRemainingAuthenticationFailureCount()
    {
      operations.add(
          OperationType.GET_REMAINING_AUTHENTICATION_FAILURE_COUNT);
    }

    public void requestLastLoginTime()
    {
      operations.add(OperationType.GET_LAST_LOGIN_TIME);
    }

    public void setLastLoginTime(Date date)
    {
      if(date == null)
      {
        operations.add(OperationType.SET_LAST_LOGIN_TIME);

      }
      else
      {
        operations.add(new MultiValueOperation(
            OperationType.SET_LAST_LOGIN_TIME,
            ByteString.valueOf(GeneralizedTimeSyntax.format(date))));
      }
    }

    public void clearLastLoginTime()
    {
      operations.add(OperationType.CLEAR_LAST_LOGIN_TIME);
    }

    public void requestSecondsUntilIdleLockout()
    {
      operations.add(OperationType.GET_SECONDS_UNTIL_IDLE_LOCKOUT);
    }

    public void requestPasswordResetState()
    {
      operations.add(OperationType.GET_PASSWORD_RESET_STATE);
    }

    public void setPasswordResetState(boolean state)
    {
      operations.add(new MultiValueOperation(
          OperationType.SET_LAST_LOGIN_TIME,
          ByteString.valueOf(String.valueOf(state))));
    }

    public void clearPasswordResetState()
    {
      operations.add(OperationType.CLEAR_PASSWORD_RESET_STATE);
    }

    public void requestSecondsUntilPasswordResetLockout()
    {
      operations.add(
          OperationType.GET_SECONDS_UNTIL_PASSWORD_RESET_LOCKOUT);
    }

    public void requestGraceLoginUseTimes()
    {
      operations.add(OperationType.GET_GRACE_LOGIN_USE_TIMES);
    }

    public void addGraceLoginUseTime(Date date)
    {
      if(date == null)
      {
        operations.add(OperationType.ADD_GRACE_LOGIN_USE_TIME);
      }
      else
      {
        operations.add(new MultiValueOperation(
            OperationType.ADD_GRACE_LOGIN_USE_TIME,
            ByteString.valueOf(GeneralizedTimeSyntax.format(date))));
      }
    }

    public void setGraceLoginUseTimes(Date... dates)
    {
      if(dates == null)
      {
        operations.add(OperationType.SET_GRACE_LOGIN_USE_TIMES);
      }
      else
      {
        ArrayList<ByteString> times =
            new ArrayList<ByteString>(dates.length);
        for(Date date : dates)
        {
          times.add(ByteString.valueOf(
              GeneralizedTimeSyntax.format(date)));
        }
        operations.add(new MultiValueOperation(
            OperationType.SET_GRACE_LOGIN_USE_TIMES, times));
      }
    }

    public void clearGraceLoginUseTimes()
    {
      operations.add(OperationType.CLEAR_GRACE_LOGIN_USE_TIMES);
    }

    public void requestRemainingGraceLoginCount()
    {
      operations.add(OperationType.GET_REMAINING_GRACE_LOGIN_COUNT);
    }

    public void requestPasswordChangedByRequiredTime()
    {
      operations.add(
          OperationType.GET_PASSWORD_CHANGED_BY_REQUIRED_TIME);
    }

    public void setPasswordChangedByRequiredTime(boolean state)
    {
      operations.add(new MultiValueOperation(
          OperationType.SET_PASSWORD_CHANGED_BY_REQUIRED_TIME,
          ByteString.valueOf(String.valueOf(state))));
    }

    public void clearPasswordChangedByRequiredTime()
    {
      operations.add(
          OperationType.CLEAR_PASSWORD_CHANGED_BY_REQUIRED_TIME);
    }

    public void requestSecondsUntilRequiredChangeTime()
    {
      operations.add(
          OperationType.GET_SECONDS_UNTIL_REQUIRED_CHANGE_TIME);
    }

    public void requestPasswordHistory()
    {
      operations.add(
          OperationType.GET_PASSWORD_HISTORY);
    }

    public void clearPasswordHistory()
    {
      operations.add(OperationType.CLEAR_PASSWORD_HISTORY);
    }

    public void addOperation(Operation operation)
    {
      operations.add(operation);
    }

    public Iterable<Operation> getOperations()
    {
      return operations;
    }

    public ByteString getRequestValue() {
      return encode(targetUser, operations);
    }

    public void toString(StringBuilder buffer) {
      buffer.append("PasswordPolicyStateExtendedRequest(requestName=");
      buffer.append(requestName);
      buffer.append(", targetUser=");
      buffer.append(targetUser);
      buffer.append(", operations=");
      buffer.append(operations);
      buffer.append(", controls=");
      buffer.append(getControls());
      buffer.append(")");
    }
  }

  public static class Response extends
      ExtendedResponse<PasswordPolicyStateExtendedOperation>
      implements OperationContainer
  {
    String targetUser;
    List<Operation> operations = new ArrayList<Operation>();

    public Response(ResultCode resultCode,
                                               String matchedDN,
                                               String diagnosticMessage,
                                               String targetUser)
    {
      super(resultCode, matchedDN, diagnosticMessage);
      this.responseName = OID_PASSWORD_POLICY_STATE_EXTOP;
      Validator.ensureNotNull(targetUser);
      this.targetUser = targetUser;
    }

    public Response(ResultCode resultCode,
                                               String matchedDN,
                                               String diagnosticMessage,
                                               DN targetUser)
    {
      super(resultCode, matchedDN, diagnosticMessage);
      this.responseName = OID_PASSWORD_POLICY_STATE_EXTOP;
      Validator.ensureNotNull(targetUser);
      this.targetUser = targetUser.toString();
    }

    public PasswordPolicyStateExtendedOperation getExtendedOperation() {
      return SINGLETON;
    }

    public void addOperation(Operation operation)
    {
      operations.add(operation);
    }

    public Iterable<Operation> getOperations()
    {
      return operations;
    }

    public ByteString getResponseValue() {
      return encode(targetUser, operations);
    }

    public void toString(StringBuilder buffer) {
      buffer.append("PasswordPolicyStateExtendedResponse(resultCode=");
      buffer.append(resultCode);
      buffer.append(", matchedDN=");
      buffer.append(matchedDN);
      buffer.append(", diagnosticMessage=");
      buffer.append(diagnosticMessage);
      buffer.append(", referrals=");
      buffer.append(referrals);
      buffer.append(", responseName=");
      buffer.append(responseName);
      buffer.append(", targetUser=");
      buffer.append(targetUser);
      buffer.append(", operations=");
      buffer.append(operations);
      buffer.append(", controls=");
      buffer.append(getControls());
      buffer.append(")");
    }
  }



  @Override
  public Request decodeRequest(String requestName,
                                                          ByteString requestValue)
      throws DecodeException
  {
    if(requestValue == null || requestValue.length() <= 0)
    {
      throw new DecodeException(ERR_PWPSTATE_EXTOP_NO_REQUEST_VALUE.get());
    }

    try
    {
      ASN1Reader reader = ASN1.getReader(requestValue);
      reader.readStartSequence();

      // Read the target user DN
      Request request =
          new Request(
              reader.readOctetStringAsString());

      decodeOperations(reader, request);
      reader.readEndSequence();
      return request;
    }
    catch(IOException ioe)
    {
      Message message =
          ERR_EXTOP_CANCEL_CANNOT_DECODE_REQUEST_VALUE.get(
              getExceptionMessage(ioe));
      throw new DecodeException(message, ioe);
    }
  }

  @Override
  public Response decodeResponse(
      ResultCode resultCode, String matchedDN,
      String diagnosticMessage, String responseName,
      ByteString responseValue)
      throws DecodeException
  {
    if(responseValue == null || responseValue.length() <= 0)
    {
      throw new DecodeException(
          ERR_PWPSTATE_EXTOP_NO_REQUEST_VALUE.get());
    }

    try
    {
      ASN1Reader reader = ASN1.getReader(responseValue);
      reader.readStartSequence();

      // Read the target user DN
      Response response =
          new Response(
              resultCode, matchedDN, diagnosticMessage,
              reader.readOctetStringAsString());

      decodeOperations(reader, response);
      reader.readEndSequence();
      return response;
    }
    catch(IOException ioe)
    {
      Message message =
          ERR_EXTOP_CANCEL_CANNOT_DECODE_REQUEST_VALUE.get(
              getExceptionMessage(ioe));
      throw new DecodeException(message, ioe);
    }
  }

  private static ByteString encode(String targetUser,
                                   List<Operation> operations)
  {
      ByteStringBuilder buffer = new ByteStringBuilder(6);
      ASN1Writer writer = ASN1.getWriter(buffer);

      try
      {
        writer.writeStartSequence();
        writer.writeOctetString(targetUser);
        if(!operations.isEmpty())
        {
          writer.writeStartSequence();
          for(Operation operation : operations)
          {
            writer.writeStartSequence();
            writer.writeEnumerated(
                operation.getOperationType().ordinal());
            if(operation.getValues() != null)
            {
              writer.writeStartSequence();
              for(ByteString value : operation.getValues())
              {
                writer.writeOctetString(value);
              }
              writer.writeEndSequence();
            }
            writer.writeEndSequence();
          }
          writer.writeEndSequence();
        }
        writer.writeEndSequence();
      }
      catch(IOException ioe)
      {
        // This should never happen unless there is a bug somewhere.
        throw new RuntimeException(ioe);
      }

      return buffer.toByteString();
    }

  private static void decodeOperations(
      ASN1Reader reader, OperationContainer container)
      throws IOException, DecodeException
  {
          // See if we have operations
      if(reader.hasNextElement())
      {
        reader.readStartSequence();
        int opType;
        OperationType type;
        while(reader.hasNextElement())
        {
          reader.readStartSequence();
          // Read the opType
          opType = reader.readEnumerated();
          try
          {
            type = OperationType.values()[opType];
          }
          catch(IndexOutOfBoundsException iobe)
          {
            throw new DecodeException(
                ERR_PWPSTATE_EXTOP_UNKNOWN_OP_TYPE.get(
                    String.valueOf(opType)), iobe);
          }

          // See if we have any values
          if(reader.hasNextElement())
          {
            reader.readStartSequence();
            ArrayList<ByteString> values =
                new ArrayList<ByteString>();
            while(reader.hasNextElement())
            {
              values.add(reader.readOctetString());
            }
            reader.readEndSequence();
            container.addOperation(
                new MultiValueOperation(type, values));
          }
          else
          {
            container.addOperation(type);
          }
          reader.readEndSequence();
        }
        reader.readEndSequence();
      }
  }
}
