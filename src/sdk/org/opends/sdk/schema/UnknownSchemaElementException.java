package org.opends.sdk.schema;

import org.opends.messages.Message;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Sep 2, 2009
 * Time: 5:24:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class UnknownSchemaElementException extends SchemaException
{
  public UnknownSchemaElementException(Message message) {
    super(message);
  }

  public UnknownSchemaElementException(Message message, Throwable cause) {
    super(message, cause);
  }
}
