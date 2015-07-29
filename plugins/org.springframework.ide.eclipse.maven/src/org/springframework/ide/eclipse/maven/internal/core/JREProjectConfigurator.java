/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.maven.internal.core;

import java.util.LinkedHashMap;

import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.m2e.jdt.IClasspathDescriptor;
import org.eclipse.m2e.jdt.IClasspathDescriptor.EntryFilter;
import org.eclipse.m2e.jdt.IClasspathEntryDescriptor;
import org.eclipse.m2e.jdt.IJavaProjectConfigurator;
import org.springframework.ide.eclipse.maven.AbstractSpringProjectConfigurator;

/**
 * project configurator that maps the JRE level to a specific property setting,
 * if available (to allow projects to define a different JRE level than the
 * compiler target level).
 * 
 * Inspired by the m2e AbstractJavaProjectConfigurator.
 * 
 * @author Martin Lippert
 * @since 3.7.1
 */
public class JREProjectConfigurator extends AbstractSpringProjectConfigurator implements IJavaProjectConfigurator {

	protected static final LinkedHashMap<String, String> ENVIRONMENTS = new LinkedHashMap<String, String>();

	static {
		ENVIRONMENTS.put("1.6", "JavaSE-1.6"); //$NON-NLS-1$ //$NON-NLS-2$
		ENVIRONMENTS.put("1.7", "JavaSE-1.7"); //$NON-NLS-1$ //$NON-NLS-2$
		ENVIRONMENTS.put("1.8", "JavaSE-1.8"); //$NON-NLS-1$ //$NON-NLS-2$
		ENVIRONMENTS.put("1.9", "JavaSE-1.9"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected void doConfigure(MavenProject mavenProject, IProject project, ProjectConfigurationRequest request, IProgressMonitor monitor)
			throws CoreException {
	}
	
	@Override
	public void configureRawClasspath(ProjectConfigurationRequest configRequest, IClasspathDescriptor classpathDescriptor, IProgressMonitor progressMonitor) throws CoreException {
		String jreProperty = getJREConfigIfPresent(configRequest);

		if (jreProperty != null) {
			String environmentID = ENVIRONMENTS.get(jreProperty);
			if (environmentID == null) {
				if (ENVIRONMENTS.containsValue(jreProperty)) {
					environmentID = jreProperty;
				}
			}
			
			if (environmentID != null) {
				updateJREClasspathContainer(classpathDescriptor, environmentID);
			}
		}
	}

	protected String getJREConfigIfPresent(ProjectConfigurationRequest configRequest) {
		MavenProject mavenProject = configRequest.getMavenProject();
		Plugin plugin = mavenProject.getBuild().getPluginManagement().getPluginsAsMap().get("org.apache.maven.plugins:maven-compiler-plugin");
		if (plugin != null) {
			Object configuration = plugin.getConfiguration();
			if (configuration instanceof Xpp3Dom) {
				Xpp3Dom jrelevel = ((Xpp3Dom) configuration).getChild("java.classpath.jre");
				if (jrelevel != null) {
					String value = jrelevel.getValue();
					if (value != null) {
						return value;
					}
				}
			}
		}
		return null;
	}

	protected void updateJREClasspathContainer(IClasspathDescriptor classpathDescriptor, String environmentID) {
		IClasspathEntry cpe;
		IExecutionEnvironment executionEnvironment = getExecutionEnvironment(environmentID);
		if (executionEnvironment == null) {
			cpe = JavaRuntime.getDefaultJREContainerEntry();
		} else {
			IPath containerPath = JavaRuntime.newJREContainerPath(executionEnvironment);
			cpe = JavaCore.newContainerEntry(containerPath);
		}
		
		classpathDescriptor.replaceEntry(new EntryFilter() {
			@Override
			public boolean accept(IClasspathEntryDescriptor classpathDescriptor) {
				return JavaRuntime.JRE_CONTAINER.equals(classpathDescriptor.getPath().segment(0));
			}
		}, cpe);
	}

	private IExecutionEnvironment getExecutionEnvironment(String environmentId) {
		IExecutionEnvironmentsManager manager = JavaRuntime.getExecutionEnvironmentsManager();
		for (IExecutionEnvironment environment : manager.getExecutionEnvironments()) {
			if (environment.getId().equals(environmentId)) {
				return environment;
			}
		}
		return null;
	}

}
