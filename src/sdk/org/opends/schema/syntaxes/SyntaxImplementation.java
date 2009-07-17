package org.opends.schema.syntaxes;

import org.opends.server.types.ByteSequence;
import org.opends.server.util.Validator;
import org.opends.messages.MessageBuilder;
import org.opends.schema.Syntax;

import java.util.List;
import java.util.Map;

/**
 * This class defines the set of methods and structures that must be
 * implemented to define a new attribute syntax.
 */
public abstract class SyntaxImplementation extends Syntax
{
  private final String name;

  protected SyntaxImplementation(String oid, String name, String description,
                                 Map<String, List<String>> extraProperties)
  {
    super(oid, description, extraProperties);
    Validator.ensureNotNull(name);
    this.name = name;
  }

  protected SyntaxImplementation(Syntax orginalSyntax, String name)
  {
    super(orginalSyntax);
    Validator.ensureNotNull(name);
    this.name = name;
  }

  /**
   * Retrieves the common name for this attribute syntax.
   *
   * @return  The common name for this attribute syntax.
   */
  public final String getSyntaxName()
  {
    return name;
  }

  /**
   * Indicates whether this attribute syntax would likely be a
   * human readable string.
   * @return {@code true} if this attribute syntax would likely be a
   * human readable string or {@code false} if not.
   */
  public abstract boolean isHumanReadable();

  /**
   * Indicates whether the provided value is acceptable for use in an
   * attribute with this syntax.  If it is not, then the reason may be
   * appended to the provided buffer.
   *
   * @param  value          The value for which to make the
   *                        determination.
   * @param  invalidReason  The buffer to which the invalid reason
   *                        should be appended.
   *
   * @return  {@code true} if the provided value is acceptable for use
   *          with this syntax, or {@code false} if not.
   */
  public abstract boolean valueIsAcceptable(ByteSequence value,
                                            MessageBuilder invalidReason);
}
