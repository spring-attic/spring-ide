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
package org.springframework.ide.eclipse.core.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ObjectUtils;

/**
 * {@link Resource} implementation for Eclipse {@link IStorage storage} handles.
 * 
 * @author Torsten Juergeleit
 */
public class StorageResource extends AbstractResource implements IAdaptable {

	private IStorage storage;

	public StorageResource(IStorage storage) {
		this.storage = storage;
	}

	@Override
	public boolean exists() {
		return (storage != null);
	}

	public InputStream getInputStream() throws IOException {
		if (storage == null) {
			throw new FileNotFoundException("Storage not available");
		}
		try {
			return storage.getContents();
		} catch (CoreException e) {
			throw new IOException(e.getMessage());
		}
	}

	public String getDescription() {
		return "storage [" + (storage != null ? storage.getName() : "") + "]";
	}

	/**
	 * Adapts to {@link IStorage}.
	 */
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IStorage.class)) {
			return storage;
		}
		return storage.getAdapter(adapter);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof StorageResource)) {
			return false;
		}
		StorageResource that = (StorageResource) other;
		return ObjectUtils.nullSafeEquals(this.storage, that.storage);
	}

	@Override
	public int hashCode() {
		return ObjectUtils.nullSafeHashCode(storage);
	}
}
