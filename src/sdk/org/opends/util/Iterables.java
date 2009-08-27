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

package org.opends.util;



import java.util.Iterator;
import java.util.NoSuchElementException;



/**
 * Utility methods for manipulating {@link Iterable}s.
 */
public final class Iterables
{
  // Prevent instantiation
  private Iterables()
  {
    // Do nothing.
  }



  private static final class FilteredIterable<M, P> implements
      Iterable<M>
  {

    private final Iterable<M> iterable;
    private final Predicate<? super M, P> predicate;
    private final P parameter;



    private final class IteratorImpl implements Iterator<M>
    {
      private final Iterator<M> iterator;
      private M next = null;
      private boolean hasNextMustIterate = true;



      private IteratorImpl()
      {
        this.iterator = iterable.iterator();
      }



      /**
       * {@inheritDoc}
       */
      public boolean hasNext()
      {
        if (hasNextMustIterate)
        {
          hasNextMustIterate = false;
          while (iterator.hasNext())
          {
            next = iterator.next();
            if (predicate.matches(next, parameter))
            {
              return true;
            }
          }
          next = null;
          return false;
        }
        else
        {
          return next != null;
        }
      }



      /**
       * {@inheritDoc}
       */
      public M next()
      {
        if (!hasNext())
        {
          throw new NoSuchElementException();
        }
        hasNextMustIterate = true;
        return next;
      }



      /**
       * {@inheritDoc}
       */
      public void remove()
      {
        iterator.remove();
      }

    }



    // Constructed via factory methods.
    private FilteredIterable(Iterable<M> iterable,
        Predicate<? super M, P> predicate, P p)
    {
      this.iterable = iterable;
      this.predicate = predicate;
      this.parameter = p;
    }



    /**
     * {@inheritDoc}
     */
    public Iterator<M> iterator()
    {
      return new IteratorImpl();
    }

  }



  private static final class TransformedIterable<M, N, P> implements
      Iterable<N>
  {

    private final Iterable<M> iterable;
    private final Function<? super M, ? extends N, P> function;
    private final P parameter;



    private final class IteratorImpl implements Iterator<N>
    {
      private final Iterator<M> iterator;



      private IteratorImpl()
      {
        this.iterator = iterable.iterator();
      }



      /**
       * {@inheritDoc}
       */
      public boolean hasNext()
      {
        return iterator.hasNext();
      }



      /**
       * {@inheritDoc}
       */
      public N next()
      {
        return function.apply(iterator.next(), parameter);
      }



      /**
       * {@inheritDoc}
       */
      public void remove()
      {
        iterator.remove();
      }

    }



    // Constructed via factory methods.
    private TransformedIterable(Iterable<M> iterable,
        Function<? super M, ? extends N, P> function, P p)
    {
      this.iterable = iterable;
      this.function = function;
      this.parameter = p;
    }



    /**
     * {@inheritDoc}
     */
    public Iterator<N> iterator()
    {
      return new IteratorImpl();
    }

  }



  /**
   * Returns a filtered view of {@code iterable} containing only those
   * elements which match {@code predicate}. The returned iterable's
   * iterator supports element removal via the {@code remove()} method
   * subject to any constraints imposed by {@code iterable}.
   *
   * @param <M>
   *          The type of elements contained in {@code iterable}.
   * @param <P>
   *          The type of the additional parameter to the predicate's
   *          {@code matches} method. Use {@link java.lang.Void} for
   *          predicates that do not need an additional parameter.
   * @param iterable
   *          The iterable to be filtered.
   * @param predicate
   *          The predicate.
   * @param p
   *          A predicate specified parameter.
   * @return A filtered view of {@code iterable} containing only those
   *         elements which match {@code predicate}.
   */
  public static <M, P> Iterable<M> filter(Iterable<M> iterable,
      Predicate<? super M, P> predicate, P p)
  {
    return new FilteredIterable<M, P>(iterable, predicate, p);
  }



  /**
   * Returns a filtered view of {@code iterable} containing only those
   * elements which match {@code predicate}. The returned iterable's
   * iterator supports element removal via the {@code remove()} method
   * subject to any constraints imposed by {@code iterable}.
   *
   * @param <M>
   *          The type of elements contained in {@code iterable}.
   * @param iterable
   *          The iterable to be filtered.
   * @param predicate
   *          The predicate.
   * @return A filtered view of {@code iterable} containing only those
   *         elements which match {@code predicate}.
   */
  public static <M> Iterable<M> filter(Iterable<M> iterable,
      Predicate<? super M, Void> predicate)
  {
    return new FilteredIterable<M, Void>(iterable, predicate, null);
  }



  /**
   * Returns a view of {@code iterable} whose values have been mapped to
   * elements of type {@code N} using {@code function}. The returned
   * iterable's iterator supports element removal via the {@code
   * remove()} method subject to any constraints imposed by {@code
   * iterable}.
   *
   * @param <M>
   *          The type of elements contained in {@code iterable}.
   * @param <N>
   *          The type of elements contained in the returned iterable.
   * @param <P>
   *          The type of the additional parameter to the function's
   *          {@code apply} method. Use {@link java.lang.Void} for
   *          functions that do not need an additional parameter.
   * @param iterable
   *          The iterable to be transformed.
   * @param function
   *          The function.
   * @param p
   *          A predicate specified parameter.
   * @return A view of {@code iterable} whose values have been mapped to
   *         elements of type {@code N} using {@code function}.
   */
  public static <M, N, P> Iterable<N> transform(Iterable<M> iterable,
      Function<? super M, ? extends N, P> function, P p)
  {
    return new TransformedIterable<M, N, P>(iterable, function, p);
  }



  /**
   * Returns a view of {@code iterable} whose values have been mapped to
   * elements of type {@code N} using {@code function}. The returned
   * iterable's iterator supports element removal via the {@code
   * remove()} method subject to any constraints imposed by {@code
   * iterable}.
   *
   * @param <M>
   *          The type of elements contained in {@code iterable}.
   * @param <N>
   *          The type of elements contained in the returned iterable.
   * @param iterable
   *          The iterable to be transformed.
   * @param function
   *          The function.
   * @return A view of {@code iterable} whose values have been mapped to
   *         elements of type {@code N} using {@code function}.
   */
  public static <M, N> Iterable<N> transform(Iterable<M> iterable,
      Function<? super M, ? extends N, Void> function)
  {
    return new TransformedIterable<M, N, Void>(iterable, function, null);
  }

}
