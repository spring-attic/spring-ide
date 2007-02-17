/*
 * Copyright 2002-2007 the original author or authors.
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

	public int hashCode() {
		return ObjectUtils.nullSafeHashCode(storage);
	}
}
