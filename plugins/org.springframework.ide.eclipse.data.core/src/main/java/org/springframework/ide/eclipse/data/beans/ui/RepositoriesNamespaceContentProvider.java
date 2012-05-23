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
package org.springframework.ide.eclipse.data.beans.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.ide.eclipse.beans.core.metadata.model.IBeanMetadata;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.model.metadata.BeanMetadataReference;
import org.springframework.ide.eclipse.beans.ui.model.metadata.IBeanMetadataContentProvider;
import org.springframework.ide.eclipse.beans.ui.namespaces.DefaultNamespaceContentProvider;
import org.springframework.ide.eclipse.data.metadata.ui.RepositoriesBeanMetadata;

/**
 * Removes repository factory properties from the outline view.
 * 
 * @author Oliver Gierke
 */
public class RepositoriesNamespaceContentProvider extends DefaultNamespaceContentProvider implements
		IBeanMetadataContentProvider {

	private static final List<String> FILTER_PROPERTIES = Arrays.asList("repositoryInterface", "domainClass",
			"transactionManager");

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.beans.ui.namespaces.DefaultNamespaceContentProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement) {

		// Filter bean properties
		if (parentElement instanceof IBean) {
			return getFilteredProperties((IBean) parentElement).toArray();
		}

		if (parentElement instanceof RepositoriesBeanMetadata) {
			return getFilteredProperties(((RepositoriesBeanMetadata) parentElement).getValue()).toArray();
		}

		return super.getChildren(parentElement);
	}

	public List<IBeanProperty> getFilteredProperties(IBean bean) {

		List<IBeanProperty> filtered = new ArrayList<IBeanProperty>();

		for (Object child : super.getChildren(bean)) {

			if (child instanceof IBeanProperty) {

				IBeanProperty property = (IBeanProperty) child;
				if (!FILTER_PROPERTIES.contains(property.getElementName())) {
					filtered.add(property);
				}
			}
		}

		return filtered;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.beans.ui.model.metadata.IBeanMetadataContentProvider#getBeanMetadataReference(org.springframework.ide.eclipse.beans.core.metadata.model.IBeanMetadata, org.springframework.ide.eclipse.beans.core.model.IBeansProject)
	 */
	public BeanMetadataReference getBeanMetadataReference(IBeanMetadata metadata, IBeansProject project) {
		return new BeanMetadataReference(project, metadata.getKey());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.beans.ui.model.metadata.IBeanMetadataContentProvider#supports(java.lang.Object)
	 */
	public boolean supports(Object element) {
		return element instanceof RepositoriesBeanMetadata;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.beans.ui.namespaces.DefaultNamespaceContentProvider#hasChildren(java.lang.Object)
	 */
	@Override
	public boolean hasChildren(Object element) {
		return true;
	}
}
