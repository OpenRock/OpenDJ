package org.opends.sdk.schema;

/**
 * This enumeration defines the set of possible objectclass types that
 * may be used, as defined in RFC 2252.
 */
@org.opends.server.types.PublicAPI(
     stability=org.opends.server.types.StabilityLevel.UNCOMMITTED,
     mayInstantiate=false,
     mayExtend=false,
     mayInvoke=true)
public enum ObjectClassType
{
  /**
   * The objectclass type that to use for classes declared "abstract".
   */
  ABSTRACT("ABSTRACT"),



  /**
   * The objectclass type that to use for classes declared
   * "structural".
   */
  STRUCTURAL("STRUCTURAL"),



  /**
   * The objectclass type that to use for classes declared
   * "auxiliary".
   */
  AUXILIARY("AUXILIARY");



  // The string representation of this objectclass type.
  String typeString;



  /**
   * Creates a new objectclass type with the provided string
   * representation.
   *
   * @param  typeString  The string representation for this
   *                     objectclass type.
   */
  private ObjectClassType(String typeString)
  {
    this.typeString = typeString;
  }



  /**
   * Retrieves a string representation of this objectclass type.
   *
   * @return  A string representation of this objectclass type.
   */
  public String toString()
  {
    return typeString;
  }
}


