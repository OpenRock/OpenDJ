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
 *      Copyright 2015 ForgeRock AS.
 */
package org.opends.server.loggers;

import static org.opends.messages.ConfigMessages.*;
import static org.forgerock.json.fluent.JsonValue.*;
import static org.forgerock.json.resource.Requests.*;
import static org.forgerock.opendj.ldap.ResultCode.*;
import static org.opends.server.util.ServerConstants.*;
import static org.opends.server.util.StaticUtils.*;

import java.util.List;

import org.forgerock.audit.AuditService;
import org.forgerock.audit.impl.AuditServiceImpl;
import org.forgerock.i18n.LocalizableMessage;
import org.forgerock.json.fluent.JsonValue;
import org.forgerock.json.resource.Connection;
import org.forgerock.json.resource.ConnectionFactory;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.json.resource.Resources;
import org.forgerock.json.resource.Router;
import org.forgerock.json.resource.RoutingMode;
import org.opends.server.admin.server.ConfigurationChangeListener;
import org.opends.server.admin.std.server.CrestBasedAuditLogPublisherCfg;
import org.forgerock.opendj.config.server.ConfigChangeResult;
import org.forgerock.opendj.config.server.ConfigException;
import org.opends.server.core.*;
import org.opends.server.types.*;
import org.forgerock.opendj.ldap.ByteSequence;
import org.forgerock.opendj.ldap.ByteString;
import org.opends.server.util.Base64;
import org.opends.server.util.StaticUtils;
import org.opends.server.util.TimeThread;

/**
 * Publishes audit events to the CREST audit service.
 */
