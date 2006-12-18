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
package org.springframework.ide.eclipse.aop.ui.navigator;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonContentProvider;
import org.eclipse.ui.navigator.INavigatorContentExtension;
import org.springframework.ide.eclipse.aop.core.model.IAopProject;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.ui.BeansAopPlugin;
import org.springframework.ide.eclipse.aop.ui.navigator.util.MethodWrapper;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.model.IModelChangeListener;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent;

/**
 */
@SuppressWarnings("restriction")
public class BeansAopNavigatorContentProvider implements
        ICommonContentProvider, IModelChangeListener {

    @SuppressWarnings("unused")
    private INavigatorContentExtension contentExtension;

    private StructuredViewer viewer;

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (viewer instanceof StructuredViewer) {
            this.viewer = (StructuredViewer) viewer;

            if (oldInput == null && newInput != null) {
                BeansCorePlugin.getModel().addChangeListener(this);
            }
            else if (oldInput != null && newInput == null) {
                BeansCorePlugin.getModel().removeChangeListener(this);
            }
        }
        else {
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

    @SuppressWarnings("restriction")
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof IMethod
                && parentElement instanceof SourceMethod) {
            IMethod method = (IMethod) parentElement;
            IAopProject project = BeansAopPlugin.getModel().getProject(
                    method.getJavaProject().getProject());
            if (project != null && project.getAllReferences().size() > 0) {
                List<IAopReference> references = project.getAllReferences();
                Set<IAopReference> foundReferences = new LinkedHashSet<IAopReference>();
                for (IAopReference reference : references) {
                    if (reference.getTarget().equals(method)) {
                        foundReferences.add(reference);
                    }
                }
                return new Object[] { new AopReference(foundReferences) };
            }
        }
        else if (parentElement instanceof AopReference) {
            return ((AopReference) parentElement).getElementChildren();
       } else if (parentElement instanceof IAopReference) {
            return new Object[] { new MethodWrapper(((IAopReference) parentElement).getSource()) };
        }
        return IModelElement.NO_CHILDREN;
    }

    public Object getParent(Object element) {
        if (element instanceof IModelElement) {
            return ((IModelElement) element).getElementParent();
        }
        else if (element instanceof IFile) {
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
        if (element instanceof IMethod && element instanceof SourceMethod) {
            IMethod method = (IMethod) element;
            IAopProject project = BeansAopPlugin.getModel().getProject(
                    method.getJavaProject().getProject());
            if (project != null && project.getAllReferences().size() > 0) {
                List<IAopReference> references = project.getAllReferences();
                // List<IAopReference> foundReferences = new
                // ArrayList<IAopReference>();
                for (IAopReference reference : references) {
                    if (reference.getTarget().equals(method)) {
                        return true;
                    }
                }
            }
            else {
                return false;
            }
        }
        else if (element instanceof AopReference) {
            return true;
        }
        else if (element instanceof IAopReference) {
            return true;
        }
        return false;
    }

    public void elementChanged(ModelChangeEvent event) {
        IModelElement element = event.getElement();
        if (element instanceof IBeansConfig) {
            IBeansConfig config = (IBeansConfig) element;
            Set<String> classes = config.getBeanClasses();
            for (String clz : classes) {
                IType type = BeansModelUtils.getJavaType(config
                        .getElementResource().getProject(), clz);
                if (type != null && type instanceof SourceType) {
                    refreshViewer(type);
                }
            }
        }
    }

    protected void refreshViewer(final Object element) {
        if (viewer instanceof StructuredViewer) {
            Control ctrl = viewer.getControl();

            // Are we in the UI thread?
            if (ctrl.getDisplay().getThread() == Thread.currentThread()) {
                viewer.refresh(element);
            }
            else {
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

    public class AopReference {

        private Set<IAopReference> refs = new LinkedHashSet<IAopReference>();

        public AopReference(Set<IAopReference> refs) {
            this.refs = refs;
        }

        public Object[] getElementChildren() {
            return this.refs.toArray(new Object[refs.size()]);
        }

        public String getElementName() {
            return "advised by";
        }
    }
}
