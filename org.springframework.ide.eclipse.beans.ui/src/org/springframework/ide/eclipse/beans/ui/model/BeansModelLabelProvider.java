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
package org.springframework.ide.eclipse.beans.ui.model;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.beans.core.internal.model.BeanClassReferences;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.namespaces.DefaultNamespaceLabelProvider;
import org.springframework.ide.eclipse.beans.ui.namespaces.INamespaceLabelProvider;
import org.springframework.ide.eclipse.beans.ui.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.core.MarkerUtils;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.ide.eclipse.ui.viewers.DecoratingWorkbenchTreePathLabelProvider;

/**
 * This {@link ILabelProvider} knows about the beans core model's
 * {@link IModelElement}s.
 * 
 * @author Torsten Juergeleit
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
	protected int getSeverity(Object element, Object parentElement) {
		int severity = 0;
		if (element instanceof ISourceModelElement) {
			ISourceModelElement source = (ISourceModelElement) element;
			severity = MarkerUtils.getHighestSeverityFromMarkersInRange(source
					.getElementResource(), source.getElementStartLine(), source
					.getElementEndLine());
		}
		else if (element instanceof IResourceModelElement) {
			if (element instanceof IBeansProject) {
				for (IBeansConfig config : ((IBeansProject) element)
						.getConfigs()) {
					severity = MarkerUtils
							.getHighestSeverityFromMarkersInRange(config
									.getElementResource(), -1, -1);
					if (severity == IMarker.SEVERITY_ERROR) {
						break;
					}
				}
			}
			else if (element instanceof IBeansConfigSet) {
				for (IBeansConfig config : ((IBeansConfigSet) element)
						.getConfigs()) {
					severity = MarkerUtils
							.getHighestSeverityFromMarkersInRange(config
									.getElementResource(), -1, -1);
					if (severity == IMarker.SEVERITY_ERROR) {
						break;
					}
				}
			}
			else {
				severity = MarkerUtils.getHighestSeverityFromMarkersInRange(
						((IResourceModelElement) element).getElementResource(),
						-1, -1);
			}
		}
		else if (element instanceof IResource) {
			severity = MarkerUtils.getHighestSeverityFromMarkersInRange(
					(IResource) element, -1, -1);
		}
		else if (element instanceof ZipEntryStorage) {
			IResource resource = ((ZipEntryStorage) element).getFile();
			severity = MarkerUtils.getHighestSeverityFromMarkersInRange(
					resource, -1, -1);
		}
		return severity;
	}

	@Override
	protected Image getImage(Object element, Object parentElement,
			int severity) {
		Image image = null;
		if (element instanceof ISourceModelElement) {
			INamespaceLabelProvider provider = NamespaceUtils
					.getLabelProvider((ISourceModelElement) element);
			IModelElement context = (parentElement instanceof IModelElement
					? (IModelElement) parentElement : null);
			if (provider != null) {
				image = provider.getImage((ISourceModelElement) element,
						context);
			}
			else {
				image = DEFAULT_NAMESPACE_LABEL_PROVIDER.getImage(
						(ISourceModelElement) element, context);
			}
		}
		else if (element instanceof IModelElement) {
			if (parentElement instanceof IModelElement) {
				image = BeansModelImages.getImage((IModelElement) element,
						(IModelElement) parentElement);
			}
			else {
				image = BeansModelImages.getImage((IModelElement) element);
			}
		}
		else if (element instanceof ZipEntryStorage) {
			return super.getImage(((ZipEntryStorage) element).getFile(),
					parentElement, severity);
		}
		else if (element instanceof BeanClassReferences) {
			image = BeansUIImages.getImage(BeansUIImages.IMG_OBJS_REFERENCE);
		}
		if (image != null) {
			if (isDecorating()) {
				image = SpringUIUtils.getDecoratedImage(image, severity);
				image = PlatformUI.getWorkbench().getDecoratorManager()
             		.getLabelDecorator().decorateImage(image, element);
			}
			return image;
		}
		return super.getImage(element, parentElement, severity);
	}

	@Override
	protected String getText(Object element, Object parentElement,
			int severity) {
		if (element instanceof ISourceModelElement) {
			INamespaceLabelProvider provider = NamespaceUtils
					.getLabelProvider((ISourceModelElement) element);
			IModelElement context = (parentElement instanceof IModelElement
					? (IModelElement) parentElement : null);
			if (provider != null) {
				return provider.getText((ISourceModelElement) element, context);
			}
			else {
				return DEFAULT_NAMESPACE_LABEL_PROVIDER.getText(
						(ISourceModelElement) element, context);
			}
		}
		else if (element instanceof IModelElement) {
			return BeansModelLabels.getElementLabel((IModelElement) element, 0);
		}
		else if (element instanceof ZipEntryStorage) {
			ZipEntryStorage storage = (ZipEntryStorage) element;
			StringBuffer buf = new StringBuffer(storage.getFile()
					.getProjectRelativePath().toString());
			buf.append(" - " + storage.getFullPath().toString());
			return buf.toString();
		}
		else if (element instanceof BeanClassReferences) {
			return BeansUIPlugin.getResourceString("BeanClassReferences.label");
		}
		return super.getText(element, parentElement, severity);
	}
}
