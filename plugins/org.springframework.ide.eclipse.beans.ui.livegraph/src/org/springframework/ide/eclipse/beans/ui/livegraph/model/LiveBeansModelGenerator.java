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

	public static LiveBeansModel connectToModel(String serviceUrl, String appName) {
		JMXConnector connector = null;
		try {
			if (serviceUrl != null && serviceUrl.length() > 0 && appName != null && appName.length() > 0) {
				connector = JMXConnectorFactory.connect(new JMXServiceURL(serviceUrl));
				ObjectName name = ObjectName.getInstance("", "application", appName);
				MBeanServerConnection connection = connector.getMBeanServerConnection();
				LiveBeansViewMBean mbean = MBeanServerInvocationHandler.newProxyInstance(connection, name,
						LiveBeansViewMBean.class, false);
				return generateModel(mbean);
			}
		}
		catch (MalformedObjectNameException e) {
			StatusHandler.log(new Status(IStatus.ERROR, LiveGraphUiPlugin.PLUGIN_ID,
					"An error occurred while connecting to server. Please check that the application name is correct.",
					e));
		}
		catch (MalformedURLException e) {
			StatusHandler.log(new Status(IStatus.ERROR, LiveGraphUiPlugin.PLUGIN_ID,
					"An error occurred while connecting to server. Please check that the service URL is correct.", e));
		}
		catch (IOException e) {
			StatusHandler.log(new Status(IStatus.ERROR, LiveGraphUiPlugin.PLUGIN_ID,
					"An error occurred while connecting to server.", e));
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
		return new LiveBeansModel();
	}

	private static LiveBeansModel generateModel(LiveBeansViewMBean mbean) {
		LiveBeansModel model = new LiveBeansModel();
		try {
			if (mbean != null) {
				String json = mbean.getSnapshotAsJson();
				Collection<LiveBean> collection = LiveBeansJsonParser.parse(json);
				model.getBeans().addAll(collection);
			}
		}
		catch (JSONException e) {
			StatusHandler.log(new Status(IStatus.ERROR, LiveGraphUiPlugin.PLUGIN_ID,
					"An error occurred while generating graph model.", e));
		}
		return model;
	}

}
