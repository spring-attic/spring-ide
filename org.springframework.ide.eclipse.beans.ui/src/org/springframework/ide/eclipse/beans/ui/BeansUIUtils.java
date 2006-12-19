/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.views.properties.FilePropertySource;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.ResourcePropertySource;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.model.properties.ChildBeanProperties;
import org.springframework.ide.eclipse.beans.ui.model.properties.ConfigSetProperties;
import org.springframework.ide.eclipse.beans.ui.model.properties.ConstructorArgumentProperties;
import org.springframework.ide.eclipse.beans.ui.model.properties.PropertyProperties;
import org.springframework.ide.eclipse.beans.ui.model.properties.RootBeanProperties;
import org.springframework.ide.eclipse.beans.ui.properties.ProjectPropertyPage;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.ide.eclipse.ui.TreePathBuilder;
import org.springframework.ide.eclipse.ui.editors.ZipEntryEditorInput;
import org.w3c.dom.Element;

/**
 * Some helper methods.
 * 
 * @author Torsten Juergeleit
 */
public final class BeansUIUtils {

	/**
	 * Returns edited file for given <code>IWorkbenchPart</code>
	 * if it's an editor editing a Spring bean config file.
	 */
	public static IFile getConfigFile(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			IEditorInput input = ((IEditorPart) part).getEditorInput();
			if (input instanceof IFileEditorInput) {
				IFile file = ((IFileEditorInput) input).getFile();
				IBeansProject project = BeansCorePlugin.getModel().getProject(
															 file.getProject());
				if (project != null && project.hasConfig(file)) {
					return file;
				}
			}
		}
		return null;
	}

	/**
	 * Returns <code>IBeansConfig</code> for given <code>IWorkbenchPart</code>
	 * if it's an editor editing a Spring bean config file.
	 */
	public static IBeansConfig getConfig(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			IEditorInput input = ((IEditorPart) part).getEditorInput();
			if (input instanceof IFileEditorInput) {
				IFile file = ((IFileEditorInput) input).getFile();
				return BeansCorePlugin.getModel().getConfig(file);
			} else if (input instanceof ZipEntryEditorInput) {
				ZipEntryStorage storage = (ZipEntryStorage)
						((ZipEntryEditorInput) input).getStorage();
				IBeansProject project = BeansCorePlugin.getModel().getProject(
						storage.getZipResource().getProject());
				if (project != null) {
					return project.getConfig(storage.getFullName());
				}
			}
		}
		return null;
	}

	/**
	 * Returns a corresponding instance of <code>IPropertySource</code> for the
	 * given model element ID or null.
	 * @param id  the model element ID
	 */
	public static IPropertySource getPropertySource(String id) {
		IModelElement element = BeansCorePlugin.getModel().getElement(id);
		return (element != null ? getPropertySource(element) : null);
	}

	/**
	 * Returns a corresponding instance of <code>IPropertySource</code> for the
	 * given <code>IModelElement</code> or null.
	 */
	public static IPropertySource getPropertySource(IModelElement element) {
		if (element instanceof IBeansProject) {
			return new ResourcePropertySource(
										((IBeansProject) element).getProject());
		} else if (element instanceof IBeansConfig) {
			IFile file = (IFile) ((IBeansConfig) element).getElementResource();
			if (file != null && file.exists()) {
				return new FilePropertySource(file);
			}
		} else if (element instanceof IBeansConfigSet) {
			return new ConfigSetProperties(((IBeansConfigSet) element));
			
		} else if (element instanceof IBean) {
			IBean bean = ((IBean) element);
			if (bean.isRootBean()) {
				return new RootBeanProperties(bean);
			} else if (bean.isChildBean()){
				return new ChildBeanProperties(bean);
			} else {
				// FIXME add support for factory beans
//				return new FactoryBeanProperties(bean);
			}
		} else if (element instanceof IBeanConstructorArgument) {
			return new ConstructorArgumentProperties(
											(IBeanConstructorArgument) element);
		} else if (element instanceof IBeanProperty) {
			return new PropertyProperties((IBeanProperty) element);
		}
		return null;
	}

	public static void showProjectPropertyPage(IProject project, int block) {
		if (project != null) {
			String title = BeansUIPlugin
					.getResourceString("PropertiesPage.title")
					+ project.getName();
			IPreferencePage page = new ProjectPropertyPage(project, block);
			SpringUIUtils.showPreferencePage(ProjectPropertyPage.ID,
					page, title);
		}
	}

	/**
	 * Opens given <code>IResourceModelElement element</code> in associated
	 * editor.
	 */
	public static IEditorPart openInEditor(IResourceModelElement element) {
		IResourceModelElement sourceElement;
		int line;
		if (element instanceof ISourceModelElement) {
			ISourceModelElement source = (ISourceModelElement) element;
			sourceElement = source.getElementSourceElement();
			line = source.getElementStartLine();
		} else if (element instanceof IBeansConfig) {
			sourceElement = element;
			line = -1;
		} else {
			return null;
		}
		IResource resource = sourceElement.getElementResource();
		if (resource instanceof IFile) {
			IFile file = (IFile) resource;
			if (sourceElement.isElementArchived()) {
				try {
					ZipEntryStorage storage = new ZipEntryStorage(sourceElement);
					IEditorInput input = new ZipEntryEditorInput(storage);
					IEditorDescriptor desc = IDE.getEditorDescriptor(storage
							.getName());
					IEditorPart editor = SpringUIUtils.openInEditor(input, desc
							.getId());
					IMarker marker = file.createMarker(IMarker.TEXT);
					marker.setAttribute(IMarker.LINE_NUMBER, line);
					IDE.gotoMarker(editor, marker);
					return editor;
				} catch (CoreException e) {
					BeansCorePlugin.log(e);
				}
			} else {
				return SpringUIUtils.openInEditor(file, line);
			}
		}
		return null;
	}

	public static IModelElement getSelectedElement(ISelection selection,
			IModelElement contextElement) {
		if (selection instanceof IStructuredSelection
				&& !selection.isEmpty()) {
			IStructuredSelection sSelection = (IStructuredSelection) selection;
			if (sSelection.size() == 1) {
				Object sElement = sSelection.getFirstElement();
				if (sElement instanceof Element) {
					return BeansModelUtils.getModelElement((Element) sElement,
							contextElement);
				}
			}
		}
		return null;
	}

	public static TreePath createTreePath(IModelElement element) {
		TreePathBuilder path = new TreePathBuilder();
		while (element != null && element.getElementParent() != null) {
			path.addParent(element);
			if (element instanceof IBeansConfig) {
				IBeansConfig config = (IBeansConfig) element;
				if (config.isElementArchived()) {
					path.addParent(new ZipEntryStorage(config));
				} else {
					path.addParent(config.getElementResource());
				}
			}
			element = element.getElementParent();
		}
		return path.getPath();
	}
}
