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
package org.springframework.ide.eclipse.beans.ui.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
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
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * This content provider is used to display a tree of non-java resources.
 * 
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class NonJavaResourceContentProvider implements ITreeContentProvider {

	private static final Path JVM_CLASSPATH_CONTAINER = new Path(
			"org.eclipse.jdt.launching.JRE_CONTAINER");

	public static final Object[] NO_CHILDREN = new Object[0];

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
				if (JdtUtils.isJavaProject(project)) {
					element = JavaCore.create(project);
				}
			}
			if (element instanceof IJavaProject) {
				return getPackageFragmentRoots((IJavaProject) element);
			}
			if (element instanceof IProject) {
				return getResources((IProject) element);
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
		}
		catch (JavaModelException e) {
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
			}
			catch (JavaModelException e) {
				return true;
			}
		}
		Object[] children = getChildren(element);
		return (children != null) && children.length > 0;
	}

	public Object getParent(Object element) {
		if (!exists(element)) {
			return null;
		}
		return internalGetParent(element);
	}

	private Object[] getPackageFragments(IPackageFragmentRoot root) throws JavaModelException {
		Object[] fragments = root.getChildren();
		Object[] nonJavaResources = getNonJavaResources(root);
		if (nonJavaResources == null) {
			return fragments;
		}
		return concatenate(fragments, nonJavaResources);
	}

	private Object[] getNonJavaResources(IPackageFragmentRoot root) throws JavaModelException {
		Object[] nonJavaResources = root.getNonJavaResources();

		// Replace JAR entries with our own wrapper
		if (root.getKind() == IPackageFragmentRoot.K_BINARY && root.getResource() instanceof IFile) {
			for (int i = 0; i < nonJavaResources.length; i++) {
				Object resource = nonJavaResources[i];
				if (resource instanceof IStorage) {
					IStorage storage = (IStorage) resource;
					nonJavaResources[i] = new ZipEntryStorage((IFile) root.getResource(), storage
							.getFullPath().toString());
				}
			}
		}
		return nonJavaResources;
	}

	protected Object[] getPackageFragmentRoots(IJavaProject project) throws JavaModelException {
		if (!project.getProject().isOpen()) {
			return NO_CHILDREN;
		}

		List<IJavaElement> list = new ArrayList<IJavaElement>();
		for (IPackageFragmentRoot root : project.getPackageFragmentRoots()) {
			if (!JVM_CLASSPATH_CONTAINER.equals(root.getRawClasspathEntry().getPath())) {
				if (isProjectPackageFragmentRoot(root)) {
					for (IJavaElement element : root.getChildren()) {
						list.add(element);
					}
				}
				else if (hasChildren(root)) {
					list.add(root);
				}
			}
		}
		return concatenate(list.toArray(), project.getNonJavaResources());
	}

	protected Object[] getJavaProjects(IJavaModel model) throws JavaModelException {
		return model.getJavaProjects();
	}

	private Object[] getPackageContents(IPackageFragment fragment) throws JavaModelException {
		return getNonJavaResources(fragment);
	}

	private Object[] getNonJavaResources(IPackageFragment fragment) throws JavaModelException {
		Object[] nonJavaResources = fragment.getNonJavaResources();
		IPackageFragmentRoot root = (IPackageFragmentRoot) fragment
				.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
		if (root != null && root.getKind() == IPackageFragmentRoot.K_BINARY) {
			for (int i = 0; i < nonJavaResources.length; i++) {
				Object resource = nonJavaResources[i];
				if (resource instanceof IFile) {
					nonJavaResources[i] = resource;
				}
				else if (resource instanceof IStorage && root.getResource() instanceof IFile) {
					IStorage storage = (IStorage) resource;
					nonJavaResources[i] = new ZipEntryStorage((IFile) root.getResource(), storage
							.getFullPath().toString());
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
			List<Object> nonJavaResources = new ArrayList<Object>();

			// Can be on classpath but as a member of non-java resource folder
			for (IResource member : members) {

				// A resource can also be a java element in the case of
				// exclusion and inclusion filters. We therefore exclude Java
				// elements from the list of non-Java resources.
				if (isFolderOnClasspath) {
					if (javaProject.findPackageFragmentRoot(member.getFullPath()) == null) {
						nonJavaResources.add(member);
					}
				}
				else if (!javaProject.isOnClasspath(member)) {
					nonJavaResources.add(member);
				}
			}
			return nonJavaResources.toArray();
		}
		catch (CoreException e) {
			return NO_CHILDREN;
		}
	}

	protected boolean isInternalLibrary(IJavaProject project, IPackageFragmentRoot root) {
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

	protected boolean isClasspathChange(IJavaElementDelta delta) {
		// need to test the flags only for package fragment roots
		if (delta.getElement().getElementType() != IJavaElement.PACKAGE_FRAGMENT_ROOT) {
			return false;
		}

		int flags = delta.getFlags();
		return (delta.getKind() == IJavaElementDelta.CHANGED
				&& ((flags & IJavaElementDelta.F_ADDED_TO_CLASSPATH) != 0)
				|| ((flags & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) != 0) || ((flags & IJavaElementDelta.F_REORDER) != 0));
	}

	protected Object skipProjectPackageFragmentRoot(IPackageFragmentRoot root) {
		if (isProjectPackageFragmentRoot(root)) {
			return root.getParent();
		}
		return root;
	}

	protected boolean isPackageFragmentEmpty(IJavaElement element) throws JavaModelException {
		if (element instanceof IPackageFragment) {
			IPackageFragment fragment = (IPackageFragment) element;
			if (fragment.exists()
					&& !(fragment.hasChildren() || fragment.getNonJavaResources().length > 0)
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
		}
		else if (element instanceof IJavaElement) {
			IJavaElement parent = ((IJavaElement) element).getParent();

			// For package fragments that are contained in a project package
			// fragment we have to skip the package fragment root as the parent.
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
