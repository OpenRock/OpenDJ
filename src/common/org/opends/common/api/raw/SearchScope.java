package org.opends.common.api.raw;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import static org.opends.server.protocols.ldap.LDAPConstants.*;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 18, 2009 Time: 12:25:12
 * PM To change this template use File | Settings | File Templates.
 */
public final class SearchScope
{
  private static final ArrayList<SearchScope> ELEMENTS =
      new ArrayList<SearchScope>(4);

  public static final SearchScope BASE_OBJECT =
      register(SCOPE_BASE_OBJECT, "baseObject");
  public static final SearchScope SINGLE_LEVEL =
      register(SCOPE_SINGLE_LEVEL, "singleLevel");
  public static final SearchScope WHOLE_SUBTREE =
      register(SCOPE_WHOLE_SUBTREE, "wholeSubtree");
  public static final SearchScope SUBORDINATE_SUBTREE =
      register(SCOPE_SUBORDINATE_SUBTREE, "subordinateSubtree");

  private int intValue;
  private String name;


  public static SearchScope valueOf(int intValue)
  {
    SearchScope e = ELEMENTS.get(intValue);
    if(e == null)
    {
      e = new SearchScope(intValue, "undefinded("+intValue+")");
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
    return name;
  }

  private SearchScope(int intValue, String name)
  {
    this.intValue = intValue;
    this.name = name;
  }

  public final static SearchScope register(int intValue, String name)
  {
    SearchScope t = new SearchScope(intValue, name);
    ELEMENTS.add(intValue, t);
    return t;
  }
}
