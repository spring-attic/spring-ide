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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreePathLabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.core.internal.model.BeanClassReferences;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.namespaces.DefaultNamespaceLabelProvider;
import org.springframework.ide.eclipse.beans.ui.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.core.MarkerUtils;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.ModelUtils;
import org.springframework.ide.eclipse.ui.SpringUILabelProvider;

/**
 * This class is an {@link ILabelProvider} which knows about the beans core
 * model's {@link IModelElement}s. If the given element is not of type
 * {@link IModelElement} then it tries to adapt it via {@link IAdaptable}.
 * 
 * @author Torsten Juergeleit
 */
public class BeansModelLabelProvider extends SpringUILabelProvider {

	public static final DefaultNamespaceLabelProvider
		DEFAULT_NAMESPACE_LABEL_PROVIDER = new DefaultNamespaceLabelProvider();

	public BeansModelLabelProvider() {
		super(false);
	}

	public BeansModelLabelProvider(boolean isDecorating) {
		super(isDecorating);
	}

	protected Image getBaseImage(Object element) {
		Object adaptedElement = ModelUtils.adaptToModelElement(element);
		if (adaptedElement instanceof ISourceModelElement) {
			ILabelProvider provider = NamespaceUtils
					.getLabelProvider((ISourceModelElement) adaptedElement);
			if (provider != null) {
				return provider.getImage(adaptedElement);
			} else {
				return DEFAULT_NAMESPACE_LABEL_PROVIDER
						.getImage(adaptedElement);
			}
		} else if (adaptedElement instanceof IModelElement) {
			return BeansModelImages.getImage((IModelElement) adaptedElement);
		}
		if (element instanceof ZipEntryStorage) {
			return super.getBaseImage(((ZipEntryStorage) element).getFile());
		} else if (element instanceof BeanClassReferences) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_REFERENCE);
		}
		return super.getBaseImage(element);
	}

	protected int getSeverity(Object element) {
		int severity = 0;
		if (element instanceof ISourceModelElement) {
			ISourceModelElement source = (ISourceModelElement) element;
			severity = MarkerUtils.getHighestSeverityFromMarkersInRange(source
					.getElementResource(), source.getElementStartLine(), source
					.getElementEndLine());
		}
		else if (element instanceof IResourceModelElement) {
			if (element instanceof IBeansConfigSet) {
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

	protected String getBaseText(Object element) {
		Object adaptedElement = ModelUtils.adaptToModelElement(element);
		if (adaptedElement instanceof ISourceModelElement) {
			ILabelProvider provider = NamespaceUtils
					.getLabelProvider((ISourceModelElement) adaptedElement);
			if (provider != null) {
				return provider.getText(adaptedElement);
			} else {
				return DEFAULT_NAMESPACE_LABEL_PROVIDER
						.getText(adaptedElement);
			}
		} else if (adaptedElement instanceof IModelElement) {
			return BeansModelLabels.getElementLabel(
					(IModelElement) adaptedElement, 0);
		}
		if (element instanceof IFile) {
			return ((IFile) element).getProjectRelativePath().toString();
		} else if (element instanceof ZipEntryStorage) {
			ZipEntryStorage storage = (ZipEntryStorage) element;
			StringBuffer buf = new StringBuffer(storage.getFile()
					.getProjectRelativePath().toString());
			buf.append(" - " + storage.getFullPath().toString());
			return buf.toString();
		} else if (element instanceof BeanClassReferences) {
			return BeansUIPlugin.getResourceString(
					"BeanClassReferences.label");
		}
		return super.getBaseText(element);
	}

	public void updateLabel(ViewerLabel label, TreePath elementPath) {
		Object element = ModelUtils.adaptToModelElement(elementPath
				.getLastSegment());
		if (element instanceof ISourceModelElement) {
			ILabelProvider provider = NamespaceUtils
					.getLabelProvider((ISourceModelElement) element);
			if (provider != null
					&& provider instanceof ITreePathLabelProvider) {
				((ITreePathLabelProvider) provider).updateLabel(label,
						elementPath);
			} else {
				DEFAULT_NAMESPACE_LABEL_PROVIDER
						.updateLabel(label, elementPath);
			}
			if (isDecorating()) {
				label.setImage(getDecoratedImage(element, label.getImage()));
				label.setText(getDecoratedText(element, label.getText()));
			}
		} else {
			label.setImage(getImage(element));
			label.setText(getText(element));
		}
	}
}
