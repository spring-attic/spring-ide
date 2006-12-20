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
package org.springframework.ide.eclipse.aop.ui.navigator.model;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelProvider;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

public class AdvisedRootAopReference implements IReferenceNode,
        IRevealableReferenceNode {

    private List<IAopReference> reference;

    private boolean isBeanConfig = false;

    private static BeansModelLabelProvider labelProvider = new BeansModelLabelProvider();

    public AdvisedRootAopReference(List<IAopReference> reference) {
        this(reference, false);
    }

    public AdvisedRootAopReference(List<IAopReference> reference,
            boolean isBeanConfig) {
        this.reference = reference;
        this.isBeanConfig = isBeanConfig;
    }

    public IReferenceNode[] getChildren() {
        return new IReferenceNode[] { new AdvisedAopTargetMethod(
                this.reference, this.isBeanConfig) };
    }

    public Image getImage() {
        return labelProvider.getImage(reference.get(0).getTargetBean());
    }

    public String getNodeName() {
        return reference.get(0).getTargetBean().getElementName()
                + " ["
                + reference.get(0).getResource().getProjectRelativePath()
                        .toString() + "]";
    }

    public List<IAopReference> getReference() {
        return reference;
    }

    public boolean hasChildren() {
        return true;
    }

    public void openAndReveal() {
        IResource resource = reference.get(0).getResource();
        SpringUIUtils.openInEditor((IFile) resource, reference.get(0)
                .getTargetBean().getElementStartLine());
    }

    public int getLineNumber() {
        return reference.get(0).getTargetBean().getElementStartLine();
    }

    public IResource getResource() {
        return reference.get(0).getTargetBean().getElementResource();
    }

}
