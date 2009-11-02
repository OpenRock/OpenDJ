package org.opends.sdk.tools;

import org.opends.sdk.tools.args.*;
import org.opends.sdk.*;
import org.opends.sdk.requests.Requests;
import org.opends.sdk.requests.ModifyRequest;
import org.opends.sdk.responses.*;
import static org.opends.server.util.StaticUtils.filterExitCode;
import org.opends.server.util.cli.ConsoleApplication;
import static org.opends.server.tools.ToolConstants.*;
import org.opends.messages.Message;
import static org.opends.messages.ToolMessages.*;
import static org.opends.messages.ToolMessages.ERR_TOOL_CONFLICTING_ARGS;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Nov 2, 2009 Time: 4:36:01
 * PM To change this template use File | Settings | File Templates.
 */
public class ModRate extends ConsoleApplication
{
  private BooleanArgument verbose;

  /**
   * The main method for SearchRate tool.
   *
   * @param  args  The command-line arguments provided to this program.
   */

  public static void main(String[] args)
  {
    int retCode = mainModRate(args, System.in, System.out, System.err);

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
    return mainModRate(args, System.in, System.out, System.err);
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

  public static int mainModRate(String[] args, InputStream inStream,
                                   OutputStream outStream, OutputStream errStream)

  {
    return new ModRate(inStream, outStream, errStream).run(args);
  }

  private ModRate(InputStream in, OutputStream out, OutputStream err) {
    super(in, out, err);

  }

  private int run(String[] args)
  {
    // Create the command-line argument parser for use with this program.
    Message toolDescription = Message.raw("This utility can be used to " +
                                          "measure modify performance");
    // TODO: correct usage
    ArgumentParser argParser =
        new ArgumentParser(SearchRate.class.getName(), toolDescription, false,
                           true, 1, 0, "[modifyString ...]");
    ArgumentParserConnectionFactory connectionFactory;

    BooleanArgument   showUsage;
    StringArgument propertiesFileArgument;
    BooleanArgument   noPropertiesFileArgument;
    StringArgument baseDN;
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
           Message.raw("arguments for variables in the modifyString and/or " +
                       "base DN"));
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

    this.modStrings = argParser.getTrailingArguments().toArray(
        new String[argParser.getTrailingArguments().size()]);
    this.baseDN = baseDN.getValue();

    int numConnectionsInt;
    int numThreadsInt;
    try
    {
      numConnectionsInt = numConnections.getIntValue();
      numThreadsInt = numThreads.getIntValue();
      this.maxIterations = maxIterations.getIntValue();
      this.statReportInterval = statInterval.getIntValue() * 1000;
    }
    catch(ArgumentException ex1)
    {
      println(ex1.getMessageObject());
      return ResultCode.CLIENT_SIDE_PARAM_ERROR.intValue();
    }
    this.async = async.isPresent();

    if (!noRebind.isPresent() && numThreadsInt > 1) {
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
      for(String modString : modStrings)
      {
        String.format(modString, data);
      }
      String.format(this.baseDN, data);
    }

    List<Thread> threads = new ArrayList<Thread>();

    Thread statsThread = new StatsThread();
    statsThread.start();

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
            thread = new ModifyThread(connection, connectionFactory, null);
          }
          else
          {
            DataSource[] threadLocalDS = new DataSource[dataSources.length];
            for(int k = 0; k < dataSources.length; k++)
            {
              threadLocalDS[k] = dataSources[k].duplicate();
            }
            thread = new ModifyThread(connection, connectionFactory,
                                      threadLocalDS);
          }
          threads.add(thread);
          thread.start();
        }
      }

      for(Thread t : threads)
      {
        t.join();
      }
      while(!stopRequested &&
            successCount.get() + failedCount.get() < modCount.get())
      {
        Thread.sleep(1000);
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

  private final AtomicInteger modCount = new AtomicInteger();
  private final AtomicInteger successCount = new AtomicInteger();
  private final AtomicInteger failedCount = new AtomicInteger();
  private final AtomicInteger modRecentCount = new AtomicInteger();
  private final AtomicInteger successRecentCount = new AtomicInteger();
  private final AtomicInteger failedRecentCount = new AtomicInteger();
  private volatile boolean stopRequested;
  private int maxIterations;
  private final IncrementHandler handler = new IncrementHandler();
  private boolean async;
  private int statReportInterval;
  private String baseDN;
  private String[] modStrings;


  private class IncrementHandler implements ResultHandler<Result>
  {
    public void handleResult(Result result) {
      successRecentCount.getAndIncrement();
      successCount.getAndIncrement();
    }

    public void handleError(ErrorResultException error) {
      failedRecentCount.getAndIncrement();
      failedCount.getAndIncrement();
      println(Message.raw(error.getResult().toString()));
    }
  }

  private class ModifyThread extends Thread
  {
    private int count;
    private final Connection connection;
    private final ConnectionFactory connectionFactory;
    private final DataSource[] dataSources;

    private ModifyThread(Connection connection,
                         ConnectionFactory connectionFactory,
                         DataSource[] dataSources)
    {
      this.connection = connection;
      this.connectionFactory = connectionFactory;
      this.dataSources = dataSources;
    }

    public void run()
    {
      ModifyRequest mr;
      Object[] data = null;

      ResultFuture future;
      Connection connection;
      while(!stopRequested && !(maxIterations > 0 && count >= maxIterations))
      {
        if(dataSources != null)
        {
          data = DataSource.generateData(dataSources, data);
        }
        mr = newModifyRequest(data);
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
        future = connection.modify(mr, handler);
        modRecentCount.getAndIncrement();
        modCount.getAndIncrement();
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
      }
    }

    private ModifyRequest newModifyRequest(Object[] data)
    {
      String formattedString;
      int colonPos;
      ModifyRequest mr;
      if(data == null)
      {
        mr = Requests.newModifyRequest(baseDN);
      }
      else
      {
        mr = Requests.newModifyRequest(String.format(baseDN, data));
      }
      for(int i = 0; i < modStrings.length; i++)
      {
        if(data == null)
        {
          formattedString = modStrings[i];
        }
        else
        {
          formattedString = String.format(modStrings[i], data);
        }
        colonPos = formattedString.indexOf(':');
        if(colonPos > 0)
        {
          mr.addChange(ModificationType.REPLACE,
                       formattedString.substring(0, colonPos),
                       formattedString.substring(colonPos + 1));
        }
      }
      return mr;
    }
  }

  private class StatsThread extends Thread
  {
    @Override
    public void run()
    {
      long lastStatTime;
      double elapsedTime;
      while(!stopRequested)
      {
        lastStatTime = System.currentTimeMillis();
        try
        {
          sleep(statReportInterval);
        }
        catch(InterruptedException ie)
        {
          // Ignore.
        }
        elapsedTime = (System.currentTimeMillis() - lastStatTime)/1000.0;
        println(
            Message.raw("Result rate: " +
                        Math.round(successRecentCount.getAndSet(0) / elapsedTime) +
                        " Request rate: " +
                        Math.round(modRecentCount.getAndSet(0) / elapsedTime)));
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
