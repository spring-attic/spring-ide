/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.roo.shell.eclipse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.felix.framework.util.Util;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

/**
 * Loads Roo via Felix.
 * <p>
 * This class is based on Apache Felix's org.apache.felix.main.Main class.
 * <p>
 * For maximum compatibility with Felix, this class has minimal changes from the
 * original. All changes are noted with a
 * "**** CHANGE FROM ORIGINAL FELIX VERSION ****" comment.
 * @author Christian Dupuis
 * @since 1.1.0
 */
public class Main {
	/**
	 * Switch for specifying bundle directory.
	 **/
	public static final String BUNDLE_DIR_SWITCH = "-b";

	/**
	 * Name of the configuration directory.
	 */
	public static final String CONFIG_DIRECTORY = "conf";

	/**
	 * The default name used for the configuration properties file.
	 **/
	public static final String CONFIG_PROPERTIES_FILE_VALUE = "config.properties";

	/**
	 * The property name used to specify an URL to the configuration property
	 * file to be used for the created the framework instance.
	 **/
	public static final String CONFIG_PROPERTIES_PROP = "felix.config.properties";

	/**
	 * The property name used to specify whether the launcher should install a
	 * shutdown hook.
	 **/
	public static final String SHUTDOWN_HOOK_PROP = "felix.shutdown.hook";

	/**
	 * The default name used for the system properties file.
	 **/
	public static final String SYSTEM_PROPERTIES_FILE_VALUE = "system.properties";

	/**
	 * The property name used to specify an URL to the system property file.
	 **/
	public static final String SYSTEM_PROPERTIES_PROP = "felix.system.properties";

	/**
	 * Version of Roo bundle cache. STS auto installs bundles into Roo and if bundles in STS change, e.g. due to an STS update, the cache needs to be cleaned.  
	 */
	private static final int CACHE_VERSION = 2;
	
	private Framework m_fwk = null;

