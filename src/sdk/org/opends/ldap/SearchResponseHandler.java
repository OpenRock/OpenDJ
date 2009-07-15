package org.opends.ldap;



import org.opends.ldap.responses.SearchResult;
import org.opends.ldap.responses.SearchResultEntry;
import org.opends.ldap.responses.SearchResultReference;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 1, 2009 Time:
 * 3:19:43 PM To change this template use File | Settings | File
 * Templates.
 */
public interface SearchResponseHandler extends
    ResponseHandler<SearchResult>
{
  public void handleSearchResultEntry(SearchResultEntry entry);



  public void handleSearchResultReference(
      SearchResultReference reference);
}
