package org.opends.schema;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 30, 2009
 * Time: 2:16:32 PM
 * To change this template use File | Settings | File Templates.
 */
public interface SchemaElement
{
    protected abstract String getDefinition();

  protected abstract String getIdentifier();
}
