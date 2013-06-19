/*******************************************************************************
 * Copyright (c) 2007, 2010 Spring IDE Developers
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
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;
import org.eclipse.ui.navigator.IDescriptionProvider;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.metadata.model.IBeanMetadata;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigFactory;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUILabels;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelProvider;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabels;
import org.springframework.ide.eclipse.beans.ui.model.metadata.BeanMetadataReference;
import org.springframework.ide.eclipse.beans.ui.model.metadata.BeanMetadataUtils;
import org.springframework.ide.eclipse.beans.ui.model.metadata.IBeanMetadataLabelProvider;
import org.springframework.ide.eclipse.beans.ui.namespaces.INamespaceLabelProvider;
import org.springframework.ide.eclipse.beans.ui.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.model.ILazyInitializedModelElement;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.ISpringProject;

/**
 * {@link ICommonLabelProvider} which knows about the beans core model's {@link IModelElement elements}.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class BeansNavigatorLabelProvider extends BeansModelLabelProvider implements ICommonLabelProvider,
		IFontProvider, IColorProvider {

	private Color grayColor = new Color(Display.getDefault(), 150, 150, 150);

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
		if (element instanceof IBeanMetadata) {
			IBeanMetadataLabelProvider labelProvider = BeanMetadataUtils.getLabelProvider((IBeanMetadata) element);
			if (labelProvider != null) {
				labelProvider.getDescription(element);
			}
		}

		if (element instanceof IBeansProject) {
			return "Beans" // TODO Externalize string
					+ " - " + ((IBeansProject) element).getProject().getName();
		}
		else if (element instanceof ISourceModelElement) {
			INamespaceLabelProvider provider = NamespaceUtils.getLabelProvider((ISourceModelElement) element);
			if (provider != null && provider instanceof IDescriptionProvider) {
				return ((IDescriptionProvider) provider).getDescription(element);
			}
			else {
				return DEFAULT_NAMESPACE_LABEL_PROVIDER.getDescription(element);
			}
		}
		else if (element instanceof IModelElement) {
			return BeansModelLabels.getElementLabel((IModelElement) element, BeansUILabels.APPEND_PATH
					| BeansUILabels.DESCRIPTION);
		}
		if (element instanceof IFile) {
			IBeansConfig config = BeansCorePlugin.getModel().getConfig(BeansConfigFactory.getConfigId((IFile) element));
			if (config != null) {
				return BeansModelLabels.getElementLabel(config, BeansUILabels.APPEND_PATH | BeansUILabels.DESCRIPTION);
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
			return BeanMetadataUtils.getLabelProvider((BeanMetadataReference) element).getDescription(element);
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
	public void dispose() {
		super.dispose();
		if (grayColor != null) {
			grayColor.dispose();
		}
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
		if (element instanceof ILazyInitializedModelElement
				&& !((ILazyInitializedModelElement) element).isInitialized()) {
			return "initializing..."; // TODO CD Externalize string
		}
		else if (element instanceof IBeansProject) {
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
		return super.getText(element, parentElement);
	}

	public Font getFont(Object element) {
		if (element instanceof ILazyInitializedModelElement
				&& !((ILazyInitializedModelElement) element).isInitialized()) {
			return JFaceResources.getFontRegistry().getItalic(JFaceResources.DIALOG_FONT);
		}
		return null;
	}

	public Color getBackground(Object element) {
		return null;
	}

	public Color getForeground(Object element) {
		if (element instanceof ILazyInitializedModelElement
				&& !((ILazyInitializedModelElement) element).isInitialized()) {
			return grayColor;
		}
		return null;
	}
}
