package org.opends.sdk.ldap;

import org.opends.sdk.*;
import org.opends.sdk.responses.Result;
import org.opends.sdk.responses.BindResult;
import org.opends.sdk.responses.CompareResult;
import org.opends.sdk.requests.*;

import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Nov 25, 2009 Time: 11:12:29
 * AM To change this template use File | Settings | File Templates.
 */
public class ConnectionPool
    extends AbstractConnectionFactory<AsynchronousConnection>
{
  private final ConnectionFactory<?> connectionFactory;
  private volatile int numConnections;
  private final int poolSize;
  private final ArrayBlockingQueue<AsynchronousConnection> pool;
  private final ConcurrentLinkedQueue<PendingConnectionFuture> pendingFutures;
  private final Object lock = new Object();

  private class PooledConnectionWapper implements AsynchronousConnection
  {
    private AsynchronousConnection connection;

    private PooledConnectionWapper(AsynchronousConnection connection)
    {
      this.connection = connection;
    }

    public void abandon(AbandonRequest request)
        throws UnsupportedOperationException, IllegalStateException,
               NullPointerException
    {
      if(connection == null)
      {
        throw new IllegalStateException();
      }
      connection.abandon(request);
    }

    public <P> ResultFuture<Result> add(AddRequest request,
                                        ResultHandler<Result, P> handler, P p)
        throws UnsupportedOperationException, IllegalStateException,
               NullPointerException
    {
      if(connection == null)
      {
        throw new IllegalStateException();
      }
      return connection.add(request, handler, p);
    }

    public <P> ResultFuture<BindResult> bind(
        BindRequest request, ResultHandler<? super BindResult, P> handler, P p)
        throws UnsupportedOperationException, IllegalStateException,
               NullPointerException
    {
      if(connection == null)
      {
        throw new IllegalStateException();
      }
      return connection.bind(request, handler, p);
    }

    public void close()
    {
      synchronized(lock)
      {
        // See if there waiters pending
        PendingConnectionFuture future = pendingFutures.poll();
        if(future != null)
        {
          PooledConnectionWapper pooledConnection =
              new PooledConnectionWapper(connection);
          future.connection(pooledConnection);
          return;
        }

        // No waiters. Put back in pool.
        pool.add(connection);
      }

    }

    public void close(UnbindRequest request) throws NullPointerException
    {
      close();
    }

    public <P> ResultFuture<CompareResult> compare(
        CompareRequest request, ResultHandler<? super CompareResult, P> handler,
        P p) throws UnsupportedOperationException, IllegalStateException,
                    NullPointerException
    {
      if(connection == null)
      {
        throw new IllegalStateException();
      }
      return connection.compare(request, handler, p);
    }

    public <P> ResultFuture<Result> delete(
        DeleteRequest request, ResultHandler<Result, P> handler, P p)
        throws UnsupportedOperationException, IllegalStateException,
               NullPointerException
    {
      if(connection == null)
      {
        throw new IllegalStateException();
      }
      return delete(request, handler, p);
    }

    public <R extends Result, P> ResultFuture<R> extendedRequest(
        ExtendedRequest<R> request, ResultHandler<? super R, P> handler, P p)
        throws UnsupportedOperationException, IllegalStateException,
               NullPointerException
    {
      if(connection == null)
      {
        throw new IllegalStateException();
      }
      return connection.extendedRequest(request, handler, p);
    }

    public <P> ResultFuture<Result> modify(
        ModifyRequest request, ResultHandler<Result, P> handler, P p)
        throws UnsupportedOperationException, IllegalStateException,
               NullPointerException
    {
      if(connection == null)
      {
        throw new IllegalStateException();
      }
      return connection.modify(request, handler, p);
    }

    public <P> ResultFuture<Result> modifyDN(
        ModifyDNRequest request, ResultHandler<Result, P> handler, P p)
        throws UnsupportedOperationException, IllegalStateException,
               NullPointerException
    {
      if(connection == null)
      {
        throw new IllegalStateException();
      }
      return connection.modifyDN(request, handler, p);
    }

    public <P> ResultFuture<Result> search(
        SearchRequest request, ResultHandler<Result, P> resultHandler,
        SearchResultHandler<P> searchResulthandler, P p)
        throws UnsupportedOperationException, IllegalStateException,
               NullPointerException
    {
      if(connection == null)
      {
        throw new IllegalStateException();
      }
      return connection.search(request, resultHandler, searchResulthandler, p);
    }

    public void addConnectionEventListener(ConnectionEventListener listener)
        throws IllegalStateException, NullPointerException
    {
      if(connection == null)
      {
        throw new IllegalStateException();
      }
    }

    public void removeConnectionEventListener(ConnectionEventListener listener)
        throws NullPointerException
    {
      if(connection == null)
      {
        throw new IllegalStateException();
      }
    }
  }

  public class CompletedConnectionFuture implements ConnectionFuture<AsynchronousConnection>
  {
    private final PooledConnectionWapper connection;

    public CompletedConnectionFuture(PooledConnectionWapper connection)
    {
      this.connection = connection;
    }

    public boolean cancel(boolean mayInterruptIfRunning)
    {
      return false;
    }

    public AsynchronousConnection get()
        throws InterruptedException, ErrorResultException
    {
      return connection;
    }

    public AsynchronousConnection get(long timeout, TimeUnit unit)
        throws InterruptedException, TimeoutException, ErrorResultException
    {
      return connection;
    }

    public boolean isCancelled()
    {
      return false;
    }

    public boolean isDone()
    {
      return true;
    }
  }

  public class PendingConnectionFuture<P>
      implements ConnectionFuture<AsynchronousConnection>
  {
    private volatile boolean isCancelled;
    private volatile PooledConnectionWapper connection;
    private volatile ErrorResultException err;
    private final ConnectionResultHandler<? super AsynchronousConnection, P> handler;
    private final P p;
    private final CountDownLatch latch = new CountDownLatch(1);

    public PendingConnectionFuture()
    {
      this.handler = null;
      this.p = null;
    }

    public PendingConnectionFuture(
        P p, ConnectionResultHandler<? super AsynchronousConnection, P> handler)
    {
      this.handler = handler;
      this.p = p;
    }

    public synchronized boolean cancel(boolean mayInterruptIfRunning)
    {
      if(pendingFutures.remove(this))
      {
        return (isCancelled = true);
      }
      return false;
    }

    public AsynchronousConnection get() throws InterruptedException, ErrorResultException
    {
      latch.await();
      if(err != null)
      {
        throw err;
      }
      return connection;
    }

    public AsynchronousConnection get(long timeout, TimeUnit unit)
        throws InterruptedException, TimeoutException, ErrorResultException
    {
      latch.await(timeout, unit);
      if(err != null)
      {
        throw err;
      }
      return connection;
    }

    public synchronized boolean isCancelled()
    {
      return isCancelled;
    }

    public boolean isDone()
    {
      return latch.getCount() == 0;
    }

    private void connection(PooledConnectionWapper connection)
    {
      this.connection = connection;
      if(handler != null)
      {
        handler.handleConnection(p, connection);
      }
      latch.countDown();
    }

    private void error(ErrorResultException e)
    {
      this.err = e;
      if(handler != null)
      {
        handler.handleConnectionError(p, e);
      }
      latch.countDown();
    }
  }

  public ConnectionPool(ConnectionFactory<?> connectionFactory, int poolSize)
  {
    this.connectionFactory = connectionFactory;
    this.poolSize = poolSize;
    this.pool = new ArrayBlockingQueue<AsynchronousConnection>(poolSize);
    this.pendingFutures = new ConcurrentLinkedQueue<PendingConnectionFuture>();
  }

  private class WrapConnectionResultHandler
    implements ConnectionResultHandler<AsynchronousConnection, Void>
  {
    private final PendingConnectionFuture future;

    private WrapConnectionResultHandler(PendingConnectionFuture future)
    {
      this.future = future;
    }

    public void handleConnection(java.lang.Void p,
                                 AsynchronousConnection connection)
    {
      PooledConnectionWapper pooledConnection =
          new PooledConnectionWapper(connection);
      future.connection(pooledConnection);
    }

    public void handleConnectionError(java.lang.Void p,
                                      ErrorResultException error)
    {
      future.error(error);
    }
  }

  public <P> ConnectionFuture<AsynchronousConnection> getAsynchronousConnection(
      ConnectionResultHandler<? super AsynchronousConnection, P> handler,
      P p)
  {
        synchronized(lock)
    {
      // Check to see if we have a connection in the pool
      AsynchronousConnection conn = pool.poll();

      if(conn != null)
      {
        PooledConnectionWapper pooledConnection =
            new PooledConnectionWapper(conn);
        if(handler != null)
        {
          handler.handleConnection(p, pooledConnection);
        }
        return new CompletedConnectionFuture(pooledConnection);
      }

      PendingConnectionFuture<P> pendingFuture =
          new PendingConnectionFuture<P>(p, handler);
      // Pool was empty. Maybe a new connection if pool size is not reached
      if(numConnections < poolSize)
      {
        numConnections++;
        WrapConnectionResultHandler wrapHandler =
            new WrapConnectionResultHandler(pendingFuture);
        connectionFactory.getAsynchronousConnection(wrapHandler, null);
      }
      else
      {
        // Have to wait
        pendingFutures.add(pendingFuture);
      }

      return pendingFuture;
    }
  }
}
