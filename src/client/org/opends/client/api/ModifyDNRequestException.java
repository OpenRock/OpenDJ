package org.opends.client.api;

import org.opends.common.api.response.ModifyDNResponse;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 7, 2009
 * Time: 3:33:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModifyDNRequestException extends RequestException
{
  private ModifyDNResponse response;

  public ModifyDNRequestException(Throwable cause) {
    super(cause);
  }

  public ModifyDNRequestException(ModifyDNResponse response)
  {
    super(response.getDiagnosticMessage());
    this.response = response;
  }

  public ModifyDNResponse getResponse() {
    return response;
  }
}