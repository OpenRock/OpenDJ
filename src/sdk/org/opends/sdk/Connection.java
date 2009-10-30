/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE
 * or https://OpenDS.dev.java.net/OpenDS.LICENSE.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE.  If applicable,
 * add the following below this CDDL HEADER, with the fields enclosed
 * by brackets "[]" replaced with your own identifying information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Copyright 2009 Sun Microsystems, Inc.
 */

package org.opends.sdk;



import java.io.Closeable;

import org.opends.sdk.requests.AbandonRequest;
import org.opends.sdk.requests.AddRequest;
import org.opends.sdk.requests.BindRequest;
import org.opends.sdk.requests.CompareRequest;
import org.opends.sdk.requests.DeleteRequest;
import org.opends.sdk.requests.ExtendedRequest;
import org.opends.sdk.requests.ModifyDNRequest;
import org.opends.sdk.requests.ModifyRequest;
import org.opends.sdk.requests.SearchRequest;
import org.opends.sdk.requests.UnbindRequest;
import org.opends.sdk.responses.BindResult;
import org.opends.sdk.responses.BindResultFuture;
import org.opends.sdk.responses.CompareResult;
import org.opends.sdk.responses.CompareResultFuture;
import org.opends.sdk.responses.ExtendedResultFuture;
import org.opends.sdk.responses.GenericExtendedResult;
import org.opends.sdk.responses.Result;
import org.opends.sdk.responses.ResultFuture;
import org.opends.sdk.responses.ResultHandler;
import org.opends.sdk.responses.SearchResultEntry;
import org.opends.sdk.responses.SearchResultFuture;
import org.opends.sdk.responses.SearchResultHandler;
import org.opends.sdk.util.ByteString;



/**
 * A connection with a Directory Server over which read and update
 * operations may be performed. See RFC 4511 for the LDAPv3 protocol
 * specification and more information about the types of operations
 * defined in LDAP.
 * <p>
 * <h3>Operation processing</h3>
 * <p>
 * All operations are performed asynchronously and return a
 * {@link ResultFuture} or sub-type thereof which can be used for
 * retrieving the result using the {@link ResultFuture#get} method.
 * Operation failures, for whatever reason, are signalled by the
 * {@link ResultFuture#get()} method throwing an
 * {@link ErrorResultException}.
 * <p>
 * Synchronous operations are easily simulated by immediately getting
 * the result:
 *
 * <pre>
 * Connection connection = ...;
 * AddRequest request = ...;
 * // Will block until operation completes, and
 * // throws exception on failure.
 * connection.add(request).get();
 * </pre>
 *
 * Operations can be performed in parallel whilst taking advantage of
 * the simplicity of a synchronous application design:
 *
 * <pre>
 * Connection connection1 = ...;
 * Connection connection2 = ...;
 * AddRequest request = ...;
 * // Add the entry to the first server (don't block).
 * ResultFuture future1 = connection1.add(request);
 * // Add the entry to the second server (in parallel).
 * ResultFuture future2 = connection2.add(request);
 * // Total time = is O(1) instead of O(n).
 * future1.get();
 * future2.get();
 * </pre>
 *
 * More complex client applications can take advantage of a fully
 * asynchronous event driven design using {@link ResultHandler}s:
 *
 * <pre>
 * Connection connection = ...;
 * SearchRequest request = ...;
 * // Process results in the search result handler
 * // in a separate thread.
 * SearchResponseHandler handle = ...;
 * connection.search(request, handler);
 * </pre>
 * <p>
 * <h3>Closing connections</h3>
 * <p>
 * Applications must ensure that a connection is closed by calling
 * {@link #close()} even if a fatal error occurs on the connection. Once
 * a connection has been closed by the client application, any attempts
 * to continue to use the connection will result in an
 * {@link IllegalStateException} being thrown. Note that, if a fatal
 * error is encountered on the connection, then the application can
 * continue to use the connection. In this case all requests subsequent
 * to the failure will fail with an appropriate
 * {@link ErrorResultException} when their result is retrieved.
 * <p>
 * <h3>Event notification</h3>
 * <p>
 * Applications can choose to be notified when a connection is closed by
 * the application, receives an unsolicited notification, or experiences
 * a fatal error by registering a {@link ConnectionEventListener} with
 * the connection using the {@link #addConnectionEventListener} method.
 * <p>
 * <h3>TO DO</h3>
 * <p>
 * <ul>
 * <li>do we need isClosed() and isValid()?
 * <li>do we need connection event notification of client close? JDBC
 * and JCA have this functionality in their pooled (managed) connection
 * APIs. We need some form of event notification at the app level for
 * unsolicited notifications.
 * <li>method for adding an entry.
 * <li>method for performing update operation (e.g. LDIF change
 * records).
 * <li>simple search API with blocking entry iterator and easy access to
 * first entry.
 * <li>should unsupported methods throw UnsupportedOperationException or
 * throw an ErrorResultException using an UnwillingToPerform result code
 * (or something similar)?
 * </ul>
 *
 * @see <a href="http://tools.ietf.org/html/rfc4511">RFC 4511 -
 *      Lightweight Directory Access Protocol (LDAP): The Protocol </a>
 */
