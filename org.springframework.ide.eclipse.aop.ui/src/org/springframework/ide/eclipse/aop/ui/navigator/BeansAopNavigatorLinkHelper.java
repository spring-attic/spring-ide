/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.aop.ui.navigator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.navigator.ILinkHelper;
import org.eclipse.ui.part.FileEditorInput;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.ui.navigator.util.MethodWrapper;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 */
public class BeansAopNavigatorLinkHelper implements ILinkHelper {

    public void activateEditor(IWorkbenchPage page,
            IStructuredSelection selection) {
        if (selection != null && !selection.isEmpty()) {
            if (selection.getFirstElement() instanceof MethodWrapper) {
                IMethod method = ((MethodWrapper) selection.getFirstElement()).getMethod();
                IResource resource;
                try {
                    resource = method.getUnderlyingResource();
                    IEditorInput input = new FileEditorInput((IFile) resource);
                    IEditorPart editor = page.findEditor(input);
                    if (editor != null) {
                        SpringUIUtils.openInEditor(method);
                    }
                }
                catch (JavaModelException e) {
                }
            }
            else if (selection.getFirstElement() instanceof IAopReference) {
                IAopReference reference = (IAopReference) selection
                        .getFirstElement();
                IResource resource = reference.getResource();
                if (resource instanceof IFile && resource.exists()) {
                    IEditorInput input = new FileEditorInput((IFile) resource);
                    IEditorPart editor = page.findEditor(input);
                    if (editor != null) {
                        SpringUIUtils.openInEditor((IFile) resource, reference
                                .getDefinition().getAspectLineNumber());
                    }
                }
            }
        }
    }

    public IStructuredSelection findSelection(IEditorInput input) {
        return StructuredSelection.EMPTY;
    }
}
