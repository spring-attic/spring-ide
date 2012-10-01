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
import java.util.Collection;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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

	public static LiveBeansModel connectToModel(JMXConnector connector, String appName) {
		try {
			if (connector != null && appName != null && appName.length() > 0) {
				ObjectName name = ObjectName.getInstance("", "application", "/".concat(appName));
				MBeanServerConnection connection = connector.getMBeanServerConnection();
				LiveBeansViewMBean mbean = MBeanServerInvocationHandler.newProxyInstance(connection, name,
						LiveBeansViewMBean.class, false);
				return generateModel(mbean, appName);
			}
		}
		catch (MalformedObjectNameException e) {
			StatusHandler.log(new Status(IStatus.ERROR, LiveGraphUiPlugin.PLUGIN_ID,
					"An error occurred while connecting to server. Please check that the application name is correct.",
					e));
		}
		catch (IOException e) {
			StatusHandler.log(new Status(IStatus.ERROR, LiveGraphUiPlugin.PLUGIN_ID,
					"An error occurred while connecting to server.", e));
		}
		return new LiveBeansModel();
	}

	public static LiveBeansModel connectToModel(String serviceUrl, String appName) {
		return connectToModel(serviceUrl, null, null, appName);
	}

	public static LiveBeansModel connectToModel(String serviceUrl, String username, String password, String appName) {
		JMXConnector connector = null;
		try {
			connector = setupConnector(serviceUrl, username, password);
			return connectToModel(connector, appName);
		}
		finally {
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
	}

	private static LiveBeansModel generateModel(LiveBeansViewMBean mbean, String appName) {
		LiveBeansModel model = new LiveBeansModel();
		try {
			if (mbean != null) {
				String json = mbean.getSnapshotAsJson();
				Collection<LiveBean> collection = LiveBeansJsonParser.parse(json, appName);
				model.getBeans().addAll(collection);
			}
		}
		catch (JSONException e) {
			StatusHandler.log(new Status(IStatus.ERROR, LiveGraphUiPlugin.PLUGIN_ID,
					"An error occurred while generating graph model.", e));
		}
		return model;
	}

	private static JMXConnector setupConnector(String serviceUrl, String username, String password) {
		JMXConnector connector = null;
		try {
			if (serviceUrl != null && serviceUrl.length() > 0) {
				Map env = null;
				if (username != null && password != null) {
					String[] creds = new String[] { username, password };
					env.put(JMXConnector.CREDENTIALS, creds);
				}
				connector = JMXConnectorFactory.connect(new JMXServiceURL(serviceUrl), env);
			}
		}
		catch (MalformedURLException e) {
			StatusHandler.log(new Status(IStatus.ERROR, LiveGraphUiPlugin.PLUGIN_ID,
					"An error occurred while connecting to server. Please check that the service URL is correct.", e));
		}
		catch (IOException e) {
			StatusHandler.log(new Status(IStatus.ERROR, LiveGraphUiPlugin.PLUGIN_ID,
					"An error occurred while connecting to server.", e));
		}
		return connector;
	}

}
