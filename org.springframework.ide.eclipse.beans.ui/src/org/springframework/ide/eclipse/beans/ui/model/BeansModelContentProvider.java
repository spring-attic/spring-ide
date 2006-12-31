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

package org.springframework.ide.eclipse.beans.ui.model;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeanClassReferences;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.namespaces.DefaultNamespaceContentProvider;
import org.springframework.ide.eclipse.beans.ui.namespaces.NamespaceUtils;
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

	private StructuredViewer viewer;

	public final void inputChanged(Viewer viewer, Object oldInput,
			Object newInput) {
		if (viewer instanceof StructuredViewer) {
			this.viewer = (StructuredViewer) viewer;

			if (oldInput == null && newInput != null) {
				BeansCorePlugin.getModel().addChangeListener(this);
			} else if (oldInput != null && newInput == null) {
				BeansCorePlugin.getModel().removeChangeListener(this);
			}
		} else {
			this.viewer = null;
		}
	}

	public final void dispose() {
		if (viewer != null && viewer.getInput() != null) {
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
			getJavaTypeChildren((IType) parentElement);
		} else if (parentElement instanceof BeanClassReferences) {
			Set<IBean> beans = ((BeanClassReferences) parentElement)
					.getBeans();
			return beans.toArray(new IBean[beans.size()]);
		} else if (parentElement instanceof IAdaptable) {
			IProject project = (IProject) ((IAdaptable) parentElement)
					.getAdapter(IProject.class);
			if (project != null) {
				IBeansProject beansProject = BeansCorePlugin.getModel()
						.getProject(project);
				if (beansProject != null) {
					return getProjectChildren(beansProject, true);
				}
			}
		}
		return IModelElement.NO_CHILDREN;
	}

	private Object[] getProjectChildren(IBeansProject project,
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
		children.addAll(((IBeansProject) project).getConfigSets());
		return children.toArray(new Object[children.size()]);
	}

	private Object[] getConfigSetChildren(IBeansConfigSet configSet) {
		Set<IBean> beans = new LinkedHashSet<IBean>();
		for (IBeansConfig config : configSet.getConfigs()) {
			Object[] children = getChildren(config);
			for (Object child : children) {
				if (child instanceof IBean) {
					beans.add((IBean) child);
				}
			}
		}
		return beans.toArray(new IBean[beans.size()]);
	}

	private Object[] getJavaTypeChildren(IType type) {
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
			return ((IModelElement) element).getElementParent();
		} else if (element instanceof IFile) {
			return BeansCorePlugin.getModel().getConfig((IFile) element)
					.getElementParent();
		} else if (element instanceof ZipEntryStorage) {
			return BeansCorePlugin.getModel().getConfig(
					((ZipEntryStorage) element).getFullName())
					.getElementParent();
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
		if (viewer instanceof StructuredViewer) {
			Control ctrl = viewer.getControl();

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
						viewer.refresh(element);
					}
				});
			}
		}
	}
}
