/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.eclipse.core.internal.project;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.project.IProjectBuilder;

/**
 * Incremental project builder which implements the Strategy GOF pattern.
 * For every modified file within a Spring project all implementations of the
 * interface
 * <code>org.springframework.ide.eclipse.core.project.IProjectBuilder</code>
 * provided via the extension point
 * <code>org.springframework.ide.eclipse.core.builders</code> are called.
 * @see org.springframework.ide.eclipse.core.project.IProjectBuilder
 */
public class SpringProjectBuilder extends IncrementalProjectBuilder {

	public static final String BUILDERS_EXTENSION_POINT =
											SpringCore.PLUGIN_ID + ".builders"; 
	private List builders;

	public void setInitializationData(IConfigurationElement cfig,
					   String propertyName, Object data) throws CoreException {
		super.setInitializationData(cfig, propertyName, data);

		// Fill the list of the members of the <i>builders</i> extension-point
		builders = new ArrayList();		
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtension[] extensions = registry.getExtensionPoint(
									 BUILDERS_EXTENSION_POINT).getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			IExtension extension = extensions[i];
			IConfigurationElement[] elements =
										  extension.getConfigurationElements();
			for (int j = 0;  j < elements.length; j++) {
				try {
					IConfigurationElement element = elements[j];

					Object builder = element.createExecutableExtension("class");
					if (builder instanceof IProjectBuilder) {
						builders.add(builder);
					}
				} catch (CoreException e) {
					SpringCore.log(e);
				}
			}
		}				
	}

	protected final IProject[] build(int kind, Map args, IProgressMonitor monitor)
														  throws CoreException {   
		IProject project = getProject();
		IResourceDelta delta = (kind != FULL_BUILD ? getDelta(project) : null);
		if (delta == null || kind == FULL_BUILD) {
			if (SpringCoreUtils.isSpringProject(project)) {
			    project.accept(new Visitor(monitor));
			}
		} else {
			delta.accept(new DeltaVisitor(monitor));
		}
		return null;
   	}

	private class Visitor implements IResourceVisitor {
		private IProgressMonitor monitor;
		private List builders;
		
		public Visitor(IProgressMonitor monitor) {
			this.monitor = monitor;
		}

		public boolean visit(IResource resource) {
			if (resource instanceof IFile) {
				build((IFile) resource, monitor);
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
				visitChildren = SpringCoreUtils.isSpringProject((IProject) resource);
			} else if (resource instanceof IFolder) {
				visitChildren = true;
			} else if (resource instanceof IFile) {
				switch (aDelta.getKind()) {
					case IResourceDelta.ADDED :
					case IResourceDelta.CHANGED :
						if (resource instanceof IFile) {
							build((IFile) resource, monitor);
						}
						visitChildren = true;
						break;

					case IResourceDelta.REMOVED :
						break;
				}
			}
			return visitChildren;
		}
	}

	private void build(IFile file, IProgressMonitor monitor) {
		for (Iterator builders = this.builders.iterator();
														builders.hasNext(); ) {
			IProjectBuilder builder = (IProjectBuilder) builders.next();
			builder.build(file, monitor);
		}
	}
}
