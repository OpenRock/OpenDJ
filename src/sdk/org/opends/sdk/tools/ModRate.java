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
    ModifyPerformanceRunner runner;

    BooleanArgument   showUsage;
    StringArgument propertiesFileArgument;
    BooleanArgument   noPropertiesFileArgument;
    StringArgument baseDN;

    try
    {
      connectionFactory = new ArgumentParserConnectionFactory(argParser, this);
      runner = new ModifyPerformanceRunner(argParser, this);
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
      runner.validate();
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

    runner.modStrings = argParser.getTrailingArguments().toArray(
        new String[argParser.getTrailingArguments().size()]);
    runner.baseDN = baseDN.getValue();

    try
    {

      // Try it out to make sure the format string and data sources match.
      Object[] data = DataSource.generateData(runner.getDataSources(), null);
      for(String modString : runner.modStrings)
      {
        String.format(modString, data);
      }
      String.format(runner.baseDN, data);
    }
    catch(Exception ex1)
    {
      println(Message.raw("Error formatting filter or base DN: " +
          ex1.toString()));
      return ResultCode.CLIENT_SIDE_PARAM_ERROR.intValue();
    }

    return runner.run(connectionFactory);
  }


  private class ModifyPerformanceRunner extends PerformanceRunner
  {
    private String baseDN;
    private String[] modStrings;

    private ModifyPerformanceRunner(ArgumentParser argParser,
                                    ConsoleApplication app)
        throws ArgumentException {
      super(argParser, app);
    }

    WorkerThread newWorkerThread(Connection connection,
                                 ConnectionFactory connectionFactory) {
      return new ModifyWorkerThread(connection, connectionFactory);
    }

    StatsThread newStatsThread() {
      return new StatsThread(new String[0]);
    }

    private class ModifyWorkerThread
        extends WorkerThread<ResultHandler<Result>>
    {
      private ModifyRequest mr;
      private Object[] data;

      private ModifyWorkerThread(Connection connection,
                                 ConnectionFactory connectionFactory)
      {
        super(connection, connectionFactory);
      }

      public ResultHandler<Result> getHandler(long startTime)
      {
        return new UpdateStatsResultHandler<Result>(startTime);
      }
      public ResultFuture performOperation(Connection connection,
                                           ResultHandler<Result> handler,
                                           DataSource[] dataSources)
      {
        if(dataSources != null)
        {
          data = DataSource.generateData(dataSources, data);
        }
        mr = newModifyRequest(data);
        return connection.modify(mr, handler);
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