	/**
	 * <p>
	 * This method performs the main task of constructing an framework instance
	 * and starting its execution. The following functions are performed when
	 * invoked:
	 * </p>
	 * <ol>
	 * <li><i><b>Examine and verify command-line arguments.</b></i> The launcher
	 * accepts a "<tt>-b</tt>" command line switch to set the bundle auto-deploy
	 * directory and a single argument to set the bundle cache directory.</li>
	 * <li><i><b>Read the system properties file.</b></i> This is a file
	 * containing properties to be pushed into <tt>System.setProperty()</tt>
	 * before starting the framework. This mechanism is mainly shorthand for
	 * people starting the framework from the command line to avoid having to
	 * specify a bunch of <tt>-D</tt> system property definitions. The only
	 * properties defined in this file that will impact the framework's behavior
	 * are the those concerning setting HTTP proxies, such as
	 * <tt>http.proxyHost</tt>, <tt>http.proxyPort</tt>, and
	 * <tt>http.proxyAuth</tt>. Generally speaking, the framework does not use
	 * system properties at all.</li>
	 * <li><i><b>Read the framework's configuration property file.</b></i> This
	 * is a file containing properties used to configure the framework instance
	 * and to pass configuration information into bundles installed into the
	 * framework instance. The configuration property file is called
	 * <tt>config.properties</tt> by default and is located in the
	 * <tt>conf/</tt> directory of the Felix installation directory, which is
	 * the parent directory of the directory containing the <tt>felix.jar</tt>
	 * file. It is possible to use a different location for the property file by
	 * specifying the desired URL using the <tt>felix.config.properties</tt>
	 * system property; this should be set using the <tt>-D</tt> syntax when
	 * executing the JVM. If the <tt>config.properties</tt> file cannot be
	 * found, then default values are used for all configuration properties.
	 * Refer to the <a href="Felix.html#Felix(java.util.Map)"> <tt>Felix</tt>
	 * </a> constructor documentation for more information on framework
	 * configuration properties.</li>
	 * <li><i><b>Copy configuration properties specified as system properties
	 * into the set of configuration properties.</b></i> Even though the Felix
	 * framework does not consult system properties for configuration
	 * information, sometimes it is convenient to specify them on the command
	 * line when launching Felix. To make this possible, the Felix launcher
	 * copies any configuration properties specified as system properties into
	 * the set of configuration properties passed into Felix.</li>
	 * <li><i><b>Add shutdown hook.</b></i> To make sure the framework shutdowns
	 * cleanly, the launcher installs a shutdown hook; this can be disabled with
	 * the <tt>felix.shutdown.hook</tt> configuration property.</li>
	 * <li><i><b>Create and initialize a framework instance.</b></i> The OSGi
	 * standard <tt>FrameworkFactory</tt> is retrieved from
	 * <tt>META-INF/services</tt> and used to create a framework instance with
	 * the configuration properties.</li>
	 * <li><i><b>Auto-deploy bundles.</b></i> All bundles in the auto-deploy
	 * directory are deployed into the framework instance.</li>
	 * <li><i><b>Start the framework.</b></i> The framework is started and the
	 * launcher thread waits for the framework to shutdown.</li>
	 * </ol>
	 * <p>
	 * It should be noted that simply starting an instance of the framework is
	 * not enough to create an interactive session with it. It is necessary to
	 * install and start bundles that provide a some means to interact with the
	 * framework; this is generally done by bundles in the auto-deploy directory
	 * or specifying an "auto-start" property in the configuration property
	 * file. If no bundles providing a means to interact with the framework are
	 * installed or if the configuration property file cannot be found, the
	 * framework will appear to be hung or deadlocked. This is not the case, it
	 * is executing correctly, there is just no way to interact with it.
	 * </p>
	 * <p>
	 * The launcher provides two ways to deploy bundles into a framework at
	 * startup, which have associated configuration properties:
	 * </p>
	 * <ul>
	 * <li>Bundle auto-deploy - Automatically deploys all bundles from a
	 * specified directory, controlled by the following configuration
	 * properties:
	 * <ul>
	 * <li><tt>felix.auto.deploy.dir</tt> - Specifies the auto-deploy directory
	 * from which bundles are automatically deploy at framework startup. The
	 * default is the <tt>bundle/</tt> directory of the current directory.</li>
	 * <li><tt>felix.auto.deploy.action</tt> - Specifies the auto-deploy actions
	 * to be found on bundle JAR files found in the auto-deploy directory. The
	 * possible actions are <tt>install</tt>, <tt>update</tt>, <tt>start</tt>,
	 * and <tt>uninstall</tt>. If no actions are specified, then the auto-deploy
	 * directory is not processed. There is no default value for this property.</li>
	 * </ul>
	 * </li>
	 * <li>Bundle auto-properties - Configuration properties which specify URLs
	 * to bundles to install/start:
	 * <ul>
	 * <li><tt>felix.auto.install.N</tt> - Space-delimited list of bundle URLs
	 * to automatically install when the framework is started, where <tt>N</tt>
	 * is the start level into which the bundle will be installed (e.g.,
	 * felix.auto.install.2).</li>
	 * <li><tt>felix.auto.start.N</tt> - Space-delimited list of bundle URLs to
	 * automatically install and start when the framework is started, where
	 * <tt>N</tt> is the start level into which the bundle will be installed
	 * (e.g., felix.auto.start.2).</li>
	 * </ul>
	 * </li>
	 * </ul>
	 * <p>
	 * These properties should be specified in the <tt>config.properties</tt> so
	 * that they can be processed by the launcher during the framework startup
	 * process.
	 * </p>
	 * @param args Accepts arguments to set the auto-deploy directory and/or the
	 * bundle cache directory.
	 * @throws Exception If an error occurs.
	 **/
	public Framework start(String projectDir, String bundleDir, String cacheDir, String configProperties, String rooVersion) throws Exception {

		// Read configuration properties.
		Map<String, String> configProps = new HashMap<String, String>();
		((Map)configProps).putAll(Main.loadConfigProperties(configProperties));

		configProps.put("felix.service.urlhandlers", "false");
		configProps.put("roo.working.directory", projectDir);
		
		// If there is a passed in bundle auto-deploy directory, then
		// that overwrites anything in the config file.
		if (bundleDir != null) {
			configProps.put(AutoProcessor.AUTO_DEPLOY_DIR_PROPERY, bundleDir);
		}

		// If there is a passed in bundle cache directory, then
		// that overwrites anything in the config file.
		if (cacheDir != null) {
			configProps.put(Constants.FRAMEWORK_STORAGE, cacheDir);
		}

		configProps.put("org.osgi.framework.system.packages.extra", "org.springsource.ide.eclipse.commons.frameworks.core.internal.plugins");
		configProps.put("roobot.index.dowload", "false");
		
		if (shouldCleanCache(cacheDir)) {
			configProps.put("org.osgi.framework.storage.clean", "onFirstInit");
		}
		
		// If enabled, register a shutdown hook to make sure the framework is
		// cleanly shutdown when the VM exits.
		String enableHook = configProps.get(SHUTDOWN_HOOK_PROP);
		if ((enableHook == null) || !enableHook.equalsIgnoreCase("false")) {
			Runtime.getRuntime().addShutdownHook(new Thread("Felix Shutdown Hook") {
				public void run() {
					try {
						if (m_fwk != null) {
							m_fwk.stop();
							m_fwk.waitForStop(0);
						}
					}
					catch (Exception ex) {
						System.err.println("Error stopping framework: " + ex);
					}
				}
			});
		}

		// Print welcome banner. // **** CHANGE FROM ORIGINAL FELIX VERSION ****
		// System.out.println("\nWelcome to Felix");
		// System.out.println("================\n");
		// **** END OF CHANGE FROM ORIGINAL FELIX VERSION ****

		try {
			// Create an instance of the framework.
			FrameworkFactory factory = getFrameworkFactory();
			m_fwk = factory.newFramework(configProps);
			// Initialize the framework, but don't start it yet.
			m_fwk.init();
			// Use the system bundle context to process the auto-deploy
			// and auto-install/auto-start properties.
			AutoProcessor.process(configProps, m_fwk.getBundleContext(), rooVersion);
			// Start the framework.
			m_fwk.start();
			return m_fwk;
		}
		catch (Exception ex) {
			System.err.println("Could not create framework: " + ex);
			ex.printStackTrace();
			// System.exit(-1);
		}
		
		return null;
	}

