package org.opends.sdk;



import static org.opends.messages.CoreMessages.*;

import java.util.Arrays;
import java.util.List;

import org.opends.messages.Message;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 18, 2009 Time:
 * 12:25:12 PM To change this template use File | Settings | File
 * Templates.
 */
public final class SearchScope
{
  private static final SearchScope[] ELEMENTS = new SearchScope[4];

  public static final SearchScope BASE_OBJECT =
      register(0, INFO_SEARCH_SCOPE_BASE_OBJECT.get());
  public static final SearchScope SINGLE_LEVEL =
      register(1, INFO_SEARCH_SCOPE_SINGLE_LEVEL.get());
  public static final SearchScope WHOLE_SUBTREE =
      register(2, INFO_SEARCH_SCOPE_WHOLE_SUBTREE.get());
  public static final SearchScope SUBORDINATE_SUBTREE =
      register(3, INFO_SEARCH_SCOPE_SUBORDINATE_SUBTREE.get());



  public static SearchScope register(int intValue, Message name)
  {
    SearchScope t = new SearchScope(intValue, name);
    ELEMENTS[intValue] = t;
    return t;
  }



  public static SearchScope valueOf(int intValue)
  {
    SearchScope e = ELEMENTS[intValue];
    if (e == null)
    {
      e = new SearchScope(intValue, INFO_UNDEFINED_TYPE.get(intValue));
    }
    return e;
  }



  public static List<SearchScope> values()
  {
    return Arrays.asList(ELEMENTS);
  }

  private final int intValue;

  private final Message name;



  private SearchScope(int intValue, Message name)
  {
    this.intValue = intValue;
    this.name = name;
  }



  @Override
  public boolean equals(Object o)
  {
    return (this == o)
        || ((o instanceof SearchScope) && (this.intValue == ((SearchScope) o).intValue));

  }



  @Override
  public int hashCode()
  {
    return intValue;
  }



  public int intValue()
  {
    return intValue;
  }



  @Override
  public String toString()
  {
    return name.toString();
  }
}
