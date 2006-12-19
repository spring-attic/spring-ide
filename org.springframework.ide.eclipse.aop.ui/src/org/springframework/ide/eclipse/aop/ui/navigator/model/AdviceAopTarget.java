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
package org.springframework.ide.eclipse.aop.ui.navigator.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelProvider;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

public class AdviceAopTarget implements IReferenceNode,
        IRevealableReferenceNode {

    private IAopReference reference;

    private static BeansModelLabelProvider labelProvider = new BeansModelLabelProvider();

    public AdviceAopTarget(IAopReference reference) {
        this.reference = reference;
    }

    public IReferenceNode[] getChildren() {
        return new IReferenceNode[] { new AdviceAopTargetMethod(reference) };
    }

    public Image getImage() {
        return labelProvider.getImage(reference.getTargetBean());
    }

    public String getNodeName() {
        return reference.getTargetBean().getElementName() + " ["
                + reference.getResource().getProjectRelativePath().toString()
                + "]";
    }

    public boolean hasChildren() {
        return true;
    }

    public void openAndReveal() {
        IResource resource = reference.getResource();
        SpringUIUtils.openInEditor((IFile) resource, reference.getTargetBean()
                .getElementStartLine());
    }

}
