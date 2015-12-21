/*******************************************************************************
 * Copyright (c) 2007, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.workingsets;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.navigator.BeansNavigatorContentProvider;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISpringProject;

/**
 * Simple extension of the {@link BeansNavigatorContentProvider} that prevents
 * children for {@link IBeansConfigSet} and {@link IFile} instances.
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeansWorkingSetContentProvider extends
		BeansNavigatorContentProvider implements ITreeContentProvider {

	@Override
	public Object getParent(Object element) {
		if (element instanceof IBeansProject) {
			return SpringCore.getModel().getProject(
					((IBeansProject) element).getProject());
		}

		return super.getParent(element);
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IBeansConfigSet) {
			return IModelElement.NO_CHILDREN;
		}
		else if (parentElement instanceof IFile) {
			return IModelElement.NO_CHILDREN;
		}
		else if (parentElement instanceof IBeansModelElement) {
			return super.getChildren(parentElement);
		}
		else if (parentElement instanceof ISpringProject) {
			return super.getChildren(parentElement);
		}
		else {
			return IModelElement.NO_CHILDREN;
		}
	}
}
