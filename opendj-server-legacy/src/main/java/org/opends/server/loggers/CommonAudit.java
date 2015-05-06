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

import java.io.File;
import java.util.Map;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.forgerock.audit.AuditService;
import org.forgerock.i18n.LocalizableMessage;
import org.forgerock.json.fluent.JsonValue;
import org.forgerock.json.resource.Connection;
import org.forgerock.json.resource.ConnectionFactory;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.json.resource.Resources;
import org.forgerock.json.resource.Router;
import org.forgerock.json.resource.RoutingMode;
import org.opends.server.core.ServerContext;
import org.opends.server.types.InitializationException;

/**
 * Entry point for the common audit service.
 */
public class CommonAudit
{
  /** The audit service that accepts audit events. */
  private final AuditService auditService;

  /** The internal connection factory  to the audit service. */
  private final ConnectionFactory connectionFactory;


  /**
   * Creates the common audit.
   *
   * @param serverContext
   *            The server context.
   * @throws InitializationException
   *            If an error occurs during initialization.
   */
  public CommonAudit(ServerContext serverContext) throws InitializationException
  {
    try
    {
      auditService = new AuditService();

      // configure from json config file
      final JsonFactory jsonFactory = new JsonFactory();
      jsonFactory.configure(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, true);
      ObjectMapper mapper = new ObjectMapper(jsonFactory);

      File auditConfigPath = new File(serverContext.getEnvironment().getInstanceRoot(), "config/audit-config.json");
      final JsonValue jsonConfig = new JsonValue(mapper.readValue(auditConfigPath, Map.class));
      //config.put(new JsonPointer("/logTo/0/location"), logDirectoryPath.getAbsolutePath());
      auditService.configure(jsonConfig);

      final Router router = new Router();
      router.addRoute(RoutingMode.STARTS_WITH, "/audit", auditService);
      connectionFactory = Resources.newInternalConnectionFactory(router);
    }
    catch (Exception e)
    {
      throw new InitializationException(LocalizableMessage.raw("Unable to initialize the audit service: %s", e), e);
    }
  }

  /**
   * Returns a internal connection to the audit service.
   *
   * @return a connection to audit service
   * @throws ResourceException
   *            If an error occurs.
   */
  public Connection getInternalConnection() throws ResourceException {
    return connectionFactory.getConnection();
  }

}
