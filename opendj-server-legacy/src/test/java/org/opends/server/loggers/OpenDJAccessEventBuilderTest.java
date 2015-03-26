/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2015 ForgeRock AS.
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
