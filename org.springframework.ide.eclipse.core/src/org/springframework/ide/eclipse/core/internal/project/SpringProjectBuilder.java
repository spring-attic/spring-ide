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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.project.ProjectBuilderDefinition;
import org.springframework.ide.eclipse.core.project.ProjectBuilderDefinitionFactory;

/**
 * Incremental project builder which implements the Strategy GOF pattern. For
 * every modified file within a Spring project all implementations of the
 * interface
 * <code>org.springframework.ide.eclipse.core.project.IProjectBuilder</code>
 * provided via the extension point
 * <code>org.springframework.ide.eclipse.core.builders</code> are called.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class SpringProjectBuilder extends IncrementalProjectBuilder {

	@Override
	protected final IProject[] build(int kind, Map args,
			IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		IResourceDelta delta = (kind != FULL_BUILD ? getDelta(project) : null);
		if (delta == null || kind == FULL_BUILD) {
			if (SpringCoreUtils.isSpringProject(project)) {
				project.accept(new Visitor(monitor));
			}
		}
		else {
			delta.accept(new DeltaVisitor(monitor));
		}
		return null;
	}

	private class Visitor implements IResourceVisitor {
		private IProgressMonitor monitor;

		public Visitor(IProgressMonitor monitor) {
			this.monitor = monitor;
		}

		public boolean visit(IResource resource) {
			if (resource instanceof IFile) {
				runBuilders((IFile) resource, monitor);
			}
			return true;
		}
	}

	private class DeltaVisitor implements IResourceDeltaVisitor {
		private IProgressMonitor monitor;

		public DeltaVisitor(IProgressMonitor monitor) {
			this.monitor = monitor;
		}

		public boolean visit(IResourceDelta aDelta) {
			boolean visitChildren = false;

			IResource resource = aDelta.getResource();
			if (resource instanceof IProject) {

				// Only check projects with Spring beans nature
				visitChildren = SpringCoreUtils.isSpringProject(resource);
			}
			else if (resource instanceof IFolder) {
				visitChildren = true;
			}
			else if (resource instanceof IFile) {
				switch (aDelta.getKind()) {
				case IResourceDelta.ADDED:
				case IResourceDelta.CHANGED:
					if (resource instanceof IFile) {
						runBuilders((IFile) resource, monitor);
					}
					visitChildren = true;
					break;

				case IResourceDelta.REMOVED:
					break;
				}
			}
			return visitChildren;
		}
	}

	private void runBuilders(final IFile file, final IProgressMonitor monitor) {
		for (final ProjectBuilderDefinition builderHolder : ProjectBuilderDefinitionFactory
				.getProjectBuilderDefinitions()) {
			ISafeRunnable code = new ISafeRunnable() {
				public void run() throws Exception {
					SubProgressMonitor subMonitor = new SubProgressMonitor(
							monitor, 1);
					builderHolder.getProjectBuilder().cleanup(file, monitor);
					if (builderHolder.isEnabled(file.getProject())) {
						builderHolder.getProjectBuilder().build(file,
								subMonitor);
					}
				}

				public void handleException(Throwable e) {
					// nothing to do - exception is already logged
				}
			};
			SafeRunner.run(code);
		}
	}
}
