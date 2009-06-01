package org.opends.common.api.raw.response;

import org.opends.server.core.operations.Response;
import org.opends.server.core.operations.Schema;
import org.opends.server.types.DirectoryException;
import org.opends.server.util.Validator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 25, 2009 Time: 6:00:27
 * PM To change this template use File | Settings | File Templates.
 */
public class RawSearchResultReference extends RawResponse
{
  private List<String> uris;

  public RawSearchResultReference(String uri)
  {
    Validator.ensureNotNull(uri);
    this.uris = new ArrayList<String>(1);
    this.uris.add(uri);
  }

  public Iterable<String> getURIs()
  {
    return uris;
  }

  public RawSearchResultReference addURI(String uri)
  {
    Validator.ensureNotNull(uri);
    uris.add(uri);
    return this;
  }

  public Response toResponse(Schema schema) throws DirectoryException
  {
    // TODO: not yet implemented.
    return null;
  }

  public void toString(StringBuilder buffer)
  {
    buffer.append("ResultReferenceResponse(uris=");
    buffer.append(uris);
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}
