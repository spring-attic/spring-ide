/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core.cli;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.springframework.ide.eclipse.boot.core.BootActivator;

public class BootGroovyScriptLaunchConfigurationDelegate extends LaunchConfigurationDelegate {
	
	public static final String ID = "org.springsource.ide.eclipse.boot.groovy.script.launch";
	
	/*
	  Example of a commandline invocation of the spring boot runtime. This is what we 
	  are to emulate in here:

	/usr/lib/jvm/java-7-oracle/bin/java 
		-cp 
		.:/home/kdvolder/Applications/spring-0.5.0.M6/bin:/home/kdvolder/Applications/spring-0.5.0.M6/lib/spring-boot-cli-0.5.0.M6.jar 
		org.springframework.boot.loader.JarLauncher
		run
		app.groovy

	*/

	private static final String SCRIPT_RSRC = "spring.groovy.script.rsrc";

	public static void setScript(ILaunchConfigurationWorkingCopy wc, IFile rsrc) {
		wc.setAttribute(SCRIPT_RSRC, rsrc.getFullPath().toString());
	}

	public static IFile getScript(ILaunchConfiguration conf) throws CoreException {
		String fullPathStr = conf.getAttribute(SCRIPT_RSRC, (String)null);
		if (fullPathStr!=null) {
			IPath fullPath = new Path(fullPathStr);
			Assert.isLegal(fullPath.segmentCount()>=2);
			String projectName = fullPath.segment(0);
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			return project.getFile(fullPath.removeFirstSegments(1));
		}
		return null;
	}
	
	public static String getProjectName(ILaunchConfiguration conf) throws CoreException {
		String fullPathStr = conf.getAttribute(SCRIPT_RSRC, (String)null);
		if (fullPathStr!=null) {
			IPath fullPath = new Path(fullPathStr);
			return fullPath.segment(0);
		}
		return null;
	}
	
	public static IProject getProject(ILaunchConfiguration conf) throws CoreException {
		String name = getProjectName(conf);
		if (name!=null) {
			return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		}
		return null;
	}

	public static File getSpringBootHome(ILaunchConfiguration conf) throws Exception {
		return getSpringBootInstall().getHome();
	}

	public static BootInstall getSpringBootInstall() throws Exception {
		return BootInstallManager.getInstance().getDefaultInstall();
	}

	public static String[] getSpringBootClasspath(ILaunchConfiguration conf) throws Exception {
		File[] libjars = getSpringBootInstall().getBootLibJars();
		ArrayList<String> classpath = new ArrayList<String>(libjars.length+1);
		classpath.add(".");
		for (File jarFile : libjars) {
			classpath.add(jarFile.toString());
		}
		return classpath.toArray(new String[classpath.size()]);
	}
	
	protected String getMainTypeName(ILaunchConfiguration conf) {
		return "org.springframework.boot.loader.JarLauncher";
	}
	
	protected void checkCancelled(IProgressMonitor monitor) throws CoreException {
		if (monitor.isCanceled()) {
			throw new CoreException(Status.CANCEL_STATUS);
		}
	}

	@Override
	public void launch(ILaunchConfiguration conf, String mode, 
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		//TODO: some common things that Java launch configs do that this one does not (yet) do but probably should
		//  - offer to save unsaved files
		//  - check for errors in project
		//  - source locators (for debugging processes)
		//  - launching in debug mode
		try {
			IFile scriptFile = getScript(conf);
			IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 5);
			checkCancelled(subMonitor);
			subMonitor.beginTask("Starting Spring Script: "+scriptFile.getName(), 5);
			subMonitor.worked(1);
			checkCancelled(subMonitor);
			subMonitor.subTask("Configuring launch parameters...");
	
			IVMRunner runner;
			IVMInstall vm = verifyVMInstall(conf);
			runner = vm.getVMRunner(mode);
	
			String mainTypeName = getMainTypeName(conf);
			IProject project = getProject(conf);
			String[] classpath = getSpringBootClasspath(conf);
	
			File springBootHome = getSpringBootHome(conf);
			System.out.println("Spring Boot Home = "+springBootHome);

			ArrayList<String> programArgs = new ArrayList<String>();
			programArgs.add("run");
			programArgs.add(scriptFile.getProjectRelativePath().toString());
			
			VMRunnerConfiguration runConfiguration = new VMRunnerConfiguration(mainTypeName, classpath);
			
			runConfiguration.setProgramArguments(programArgs.toArray(new String[programArgs.size()]));
//			runConfiguration.setVMArguments(vmArgs.toArray(new String[vmArgs.size()]));
			runConfiguration.setWorkingDirectory(project.getLocation().toFile().toString());
//			runConfiguration.setEnvironment(envp);
//			runConfiguration.setVMSpecificAttributesMap(vmAttributesMap);
			
			runner.run(runConfiguration, launch, monitor);
			
		}
		catch (Exception e) {
			BootActivator.log(e);
		}
	}



	protected IVMInstall verifyVMInstall(ILaunchConfiguration conf) {
		//Extremely simplistic implementation. Just gets the default JVM for this workspace.
		//TODO: project specific JVM selection or mayeb the JVM should be associated with
		// spring boot installation.
		return JavaRuntime.getDefaultVMInstall();
	}


}
