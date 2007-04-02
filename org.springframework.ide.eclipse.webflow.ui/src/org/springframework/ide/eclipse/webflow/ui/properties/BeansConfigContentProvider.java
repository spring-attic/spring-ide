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
package org.springframework.ide.eclipse.webflow.ui.properties;

import java.util.Set;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeansConfigContentProvider implements IStructuredContentProvider {

	private Set<IBeansConfig> configs;

	public BeansConfigContentProvider(Set<IBeansConfig> configs) {
		this.configs = configs;
	}

	public Object[] getElements(Object obj) {
		return configs.toArray();
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public void dispose() {
	}
}
