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
 *      Portions Copyright 2008 Sun Microsystems, Inc.
 */
package org.opends.server.core.dataproviders;



import static org.opends.messages.ConfigMessages.*;
import static org.opends.server.loggers.debug.DebugLogger.*;
import static org.opends.server.util.StaticUtils.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opends.messages.Message;
import org.opends.server.admin.ClassPropertyDefinition;
import org.opends.server.admin.server.ConfigurationAddListener;
import org.opends.server.admin.server.ConfigurationChangeListener;
import org.opends.server.admin.server.ConfigurationDeleteListener;
import org.opends.server.admin.server.ServerManagementContext;
import org.opends.server.admin.std.meta.DataProviderCfgDefn;
import org.opends.server.admin.std.server.DataProviderCfg;
import org.opends.server.admin.std.server.RootCfg;
import org.opends.server.config.ConfigException;
import org.opends.server.core.DirectoryServer;
import org.opends.server.loggers.debug.DebugTracer;
import org.opends.server.types.ConfigChangeResult;
import org.opends.server.types.DN;
import org.opends.server.types.DebugLogLevel;
import org.opends.server.types.InitializationException;
import org.opends.server.types.ResultCode;



/**
 * Manages the set of available internal and user data providers and
 * coordinates data provider connection management.
 */
public final class DataProviderConfigManager
{

  /**
   * Internal interface for listening for configuration changes.
   */
  private final class ConfigurationListener implements
      ConfigurationChangeListener<DataProviderCfg>,
      ConfigurationAddListener<DataProviderCfg>,
      ConfigurationDeleteListener<DataProviderCfg>
  {

    /**
     * {@inheritDoc}
     */
    public ConfigChangeResult applyConfigurationAdd(
        DataProviderCfg configuration)
    {
      ResultCode resultCode = ResultCode.SUCCESS;
      boolean adminActionRequired = false;
      List<Message> messages = new ArrayList<Message>();

      // Register as a change listener for the data provider so that
      // we can be notified when it is disabled or enabled.
      configurations.put(configuration.dn(), configuration);
      configuration.addChangeListener(listener);

      // Create the data provider.
      try
      {
        // Register the data provider.
        String name = getDataProviderName(configuration);
        DataProviderID id = DataProviderID.newUserID(name);
        DataProvider provider =
            getDataProvider(id, configuration, messages);
        registerDataProvider(id, provider);
      }
      catch (ConfigException e)
      {
        if (debugEnabled())
        {
          TRACER.debugCaught(DebugLogLevel.ERROR, e);
        }

        messages.add(e.getMessageObject());
        resultCode = DirectoryServer.getServerErrorResultCode();
        return new ConfigChangeResult(resultCode, adminActionRequired,
            messages);
      }

      // Return the configuration result.
      return new ConfigChangeResult(resultCode, adminActionRequired,
          messages);
    }



    /**
     * {@inheritDoc}
     */
    public ConfigChangeResult applyConfigurationChange(
        DataProviderCfg configuration)
    {
      ResultCode resultCode = ResultCode.SUCCESS;
      boolean adminActionRequired = false;
      ArrayList<Message> messages = new ArrayList<Message>();

      String name = getDataProviderName(configuration);
      DataProviderID id = DataProviderID.newUserID(name);
      DataProvider provider = id2provider.get(id);

      // Whatever happens, we're going to need to save the new
      // configuration.
      DN dn = configuration.dn();
      DataProviderCfg oldConfiguration = configurations.get(dn);
      configurations.put(dn, configuration);

      String newClass = configuration.getJavaClass().trim();
      String oldClass = oldConfiguration.getJavaClass().trim();

      if (!oldClass.equals(newClass))
      {
        // The implementation class has been changed. Finalize the old
        // data provider and replace it with the new one.
        if (provider != null)
        {
          deregisterDataProvider(id);
        }

        // Create the new data provider.
        try
        {
          provider = getDataProvider(id, configuration, messages);
          registerDataProvider(id, provider);
        }
        catch (ConfigException e)
        {
          if (debugEnabled())
          {
            TRACER.debugCaught(DebugLogLevel.ERROR, e);
          }

          messages.add(e.getMessageObject());
          resultCode = DirectoryServer.getServerErrorResultCode();
          return new ConfigChangeResult(resultCode,
              adminActionRequired, messages);
        }
      }

      // Return the configuration result.
      return new ConfigChangeResult(resultCode, adminActionRequired,
          messages);
    }



    /**
     * {@inheritDoc}
     */
    public ConfigChangeResult applyConfigurationDelete(
        DataProviderCfg configuration)
    {
      ResultCode resultCode = ResultCode.SUCCESS;
      boolean adminActionRequired = false;
      ArrayList<Message> messages = new ArrayList<Message>();

      // First remove the configuration.
      configurations.remove(configuration.dn());

      // Disable the data provider if required.
      String name = getDataProviderName(configuration);
      DataProviderID id = DataProviderID.newUserID(name);
      DataProvider provider = id2provider.get(id);

      if (provider != null)
      {
        deregisterDataProvider(id);
      }

      // Return the configuration result.
      return new ConfigChangeResult(resultCode, adminActionRequired,
          messages);
    }



    /**
     * {@inheritDoc}
     */
    public boolean isConfigurationAddAcceptable(
        DataProviderCfg configuration, List<Message> unacceptableReasons)
    {
      // It's enabled so always validate the class.
      return isJavaClassAcceptable(configuration, unacceptableReasons);
    }



    /**
     * {@inheritDoc}
     */
    public boolean isConfigurationChangeAcceptable(
        DataProviderCfg configuration, List<Message> unacceptableReasons)
    {
      // It's enabled so always validate the class.
      return isJavaClassAcceptable(configuration, unacceptableReasons);
    }



    /**
     * {@inheritDoc}
     */
    public boolean isConfigurationDeleteAcceptable(
        DataProviderCfg configuration, List<Message> unacceptableReasons)
    {
      // A delete should always be acceptable, so just return true.
      return true;
    }

  }

