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
package org.springframework.ide.eclipse.maven.internal.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.project.IProjectBuilder;
import org.springframework.ide.eclipse.maven.MavenCorePlugin;


/**
 * {@link IProjectBuilder} that overcomes some limitations of the DataNucleus Enhancer.
 * <p>
 * This implementation will check if any of the incremental changes require a re-enhancing.
 * @author Christian Dupuis
 * @since 2.5.0
 */
@SuppressWarnings("restriction")
public class DataNucleusEnhancerProjectBuilder implements IProjectBuilder, InitializingBean {

	private static final String ENHANCER_JOB_CLASS_NAME = "org.datanucleus.ide.eclipse.jobs.EnhancerJob";

	private static final char[] ENTITY_BINARY_CLASS_NAME = "Ljavax/persistence/Entity;".toCharArray();

	private static final char[][] ENHANCED_BINARY_CLASS_NAMES = { "javax/jdo/spi/Detachable".toCharArray(),
			"javax/jdo/spi/PersistenceCapable".toCharArray() };

	private static boolean IS_DATANUCLEUS_PRESENT = isPresent();

	private static boolean isPresent() {
		try {
			Class.forName(ENHANCER_JOB_CLASS_NAME);
			return true;
		}
		catch (ClassNotFoundException e) {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void build(Set<IResource> affectedResources, int kind, IProgressMonitor monitor) throws CoreException {
		final Set<IJavaProject> javaProjects = new HashSet<IJavaProject>();
		for (IResource resource : affectedResources) {
			IJavaProject javaProject = JdtUtils.getJavaProject(resource);
			if (javaProject != null) {
				javaProjects.add(javaProject);
			}
		}

		for (IJavaProject javaProject : javaProjects) {
			try {
				// Reflectively load the DataNucleus project builder to prevent compile time dependency
				Class<?> enhancerJobClass = MavenCorePlugin.getDefault().getBundle().loadClass(ENHANCER_JOB_CLASS_NAME);
				Job enhancerJob = (Job) enhancerJobClass.getConstructor(IJavaProject.class).newInstance(javaProject);
				enhancerJob.setPriority(Job.SHORT);
				enhancerJob.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
				enhancerJob.addJobChangeListener(new JobChangeAdapter() {
					
					public void done(IJobChangeEvent event) {
						Job refreshJob = new Job("Update Resource Tree after enhance") {
							
							@Override
							protected IStatus run(IProgressMonitor monitor) {
								for (IJavaProject javaProject : javaProjects) {
									try {
										javaProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
									}
									catch (CoreException e) {
									}
								}
								return Status.OK_STATUS;
							}
						};;
						refreshJob.setPriority(Job.SHORT);
						refreshJob.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
						refreshJob.setSystem(true);
						refreshJob.schedule();
					}
					
				});
				enhancerJob.schedule();
			}
			catch (Exception e) {
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void cleanup(IResource resource, IProgressMonitor monitor) throws CoreException {
		// Nothing to clean up here
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<IResource> getAffectedResources(IResource resource, int kind, int deltaKind) throws CoreException {
		if (IS_DATANUCLEUS_PRESENT) {

			if (kind == IncrementalProjectBuilder.FULL_BUILD) {
				return Collections.singleton((IResource) resource.getProject());
			}
			else if (resource instanceof IFile && resource.getName().endsWith(".class") && resource.isAccessible()
					&& resource.isSynchronized(IResource.DEPTH_ZERO)) {
				// Check if the class has the @Entity annotation
				InputStream is = ((IFile) resource).getContents();
				try {
					ClassFileReader classFileReader = ClassFileReader.read(is, resource.getName());
					IBinaryAnnotation[] annotations = classFileReader.getAnnotations();
					if (annotations != null) {
						for (IBinaryAnnotation annotation : annotations) {
							if (CharOperation.equals(ENTITY_BINARY_CLASS_NAME, annotation.getTypeName())) {

								if (classFileReader.getInterfaceNames() != null) {
									for (char[] interfaceName : classFileReader.getInterfaceNames()) {
										for (char[] enhancedInterfaceName : ENHANCED_BINARY_CLASS_NAMES) {
											if (CharOperation.equals(interfaceName, enhancedInterfaceName)) {
												return Collections.emptySet();
											}
										}
									}
								}

								return Collections.singleton(resource);
							}
						}
					}
				}
				catch (ClassFormatException e) {
					// TODO CD add error handling
				}
				catch (IOException e) {
					// TODO CD add error handling
				}
				finally {
					if (is != null) {
						try {
							is.close();
						}
						catch (IOException e) {
							// TODO CD add error handling
						}
					}
				}
			}
		}
		return Collections.emptySet();
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		if (!IS_DATANUCLEUS_PRESENT) {
			throw new BeanCreationException("DataNucleus not available");
		}
	}

}
