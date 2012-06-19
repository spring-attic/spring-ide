/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.metadata.ui;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.navigator.IDescriptionProvider;
import org.springframework.ide.eclipse.beans.core.metadata.model.AbstractAnnotationMetadata;
import org.springframework.ide.eclipse.beans.core.metadata.model.IBeanMetadata;
import org.springframework.ide.eclipse.beans.core.metadata.model.IMethodMetadata;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelImages;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelProvider;
import org.springframework.ide.eclipse.beans.ui.model.metadata.BeanMetadataNode;
import org.springframework.ide.eclipse.beans.ui.model.metadata.BeanMetadataReference;
import org.springframework.ide.eclipse.beans.ui.model.metadata.IBeanMetadataLabelProvider;
import org.springframework.ide.eclipse.beans.ui.namespaces.INamespaceLabelProvider;
import org.springframework.ide.eclipse.beans.ui.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.metadata.MetadataUIImages;
import org.springframework.ide.eclipse.metadata.core.AspectAnnotationMetadata;
import org.springframework.ide.eclipse.metadata.core.TransactionalAnnotationMetadata;


/**
 * {@link IBeanMetadataLabelProvider} that knows about the different annotation {@link IBeanMetadata} and
 * {@link IMethodMetadata}.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 1.0.0
 */
public class AnnotationMetadataLabelProvider extends LabelProvider implements IBeanMetadataLabelProvider {

	public String getDescription(Object element) {
		if (element instanceof BeanMetadataReference) {
			String key = ((BeanMetadataReference) element).getKey();
			return Messages.AnnotationMetadataLabelProvider_DESCRIPTION_STEREOTYPE_ANNOTATION_GROUPING + key;
		}
		else if (element instanceof AbstractAnnotationMetadata) {
			INamespaceLabelProvider provider = NamespaceUtils.getLabelProvider(((AbstractAnnotationMetadata) element)
					.getBean());
			if (provider != null && provider instanceof IDescriptionProvider) {
				return ((IDescriptionProvider) provider).getDescription(((AbstractAnnotationMetadata) element)
						.getBean());
			}
			else {
				return BeansModelLabelProvider.DEFAULT_NAMESPACE_LABEL_PROVIDER
						.getDescription(((AbstractAnnotationMetadata) element).getBean());
			}
		}
		return null;
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof RequestMappingBeanMetadataReference) {
			return MetadataUIImages.getImage(MetadataUIImages.IMG_OBJS_REQUEST_MAPPING);
		}
		else if (element instanceof AbstractAnnotationMetadata) {
			return BeansModelImages.getImage(((AbstractAnnotationMetadata) element).getBean());
		}
		else if (element instanceof BeanMetadataReference) {
			Object child = ((BeanMetadataReference) element).firstChild();
			if (child instanceof TransactionalAnnotationMetadata) {
				return MetadataUIImages.getImage(MetadataUIImages.IMG_OBJS_TX);
			}
			else if (child instanceof AspectAnnotationMetadata) {
				return MetadataUIImages.getImage(MetadataUIImages.IMG_OBJS_ASPECT);
			}
			return MetadataUIImages.getImage(MetadataUIImages.IMG_OBJS_ANNOTATION);
		}
		return super.getImage(element);
	}

	@Override
	public String getText(Object element) {
		if (element instanceof BeanMetadataReference) {
			String key = ((BeanMetadataReference) element).getKey();
			int ix = key.lastIndexOf('.');
			if (ix > 0) {
				return key.substring(ix + 1) + " - " + key.substring(0, ix); //$NON-NLS-1$
			}
			else {
				return key;
			}
		}
		else if (element instanceof AbstractAnnotationMetadata) {
			INamespaceLabelProvider provider = NamespaceUtils.getLabelProvider(((AbstractAnnotationMetadata) element)
					.getBean());
			if (provider != null) {
				return provider.getText(((AbstractAnnotationMetadata) element).getBean(), null, true);
			}
			else {
				return BeansModelLabelProvider.DEFAULT_NAMESPACE_LABEL_PROVIDER.getText(
						((AbstractAnnotationMetadata) element).getBean(), null, true);
			}
		}
		return super.getText(element);
	}

	public boolean supports(Object object) {
		if (object instanceof AbstractAnnotationMetadata) {
			return true;
		}
		else if (object instanceof BeanMetadataReference) {
			return true;
		}
		else if (object instanceof BeanMetadataNode) {
			return true;
		}
		else if (object instanceof IBeanMetadata) {
			return true;
		}
		return false;
	}

}