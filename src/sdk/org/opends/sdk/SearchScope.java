package org.opends.sdk;


import java.util.Arrays;
import java.util.List;



/**
 * This enumeration defines the set of possible scopes that may be
 * used for a search request.  This is based on the LDAP specification
 * defined in RFC 2251 but also includes the subordinate subtree
 * search scope defined in draft-sermersheim-ldap-subordinate-scope.
 */
public final class SearchScope
{
  private static final SearchScope[] ELEMENTS = new SearchScope[4];

  public static final SearchScope BASE_OBJECT =
      register(0, "base");
  public static final SearchScope SINGLE_LEVEL =
      register(1, "single");
  public static final SearchScope WHOLE_SUBTREE =
      register(2, "sub");
  public static final SearchScope SUBORDINATE_SUBTREE =
      register(3, "subordinate");


  public static SearchScope register(int intValue, String name)
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
      e = new SearchScope(intValue, "undefined("+intValue+")");
    }
    return e;
  }



  public static List<SearchScope> values()
  {
    return Arrays.asList(ELEMENTS);
  }

  private final int intValue;
  private final String name;



  private SearchScope(int intValue, String name)
  {
    this.intValue = intValue;
    this.name = name;
  }



  @Override
  public boolean equals(Object o)
  {
    return (this == o)
        || ((o instanceof SearchScope) &&
        (this.intValue == ((SearchScope) o).intValue));

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
    return name;
  }
}
