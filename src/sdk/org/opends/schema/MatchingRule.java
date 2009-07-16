package org.opends.schema;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 9, 2009
 * Time: 2:06:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class MatchingRule extends AbstractSchemaElement
{
  // The OID that may be used to reference this definition.
  private final String oid;

  // The definition string used to create this objectclass.
  protected final String definition;

  public

  protected String getDefinition() {
    return definition;
  }

  protected String getIdentifier() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  protected void toStringContent(StringBuilder buffer) {
    //To change body of implemented methods use File | Settings | File Templates.
  }
}
