package org.opends.sdk.schema;

/**
 * This enumeration defines the set of possible attribute usage values
 * that may apply to an attribute type, as defined in RFC 2252.
 */
public enum AttributeUsage
{
  /**
   * The attribute usage intended for user-defined attribute types.
   */
  USER_APPLICATIONS("userApplications", false),



  /**
   * The attribute usage intended for standard operational attributes.
   */
  DIRECTORY_OPERATION("directoryOperation", true),



  /**
   * The attribute usage intended for non-standard operational
   * attributes shared among multiple DSAs.
   */
  DISTRIBUTED_OPERATION("distributedOperation", true),



  /**
   * The attribute usage intended for non-standard operational
   * attributes used by a single DSA.
   */
  DSA_OPERATION("dSAOperation", true);



  // The string representation of this attribute usage.
  private final String usageString;

  // Flag indicating whether or not the usage should be categorized as
  // operational.
  private final boolean isOperational;



  /**
   * Creates a new attribute usage with the provided string
   * representation.
   *
   * @param usageString
   *          The string representation of this attribute usage.
   * @param isOperational
   *          <code>true</code> if attributes having this attribute
   *          usage are operational, or <code>false</code>
   *          otherwise.
   */
  private AttributeUsage(String usageString, boolean isOperational)
  {
    this.usageString = usageString;
    this.isOperational = isOperational;
  }



  /**
   * Retrieves a string representation of this attribute usage.
   *
   * @return  A string representation of this attribute usage.
   */
  public String toString()
  {
    return usageString;
  }



  /**
   * Determine whether or not attributes having this attribute usage
   * are operational.
   *
   * @return Returns <code>true</code> if attributes having this
   *         attribute usage are operational, or <code>false</code>
   *         otherwise.
   */
  public boolean isOperational() {
    return isOperational;
  }
}
