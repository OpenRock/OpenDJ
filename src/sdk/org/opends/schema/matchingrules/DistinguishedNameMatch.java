package org.opends.schema.matchingrules;

import org.opends.schema.MatchingRule;
import org.opends.schema.SchemaUtils;
import static org.opends.server.schema.SchemaConstants.*;

import java.util.Collections;

/**
 * This class defines the distinguishedNameMatch matching rule defined in X.520
 * and referenced in RFC 2252.
 */
public class DistinguishedNameMatch extends EqualityMatchingRule
{
  /**
   * Creates a new instance of this matching rule.
   */
  public DistinguishedNameMatch()
  {
    super(EMR_DN_OID, Collections.singletonList(EMR_DN_NAME), "", false,
        SYNTAX_DN_OID, SchemaUtils.RFC4512_ORIGIN);
  }
}
