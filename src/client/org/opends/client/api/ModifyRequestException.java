package org.opends.client.api;

import org.opends.common.api.response.ModifyResponse;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 7, 2009
 * Time: 3:33:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModifyRequestException extends RequestException
{
  private ModifyResponse response;

  public ModifyRequestException(Throwable cause) {
    super(cause);
  }

  public ModifyRequestException(ModifyResponse response)
  {
    super(response.getDiagnosticMessage());
    this.response = response;
  }

  public ModifyResponse getResponse() {
    return response;
  }
}
