/*
 * Copyright 2004 by EkoLiving Pty Ltd.  All Rights Reserved.
 *
 * This software is the proprietary information of EkoLiving Pty Ltd.
 * Use is subject to license terms.
 */

package org.openmaji.implementation.server.utility.meemspace;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


import org.openmaji.implementation.server.Common;
import org.openmaji.implementation.server.manager.lifecycle.meemkit.MeemkitClassloaderMonitor;
import org.openmaji.meem.Facet;
import org.openmaji.meem.Meem;
import org.openmaji.meem.wedge.lifecycle.LifeCycleClient;
import org.openmaji.meem.wedge.lifecycle.LifeCycleTransition;
import org.openmaji.meem.wedge.reference.Reference;
import org.openmaji.server.helper.EssentialMeemHelper;
import org.openmaji.server.helper.ReferenceHelper;
import org.openmaji.server.utility.PigeonHole;
import org.openmaji.server.utility.TimeoutException;
import org.openmaji.system.gateway.ServerGateway;
import org.openmaji.system.meemkit.core.MeemkitDescriptor;
import org.openmaji.system.meemkit.core.MeemkitManager;
import org.swzoo.log2.core.LogFactory;
import org.swzoo.log2.core.LogTools;
import org.swzoo.log2.core.Logger;


public class MeemSpaceCreateUtility
{
  private static final Logger logger = LogFactory.getLogger();

  public static final String PROPERTY_MEEMKIT_JARS_URL = "org.openmaji.meemkit_jars_url";

