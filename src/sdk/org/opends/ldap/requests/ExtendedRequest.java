package org.opends.ldap.requests;



import org.opends.ldap.responses.Result;
import org.opends.server.types.ByteString;
import org.opends.server.util.Validator;
import org.opends.spi.ExtendedOperation;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 19, 2009 Time:
 * 8:39:31 PM To change this template use File | Settings | File
 * Templates.
 */
public abstract class ExtendedRequest<Q extends ExtendedRequest<Q, R>, R extends Result>
    extends Request
{
  // The extended request name OID.
  private String requestName;



  /**
   * Creates a new raw extended request using the provided OID.
   * <p>
   * The new raw extended request will contain an empty list of
   * controls, and no value.
   *
   * @param requestName
   *          The extended request name OID.
   */
  protected ExtendedRequest(String requestName)
  {
    Validator.ensureNotNull(requestName);
    this.requestName = requestName;
  }



  // FIXME: this should not be exposed to clients.
  public abstract ExtendedOperation<Q, R> getExtendedOperation();



  /**
   * Get the request name OID of this extended request.
   *
   * @return The response name OID.
   */
  public String getRequestName()
  {
    return requestName;
  }



  /**
   * Get the response value of this intermediate response or
   * <code>NULL</code> if it is not available.
   *
   * @return the response value or <code>NULL</code>.
   */
  public abstract ByteString getRequestValue();



  /**
   * Sets the name OID for this extended request.
   *
   * @param requestName
   *          The name OID for this extended request.
   * @return This raw extended request.
   */
  @SuppressWarnings("unchecked")
  public Q setRequestName(String requestName)
  {
    Validator.ensureNotNull(requestName);
    this.requestName = requestName;
    return (Q) this;
  }
}