  /**
   * Event listener which disables a data provider when the last
   * connection is closed.
   */
  private final class EventListener implements
      DataProviderConnectionEventListener
  {

    // The connection.
    private final DataProviderConnection connection;

    // The data provider ID.
    private final DataProviderID id;



    private EventListener(DataProviderID id,
        DataProviderConnection connection)
    {
      this.id = id;
      this.connection = connection;
    }



    /**
     * {@inheritDoc}
     */
    public void dataProviderConnectionClosed()
    {
      // Remove the connection from the connection list and stop
      // the data provider if it was the last connection.
      synchronized (lock)
      {
        DataProvider provider = id2provider.get(id);

        if (provider != null)
        {
          List<DataProviderConnection> connections =
              id2connections.get(id);
          if (connections.remove(connection) && connections.isEmpty())
          {
            provider.stopDataProvider();
          }
        }
      }
    }



    /**
     * {@inheritDoc}
     */
    public void dataProviderStateChanged(DataProviderEvent event)
    {
      // No implementation required.
    }

  }



  // The singleton instance.
  private static final DataProviderConfigManager INSTANCE =
      new DataProviderConfigManager();

  // The tracer object for the debug logger.
  private static final DebugTracer TRACER = getTracer();



  /**
   * Returns the global data provider configuration manager instance.
   *
   * @return The global data provider configuration manager instance.
   */
  public static DataProviderConfigManager getInstance()
  {
    return INSTANCE;
  }



  // The table of user data provider configurations.
  private final Map<DN, DataProviderCfg> configurations =
      new HashMap<DN, DataProviderCfg>();

  // The table of active connections to data providers.
  private final Map<DataProviderID, List<DataProviderConnection>>
    id2connections =
      new HashMap<DataProviderID, List<DataProviderConnection>>();

  // The table of available data providers.
  private final Map<DataProviderID, DataProvider> id2provider =
      new HashMap<DataProviderID, DataProvider>();

  // Indicates whether or not this configuration manager has already
  // been initialized.
  private boolean isInitialized = false;

  // The configuration listener.
  private final ConfigurationListener listener =
      new ConfigurationListener();

  // Lock object for synchronizing access.
  private final Object lock = new Object();



  /**
   * Creates a new data provider manager.
   */
  private DataProviderConfigManager()
  {
    // No implementation required.
  }



