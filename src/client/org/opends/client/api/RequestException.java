package org.opends.client.api;

import org.opends.common.api.response.Response;

import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 7, 2009
 * Time: 1:32:15 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class RequestException extends ExecutionException
{
  protected RequestException(String message) {
    super(message);
  }

  protected RequestException(String message, Throwable cause) {
    super(message, cause);
  }

  protected RequestException(Throwable cause) {
    super(cause);
  }

  /**
   * The error response received from the server, if any.
   * @return
   */
  public abstract Response getResponse();
}
