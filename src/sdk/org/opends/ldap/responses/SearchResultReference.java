package org.opends.ldap.responses;



import java.util.ArrayList;
import java.util.List;

import org.opends.ldap.Message;
import org.opends.server.util.Validator;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 25, 2009 Time:
 * 6:00:27 PM To change this template use File | Settings | File
 * Templates.
 */
public final class SearchResultReference extends Message implements
    Response
{
  private List<String> uris;



  public SearchResultReference(String uri, String... uris)
  {
    Validator.ensureNotNull(uri);
    if (uris == null)
    {
      this.uris = new ArrayList<String>(1);
      this.uris.add(uri);
    }
    else
    {
      this.uris = new ArrayList<String>(uris.length + 1);
      this.uris.add(uri);
      for (String u : uris)
      {
        Validator.ensureNotNull(u);
        this.uris.add(u);
      }
    }
  }



  public SearchResultReference addURI(String... uris)
  {
    if (uris != null)
    {
      for (String uri : uris)
      {
        Validator.ensureNotNull(uri);
        this.uris.add(uri);
      }
    }
    return this;
  }



  public Iterable<String> getURIs()
  {
    return uris;
  }



  @Override
  public void toString(StringBuilder buffer)
  {
    buffer.append("SearchResultReference(uris=");
    buffer.append(uris);
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}
