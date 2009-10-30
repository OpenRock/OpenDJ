package org.opends.sdk.controls;

import org.opends.sdk.util.Validator;

import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Oct 23, 2009
 * Time: 3:41:50 PM
 * To change this template use File | Settings | File Templates.
 */
public final class Controls
{
  private static final Map<String, ControlDecoder> registeredControls =
      new HashMap<String, ControlDecoder>();

  public static void registerControl(String oid, ControlDecoder decoder)
  {
    Validator.ensureNotNull(oid, decoder);
    registeredControls.put(oid, decoder);
  }

  public static ControlDecoder getDecoder(String oid)
  {
    return registeredControls.get(oid);
  }
}
