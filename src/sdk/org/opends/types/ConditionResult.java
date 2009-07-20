package org.opends.types;

/**
 * This enumeration defines a result that could be returned from a
 * boolean operation that may evaluate to true or false, but may also
 * be undefined (i.e., "maybe").  A result of undefined indicates that
 * further investigation may be required.
 */
public enum ConditionResult
{
  /**
   * Indicates that the result of the associated check returned
   * "true".
   */
  TRUE("true"),



  /**
   * Indicates that the result of the associated check returned
   * "false".
   */
  FALSE("false"),



  /**
   * Indicates that the associated check did not yield a definitive
   * result and that additional checking might be required.
   */
  UNDEFINED("undefined");



  // The human-readable name for this result.
  private String resultName;



  /**
   * Creates a new condition result with the provided name.
   *
   * @param  resultName  The human-readable name for this condition
   *                     result.
   */
  private ConditionResult(String resultName)
  {
    this.resultName = resultName;
  }

  /**
   Returns the logical inverse of a ConditionResult value. The inverse
   of the UNDEFINED value is UNDEFINED.

   @param value The value to invert.
   @return The logical inverse of the supplied value.
   */
  public static ConditionResult inverseOf(ConditionResult value) {
    switch (value) {
      case TRUE:
        return FALSE;
      case FALSE:
        return TRUE;
      case UNDEFINED:
        return UNDEFINED;
    }
    assert false : "internal error: missing switch case" ;
    return UNDEFINED;
  }


  /**
   * Retrieves the human-readable name for this condition result.
   *
   * @return  The human-readable name for this condition result.
   */
  public String toString()
  {
    return resultName;
  }
}

