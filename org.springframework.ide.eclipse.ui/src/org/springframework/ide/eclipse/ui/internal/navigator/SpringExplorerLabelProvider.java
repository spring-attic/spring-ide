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
package org.springframework.ide.eclipse.ui.internal.navigator;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;
import org.springframework.ide.eclipse.core.model.ISpringProject;
import org.springframework.ide.eclipse.ui.SpringUILabelProvider;

/**
 * This {@link ICommonLabelProvider} knows about the Spring projects.
 * 
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class SpringExplorerLabelProvider extends SpringUILabelProvider
		implements ICommonLabelProvider {

	public SpringExplorerLabelProvider() {
		super(true);
	}

	public String getDescription(Object element) {
		if (element instanceof ISpringProject) {
			return ((ISpringProject) element).getElementName();
		}
		return null;
	}

	public void init(ICommonContentExtensionSite config) {
	}

	public void restoreState(IMemento memento) {
	}

	public void saveState(IMemento memento) {
	}
}
