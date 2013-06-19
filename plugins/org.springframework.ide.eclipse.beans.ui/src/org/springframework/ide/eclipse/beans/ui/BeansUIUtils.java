/*******************************************************************************
 * Copyright (c) 2004, 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.views.properties.FilePropertySource;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.ResourcePropertySource;
import org.springframework.core.io.Resource;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.AbstractBeansConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.BeanClassReferences;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigFactory;
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
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @author Martin Lippert
 * @author Leo Dos Santos
 */
public final class BeansUIUtils {

	/**
	 * Returns edited file for given <code>IWorkbenchPart</code> if it's an
	 * editor editing a Spring bean config file.
	 */
	public static IFile getConfigFile(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			IEditorInput input = ((IEditorPart) part).getEditorInput();
			if (input instanceof IFileEditorInput) {
				IFile file = ((IFileEditorInput) input).getFile();
				IBeansProject project = BeansCorePlugin.getModel().getProject(
						file.getProject());
				if (project != null && project.getConfig(BeansConfigFactory.getConfigId(file), true) != null) {
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
				return BeansCorePlugin.getModel().getConfig(BeansConfigFactory.getConfigId(file));
			}
			else if (input instanceof ZipEntryEditorInput) {
				ZipEntryStorage storage = (ZipEntryStorage) ((ZipEntryEditorInput) input)
						.getStorage();
				IBeansProject project = BeansCorePlugin.getModel().getProject(
						storage.getFile().getProject());
				if (project != null) {
					return project.getConfig(BeansConfigFactory.getConfigId(storage, project.getProject()));
				}
			}
		}
		return null;
	}

	/**
	 * Returns a corresponding instance of <code>IPropertySource</code> for
	 * the given model element ID or null.
	 * @param id the model element ID
	 */
	public static IPropertySource getPropertySource(String id) {
		IModelElement element = BeansCorePlugin.getModel().getElement(id);
		return (element != null ? getPropertySource(element) : null);
	}

	/**
	 * Returns a corresponding instance of <code>IPropertySource</code> for
	 * the given <code>IModelElement</code> or null.
	 */
	public static IPropertySource getPropertySource(IModelElement element) {
		if (element instanceof IBeansProject) {
			return new ResourcePropertySource(((IBeansProject) element)
					.getProject());
		}
		else if (element instanceof IBeansConfig) {
			IFile file = (IFile) ((IBeansConfig) element).getElementResource();
			if (file != null && file.exists()) {
				return new FilePropertySource(file);
			}
		}
		else if (element instanceof IBeansConfigSet) {
			return new ConfigSetProperties(((IBeansConfigSet) element));

		}
		else if (element instanceof IBean) {
			IBean bean = ((IBean) element);
			if (bean.isRootBean()) {
				return new RootBeanProperties(bean);
			}
			else if (bean.isChildBean()) {
				return new ChildBeanProperties(bean);
			}
			else {
				// FIXME add support for factory beans
				// return new FactoryBeanProperties(bean);
			}
		}
		else if (element instanceof IBeanConstructorArgument) {
			return new ConstructorArgumentProperties(
					(IBeanConstructorArgument) element);
		}
		else if (element instanceof IBeanProperty) {
			return new PropertyProperties((IBeanProperty) element);
		}
		else if (element instanceof BeanClassReferences) {
			// TODO implement IPropertySource for BeanClassReferences
		}
		return null;
	}

	public static void showProjectPropertyPage(IProject project,
			Map<String, Object> data) {
		if (project != null) {
			SpringUIUtils.showPreferenceDialog(ProjectPropertyPage.ID, project,
					data);
		}
	}

	public static IEditorPart openInEditor(IResourceModelElement element) {
		return openInEditor(element, true);
	}

