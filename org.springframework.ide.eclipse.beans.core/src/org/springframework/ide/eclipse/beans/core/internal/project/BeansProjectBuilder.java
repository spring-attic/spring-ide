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

package org.springframework.ide.eclipse.beans.core.internal.project;

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
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;

public abstract class BeansProjectBuilder extends IncrementalProjectBuilder {

	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
														  throws CoreException {   
		IResourceDelta delta = (kind != FULL_BUILD ? getDelta(getProject()) :
													 null);
		if (delta == null || kind == FULL_BUILD) {
			IProject project = getProject();
			if (BeansCoreUtils.isBeansProject(project)) {
			    project.accept(new Visitor(monitor));
			}
		} else {
			delta.accept(new DeltaVisitor(monitor));
		}
		return null;
   	}

	protected abstract void buildFile(IFile file, IProgressMonitor monitor);

	private class Visitor implements IResourceVisitor {
		private IProgressMonitor monitor;
		
		public Visitor(IProgressMonitor monitor) {
			this.monitor = monitor;
		}

		public boolean visit(IResource resource) {
			if (resource instanceof IFile &&
									   BeansCoreUtils.isBeansConfig(resource)) {
				buildFile((IFile) resource, monitor);
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
				visitChildren = BeansCoreUtils.isBeansProject((IProject)
																	  resource);
			} else if (resource instanceof IFolder) {
				visitChildren = true;
			} else if (resource instanceof IFile) {
				switch (aDelta.getKind()) {
					case IResourceDelta.ADDED :
					case IResourceDelta.CHANGED :
						if (BeansCoreUtils.isBeansConfig(resource)) {
							buildFile((IFile) resource, monitor);
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
}
