package org.opends.sdk.tools;

import org.opends.sdk.util.Validator;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.io.*;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Nov 2, 2009 Time: 12:08:01
 * PM To change this template use File | Settings | File Templates.
 */
public final class DataSource
{
  private static interface IDataSource
  {
    public Object getData();
    public IDataSource duplicate();
  }

  private static class RandomNumberDataSource implements IDataSource
  {
    private final Random random;
    private final int offset;
    private final int range;

    public RandomNumberDataSource(long seed, int low, int high)
    {
      random = new Random(seed);
      offset = low;
      range = high - low;
    }

    public Object getData()
    {
      return random.nextInt(range) + offset;
    }

    public IDataSource duplicate()
    {
      // There is no state info so threads can just share one instance.
      return this;
    }
  }

  private static class IncrementNumberDataSource implements IDataSource
  {
    private final int low;
    private int next;
    private final int high;

    public IncrementNumberDataSource(int low, int high)
    {
      this.low = this.next = low;
      this.high = high;
    }

    public Object getData()
    {
      if(next == high)
      {
        next = low;
        return high;
      }

      return next++;
    }

    public IDataSource duplicate()
    {
      return new IncrementNumberDataSource(low, high);
    }
  }

  private static class RandomLineFileDataSource implements IDataSource
  {
    private final List<String> lines;
    private final Random random;

    public RandomLineFileDataSource(long seed, String file) throws IOException
    {
      lines = new ArrayList<String>();
      random = new Random(seed);
      BufferedReader in
          = new BufferedReader(new FileReader(file));
      String line;
      while((line = in.readLine()) != null)
      {
        lines.add(line);
      }
    }

    public Object getData()
    {
      return lines.get(random.nextInt(lines.size()));
    }

    public IDataSource duplicate()
    {
      return this;
    }
  }

  private static class IncrementLineFileDataSource implements IDataSource
  {
    private final List<String> lines;
    private int next;

    public IncrementLineFileDataSource(String file) throws IOException
    {
      lines = new ArrayList<String>();
      BufferedReader in
          = new BufferedReader(new FileReader(file));
      String line;
      while((line = in.readLine()) != null)
      {
        lines.add(line);
      }
    }

    private IncrementLineFileDataSource(List<String> lines)
    {
      this.lines = lines;
    }

    public Object getData()
    {
      if(next == lines.size())
      {
        next = 0;
      }

      return lines.get(next++);
    }

    public IDataSource duplicate()
    {
      return new IncrementLineFileDataSource(lines);
    }
  }

  private static class RandomStringDataSource implements IDataSource
  {
    private final Random random;

    private RandomStringDataSource(int seed, String pattern)
    {
      StringBuilder sb = new StringBuilder();
      char c;
      for(int i = 0; i < pattern.length(); i++)
      {
        c = pattern.charAt(i);
        if(c == '(')
        {
          c = pattern.charAt(++i);
          
        }
      }
      random = new Random(seed);
      
    }
    
    public Object getData()
    {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public IDataSource duplicate()
    {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
  }

  private static class StaticDataSource implements IDataSource
  {
    private final Object data;

    private StaticDataSource(Object data)
    {
      this.data = data;
    }

    public Object getData()
    {
      return data;
    }

    public IDataSource duplicate()
    {
      // There is no state info so threads can just share one instance.
      return this;
    }
  }

  private IDataSource impl;

  private DataSource(IDataSource impl)
  {
    this.impl = impl;
  }

  public Object getData()
  {
    return impl.getData();
  }

  public DataSource duplicate()
  {
    IDataSource dup = impl.duplicate();
    if(dup == impl)
    {
      return this;
    }
    else
    {
      return new DataSource(dup);
    }
  }

  /**
   * Parses a list of source definitions into an array of data source objects.
   * A data source is defined as follows:
   * - rand({min},{max}) generates a random integer between the min and max.
   * - rand({filename}) retrieves a random line from a file.
   * - inc({min},{max}) returns incremental integer between the min and max.
   * - inc({filename}) retrieves lines in order from a file.
   * - {number} always return the integer as given.
   * - {string} always return the string as given.
   *
   * @param sources The list of source definitions to parse.
   * @return The array of parsed data sources.
   * @throws IOException If an exception occurs while reading a file.
   */
  public static DataSource[] parse(List<String> sources) throws IOException
  {
    Validator.ensureNotNull(sources);
    DataSource[] dataSources = new DataSource[sources.size()];
    for(int i = 0; i < sources.size(); i++)
    {
      String dataSourceDef = sources.get(i);
      if(dataSourceDef.startsWith("rand(") && dataSourceDef.endsWith(")"))
      {
        int lparenPos = dataSourceDef.indexOf("(");
        int commaPos = dataSourceDef.indexOf(",");
        int rparenPos = dataSourceDef.indexOf(")");
        if(commaPos < 0)
        {
          // This is a file name
          dataSources[i] =
              new DataSource(new RandomLineFileDataSource(
                  0, dataSourceDef.substring(lparenPos+1, rparenPos)));
        }
        else
        {
          // This range of integers
          int low =
              Integer.parseInt(dataSourceDef.substring(lparenPos+1, commaPos));
          int high =
              Integer.parseInt(dataSourceDef.substring(commaPos+1, rparenPos));
          dataSources[i] =
              new DataSource(new RandomNumberDataSource(0, low, high));
        }
      }
      else if(dataSourceDef.startsWith("inc(") && dataSourceDef.endsWith(")"))
      {
        int lparenPos = dataSourceDef.indexOf("(");
        int commaPos = dataSourceDef.indexOf(",");
        int rparenPos = dataSourceDef.indexOf(")");
        if(commaPos < 0)
        {
          // This is a file name
          dataSources[i] =
              new DataSource(new IncrementLineFileDataSource(
                  dataSourceDef.substring(lparenPos+1, rparenPos)));
        }
        else
        {
          int low =
              Integer.parseInt(dataSourceDef.substring(lparenPos+1, commaPos));
          int high =
              Integer.parseInt(dataSourceDef.substring(commaPos+1, rparenPos));
          dataSources[i] =
              new DataSource(new IncrementNumberDataSource(low, high));
        }
      }
      else
      {
        try
        {
          dataSources[i] = new DataSource(
              new StaticDataSource(Integer.parseInt(dataSourceDef)));
        }
        catch(NumberFormatException nfe)
        {
          dataSources[i] = new DataSource(
              new StaticDataSource(dataSourceDef));
        }
      }
    }

    return dataSources;
  }

  /**
   * Returns Generated data from the specified data sources.
   * Generated data will be placed in the specified data array. If the data
   * array is null or smaller than the number of data sources, one will be
   * allocated.
   *
   * @param dataSources Data sources that will generate arguments referenced
   * by the format specifiers in the format string.
   * @param data The array where genereated data will be placed to format the
   * string.
   * @return A formatted string
   */
  public static Object[] generateData(DataSource[] dataSources, Object[] data)
  {
    if(data == null || data.length < dataSources.length)
    {
      data = new Object[dataSources.length];
    }
    for(int i = 0; i < dataSources.length; i++)
    {
      data[i] = dataSources[i].getData();
    }
    return data;
  }
}