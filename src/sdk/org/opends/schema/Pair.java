package org.opends.schema;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.opends.server.util.Validator;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 30, 2009
 * Time: 1:52:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class Pair<K, V>
{
  private K key;
  private V value;

  private Pair(K key)
  {
    this.key = key;
  }

  public void setValue(V value)
  {
    Validator.ensureNotNull(value);
    this.value = value;
  }

  public V getValue()
  {
    return value;
  }

  public K getKey()
  {
    return key;
  }

  @Override
  public String toString() {
    return key.toString();
  }

  @Override
  public int hashCode() {
    if(key != null)
    {
      return key.hashCode();
    }
    return super.hashCode();
  }


  public static <K, V> Set<Pair<K, V>> createPairs(Set<K> keys)
  {
    if(keys == null || keys.isEmpty())
    {
      return Collections.emptySet();
    }

    Set<Pair<K, V>> pairs = new HashSet<Pair<K, V>>(keys.size());
    for(K string : keys)
    {
      pairs.add(new Pair<K, V>(string));
    }

    return pairs;
  }

  public static <K, V> Pair<K, V> createPair(K key)
  {
    return new Pair<K, V>(key);
  }

  public static <K, V> Iterator<V> valueIterator(
      final Collection<Pair<K, V>> pairs)
  {
    return new Iterator<V>() {
      private Iterator<Pair<K, V>> i = pairs.iterator();
      public boolean hasNext() {
        return i.hasNext();
      }
      public V next() {
        return i.next().getValue();
      }
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  public static <K, V> Iterator<K> keyIterator(
      final Collection<Pair<K, V>> pairs)
  {
    return new Iterator<K>() {
      private Iterator<Pair<K, V>> i = pairs.iterator();
      public boolean hasNext() {
        return i.hasNext();
      }
      public K next() {
        return i.next().getKey();
      }
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }
}
