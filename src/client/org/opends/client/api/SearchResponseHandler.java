package org.opends.client.api;

import org.opends.common.api.response.RawSearchResultDone;
import org.opends.common.api.response.RawSearchResultEntry;
import org.opends.common.api.response.RawSearchResultReference;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 1, 2009 Time: 3:19:43
 * PM To change this template use File | Settings | File Templates.
 */
public interface SearchResponseHandler
    extends ResponseHandler<RawSearchResultDone>
{
  public void handleSearchResultEntry(RawSearchResultEntry entry);

  public void handleSearchResultReference(RawSearchResultReference reference);
}
