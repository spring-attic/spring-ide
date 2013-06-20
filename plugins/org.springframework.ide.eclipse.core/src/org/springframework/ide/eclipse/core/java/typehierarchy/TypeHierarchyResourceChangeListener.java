/*******************************************************************************
 * Copyright (c) 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java.typehierarchy;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.springframework.ide.eclipse.core.SpringCore;

/**
 * @author Martin Lippert
 * @since 3.3.0
 */
public class TypeHierarchyResourceChangeListener implements IResourceChangeListener {

	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.PRE_BUILD) {
			try {
				event.getDelta().accept(new CacheResetVisitor());
			} catch (CoreException e) {
				SpringCore.log(e);
			}
		}
	}
	
	protected static class CacheResetVisitor implements IResourceDeltaVisitor {
		public boolean visit(IResourceDelta delta) {
			IResource res = delta.getResource();
			if (res instanceof IProject) {
				SpringCore.getTypeHierarchyEngine().clearCache((IProject) res);
				return false;
			}
			return true;
		}
	}

}