  /**
   * Creates a connection to the data provider identified by the
   * provided ID.
   *
   * @param id
   *          The unique ID of the data provider.
   * @return A connection to the data provider identified by the
   *         provided ID, or {@code null} if {@code id} was not
   *         recognized.
   */
  public DataProviderConnection connect(DataProviderID id)
  {
    synchronized (lock)
    {
      DataProvider provider = id2provider.get(id);

      if (provider != null)
      {
        List<DataProviderConnection> connections =
            id2connections.get(id);

        // Enable the data provider if this is the first connection.
        if (connections.isEmpty())
        {
          provider.startDataProvider();
        }

        DataProviderConnection connection = provider.connect();
        connection.registerEventListener(new EventListener(id,
            connection));
        connections.add(connection);
        return connection;
      }
      else
      {
        return null;
      }
    }
  }



  /**
   * Deregisters a data provider from this manager and finalizes it.
   *
   * @param id
   *          The unique ID of the data provider.
   */
  public void deregisterDataProvider(DataProviderID id)
  {
    synchronized (lock)
    {
      DataProvider provider = id2provider.remove(id);

      if (!id2connections.get(id).isEmpty())
      {
        // FIXME: notify and close the connections.

        try
        {
          provider.stopDataProvider();
        }
        catch (Exception e)
        {
          if (debugEnabled())
          {
            TRACER.debugCaught(DebugLogLevel.ERROR, e);
          }
        }
      }

      try
      {
        provider.finalizeDataProvider();
      }
      catch (Exception e)
      {
        if (debugEnabled())
        {
          TRACER.debugCaught(DebugLogLevel.ERROR, e);
        }
      }

      id2connections.remove(id);
    }
  }



  // Load and initialize the data provider named in the config.
  // Returns null if the data provider's lock file cannot be acquired.
  private DataProvider getDataProvider(DataProviderID id,
      DataProviderCfg config, List<Message> messages)
      throws ConfigException
  {
    String className = config.getJavaClass();
    DataProviderCfgDefn d = DataProviderCfgDefn.getInstance();
    ClassPropertyDefinition pd = d.getJavaClassPropertyDefinition();

    // Load the class and cast it to a data provider factory.
    Class<? extends DataProviderFactory> theClass;
    DataProviderFactory<?> factory;

    try
    {
      theClass = pd.loadClass(className, DataProviderFactory.class);
      factory = theClass.newInstance();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_CONFIG_DATA_PROVIDER_CANNOT_INITIALIZE.get(String
              .valueOf(className), String.valueOf(config.dn()),
              stackTraceToSingleLineString(e));
      throw new ConfigException(message, e);
    }

    // Perform the necessary initialization for the data provider.
    DataProvider provider;
    try
    {
      // Determine the initialization method to use: it must take
      // a single parameter which is the configuration object.
      Method method =
          theClass.getMethod("createDataProvider",
              DataProviderID.class, config.configurationClass());

      // Create the instance.
      provider = (DataProvider) method.invoke(factory, id, config);
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      Message message =
          ERR_CONFIG_DATA_PROVIDER_CANNOT_INITIALIZE.get(String
              .valueOf(className), String.valueOf(config.dn()),
              stackTraceToSingleLineString(e));
      throw new ConfigException(message, e);
    }

    // The data provider has been successfully initialized.
    return provider;
  }



  // Gets the name of the provided configuration.
  private String getDataProviderName(DataProviderCfg configuration)
  {
    DN dn = configuration.dn();
    return dn.getRDN().getAttributeValue(0).toString();
  }



