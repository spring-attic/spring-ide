/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;

/**
 * This content provider is used to display a tree of non-java resources.
 * @author Torsten Juergeleit
 */
public class NonJavaResourceContentProvider implements ITreeContentProvider {

	protected static final Object[] NO_CHILDREN = new Object[0];
	
	public Object[] getElements(Object parent) {
		return getChildren(parent);
	}
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public void dispose() {
	}

	public Object[] getChildren(Object element) {
		if (!exists(element)) {
			return NO_CHILDREN;
		}
		try {
			if (element instanceof IProject) {
				IProject project = (IProject) element;
				if (SpringCoreUtils.isJavaProject(project)) {
					element = JavaCore.create(project);
				}
			}
			if (element instanceof IJavaProject) { 
				return getPackageFragmentRoots((IJavaProject) element);
			}
			if (element instanceof IPackageFragmentRoot) {
				return getPackageFragments((IPackageFragmentRoot) element);
			}
			if (element instanceof IPackageFragment) { 
				return getPackageContents((IPackageFragment) element);
			}
			if (element instanceof IFolder) {
				return getResources((IFolder) element);
			}
		} catch (JavaModelException e) {
			return NO_CHILDREN;
		}		
		return NO_CHILDREN;	
	}

	public boolean hasChildren(Object element) {
		if (element instanceof IProject) {
			IProject project = (IProject) element;
			if (!project.isOpen()) {
				return false;
			}
		}
		if (element instanceof IJavaProject) {
			IJavaProject javaProject = (IJavaProject) element;
			if (!javaProject.getProject().isOpen()) {
				return false;
			}	
		}
		if (element instanceof IParent) {
			try {
				// when we have Java children return true, else we fetch all
				// the children
				if (((IParent) element).hasChildren()) {
					return true;
				}
			} catch(JavaModelException e) {
				return true;
			}
		}
		Object[] children = getChildren(element);
		return (children != null) && children.length > 0;
	}

	public Object getParent(Object element) {
		if (!exists(element)){
			return null;
		}
		return internalGetParent(element);
	}

	private Object[] getPackageFragments(IPackageFragmentRoot root)
			throws JavaModelException {
		Object[] fragments = root.getChildren();
		Object[] nonJavaResources = getNonJavaResources(root);
		if (nonJavaResources == null) {
			return fragments;
		}
		return concatenate(fragments, nonJavaResources);
	}

	private Object[] getNonJavaResources(IPackageFragmentRoot root)
			throws JavaModelException {
		Object[] nonJavaResources = root.getNonJavaResources(); 
		if (root.getKind() == IPackageFragmentRoot.K_BINARY) {
			for (int i = 0; i < nonJavaResources.length; i++) {
				Object resource = nonJavaResources[i];
				if (resource instanceof IStorage) {
					IStorage storage = (IStorage) resource;
					nonJavaResources[i] = new ZipEntryStorage(root
							.getResource(), storage.getFullPath().toString());
				}
			}
		}
		return nonJavaResources;
	}

	protected Object[] getPackageFragmentRoots(IJavaProject project)
			throws JavaModelException {
		if (!project.getProject().isOpen()) {
			return NO_CHILDREN;
		}
		IPackageFragmentRoot[] roots = project.getPackageFragmentRoots();

		// filter out JARs not contained in the project itself and replace
		// package fragment roots that correspond to projects with the package
		// fragments directly
		List list = new ArrayList(roots.length);
		for (int i = 0; i < roots.length; i++) {
			IPackageFragmentRoot root = roots[i];
			if (isInternalLibrary(project, root)) {
				if (isProjectPackageFragmentRoot(root)) {
					Object[] children = root.getChildren();
					for (int k = 0; k < children.length; k++) {
						list.add(children[k]);
					}
				} else if (hasChildren(root)) {
					list.add(root);
				}
			}
		}
		return concatenate(list.toArray(), project.getNonJavaResources());
	}

	protected Object[] getJavaProjects(IJavaModel model)
			throws JavaModelException {
		return model.getJavaProjects();
	}

	private Object[] getPackageContents(IPackageFragment fragment)
			throws JavaModelException {
		return getNonJavaResources(fragment);
	}

	private Object[] getNonJavaResources(IPackageFragment fragment)
			throws JavaModelException {
		Object[] nonJavaResources = fragment.getNonJavaResources(); 
		IPackageFragmentRoot root = (IPackageFragmentRoot)
				fragment.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
		if (root != null && root.getKind() == IPackageFragmentRoot.K_BINARY) {
			for (int i = 0; i < nonJavaResources.length; i++) {
				Object resource = nonJavaResources[i];
				if (resource instanceof IStorage) {
					IStorage storage = (IStorage) resource;
					nonJavaResources[i] = new ZipEntryStorage(root
							.getResource(), storage.getFullPath().toString());
				}
			}
		}
		return nonJavaResources;
	}

