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

package org.springframework.ide.eclipse.beans.ui.navigator.internal;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.IPipelinedTreeContentProvider;
import org.eclipse.ui.navigator.PipelinedShapeModification;
import org.eclipse.ui.navigator.PipelinedViewerUpdate;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.model.IModelChangeListener;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent;

/**
 * @author Torsten Juergeleit
 */
public class BeansNavigatorContentProvider implements
		IPipelinedTreeContentProvider, IModelChangeListener {

	private StructuredViewer viewer;

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
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

	public void dispose() {
		if (viewer != null) {
			IBeansModel model = null;
			Object obj = viewer.getInput();
			if (obj instanceof IBeansModel) {
				model = (IBeansModel) obj;
			}
			if (model != null) {
				model.removeChangeListener(this);
			}
		}
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(BeansCorePlugin.getModel());
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IModelElement) {
			if (parentElement instanceof IBeansConfigSet) {
				Set<IBean> beans = new LinkedHashSet<IBean>();
				for (IBeansConfig config : ((IBeansConfigSet) parentElement)
						.getConfigs()) {
					if (config != null) {
						beans.addAll(config.getBeans());
					}
				}
				return beans.toArray(new IBean[beans.size()]);
			} else {
				return ((IModelElement) parentElement).getElementChildren();
			}
		} else if (parentElement instanceof IFile) {
			IBeansConfig config = BeansCorePlugin.getModel().getConfig(
					(IFile) parentElement);
			if (config != null) {
				return config.getElementChildren();
			}
		} else if (parentElement instanceof IAdaptable) {
			IProject project = (IProject) ((IAdaptable) parentElement)
					.getAdapter(IProject.class);
			if (project != null) {
				IBeansProject beansProject = BeansCorePlugin.getModel()
						.getProject(project);
				if (beansProject != null) {
					Set<IBeansConfigSet> configSets = beansProject
							.getConfigSets();
					return configSets.toArray(new IBeansConfigSet[configSets
							.size()]);
				}
			}
		}
		return IModelElement.NO_CHILDREN;
	}

	public Object getParent(Object element) {
		if (element instanceof IModelElement) {
			return ((IModelElement) element).getElementParent();
		} else if (element instanceof IFile) {
			return ((IFile) element).getParent();
		}
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof IModelElement) {
			return !(element instanceof IBeanProperty
					|| element instanceof IBeanConstructorArgument);
		} else if (element instanceof IFile) {
			IFile file = (IFile) element;
			IBeansProject project = BeansCorePlugin.getModel().getProject(
					file.getProject());
			if (project != null) {
				IBeansConfig config = project.getConfig(file);
				if (config != null) {
					return config.getElementChildren().length > 0;
				}
			}
		}
		return false;
	}

	public void elementChanged(ModelChangeEvent event) {
		IModelElement element = event.getElement();
		if (element instanceof IBeansProject
				|| element instanceof IBeansConfig) {
			refreshViewer(((IResourceModelElement) element)
					.getElementResource());
		} else {
			refreshViewer(element);
		}
	}

	protected void refreshViewer(final Object element) {
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

	public void getPipelinedChildren(Object parent, Set currentChildren) {
		currentChildren.addAll(Arrays.asList(getChildren(parent)));
	}

	public void getPipelinedElements(Object input, Set currentElements) {
		currentElements.addAll(Arrays.asList(getElements(input)));
	}

	public Object getPipelinedParent(Object object, Object suggestedParent) {
		return getParent(object);
	}

	public PipelinedShapeModification interceptAdd(PipelinedShapeModification addModification) {
		return addModification;
	}

	public PipelinedShapeModification interceptRemove(PipelinedShapeModification removeModification) {
		return removeModification;
	}

	public boolean interceptRefresh(PipelinedViewerUpdate refreshSynchronization) {
		return false;
	}

	public boolean interceptUpdate(PipelinedViewerUpdate anUpdateSynchronization) {
		return false;
	}

	public void init(ICommonContentExtensionSite aConfig) {
	}

	public void restoreState(IMemento aMemento) {
	}

	public void saveState(IMemento aMemento) {
	}
}