	/**
	 * Opens given {@link IResourceModelElement} in associated editor.
	 */
	public static IEditorPart openInEditor(IResourceModelElement element,
			boolean activate) {
		IResourceModelElement sourceElement;
		IResource resource = null;
		ZipEntryStorage zipEntryStorage = null;
		int line;
		if (element instanceof ISourceModelElement) {
			ISourceModelElement source = (ISourceModelElement) element;
			sourceElement = source.getElementSourceElement();
			line = source.getElementStartLine();
			Resource res = source.getElementSourceLocation().getResource();
			if (res instanceof IAdaptable) {
				resource = (IResource) ((IAdaptable) res)
						.getAdapter(IResource.class);
				zipEntryStorage = (ZipEntryStorage) ((IAdaptable) res)
						.getAdapter(ZipEntryStorage.class);
			}
			else {
				resource = sourceElement.getElementResource();
			}
		}
		else if (element instanceof AbstractBeansConfig) {
			sourceElement = element;
			line = ((AbstractBeansConfig) element).getElementStartLine();
			resource = sourceElement.getElementResource();
		}
		else {
			return null;
		}

		if (resource instanceof IFile) {

			// add to history
			BeansUIActivationHistory.addToHistory(element);

			IFile file = (IFile) resource;
			if (sourceElement.isElementArchived()) {
				try {
					ZipEntryStorage storage = new ZipEntryStorage(sourceElement);
					return openZipEntryStorage(line, file, storage);
				}
				catch (CoreException e) {
					BeansCorePlugin.log(e);
				}
			}
			else {
				return SpringUIUtils.openInEditor(file, line, activate);
			}
		}
		else if (zipEntryStorage != null) {
			try {
				return openZipEntryStorage(line, zipEntryStorage.getFile(),
						zipEntryStorage);
			}
			catch (CoreException e) {
				BeansCorePlugin.log(e);
			}
		}
		return null;
	}
	
	private static IEditorPart openZipEntryStorage(int line, IFile file,
			ZipEntryStorage storage) throws PartInitException, CoreException {
		IEditorInput input = new ZipEntryEditorInput(storage);
		IEditorDescriptor desc = IDE.getEditorDescriptor(storage.getName());
		IEditorPart editor = SpringUIUtils.openInEditor(input, desc.getId());
		IMarker marker = file.createMarker(IMarker.TEXT);
		marker.setAttribute(IMarker.LINE_NUMBER, line);
		IDE.gotoMarker(editor, marker);
		return editor;
	}

	public static IModelElement getSelectedElement(ISelection selection,
			IModelElement contextElement) {
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			IStructuredSelection sSelection = (IStructuredSelection) selection;
			for (Object sElement : sSelection.toArray()) {
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
				}
				else {
					path.addParent(config.getElementResource());
				}
			}
			element = element.getElementParent();
		}
		return path.getPath();
	}

	/**
	 * Returns the context ({@link IBeansConfig} or {@link IBeansConfigSet})
	 * from the given {@link ITreeSelection selection}.
	 */
	public static IModelElement getContext(ITreeSelection selection) {
		TreePath path = selection.getPaths()[0];
		for (int i = path.getSegmentCount() - 1; i > 0; i--) {
			Object segment = path.getSegment(i);
			if (segment instanceof IBeansConfigSet
					|| segment instanceof IBeansConfig) {
				return (IModelElement) segment;
			}
			else if (segment instanceof BeanClassReferences) {
				IBean bean = (IBean) path.getSegment(i + 1);
				return bean.getElementParent();
			}
			else if (segment instanceof IFile) {
				IFile file = (IFile) segment;
				IBeansConfig config = BeansCorePlugin.getModel().getConfig(BeansConfigFactory.getConfigId(file));
				if (config != null) {
					return config;
				}
			}
		}
		return null;
	}

	/**
	 * Checks if the default double click action is active
	 * @since 2.0.2
	 */
	public static boolean shouldOpenConfigFile() {
		IScopeContext context = new InstanceScope();
		IEclipsePreferences node = context.getNode(BeansUIPlugin.PLUGIN_ID);
		return node.getBoolean(
				BeansUIPlugin.DEFAULT_DOUBLE_CLICK_ACTION_PREFERENCE_ID, true);
	}
}
