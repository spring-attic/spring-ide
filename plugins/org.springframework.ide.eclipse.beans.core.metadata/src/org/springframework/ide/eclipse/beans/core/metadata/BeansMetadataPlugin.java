/*******************************************************************************
 * Copyright (c) 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.metadata;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.BundleContext;
import org.springframework.ide.eclipse.beans.core.metadata.internal.model.BeanMetadataModel;
import org.springframework.ide.eclipse.beans.core.metadata.model.IBeanMetadataModel;

/**
 * The activator class controls the plug-in life cycle
 * @author Christian Dupuis
 */
public class BeansMetadataPlugin extends Plugin {

	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.beans.core.metadata";

	private static BeansMetadataPlugin plugin;
	
	private BeanMetadataModel metadataModel;
	
	public BeansMetadataPlugin() {
		metadataModel = new BeanMetadataModel();
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		Job modelJob = new Job("Initializing Spring Tooling") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				metadataModel.start();
				return Status.OK_STATUS;
			}
		};
		// modelJob.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
		// modelJob.setSystem(true);
		modelJob.setPriority(Job.INTERACTIVE);
		modelJob.schedule();
	}

	public void stop(BundleContext context) throws Exception {
		metadataModel.stop();
		plugin = null;
		super.stop(context);
	}

	public static BeansMetadataPlugin getDefault() {
		return plugin;
	}
	
	public static final IBeanMetadataModel getMetadataModel() {
		return getDefault().metadataModel;
	}

}
