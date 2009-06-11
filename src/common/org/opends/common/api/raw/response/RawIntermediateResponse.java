package org.opends.common.api.raw.response;

import org.opends.server.core.operations.Response;
import org.opends.server.core.operations.Schema;
import org.opends.server.types.ByteString;
import org.opends.server.types.DirectoryException;
import org.opends.server.util.Validator;
import org.opends.common.api.raw.RawMessage;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 26, 2009 Time: 9:57:17
 * AM To change this template use File | Settings | File Templates.
 */
public final class RawIntermediateResponse extends RawMessage
    implements RawResponse
{
  private String responseName;
  private ByteString responseValue;

  public RawIntermediateResponse()
  {
    responseName = "".intern();
    responseValue = ByteString.empty();
  }

  public String getResponseName()
  {
    return responseName;
  }

  public RawIntermediateResponse setResponseName(
      String responseName)
  {
    Validator.ensureNotNull(responseName);
    this.responseName = responseName;
    return this;
  }

  public ByteString getResponseValue()
  {
    return responseValue;
  }

  public RawIntermediateResponse setResponseValue(
      ByteString responseValue)
  {
    Validator.ensureNotNull(responseValue);
    this.responseValue = responseValue;
    return this;
  }

  public Response toResponse(Schema schema) throws DirectoryException
  {
    // TODO: not yet implemented.
    return null;
  }

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
