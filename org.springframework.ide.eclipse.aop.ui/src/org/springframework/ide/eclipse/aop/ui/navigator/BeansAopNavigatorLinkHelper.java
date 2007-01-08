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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.navigator.ILinkHelper;
import org.springframework.ide.eclipse.aop.ui.navigator.model.IRevealableReferenceNode;

/**
 */
public class BeansAopNavigatorLinkHelper implements ILinkHelper {

    public void activateEditor(IWorkbenchPage page,
            IStructuredSelection selection) {
        if (selection != null && !selection.isEmpty()) {
            if (selection.getFirstElement() instanceof IRevealableReferenceNode) {
                // TODO fix selection hen & egg problem with ISelectionListener
                /*IRevealableReferenceNode method = (IRevealableReferenceNode) selection
                        .getFirstElement();
                IEditorInput input = new FileEditorInput((IFile) method.getResource());
                IEditorPart editor = page.findEditor(input);
                if (editor != null) {
                    method.openAndReveal();
                }*/
            }
        }
    }

    public IStructuredSelection findSelection(IEditorInput input) {
        return StructuredSelection.EMPTY;
    }
}
