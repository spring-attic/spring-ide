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
package org.springframework.ide.eclipse.beans.ui.navigator;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;
import org.eclipse.ui.navigator.IDescriptionProvider;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.metadata.IBeanMetadata;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUILabels;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelProvider;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabels;
import org.springframework.ide.eclipse.beans.ui.model.metadata.BeanMetadataReference;
import org.springframework.ide.eclipse.beans.ui.model.metadata.BeanMetadataUtils;
import org.springframework.ide.eclipse.beans.ui.namespaces.INamespaceLabelProvider;
import org.springframework.ide.eclipse.beans.ui.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.model.ILazyInitializedModelElement;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.ISpringProject;

/**
 * {@link ICommonLabelProvider} which knows about the beans core model's
 * {@link IModelElement elements}.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class BeansNavigatorLabelProvider extends BeansModelLabelProvider implements
		ICommonLabelProvider { 

	private String providerID;

	public BeansNavigatorLabelProvider() {
		super(true);
	}

	public BeansNavigatorLabelProvider(boolean isDecorating) {
		super(isDecorating);
	}

	public void init(ICommonContentExtensionSite config) {
		providerID = config.getExtension().getId();
	}

	public void restoreState(IMemento memento) {
	}

	public void saveState(IMemento memento) {
	}

	public String getDescription(Object element) {
		if (element instanceof IBeanMetadata
				&& BeanMetadataUtils.getLabelProvider((IBeanMetadata) element) != null) {
			return BeanMetadataUtils.getLabelProvider((IBeanMetadata) element).getDescription(
					element);
		}
		else if (element instanceof IBeansProject) {
			return "Beans" // TODO Externalize string
					+ " - " + ((IBeansProject) element).getProject().getName();
		}
		else if (element instanceof ISourceModelElement) {
			INamespaceLabelProvider provider = NamespaceUtils
					.getLabelProvider((ISourceModelElement) element);
			if (provider != null && provider instanceof IDescriptionProvider) {
				return ((IDescriptionProvider) provider).getDescription(element);
			}
			else {
				return DEFAULT_NAMESPACE_LABEL_PROVIDER.getDescription(element);
			}
		}
		else if (element instanceof IModelElement) {
			return BeansModelLabels.getElementLabel((IModelElement) element,
					BeansUILabels.APPEND_PATH | BeansUILabels.DESCRIPTION);
		}
		if (element instanceof IFile) {
			IBeansConfig config = BeansCorePlugin.getModel().getConfig((IFile) element);
			if (config != null) {
				return BeansModelLabels.getElementLabel(config, BeansUILabels.APPEND_PATH
						| BeansUILabels.DESCRIPTION);
			}
		}
		else if (element instanceof ZipEntryStorage) {
			// Create label of zip entry here as it is not a core model element
			ZipEntryStorage storage = (ZipEntryStorage) element;
			StringBuilder builder = new StringBuilder();
			builder.append(storage.getFullPath().lastSegment());
			builder.append(" - ");
			builder.append(storage.getFullPath().removeLastSegments(1).toString());
			builder.append(" - ");
			builder.append(storage.getFile().getFullPath().toString());
			return builder.toString();
		}
		else if (element instanceof BeanMetadataReference
				&& BeanMetadataUtils.getLabelProvider((BeanMetadataReference) element) != null) {
			return BeanMetadataUtils.getLabelProvider((BeanMetadataReference) element)
					.getDescription(element);
		}
		return null;
	}

	@Override
	public String toString() {
		return providerID;
	}

	protected String getProviderID() {
		return providerID;
	}

	@Override
	protected Image getImage(Object element, Object parentElement) {
		if (element instanceof IBeansProject) {
			Image image = BeansUIImages.getImage(BeansUIImages.IMG_OBJS_VIRTUAL_FOLDER);
			return image;
		}
		return super.getImage(element, parentElement);
	}

	@Override
	protected String getText(Object element, Object parentElement) {
		if (element instanceof IBeansProject) {
			return "Beans"; // TODO CD Externalize string
		}
		else if (element instanceof IBeansConfig
				&& (parentElement instanceof ISpringProject || parentElement instanceof IBeansProject)) {
			return ((IBeansConfig) element).getElementName();
		}
		else if (element instanceof IFile && parentElement instanceof IModelElement) {
			return ((IFile) element).getName() + " - "
					+ ((IFile) element).getProjectRelativePath().removeLastSegments(1).toString();
		}
		else if (element instanceof ILazyInitializedModelElement
				&& !((ILazyInitializedModelElement) element).isInitialized()) {
			return "loading model content..."; // TODO CD Externalize string
		}
		return super.getText(element, parentElement);
	}
}
