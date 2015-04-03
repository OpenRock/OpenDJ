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

import static org.forgerock.json.resource.Requests.*;
import static org.opends.messages.ConfigMessages.*;
import static org.opends.server.loggers.OpenDJAccessAuditEventBuilder.*;
import static org.opends.server.types.AuthenticationType.*;
import static org.opends.server.util.StaticUtils.*;

import java.util.List;
import java.util.UUID;

import org.forgerock.audit.event.AuditEvent;
import org.forgerock.i18n.LocalizableMessage;
import org.forgerock.i18n.LocalizableMessageBuilder;
import org.forgerock.json.resource.Connection;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.opendj.config.server.ConfigChangeResult;
import org.forgerock.opendj.config.server.ConfigException;
import org.forgerock.opendj.ldap.ResultCode;
import org.opends.server.admin.server.ConfigurationChangeListener;
import org.opends.server.admin.std.server.CommonAuditAccessLogPublisherCfg;
import org.opends.server.api.ClientConnection;
import org.opends.server.api.ExtendedOperationHandler;
import org.opends.server.controls.TransactionIdControl;
import org.opends.server.core.AbandonOperation;
import org.opends.server.core.AddOperation;
import org.opends.server.core.BindOperation;
import org.opends.server.core.CompareOperation;
import org.opends.server.core.DeleteOperation;
import org.opends.server.core.DirectoryServer;
import org.opends.server.core.ExtendedOperation;
import org.opends.server.core.ModifyDNOperation;
import org.opends.server.core.ModifyOperation;
import org.opends.server.core.SearchOperation;
import org.opends.server.core.ServerContext;
import org.opends.server.core.UnbindOperation;
import org.opends.server.types.AuthenticationInfo;
import org.opends.server.types.Control;
import org.opends.server.types.DN;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.DisconnectReason;
import org.opends.server.types.InitializationException;
import org.opends.server.types.Operation;
import org.opends.server.util.ServerConstants;
import org.opends.server.util.TimeThread;

/**
 * Publishes access events to the common audit service.
 */
