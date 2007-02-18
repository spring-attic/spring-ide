/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.eclipse.beans.ui.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreePathLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.springframework.ide.eclipse.beans.core.internal.model.BeanClassReferences;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.namespaces.DefaultNamespaceLabelProvider;
import org.springframework.ide.eclipse.beans.ui.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.ModelUtils;

/**
 * This class is an {@link ILabelProvider} which knows about the beans core
 * model's {@link IModelElement}s. If the given element is not of type
 * {@link IModelElement} then it tries to adapt it via {@link IAdaptable}.
 * 
 * @author Torsten Juergeleit
 */
public class BeansModelLabelProvider extends LabelProvider implements
		ITreePathLabelProvider {

	public static final DefaultNamespaceLabelProvider
		DEFAULT_NAMESPACE_LABEL_PROVIDER = new DefaultNamespaceLabelProvider();

	private boolean isDecorating;
	private WorkbenchLabelProvider wbLabelProvider;


	public BeansModelLabelProvider() {
		this(false);
	}

	public BeansModelLabelProvider(boolean isDecorating) {
		this.isDecorating = isDecorating;
		this.wbLabelProvider = new WorkbenchLabelProvider();
	}

	public void dispose() {
		wbLabelProvider.dispose();
		super.dispose();
	}

	public Image getImage(Object element) {
		Image image = getBaseImage(element);
		if (isDecorating) {
			return getDecoratedImage(element, image);
		}
		return image;
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
			return wbLabelProvider.getImage(((ZipEntryStorage) element)
					.getFile());
		} else if (element instanceof BeanClassReferences) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_REFERENCE);
		}
		return wbLabelProvider.getImage(element);
	}

	protected Image getDecoratedImage(Object element, Image image) {
		IResource resource = null;
		int startLine = -1;
		int endLine = -1;
		if (element instanceof IResourceModelElement) {
			resource = ((IResourceModelElement) element).getElementResource();
			if (element instanceof ISourceModelElement) {
				startLine = ((ISourceModelElement) element)
						.getElementStartLine();
				endLine = ((ISourceModelElement) element).getElementEndLine();
			}
		} else if (element instanceof IResource) {
			resource = (IResource) element;
		} else if (element instanceof ZipEntryStorage) {
			resource = ((ZipEntryStorage) element).getFile();
		}
		if (resource != null) {
			try {
				int severity = -1;
				IMarker[] markers = resource.findMarkers(IMarker.PROBLEM, true,
						IResource.DEPTH_INFINITE);
				for (IMarker marker : markers) {
					if (startLine == -1
							|| isMarkerInRange(marker, startLine, endLine)) {
						int sev = marker.getAttribute(IMarker.SEVERITY, -1);
						if (sev == IMarker.SEVERITY_WARNING) {
							severity = sev;
						} else if (sev == IMarker.SEVERITY_ERROR) {
							severity = sev;
							break;
						}
					}
				}
				if (severity == IMarker.SEVERITY_WARNING) {
					return BeansModelImages.getDecoratedImage(image,
							BeansModelImages.FLAG_WARNING);
				} else if (severity == IMarker.SEVERITY_ERROR) {
					return BeansModelImages.getDecoratedImage(image,
							BeansModelImages.FLAG_ERROR);
				}
			} catch (CoreException e) {
				BeansUIPlugin.log(e);
			}
		}
		return image;
	}

	private boolean isMarkerInRange(IMarker marker, int startLine, int endLine)
			throws CoreException {
		if (startLine >= 0 && endLine >= startLine
				&& marker.isSubtypeOf(IMarker.TEXT)) {
			int line = marker.getAttribute(IMarker.LINE_NUMBER, -1);
			return (line >= startLine && line <= endLine);
		}
		return false;
	}

	public String getText(Object element) {
		String text = getBaseText(element);
		if (isDecorating) {
			return getDecoratedText(element, text);
		}
		return text;
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
		return wbLabelProvider.getText(element);
	}

	protected String getDecoratedText(Object element, String text) {
		return text;
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
			if (isDecorating) {
				label.setImage(getDecoratedImage(element, label.getImage()));
				label.setText(getDecoratedText(element, label.getText()));
			}
		} else {
			label.setImage(getImage(element));
			label.setText(getText(element));
		}
	}
}
