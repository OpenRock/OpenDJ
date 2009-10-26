package org.opends.sdk.ldap;

import org.opends.sdk.*;
import org.opends.sdk.responses.BindResult;
import org.opends.sdk.responses.ResultHandler;
import org.opends.sdk.responses.BindResultFuture;
import org.opends.sdk.util.Validator;
import org.opends.sdk.requests.BindRequest;

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

  private class ConnectionFutureImpl implements ConnectionFuture,
      ConnectionResultHandler, ResultHandler<BindResult>
  {
    private volatile Connection connection;
    private volatile ErrorResultException exception;
    private volatile ConnectionFuture connectFuture;
    private volatile BindResultFuture bindFuture;
    private final CountDownLatch latch = new CountDownLatch(1);
    private final ConnectionResultHandler handler;
    private boolean cancelled;

    private ConnectionFutureImpl(ConnectionResultHandler handler) {
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

    public Connection get() throws InterruptedException, ErrorResultException {
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

    public Connection get(long timeout, TimeUnit unit)
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
      this.connection = connection;
      this.bindFuture = connection.bind(bindRequest, this);
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

  /**
   * Connects to a Directory Server associated with this connection
   * factory.
   *
   * @param handler The completion handler, or {@code null} if no handler is
   *                to be used.
   * @return A future which can be used to retrieve the connection.
   */
  public ConnectionFuture connect(ConnectionResultHandler handler) {
    ConnectionFutureImpl future = new ConnectionFutureImpl(handler);
    future.connectFuture = parentFactory.connect(future);
    return future;
  }
}
