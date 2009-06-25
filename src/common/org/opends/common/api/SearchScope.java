package org.opends.common.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import static org.opends.server.protocols.ldap.LDAPConstants.*;
import static org.opends.messages.CoreMessages.*;
import org.opends.messages.Message;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 18, 2009 Time: 12:25:12
 * PM To change this template use File | Settings | File Templates.
 */
public final class SearchScope
{
  private static final ArrayList<SearchScope> ELEMENTS =
      new ArrayList<SearchScope>(4);

  public static final SearchScope BASE_OBJECT =
      register(SCOPE_BASE_OBJECT,
          INFO_SEARCH_SCOPE_BASE_OBJECT.get());
  public static final SearchScope SINGLE_LEVEL =
      register(SCOPE_SINGLE_LEVEL,
          INFO_SEARCH_SCOPE_SINGLE_LEVEL.get());
  public static final SearchScope WHOLE_SUBTREE =
      register(SCOPE_WHOLE_SUBTREE,
          INFO_SEARCH_SCOPE_WHOLE_SUBTREE.get());
  public static final SearchScope SUBORDINATE_SUBTREE =
      register(SCOPE_SUBORDINATE_SUBTREE,
          INFO_SEARCH_SCOPE_SUBORDINATE_SUBTREE.get());

  private int intValue;
  private Message name;


  public static SearchScope valueOf(int intValue)
  {
    SearchScope e = ELEMENTS.get(intValue);
    if(e == null)
    {
      e = new SearchScope(intValue, 
          INFO_UNDEFINED_TYPE.get(intValue));
    }
    return e;
  }

  public static List<SearchScope> values()
  {
    return Collections.unmodifiableList(ELEMENTS);
  }

  public int intValue()
  {
    return intValue;
  }

  public String toString()
  {
    return name.toString();
  }

  private SearchScope(int intValue, Message name)
  {
    this.intValue = intValue;
    this.name = name;
  }

  public static SearchScope register(int intValue, Message name)
  {
    SearchScope t = new SearchScope(intValue, name);
    ELEMENTS.add(intValue, t);
    return t;
  }

  @Override
  public boolean equals(Object o)
  {
    return this == o || o instanceof SearchScope &&
        this.intValue == ((SearchScope) o).intValue;

  }

  @Override
  public int hashCode()
  {
    return intValue;
  }
}
