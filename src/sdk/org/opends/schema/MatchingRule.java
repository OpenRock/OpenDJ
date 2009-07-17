package org.opends.schema;

import org.opends.server.util.Validator;

import java.util.List;
import java.util.Map;

/**
 * This interface defines the set of methods that must be implemented
 * by a Directory Server module that implements a matching rule.
 */
public abstract class MatchingRule extends AbstractSchemaElement
{
  // The OID that may be used to reference this definition.
  private final String oid;

  private final String syntax;

  // The definition string used to create this objectclass.
  private final String definition;

  protected MatchingRule(String oid,
                     List<String> names,
                     String description,
                     boolean obsolete,
                     String syntax,
                     Map<String, List<String>> extraProperties)
  {
    super(names, description, obsolete, extraProperties);

    Validator.ensureNotNull(oid, syntax);
    this.oid = oid;
    this.syntax = syntax;
    this.definition = buildDefinition();
  }

  protected MatchingRule(String oid,
                     List<String> names,
                     String description,
                     boolean obsolete,
                     String syntax,
                     Map<String, List<String>> extraProperties,
                     String definition)
  {
    super(names, description, obsolete, extraProperties);

    Validator.ensureNotNull(oid, syntax, definition);
    this.oid = oid;
    this.syntax = syntax;
    this.definition = definition;
  }

      /**
   * Retrieves the OID for this schema definition.
   *
   * @return The OID for this schema definition.
   */
  public final String getOID() {

    return oid;
  }


  /**
   * Retrieves the name or OID for this schema definition. If it has
   * one or more names, then the primary name will be returned. If it
   * does not have any names, then the OID will be returned.
   *
   * @return The name or OID for this schema definition.
   */
  public String getNameOrOID() {
    if(names.isEmpty())
    {
      return oid;
    }
    return names.get(0);
  }

  /**
   * Indicates whether this schema definition has the specified name
   * or OID.
   *
   * @param value
   *          The value for which to make the determination.
   * @return <code>true</code> if the provided value matches the OID
   *         or one of the names assigned to this schema definition,
   *         or <code>false</code> if not.
   */
  public boolean hasNameOrOID(String value) {
    return hasName(value) ||
        getOID().equals(value);
  }

  /**
   * Retrieves the OID of the syntax with which this matching rule is
   * associated.
   *
   * @return The OID of the syntax with which this matching rule is
   *         associated.
   */
  String getSyntax()
  {
    return syntax;
  }

  protected String getDefinition() {
    return definition;
  }

  protected String getIdentifier() {
    return oid;
  }

  protected void toStringContent(StringBuilder buffer) {
    buffer.append(" SYNTAX ");
    buffer.append(syntax);
  }
}
