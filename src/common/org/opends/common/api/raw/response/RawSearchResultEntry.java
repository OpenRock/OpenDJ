package org.opends.common.api.raw.response;

import org.opends.common.api.raw.RawPartialAttribute;
import org.opends.common.api.raw.RawMessage;
import org.opends.server.core.operations.Response;
import org.opends.server.core.operations.Schema;
import org.opends.server.types.DirectoryException;
import org.opends.server.util.Validator;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 25, 2009 Time: 5:49:01
 * PM To change this template use File | Settings | File Templates.
 */
public final class RawSearchResultEntry extends RawMessage
    implements RawResponse
{
  private String objectName;
  private List<RawPartialAttribute> partialAttributeList;

  public RawSearchResultEntry(String objectName)
  {
    Validator.ensureNotNull(objectName);
    this.objectName = objectName;
    this.partialAttributeList = new LinkedList<RawPartialAttribute>();
  }

  public String getObjectName()
  {
    return objectName;
  }

  public RawSearchResultEntry setObjectName(String objectName)
  {
    Validator.ensureNotNull(objectName);
    this.objectName = objectName;
    return this;
  }

  public Iterable<RawPartialAttribute> getPartialAttributeList()
  {
    return partialAttributeList;
  }

  public RawSearchResultEntry addPartialAttribute(
      RawPartialAttribute partialAttribute)
  {
    Validator.ensureNotNull(partialAttribute);
    partialAttributeList.add(partialAttribute);
    return this;
  }

  public Response toResponse(Schema schema) throws DirectoryException
  {
    // TODO: not yet implemented.
    return null;
  }

  public void toString(StringBuilder buffer)
  {
    buffer.append("SearchResultEntry(objectName=");
    buffer.append(objectName);
    buffer.append(", partialAttributeList=");
    buffer.append(partialAttributeList);
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
  }
}