public interface Connection extends Closeable
{

  /**
   * Abandons the unfinished operation identified in the provided
   * abandon request.
   * <p>
   * <b>Note:</b> a more convenient approach to abandoning unfinished
   * operations is provided via the {@link ResultFuture#cancel(boolean)}
   * method.
   *
   * @param request
   *          The request identifying the operation to be abandoned.
   * @throws UnsupportedOperationException
   *           If this connection does not support abandon operations.
   * @throws IllegalStateException
   *           If this connection has already been closed, i.e. if
   *           {@code isClosed() == true}.
   * @throws NullPointerException
   *           If {@code request} was {@code null}.
   */
  void abandon(AbandonRequest request)
      throws UnsupportedOperationException, IllegalStateException,
      NullPointerException;



  /**
   * Abandons the unfinished operation identified by the provided
   * message ID.
   * <p>
   * <b>Note:</b> a more convenient approach to abandoning unfinished
   * operations is provided via the {@link ResultFuture#cancel(boolean)}
   * method.
   * <p>
   * This method is semantically equivalent to the following code:
   *
   * <pre>
   * AbandonRequest request = Requests.newAbandonRequest(messageID);
   * connection.abandon(request);
   * </pre>
   *
   * @param messageID
   *          The message ID of the request to be abandoned.
   * @throws UnsupportedOperationException
   *           If this connection does not support abandon operations.
   * @throws IllegalStateException
   *           If this connection has already been closed, i.e. if
   *           {@code isClosed() == true}.
   */
  void abandon(int messageID) throws UnsupportedOperationException,
      IllegalStateException;



  /**
   * Adds an entry to the Directory Server using the provided add
   * request.
   *
   * @param request
   *          The add request.
   * @param handler
   *          A result handler which can be used to asynchronously
   *          process the operation result when it is received, may be
   *          {@code null}.
   * @return A future representing the result of the operation.
   * @throws UnsupportedOperationException
   *           If this connection does not support add operations.
   * @throws IllegalStateException
   *           If this connection has already been closed, i.e. if
   *           {@code isClosed() == true}.
   * @throws NullPointerException
   *           If {@code request} was {@code null}.
   */
  ResultFuture add(AddRequest request, ResultHandler<Result> handler)
      throws UnsupportedOperationException, IllegalStateException,
      NullPointerException;



  /**
   * Adds the provided entry to the Directory Server.
   * <p>
   * This method is semantically equivalent to the following code:
   *
   * <pre>
   * AddRequest request = Requests.newAddRequest(name, ldifAttributes);
   * connection.add(request, null);
   * </pre>
   *
   * @param name
   *          The distringuished name of the entry to be added.
   * @param ldifAttributes
   *          Lines of LDIF containing the attributes of the entry to be
   *          added.
   * @return A future representing the result of the operation.
   * @throws UnsupportedOperationException
   *           If this connection does not support add operations.
   * @throws IllegalArgumentException
   *           If {@code ldifAttributes} was empty or contained invalid
   *           LDIF.
   * @throws IllegalStateException
   *           If this connection has already been closed, i.e. if
   *           {@code isClosed() == true}.
   * @throws NullPointerException
   *           If {@code name} or {@code ldifAtttributes} was {@code
   *           null} .
   */
  ResultFuture add(String name, String... ldifAttributes)
      throws UnsupportedOperationException, IllegalArgumentException,
      IllegalStateException, NullPointerException;



