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
package org.springframework.ide.eclipse.data.metadata.ui;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.model.metadata.IBeanMetadataLabelProvider;

/**
 * Custom {@link IBeanMetadataLabelProvider label provider} to create labels for {@link RepositoriesBeanMetadata}.
 * 
 * @author Oliver Gierke
 */
public class RepositoriesBeanMetadataLabelProvider extends LabelProvider implements IBeanMetadataLabelProvider {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object arg0) {
		return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_BEAN);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object arg0) {

		RepositoriesBeanMetadata metadata = (RepositoriesBeanMetadata) arg0;
		return metadata.getValueAsText();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IDescriptionProvider#getDescription(java.lang.Object)
	 */
	public String getDescription(Object arg0) {
		return "Spring Data Repositories Label Description";
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.beans.ui.model.metadata.IBeanMetadataLabelProvider#supports(java.lang.Object)
	 */
	public boolean supports(Object object) {
		return object instanceof RepositoriesBeanMetadata;
	}
}
