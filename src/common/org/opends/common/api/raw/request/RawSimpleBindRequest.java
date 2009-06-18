package org.opends.common.api.raw.request;

import org.opends.server.types.ByteString;
import org.opends.server.types.DirectoryException;
import org.opends.server.util.Validator;
import org.opends.server.core.operations.BindRequest;
import org.opends.server.core.operations.Schema;
import org.opends.common.api.DN;

/**
 * Created by IntelliJ IDEA.
 * User: digitalperk
 * Date: May 26, 2009
 * Time: 7:40:13 PM
 * To change this template use File | Settings | File Templates.
 */
public final class RawSimpleBindRequest extends RawBindRequest
{
  // The simple password.
  private ByteString simplePassword;


  /**
   * Constructs a request using the Anonymous Authentication Mechanism of
   * Simple Bind
   */
  public RawSimpleBindRequest()
  {
    super("".intern());
    simplePassword = ByteString.empty();
  }

  /**
   * Constructs a request using the Unauthenticated Authentication Mechanism of
   * Simple Bind
   * @param bindDN
   */
  public RawSimpleBindRequest(String bindDN)
  {
    super(bindDN);
    simplePassword = ByteString.empty();
  }

  /**
   * Constructs a request using the Unauthenticated Authentication Mechanism of
   * Simple Bind
   * @param bindDN
   */
  public RawSimpleBindRequest(DN bindDN)
  {
    super(bindDN.toString());
    simplePassword = ByteString.empty();
  }

  /**
   * Constructs a request using the Name/Password Authentication Mechanism of
   * Simple Bind
   * @param bindDN
   * @param simplePassword
   */
  public RawSimpleBindRequest(String bindDN, ByteString simplePassword) {
    super(bindDN);
    Validator.ensureNotNull(simplePassword);
    if(simplePassword.length() <= 0)
    {
      throw new AssertionError("simplePassword must not be empty");
    }
    this.simplePassword = simplePassword;
  }

  /**
   * Constructs a request using the Name/Password Authentication Mechanism of
   * Simple Bind
   * @param bindDN
   * @param simplePassword
   */
  public RawSimpleBindRequest(DN bindDN, ByteString simplePassword) {
    super(bindDN.toString());
    Validator.ensureNotNull(simplePassword);
    if(simplePassword.length() <= 0)
    {
      throw new AssertionError("simplePassword must not be empty");
    }
    this.simplePassword = simplePassword;
  }

  /**
   * Returns the simple authentication password for this bind request.
   *
   * @return The simple authentication password for this bind request.
   */
  public ByteString getSimplePassword()
  {
    return simplePassword;
  }



  /**
   * Sets the simple authentication password for this bind request.
   *
   * @param simplePassword
   *          The simple authentication password for this bind request,
   *          or {@code null} if there is no password.
   * @return This raw bind request.
   */
  public RawBindRequest setSimplePassword(ByteString simplePassword)
  {
    if(simplePassword == null)
    {
      this.simplePassword = ByteString.empty();
    }
    else
    {
      if(simplePassword.length() <=0)
      {
        throw new AssertionError("simplePassword must not be empty");
      }
      this.simplePassword = simplePassword;
    }
    return this;
  }




  /**
   * {@inheritDoc}
   */
  @Override
  public void toString(StringBuilder buffer)
  {
    buffer.append("SimpleBindRequest(bindDN=");
    buffer.append(getBindDN());
    buffer.append(", authentication=simple");
    buffer.append(", simplePassword=");
    buffer.append(String.valueOf(simplePassword));
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}
