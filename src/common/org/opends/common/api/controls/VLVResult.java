package org.opends.common.api.controls;

import org.opends.common.api.ResultCode;

import java.util.List;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jun 30, 2009
 * Time: 5:25:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class VLVResult
{
  private static final VLVResult[] ELEMENTS = new VLVResult[81];

  public static final VLVResult SUCCESS =
      register(ResultCode.SUCCESS);
  public static final VLVResult OPERATIONS_ERROR =
      register(ResultCode.OPERATIONS_ERROR);
  public static final VLVResult PROTOCOL_ERROR =
      register(ResultCode.PROTOCOL_ERROR);
  public static final VLVResult TIME_LIMIT_EXCEEDED =
      register(ResultCode.TIME_LIMIT_EXCEEDED);
  public static final VLVResult ADMIN_LIMIT_EXCEEDED =
      register(ResultCode.ADMIN_LIMIT_EXCEEDED);
  public static final VLVResult INAPPROPRIATE_MATCHING =
      register(ResultCode.INAPPROPRIATE_MATCHING);
  public static final VLVResult INSUFFICIENT_ACCESS_RIGHTS =
      register(ResultCode.INSUFFICIENT_ACCESS_RIGHTS);
  public static final VLVResult UNWILLING_TO_PERFORM =
      register(ResultCode.UNWILLING_TO_PERFORM);
  public static final VLVResult SORT_CONTROL_MISSING =
      register(ResultCode.SORT_CONTROL_MISSING);
  public static final VLVResult OFFSET_RANGE_ERROR =
      register(ResultCode.OFFSET_RANGE_ERROR);
  public static final VLVResult OTHER =
      register(ResultCode.OTHER);

  private ResultCode resultCode;

  public static VLVResult valueOf(int intValue)
  {
    VLVResult e = ELEMENTS[intValue];
    if(e == null)
    {
      e = new VLVResult(ResultCode.valueOf(intValue));
    }
    return e;
  }

  public static List<VLVResult> values()
  {
    return Arrays.asList(ELEMENTS);
  }

  public int intValue()
  {
    return resultCode.intValue();
  }

  public String toString()
  {
    return resultCode.toString();
  }

  private VLVResult(ResultCode resultCode)
  {
    this.resultCode = resultCode;
  }

  public static VLVResult register(ResultCode resultCode)
  {
    VLVResult t = new VLVResult(resultCode);
    ELEMENTS[resultCode.intValue()] = t;
    return t;
  }


  @Override
  public boolean equals(Object o)
  {
    return this == o ||
        o instanceof VLVResult && resultCode.equals(o);

  }

  @Override
  public int hashCode()
  {
    return resultCode.hashCode();
  }
}
