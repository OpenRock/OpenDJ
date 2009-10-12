package org.opends.sdk.schema;

import org.testng.annotations.Test;
import org.testng.Assert;

/**
 * Core schema tests
 */
public class CoreSchemaTest extends SchemaTestCase
{
  @Test
  public final void testCoreSchemaWarnings()
  {
    // Make sure core schema doesn't have any warnings.
    Assert.assertTrue(CoreSchema.instance().getWarnings().isEmpty());
  }
}