public final class CrestAuditLogPublisher extends AbstractTextAccessLogPublisher<CrestBasedAuditLogPublisherCfg>
    implements ConfigurationChangeListener<CrestBasedAuditLogPublisherCfg>
{

  /** Current configuration of the publisher. */
  private CrestBasedAuditLogPublisherCfg cfg;

  /** Connection to the audit service. */
  private Connection connection;

  /** Handler for errors that may occur when publishing events. */
  // TODO: handler is file-oriented so it's probably not very well adapted !
  private LogPublisherErrorHandler errorHandler;

  /**
   * {@inheritDoc}
   */
  @Override
  public void initializeLogPublisher(CrestBasedAuditLogPublisherCfg cfg, ServerContext serverContext)
      throws ConfigException, InitializationException
  {
    try
    {
      ConnectionFactory connectionFactory = getAuditInternalConnectionFactory();
      connection = connectionFactory.getConnection();
      errorHandler = new LogPublisherErrorHandler(cfg.dn());
    }
    catch (ResourceException e)
    {
      // TODO: use the correct message = cannot connect to audit service
      throw new InitializationException(ERR_CONFIG_LOGGING_CANNOT_CREATE_WRITER.get(cfg.dn(), e), e);
    }

    initializeFilters(cfg);
    this.cfg = cfg;
    cfg.addCrestBasedAuditChangeListener(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ConfigChangeResult applyConfigurationChange(CrestBasedAuditLogPublisherCfg config)
  {
    final ConfigChangeResult ccr = new ConfigChangeResult();
    try
    {
      // TODO : apply changes accordingly to the config
      cfg = config;
    }
    catch (Exception e)
    {
      ccr.setResultCode(DirectoryServer.getServerErrorResultCode());
      ccr.addMessage(ERR_CONFIG_LOGGING_CANNOT_CREATE_WRITER.get(config.dn(), stackTraceToSingleLineString(e)));
    }
    return ccr;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void close0()
  {
    connection.close();
    cfg.removeCrestBasedAuditChangeListener(this);
  }

  // TODO: move this field, the static init and the getter in a more appropriate place
  private static final ConnectionFactory connectionFactory;

  static {
    final Router router = new Router();
    router.addRoute(RoutingMode.STARTS_WITH, AuditService.ROUTER_PREFIX, new AuditServiceImpl());
    connectionFactory = Resources.newInternalConnectionFactory(router);
  }

  /**
   * Returns the connection factory to use for connecting to audit service.
   *
   * @return the connection factory
   */
  public static ConnectionFactory getAuditInternalConnectionFactory()
  {
    return connectionFactory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isConfigurationAcceptable(CrestBasedAuditLogPublisherCfg configuration,
      List<LocalizableMessage> unacceptableReasons)
  {
    return isFilterConfigurationAcceptable(configuration, unacceptableReasons)
        && isConfigurationChangeAcceptable(configuration, unacceptableReasons);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isConfigurationChangeAcceptable(CrestBasedAuditLogPublisherCfg config,
      List<LocalizableMessage> unacceptableReasons)
  {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void logAddResponse(AddOperation addOperation)
  {
    if (!isLoggable(addOperation))
    {
      return;
    }

    StringBuilder buffer = new StringBuilder(50);
    buffer.append("dn:");
    encodeValue(addOperation.getEntryDN().toString(), buffer);
    buffer.append(EOL);

    buffer.append("changetype: add");
    buffer.append(EOL);

    for (String ocName : addOperation.getObjectClasses().values())
    {
      buffer.append("objectClass: ");
      buffer.append(ocName);
      buffer.append(EOL);
    }

    for (List<Attribute> attrList : addOperation.getUserAttributes().values())
    {
      for (Attribute a : attrList)
      {
        append(buffer, a);
      }
    }

    for (List<Attribute> attrList : addOperation.getOperationalAttributes().values())
    {
      for (Attribute a : attrList)
      {
        append(buffer, a);
      }
    }

    sendEvent(addOperation, buffer.toString());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void logDeleteResponse(DeleteOperation deleteOperation)
  {
    if (!isLoggable(deleteOperation))
    {
      return;
    }

    StringBuilder buffer = new StringBuilder(50);

    buffer.append("dn:");
    encodeValue(deleteOperation.getEntryDN().toString(), buffer);
    buffer.append(EOL);

    buffer.append("changetype: delete");
    buffer.append(EOL);

    sendEvent(deleteOperation, buffer.toString());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void logModifyDNResponse(ModifyDNOperation modifyDNOperation)
  {
    if (!isLoggable(modifyDNOperation))
    {
      return;
    }

    StringBuilder buffer = new StringBuilder(50);

    buffer.append("dn:");
    encodeValue(modifyDNOperation.getEntryDN().toString(), buffer);
    buffer.append(EOL);

    buffer.append("changetype: moddn");
    buffer.append(EOL);

    buffer.append("newrdn:");
    encodeValue(modifyDNOperation.getNewRDN().toString(), buffer);
    buffer.append(EOL);

    buffer.append("deleteoldrdn: ");
    if (modifyDNOperation.deleteOldRDN())
    {
      buffer.append("1");
    }
    else
    {
      buffer.append("0");
    }
    buffer.append(EOL);

    DN newSuperior = modifyDNOperation.getNewSuperior();
    if (newSuperior != null)
    {
      buffer.append("newsuperior:");
      encodeValue(newSuperior.toString(), buffer);
      buffer.append(EOL);
    }

    sendEvent(modifyDNOperation, buffer.toString());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void logModifyResponse(ModifyOperation modifyOperation)
  {
    if (!isLoggable(modifyOperation))
    {
      return;
    }

    StringBuilder buffer = new StringBuilder(50);

    buffer.append("dn:");
    encodeValue(modifyOperation.getEntryDN().toString(), buffer);
    buffer.append(EOL);

    buffer.append("changetype: modify");
    buffer.append(EOL);

    boolean first = true;
    for (Modification mod : modifyOperation.getModifications())
    {
      if (first)
      {
        first = false;
      }
      else
      {
        buffer.append("-");
        buffer.append(EOL);
      }

      switch (mod.getModificationType().asEnum())
      {
      case ADD:
        buffer.append("add: ");
        break;
      case DELETE:
        buffer.append("delete: ");
        break;
      case REPLACE:
        buffer.append("replace: ");
        break;
      case INCREMENT:
        buffer.append("increment: ");
        break;
      default:
        continue;
      }

      Attribute a = mod.getAttribute();
      buffer.append(a.getName());
      buffer.append(EOL);

      append(buffer, a);
    }

    sendEvent(modifyOperation, buffer.toString());
  }

  private void append(StringBuilder buffer, Attribute a)
  {
    for (ByteString v : a)
    {
      buffer.append(a.getName());
      buffer.append(":");
      encodeValue(v, buffer);
      buffer.append(EOL);
    }
  }

  private void sendEvent(Operation operation, String ldifChange)
  {
    String ldif = ldifChange.replaceAll("(\\r|\\n|\\r\\n)+", "\\\\n").replaceAll("\\/", "_");
    JsonValue jsonEvent = toJson(operation, ldif);
    sendEvent(jsonEvent);
  }

  /** Return all event information as Json. */
  private JsonValue toJson(Operation operation, String ldif)
  {
    return json(object(
        field("timestamp", TimeThread.getLocalTime()),
        field("connId", operation.getConnectionID()),
        field("opId", operation.getOperationID()),
        field("ldif", ldif)));
  }

  /** Sends an JSON-encoded event to the audit service. */
  private void sendEvent(JsonValue eventContent)
  {
    try
    {
      connection.create(null, newCreateRequest(AuditService.ROUTER_PREFIX + "/audit", eventContent));
    }
    catch (ResourceException e)
    {
      // event could not be sent to the audit service, shall we notify ?
      // if (cfg.notifyFailure()) {
      // log or throw an exception
      // }
    }
  }

  /**
   * Appends the appropriately-encoded attribute value to the provided buffer.
   *
   * @param str
   *          The ASN.1 octet string containing the value to append.
   * @param buffer
   *          The buffer to which to append the value.
   */
  private void encodeValue(ByteSequence str, StringBuilder buffer)
  {
    if (StaticUtils.needsBase64Encoding(str))
    {
      buffer.append(": ");
      buffer.append(Base64.encode(str));
    }
    else
    {
      buffer.append(" ");
      buffer.append(str.toString());
    }
  }

  /**
   * Appends the appropriately-encoded attribute value to the provided buffer.
   *
   * @param str
   *          The string containing the value to append.
   * @param buffer
   *          The buffer to which to append the value.
   */
  private void encodeValue(String str, StringBuilder buffer)
  {
    if (StaticUtils.needsBase64Encoding(str))
    {
      buffer.append(": ");
      buffer.append(Base64.encode(getBytes(str)));
    }
    else
    {
      buffer.append(" ");
      buffer.append(str);
    }
  }

  // Determines whether the provided operation should be logged.
  private boolean isLoggable(Operation operation)
  {
    return operation.getResultCode() == SUCCESS && isResponseLoggable(operation);
  }
}
