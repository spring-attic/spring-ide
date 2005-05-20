/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.web.flow.ui.views.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.ui.views.BeansView;
import org.springframework.ide.eclipse.core.model.IModel;
import org.springframework.ide.eclipse.core.model.IModelElement;

public class ShowInView
        extends Action implements IViewActionDelegate {

    private IViewPart view;

    private String elementId;

    public void init(IViewPart view) {
        this.view = view;
    }

    public void selectionChanged(IAction action, ISelection selection) {
        if (((IStructuredSelection) selection).getFirstElement() instanceof IBeansConfigSet) {
            IBeansConfigSet configSet = (IBeansConfigSet) ((IStructuredSelection) selection)
                    .getFirstElement();
            elementId = configSet.getElementID();
        }
    }

    public void run(IAction action) {
        IModel model = BeansCorePlugin.getModel();
        IModelElement element = model.getElement(elementId);
        IViewPart view = BeansView.showView();  
        if (view instanceof IShowInTarget) {
            ((IShowInTarget) view).show(new ShowInContext(element, null));
        }
    }
}
