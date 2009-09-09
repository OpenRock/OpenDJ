package org.opends.sdk.schema;

import org.testng.annotations.Test;
import org.opends.sdk.OpenDSTestCase;

/**
* An abstract class that all schema unit test should extend.
*/
@Test(groups = { "precommit", "schema", "sdk" }, sequential = true)
public abstract class SchemaTestCase extends OpenDSTestCase
{
}
