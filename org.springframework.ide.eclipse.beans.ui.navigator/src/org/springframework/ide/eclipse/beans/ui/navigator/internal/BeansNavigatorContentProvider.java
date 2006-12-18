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

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonContentProvider;
import org.eclipse.ui.navigator.INavigatorContentExtension;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.navigator.Activator;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.model.IModelChangeListener;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent;

/**
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class BeansNavigatorContentProvider implements ICommonContentProvider,
		IModelChangeListener {

	public static final String BEANS_EXPLORER_CONTENT_ID = Activator.PLUGIN_ID
			+ ".beansExplorerContent";

    public static final String BEANS_REFERENCE_CONTENT_ID = Activator.PLUGIN_ID
	    + ".beansReferenceProjectExplorerContent";

	private INavigatorContentExtension contentExtension;
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
		if (viewer != null && viewer.getInput() != null) {
			BeansCorePlugin.getModel().removeChangeListener(this);
		}
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(BeansCorePlugin.getModel());
	}

	public Object[] getChildren(Object parentElement) {
        // add bean references to jdt Type
        if (parentElement instanceof IType) {
            IType type = (IType) parentElement;
            IBeansProject project = BeansCorePlugin.getModel().getProject(
                    type.getJavaProject().getProject());
            if (project != null) {
                Set<IBean> beans = project.getBeans(type.getFullyQualifiedName());
                if (beans != null && beans.size() > 0) {
                    return new Object[] { new BeansReference(type, beans) };
                }
            }
        }
        else if (parentElement instanceof BeansReference) {
            return ((BeansReference) parentElement).getElementChildren();
        }
        else if (parentElement instanceof IModelElement) {
			if (parentElement instanceof IBeansProject) {
				Set<Object> children = new LinkedHashSet<Object>();
				for (IBeansConfig config : ((IBeansProject) parentElement).getConfigs()) {
					if (config.isElementArchived()) {
						children.add(new ZipEntryStorage(config));
					} else {
						children.add(config.getElementResource());
					}
				}
				children.addAll(((IBeansProject) parentElement).getConfigSets());
				return children.toArray(new Object[children.size()]);
			} else if (parentElement instanceof IBeansConfigSet) {
				Set<IBean> beans = new LinkedHashSet<IBean>();
				for (IBeansConfig config : ((IBeansConfigSet) parentElement)
						.getConfigs()) {
					if (config != null) {
						beans.addAll(config.getBeans());
					}
				}
				return beans.toArray(new IBean[beans.size()]);
			}
			return ((IModelElement) parentElement).getElementChildren();
		} else if (parentElement instanceof IFile) {
			IBeansConfig config = BeansCorePlugin.getModel().getConfig(
					(IFile) parentElement);
			if (config != null) {
				return new Object[] { config };
			}
		} else if (parentElement instanceof ZipEntryStorage) {
			IBeansConfig config = BeansCorePlugin.getModel().getConfig(
					((ZipEntryStorage) parentElement).getAbsoluteName());
			if (config != null) {
				return new Object[] { config };
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

			// For the BeasProjectExplorer returning the corresponding file for
			// every IBeansConfig
			if (element instanceof IBeansConfig && !contentExtension.getId()
					.equals(BEANS_EXPLORER_CONTENT_ID)) {
				return ((IBeansConfig) element).getElementResource();
			}
			return ((IModelElement) element).getElementParent();
		} else if (element instanceof IFile) {
			return BeansCorePlugin.getModel().getConfig((IFile) element)
					.getElementParent();
		}
		if (element instanceof ZipEntryStorage) {
			return BeansCorePlugin.getModel().getConfig(
					((ZipEntryStorage) element).getFullName())
					.getElementParent();
		}
		return null;
	}

	@SuppressWarnings("restriction")
    public boolean hasChildren(Object element) {
		if (element instanceof IModelElement) {
			return !(element instanceof IBeanProperty
					|| element instanceof IBeanConstructorArgument);
		} else if (element instanceof IFile) {
			IBeansConfig config = BeansCorePlugin.getModel().getConfig(
					(IFile) element);
			if (config != null) {
				return config.getElementChildren().length > 0;
			}
		} else if (element instanceof ZipEntryStorage) {
			IBeansConfig config = BeansModelUtils
					.getConfig((ZipEntryStorage) element);
			if (config != null) {
				return config.getElementChildren().length > 0;
			}
		} else if (element instanceof SourceType) {
            IType type = (IType) element;
            IBeansProject beansProject = BeansCorePlugin.getModel()
                .getProject(type.getJavaProject().getProject());
            if (beansProject != null) {
                Set<IBean> beans = beansProject.getBeans(
                        type.getFullyQualifiedName());
                return beans != null && beans.size() > 0;
            }
        } else if (element instanceof BeansReference) {
            return true;
        }
		return false;
	}

    public void elementChanged(ModelChangeEvent event) {
		IModelElement element = event.getElement();
		if (contentExtension.getId().equals(BEANS_REFERENCE_CONTENT_ID)) {
            IBeansConfig config = (IBeansConfig) element;
            Set<String> classes = config.getBeanClasses();
            for (String clz : classes) {
                IType type = BeansModelUtils.getJavaType(config.getElementResource()
                        .getProject(), clz);
                // check if we can resolve to a source type. otherwise we won't
                // add the references
                if (type != null && type instanceof SourceType) {
                    refreshViewer(type);
                }
            }
        } else if (!contentExtension.getId().equals(BEANS_EXPLORER_CONTENT_ID)
				&& (element instanceof IBeansProject
						|| element instanceof IBeansConfig)) {
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

	public void init(ICommonContentExtensionSite config) {
		contentExtension = config.getExtension();
	}

	public void saveState(IMemento aMemento) {
	}

	public void restoreState(IMemento aMemento) {
	}
    
	protected class BeansReference {
        
        private Set<IBean> beans = new LinkedHashSet<IBean>();

        private IType type = null;
        
        public BeansReference(IType type, Set<IBean> beans) {
            this.beans = beans;
            this.type = type;
        }
        
        public IModelElement[] getElementChildren() {
            return this.beans.toArray(new IModelElement[beans.size()]);
        }

        public String getElementName() {
            return "referenced by";
        }

        public IType getElementParent() {
            return this.type;
        }
    }
}
