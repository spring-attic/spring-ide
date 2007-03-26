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

	@Override
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

	@Override
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
