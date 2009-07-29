package org.opends.types;



import org.opends.server.types.ByteString;
import org.opends.server.util.Validator;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jun 25, 2009 Time: 3:54:08
 * PM To change this template use File | Settings | File Templates.
 */
public final class Change
{
  private final ModificationType modificationType;
  private final Attribute modification;



  public Change(ModificationType modificationType, Attribute attribute)
  {
    Validator.ensureNotNull(modificationType, attribute);
    this.modification = attribute;
    this.modificationType = modificationType;
  }



  public Change(ModificationType modificationType,
      String attributeDescription, ByteString... attributeValues)
  {
    Validator.ensureNotNull(modificationType);
    this.modification =
        Types.newAttribute(attributeDescription, attributeValues);
    this.modificationType = modificationType;
  }



  public Attribute getModification()
  {
    return modification;
  }



  public ModificationType getModificationType()
  {
    return modificationType;
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
    modification.toString(buffer);
    buffer.append(")");
  }
}
