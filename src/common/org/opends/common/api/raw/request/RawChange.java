package org.opends.common.api.raw.request;

import org.opends.common.api.raw.RawPartialAttribute;
import org.opends.server.types.ModificationType;
import org.opends.server.util.Validator;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 27, 2009 Time: 12:13:37
 * AM To change this template use File | Settings | File Templates.
 */
public class RawChange
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
}
