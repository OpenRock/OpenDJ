package org.opends.sdk.schema;

import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import org.testng.Assert;
import org.opends.sdk.DecodeException;
import org.opends.sdk.schema.SchemaElement;
import org.opends.sdk.schema.SchemaException;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Aug 24, 2009
 * Time: 1:56:27 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractSchemaElementTestCase extends SchemaTestCase
{
  protected static final Map<String, List<String>> EMPTY_PROPS =
      Collections.emptyMap();
  protected static final List<String> EMPTY_NAMES = Collections.emptyList();

  protected abstract SchemaElement getElement(String description,
                                     Map<String, List<String>> extraProperties)
      throws SchemaException;

  @DataProvider(name = "equalsTestData")
  public abstract Object[][] createEqualsTestData()
      throws SchemaException, DecodeException;



  /**
   * Check that the equals operator works as expected.
   *
   * @param e1
   *          The first element
   * @param e2
   *          The second element
   * @param result
   *          The expected result.
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test(dataProvider = "equalsTestData")
  public final void testEquals(SchemaElement e1,
      SchemaElement e2, boolean result) throws Exception {

    Assert.assertEquals(e1.equals(e2), result);
    Assert.assertEquals(e2.equals(e1), result);
  }



  /**
   * Check that the hasCode method operator works as expected.
   *
   * @param e1
   *          The first element
   * @param e2
   *          The second element
   * @param result
   *          The expected result.
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test(dataProvider = "equalsTestData")
  public final void testHashCode(SchemaElement e1,
      SchemaElement e2, boolean result) throws Exception {

    Assert.assertEquals(e1.hashCode() == e2.hashCode(), result);
  }



  /**
   * Check that the {@link SchemaElement#getDescription()}
   * method returns <code>null</code> when there is no description.
   *
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test
  public final void testGetDescriptionDefault() throws Exception {
    SchemaElement e = getElement("", EMPTY_PROPS);
    Assert.assertEquals(e.getDescription(), "");
  }



  /**
   * Check that the {@link SchemaElement#getDescription()}
   * method returns a description.
   *
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test
  public final void testGetDescription() throws Exception {
    SchemaElement e = getElement("hello", EMPTY_PROPS);
    Assert.assertEquals(e.getDescription(), "hello");
  }



  /**
   * Check that the
   * {@link SchemaElement#getExtraProperty(String)} method
   * returns <code>null</code> when there is no property.
   *
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test
  public final void testGetExtraPropertyDefault() throws Exception {
    SchemaElement e = getElement("", EMPTY_PROPS);
    Assert.assertNull(e.getExtraProperty("test"));
  }



  /**
   * Check that the
   * {@link SchemaElement#getExtraProperty(String)} method
   * returns values.
   *
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test
  public final void testGetExtraProperty() throws Exception {
    List<String> values = new ArrayList<String>();
    values.add("one");
    values.add("two");
    Map<String, List<String>> props = Collections.singletonMap("test", values);
    SchemaElement e = getElement("", props);

    Assert.assertNotNull(e.getExtraProperty("test"));
    int i = 0;
    for (String value : e.getExtraProperty("test")) {
      Assert.assertEquals(value, values.get(i));
      i++;
    }
  }



  /**
   * Check that the
   * {@link SchemaElement#getExtraPropertyNames()} method.
   *
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test
  public final void testGetExtraPropertyNames() throws Exception {
    SchemaElement e = getElement("", EMPTY_PROPS);
    Assert.assertNull(e.getExtraProperty("test"));
    Assert
        .assertFalse(e.getExtraPropertyNames().iterator().hasNext());
  }
}