  /**
   * Initializes this data provider configuration manager. This should
   * only be called at Directory Server startup.
   *
   * @throws ConfigException
   *           If a critical configuration problem prevents a data
   *           provider initialization from succeeding.
   * @throws InitializationException
   *           If a problem occurs while initializing a data provider
   *           that is not related to the server configuration.
   */
  void initialize() throws ConfigException, InitializationException
  {
    // Prevent multiple initialization.
    if (isInitialized)
    {
      return;
    }

    // Create an internal server management context and retrieve
    // the root configuration.
    ServerManagementContext context =
        ServerManagementContext.getInstance();
    RootCfg root = context.getRootConfiguration();

    // Register add and delete listeners.
    root.addDataProviderAddListener(listener);
    root.addDataProviderDeleteListener(listener);

    // Initialize user data providers.
    try
    {
      for (String name : root.listDataProviders())
      {
        // Get the data provider's configuration.
        DataProviderCfg configuration = root.getDataProvider(name);

        // Register as a change listener for the data provider so that
        // we can be notified when it is disabled or enabled.
        configurations.put(configuration.dn(), configuration);
        configuration.addChangeListener(listener);

        // Create the data provider and register it.
        List<Message> messages = new ArrayList<Message>();
        DataProviderID id = DataProviderID.newUserID(name);
        DataProvider provider =
            getDataProvider(id, configuration, messages);
        registerDataProvider(id, provider);
      }
    }
    catch (ConfigException e)
    {
      // Data providers which have already been created may have
      // allocated some resources. Be safe and shut everything down
      // cleanly.
      shutdown();

      // Rethrow the exception.
      throw e;
    }

    // Prevent multiple initialization.
    isInitialized = true;
  }



  // Determines whether or not the new configuration's implementation
  // class is acceptable.
  private boolean isJavaClassAcceptable(DataProviderCfg config,
      List<Message> unacceptableReasons)
  {
    String className = config.getJavaClass();
    DataProviderCfgDefn d = DataProviderCfgDefn.getInstance();
    ClassPropertyDefinition pd = d.getJavaClassPropertyDefinition();

    // Load the class and cast it to a data provider factory.
    DataProviderFactory<?> factory = null;
    Class<? extends DataProviderFactory> theClass;
    try
    {
      theClass = pd.loadClass(className, DataProviderFactory.class);
      factory = theClass.newInstance();
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      unacceptableReasons
          .add(ERR_CONFIG_DATA_PROVIDER_CANNOT_INITIALIZE.get(String
              .valueOf(className), String.valueOf(config.dn()),
              stackTraceToSingleLineString(e)));
      return false;
    }

    // Perform the necessary initialization for the data provider.
    try
    {
      // Determine the initialization method to use: it must take a
      // single parameter which is the exact type of the configuration
      // object.
      Method method =
          theClass.getMethod("isConfigurationAcceptable",
              DataProviderCfg.class, List.class);
      Boolean acceptable =
          (Boolean) method.invoke(factory, config, unacceptableReasons);

      if (!acceptable)
      {
        return false;
      }
    }
    catch (Exception e)
    {
      if (debugEnabled())
      {
        TRACER.debugCaught(DebugLogLevel.ERROR, e);
      }

      unacceptableReasons
          .add(ERR_CONFIG_DATA_PROVIDER_CANNOT_INITIALIZE.get(String
              .valueOf(className), String.valueOf(config.dn()),
              stackTraceToSingleLineString(e)));
      return false;
    }

    // The class is valid as far as we can tell.
    return true;
  }



  /**
   * Registers a data provider with this manager.
   *
   * @param id
   *          The data provider ID.
   * @param provider
   *          The data provider.
   */
  public void registerDataProvider(DataProviderID id,
      DataProvider provider)
  {
    synchronized (lock)
    {
      id2provider.put(id, provider);
      id2connections.put(id, new LinkedList<DataProviderConnection>());
    }
  }



  /**
   * Shuts down this data provider configuration manager. All registered
   * data providers will be finalized and deregistered.
   */
  void shutdown()
  {
    synchronized (lock)
    {
      // Shutdown all data providers.
      for (DataProvider provider : id2provider.values())
      {
        try
        {
          provider.finalizeDataProvider();
        }
        catch (Exception e)
        {
          if (debugEnabled())
          {
            TRACER.debugCaught(DebugLogLevel.ERROR, e);
          }
        }
      }
      id2provider.clear();

      // Clear all connections.
      id2connections.clear();
    }

    // Clean up configuration listeners.
    ServerManagementContext context =
        ServerManagementContext.getInstance();
    RootCfg root = context.getRootConfiguration();

    root.removeDataProviderAddListener(listener);
    root.removeDataProviderDeleteListener(listener);

    for (DataProviderCfg configuration : configurations.values())
    {
      configuration.removeChangeListener(listener);
    }

    configurations.clear();

    // Allow re-initialization.
    isInitialized = false;
  }

}
