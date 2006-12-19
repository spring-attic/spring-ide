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
package org.springframework.ide.eclipse.aop.ui.navigator.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.springframework.ide.eclipse.aop.ui.navigator.model.IRevealableReferenceNode;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;

/**
 * Opens the file for currently selected {@link IBeansConfig}.
 */
public class OpenConfigFileAction
        extends Action {

    private ICommonActionExtensionSite site;

    private IRevealableReferenceNode element;

    public OpenConfigFileAction(ICommonActionExtensionSite site) {
        this.site = site;
        setText("Op&en"); // TODO externalize text
    }

    public boolean isEnabled() {
        ISelection selection = site.getViewSite().getSelectionProvider()
                .getSelection();
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection sSelection = (IStructuredSelection) selection;
            if (sSelection.size() == 1) {
                if (sSelection.getFirstElement() instanceof IRevealableReferenceNode) {
                    element = (IRevealableReferenceNode) sSelection.getFirstElement();
                    return true;
                }
            }
        }
        return false;
    }

    public void run() {
        if (isEnabled()) {
            element.openAndReveal();
        }
    }
}
