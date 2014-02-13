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
package org.springframework.ide.eclipse.beans.ui.livegraph.model;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.json.JSONException;
import org.springframework.context.support.LiveBeansViewMBean;
import org.springframework.ide.eclipse.beans.ui.livegraph.LiveGraphUiPlugin;
import org.springsource.ide.eclipse.commons.core.StatusHandler;

/**
 * Loads an MBean exposed by the Spring Framework and generates a
 * {@link LiveBeansModel} from the JSON contained within.
 * 
 * @author Leo Dos Santos
 */
public class LiveBeansModelGenerator {

	/**
	 * This method will not attempt to close the given {@link JMXConnector}. If
	 * the connection has failed, clients may capture the thrown
	 * {@link CoreException} and inform the user. This method is not UI safe,
	 * and may block the UI with network operations. Clients will need to call
	 * this method from a non-blocking {@link Job}.
	 * 
	 * @param connector
	 * @param appName
	 * @return A valid {@link LiveBeansModel} model, or <code>null</code> if
	 * connection has failed
	 * @throws CoreException
	 */
	public static LiveBeansModel connectToModel(JMXConnector connector, LiveBeansSession session) throws CoreException {
		try {
			String appName = session.getApplicationName();
			if (connector != null) {
				ObjectName name;
				if (appName == null || appName.length() == 0) {
					// Standalone apps like spring-boot will have an empty app
					// name. Deal with that situation.
					name = ObjectName.getInstance("", "application", "");
				}
				else {
					name = ObjectName.getInstance("", "application", "/".concat(appName));
				}
				MBeanServerConnection connection = connector.getMBeanServerConnection();
				// Test the MBean's existence before proceeding. Will throw
				// InstanceNotFoundException
				connection.getObjectInstance(name);
				LiveBeansViewMBean mbean = MBeanServerInvocationHandler.newProxyInstance(connection, name,
						LiveBeansViewMBean.class, false);
				return generateModel(mbean, session);
			}
		}
		catch (MalformedObjectNameException e) {
			throw new CoreException(new Status(IStatus.ERROR, LiveGraphUiPlugin.PLUGIN_ID,
					"An error occurred while connecting to server. Please check that the application name is correct.",
					e));
		}
		catch (InstanceNotFoundException e) {
			throw new CoreException(new Status(IStatus.ERROR, LiveGraphUiPlugin.PLUGIN_ID,
					"An error occurred while connecting to server. Please check that the application name is correct.",
					e));
		}
		catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, LiveGraphUiPlugin.PLUGIN_ID,
					"An error occurred while connecting to server.", e));
		}
		return null;
	}

	
	/**
	 * This method will attempt to create a {@link JMXConnector} from the given
	 * parameters and will close it when it is finished. If the connection has
	 * failed, clients may capture the thrown {@link CoreException} and inform
	 * the user. This method is UI safe, and will not block the UI with network
	 * operations.
	 * 
	 * @param serviceUrl
	 * @param username
	 * @param password
	 * @param appName
	 * @return A valid {@link LiveBeansModel} model, or <code>null</code> if
	 * connection has failed
	 * @throws CoreException
	 */
	public static LiveBeansModel connectToModel(final String serviceUrl, final String username, final String password,
			final String appName) throws CoreException {
		return connectToModel(serviceUrl, username, password, appName, null);
	}
	
	/**
	 * This method will attempt to create a {@link JMXConnector} from the given
	 * parameters and will close it when it is finished. If the connection has
	 * failed, clients may capture the thrown {@link CoreException} and inform
	 * the user. This method is UI safe, and will not block the UI with network
	 * operations.
	 * 
	 * @param serviceUrl
	 * @param username
	 * @param password
	 * @param appName
	 * @param project
	 * @return A valid {@link LiveBeansModel} model, or <code>null</code> if
	 * connection has failed
	 * @throws CoreException
	 */
	public static LiveBeansModel connectToModel(final String serviceUrl, final String username, final String password,
			final String appName, final IProject project) throws CoreException {
		final CountDownLatch latch = new CountDownLatch(1);
		final LiveBeansModel[] result = new LiveBeansModel[1];
		final CoreException[] status = new CoreException[1];

		Job jmxOperation = new Job("Executing Server Command") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				JMXConnector connector = null;
				try {
					connector = setupConnector(serviceUrl, username, password);
					result[0] = connectToModel(connector, new LiveBeansSession(serviceUrl, username, password, appName, project));
				}
				catch (CoreException e) {
					status[0] = e;
				}
				finally {
					latch.countDown();
					if (connector != null) {
						try {
							connector.close();
						}
						catch (IOException e) {
							StatusHandler.log(new Status(IStatus.ERROR, LiveGraphUiPlugin.PLUGIN_ID,
									"An error occurred while closing connection to server.", e));
						}
					}
				}
				return Status.OK_STATUS;
			}
		};
		jmxOperation.schedule();

		try {
			if (latch.await(30, TimeUnit.SECONDS)) {
				if (status[0] != null) {
					throw status[0];
				}
				return result[0];
			}
		}
		catch (InterruptedException e) {
			// swallowed
		}
		return null;
	}

	private static LiveBeansModel generateModel(LiveBeansViewMBean mbean, LiveBeansSession session)
			throws CoreException {
		try {
			if (mbean != null) {
				String json = mbean.getSnapshotAsJson();
				LiveBeansJsonParser parser = new LiveBeansJsonParser(session, json);
				LiveBeansModel model = parser.parse();
				// add model to collection
				LiveBeansModelCollection.getInstance().addModel(model);
				return model;
			}
		}
		catch (JSONException e) {
			throw new CoreException(new Status(IStatus.ERROR, LiveGraphUiPlugin.PLUGIN_ID,
					"An error occurred while generating graph model.", e));
		}
		return null;
	}

	/**
	 * This method will attempt to produce an up to date {@link LiveBeansModel}
	 * from the connection information in the given model. Will return the
	 * original model if there is a failure.
	 * 
	 * @param originalModel
	 * @return {@link LiveBeansModel}
	 * @throws CoreException
	 */
	public static LiveBeansModel refreshModel(LiveBeansModel originalModel) throws CoreException {
		LiveBeansSession session = originalModel.getSession();
		if (session != null) {
			LiveBeansModel model = connectToModel(session.getServiceUrl(), session.getUsername(),
					session.getPassword(), session.getApplicationName(), session.getProject());
			if (model != null) {
				return model;
			}
		}
		return originalModel;
	}

	private static JMXConnector setupConnector(String serviceUrl, String username, String password)
			throws CoreException {
		try {
			if (serviceUrl != null && serviceUrl.length() > 0) {
				Map env = new HashMap();
				if (username != null && password != null) {
					String[] creds = new String[] { username, password };
					env.put(JMXConnector.CREDENTIALS, creds);
				}
				return JMXConnectorFactory.connect(new JMXServiceURL(serviceUrl), env);
			}
		}
		catch (MalformedURLException e) {
			throw new CoreException(new Status(IStatus.ERROR, LiveGraphUiPlugin.PLUGIN_ID,
					"An error occurred while connecting to server. Please check that the service URL is correct.", e));
		}
		catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, LiveGraphUiPlugin.PLUGIN_ID,
					"An error occurred while connecting to server.", e));
		}
		return null;
	}

}
