package org.opends.schema.syntaxes;


/**
 * This class defines the set of methods and structures that must be
 * implemented to define a new attribute syntax.
 */
public abstract class AbstractSyntaxImplementation
    implements SyntaxImplementation
{
  public String getEqualityMatchingRule()
  {
    return null;
  }

  public String getOrderingMatchingRule()
  {
    return null;
  }

  public String getSubstringMatchingRule()
  {
    return null;
  }

  public String getApproximateMatchingRule()
  {
    return null;
  }

}
