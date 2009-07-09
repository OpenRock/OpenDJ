package org.opends.ldap;



import org.opends.server.types.ByteString;
import org.opends.server.util.Validator;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jun 22, 2009 Time: 6:22:38
 * PM To change this template use File | Settings | File Templates.
 */
public final class GenericIntermediateResponse extends
    IntermediateResponse<GenericExtendedOperation>
{
  private ByteString responseValue;



  @Override
  public GenericExtendedOperation getExtendedOperation()
  {
    return GenericExtendedOperation.getInstance();
  }



  @Override
  public ByteString getResponseValue()
  {
    return responseValue;
  }



  public GenericIntermediateResponse setResponseName(String responseName)
  {
    Validator.ensureNotNull(responseName);
    this.responseName = responseName;
    return this;
  }



  public GenericIntermediateResponse setResponseValue(
      ByteString responseValue)
  {
    Validator.ensureNotNull(responseValue);
    this.responseValue = responseValue;
    return this;
  }



  @Override
  public void toString(StringBuilder buffer)
  {
    buffer.append("IntermediateResponse(responseName=");
    buffer.append(responseName);
    buffer.append(", responseValue=");
    buffer.append(responseValue);
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}
