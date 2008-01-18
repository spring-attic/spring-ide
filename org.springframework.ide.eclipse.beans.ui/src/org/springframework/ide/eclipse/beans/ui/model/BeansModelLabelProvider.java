/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.model;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.beans.core.internal.model.BeanClassReferences;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.namespaces.DefaultNamespaceLabelProvider;
import org.springframework.ide.eclipse.beans.ui.namespaces.INamespaceLabelProvider;
import org.springframework.ide.eclipse.beans.ui.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.ui.viewers.DecoratingWorkbenchTreePathLabelProvider;

/**
 * This {@link ILabelProvider} knows about the beans core model's
 * {@link IModelElement}s.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class BeansModelLabelProvider extends
		DecoratingWorkbenchTreePathLabelProvider {

	public static final DefaultNamespaceLabelProvider
		DEFAULT_NAMESPACE_LABEL_PROVIDER = new DefaultNamespaceLabelProvider();

	public BeansModelLabelProvider() {
		super(false);
	}

	public BeansModelLabelProvider(boolean isDecorating) {
		super(isDecorating);
	}

	@Override
	protected Image getImage(Object element, Object parentElement) {
		Image image = null;
		if (element instanceof ISourceModelElement) {
			INamespaceLabelProvider provider = NamespaceUtils
					.getLabelProvider((ISourceModelElement) element);
			IModelElement context = (parentElement instanceof IModelElement
					? (IModelElement) parentElement : null);
			if (provider != null) {
				image = provider.getImage((ISourceModelElement) element,
						context, isDecorating());
			}
			else {
				image = DEFAULT_NAMESPACE_LABEL_PROVIDER.getImage(
						(ISourceModelElement) element, context, isDecorating());
			}
		}
		else if (element instanceof IModelElement) {
			if (parentElement instanceof IModelElement) {
				image = BeansModelImages.getImage((IModelElement) element,
						(IModelElement) parentElement, isDecorating());
			}
			else {
				image = BeansModelImages.getImage((IModelElement) element);
			}
		}
		else if (element instanceof ZipEntryStorage) {
			return super.getImage(((ZipEntryStorage) element).getFile(),
					parentElement);
		}
		else if (element instanceof BeanClassReferences) {
			image = BeansUIImages.getImage(BeansUIImages.IMG_OBJS_REFERENCE);
		}
		if (image != null) {
			if (isDecorating()) {
				image = PlatformUI.getWorkbench().getDecoratorManager()
             		.getLabelDecorator().decorateImage(image, element);
			}
			return image;
		}
		return super.getImage(element, parentElement);
	}

	@Override
	protected String getText(Object element, Object parentElement) {
		if (element instanceof ISourceModelElement) {
			INamespaceLabelProvider provider = NamespaceUtils
					.getLabelProvider((ISourceModelElement) element);
			IModelElement context = (parentElement instanceof IModelElement
					? (IModelElement) parentElement : null);
			if (provider != null) {
				return provider.getText((ISourceModelElement) element, context, isDecorating());
			}
			else {
				return DEFAULT_NAMESPACE_LABEL_PROVIDER.getText(
						(ISourceModelElement) element, context, isDecorating());
			}
		}
		else if (element instanceof IModelElement) {
			return BeansModelLabels.getElementLabel((IModelElement) element, 0);
		}
		else if (element instanceof ZipEntryStorage) {
			// create zip entry label right here as it is not a core model element 
			ZipEntryStorage storage = (ZipEntryStorage) element;
			StringBuilder builder = new StringBuilder();
			builder.append(storage.getFullPath().lastSegment());
			builder.append(" - ");
			builder.append(storage.getFile().getProjectRelativePath().toString());
			builder.append("!");
			builder.append(storage.getFullPath().removeLastSegments(1).toString());
			return builder.toString();
		}
		else if (element instanceof BeanClassReferences) {
			return BeansUIPlugin.getResourceString("BeanClassReferences.label");
		}
		return super.getText(element, parentElement);
	}
}
