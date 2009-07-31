package org.opends.types;

import org.opends.schema.Schema;
import org.opends.schema.AttributeType;
import org.opends.schema.syntaxes.SyntaxImplementation;
import org.opends.schema.matchingrules.MatchingRuleImplementation;
import org.opends.server.types.ByteString;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 28, 2009
 * Time: 10:29:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class DNBuilder
{
  private class MultipleAttributesRDN extends RDN
  {
    private Set<AttributeTypeAndValue> attributeTypeAndValues;

    private MultipleAttributesRDN(Schema schema,
                                  Set<AttributeTypeAndValue> attributeTypeAndValues)
    {
      super(schema);
      TreeSet<AttributeTypeAndValue> set = new TreeSet<AttributeTypeAndValue>();
      set.addAll(attributeTypeAndValues);
      this.attributeTypeAndValues = Collections.unmodifiableSet(set);
    }

    public int numAttributeTypeAndValues()
    {
      return attributeTypeAndValues.size();
    }

    public Iterator<AttributeTypeAndValue> iterator() {
      return attributeTypeAndValues.iterator();
    }
  }

  public DNBuilder(Schema schema)
  {

  }

  public void addRDN(AttributeType attributeType, ByteString value)
  {

  }

  public void addAttributeTypeAndValue(AttributeType attributeType,
                                       ByteString value)
  {

  }
}