	private boolean shouldCleanCache(String cacheDir) throws FileNotFoundException {
		Properties props = new Properties();
		
		// check version
		// store file in cache directories parent since the cache will get deleted
		String cacheName;
		int index = cacheDir.lastIndexOf("sts-cache");
		if (index > -1) {
			cacheName = cacheDir.subSequence(index, cacheDir.length()).toString();
		} else {
			cacheName = "sts-cache";
		}
		File versionFile = new File(new File(cacheDir).getParentFile(), "." + cacheName + "-version");
		if (versionFile.exists()) {
			try {
				FileInputStream in = new FileInputStream(versionFile);
				try {
					props.load(in);
				} finally {
					in.close();
				}
				int version = Integer.parseInt(props.getProperty("version"));
				if (version == CACHE_VERSION) {
					// nothing to do
					return false;
				}
			} catch (Exception e) {
				// ignore
			}
		}

		// update version
		try {
			FileOutputStream out = new FileOutputStream(versionFile);
			props.setProperty("version", Integer.toString(CACHE_VERSION));
			try {
				props.store(out, "Auto generated by SpringSource Tool Suite");
			} finally {
				out.close();
			}					
		} catch (Exception e) {
			// ignore
		}

		return true;
	}

	public static void copySystemProperties(Properties configProps) {
		for (Enumeration e = System.getProperties().propertyNames(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			if (key.startsWith("felix.") || key.startsWith("org.osgi.framework.")) {
				configProps.setProperty(key, System.getProperty(key));
			}
		}
	}

	/**
	 * <p>
	 * Loads the configuration properties in the configuration property file
	 * associated with the framework installation; these properties are
	 * accessible to the framework and to bundles and are intended for
	 * configuration purposes. By default, the configuration property file is
	 * located in the <tt>conf/</tt> directory of the Felix installation
	 * directory and is called "<tt>config.properties</tt>". The installation
	 * directory of Felix is assumed to be the parent directory of the
	 * <tt>felix.jar</tt> file as found on the system class path property. The
	 * precise file from which to load configuration properties can be set by
	 * initializing the "<tt>felix.config.properties</tt>" system property to an
	 * arbitrary URL.
	 * </p>
	 * @param configProperties
	 * @return A <tt>Properties</tt> instance or <tt>null</tt> if there was an
	 * error.
	 **/
	public static Properties loadConfigProperties(String configProperties) {
		// The config properties file is either specified by a system
		// property or it is in the conf/ directory of the Felix
		// installation directory. Try to load it from one of these
		// places.

		// See if the property URL was specified as a property.
		URL propURL = null;
		String custom = configProperties;
		if (custom != null) {
			try {
				propURL = new URL(custom);
			}
			catch (MalformedURLException ex) {
				System.err.print("Main: " + ex);
				return null;
			}
		}
		else {
			// Determine where the configuration directory is by figuring
			// out where felix.jar is located on the system class path.
			File confDir = null;
			String classpath = System.getProperty("java.class.path");
			int index = classpath.toLowerCase().indexOf("felix.jar");
			int start = classpath.lastIndexOf(File.pathSeparator, index) + 1;
			if (index >= start) {
				// Get the path of the felix.jar file.
				String jarLocation = classpath.substring(start, index);
				// Calculate the conf directory based on the parent
				// directory of the felix.jar directory.
				confDir = new File(new File(new File(jarLocation).getAbsolutePath()).getParent(), CONFIG_DIRECTORY);
			}
			else {
				// Can't figure it out so use the current directory as default.
				confDir = new File(System.getProperty("user.dir"), CONFIG_DIRECTORY);
			}

			try {
				propURL = new File(confDir, CONFIG_PROPERTIES_FILE_VALUE).toURL();
			}
			catch (MalformedURLException ex) {
				System.err.print("Main: " + ex);
				return null;
			}
		}

		// Read the properties file.
		Properties props = new Properties();
		InputStream is = null;
		try {
			// Try to load config.properties.
			is = propURL.openConnection().getInputStream();
			props.load(is);
			is.close();
		}
		catch (Exception ex) {
			// Try to close input stream if we have one.
			try {
				if (is != null)
					is.close();
			}
			catch (IOException ex2) {
				// Nothing we can do.
			}

			return null;
		}

		// Perform variable substitution for system properties.
		for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
			String name = (String) e.nextElement();
			props.setProperty(name, Util.substVars(props.getProperty(name), name, null, props));
		}

		return props;
	}

