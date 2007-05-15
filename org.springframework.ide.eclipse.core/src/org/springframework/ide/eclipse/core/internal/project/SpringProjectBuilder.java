/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.internal.project;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.springframework.ide.eclipse.core.project.ProjectBuilderDefinition;
import org.springframework.ide.eclipse.core.project.ProjectBuilderDefinitionFactory;

/**
 * Incremental project builder which implements the Strategy GOF pattern. For
 * every modified file within a Spring project all implementations of the
 * interface
 * {@link org.springframework.ide.eclipse.core.project.IProjectBuilder} provided
 * via the extension point
 * <code>org.springframework.ide.eclipse.core.builders</code> are called.
 * 
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class SpringProjectBuilder extends IncrementalProjectBuilder {

	protected final IProject[] build(final int kind, Map args,
			final IProgressMonitor monitor) throws CoreException {
		final IProject project = getProject();
		final IResourceDelta delta = getDelta(project);
		for (final ProjectBuilderDefinition builderDefinition
				: ProjectBuilderDefinitionFactory
						.getProjectBuilderDefinitions()) {
			if (builderDefinition.isEnabled(project)) {
				runBuilder(builderDefinition, project, kind, delta, monitor);
			}
		}
		return null;
	}

	private void runBuilder(final ProjectBuilderDefinition builderDefinition,
			final IProject project, final int kind, final IResourceDelta delta,
			final IProgressMonitor monitor) {
		ISafeRunnable code = new ISafeRunnable() {
			public void run() throws Exception {
				SubProgressMonitor subMonitor = new SubProgressMonitor(monitor,
						1);
				subMonitor = new SubProgressMonitor(monitor, 1);
				builderDefinition.getProjectBuilder().build(project, kind,
						delta, subMonitor);
			}

			public void handleException(Throwable e) {
				// nothing to do - exception is already logged
			}
		};
		SafeRunner.run(code);
	}
}
