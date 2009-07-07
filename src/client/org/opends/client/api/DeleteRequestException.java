package org.opends.client.api;

import org.opends.common.api.response.DeleteResponse;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 7, 2009
 * Time: 3:30:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class DeleteRequestException extends RequestException
{
  private DeleteResponse response;

  public DeleteRequestException(Throwable cause) {
    super(cause);
  }

  public DeleteRequestException(DeleteResponse response)
  {
    super(response.getDiagnosticMessage());
    this.response = response;
  }

  public DeleteResponse getResponse() {
    return response;
  }
}