	/**
	 * <p>
	 * Loads the properties in the system property file associated with the
	 * framework installation into <tt>System.setProperty()</tt>. These
	 * properties are not directly used by the framework in anyway. By default,
	 * the system property file is located in the <tt>conf/</tt> directory of
	 * the Felix installation directory and is called "
	 * <tt>system.properties</tt>". The installation directory of Felix is
	 * assumed to be the parent directory of the <tt>felix.jar</tt> file as
	 * found on the system class path property. The precise file from which to
	 * load system properties can be set by initializing the "
	 * <tt>felix.system.properties</tt>" system property to an arbitrary URL.
	 * </p>
	 **/
	public static void loadSystemProperties() {
		// The system properties file is either specified by a system
		// property or it is in the same directory as the Felix JAR file.
		// Try to load it from one of these places.

		// See if the property URL was specified as a property.
		URL propURL = null;
		String custom = System.getProperty(SYSTEM_PROPERTIES_PROP);
		if (custom != null) {
			try {
				propURL = new URL(custom);
			}
			catch (MalformedURLException ex) {
				System.err.print("Main: " + ex);
				return;
			}
		}
		else {
			// Determine where the configuration directory is by figuring
			// out where felix.jar is located on the system class path.
			File confDir = null;
			String classpath = System.getProperty("java.class.path");
			int index = classpath.toLowerCase().indexOf("felix.jar");
			int start = classpath.lastIndexOf(File.pathSeparator, index) + 1;
			if (index >= start) {
				// Get the path of the felix.jar file.
				String jarLocation = classpath.substring(start, index);
				// Calculate the conf directory based on the parent
				// directory of the felix.jar directory.
				confDir = new File(new File(new File(jarLocation).getAbsolutePath()).getParent(), CONFIG_DIRECTORY);
			}
			else {
				// Can't figure it out so use the current directory as default.
				confDir = new File(System.getProperty("user.dir"), CONFIG_DIRECTORY);
			}

			try {
				propURL = new File(confDir, SYSTEM_PROPERTIES_FILE_VALUE).toURL();
			}
			catch (MalformedURLException ex) {
				System.err.print("Main: " + ex);
				return;
			}
		}

		// Read the properties file.
		Properties props = new Properties();
		InputStream is = null;
		try {
			is = propURL.openConnection().getInputStream();
			props.load(is);
			is.close();
		}
		catch (FileNotFoundException ex) {
			// Ignore file not found.
		}
		catch (Exception ex) {
			System.err.println("Main: Error loading system properties from " + propURL);
			System.err.println("Main: " + ex);
			try {
				if (is != null)
					is.close();
			}
			catch (IOException ex2) {
				// Nothing we can do.
			}
			return;
		}

		// Perform variable substitution on specified properties.
		for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
			String name = (String) e.nextElement();
			System.setProperty(name, Util.substVars(props.getProperty(name), name, null, null));
		}
	}

	/**
	 * Simple method to parse META-INF/services file for framework factory.
	 * Currently, it assumes the first non-commented line is the class name of
	 * the framework factory implementation.
	 * @return The created <tt>FrameworkFactory</tt> instance.
	 * @throws Exception if any errors occur.
	 **/
	private static FrameworkFactory getFrameworkFactory() throws Exception {
		return (FrameworkFactory) Class.forName("org.apache.felix.framework.FrameworkFactory").newInstance();
	}

}
