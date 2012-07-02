/*******************************************************************************
 * Copyright (c) 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.data.beans.ui.model;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.beans.core.internal.model.BeanClassReferences;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.navigator.BeansNavigatorContentProvider;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.data.SpringDataUtils;

/**
 * Content provider to add "referenced by" elements to Spring Data repository interfaces.
 * 
 * @author Oliver Gierke
 */
public class RepositoriesModelContentProvider extends BeansNavigatorContentProvider {

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.beans.ui.navigator.BeansNavigatorContentProvider#hasChildren(java.lang.Object)
	 */
	@Override
	public boolean hasChildren(Object element) {

		if (super.hasChildren(element)) {
			return true;
		}

		if (!(element instanceof IType)) {
			return false;
		}

		IType type = (IType) element;
		IProject project = type.getJavaProject().getProject();

		return SpringDataUtils.hasRepositoryBeanFor(project, type);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.beans.ui.navigator.BeansNavigatorContentProvider#getJavaTypeChildren(org.eclipse.jdt.core.IType)
	 */
	@Override
	protected Object[] getJavaTypeChildren(IType type) {

		IProject project = type.getJavaProject().getProject();
		Set<IBean> beans = SpringDataUtils.getRepositoryBeansFor(project, type);

		if (!beans.isEmpty()) {
			return new Object[] { new BeanClassReferences(type, beans) };
		}

		return IModelElement.NO_CHILDREN;
	}
}
