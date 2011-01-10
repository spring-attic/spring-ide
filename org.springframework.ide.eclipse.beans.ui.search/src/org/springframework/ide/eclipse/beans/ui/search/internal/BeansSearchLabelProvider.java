/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.eclipse.beans.ui.search.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ILabelProvider;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelProvider;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * This {@link ILabelProvider}Êdelegates to the {@link BeansModelLabelProvider}and appends the {@link IModelElement}'s path to the label.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeansSearchLabelProvider extends BeansModelLabelProvider {

	public BeansSearchLabelProvider(boolean isDecorating) {
		super(isDecorating);
	}	

	@Override
	protected String getText(Object element, Object parentElement) {
		if (element instanceof IFile) {
			return ((IFile) element).getProjectRelativePath().toString();
		}
		return super.getText(element, parentElement);
	}
}
