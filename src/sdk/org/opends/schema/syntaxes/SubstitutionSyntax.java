package org.opends.schema.syntaxes;

import org.opends.server.types.ByteSequence;
import org.opends.messages.MessageBuilder;

import java.util.List;
import java.util.Map;

/**
 * This class provides a substitution mechanism where one unimplemented
 * syntax can be substituted by another defined syntax. A substitution syntax
 * is an LDAPSyntaxDescriptionSyntax with X-SUBST extension.
 */
public class SubstitutionSyntax extends SyntaxDescription
{
  private SyntaxDescription substitute;

  public SubstitutionSyntax(String oid, String description, 
                            Map<String, List<String>> extraProperties,
                            String definition,
                            SyntaxDescription substitute)
  {
    super(oid, "Substitution Syntax", description, extraProperties,
        definition);
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
}
