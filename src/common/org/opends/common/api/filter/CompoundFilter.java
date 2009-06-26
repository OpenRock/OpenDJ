package org.opends.common.api.filter;

import org.opends.server.util.Validator;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 10, 2009 Time: 11:38:14
 * AM To change this template use File | Settings | File Templates.
 */
public abstract class CompoundFilter extends Filter
{
  protected List<Filter> components;

  public CompoundFilter(Filter component, Filter... components)
  {
    Validator.ensureNotNull(component);
    if(components == null)
    {
      this.components = new ArrayList<Filter>(1);
      this.components.add(component);
    }
    else
    {
      this.components = new ArrayList<Filter>(components.length + 1);
      this.components.add(component);
      for(Filter c : components)
      {
        Validator.ensureNotNull(c);
        this.components.add(c);
      }
    }
  }

  public CompoundFilter addComponent(Filter... components)
  {
    if(components != null)
    {
      for(Filter c : components)
      {
        Validator.ensureNotNull(c);
        this.components.add(c);
      }
    }
    return this;
  }

  public Iterable<Filter> getComponents()
  {
    return components;
  }
}
