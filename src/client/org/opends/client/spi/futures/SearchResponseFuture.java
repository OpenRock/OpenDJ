package org.opends.client.spi.futures;

import org.opends.common.api.request.SearchRequest;
import org.opends.common.api.response.SearchResultDone;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 7, 2009
 * Time: 3:36:55 PM
 * To change this template use File | Settings | File Templates.
 */
public interface SearchResponseFuture extends ResponseFuture
{
  public SearchRequest getRequest();

  public SearchResultDone get()
      throws InterruptedException, ExecutionException;

  public SearchResultDone get(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException,
      ExecutionException;

    /**
   * Retrieves the current number of result entries received
   * from the server.
   * @return
   */
  public int getNumSearchResultEntries();

  /**
   * Retrieves the current number of result references received
   * from the server.
   * @return
   */
  public int getNumSearchResultReferences();
}