  /**
   * Adds the provided entry to the Directory Server.
   * <p>
   * This method is semantically equivalent to the following code:
   *
   * <pre>
   * AddRequest request = Requests.asAddRequest(entry);
   * connection.add(request, null);
   * </pre>
   *
   * @param entry
   *          The entry to be added.
   * @return A future representing the result of the operation.
   * @throws UnsupportedOperationException
   *           If this connection does not support add operations.
   * @throws IllegalStateException
   *           If this connection has already been closed, i.e. if
   *           {@code isClosed() == true}.
   * @throws NullPointerException
   *           If {@code entry} was {@code null} .
   */
  ResultFuture add(AttributeSequence entry)
      throws UnsupportedOperationException, IllegalStateException,
      NullPointerException;



  /**
   * Authenticates to the Directory Server using the provided bind
   * request.
   *
   * @param request
   *          The bind request.
   * @param handler
   *          A result handler which can be used to asynchronously
   *          process the operation result when it is received, may be
   *          {@code null}.
   * @return A future representing the result of the operation.
   * @throws UnsupportedOperationException
   *           If this connection does not support bind operations.
   * @throws IllegalStateException
   *           If this connection has already been closed, i.e. if
   *           {@code isClosed() == true}.
   * @throws NullPointerException
   *           If {@code request} was {@code null}.
   */
  BindResultFuture bind(BindRequest request,
      ResultHandler<BindResult> handler)
      throws UnsupportedOperationException, IllegalStateException,
      NullPointerException;



  /**
   * Authenticates to the Directory Server using simple authentication
   * and the provided user name and password.
   * <p>
   * This method is semantically equivalent to the following code:
   *
   * <pre>
   * BindRequest request = Requests.newSimpleBindRequest(name, password);
   * connection.bind(request, null);
   * </pre>
   *
   * @param name
   *          The distinguished name of the Directory object that the
   *          client wishes to bind as, which may be empty.
   * @param password
   *          The password of the Directory object that the client
   *          wishes to bind as, which may be empty.
   * @return A future representing the result of the operation.
   * @throws UnsupportedOperationException
   *           If this connection does not support bind operations.
   * @throws IllegalStateException
   *           If this connection has already been closed, i.e. if
   *           {@code isClosed() == true}.
   * @throws NullPointerException
   *           If {@code name} or {@code password} was {@code null}.
   */
  BindResultFuture bind(String name, String password)
      throws UnsupportedOperationException, IllegalStateException,
      NullPointerException;



  /**
   * Releases any resources associated with this connection. For
   * physical connections to a Directory Server this will mean that an
   * unbind request is sent and the underlying socket is closed.
   * <p>
   * Other connection implementations may behave differently, and may
   * choose not to send an unbind request if its use is inappropriate
   * (for example a pooled connection will be released and returned to
   * its connection pool without ever issuing an unbind request).
   * <p>
   * This method is semantically equivalent to the following code:
   *
   * <pre>
   * UnbindRequest request = Requests.newUnbindRequest();
   * connection.close(request);
   * </pre>
   *
   * Calling {@code close} on a connection that is already closed has no
   * effect.
   */
  void close();



  /**
   * Releases any resources associated with this connection. For
   * physical connections to a Directory Server this will mean that the
   * provided unbind request is sent and the underlying socket is
   * closed.
   * <p>
   * Other connection implementations may behave differently, and may
   * choose to ignore the provided unbind request if its use is
   * inappropriate (for example a pooled connection will be released and
   * returned to its connection pool without ever issuing an unbind
   * request).
   * <p>
   * Calling {@code close} on a connection that is already closed has no
   * effect.
   *
   * @param request
   *          The unbind request to use in the case where a physical
   *          connection is closed.
   * @throws NullPointerException
   *           If {@code request} was {@code null}.
   */
  void close(UnbindRequest request) throws NullPointerException;



  /**
   * Compares an entry in the Directory Server using the provided
   * compare request.
   *
   * @param request
   *          The compare request.
   * @param handler
   *          A result handler which can be used to asynchronously
   *          process the operation result when it is received, may be
   *          {@code null}.
   * @return A future representing the result of the operation.
   * @throws UnsupportedOperationException
   *           If this connection does not support compare operations.
   * @throws IllegalStateException
   *           If this connection has already been closed, i.e. if
   *           {@code isClosed() == true}.
   * @throws NullPointerException
   *           If {@code request} was {@code null}.
   */
  CompareResultFuture compare(CompareRequest request,
      ResultHandler<CompareResult> handler)
      throws UnsupportedOperationException, IllegalStateException,
      NullPointerException;



