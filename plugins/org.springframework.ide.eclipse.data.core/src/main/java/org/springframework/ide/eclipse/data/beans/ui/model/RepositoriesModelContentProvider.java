/*
 * Copyright 2011 by the original author(s).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
