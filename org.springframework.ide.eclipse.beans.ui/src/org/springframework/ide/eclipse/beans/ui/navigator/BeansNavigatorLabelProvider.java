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
package org.springframework.ide.eclipse.beans.ui.navigator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;
import org.eclipse.ui.navigator.IDescriptionProvider;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.ui.BeansUILabels;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelProvider;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabels;
import org.springframework.ide.eclipse.beans.ui.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.ModelUtils;

/**
 * This class is a label provider for the {@link CommonNavigator} which knows
 * about the beans core model's {@link IModelElement elements}.
 * 
 * @author Torsten Juergeleit
 */
public class BeansNavigatorLabelProvider extends BeansModelLabelProvider
		implements ICommonLabelProvider {

	private String providerID;

	public BeansNavigatorLabelProvider() {
		super(true);
	}

	public BeansNavigatorLabelProvider(boolean isDecorating) {
		super(isDecorating);
	}

	protected String getProviderID() {
		return providerID;
	}

	public String getDescription(Object element) {
		Object adaptedElement = ModelUtils.adaptToModelElement(element);
		if (adaptedElement instanceof ISourceModelElement) {
			ILabelProvider provider = NamespaceUtils
					.getLabelProvider((ISourceModelElement) adaptedElement);
			if (provider != null && provider instanceof IDescriptionProvider) {
				return ((IDescriptionProvider) provider)
						.getDescription(adaptedElement);
			} else {
				return DEFAULT_NAMESPACE_LABEL_PROVIDER
						.getDescription(adaptedElement);
			}
		} else if (adaptedElement instanceof IModelElement) {
			return BeansModelLabels
					.getElementLabel((IModelElement) adaptedElement,
							BeansUILabels.APPEND_PATH
									| BeansUILabels.DESCRIPTION);
		}
		if (element instanceof IFile) {
			IBeansConfig config = BeansCorePlugin.getModel().getConfig(
					(IFile) element);
			if (config != null) {
				return BeansModelLabels.getElementLabel(config,
						BeansUILabels.APPEND_PATH
								| BeansUILabels.DESCRIPTION);
			}
		} else if (element instanceof ZipEntryStorage) {
			IBeansConfig config = BeansCorePlugin.getModel().getConfig(
					((ZipEntryStorage) element).getAbsoluteName());
			if (config != null) {
				return BeansModelLabels.getElementLabel(config,
						BeansUILabels.APPEND_PATH
								| BeansUILabels.DESCRIPTION);
			}
		}
		return null;
	}

	public void init(ICommonContentExtensionSite config) {
		providerID = config.getExtension().getId();
	}

	public void restoreState(IMemento memento) {
	}

	public void saveState(IMemento memento) {
	}
}