  /**
   * Compares the named entry in the Directory Server against the
   * provided attribute value assertion.
   * <p>
   * This method is semantically equivalent to the following code:
   *
   * <pre>
   * CompareRequest request =
   *     Requests.newCompareRequest(name, attributeDescription,
   *         assertionValue);
   * connection.compare(request, null);
   * </pre>
   *
   * @param name
   *          The distinguished name of the entry to be compared.
   * @param attributeDescription
   *          The name of the attribute to be compared.
   * @param assertionValue
   *          The assertion value to be compared.
   * @return A future representing the result of the operation.
   * @throws UnsupportedOperationException
   *           If this connection does not support compare operations.
   * @throws IllegalStateException
   *           If this connection has already been closed, i.e. if
   *           {@code isClosed() == true}.
   * @throws NullPointerException
   *           If {@code name}, {@code attributeDescription}, or {@code
   *           assertionValue} was {@code null}.
   */
  CompareResultFuture compare(String name, String attributeDescription,
      String assertionValue) throws UnsupportedOperationException,
      IllegalStateException, NullPointerException;



  /**
   * Deletes an entry from the Directory Server using the provided
   * delete request.
   *
   * @param request
   *          The delete request.
   * @param handler
   *          A result handler which can be used to asynchronously
   *          process the operation result when it is received, may be
   *          {@code null}.
   * @return A future representing the result of the operation.
   * @throws UnsupportedOperationException
   *           If this connection does not support delete operations.
   * @throws IllegalStateException
   *           If this connection has already been closed, i.e. if
   *           {@code isClosed() == true}.
   * @throws NullPointerException
   *           If {@code request} was {@code null}.
   */
  ResultFuture delete(DeleteRequest request,
      ResultHandler<Result> handler)
      throws UnsupportedOperationException, IllegalStateException,
      NullPointerException;



  /**
   * Deletes the named entry from the Directory Server.
   * <p>
   * This method is semantically equivalent to the following code:
   *
   * <pre>
   * DeleteRequest request = Requests.newDeleteRequest(name);
   * connection.delete(request, null);
   * </pre>
   *
   * @param name
   *          The distinguished name of the entry to be deleted.
   * @return A future representing the result of the operation.
   * @throws UnsupportedOperationException
   *           If this connection does not support delete operations.
   * @throws IllegalStateException
   *           If this connection has already been closed, i.e. if
   *           {@code isClosed() == true}.
   * @throws NullPointerException
   *           If {@code name} was {@code null}.
   */
  ResultFuture delete(String name)
      throws UnsupportedOperationException, IllegalStateException,
      NullPointerException;



  /**
   * Requests that the Directory Server performs the provided extended
   * request.
   *
   * @param <R>
   *          The type of result returned by the extended request.
   * @param request
   *          The extended request.
   * @param handler
   *          A result handler which can be used to asynchronously
   *          process the operation result when it is received, may be
   *          {@code null}.
   * @return A future representing the result of the operation.
   * @throws UnsupportedOperationException
   *           If this connection does not support extended operations.
   * @throws IllegalStateException
   *           If this connection has already been closed, i.e. if
   *           {@code isClosed() == true}.
   * @throws NullPointerException
   *           If {@code request} was {@code null}.
   */
  <R extends Result> ExtendedResultFuture<R> extendedRequest(
      ExtendedRequest<R> request, ResultHandler<R> handler)
      throws UnsupportedOperationException, IllegalStateException,
      NullPointerException;



  /**
   * Requests that the Directory Server performs the provided extended
   * request.
   * <p>
   * This method is semantically equivalent to the following code:
   *
   * <pre>
   * GenericExtendedRequest request =
   *     Requests.newGenericExtendedRequest(requestName, requestValue);
   * connection.extendedRequest(request, null);
   * </pre>
   *
   * @param requestName
   *          The dotted-decimal representation of the unique OID
   *          corresponding to the extended request.
   * @param requestValue
   *          The content of the extended request in a form defined by
   *          the extended operation, or {@code null} if there is no
   *          content.
   * @return A future representing the result of the operation.
   * @throws UnsupportedOperationException
   *           If this connection does not support extended operations.
   * @throws IllegalStateException
   *           If this connection has already been closed, i.e. if
   *           {@code isClosed() == true}.
   * @throws NullPointerException
   *           If {@code requestName} was {@code null}.
   */
  ExtendedResultFuture<GenericExtendedResult> extendedRequest(
      String requestName, ByteString requestValue)
      throws UnsupportedOperationException, IllegalStateException,
      NullPointerException;



