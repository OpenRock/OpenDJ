package org.opends.sdk;

import org.opends.sdk.responses.Result;
import org.opends.sdk.responses.BindResult;
import org.opends.sdk.responses.CompareResult;
import org.opends.sdk.responses.GenericExtendedResult;
import org.opends.sdk.requests.*;

import java.util.List;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Nov 30, 2009
 * Time: 4:15:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class HeartBeatConnectionFactory
    extends AbstractConnectionFactory<
      HeartBeatConnectionFactory.HeartBeatAsynchronousConnection>
{
  private final SearchRequest heartBeat;
  private final int interval;
  private final List<HeartBeatAsynchronousConnection> activeConnections;
  private final ConnectionFactory<?> parentFactory;

  private boolean stopRequested;

  public HeartBeatConnectionFactory(ConnectionFactory<?> parentFactory,
                                    SearchRequest heartBeat, int interval) {
    this.heartBeat = heartBeat;
    this.interval = interval;
    this.activeConnections = new LinkedList<HeartBeatAsynchronousConnection>();
    this.parentFactory = parentFactory;

    Runtime.getRuntime().addShutdownHook(new Thread()
    {
      @Override
      public void run()
      {
        stopRequested = true;
      }
    });

    new HeartBeatThread().start();
  }

  /**
   * An asynchronous connection that sends heart beats and
   * supports all operations..
   */
  public final class HeartBeatAsynchronousConnection
      implements AsynchronousConnection, ConnectionEventListener,
      ResultHandler<Result, Void>
  {
    private final AsynchronousConnection connection;

    public HeartBeatAsynchronousConnection(AsynchronousConnection connection) {
      this.connection = connection;
    }

    public void abandon(AbandonRequest request)
        throws UnsupportedOperationException, IllegalStateException,
        NullPointerException {
      connection.abandon(request);
    }

    public <P> ResultFuture<Result> add(AddRequest request,
                                        ResultHandler<Result, P> handler, P p)
        throws UnsupportedOperationException, IllegalStateException,
        NullPointerException {
      return connection.add(request, handler, p);
    }

    public <P> ResultFuture<BindResult> bind(
        BindRequest request, ResultHandler<? super BindResult, P> handler, P p)
        throws UnsupportedOperationException, IllegalStateException,
        NullPointerException {
      return connection.bind(request, handler, p);
    }

    public void close() {
      synchronized(activeConnections)
      {
        connection.removeConnectionEventListener(this);
        activeConnections.remove(this);
      }
      connection.close();
    }

    public void close(UnbindRequest request) throws NullPointerException {
      synchronized(activeConnections)
      {
        connection.removeConnectionEventListener(this);
        activeConnections.remove(this);
      }
      connection.close(request);
    }

    public <P> ResultFuture<CompareResult> compare(
        CompareRequest request, ResultHandler<? super CompareResult, P> handler,
        P p) throws UnsupportedOperationException, IllegalStateException,
        NullPointerException {
      return connection.compare(request, handler, p);
    }

    public <P> ResultFuture<Result> delete(DeleteRequest request,
                                           ResultHandler<Result, P> handler,
                                           P p)
        throws UnsupportedOperationException, IllegalStateException,
        NullPointerException {
      return connection.delete(request, handler, p);
    }

    public <R extends Result, P> ResultFuture<R> extendedRequest(
        ExtendedRequest<R> request, ResultHandler<? super R, P> handler, P p)
        throws UnsupportedOperationException, IllegalStateException,
        NullPointerException {
      return connection.extendedRequest(request, handler, p);
    }

    public <P> ResultFuture<Result> modify(ModifyRequest request,
                                           ResultHandler<Result, P> handler,
                                           P p)
        throws UnsupportedOperationException, IllegalStateException,
        NullPointerException {
      return connection.modify(request, handler, p);
    }

    public <P> ResultFuture<Result> modifyDN(ModifyDNRequest request,
                                             ResultHandler<Result, P> handler,
                                             P p)
        throws UnsupportedOperationException, IllegalStateException,
        NullPointerException {
      return connection.modifyDN(request, handler, p);
    }

    public <P> ResultFuture<Result> search(
        SearchRequest request, ResultHandler<Result, P> resultHandler,
        SearchResultHandler<P> searchResultHandler, P p)
        throws UnsupportedOperationException, IllegalStateException,
        NullPointerException {
      return connection.search(request, resultHandler, searchResultHandler, p);
    }

    public void addConnectionEventListener(ConnectionEventListener listener)
        throws IllegalStateException, NullPointerException {
      connection.addConnectionEventListener(listener);
    }

    public void removeConnectionEventListener(ConnectionEventListener listener)
        throws NullPointerException {
      connection.removeConnectionEventListener(listener);
    }

    public void connectionReceivedUnsolicitedNotification(
        GenericExtendedResult notification) {
      // Do nothing
    }

    public void connectionErrorOccurred(boolean isDisconnectNotification,
                                        ErrorResultException error) {
      synchronized(activeConnections)
      {
        connection.removeConnectionEventListener(this);
        activeConnections.remove(this);
      }
    }

    public void handleErrorResult(Void aVoid, ErrorResultException error) {
      // TODO: Log a message
      close();
    }

    public void handleResult(Void aVoid, Result result) {
      // Do nothing
    }

    private void sendHeartBeat()
    {
      search(heartBeat, this, null, null);
    }
  }

  private final class HeartBeatThread extends Thread
  {
    private HeartBeatThread() {
      super("Heart Beat Thread");
    }

    public void run()
    {
      while(!stopRequested)
      {
        synchronized(activeConnections)
        {
          for(HeartBeatAsynchronousConnection connection : activeConnections)
          {
            connection.sendHeartBeat();
          }
        }
        try {
          sleep(interval);
        } catch (InterruptedException e) {
          // Ignore
        }
      }
    }
  }

  private final class ConnectionFutureImpl<P> implements
      ConnectionFuture<HeartBeatAsynchronousConnection>,
      ConnectionResultHandler<AsynchronousConnection, Void>
  {
    private volatile HeartBeatAsynchronousConnection heartBeatConnection;

    private volatile ErrorResultException exception;

    private volatile ConnectionFuture<?> connectFuture;

    private final CountDownLatch latch = new CountDownLatch(1);

    private final ConnectionResultHandler<? super HeartBeatAsynchronousConnection, P> handler;

    private final P p;

    private boolean cancelled;



    private ConnectionFutureImpl(
        ConnectionResultHandler<? super HeartBeatAsynchronousConnection, P> handler,
        P p)
    {
      this.handler = handler;
      this.p = p;
    }



    public boolean cancel(boolean mayInterruptIfRunning)
    {
      cancelled = connectFuture.cancel(mayInterruptIfRunning);
      if (cancelled)
      {
        latch.countDown();
      }
      return cancelled;
    }



    public HeartBeatAsynchronousConnection get()
        throws InterruptedException, ErrorResultException
    {
      latch.await();
      if (cancelled)
      {
        throw new CancellationException();
      }
      if (exception != null)
      {
        throw exception;
      }
      return heartBeatConnection;
    }



    public HeartBeatAsynchronousConnection get(long timeout,
        TimeUnit unit) throws InterruptedException, TimeoutException,
        ErrorResultException
    {
      latch.await(timeout, unit);
      if (cancelled)
      {
        throw new CancellationException();
      }
      if (exception != null)
      {
        throw exception;
      }
      return heartBeatConnection;
    }



    public boolean isCancelled()
    {
      return cancelled;
    }



    public boolean isDone()
    {
      return latch.getCount() == 0;
    }



    public void handleConnection(Void v,
        AsynchronousConnection connection)
    {
      heartBeatConnection = new HeartBeatAsynchronousConnection(connection);
      synchronized(activeConnections)
      {
        connection.addConnectionEventListener(heartBeatConnection);
        activeConnections.add(heartBeatConnection);
      }
      if(handler != null)
      {
        handler.handleConnection(p, heartBeatConnection);
      }
      latch.countDown();
    }



    public void handleConnectionError(Void v, ErrorResultException error)
    {
      exception = error;
      if(handler != null)
      {
        handler.handleConnectionError(p, error);
      }
      latch.countDown();
    }
  }

  public <P> ConnectionFuture<? extends HeartBeatAsynchronousConnection>
  getAsynchronousConnection(ConnectionResultHandler<? super
      HeartBeatAsynchronousConnection, P> pConnectionResultHandler, P p) {
    ConnectionFutureImpl<P> future =
        new ConnectionFutureImpl<P>(pConnectionResultHandler, p);
    future.connectFuture =
        parentFactory.getAsynchronousConnection(future, null);
    return future;
  }
}
