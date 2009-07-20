package org.opends.ldap.impl;



import java.util.concurrent.ExecutorService;

import org.opends.ldap.ResponseHandler;
import org.opends.ldap.requests.ExtendedRequest;
import org.opends.ldap.responses.ExtendedResult;
import org.opends.ldap.responses.ExtendedResultFuture;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jul 8, 2009 Time: 2:12:46
 * PM To change this template use File | Settings | File Templates.
 */
public final class ExtendedResultFutureImpl<R extends ExtendedResult<R>>
    extends ResultFutureImpl<ExtendedRequest<?, R>, R> implements
    ExtendedResultFuture<R>
{

  public ExtendedResultFutureImpl(int messageID,
      ExtendedRequest<?, R> request, ResponseHandler<R> handler,
      LDAPConnection connection, ExecutorService handlerExecutor)
  {
    super(messageID, request, handler, connection, handlerExecutor);
  }
}