  /**
   * 
   * @throws IOException
   */
  public void installMeemkits() throws IOException
  {
    String urlString = System.getProperty(PROPERTY_MEEMKIT_JARS_URL);
    if ( urlString == null )
    {
        throw new IOException("Property "+PROPERTY_MEEMKIT_JARS_URL+" is not set");
    }

    URL jarsUrl;
    try {
    	jarsUrl = new URL(urlString);
    }
    catch (MalformedURLException e) {
        throw new IOException("Property "+PROPERTY_MEEMKIT_JARS_URL+" URL is not handled: " + urlString);
    }

    String protocol = jarsUrl.getProtocol();
    if ( "http".equalsIgnoreCase(protocol) )
    {
      //TODO Do a HTTP GET of the url and extract the list of meemkits
      throw new IOException("HTTP URLs currently not supported for property "+PROPERTY_MEEMKIT_JARS_URL);
    }

    if ( "file".equalsIgnoreCase(protocol) == false )
    {
      throw new IOException("Unrecognised protocol for "+urlString);
    }

    // Get a reference to the MeemkitManager Meem but make sure that it is
    // READY before making any invocations on it

    final Meem meemkitManagerMeem = EssentialMeemHelper.getEssentialMeem(MeemkitManager.spi.getIdentifier());
    
    if (meemkitManagerMeem == null) {
    	LogTools.info(logger, "Could not locate Meemkit Manager Meem.  Aborting installation of meemkits");
    	return;
    }
    final PigeonHole readyPigeonHole = new PigeonHole();

    Runnable runnable = new Runnable() {
      public void run()
      {
        ServerGateway serverGateway = ServerGateway.spi.create();
        LifeCycleClient client = new MyLifeCycleClient(readyPigeonHole);
        Facet proxy = serverGateway.getTargetFor(client, LifeCycleClient.class);
        Reference reference = Reference.spi.create("lifeCycleClient", proxy, true, null);
        meemkitManagerMeem.addOutboundReference(reference, false);
      }
    };
    Thread thread = new Thread(runnable, "MeemkitManager LifeCycleClient thread");
    thread.start();

    try
    {
    	// wait to receive LifeCycleClient
      readyPigeonHole.get(15000);
    }
    catch ( TimeoutException ex )
    {
      throw new IOException("Timeout waiting for the MeemkitManager to go READY");
    }

    String meemkitsDirectory = jarsUrl.getPath();
    LogTools.info(logger,"Installing all meemkits from "+meemkitsDirectory);

    // It should now be safe to invoke the MeemkitManager
    MeemkitManager mm = (MeemkitManager) ReferenceHelper.getTarget(meemkitManagerMeem, MeemkitManager.spi.getIdentifier(), MeemkitManager.class);

    File meemkitJarsFile = new File(meemkitsDirectory);
    
    if ( meemkitJarsFile.isAbsolute() == false ) {
    	// make the path relative to MAJI_HOME
    	meemkitJarsFile = new File(System.getProperty(Common.PROPERTY_MAJI_HOME) + System.getProperty("file.separator") + meemkitsDirectory);
    	meemkitsDirectory = meemkitJarsFile.getPath();
    	jarsUrl = meemkitJarsFile.toURI().toURL();
    }
    	
    if ( meemkitJarsFile.exists() == false )
    {
      throw new IOException("Directory '" + meemkitsDirectory + "' does not exist");
    }

    if ( meemkitJarsFile.isDirectory() == false )
    {
      throw new IOException("'" + meemkitsDirectory + "' is not a directory");
    }

    File[] files = meemkitJarsFile.listFiles();
    if ( files == null || files.length == 0 )
    {
    	LogTools.info(logger, "No meemkits in " + meemkitsDirectory);
    	return;
      //throw new IOException("No meemkits in " + meemkitsDirectory);
    }
    
    int numberInstalled = 0;
    for ( int i=0; i<files.length; i++ )
    {
      File file = files[i];
      if ( file.getName().endsWith(MeemkitDescriptor.ARCHIVE_SUFFIX) )
      {
        ServerGateway serverGateway = ServerGateway.spi.create();
        PigeonHole pigeonHole = new PigeonHole();
        MeemkitClassloaderMonitor client = new MyMeemkitClassloaderMonitor(pigeonHole);
        Facet proxy = serverGateway.getTargetFor(client, MeemkitClassloaderMonitor.class);
        Reference reference = Reference.spi.create("meemkitClassloaderMonitorClient", proxy, true);
        meemkitManagerMeem.addOutboundReference(reference, false);

        numberInstalled++;
        
        String sourcePath = jarsUrl.getPath() + "/" + file.getName();
        try {
	        URL meemkitUrl = new URL(
	        		jarsUrl.getProtocol(),
	        		"",
	        		sourcePath
	        	);
	        //String source = jarsUri.toString() meemkitsDirectory + File.separator + file.getName();
	        //URL url = new URL(source);
	        mm.installMeemkit(null, meemkitUrl);
	        try
	        {
	          pigeonHole.get(60000);
	        }
	        catch ( TimeoutException ex )
	        {
	          throw new IOException("Timeout waiting for " + meemkitUrl);
	        }
	        finally
	        {
	          serverGateway.revokeTarget(proxy, client);
	          meemkitManagerMeem.removeOutboundReference(reference);
	        }
        }
        catch (MalformedURLException e) {
        	LogTools.info(logger, "Problem with meemkit URL path: " + sourcePath);
        }
      }
    }
    
    if ( numberInstalled == 0 )
    {
      //throw new IOException("No meemkits found in "+meemkitsDirectory);
      LogTools.info(logger, "No meemkits found in "+meemkitsDirectory);
    }
  }
  
  /* ------------------------------------------------------------------------ */

  private class MyMeemkitClassloaderMonitor implements MeemkitClassloaderMonitor
  {
    private final PigeonHole pigeonHole;

    public MyMeemkitClassloaderMonitor(PigeonHole pigeonHole)
    {
      this.pigeonHole = pigeonHole;
    }

    public void meemkitClassloaderStarted(String meemkitName)
    {
      pigeonHole.put(meemkitName);
    }

    public void meemkitClassloaderStopped(String meemkitName)
    {
      // Ignore
    }
  }

  /* ------------------------------------------------------------------------ */

  private class MyLifeCycleClient implements LifeCycleClient
  {
    private final PigeonHole pigeonHole;

    public MyLifeCycleClient(PigeonHole pigeonHole)
    {
      this.pigeonHole = pigeonHole;
    }

    public void lifeCycleStateChanging(LifeCycleTransition transition)
    {
      // Ignore
    }

    public void lifeCycleStateChanged(LifeCycleTransition transition)
    {
      if ( transition.equals(LifeCycleTransition.PENDING_READY) )
      {
        synchronized ( pigeonHole )
        {
          pigeonHole.put(new Boolean(true));
        }
      }
    }
  }
}
