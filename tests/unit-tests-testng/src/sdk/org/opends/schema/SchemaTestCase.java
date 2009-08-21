package org.opends.schema;

import org.testng.annotations.Test;
import org.opends.OpenDSTestCase;

/**
* An abstract class that all schema unit test should extend.
*/
@Test(groups = { "precommit", "schema", "sdk" }, sequential = true)
public abstract class SchemaTestCase extends OpenDSTestCase
{
}
