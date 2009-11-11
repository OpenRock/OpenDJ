package org.opends.sdk.ldap;

import org.opends.sdk.*;
import org.opends.sdk.responses.*;
import org.opends.sdk.util.Validator;
import org.opends.sdk.util.ByteString;
import org.opends.sdk.requests.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CancellationException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Oct 21, 2009
 * Time: 4:33:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class AuthenticatedConnectionFactory implements ConnectionFactory
{
  private final BindRequest bindRequest;
  private final ConnectionFactory parentFactory;

  public AuthenticatedConnectionFactory(ConnectionFactory connectionFactory,
                                        BindRequest bindRequest)
  {
    Validator.ensureNotNull(connectionFactory, bindRequest);
    this.parentFactory = connectionFactory;
    this.bindRequest = bindRequest;
  }

  private class AuthenticatedConnectionFutureImpl implements ConnectionFuture,
      ConnectionResultHandler, ResultHandler<BindResult>
  {
    private volatile AuthenticatedConnection connection;
    private volatile ErrorResultException exception;
    private volatile ConnectionFuture connectFuture;
    private volatile BindResultFuture bindFuture;
    private final CountDownLatch latch = new CountDownLatch(1);
    private final ConnectionResultHandler handler;
    private boolean cancelled;

    private AuthenticatedConnectionFutureImpl(ConnectionResultHandler handler) {
      this.handler = handler;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
      cancelled = connectFuture.cancel(mayInterruptIfRunning) ||
          bindFuture != null && bindFuture.cancel(mayInterruptIfRunning);
      if(cancelled)
      {
        latch.countDown();
      }
      return cancelled;
    }

    public AuthenticatedConnection get()
        throws InterruptedException, ErrorResultException {
      latch.await();
      if(cancelled)
      {
        throw new CancellationException();
      }
      if(exception != null)
      {
        throw exception;
      }
      return connection;
    }

    public AuthenticatedConnection get(long timeout, TimeUnit unit)
        throws InterruptedException, TimeoutException, ErrorResultException {
      latch.await(timeout, unit);
      if(cancelled)
      {
        throw new CancellationException();
      }
      if(exception != null)
      {
        throw exception;
      }
      return connection;
    }

    public boolean isCancelled() {
      return cancelled;
    }

    public boolean isDone() {
      return latch.getCount() == 0;
    }

    public void handleConnection(Connection connection) {
      this.connection = new AuthenticatedConnectionWrapper(connection);
      this.bindFuture = this.connection.bind(bindRequest, this);
    }

    public void handleConnectionError(ErrorResultException error) {
      exception = error;
      latch.countDown();
    }

    public void handleResult(BindResult result) {
      latch.countDown();
      if(handler != null)
      {
        handler.handleConnection(connection);
      }
    }

    public void handleError(ErrorResultException error) {
      exception = error;
      latch.countDown();
      if(handler != null)
      {
        handler.handleConnectionError(exception);
      }
    }
  }

  private static class AuthenticatedConnectionWrapper
      implements AuthenticatedConnection
  {
    private final Connection connection;
    private BindRequest authenticatedBindRequest;
    private BindResult authenticatedBindResult;

    private class AuthenticationSaverHandler
        implements ResultHandler<BindResult>
    {
      private final ResultHandler<BindResult> parentHandler;
      private final BindRequest pendingBindRequest;

      private AuthenticationSaverHandler(
          ResultHandler<BindResult> parentHandler,
          BindRequest pendingBindRequest) {
        this.parentHandler = parentHandler;
        this.pendingBindRequest = pendingBindRequest;
      }

      public void handleResult(BindResult result) {
        synchronized(AuthenticatedConnectionWrapper.this)
        {
          authenticatedBindRequest = pendingBindRequest;
          authenticatedBindResult = result;
        }
        if(parentHandler != null)
        {
          parentHandler.handleResult(result);
        }
      }

      public void handleError(ErrorResultException error) {
        if(parentHandler != null)
        {
          parentHandler.handleError(error);
        }
      }
    }

    private AuthenticatedConnectionWrapper(Connection connection) {
      this.connection = connection;
    }

    public synchronized BindRequest getAuthenticatedBindRequest() {
      return authenticatedBindRequest;
    }

    public synchronized BindResult getAuthenticatedBindResult() {
      return authenticatedBindResult;
    }

    public void abandon(AbandonRequest request)
        throws UnsupportedOperationException, IllegalStateException,
        NullPointerException {
      connection.abandon(request);
    }

    public void abandon(int messageID)
        throws UnsupportedOperationException, IllegalStateException {
      connection.abandon(messageID);
    }

    public ResultFuture add(AddRequest request, ResultHandler<Result> handler)
        throws UnsupportedOperationException, IllegalStateException,
        NullPointerException {
      return connection.add(request, handler);
    }

    public ResultFuture add(String name, String... ldifAttributes)
        throws UnsupportedOperationException, IllegalArgumentException,
        IllegalStateException, NullPointerException {
      return connection.add(name, ldifAttributes);
    }

    public ResultFuture add(AttributeSequence entry)
        throws UnsupportedOperationException, IllegalStateException,
        NullPointerException {
      return connection.add(entry);
    }

    public BindResultFuture bind(BindRequest request,
                                 ResultHandler<BindResult> handler)
        throws UnsupportedOperationException, IllegalStateException,
        NullPointerException {
      return connection.bind(request,
          new AuthenticationSaverHandler(handler, request));
    }

    public BindResultFuture bind(String name, String password)
        throws UnsupportedOperationException, IllegalStateException,
        NullPointerException {
      return bind(Requests.newSimpleBindRequest(name, password), null);
    }

    public void close() {
      connection.close();
    }

    public void close(UnbindRequest request) throws NullPointerException {
      connection.close(request);
    }

    public CompareResultFuture compare(CompareRequest request,
                                       ResultHandler<CompareResult> handler)
        throws UnsupportedOperationException, IllegalStateException,
        NullPointerException {
      return connection.compare(request, handler);
    }

    public CompareResultFuture compare(String name, String attributeDescription,
                                       String assertionValue)
        throws UnsupportedOperationException, IllegalStateException,
        NullPointerException {
      return connection.compare(name, attributeDescription, assertionValue);
    }

    public ResultFuture delete(DeleteRequest request,
                               ResultHandler<Result> handler)
        throws UnsupportedOperationException, IllegalStateException,
        NullPointerException {
      return connection.delete(request, handler);
    }

    public ResultFuture delete(String name)
        throws UnsupportedOperationException, IllegalStateException,
        NullPointerException {
      return connection.delete(name);
    }

    public <R extends Result> ExtendedResultFuture<R> extendedRequest(
        ExtendedRequest<R> request, ResultHandler<R> handler)
        throws UnsupportedOperationException, IllegalStateException,
        NullPointerException {
      return connection.extendedRequest(request, handler);
    }

    public ExtendedResultFuture<GenericExtendedResult> extendedRequest(
        String requestName, ByteString requestValue)
        throws UnsupportedOperationException, IllegalStateException,
        NullPointerException {
      return connection.extendedRequest(requestName, requestValue);
    }

    public ResultFuture modify(ModifyRequest request,
                               ResultHandler<Result> handler)
        throws UnsupportedOperationException, IllegalStateException,
        NullPointerException {
      return connection.modify(request, handler);
    }

    public ResultFuture modify(String name, String... ldifChanges)
        throws UnsupportedOperationException, IllegalArgumentException,
        IllegalStateException, NullPointerException {
      return connection.modify(name, ldifChanges);
    }

    public ResultFuture modifyDN(ModifyDNRequest request,
                                 ResultHandler<Result> handler)
        throws UnsupportedOperationException, IllegalStateException,
        NullPointerException {
      return connection.modifyDN(request, handler);
    }

    public ResultFuture modifyDN(String name, String newRDN)
        throws UnsupportedOperationException, IllegalStateException,
        NullPointerException {
      return connection.modifyDN(name, newRDN);
    }

    public SearchResultFuture search(SearchRequest request,
                                     SearchResultHandler handler)
        throws UnsupportedOperationException, IllegalStateException,
        NullPointerException {
      return connection.search(request, handler);
    }

    public SearchResultFuture search(String baseObject, SearchScope scope,
                                     String filter,
                                     String... attributeDescriptions)
        throws UnsupportedOperationException, IllegalArgumentException,
        IllegalStateException, NullPointerException {
      return connection.search(baseObject, scope, filter,
          attributeDescriptions);
    }

    public SearchResultEntry get(String dn, String... attributes)
        throws IllegalArgumentException, IllegalStateException,
        NullPointerException, ErrorResultException, InterruptedException {
      return connection.get(dn, attributes);
    }

    public void addConnectionEventListener(ConnectionEventListener listener)
        throws IllegalStateException, NullPointerException {
      connection.addConnectionEventListener(listener);
    }

    public void removeConnectionEventListener(ConnectionEventListener listener)
        throws NullPointerException {
      connection.removeConnectionEventListener(listener);
    }
  }

  /**
   * Connects to a Directory Server associated with this connection
   * factory.
   *
   * @param handler The completion handler, or {@code null} if no handler is
   *                to be used.
   * @return A future which can be used to retrieve the connection.
   */
  public ConnectionFuture connect(ConnectionResultHandler handler) {
    AuthenticatedConnectionFutureImpl future =
        new AuthenticatedConnectionFutureImpl(handler);
    future.connectFuture = parentFactory.connect(future);
    return future;
  }
}