  // /**
  // * Indicates whether or not this connection has been explicitly
  // closed
  // * by calling {@code close}. This method will not return {@code
  // true}
  // * if a fatal error has occurred on the connection unless {@code
  // * close} has been called.
  // *
  // * @return {@code true} if this connection has been explicitly
  // closed
  // * by calling {@code close}, or {@code false} otherwise.
  // */
  // boolean isClosed();
  //
  //
  //
  // /**
  // * Indicates whether or not this connection is valid. A connection
  // is
  // * not valid if the method {@code close} has been called on it or if
  // * certain fatal errors have occurred. This method is guaranteed to
  // * return {@code false} only when it is called after the method
  // * {@code close} has been called.
  // * <p>
  // * Implementations may choose to send a no-op request to the
  // * underlying Directory Server in order to determine if the
  // underlying
  // * connection is still valid.
  // *
  // * @return {@code true} if this connection is valid, or {@code
  // false}
  // * otherwise.
  // * @throws InterruptedException
  // * If the current thread was interrupted while waiting.
  // */
  // boolean isValid() throws InterruptedException;
  //
  //
  //
  // /**
  // * Indicates whether or not this connection is valid. A connection
  // is
  // * not valid if the method {@code close} has been called on it or if
  // * certain fatal errors have occurred. This method is guaranteed to
  // * return {@code false} only when it is called after the method
  // * {@code close} has been called.
  // * <p>
  // * Implementations may choose to send a no-op request to the
  // * underlying Directory Server in order to determine if the
  // underlying
  // * connection is still valid.
  // *
  // * @param timeout
  // * The maximum time to wait.
  // * @param unit
  // * The time unit of the timeout argument.
  // * @return {@code true} if this connection is valid, or {@code
  // false}
  // * otherwise.
  // * @throws InterruptedException
  // * If the current thread was interrupted while waiting.
  // * @throws TimeoutException
  // * If the wait timed out.
  // */
  // boolean isValid(long timeout, TimeUnit unit)
  // throws InterruptedException, TimeoutException;

  /**
   * Modifies an entry in the Directory Server using the provided modify
   * request.
   *
   * @param request
   *          The modify request.
   * @param handler
   *          A result handler which can be used to asynchronously
   *          process the operation result when it is received, may be
   *          {@code null}.
   * @return A future representing the result of the operation.
   * @throws UnsupportedOperationException
   *           If this connection does not support modify operations.
   * @throws IllegalStateException
   *           If this connection has already been closed, i.e. if
   *           {@code isClosed() == true}.
   * @throws NullPointerException
   *           If {@code request} was {@code null}.
   */
  ResultFuture modify(ModifyRequest request,
      ResultHandler<Result> handler)
      throws UnsupportedOperationException, IllegalStateException,
      NullPointerException;



  /**
   * Modifies the named entry in the Directory Server using the provided
   * list of changes.
   * <p>
   * This method is semantically equivalent to the following code:
   *
   * <pre>
   * ModifyRequest request = Requests.newModifyRequest(name, ldifChanges);
   * connection.modify(request, null);
   * </pre>
   *
   * @param name
   *          The distinguished name of the entry to be modified.
   * @param ldifChanges
   *          Lines of LDIF containing the changes to be made to the
   *          entry.
   * @return A future representing the result of the operation.
   * @throws UnsupportedOperationException
   *           If this connection does not support modify operations.
   * @throws IllegalArgumentException
   *           If {@code ldifChanges} was empty or contained invalid
   *           LDIF.
   * @throws IllegalStateException
   *           If this connection has already been closed, i.e. if
   *           {@code isClosed() == true}.
   * @throws NullPointerException
   *           If {@code name} or {@code ldifChanges} was {@code null} .
   */
  ResultFuture modify(String name, String... ldifChanges)
      throws UnsupportedOperationException, IllegalArgumentException,
      IllegalStateException, NullPointerException;



  /**
   * Renames an entry in the Directory Server using the provided modify
   * DN request.
   *
   * @param request
   *          The modify DN request.
   * @param handler
   *          A result handler which can be used to asynchronously
   *          process the operation result when it is received, may be
   *          {@code null}.
   * @return A future representing the result of the operation.
   * @throws UnsupportedOperationException
   *           If this connection does not support modify DN operations.
   * @throws IllegalStateException
   *           If this connection has already been closed, i.e. if
   *           {@code isClosed() == true}.
   * @throws NullPointerException
   *           If {@code request} was {@code null}.
   */
  ResultFuture modifyDN(ModifyDNRequest request,
      ResultHandler<Result> handler)
      throws UnsupportedOperationException, IllegalStateException,
      NullPointerException;



