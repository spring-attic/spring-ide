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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.springframework.context.support.ClassPathXmlApplicationContext;
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

	public static LiveBeansModel generateModel() {
		LiveBeansModel model = new LiveBeansModel();
		try {
			LiveGraphUiPlugin plugin = LiveGraphUiPlugin.getDefault();
			URL jmxConfig = plugin.getBundle().getResource("jmx-client-config.xml");
			ClassLoader loader = plugin.getClass().getClassLoader();

			if (jmxConfig != null && loader != null) {
				ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext();
				context.setClassLoader(loader);
				context.setConfigLocation(jmxConfig.toString());
				context.refresh();

				LiveBeansViewMBean remoteModel = (LiveBeansViewMBean) context.getBean("proxy");
				String json = remoteModel.getSnapshotAsJson();

				// String name = remoteModel.getName();
				// LiveBean remoteBean = new LiveBean(name);
				// model.getBeans().add(remoteBean);
			}

			URL jsonFile = plugin.getBundle().getResource("json.txt");
			if (jsonFile != null) {
				InputStream stream = jsonFile.openConnection().getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
				StringBuilder builder = new StringBuilder();
				String output = "";
				while ((output = reader.readLine()) != null) {
					builder.append(output);
				}
				reader.close();
				Collection<LiveBean> collection = LiveBeansJsonParser.parse(builder.toString());
				model.getBeans().addAll(collection);
			}
		}
		catch (Exception e) {
			StatusHandler.log(new Status(IStatus.ERROR, LiveGraphUiPlugin.PLUGIN_ID,
					"An error occured while loading graph model.", e));
		}
		return model;
	}

}
