package org.opends.common.api.raw.request;

import org.opends.common.api.raw.RawPartialAttribute;
import org.opends.server.types.ModificationType;
import org.opends.server.util.Validator;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 27, 2009 Time: 12:13:37
 * AM To change this template use File | Settings | File Templates.
 */
public final class RawChange
{
  private int modificationType;
  private RawPartialAttribute modification;

  public RawChange(int modificationType, RawPartialAttribute modification)
  {
    Validator.ensureNotNull(modificationType, modification);
    this.modificationType = modificationType;
    this.modification = modification;
  }

  public int getModificationType()
  {
    return modificationType;
  }

  public RawChange setModificationType(int modificationType)
  {
    Validator.ensureNotNull(modificationType);
    this.modificationType = modificationType;
    return this;
  }

  public RawPartialAttribute getModification()
  {
    return modification;
  }

  public RawChange setModification(RawPartialAttribute modification)
  {
    Validator.ensureNotNull(modification);
    this.modification = modification;
    return this;
  }

  /**
   * Returns a string representation of this request.
   *
   * @return A string representation of this request.
   */
  @Override
  public final String toString()
  {
    StringBuilder builder = new StringBuilder();
    toString(builder);
    return builder.toString();
  }



  /**
   * Appends a string representation of this request to the provided
   * buffer.
   *
   * @param buffer
   *          The buffer into which a string representation of this
   *          request should be appended.
   */
  public void toString(StringBuilder buffer)
  {
    buffer.append("Change(modificationType=");
    buffer.append(modificationType);
    buffer.append(", modification=");
    buffer.append(modification);
    buffer.append(")");
  }
}
