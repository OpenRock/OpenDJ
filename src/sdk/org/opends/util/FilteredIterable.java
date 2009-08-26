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
 * A filtered view of an {@link Iterable} containing only values which
 * match the provided {@link Predicate}. Filtered iterables support
 * element removal via the {@link Iterator#remove()} method subject to
 * any constraints imposed by the underlying iterable.
 *
 * @param <T>
 *          The type of elements contained in the underlying iterable.
 * @param <P>
 *          The type of the additional parameter to the predicate's
 *          {@code matches} method. Use {@link java.lang.Void} for
 *          predicates that do not need an additional parameter.
 */
public final class FilteredIterable<T, P> implements Iterable<T>
{

  private final Iterable<T> iterable;
  private final Predicate<? super T, P> predicate;
  private final P parameter;



  private final class IteratorImpl implements Iterator<T>
  {
    private final Iterator<T> iterator;
    private T next = null;
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
          if (predicate.matches(parameter, next))
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
    public T next()
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



  /**
   * Creates a filtered view of the provided iterable which will only
   * contain those elements matching the provided predicate.
   *
   * @param iterable
   *          The iterable to be filtered.
   * @param predicate
   *          The predicate.
   * @param p
   *          A predicate specified parameter.
   */
  public FilteredIterable(Iterable<T> iterable,
      Predicate<? super T, P> predicate, P p)
  {
    this.iterable = iterable;
    this.predicate = predicate;
    this.parameter = p;
  }



  /**
   * {@inheritDoc}
   */
  public Iterator<T> iterator()
  {
    return new IteratorImpl();
  }

}
