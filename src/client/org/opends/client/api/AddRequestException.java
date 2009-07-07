package org.opends.client.api;

import org.opends.common.api.response.AddResponse;
import org.opends.common.api.response.Response;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 7, 2009
 * Time: 1:37:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class AddRequestException extends RequestException
{
  private AddResponse response;

  public AddRequestException(Throwable cause) {
    super(cause);
  }

  public AddRequestException(AddResponse response)
  {
    super(response.getDiagnosticMessage());
    this.response = response;
  }

  public AddResponse getResponse() {
    return response;
  }
}
