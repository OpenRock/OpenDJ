package org.opends.common.api.raw;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 * User: digitalperk
 * Date: May 25, 2009
 * Time: 2:44:59 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class RawMessage
{
  // The list of controls included with this request.
  private List<RawControl> controls;



  /**                                                         
   * Creates a new message.
   */
  protected RawMessage()
  {
    this.controls = Collections.emptyList();
  }



  /**
   * Ensures that this request contains the specified control, replacing
   * any existing control having the same OID.
   *
   * @param control
   *          The control to be added to this request.
   * @return {@code false} if this request already contained a control
   *         with the same OID, or {@code true} otherwise.
   */
  public final boolean addControl(RawControl control)
  {
    boolean result = true;
    if(controls == Collections.EMPTY_LIST)
    {
      controls = new LinkedList<RawControl>();
    }
    else
    {
      result = (removeControl(control.getOID()) == null);
    }
    controls.add(control);
    return result;
  }



  /**
   * Returns the specified control included with this request.
   *
   * @param oid
   *          The OID of the control to be returned.
   * @return The control, or {@code null} if the control is not included
   *         with this request.
   */
  public final RawControl getControl(String oid)
  {
    // Avoid creating an iterator if possible.
    if (controls.isEmpty())
    {
      return null;
    }

    for (RawControl control : controls)
    {
      if (control.getOID().equals(oid))
      {
        return control;
      }
    }

    return null;
  }



  /**
   * Returns an {@code Iterable} containing the controls included with
   * this request. The returned {@code Iterable} may be used to remove
   * controls from this request.
   *
   * @return An {@code Iterable} containing the controls included with
   *         this request
   */
  public final Iterable<RawControl> getControls()
  {
    return controls;
  }



  /**
   * Indicates whether or not this request has any controls.
   *
   * @return {@code true} if this request has any controls, otherwise
   *         {@code false}.
   */
  public final boolean hasControls()
  {
    return !controls.isEmpty();
  }



  /**
   * Removes the specified control from this request if present.
   *
   * @param oid
   *          The OID of the control to be removed.
   * @return The removed control, or {@code null} if the control is not
   *         included with this request.
   */
  public final RawControl removeControl(String oid)
  {
    // Avoid creating an iterator if possible.
    if (controls.isEmpty())
    {
      return null;
    }

    Iterator<RawControl> iterator = controls.iterator();
    while (iterator.hasNext())
    {
      RawControl control = iterator.next();
      if (control.getOID().equals(oid))
      {
        iterator.remove();
        return control;
      }
    }

    return null;
  }



  /**
   * Returns a string representation of this request.
   *
   * @return A string representation of this request.
   */
  @Override
  public final String toString()
  {
    StringBuilder builder = new StringBuilder();
    toString(builder);
    return builder.toString();
  }



  /**
   * Appends a string representation of this request to the provided
   * buffer.
   *
   * @param buffer
   *          The buffer into which a string representation of this
   *          request should be appended.
   */
  public abstract void toString(StringBuilder buffer);
}
