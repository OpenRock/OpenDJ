package org.opends.ldap.responses;



import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.opends.ldap.requests.SearchRequest;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 7, 2009 Time: 3:36:55
 * PM To change this template use File | Settings | File Templates.
 */
public interface SearchResponseFuture extends ResponseFuture
{
  public SearchResultDone get() throws InterruptedException,
      ExecutionException;



  public SearchResultDone get(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException, ExecutionException;



  /**
   * Retrieves the current number of result entries received from the
   * server.
   * 
   * @return
   */
  public int getNumSearchResultEntries();



  /**
   * Retrieves the current number of result references received from the
   * server.
   * 
   * @return
   */
  public int getNumSearchResultReferences();



  public SearchRequest getRequest();
}
