package org.opends.sdk.tools;

import org.opends.server.util.cli.ConsoleApplication;
import static org.opends.server.util.StaticUtils.filterExitCode;
import static org.opends.server.tools.ToolConstants.*;
import static org.opends.server.tools.ToolConstants.OPTION_LONG_BASEDN;
import org.opends.messages.Message;
import static org.opends.messages.ToolMessages.*;
import static org.opends.messages.ToolMessages.INFO_SEARCH_DESCRIPTION_SEARCH_SCOPE;
import org.opends.sdk.tools.args.*;
import org.opends.sdk.*;
import org.opends.sdk.responses.*;
import org.opends.sdk.requests.SearchRequest;
import org.opends.sdk.requests.Requests;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.GarbageCollectorMXBean;
import static java.lang.Thread.sleep;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Oct 28, 2009
 * Time: 2:27:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class SearchRate extends ConsoleApplication
{
  private BooleanArgument verbose;

  /**
   * The main method for SearchRate tool.
   *
   * @param  args  The command-line arguments provided to this program.
   */

  public static void main(String[] args)
  {
    int retCode = mainSearchRate(args, System.in, System.out, System.err);

    if(retCode != 0)
    {
      System.exit(filterExitCode(retCode));
    }
  }

  /**
   * Parses the provided command-line arguments and uses that information to
   * run the ldapsearch tool.
   *
   * @param  args  The command-line arguments provided to this program.
   *
   * @return The error code.
   */

  public static int mainSearchRate(String[] args)
  {
    return mainSearchRate(args, System.in, System.out, System.err);
  }

  /**
   * Parses the provided command-line arguments and uses that information to
   * run the ldapsearch tool.
   *
   * @param  args              The command-line arguments provided to this
   *                           program.
   * @param  outStream         The output stream to use for standard output, or
   *                           <CODE>null</CODE> if standard output is not
   *                           needed.
   * @param  errStream         The output stream to use for standard error, or
   *                           <CODE>null</CODE> if standard error is not
   *                           needed.
   *
   * @return The error code.
   */

  public static int mainSearchRate(String[] args, InputStream inStream,
                                   OutputStream outStream, OutputStream errStream)

  {
    return new SearchRate(inStream, outStream, errStream).run(args);
  }

  private SearchRate(InputStream in, OutputStream out, OutputStream err) {
    super(in, out, err);

  }

  private int run(String[] args)
  {
    // Create the command-line argument parser for use with this program.
    Message toolDescription = Message.raw("This utility can be used to " +
                                          "measure search performance");
    // TODO: correct usage
    ArgumentParser argParser =
        new ArgumentParser(SearchRate.class.getName(), toolDescription, false,
                           true, 1, 0, "[filter] [attributes ...]");
    ArgumentParserConnectionFactory connectionFactory;

    BooleanArgument   showUsage;
    StringArgument    propertiesFileArgument;
    BooleanArgument   noPropertiesFileArgument;
    StringArgument baseDN;
    MultiChoiceArgument<SearchScope> searchScope;
    MultiChoiceArgument<DereferenceAliasesPolicy> dereferencePolicy;
    IntegerArgument numThreads;
    IntegerArgument maxIterations;
    IntegerArgument statInterval;
    BooleanArgument keepConnectionsOpen;
    BooleanArgument noRebind;
    BooleanArgument async;
    StringArgument arguments;
    IntegerArgument numConnections;

    try
    {
      connectionFactory = new ArgumentParserConnectionFactory(argParser, this);
      propertiesFileArgument = new StringArgument("propertiesFilePath",
          null, OPTION_LONG_PROP_FILE_PATH,
          false, false, true, INFO_PROP_FILE_PATH_PLACEHOLDER.get(), null, null,
          INFO_DESCRIPTION_PROP_FILE_PATH.get());
      argParser.addArgument(propertiesFileArgument);
      argParser.setFilePropertiesArgument(propertiesFileArgument);

      noPropertiesFileArgument = new BooleanArgument(
          "noPropertiesFileArgument", null, OPTION_LONG_NO_PROP_FILE,
          INFO_DESCRIPTION_NO_PROP_FILE.get());
      argParser.addArgument(noPropertiesFileArgument);
      argParser.setNoPropertiesFileArgument(noPropertiesFileArgument);

      baseDN = new StringArgument("baseDN", OPTION_SHORT_BASEDN,
                                  OPTION_LONG_BASEDN, true, false, true,
                                  INFO_BASEDN_PLACEHOLDER.get(), null, null,
                                  INFO_SEARCH_DESCRIPTION_BASEDN.get());
      baseDN.setPropertyName(OPTION_LONG_BASEDN);
      argParser.addArgument(baseDN);


      searchScope = new MultiChoiceArgument<SearchScope>(
          "searchScope", 's', "searchScope", false,
          true, INFO_SEARCH_SCOPE_PLACEHOLDER.get(), SearchScope.values(),
          false,
          INFO_SEARCH_DESCRIPTION_SEARCH_SCOPE.get());
      searchScope.setPropertyName("searchScope");
      searchScope.setDefaultValue(SearchScope.WHOLE_SUBTREE);
      argParser.addArgument(searchScope);

      dereferencePolicy = new MultiChoiceArgument<DereferenceAliasesPolicy>(
          "derefpolicy", 'a', "dereferencePolicy", false, true,
          INFO_DEREFERENCE_POLICE_PLACEHOLDER.get(),
          DereferenceAliasesPolicy.values(), false,
          INFO_SEARCH_DESCRIPTION_DEREFERENCE_POLICY.get());
      dereferencePolicy.setPropertyName("dereferencePolicy");
      dereferencePolicy.setDefaultValue(DereferenceAliasesPolicy.NEVER);
      argParser.addArgument(dereferencePolicy);

      numThreads = new IntegerArgument("numThreads", 't', "numThreads", false,
          false, true, Message.raw("{numThreads}"), 1, null,
          true, 1, false, 0,
          Message.raw("number of search threads per connection"));
      numThreads.setPropertyName("numThreads");
      argParser.addArgument(numThreads);

      numConnections = new IntegerArgument("numConnections", 'c',
          "numConnections", false, false, true, Message.raw("{numConnections}"),
          1, null, true, 1, false, 0, Message.raw("number of connections"));
      numThreads.setPropertyName("numConnections");
      argParser.addArgument(numConnections);

      maxIterations = new IntegerArgument("maxIterations", 'm', "maxIterations",
          false, false, true, Message.raw("{maxIterations}"), 0, null,
          Message.raw("max searches per thread, 0 for unlimited"));
      numThreads.setPropertyName("maxIterations");
      argParser.addArgument(maxIterations);

      statInterval = new IntegerArgument("statInterval", 'i', "statInterval",
          false, false, true, Message.raw("{statInterval}"), 5, null,
          true, 1, false, 0,
          Message.raw("Display results each specified number of seconds"));
      numThreads.setPropertyName("statInterval");
      argParser.addArgument(statInterval);

      keepConnectionsOpen = new BooleanArgument(
          "keepConnectionsOpen", 'f', "keepConnectionsOpen",
          Message.raw("keep connections open"));
      keepConnectionsOpen.setPropertyName("keepConnectionsOpen");
      argParser.addArgument(keepConnectionsOpen);

      noRebind = new BooleanArgument(
          "noRebind", 'F', "noRebind",
          Message.raw("keep connections open and don't rebind"));
      keepConnectionsOpen.setPropertyName("noRebind");
      argParser.addArgument(noRebind);

      async = new BooleanArgument(
          "asynchronous", 'A', "asynchronous",
          Message.raw("asynch, don't wait for results"));
      keepConnectionsOpen.setPropertyName("asynchronous");
      argParser.addArgument(async);

      arguments = new StringArgument("arguments", 'g', "arguments",
                                     false, true, true,
                                     Message.raw("{arguments}"),
                                     null, null,
           Message.raw("arguments for variables in the filter and/or base DN"));
      arguments.setPropertyName("arguments");
      argParser.addArgument(arguments);


      verbose = new BooleanArgument("verbose", 'v', "verbose",
                                    INFO_DESCRIPTION_VERBOSE.get());
      verbose.setPropertyName("verbose");
      argParser.addArgument(verbose);

      showUsage =
          new BooleanArgument("showUsage", OPTION_SHORT_HELP,
                              OPTION_LONG_HELP,
                              INFO_DESCRIPTION_SHOWUSAGE.get());
      argParser.addArgument(showUsage);
      argParser.setUsageArgument(showUsage, getOutputStream());
    } catch (ArgumentException ae)
    {
      Message message = ERR_CANNOT_INITIALIZE_ARGS.get(ae.getMessage());
      println(message);
      return ResultCode.CLIENT_SIDE_PARAM_ERROR.intValue();
    }

    // Parse the command-line arguments provided to this program.
    try
    {
      argParser.parseArguments(args);
      connectionFactory.validate();
    }
    catch (ArgumentException ae)
    {
      Message message = ERR_ERROR_PARSING_ARGS.get(ae.getMessage());
      println(message);
      println(argParser.getUsageMessage());
      return ResultCode.CLIENT_SIDE_PARAM_ERROR.intValue();
    }

    // If we should just display usage or version information,
    // then print it and exit.
    if (argParser.usageOrVersionDisplayed())
    {
      return 0;
    }

    if (keepConnectionsOpen.isPresent()
        && noRebind.isPresent()) {
      Message message = ERR_TOOL_CONFLICTING_ARGS.get(
          keepConnectionsOpen.getLongIdentifier(),
          noRebind.getLongIdentifier());
      println(message);
      return ResultCode.CLIENT_SIDE_PARAM_ERROR.intValue();
    }

    List<String> attributes = new LinkedList<String>();
    ArrayList<String> filterAndAttributeStrings =
        argParser.getTrailingArguments();
    if(filterAndAttributeStrings.size() > 0)
    {
      // the list of trailing arguments should be structured as follow:
      // the first trailing argument is
      // considered the filter, the other as attributes.
      filter = filterAndAttributeStrings.remove(0);
      // The rest are attributes
      for(String s : filterAndAttributeStrings)
      {
        attributes.add(s);
      }
    }
    this.attributes = attributes.toArray(new String[attributes.size()]);
    this.baseDN = baseDN.getValue();
    try
    {
      scope = searchScope.getTypedValue();
    }
    catch(ArgumentException ex1)
    {
      println(ex1.getMessageObject());
      return ResultCode.CLIENT_SIDE_PARAM_ERROR.intValue();
    }

    try
    {
      dereferencesAliasesPolicy = dereferencePolicy.getTypedValue();
    }
    catch(ArgumentException ex1)
    {
      println(ex1.getMessageObject());
      return ResultCode.CLIENT_SIDE_PARAM_ERROR.intValue();
    }

    int numConnectionsInt;
    int numThreadsInt;
    try
    {
      numConnectionsInt = numConnections.getIntValue();
      numThreadsInt = numThreads.getIntValue();
      this.numThreads = numConnectionsInt * numThreadsInt;
      this.maxIterations = maxIterations.getIntValue();
      this.statReportInterval = statInterval.getIntValue() * 1000;
    }
    catch(ArgumentException ex1)
    {
      println(ex1.getMessageObject());
      return ResultCode.CLIENT_SIDE_PARAM_ERROR.intValue();
    }
    this.async = async.isPresent();

    if (!noRebind.isPresent() && this.numThreads > 1) {
      println(Message.raw(
          "--"+noRebind.getLongIdentifier() + " must be used if --" +
          numThreads.getLongIdentifier() + " is > 1"));
      return ResultCode.CLIENT_SIDE_PARAM_ERROR.intValue();
    }

    if (!noRebind.isPresent() && async.isPresent()) {
      println(Message.raw(
          "--"+noRebind.getLongIdentifier() + " must be used when using --" +
          async.getLongIdentifier()));
      return ResultCode.CLIENT_SIDE_PARAM_ERROR.intValue();
    }


    DataSource[] dataSources = null;
    if(arguments.isPresent() && arguments.hasValue())
    {
      try
      {
        dataSources = DataSource.parse(arguments.getValues());
      }
      catch(IOException ioe)
      {
        println(Message.raw("Error occured while parsing arguments: " +
                            ioe.toString()));
        return ResultCode.CLIENT_SIDE_PARAM_ERROR.intValue();
      }

      // Try it out to make sure the format string and data sources match.
      Object[] data = DataSource.generateData(dataSources, null);
      String.format(this.filter, data);
      String.format(this.baseDN, data);
    }

    List<Thread> threads = new ArrayList<Thread>();

    Connection connection = null;
    Thread thread;
    try
    {
      for(int i = 0; i < numConnectionsInt; i++)
      {
        if(keepConnectionsOpen.isPresent() || noRebind.isPresent())
        {
          connection = connectionFactory.connect(null).get();
        }
        for(int j = 0; j < numThreadsInt; j++)
        {
          if(dataSources == null)
          {
            thread = new SearchThread(connection, connectionFactory, null);
          }
          else
          {
            DataSource[] threadLocalDS = new DataSource[dataSources.length];
            for(int k = 0; k < dataSources.length; k++)
            {
              threadLocalDS[k] = dataSources[k].duplicate();
            }
            thread = new SearchThread(connection, connectionFactory,
                                      threadLocalDS);
          }
          threads.add(thread);
          thread.start();
        }
      }

      Thread statsThread = new StatsThread();
      statsThread.start();

      for(Thread t : threads)
      {
        t.join();
      }
      stopRequested = true;
      statsThread.join();
    }
    catch (InterruptedException e)
    {
      stopRequested = true;
    }
    catch (ErrorResultException e)
    {
      stopRequested = true;
      println(Message.raw(e.getResult().getDiagnosticMessage()));
    }

    return 0;
  }

  private final AtomicInteger searchRecentCount = new AtomicInteger();
  private final AtomicInteger successRecentCount = new AtomicInteger();
  private final AtomicInteger entryRecentCount = new AtomicInteger();
  private final AtomicInteger failedRecentCount = new AtomicInteger();
  private final AtomicLong waitRecentTime = new AtomicLong();
  private final AtomicReference<ReversableArray> eTimeBuffer =
      new AtomicReference<ReversableArray>(new ReversableArray(100000));
  private volatile boolean stopRequested;
  private int maxIterations;
  private boolean async;
  private int statReportInterval;
  private String filter;
  private String baseDN;
  private SearchScope scope;
  private DereferenceAliasesPolicy dereferencesAliasesPolicy;
  private String[] attributes;
  private int numThreads;
  private int targetThroughput = 200000;

  private class IncrementHandler implements SearchResultHandler
  {
    private long eTime = System.nanoTime();

    public void handleEntry(SearchResultEntry entry) {
      entryRecentCount.getAndIncrement();
    }

    public void handleReference(SearchResultReference reference) {
    }

    public void handleResult(SearchResult result) {
      successRecentCount.getAndIncrement();
      eTime = System.nanoTime() - eTime;
      waitRecentTime.getAndAdd(eTime);
      addSample(eTime);
    }

    public void handleError(ErrorResultException error) {
      failedRecentCount.getAndIncrement();
      println(Message.raw(error.getResult().toString()));
    }

    public long getETime()
    {
      return eTime;
    }
  }

  private class SearchThread extends Thread
  {
    private int count;
    private final Connection connection;
    private final ConnectionFactory connectionFactory;
    private final DataSource[] dataSources;

    private SearchThread(Connection connection,
                         ConnectionFactory connectionFactory,
                         DataSource[] dataSources)
    {
      this.connection = connection;
      this.connectionFactory = connectionFactory;
      this.dataSources = dataSources;
    }

    public void run()
    {
      SearchRequest sr;
      Object[] data = null;
      if(dataSources == null)
      {
        sr = Requests.newSearchRequest(baseDN, scope, filter, attributes);
      }
      else
      {
        data = DataSource.generateData(dataSources, data);
        sr = Requests.newSearchRequest(String.format(baseDN, data), scope,
            String.format(filter, data), attributes);
      }
      sr.setDereferenceAliasesPolicy(dereferencesAliasesPolicy);

      SearchResultFuture future;
      Connection connection;
      IncrementHandler handler;
      double targetTimeInMS = (1.0 / (targetThroughput / numThreads))*1000.0;
      double sleepTimeInMS = 0;
      long start;
      while(!stopRequested && !(maxIterations > 0 && count >= maxIterations))
      {
        start = System.nanoTime();
        handler = new IncrementHandler();
        if(dataSources != null)
        {
          data = DataSource.generateData(dataSources, data);
          sr.setFilter(String.format(filter, data));
          sr.setName(String.format(baseDN, data));
        }
        if(this.connection == null)
        {
          try
          {
            connection = connectionFactory.connect(null).get();
          }
          catch (InterruptedException e)
          {
            // Ignore and check stop requested
            continue;
          }
          catch (ErrorResultException e)
          {
            println(Message.raw(e.getResult().getDiagnosticMessage()));
            if(e.getCause() != null)
            {
              e.getCause().printStackTrace(getErrorStream());  
            }
            stopRequested = true;
            break;
          }
        }
        else
        {
          connection = this.connection;   
        }
        future = connection.search(sr, handler);
        searchRecentCount.getAndIncrement();
        count++;
        if(!async)
        {
          try {
            future.get();
          } catch (InterruptedException e) {
            // Ignore and check stop requested
            continue;
          } catch (ErrorResultException e) {
            if(e.getCause() instanceof IOException)
            {
              e.getCause().printStackTrace(getErrorStream());
              stopRequested = true;
              break;
            }
            // Ignore. Handled by result handler
          }
          if(this.connection == null)
          {
            connection.close();
          }
        }
        if(targetThroughput > 0)
        {
          try
          {
            if(sleepTimeInMS > 1)
            {
              sleep((long)Math.floor(sleepTimeInMS));
            }
          }
          catch (InterruptedException e)
          {
            continue;
          }

          sleepTimeInMS += targetTimeInMS - ((System.nanoTime() - start) / 1000000.0);
        }
      }
    }
  }

  private class StatsThread extends Thread
  {
    private final MultiColumnPrinter printer;
    private final List<GarbageCollectorMXBean> beans;
    private long totalSuccessCount;
    private long totalSearchCount;
    private long totalEntryCount;
    private long totalFailedCount;
    private long totalWaitTime;
    private ReversableArray etimes = new ReversableArray(100000);
    ReversableArray array = new ReversableArray(200000);

    private StatsThread() {
      printer = new MultiColumnPrinter(10, 5, "-", SearchRate.this);
      printer.setTitleAlign(1);
      printer.addTitle(new String[]{"Throughput", "",
          "Response Time", "", "", "", "", "", "", ""},
          new int[]{2, 0, 2, 0, 1, 1, 1, 1, 1, 1});
     printer.addTitle(new String[]{"(srch/sec)", "",
          "(ms)", "", "", "", "", "", "", ""},
         new int[]{2, 0, 2, 0, 1, 1, 1, 1, 1, 1});
      printer.addTitle(new String[]{"Recent", "Average", "Recent",
          "Average", "Entries/srch", "Req/Res", "99", "99.9", "99.99", "99.999"});
      beans = ManagementFactory.getGarbageCollectorMXBeans();
    }

    @Override
    public void run()
    {
      printer.printTitle();
      int successCount;
      int searchCount;
      int entryCount;
      int failedCount;
      long waitTime;

      long lastStatTime;
      long lastGCDuration;
      double recentDuration;
      double averageDuration;
      String recentRate;
      String averageRate;
      String recentTime;
      String averageTime;
      String entries;
      String reqs;

      long startTime = System.currentTimeMillis();
      long statTime = startTime;
      long gcDuration = 0;
      for (GarbageCollectorMXBean bean : beans) {
        gcDuration += bean.getCollectionTime();
      }
      while(!stopRequested)
      {
        try
        {
          sleep(statReportInterval);
        }
        catch(InterruptedException ie)
        {
          // Ignore.
        }

        lastStatTime = statTime;
        statTime = System.currentTimeMillis();

        lastGCDuration = gcDuration;
        gcDuration = 0;
        for (GarbageCollectorMXBean bean : beans) {
          gcDuration += bean.getCollectionTime();
        }

        successCount = successRecentCount.getAndSet(0);
        searchCount = searchRecentCount.getAndSet(0);
        entryCount = entryRecentCount.getAndSet(0);
        failedCount = failedRecentCount.getAndSet(0);
        waitTime = waitRecentTime.getAndSet(0);
        totalSuccessCount += successCount;
        totalSearchCount += searchCount;
        totalEntryCount += entryCount;
        totalFailedCount += failedCount;
        totalWaitTime += waitTime;
        recentDuration = statTime - lastStatTime;
        averageDuration = statTime - startTime;
        recentDuration -= gcDuration - lastGCDuration;
        averageDuration -= gcDuration;
        recentDuration /= 1000.0;
        averageDuration /= 1000.0;
        recentRate = String.format("%.1f", successCount / recentDuration);
        averageRate = String.format("%.1f", totalSuccessCount / averageDuration);
        recentTime = String.format("%.3f",
            (waitTime - (gcDuration - lastGCDuration)) / successCount / 1000000.0);
        averageTime = String.format("%.3f",
            (totalWaitTime - gcDuration) / totalSuccessCount / 1000000.0);
        entries = String.format("%.1f",
            (double)entryCount / successCount);
        reqs = String.format("%.1f", (double)searchCount / successCount);

        double[] percents = new double[]{.01, .001, .0001, .00001};
        String[] strings = new String[percents.length];

        boolean changed = false;
        etimes = eTimeBuffer.getAndSet(etimes);
        int appendLength = Math.min(array.remaining(), etimes.size());
        if(appendLength > 0)
        {
          array.append(etimes, appendLength);
          for(int i = array.size - appendLength; i < array.size; i++)
          {
            siftUp(array, 0, i);
          }
          changed = true;
        }
        // Our window buffer is now full. Replace smallest with anything larger
        // and re-heapify
        for(int i = appendLength; i < etimes.size(); i++)
        {
          if(etimes.get(i) > array.get(0))
          {
            array.set(0, etimes.get(i));
            siftDown(array, 0, array.size()-1);
            changed = true;
          }
        }
        etimes.clear();

        if(changed)
        {
          // Perform heapsort
          int i = array.size()-1;
          while(i > 0)
          {
            swap(array, i, 0);
            siftDown(array, 0, i-1);
            i--;
          }
          array.reverse();
        }

        // Now everything is ordered from smallest to largest
        int index;
        for(int i = 0; i < percents.length; i++)
        {
          //System.out.println(percents[i] * totalSuccessCount);
          index = array.size() -
                  (int)Math.floor(percents[i] * totalSuccessCount) - 1;
          if(index < 0)
          {
            strings[i] = String.format("%.3f*", array.get(0) / 1000000.0);
          }
          else
          {
            strings[i] = String.format("%.3f", array.get(index) / 1000000.0);
          }
        }
        printer.printRow(recentRate, averageRate, recentTime, averageTime,
            entries, reqs, strings[0], strings[1], strings[2], strings[3]);
      }
    }
  }

  private static class ReversableArray
  {
    private final long[] array;
    private boolean reversed;
    private int size;

    public ReversableArray(int capacity)
    {
      this.array = new long[capacity];
    }

    public void set(int index, long value)
    {
      if(index >= size)
      {
        throw new IndexOutOfBoundsException();
      }
      if(!reversed)
      {
        array[index] = value;
      }
      else
      {
        array[size - index - 1] = value;
      }
    }

    public long get(int index)
    {
      if(index >= size)
      {
        throw new IndexOutOfBoundsException();
      }
      if(!reversed)
      {
        return array[index];
      }
      else
      {
        return array[size - index - 1];
      }
    }

    public int size()
    {
      return size;
    }

    public int capacity()
    {
      return array.length;
    }

    public void reverse()
    {
      reversed = !reversed;
    }

    public void append(long value)
    {
      if(size == array.length)
      {
        throw new IndexOutOfBoundsException();
      }

      if(!reversed)
      {
        array[size] = value;
      }
      else
      {
        System.arraycopy(array, 0, array, 1, size);
        array[0] = value;
      }
      size++;
    }

    public void append(ReversableArray a, int length)
    {
      if(length > a.size() || length > remaining())
      {
        throw new IndexOutOfBoundsException();
      }
      if(!reversed)
      {
        System.arraycopy(a.array, 0, array, size, length);
      }
      else
      {
        System.arraycopy(array, 0, array, length, size);
        System.arraycopy(a.array, 0, array, 0, length);
      }
      size += length;
    }

    public int remaining()
    {
      return array.length - size;
    }

    public void clear()
    {
      size = 0;
    }
  }

  private void siftDown(ReversableArray a, int start, int end)
  {
    int root = start;
    int child;
    while(root * 2 + 1 <= end)
    {
      child = root * 2 + 1;
      if(child + 1 <= end && a.get(child) > a.get(child+1))
      {
        child = child+1;
      }
      if(a.get(root) > a.get(child))
      {
        swap(a, root, child);
        root = child;
      }
      else
      {
        return;
      }
    }
  }

  private void siftUp(ReversableArray a, int start, int end)
  {
    int child = end;
    int parent;
    while(child > start)
    {
      parent = (int)Math.floor((child-1)/2);
      if(a.get(parent) > a.get(child))
      {
        swap(a, parent, child);
        child = parent;
      }
      else
      {
        return;
      }
    }
  }

  private void swap(ReversableArray a, int i, int i2)
  {
    long temp = a.get(i);
    a.set(i, a.get(i2));
    a.set(i2, temp);
  }

  private void addSample(long sample)
  {
    synchronized(this)
    {
      ReversableArray array = eTimeBuffer.get();
      if(array.remaining() == 0)
      {
        array.set(array.size() -1, sample);
      }
      else
      {
        array.append(sample);
      }
    }
  }

  /**
   * Indicates whether or not the user has requested advanced mode.
   *
   * @return Returns <code>true</code> if the user has requested
   *         advanced mode.
   */
  public boolean isAdvancedMode() {
    return false;
  }

  /**
   * Indicates whether or not the user has requested interactive
   * behavior.
   *
   * @return Returns <code>true</code> if the user has requested
   *         interactive behavior.
   */
  public boolean isInteractive() {
    return false;
  }

  /**
   * Indicates whether or not this console application is running in
   * its menu-driven mode. This can be used to dictate whether output
   * should go to the error stream or not. In addition, it may also
   * dictate whether or not sub-menus should display a cancel option
   * as well as a quit option.
   *
   * @return Returns <code>true</code> if this console application
   *         is running in its menu-driven mode.
   */
  public boolean isMenuDrivenMode() {
    return false;
  }

  /**
   * Indicates whether or not the user has requested quiet output.
   *
   * @return Returns <code>true</code> if the user has requested
   *         quiet output.
   */
  public boolean isQuiet() {
    return false;
  }

  /**
   * Indicates whether or not the user has requested script-friendly
   * output.
   *
   * @return Returns <code>true</code> if the user has requested
   *         script-friendly output.
   */
  public boolean isScriptFriendly() {
    return false;
  }

  /**
   * Indicates whether or not the user has requested verbose output.
   *
   * @return Returns <code>true</code> if the user has requested
   *         verbose output.
   */
  public boolean isVerbose() {
    return verbose.isPresent();
  }
}
