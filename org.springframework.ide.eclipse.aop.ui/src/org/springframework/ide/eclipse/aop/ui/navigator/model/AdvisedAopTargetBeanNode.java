/*
 * Copyright 2002-2007 the original author or authors.
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

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.ui.navigator.util.BeansAopNavigatorUtils;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

public class AdvisedAopTargetBeanNode implements IReferenceNode, IRevealableReferenceNode {

    private List<IAopReference> references;

    public AdvisedAopTargetBeanNode(List<IAopReference> reference) {
        this.references = reference;
    }

    public IReferenceNode[] getChildren() {
        return new IReferenceNode[] { new AdvisedAopReferenceNode(references) };
    }

    public Image getImage() {
        return BeansAopNavigatorUtils.BEAN_LABEL_PROVIDER.getImage(this.references.get(0)
                .getTargetBean());
    }

    public String getText() {
        return BeansAopNavigatorUtils.BEAN_LABEL_PROVIDER.getText(this.references.get(0)
                .getTargetBean())
                + " - "
                + this.references.get(0).getTargetBean().getElementResource()
                        .getProjectRelativePath().toString();
    }

    public boolean hasChildren() {
        return true;
    }

    public void openAndReveal() {
        IResource resource = references.get(0).getTargetBean().getElementResource();
        SpringUIUtils.openInEditor((IFile) resource, references.get(0).getTargetBean()
                .getElementStartLine());
    }

    public IAopReference getReference() {
        return this.references.get(0);
    }

    public int getLineNumber() {
        return references.get(0).getTargetBean().getElementStartLine();
    }

    public IResource getResource() {
        return references.get(0).getTargetBean().getElementResource();
    }
}
