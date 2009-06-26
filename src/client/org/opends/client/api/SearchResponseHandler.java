package org.opends.client.api;

import org.opends.common.api.response.SearchResultDone;
import org.opends.common.api.response.SearchResultEntry;
import org.opends.common.api.response.SearchResultReference;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 1, 2009 Time: 3:19:43
 * PM To change this template use File | Settings | File Templates.
 */
public interface SearchResponseHandler
    extends ResponseHandler<SearchResultDone>
{
  public void handleSearchResultEntry(SearchResultEntry entry);

  public void handleSearchResultReference(SearchResultReference reference);
}
