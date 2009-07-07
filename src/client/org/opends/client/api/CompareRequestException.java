package org.opends.client.api;

import org.opends.common.api.response.CompareResponse;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 7, 2009
 * Time: 3:29:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class CompareRequestException extends RequestException
{
  private CompareResponse response;

  public CompareRequestException(Throwable cause) {
    super(cause);
  }

  public CompareRequestException(CompareResponse response)
  {
    super(response.getDiagnosticMessage());
    this.response = response;
  }

  public CompareResponse getResponse() {
    return response;
  }
}
