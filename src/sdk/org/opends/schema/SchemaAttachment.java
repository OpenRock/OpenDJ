package org.opends.schema;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Aug 17, 2009
 * Time: 9:48:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class SchemaAttachment<T>
{
   public SchemaAttachment() {
    // Nothing to do.
  }

  public final T get(Schema schema) {
    // Schema calls back to initialValue() if this is the first time.
    return schema.getAttachment(this);
  }

  public final T remove(Schema schema) {
    return schema.removeAttachment(this);
  }

  public final void set(Schema schema, T value) {
    schema.setAttachment(this, value);
  }

  protected T initialValue() {
    // Default implementation.
    return null;
  }
}
