package org.opends.common.api.raw.request.filter;

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

  public RawCompoundFilter(RawFilter component)
  {
    Validator.ensureNotNull(component);
    components = new ArrayList<RawFilter>(2);
    components.add(component);
  }

  public RawCompoundFilter addComponent(RawFilter component)
  {
    Validator.ensureNotNull(component);
    components.add(component);
    return this;
  }

  public Iterable<RawFilter> getComponents()
  {
    return components;
  }
}
