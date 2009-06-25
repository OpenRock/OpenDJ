package org.opends.common.api.request.filter;

import org.opends.server.util.Validator;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 10, 2009 Time: 11:38:14
 * AM To change this template use File | Settings | File Templates.
 */
public abstract class RawCompoundFilter extends RawFilter
{
  protected List<RawFilter> components;

  public RawCompoundFilter(RawFilter component, RawFilter... components)
  {
    Validator.ensureNotNull(component);
    if(components == null)
    {
      this.components = new ArrayList<RawFilter>(1);
      this.components.add(component);
    }
    else
    {
      this.components = new ArrayList<RawFilter>(components.length + 1);
      this.components.add(component);
      for(RawFilter c : components)
      {
        Validator.ensureNotNull(c);
        this.components.add(c);
      }
    }
  }

  public RawCompoundFilter addComponent(RawFilter... components)
  {
    if(components != null)
    {
      for(RawFilter c : components)
      {
        Validator.ensureNotNull(c);
        this.components.add(c);
      }
    }
    return this;
  }

  public Iterable<RawFilter> getComponents()
  {
    return components;
  }
}
