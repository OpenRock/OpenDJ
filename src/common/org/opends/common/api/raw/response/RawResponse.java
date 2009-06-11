package org.opends.common.api.raw.response;

import org.opends.common.api.raw.RawMessage;
import org.opends.server.core.operations.Response;
import org.opends.server.core.operations.Schema;
import org.opends.server.types.DirectoryException;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 25, 2009 Time: 2:50:43
 * PM To change this template use File | Settings | File Templates.
 */
public interface RawResponse
{
  /**
   * Returns a decoded response representing this raw response. Subsequent
   * changes to this raw response will not be reflected in the returned
   * response.
   *
   * @param schema The schema to use when decoding this raw request.
   *
   * @return A decoded request representing this raw response.
   *
   * @throws DirectoryException If this raw response could not be decoded.
   */
  public Response toResponse(Schema schema)
      throws DirectoryException;
}
