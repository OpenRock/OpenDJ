package org.opends.schema.syntaxes;

import org.opends.server.types.ByteSequence;
import org.opends.server.util.Validator;
import org.opends.messages.MessageBuilder;
import org.opends.schema.Syntax;

import java.util.List;
import java.util.Map;

/**
 * This class provides a substitution mechanism where one potentially
 * unimplemented syntax can be substituted by another implemented syntax.
 * A substitution syntax is an LDAPSyntaxDescriptionSyntax with X-SUBST
 * extension.
 */
public class SubstitutionSyntax extends SyntaxImplementation
{
  private SyntaxImplementation substitute;

  /**
   * Construct a new substitution syntax implementation for the given syntax
   * with the given substitute syntax implementation.
   *
   * @param syntax The syntax being substituted.
   * @param substitute The syntax providing the substitute implementation.
   */
  public SubstitutionSyntax(Syntax syntax,
                            SyntaxImplementation substitute)
  {
    super(syntax, "Substitution Syntax");
    Validator.ensureNotNull(substitute);
    this.substitute = substitute;
  }

  public boolean isHumanReadable() {
    return substitute.isHumanReadable();
  }

  public boolean valueIsAcceptable(ByteSequence value,
                                   MessageBuilder invalidReason)
  {
    return substitute.valueIsAcceptable(value, invalidReason);
  }

  public SyntaxImplementation getSubstitute()
  {
    return substitute;
  }
}
