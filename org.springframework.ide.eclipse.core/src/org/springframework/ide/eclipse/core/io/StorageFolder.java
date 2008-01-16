/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.io;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.internal.resources.Container;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IPackageFragment;

/**
 * {@link IFolder} implementation that bridges the gap between an
 * {@link IPackageFragment} and {@link IResource} and the Eclipse
 * {@link IFolder} implementation.
 * <p>
 * Note: this class is not intended to be a fully-complaint {@link IFolder}
 * implementation. Only the required methods are implemented.
 * @author Christian Dupuis
 * @since 2.0.3
 */
@SuppressWarnings("restriction")
class StorageFolder extends Container implements IFolder {

	IPackageFragment packageFragment;

	private IResource parentResource;

	public StorageFolder(IResource parentResource,
			IPackageFragment packageFragment) {
		super(parentResource.getRawLocation(), (Workspace) ResourcesPlugin
				.getWorkspace());
		this.packageFragment = packageFragment;
		this.parentResource = parentResource;
	}

	@Override
	public int getType() {
		return 0;
	}

	public void create(boolean force, boolean local, IProgressMonitor monitor)
			throws CoreException {
	}

	public void create(int updateFlags, boolean local, IProgressMonitor monitor)
			throws CoreException {
	}

	public String getDefaultCharset(boolean checkImplicit) throws CoreException {
		return null;
	}

	@Override
	public IPath getRawLocation() {
		IPath path = parentResource.getRawLocation();
		String resourcePath = org.springframework.util.StringUtils.replace(
				packageFragment.getElementName(), ".", "/");
		return path.append(resourcePath);
	}

	@Override
	public IResource[] members() throws CoreException {

		Object[] nonJavaResources = packageFragment.getNonJavaResources();
		List<IResource> resources = new ArrayList<IResource>();

		// Replace JAR entries with our own wrapper
		for (int i = 0; i < nonJavaResources.length; i++) {
			Object resource = nonJavaResources[i];
			if (resource instanceof IStorage) {
				IStorage storage = (IStorage) resource;
				resources.add(new StorageFile(packageFragment,
						new ZipEntryStorage((IFile) parentResource, storage
								.getFullPath().toString())));
			}
		}

		return resources.toArray(new IResource[resources.size()]);
	}

}
