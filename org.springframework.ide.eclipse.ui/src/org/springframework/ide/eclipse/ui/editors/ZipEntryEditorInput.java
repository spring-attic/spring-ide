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

package org.springframework.ide.eclipse.ui.editors;

import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;

/**
 * An <code>IEditorInput</code> for a <code>ZipEntryStorage</code>.
 * @see ZipEntryStorage
 * @author Torsten Juergeleit
 */
public class ZipEntryEditorInput implements IStorageEditorInput {

	private ZipEntryStorage storage;

	public ZipEntryEditorInput(ZipEntryStorage storage) {
		this.storage = storage;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ZipEntryEditorInput)) {
			return false;
		}
		ZipEntryEditorInput other = (ZipEntryEditorInput) obj;
		return storage.equals(other.storage);
	}

	public int hashCode() {
		return storage.hashCode();
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getName() {
		return storage.getName();
	}

	public String getToolTipText() {
		return storage.getFullPath().toString();
	}

	public ImageDescriptor getImageDescriptor() {
		IEditorRegistry registry = PlatformUI.getWorkbench()
				.getEditorRegistry();
		return registry.getImageDescriptor(storage.getFullPath()
				.getFileExtension());
	}

	public boolean exists() {
		// ZIP entries can't be deleted
		return true;
	}

	public Object getAdapter(Class adapter) {
		return storage.getAdapter(adapter);
	}

	public IStorage getStorage() {
		return storage;
	}
}
