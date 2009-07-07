package org.opends.client.api.futures;

import org.opends.common.api.request.DeleteRequest;
import org.opends.common.api.request.SearchRequest;
import org.opends.common.api.response.DeleteResponse;
import org.opends.common.api.response.SearchResultDone;
import org.opends.client.api.DeleteRequestException;
import org.opends.client.api.SearchRequestException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 7, 2009
 * Time: 3:36:55 PM
 * To change this template use File | Settings | File Templates.
 */
public interface SearchResponseFuture 
    extends ResponseFuture<SearchRequest, SearchResultDone>
{
  public SearchResultDone get()
      throws InterruptedException, SearchRequestException;

  public SearchResultDone get(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException,
      SearchRequestException;
}
