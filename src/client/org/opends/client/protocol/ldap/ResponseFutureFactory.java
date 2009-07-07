package org.opends.client.protocol.ldap;

import org.opends.common.api.response.*;
import org.opends.common.api.request.*;
import org.opends.common.api.extended.ExtendedRequest;
import org.opends.common.api.extended.ExtendedResponse;
import org.opends.common.api.extended.ExtendedOperation;
import org.opends.client.api.futures.*;
import org.opends.client.api.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CancellationException;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 7, 2009
 * Time: 2:30:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class ResponseFutureFactory
{
  static class Add
      extends AbstractResponseFuture<AddRequest, AddResponse>
      implements AddResponseFuture
  {
    private Add(int messageID, AddRequest request,
                    ResponseHandler<AddResponse> addResponseHandler,
                    LDAPConnection connection)
    {
      super(messageID, request, addResponseHandler, connection);
    }

    public AddResponse get()
        throws InterruptedException, AddRequestException
    {
      latch.await();

      if(failure != null)
      {
        throw new AddRequestException(failure);
      }
      if(isCancelled)
      {
        throw new CancellationException();
      }

      return result;
    }

    public AddResponse get(long timeout, TimeUnit unit)
        throws InterruptedException, TimeoutException,
        AddRequestException
    {
      if(!latch.await(timeout, unit))
      {
        throw new TimeoutException();
      }
      if(failure != null)
      {
        throw new AddRequestException(failure);
      }
      if(isCancelled)
      {
        throw new CancellationException();
      }

      return result;
    }
  }

  static class Bind
      extends AbstractResponseFuture<BindRequest, BindResponse>
      implements BindResponseFuture
  {
    private Bind(int messageID, BindRequest request,
                    ResponseHandler<BindResponse> bindResponseHandler,
                    LDAPConnection connection)
    {
      super(messageID, request, bindResponseHandler, connection);
    }

    public BindResponse get()
        throws InterruptedException, BindRequestException
    {
      latch.await();

      if(failure != null)
      {
        throw new BindRequestException(failure);
      }
      if(isCancelled)
      {
        throw new CancellationException();
      }

      return result;
    }

    public BindResponse get(long timeout, TimeUnit unit)
        throws InterruptedException, TimeoutException,
        BindRequestException
    {
      if(!latch.await(timeout, unit))
      {
        throw new TimeoutException();
      }
      if(failure != null)
      {
        throw new BindRequestException(failure);
      }
      if(isCancelled)
      {
        throw new CancellationException();
      }

      return result;
    }
  }

  static class Compare
      extends AbstractResponseFuture<CompareRequest, CompareResponse>
      implements CompareResponseFuture
  {
    private Compare(int messageID, CompareRequest request,
                    ResponseHandler<CompareResponse> compareResponseHandler,
                    LDAPConnection connection)
    {
      super(messageID, request, compareResponseHandler, connection);
    }

    public CompareResponse get()
        throws InterruptedException, CompareRequestException
    {
      latch.await();

      if(failure != null)
      {
        throw new CompareRequestException(failure);
      }
      if(isCancelled)
      {
        throw new CancellationException();
      }

      return result;
    }

    public CompareResponse get(long timeout, TimeUnit unit)
        throws InterruptedException, TimeoutException,
        CompareRequestException
    {
      if(!latch.await(timeout, unit))
      {
        throw new TimeoutException();
      }
      if(failure != null)
      {
        throw new CompareRequestException(failure);
      }
      if(isCancelled)
      {
        throw new CancellationException();
      }

      return result;
    }
  }

  static class Delete
      extends AbstractResponseFuture<DeleteRequest, DeleteResponse>
      implements DeleteResponseFuture
  {
    private Delete(int messageID, DeleteRequest request,
                    ResponseHandler<DeleteResponse> responseHandler,
                    LDAPConnection connection)
    {
      super(messageID, request, responseHandler, connection);
    }

    public DeleteResponse get()
        throws InterruptedException, DeleteRequestException
    {
      latch.await();

      if(failure != null)
      {
        throw new DeleteRequestException(failure);
      }
      if(isCancelled)
      {
        throw new CancellationException();
      }

      return result;
    }

    public DeleteResponse get(long timeout, TimeUnit unit)
        throws InterruptedException, TimeoutException,
        DeleteRequestException
    {
      if(!latch.await(timeout, unit))
      {
        throw new TimeoutException();
      }
      if(failure != null)
      {
        throw new DeleteRequestException(failure);
      }
      if(isCancelled)
      {
        throw new CancellationException();
      }

      return result;
    }
  }

    static class ModifyDN
      extends AbstractResponseFuture<ModifyDNRequest, ModifyDNResponse>
      implements ModifyDNResponseFuture
  {
    private ModifyDN(int messageID, ModifyDNRequest request,
                    ResponseHandler<ModifyDNResponse> responseHandler,
                    LDAPConnection connection)
    {
      super(messageID, request, responseHandler, connection);
    }

    public ModifyDNResponse get()
        throws InterruptedException, ModifyDNRequestException
    {
      latch.await();

      if(failure != null)
      {
        throw new ModifyDNRequestException(failure);
      }
      if(isCancelled)
      {
        throw new CancellationException();
      }

      return result;
    }

    public ModifyDNResponse get(long timeout, TimeUnit unit)
        throws InterruptedException, TimeoutException,
        ModifyDNRequestException
    {
      if(!latch.await(timeout, unit))
      {
        throw new TimeoutException();
      }
      if(failure != null)
      {
        throw new ModifyDNRequestException(failure);
      }
      if(isCancelled)
      {
        throw new CancellationException();
      }

      return result;
    }
  }

  static class Modify
      extends AbstractResponseFuture<ModifyRequest, ModifyResponse>
      implements ModifyResponseFuture
  {
    private Modify(int messageID, ModifyRequest request,
                    ResponseHandler<ModifyResponse> responseHandler,
                    LDAPConnection connection)
    {
      super(messageID, request, responseHandler, connection);
    }

    public ModifyResponse get()
        throws InterruptedException, ModifyRequestException
    {
      latch.await();

      if(failure != null)
      {
        throw new ModifyRequestException(failure);
      }
      if(isCancelled)
      {
        throw new CancellationException();
      }

      return result;
    }

    public ModifyResponse get(long timeout, TimeUnit unit)
        throws InterruptedException, TimeoutException,
        ModifyRequestException
    {
      if(!latch.await(timeout, unit))
      {
        throw new TimeoutException();
      }
      if(failure != null)
      {
        throw new ModifyRequestException(failure);
      }
      if(isCancelled)
      {
        throw new CancellationException();
      }

      return result;
    }
  }
}