public final class CommonAuditAccessLogPublisher extends AbstractTextAccessLogPublisher<CommonAuditAccessLogPublisherCfg>
    implements ConfigurationChangeListener<CommonAuditAccessLogPublisherCfg>
{

  /** Connection to the audit service. */
  private Connection connection;

  private CommonAuditAccessLogPublisherCfg cfg;
  private boolean includeControlOIDs;
  private String timeStampFormat = "dd/MMM/yyyy:HH:mm:ss Z";

  /**
   * {@inheritDoc}
   */
  @Override
  public void initializeLogPublisher(final CommonAuditAccessLogPublisherCfg cfg, ServerContext serverContext)
      throws ConfigException, InitializationException
  {

    try
    {
      connection = serverContext.getCommonAudit().getInternalConnection();
    }
    catch (ResourceException e)
    {
      // TODO: use the correct message = cannot connect to audit service
      throw new InitializationException(ERR_CONFIG_LOGGING_CANNOT_CREATE_WRITER.get(cfg.dn(), e), e);
    }

    initializeFilters(cfg);

    this.cfg = cfg;
    includeControlOIDs = cfg.isLogControlOids();
    timeStampFormat = cfg.getLogRecordTimeFormat();

    cfg.addCommonAuditAccessChangeListener(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ConfigChangeResult applyConfigurationChange(final CommonAuditAccessLogPublisherCfg config)
  {
    final ConfigChangeResult ccr = new ConfigChangeResult();
    try
    {
      // TODO : apply changes accordingly to the config
      cfg = config;
      includeControlOIDs = cfg.isLogControlOids();
    }
    catch (final Exception e)
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
  public boolean isConfigurationAcceptable(final CommonAuditAccessLogPublisherCfg configuration,
      final List<LocalizableMessage> unacceptableReasons)
  {
    return isFilterConfigurationAcceptable(configuration, unacceptableReasons)
        && isConfigurationChangeAcceptable(configuration, unacceptableReasons);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isConfigurationChangeAcceptable(final CommonAuditAccessLogPublisherCfg config,
      final List<LocalizableMessage> unacceptableReasons)
  {
    return true;
  }

  /**
   * Writes a message to the access logger with information about the result of
   * the provided abandon operation.
   *
   * @param abandonOperation
   *          The abandon operation containing the information to use to log the
   *          abandon request.
   */
  @Override
  public void logAbandonResult(final AbandonOperation abandonOperation)
  {
    if (!isResponseLoggable(abandonOperation))
    {
      return;
    }
    OpenDJAccessAuditEventBuilder<?> builder = getEventBuilder(abandonOperation, "ABANDON");
    appendAbandonRequest(abandonOperation, builder);
    appendResultCodeAndMessage(abandonOperation, builder);

    sendEvent(builder.toEvent());
  }

  /**
   * Writes a message to the access logger with information about the add
   * response associated with the provided add operation.
   *
   * @param addOperation
   *          The add operation containing the information to use to log the add
   *          response.
   */
  @Override
  public void logAddResponse(final AddOperation addOperation)
  {
    if (!isResponseLoggable(addOperation))
    {
      return;
    }
    OpenDJAccessAuditEventBuilder<?> builder = getEventBuilder(addOperation, "ADD");
    appendAddRequest(addOperation, builder);
    appendResultCodeAndMessage(addOperation, builder);
    builder.authenticationId(addOperation.getProxiedAuthorizationDN().toString());

    sendEvent(builder.toEvent());
  }

  /**
   * Writes a message to the access logger with information about the bind
   * response associated with the provided bind operation.
   *
   * @param bindOperation
   *          The bind operation containing the information to use to log the
   *          bind response.
   */
  @Override
  public void logBindResponse(final BindOperation bindOperation)
  {
    if (!isResponseLoggable(bindOperation))
    {
      return;
    }

    OpenDJAccessAuditEventBuilder<?> builder = getEventBuilder(bindOperation, "BIND");
    appendBindRequest(bindOperation, builder);
    appendResultCodeAndMessage(bindOperation, builder);

    final LocalizableMessage failureMessage = bindOperation.getAuthFailureReason();
    if (failureMessage != null)
    {
      // this code path is mutually exclusive with the if result code is success
      // down below
      builder.ldapFailureMessage(failureMessage.toString());
      if (bindOperation.getSASLMechanism() != null && bindOperation.getSASLAuthUserEntry() != null)
      { // SASL bind and we have successfully found a user entry for auth
        builder.authenticationId(bindOperation.getSASLAuthUserEntry().getName().toString());
      }
      else
      { // SASL bind failed to find user entry for auth or simple bind
        builder.authenticationId(bindOperation.getRawBindDN().toString());
      }
    }

    if (bindOperation.getResultCode() == ResultCode.SUCCESS)
    {
      // this code path is mutually exclusive with the if failure message exist
      // just above
      final AuthenticationInfo authInfo = bindOperation.getAuthenticationInfo();
      if (authInfo != null)
      {
        final DN authDN = authInfo.getAuthenticationDN();
        if (authDN != null)
        {
          builder.authenticationId(authDN.toString());

          final DN authzDN = authInfo.getAuthorizationDN();
          if (!authDN.equals(authzDN))
          {
            builder.authorizationId("", authzDN.toString());
          }
        }
        else
        {
          builder.authenticationId("");
        }
      }
    }

    sendEvent(builder.toEvent());
  }

  /**
   * Writes a message to the access logger with information about the compare
   * response associated with the provided compare operation.
   *
   * @param compareOperation
   *          The compare operation containing the information to use to log the
   *          compare response.
   */
  @Override
  public void logCompareResponse(final CompareOperation compareOperation)
  {
    if (!isResponseLoggable(compareOperation))
    {
      return;
    }
    OpenDJAccessAuditEventBuilder<?> builder = getEventBuilder(compareOperation, "COMPARE");
    appendCompareRequest(compareOperation, builder);
    appendResultCodeAndMessage(compareOperation, builder);
    DN proxiedAuthorizationDN = compareOperation.getProxiedAuthorizationDN();
    if (proxiedAuthorizationDN!=null) {
      builder.authorizationId("", proxiedAuthorizationDN.toString());
    }

    sendEvent(builder.toEvent());
  }

  /**
   * Writes a message to the access logger with information about a new client
   * connection that has been established, regardless of whether it will be
   * immediately terminated.
   *
   * @param clientConnection
   *          The client connection that has been established.
   */
  @Override
  public void logConnect(final ClientConnection clientConnection)
  {
    if (!isConnectLoggable(clientConnection))
    {
      return;
    }
    OpenDJAccessAuditEventBuilder<?> builder = openDJAccessEvent()
        .timestamp(System.currentTimeMillis())
        .client(clientConnection.getClientAddress(), clientConnection.getClientPort())
        .server(clientConnection.getServerAddress(), clientConnection.getServerPort())
        .resourceOperation("CONNECT")
        .transactionId(UUID.randomUUID().toString())
        .messageId(String.format("DJ-%s-%s", clientConnection.getProtocol(), "CONNECT"))
        .response(String.valueOf(ResultCode.SUCCESS.intValue()), 0)
        .ldapConnectionId(clientConnection.getConnectionID());

    sendEvent(builder.toEvent());
  }

  /**
   * Writes a message to the access logger with information about the delete
   * response associated with the provided delete operation.
   *
   * @param deleteOperation
   *          The delete operation containing the information to use to log the
   *          delete response.
   */
  @Override
  public void logDeleteResponse(final DeleteOperation deleteOperation)
  {
    if (!isResponseLoggable(deleteOperation))
    {
      return;
    }
    OpenDJAccessAuditEventBuilder<?> builder = getEventBuilder(deleteOperation, "DELETE");
    appendDeleteRequest(deleteOperation, builder);
    appendResultCodeAndMessage(deleteOperation, builder);
    DN proxiedAuthorizationDN = deleteOperation.getProxiedAuthorizationDN();
    if (proxiedAuthorizationDN!=null) {
      builder.authorizationId("", proxiedAuthorizationDN.toString());
    }

    sendEvent(builder.toEvent());
  }

  /**
   * Writes a message to the access logger with information about the
   * termination of an existing client connection.
   *
   * @param clientConnection
   *          The client connection that has been terminated.
   * @param disconnectReason
   *          A generic disconnect reason for the connection termination.
   * @param message
   *          A human-readable message that can provide additional information
   *          about the disconnect.
   */
  @Override
  public void logDisconnect(final ClientConnection clientConnection, final DisconnectReason disconnectReason,
      final LocalizableMessage message)
  {
    if (!isDisconnectLoggable(clientConnection))
    {
      return;
    }
    OpenDJAccessAuditEventBuilder<?> builder = openDJAccessEvent()
        .timestamp(System.currentTimeMillis())
        .client(clientConnection.getClientAddress(), clientConnection.getClientPort())
        .server(clientConnection.getServerAddress(), clientConnection.getServerPort())
        .resourceOperation("DISCONNECT")
        .transactionId(UUID.randomUUID().toString())
        .messageId(String.format("DJ-%s-%s", clientConnection.getProtocol(), "DISCONNECT"))
        .response(String.valueOf(ResultCode.SUCCESS.intValue()), 0)
        .ldapConnectionId(clientConnection.getConnectionID())
        .ldapReason(disconnectReason.toString())
        .ldapMessage(message.toString());

    sendEvent(builder.toEvent());
  }

  /**
   * Writes a message to the access logger with information about the extended
   * response associated with the provided extended operation.
   *
   * @param extendedOperation
   *          The extended operation containing the info to use to log the
   *          extended response.
   */
  @Override
  public void logExtendedResponse(final ExtendedOperation extendedOperation)
  {
    // TODO: update the method to make it work
    if (!isResponseLoggable(extendedOperation))
    {
      return;
    }
    OpenDJAccessAuditEventBuilder<?> builder = getEventBuilder(extendedOperation, "EXTENDED");
    //appendExtendedRequest(extendedOperation, object);
    appendResultCodeAndMessage(extendedOperation, builder);
    final String oid = extendedOperation.getResponseOID();
    if (oid != null)
    {
      final ExtendedOperationHandler<?> extOpHandler = DirectoryServer.getExtendedOperationHandler(oid);
      if (extOpHandler != null)
      {
        String name = extOpHandler.getExtendedOperationName();
        //appendField(object, "name", name);
      }
      //appendField(object, "oid", oid);
    }

    sendEvent(builder.toEvent());
  }

  /**
   * Writes a message to the access logger with information about the modify DN
   * response associated with the provided modify DN operation.
   *
   * @param modifyDNOperation
   *          The modify DN operation containing the information to use to log
   *          the modify DN response.
   */
  @Override
  public void logModifyDNResponse(final ModifyDNOperation modifyDNOperation)
  {
    if (!isResponseLoggable(modifyDNOperation))
    {
      return;
    }
    OpenDJAccessAuditEventBuilder<?> builder = getEventBuilder(modifyDNOperation, "MODIFYDN");
    appendModifyDNRequest(modifyDNOperation, builder);
    appendResultCodeAndMessage(modifyDNOperation, builder);
    DN proxiedAuthorizationDN = modifyDNOperation.getProxiedAuthorizationDN();
    if (proxiedAuthorizationDN!=null) {
      builder.authorizationId("", proxiedAuthorizationDN.toString());
    }

    sendEvent(builder.toEvent());
  }

  /**
   * Writes a message to the access logger with information about the modify
   * response associated with the provided modify operation.
   *
   * @param modifyOperation
   *          The modify operation containing the information to use to log the
   *          modify response.
   */
  @Override
  public void logModifyResponse(final ModifyOperation modifyOperation)
  {
    if (!isResponseLoggable(modifyOperation))
    {
      return;
    }
    OpenDJAccessAuditEventBuilder<?> builder = getEventBuilder(modifyOperation, "MODIFY");
    appendModifyRequest(modifyOperation, builder);
    appendResultCodeAndMessage(modifyOperation, builder);
    DN proxiedAuthorizationDN = modifyOperation.getProxiedAuthorizationDN();
    if (proxiedAuthorizationDN!=null) {
      builder.authorizationId("", proxiedAuthorizationDN.toString());
    }

    sendEvent(builder.toEvent());
  }

  /**
   * Writes a message to the access logger with information about the completion
   * of the provided search operation.
   *
   * @param searchOperation
   *          The search operation containing the information to use to log the
   *          search result done message.
   */
  @Override
  public void logSearchResultDone(final SearchOperation searchOperation)
  {
    if (!isResponseLoggable(searchOperation))
    {
      return;
    }
    OpenDJAccessAuditEventBuilder<?> builder = getEventBuilder(searchOperation, "SEARCH");
    builder
        .ldapSearch(searchOperation)
        .ldapNEntries(searchOperation.getEntriesSent());
        appendResultCodeAndMessage(searchOperation, builder);
    DN proxiedAuthorizationDN = searchOperation.getProxiedAuthorizationDN();
    if (proxiedAuthorizationDN!=null) {
      builder.authorizationId("", proxiedAuthorizationDN.toString());
    }

    sendEvent(builder.toEvent());
  }

  /**
   * Writes a message to the access logger with information about the unbind
   * request associated with the provided unbind operation.
   *
   * @param unbindOperation
   *          The unbind operation containing the info to use to log the unbind
   *          request.
   */
  @Override
  public void logUnbind(final UnbindOperation unbindOperation)
  {
    if (!isRequestLoggable(unbindOperation))
    {
      return;
    }
    sendEvent(getEventBuilder(unbindOperation, "UNBIND").toEvent());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void close0()
  {
   // writer.shutdown();
    TimeThread.removeUserDefinedFormatter(timeStampFormat);
    if (cfg != null)
    {
      cfg.removeCommonAuditAccessChangeListener(this);
    }
  }

  private void appendAbandonRequest(final AbandonOperation abandonOperation,
      final OpenDJAccessAuditEventBuilder<?> builder)
  {
    builder.ldapIdToAbandon(abandonOperation.getIDToAbandon());
  }

  private void appendAddRequest(final AddOperation addOperation, OpenDJAccessAuditEventBuilder<?> builder)
  {
    builder.ldapDn(addOperation.getRawEntryDN().toString());
  }

  private void appendBindRequest(final BindOperation bindOperation, final OpenDJAccessAuditEventBuilder<?> builder)
  {
    // TODO fix this method by adding missing params

    //appendField(object, "version", bindOperation.getProtocolVersion());
    final String authType = bindOperation.getAuthenticationType() != SASL ?
        bindOperation.getAuthenticationType().toString() : "SASL mechanism=" + bindOperation.getSASLMechanism();
    //object.put("type", authType);

    builder.ldapDn(bindOperation.getRawBindDN().toString());

  }

  private void appendCompareRequest(final CompareOperation compareOperation,
      final OpenDJAccessAuditEventBuilder<?> builder)
  {
    // TODO fix this method by adding missing params

    builder.ldapDn(compareOperation.getRawEntryDN().toString());
    //appendField(object, "attr", compareOperation.getAttributeType().getNameOrOID());
  }

  private void appendDeleteRequest(final DeleteOperation deleteOperation,
      final OpenDJAccessAuditEventBuilder<?> builder)
  {
    builder.ldapDn(deleteOperation.getRawEntryDN().toString());
  }

  private void appendExtendedRequest(final ExtendedOperation extendedOperation,
      final OpenDJAccessAuditEventBuilder<?> builder)
  {
    // TODO: fix this method
    final String oid = extendedOperation.getRequestOID();
    final ExtendedOperationHandler<?> extOpHandler = DirectoryServer.getExtendedOperationHandler(oid);
    if (extOpHandler != null)
    {
      final String name = extOpHandler.getExtendedOperationName();
      //appendField(value, "name", name);
    }
    //appendField(value, "oid", oid);

  }

  private void appendModifyDNRequest(final ModifyDNOperation modifyDNOperation,
      final OpenDJAccessAuditEventBuilder<?> builder)
  {
    // TODO fix this method by adding missing params

    builder.ldapDn(modifyDNOperation.getRawEntryDN().toString());
//    appendField(object, "newRDN", modifyDNOperation.getRawNewRDN());
//    appendField(object, "deleteOldRDN", modifyDNOperation.deleteOldRDN());
//    appendField(object, "newSuperior", modifyDNOperation.getRawNewSuperior());
  }

  private void appendModifyRequest(final ModifyOperation modifyOperation,
      final OpenDJAccessAuditEventBuilder<?> builder)
  {
    builder.ldapDn(modifyOperation.getRawEntryDN().toString());
  }

  private OpenDJAccessAuditEventBuilder<?> appendResultCodeAndMessage(
      Operation operation,
      OpenDJAccessAuditEventBuilder<?> builder)
  {
    final LocalizableMessageBuilder message = operation.getErrorMessage();
    if (message != null && message.length() > 0)
    {
      builder.responseWithMessage(
          String.valueOf(operation.getResultCode().intValue()),
          getExecutionTime(operation),
          message.toString());
    }
    else {
      builder.response(String.valueOf(operation.getResultCode().intValue()), getExecutionTime(operation));
    }
    builder.ldapMaskedResultAndMessage(operation);
    return builder;
  }

  /** Returns an event builder with all common fields filled. */
  private OpenDJAccessAuditEventBuilder<?> getEventBuilder(final Operation operation, final String opType)
  {
    ClientConnection clientConn = operation.getClientConnection();

    OpenDJAccessAuditEventBuilder<?> builder = openDJAccessEvent()
      .timestamp(System.currentTimeMillis())
      .client(clientConn.getClientAddress(), clientConn.getClientPort())
      .server(clientConn.getServerAddress(), clientConn.getServerPort())
      .resourceOperation(opType)
      .ldapAdditionalItems(operation)
      .ldapSync(operation)
      .ldapIds(operation)
      .transactionId(getTransactionId(operation))
      .messageId(String.format("DJ-%s-%s", clientConn.getProtocol(), opType));

    if (includeControlOIDs)
    {
      builder.ldapControls(operation);
    }

    return builder;
  }

  private String getTransactionId(Operation operation) {
    String transactionId = getTransactionIdFromControl(operation);
    if (transactionId==null) {
      transactionId = UUID.randomUUID().toString();
    }
    return transactionId;
  }

  private String getTransactionIdFromControl(Operation operation)
  {
    for (Control control : operation.getRequestControls()) {
      if (control.getOID().equals(ServerConstants.OID_TRANSACTION_ID_CONTROL)) {
        try
        {
          return operation.getRequestControl(TransactionIdControl.DECODER).getTransactionId();
        }
        catch (DirectoryException e)
        {
            logger.error(LocalizableMessage.raw("Error when trying to decode TransactionIdControl: %s", e), e);
        }
      }
    }
    return null;
  }

  private long getExecutionTime(final Operation operation)
  {
    long etime = operation.getProcessingNanoTime();
    if (etime <= -1)
    {
      // if it is not configured for nanos, then use millis.
      etime = operation.getProcessingTime();
    }
    return etime;
  }

  /** Sends an JSON-encoded event to the audit service. */
  private void sendEvent(AuditEvent event)
  {
    try
    {
      connection.create(null, newCreateRequest("/audit/access", event.getValue()));
    }
    catch (ResourceException e)
    {
      logger.error(LocalizableMessage.raw("Error when sending event to /audit/access: %s.", e), e);
    }
  }

}
