/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.ui;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaApplicationLaunchShortcut;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.StructuredSelection;
import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;
import org.springsource.ide.eclipse.commons.frameworks.core.maintype.MainTypeFinder;


@SuppressWarnings("restriction")
public class BootLaunchShortcut extends JavaApplicationLaunchShortcut {
	
	/**
	 * Launch configuration id of the configs created by this shortcut.
	 */
	public static final String LAUNCH_CONFIG_TYPE_ID = 
			IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION;

	public static final String JMX_PORT_PROP = "com.sun.management.jmxremote.port";
	
	/**
	 * VM args that enable 'live bean graph' and jmx.
	 * @throws UnsupportedEncodingException 
	 */
	public static String liveBeanVmArgs(int jmxPort) {
		return liveBeanVmArgs(""+jmxPort);
	}
	
	public static String liveBeanVmArgs(String jmxPort) {
		return
				"-Dspring.liveBeansView.mbeanDomain\n" + //enable live beans construction
				"-Dcom.sun.management.jmxremote\n" + //enable jmx to access the beans
				"-D"+ JMX_PORT_PROP +"="+jmxPort + "\n" +
				"-Dcom.sun.management.jmxremote.authenticate=false\n" +
				"-Dcom.sun.management.jmxremote.ssl=false\n";
	}

	
			
	@Override
	protected IType[] findTypes(Object[] elements, IRunnableContext context)
			throws InterruptedException, CoreException {
		//For spring boot app, instead of searching for a main type in the entire project and all its
		// libraries... try to look inside the project's pom for the corresponding property.
		for (Object e : elements) {
			if (e instanceof IProject) {
				try {
					e = JavaCore.create((IProject)e);
				} catch (Throwable ignore) {
				}
			}
			if (e instanceof IJavaElement) {
				final IJavaProject jp = ((IJavaElement)e).getJavaProject();
				final IType[][] result = new IType[][] { null };
				try {
					context.run(/*fork*/false, /*true*/false, new IRunnableWithProgress() {
						@Override
						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							try {
								result[0] = MainTypeFinder.guessMainTypes(jp, monitor);
							} catch (CoreException e) {
								throw new InvocationTargetException(e);
							}
						}
					});
				} catch (InvocationTargetException exception) {
					throw ExceptionUtil.coreException(exception);
				}
				return result[0];
			}
		}
		//This isn't the best thing to to do as it searches also in all the library jars for main types. But it is
		// only a fallback option if the above code failed. (Or should we rather signal an error instead?)
		return super.findTypes(elements, context);
	}
	
	@Override
	protected ILaunchConfigurationType getConfigurationType() {
		return getLaunchManager().getLaunchConfigurationType(LAUNCH_CONFIG_TYPE_ID);		
	}

	/**
	 * Overridden, copied and changed to alter the generated launch configuration name.
	 */
	@Override
	protected ILaunchConfiguration createConfiguration(IType type) {
		ILaunchConfiguration config = null;
		ILaunchConfigurationWorkingCopy wc = null;
		try {
			int jmxPort = (int) (5000 + Math.random()*60000); //TODO: better way to pick this port?
			ILaunchConfigurationType configType = getConfigurationType();
			String projectName = type.getJavaProject().getElementName();
			wc = configType.newInstance(null, projectName+" - "+getLaunchManager().generateLaunchConfigurationName(
					type.getTypeQualifiedName('.')));
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, type.getFullyQualifiedName());
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, liveBeanVmArgs(jmxPort));
			wc.setMappedResources(new IResource[] {type.getUnderlyingResource()});
			config = wc.doSave();
		} catch (CoreException exception) {
			MessageDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchShell(), LauncherMessages.JavaLaunchShortcut_3, exception.getStatus().getMessage());
		}
		return config;
	}

	/**
	 * Returns the singleton launch manager.
	 *
	 * @return launch manager
	 */
	private ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	public static void launch(IProject project, String mode) {
		BootLaunchShortcut shortcut = new BootLaunchShortcut();
		StructuredSelection selection = new StructuredSelection(new Object[] {project});
		shortcut.launch(selection, mode);
	}

}
