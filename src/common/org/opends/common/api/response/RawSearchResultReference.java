package org.opends.common.api.response;

import org.opends.server.util.Validator;
import org.opends.common.api.RawMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 25, 2009 Time: 6:00:27
 * PM To change this template use File | Settings | File Templates.
 */
public final class RawSearchResultReference extends RawMessage
    implements RawResponse
{
  private List<String> uris;

  public RawSearchResultReference(String uri, String... uris)
  {
    Validator.ensureNotNull(uri);
    if(uris == null)
    {
      this.uris = new ArrayList<String>(1);
      this.uris.add(uri);
    }
    else
    {
      this.uris = new ArrayList<String>(uris.length + 1);
      this.uris.add(uri);
      for(String u : uris)
      {
        Validator.ensureNotNull(u);
        this.uris.add(u);
      }
    }
  }

  public Iterable<String> getURIs()
  {
    return uris;
  }

  public RawSearchResultReference addURI(String... uris)
  {
    if(uris != null)
    {
      for(String uri : uris)
      {
        Validator.ensureNotNull(uri);
        this.uris.add(uri);
      }
    }
    return this;
  }

  public void toString(StringBuilder buffer)
  {
    buffer.append("SearchResultReference(uris=");
    buffer.append(uris);
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}
