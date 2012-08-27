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

import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.ide.eclipse.beans.ui.livegraph.LiveGraphUiPlugin;
import org.springframework.ide.eclipse.beans.ui.livegraph.remote.IRemoteLiveBeansModel;
import org.springsource.ide.eclipse.commons.core.StatusHandler;

/**
 * Loads an MBean exposed by the Spring Framework and generates a
 * {@link LiveBeansModel} from the information contained within.
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

				IRemoteLiveBeansModel remoteModel = (IRemoteLiveBeansModel) context.getBean("proxy");
				String name = remoteModel.getName();
				LiveBean remoteBean = new LiveBean(name);
				model.getBeans().add(remoteBean);
			}
		}
		catch (Exception e) {
			StatusHandler.log(new Status(IStatus.ERROR, LiveGraphUiPlugin.PLUGIN_ID,
					"An error occured while loading graph model.", e));
		}
		return model;
	}

}
