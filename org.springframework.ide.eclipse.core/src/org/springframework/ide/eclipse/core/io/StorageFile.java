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

import org.eclipse.core.internal.resources.Resource;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IPackageFragment;
import org.springframework.util.ObjectUtils;

/**
 * {@link Resource} implementation that bridges the gap between an
 * {@link ZipEntryStorage} or {@link IStorage} and the Eclipse {@link IResource}
 * implementation.
 * <p>
 * Note: this class is not intended to be a fully-complaint {@link Resource}
 * implementation. Only the required methods are implemented.
 * @author Christian Dupuis
 * @since 2.0.3
 */
@SuppressWarnings("restriction")
class StorageFile extends Resource {

	private ZipEntryStorage entryStorage;

	public StorageFile(IPackageFragment packageFramgement,
			ZipEntryStorage entryStorage) {
		super(packageFramgement.getResource().getRawLocation(),
				(Workspace) ResourcesPlugin.getWorkspace());
		this.entryStorage = entryStorage;
	}

	@Override
	public int getType() {
		return 0;
	}

	@Override
	public IPath getRawLocation() {
		return new Path(entryStorage.getFile().getRawLocation().toString()
				+ entryStorage.getEntryName());

	}

	@Override
	public int hashCode() {
		return 32 * ObjectUtils.nullSafeHashCode(entryStorage);
	}

	@Override
	public boolean equals(Object target) {
		if (target instanceof StorageFile) {
			return ObjectUtils.nullSafeEquals(
					((StorageFile) target).entryStorage, this.entryStorage);
		}
		return false;
	}

	public ZipEntryStorage getStorage() {
		return entryStorage;
	}
	
}