	private Object[] getResources(IContainer container) {
		try {
			IResource[] members = container.members();
			IJavaProject javaProject = JavaCore.create(container.getProject());
			if (javaProject == null || !javaProject.exists()) {
				return members;
			}
			boolean isFolderOnClasspath = javaProject.isOnClasspath(container);
			List nonJavaResources = new ArrayList();
			// Can be on classpath but as a member of non-java resource folder
			for (int i = 0; i < members.length; i++) {
				IResource member = members[i];
				// A resource can also be a java element
				// in the case of exclusion and inclusion filters.
				// We therefore exclude Java elements from the list
				// of non-Java resources.
				if (isFolderOnClasspath) {
					if (javaProject.findPackageFragmentRoot(member
							.getFullPath()) == null) {
						nonJavaResources.add(member);
					}
				} else if (!javaProject.isOnClasspath(member)) {
					nonJavaResources.add(member);
				}
			}
			return nonJavaResources.toArray();
		} catch (CoreException e) {
			return NO_CHILDREN;
		}
	}

	protected boolean isInternalLibrary(IJavaProject project,
			IPackageFragmentRoot root) {
		if (root.isArchive()) {
			IResource resource = root.getResource();
			if (resource != null) {
				IProject jarProject = resource.getProject();
				IProject container = root.getJavaProject().getProject();
				return container.equals(jarProject);
			}
			return false;
		}
		return true;
	}
	
	protected boolean isClassPathChange(IJavaElementDelta delta) {
		// need to test the flags only for package fragment roots
		if (delta.getElement().getElementType() !=
				IJavaElement.PACKAGE_FRAGMENT_ROOT) {
			return false;
		}

		int flags = delta.getFlags();
		return (delta.getKind() == IJavaElementDelta.CHANGED
				&& ((flags & IJavaElementDelta.F_ADDED_TO_CLASSPATH) != 0)
				|| ((flags & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) != 0)
				|| ((flags & IJavaElementDelta.F_REORDER) != 0));
	}

	protected Object skipProjectPackageFragmentRoot(IPackageFragmentRoot root) {
		if (isProjectPackageFragmentRoot(root)) {
			return root.getParent();
		}
		return root;
	}

	protected boolean isPackageFragmentEmpty(IJavaElement element)
			throws JavaModelException {
		if (element instanceof IPackageFragment) {
			IPackageFragment fragment = (IPackageFragment) element;
			if (fragment.exists() &&
					!(fragment.hasChildren() ||
							fragment.getNonJavaResources().length > 0)
					&& fragment.hasSubpackages()) {
				return true;
			}
		}
		return false;
	}

	protected boolean isProjectPackageFragmentRoot(IPackageFragmentRoot root) {
		IResource resource = root.getResource();
		return (resource instanceof IProject);
	}
	
	protected boolean exists(Object element) {
		if (element == null) {
			return false;
		}
		if (element instanceof IResource) {
			return ((IResource) element).exists();
		}
		if (element instanceof IJavaElement) {
			return ((IJavaElement) element).exists();
		}
		return true;
	}
	
	protected Object internalGetParent(Object element) {

		// try to map resources to the containing package fragment
		if (element instanceof IResource) {
			IResource parent = ((IResource) element).getParent();
			IJavaElement jParent = JavaCore.create(parent);
			// http://bugs.eclipse.org/bugs/show_bug.cgi?id=31374
			if (jParent != null && jParent.exists()) {
				return jParent;
			}
			return parent;
		} else if (element instanceof IJavaElement) {
			IJavaElement parent = ((IJavaElement) element).getParent();
			// for package fragments that are contained in a project package
			// fragment
			// we have to skip the package fragment root as the parent.
			if (element instanceof IPackageFragment) {
				return skipProjectPackageFragmentRoot((IPackageFragmentRoot) parent);
			}
			return parent;
		}
		return null;
	}

	protected static Object[] concatenate(Object[] a1, Object[] a2) {
		int a1Len = a1.length;
		int a2Len = a2.length;
		Object[] res = new Object[a1Len + a2Len];
		System.arraycopy(a1, 0, res, 0, a1Len);
		System.arraycopy(a2, 0, res, a1Len, a2Len);
		return res;
	}
}
