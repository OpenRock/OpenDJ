package org.opends.common.api.raw.request;

import org.opends.server.types.ByteString;
import org.opends.server.types.DirectoryException;
import org.opends.server.util.Validator;
import org.opends.server.core.operations.BindRequest;
import org.opends.server.core.operations.Schema;

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


  public RawSimpleBindRequest(String bindDN, ByteString simplePassword) {
    super(bindDN);
    Validator.ensureNotNull(simplePassword);
    this.simplePassword = simplePassword;
  }

  /**
   * Returns the simple authentication password for this bind request.
   *
   * @return The simple authentication password for this bind request,
   *         or {@code null} if there is no password.
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
    Validator.ensureNotNull(simplePassword);
    this.simplePassword = simplePassword;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  public BindRequest toRequest(Schema schema) throws DirectoryException
  {
    // TODO: not yet implemented.
    return null;
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
