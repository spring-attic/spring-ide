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

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeanClassReferences;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.namespaces.DefaultNamespaceContentProvider;
import org.springframework.ide.eclipse.beans.ui.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.model.IModelChangeListener;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent.Type;

/**
 * This class is a content provider which knows about the beans core model's
 * {@link IModelElement elements}.
 * 
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class BeansModelContentProvider implements ITreeContentProvider,
		IModelChangeListener {

	public static final DefaultNamespaceContentProvider
			DEFAULT_NAMESPACE_CONTENT_PROVIDER = new
					DefaultNamespaceContentProvider();

	private boolean refresh;
	private StructuredViewer viewer;

	public BeansModelContentProvider() {
		this(true);
	}

	public BeansModelContentProvider(boolean refresh) {
		this.refresh = refresh;
		if (refresh) {
			BeansCorePlugin.getModel().addChangeListener(this);
		}
	}

	public final void inputChanged(Viewer viewer, Object oldInput,
			Object newInput) {
		if (viewer instanceof StructuredViewer) {
			this.viewer = (StructuredViewer) viewer;
		} else {
			this.viewer = null;
		}
	}

	public final void dispose() {
		if (refresh) {
			BeansCorePlugin.getModel().removeChangeListener(this);
		}
	}

	public Object[] getElements(Object inputElement) {
		return BeansCorePlugin.getModel().getElementChildren();
	}

    public boolean hasChildren(Object element) {
		if (element instanceof ISourceModelElement) {
			ITreeContentProvider provider = NamespaceUtils
					.getContentProvider((ISourceModelElement) element);
			if (provider != null) {
				return provider.hasChildren(element);
			} else {
				return DEFAULT_NAMESPACE_CONTENT_PROVIDER.hasChildren(element);
			}
		} else if (element instanceof IModelElement) {
			return ((IModelElement) element).getElementChildren().length > 0;
		} else if (element instanceof IFile) {
			IBeansConfig config = BeansCorePlugin.getModel().getConfig(
					(IFile) element);
			if (config != null) {
				// The single IBeansConfig node is available
				return true;
			}
		} else if (element instanceof ZipEntryStorage) {
			IBeansConfig config = BeansModelUtils
					.getConfig((ZipEntryStorage) element);
			if (config != null) {
				// The single IBeansConfig node is available
				return true;
			}
		} else if (element instanceof IType) {
			IType type = (IType) element;

			// Only source types are supported
			if (!type.isBinary()) {
				IBeansProject beansProject = BeansCorePlugin.getModel()
						.getProject(type.getJavaProject().getProject());
				if (beansProject != null) {
					Set<IBean> beans = beansProject.getBeans(type
							.getFullyQualifiedName());
					return beans != null && beans.size() > 0;
				}
			}
		} else if (element instanceof BeanClassReferences) {
			return true;
		}
		return false;
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ISourceModelElement) {
			ITreeContentProvider provider = NamespaceUtils
					.getContentProvider((ISourceModelElement) parentElement);
			if (provider != null) {
				return provider.getChildren(parentElement);
			} else {
				return DEFAULT_NAMESPACE_CONTENT_PROVIDER
						.getChildren(parentElement);
			}
		} else if (parentElement instanceof IModelElement) {
			if (parentElement instanceof IBeansProject) {
				return getProjectChildren((IBeansProject) parentElement, false);
			} else if (parentElement instanceof IBeansConfigSet) {
				return getConfigSetChildren((IBeansConfigSet) parentElement);
			}
			return ((IModelElement) parentElement).getElementChildren();
		} else if (parentElement instanceof IFile) {
			IBeansConfig config = BeansCorePlugin.getModel().getConfig(
					(IFile) parentElement);
			if (config != null) {
				return new IBeansConfig[] { config };
			}
		} else if (parentElement instanceof ZipEntryStorage) {
			IBeansConfig config = BeansCorePlugin.getModel().getConfig(
					((ZipEntryStorage) parentElement).getAbsoluteName());
			if (config != null) {
				return new IBeansConfig[] { config };
			}
		} else if (parentElement instanceof IType) {
			return getJavaTypeChildren((IType) parentElement);
		} else if (parentElement instanceof BeanClassReferences) {
			Set<IBean> beans = ((BeanClassReferences) parentElement)
					.getBeans();
			return beans.toArray(new IBean[beans.size()]);
		}
		return IModelElement.NO_CHILDREN;
	}

	protected Object[] getProjectChildren(IBeansProject project,
			boolean onlyConfigSets) {
		Set<Object> children = new LinkedHashSet<Object>();
		if (!onlyConfigSets) {
			for (IBeansConfig config : project.getConfigs()) {
				if (config.isElementArchived()) {
					children.add(new ZipEntryStorage(config));
				} else {
					children.add(config.getElementResource());
				}
			}
		}
		children.addAll(project.getConfigSets());
		return children.toArray();
	}

	protected Object[] getConfigSetChildren(IBeansConfigSet configSet) {
		Set<ISourceModelElement> children =
				new LinkedHashSet<ISourceModelElement>();
		for (IBeansConfig config : configSet.getConfigs()) {
			Object[] configChildren = getChildren(config);
			for (Object child : configChildren) {
				if (child instanceof IBean
						|| child instanceof IBeansComponent) {
					children.add((ISourceModelElement) child);
				}
			}
		}
		return children.toArray();
	}

	protected Object[] getJavaTypeChildren(IType type) {
		IBeansProject project = BeansCorePlugin.getModel().getProject(
				type.getJavaProject().getProject());
		if (project != null) {

			// Add bean references to JDT type
			Set<IBean> beans = project.getBeans(type.getFullyQualifiedName());
			if (beans != null && beans.size() > 0) {
				return new Object[] { new BeanClassReferences(type, beans) };
			}
		}
		return IModelElement.NO_CHILDREN;
	}

	public Object getParent(Object element) {
		if (element instanceof ISourceModelElement) {
			ITreeContentProvider provider = NamespaceUtils
					.getContentProvider((ISourceModelElement) element);
			if (provider != null) {
				return provider.getParent(element);
			}
			return DEFAULT_NAMESPACE_CONTENT_PROVIDER.getParent(element);
		} else if (element instanceof IModelElement) {
			if (element instanceof IBeansConfig) {
				return ((IBeansConfig) element).getElementResource();
			}
			else if (element instanceof IBeansProject) {
				return SpringCore.getModel().getProject(
						((IBeansProject) element).getProject());
			}
			return ((IModelElement) element).getElementParent();
		} else if (element instanceof IFile) {
			IBeansConfig config = BeansCorePlugin.getModel().getConfig(
					(IFile) element);
			if (config != null) {
				return config.getElementParent();
			}
		} else if (element instanceof ZipEntryStorage) {
			IBeansConfig config = BeansCorePlugin.getModel().getConfig(
					((ZipEntryStorage) element).getFullName());
			if (config != null) {
				return config.getElementParent();
			}
		} else if (element instanceof BeanClassReferences) {
			return ((BeanClassReferences) element).getBeanClass();
		}
		return null;
	}

	public void elementChanged(ModelChangeEvent event) {
		IModelElement element = event.getElement();

		// For events of type ADDED or REMOVED refresh the parent of the changed
		// model element
		if (event.getType() == Type.CHANGED) {
			refreshViewerForElement(element);
		} else {
			refreshViewerForElement(element.getElementParent());
		}
	}

	protected final StructuredViewer getViewer() {
		return viewer;
	}

	protected final void refreshViewerForElement(final Object element) {
		if (viewer instanceof StructuredViewer && element != null) {

			// Abort if this happens after disposes
			Control ctrl = viewer.getControl();
			if (ctrl == null || ctrl.isDisposed()) {
				return;
			}

			// Are we in the UI thread?
			if (ctrl.getDisplay().getThread() == Thread.currentThread()) {
				viewer.refresh(element);
			} else {
				ctrl.getDisplay().asyncExec(new Runnable() {
					public void run() {

						// Abort if this happens after disposes
						Control ctrl = viewer.getControl();
						if (ctrl == null || ctrl.isDisposed()) {
							return;
						}

						// If the model changed then refresh the whole viewer 
						if (element instanceof IBeansModel) {
							viewer.refresh();
						} else {
							viewer.refresh(element);
						}
					}
				});
			}
		}
	}
}
