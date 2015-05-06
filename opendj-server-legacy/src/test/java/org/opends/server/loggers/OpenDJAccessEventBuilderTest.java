/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at legal-notices/CDDLv1_0.txt
 * or http://forgerock.org/license/CDDLv1.0.html.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at legal-notices/CDDLv1_0.txt.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Copyright 2015 ForgeRock AS
 */
package org.opends.server.loggers;

import static org.assertj.core.api.Assertions.*;

import org.forgerock.audit.event.AuditEvent;
import org.forgerock.json.fluent.JsonValue;
import org.testng.annotations.Test;

@SuppressWarnings("javadoc")
public class OpenDJAccessEventBuilderTest {

    @Test
    public void ensureEventIsCorrectlyBuilt() {
      AuditEvent event = OpenDJAccessAuditEventBuilder.openDJAccessEvent()
                .transactionId("transactionId")
                .messageId("DJ-SEARCH")
                .ldapConnectionId(1)
                .toEvent();

        JsonValue value = event.getValue();
        assertThat(value.get("transactionId").getObject()).isEqualTo("transactionId");
        assertThat(value.get("messageId").getObject()).isEqualTo("DJ-SEARCH");
        assertThat(value.get("ldap").get("connId").getObject()).isEqualTo(1L);
    }

}
