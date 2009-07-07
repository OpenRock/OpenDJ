package org.opends.client.api;

import org.opends.common.api.extended.ExtendedResponse;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 7, 2009
 * Time: 3:38:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExtendedRequestException extends RequestException
{
  private ExtendedResponse response;

  public ExtendedRequestException(Throwable cause) {
    super(cause);
  }

  public ExtendedRequestException(ExtendedResponse response)
  {
    super(response.getDiagnosticMessage());
    this.response = response;
  }

  public ExtendedResponse getResponse() {
    return response;
  }
}