  /**
   * Renames the named entry in the Directory Server using the provided
   * new RDN.
   * <p>
   * This method is semantically equivalent to the following code:
   *
   * <pre>
   * ModifyDNRequest request = Requests.newModifyDNRequest(name, newRDN);
   * connection.modifyDN(request, null);
   * </pre>
   *
   * @param name
   *          The distinguished name of the entry to be renamed.
   * @param newRDN
   *          The new RDN of the entry.
   * @return A future representing the result of the operation.
   * @throws UnsupportedOperationException
   *           If this connection does not support modify DN operations.
   * @throws IllegalStateException
   *           If this connection has already been closed, i.e. if
   *           {@code isClosed() == true}.
   * @throws NullPointerException
   *           If {@code name} or {@code newRDN} was {@code null}.
   */
  ResultFuture modifyDN(String name, String newRDN)
      throws UnsupportedOperationException, IllegalStateException,
      NullPointerException;



  /**
   * Searches the Directory Server using the provided search request.
   *
   * @param request
   *          The search request.
   * @param handler
   *          A result handler which can be used to asynchronously
   *          process the operation result when it is received, may be
   *          {@code null}.
   * @return A future representing the result of the operation.
   * @throws UnsupportedOperationException
   *           If this connection does not support search operations.
   * @throws IllegalStateException
   *           If this connection has already been closed, i.e. if
   *           {@code isClosed() == true}.
   * @throws NullPointerException
   *           If {@code request} was {@code null}.
   */
  SearchResultFuture search(SearchRequest request,
      SearchResultHandler handler)
      throws UnsupportedOperationException, IllegalStateException,
      NullPointerException;



  /**
   * Searches the Directory Server using the provided search parameters.
   * <p>
   * This method is semantically equivalent to the following code:
   *
   * <pre>
   * SearchRequest request =
   *     Requests.newSearchRequest(baseDN, scope, filter, attributes);
   * connection.search(request, null);
   * </pre>
   *
   * @param baseObject
   *          The distinguished name of the base entry relative to which
   *          the search is to be performed.
   * @param scope
   *          The scope of the search.
   * @param filter
   *          The filter that defines the conditions that must be
   *          fulfilled in order for an entry to be returned.
   * @param attributeDescriptions
   *          The names of the attributes to be included with each
   *          entry.
   * @return A future representing the result of the operation.
   * @throws UnsupportedOperationException
   *           If this connection does not support search operations.
   * @throws IllegalArgumentException
   *           If {@code filter} is not a valid LDAP string
   *           representation of a filter.
   * @throws IllegalStateException
   *           If this connection has already been closed, i.e. if
   *           {@code isClosed() == true}.
   * @throws NullPointerException
   *           If the {@code baseObject}, {@code scope}, or {@code
   *           filter} were {@code null}.
   */
  SearchResultFuture search(String baseObject, SearchScope scope,
      String filter, String... attributeDescriptions)
      throws UnsupportedOperationException, IllegalArgumentException,
      IllegalStateException, NullPointerException;

  SearchResultEntry get(String dn, String... attributes)
      throws IllegalArgumentException, IllegalStateException,
      NullPointerException, ErrorResultException, InterruptedException;



  /**
   * Registers the provided connection event listener so that it will be
   * notified when this connection is closed by the application,
   * receives an unsolicited notification, or experiences a fatal error.
   *
   * @param listener
   *          The listener which wants to be notified when events occur
   *          on this connection.
   * @throws IllegalStateException
   *           If this connection has already been closed, i.e. if
   *           {@code isClosed() == true}.
   * @throws NullPointerException
   *           If the {@code listener} was {@code null}.
   */
  void addConnectionEventListener(ConnectionEventListener listener)
      throws IllegalStateException, NullPointerException;



  /**
   * Removes the provided connection event listener from this connection
   * so that it will no longer be notified when this connection is
   * closed by the application, receives an unsolicited notification, or
   * experiences a fatal error.
   *
   * @param listener
   *          The listener which no longer wants to be notified when
   *          events occur on this connection.
   * @throws NullPointerException
   *           If the {@code listener} was {@code null}.
   */
  void removeConnectionEventListener(ConnectionEventListener listener)
      throws NullPointerException;
}
