/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.beans.ui.livegraph.model.LiveBeansModel;
import org.springframework.ide.eclipse.beans.ui.livegraph.model.LiveBeansModelGenerator;
import org.springframework.ide.eclipse.beans.ui.livegraph.views.LiveBeansGraphView;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.BootPropertyTester;
import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.actions.AbstractActionDelegate;
import org.springsource.ide.eclipse.commons.ui.launch.LaunchUtils;

public class OpenLiveBeansGraphAction extends AbstractActionDelegate {
	
	private static final String HOST = "127.0.0.1";

	@Override
	public void selectionChanged(IAction action, ISelection sel) {
		super.selectionChanged(action, sel);
		action.setEnabled(BootPropertyTester.isBootProject(getSelectedProject()));
	}

	/**
	 * @return The first selected project or null if no project is selected.
	 */
	private IProject getSelectedProject() {
		List<IProject> projects = getSelectedProjects();
		if (projects!=null && !projects.isEmpty()) {
			return projects.get(0);
		}
		return null;
	}

	@Override
	public void run(IAction action) {
		try {
			IProject project = getSelectedProject();
			connectToProject(project);
		} catch (Exception e) {
			BootActivator.log(e);
			MessageDialog.openError(getShell(), "Error", ExceptionUtil.getMessage(e)+"\n\n"
					+ "Check the error log for more details");
		}
	}

	/**
	 * Tries to open live beans view and connect it to a running process associated with the
	 * project.
	 */
	private void connectToProject(IProject project) {
		try {
			String serviceUrl = getServiceUrl(project);
			if (serviceUrl==null) {
				throw ExceptionUtil.coreException("Didn't find a JMX-enabled process for project '"+project.getName()+"'\n"+
						"To open the livebeans graph a process must be run with the following or similar VM arguments:\n\n"
						+ BootLaunchShortcut.liveBeanVmArgs("${jmxPort}")
				);
			}

			LiveBeansModel model = LiveBeansModelGenerator.connectToModel(serviceUrl, /*username*/null, /*password*/null, /*appName*/"", project);
			IViewPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView(LiveBeansGraphView.VIEW_ID);
			if (part instanceof LiveBeansGraphView) {
				((LiveBeansGraphView) part).setInput(model);
			}
		}
		catch (Exception e) {
			BootActivator.log(e);
			MessageDialog.openError(getShell(), "Error", ExceptionUtil.getMessage(e)+"\n\n"
					+ "Check the error log for more details");
		}
	}
	
	/**
	 * Determine JMX service url to be used to connect live bean graph on a running JMX-enabled process
	 * associated with given project.
	 * 
	 * @return url never null
	 * @throws CoreException with an explanation error message if url can not be determined.
	 */
	private String getServiceUrl(IProject project) throws CoreException {
		//The service url is derived from the jmxport property of an active launch associated 
		// with the property. Therefore, we look for a launch that is associated with project 
		// and sets the corresponding system property as one of its VMArguments.

		boolean hasActiveProcess = false; //set to true if at least one active process is found
										// used to create a better error message on failure.
		
		for (ILaunchConfiguration c : getLaunchConfigs(project)) {
			String jmxPortProp = getVMSystemProp(c, BootLaunchShortcut.JMX_PORT_PROP); 
			for (ILaunch l : LaunchUtils.getLaunches(c)) {
				if (!l.isTerminated()) {
					hasActiveProcess = true;
					
					if (jmxPortProp!=null) {
						//Looks like JMX is enabled via VM args.
						
						return "service:jmx:rmi:///jndi/rmi://" + HOST + ":" + jmxPortProp + "/jmxrmi";
					}
				}
			}
		}
		if (hasActiveProcess) {
			//There's a active process but looks like JMX arguments are missing
			throw ExceptionUtil.coreException("Didn't find a JMX-enabled process for project '"+project.getName()+"'\n"+
					"To open the livebeans graph a process must be run with the following VM arguments:\n\n"
					+ BootLaunchShortcut.liveBeanVmArgs("${jmxPort}")
			);
		} else {
			throw ExceptionUtil.coreException("No active process found for project '"+project.getName()+"'\n"+
					"The Live Bean Graph is created at runtime. The project must be running to open it.\n"+
					"Run your project with the 'Run As >> Spring Boot App' context menu then try opening the view again."
			);
		}
	}

	/**
	 * Extract VM argument list from launch config and look for -D<prop>=<value> arguments.
	 * @return the value corresponding to given system prop or null if this value is not found.
	 */
	private String getVMSystemProp(ILaunchConfiguration c, String propName) {
		try {
			String vmArgs = c.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, (String)null);
			if (vmArgs!=null) {
				String[] pieces = vmArgs.split("\\s+");
				String lookFor = "-D"+propName+"=";
				for (String piece : pieces) {
					if (piece.startsWith(lookFor)) {
						return piece.substring(lookFor.length());
					}
				}
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return null;
	}

	/**
	 * Collect the listing of {@link ILaunchConfiguration}s that apply to a given project.
	 */
	private static List<ILaunchConfiguration> getLaunchConfigs(IProject project) {
		String ctypeId = BootLaunchShortcut.LAUNCH_CONFIG_TYPE_ID;
		ILaunchConfigurationType ctype = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(ctypeId);
		List<ILaunchConfiguration> candidateConfigs = Collections.emptyList();
		try {
			ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(ctype);
			candidateConfigs = new ArrayList<ILaunchConfiguration>(configs.length);
			for (int i = 0; i < configs.length; i++) {
				ILaunchConfiguration config = configs[i];
				if (config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "").equals(project.getName())) { //$NON-NLS-1$
					candidateConfigs.add(config);
				}
			}
		} catch (CoreException e) {
			BootActivator.log(e);
		}
		return candidateConfigs;
	}
	

}
