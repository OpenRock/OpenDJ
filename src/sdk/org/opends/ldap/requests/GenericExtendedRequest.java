package org.opends.ldap.requests;



import org.opends.ldap.GenericExtendedOperation;
import org.opends.server.types.ByteString;
import org.opends.server.util.Validator;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jun 22, 2009 Time: 6:22:16
 * PM To change this template use File | Settings | File Templates.
 */
public final class GenericExtendedRequest extends
    ExtendedRequest<GenericExtendedOperation>
{
  // The extended request value.
  private ByteString requestValue;



  /**
   * Creates a new raw extended request using the provided OID.
   * <p>
   * The new raw extended request will contain an empty list of
   * controls, and no value.
   * 
   * @param requestName
   *          The extended request name OID.
   */
  public GenericExtendedRequest(String requestName)
  {
    super(requestName);
    this.requestValue = ByteString.empty();
  }



  @Override
  public GenericExtendedOperation getExtendedOperation()
  {
    return GenericExtendedOperation.getInstance();
  }



  /**
   * Returns the request value for this extended request.
   * 
   * @return The request value for this extended request, or {@code
   *         null} if this extended request does not have a request
   *         value.
   */
  @Override
  public ByteString getRequestValue()
  {
    return requestValue;
  }



  /**
   * Sets the name OID for this extended request.
   * 
   * @param requestName
   *          The name OID for this extended request.
   * @return This raw extended request.
   */
  public GenericExtendedRequest setRequestName(String requestName)
  {
    Validator.ensureNotNull(requestName);
    this.requestName = requestName;
    return this;
  }



  /**
   * Sets the request value for this extended request.
   * 
   * @param requestValue
   *          The request value for this extended request, or {@code
   *          null} if this extended request does not have a request
   *          value.
   * @return This raw extended request.
   */
  public GenericExtendedRequest setRequestValue(ByteString requestValue)
  {
    Validator.ensureNotNull(requestValue);
    this.requestValue = requestValue;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public void toString(StringBuilder buffer)
  {
    buffer.append("ExtendedRequest(requestName=");
    buffer.append(requestName);
    buffer.append(", requestValue=");
    buffer.append(requestValue);
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}
