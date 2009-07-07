package org.opends.client.api;

import org.opends.common.api.response.BindResponse;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 7, 2009
 * Time: 3:05:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class BindRequestException extends RequestException
{
  private BindResponse response;

  public BindRequestException(Throwable cause) {
    super(cause);
  }

  public BindRequestException(BindResponse response)
  {
    super(response.getDiagnosticMessage());
    this.response = response;
  }

  public BindResponse getResponse() {
    return response;
  }
}
