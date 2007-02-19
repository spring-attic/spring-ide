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
import org.springframework.ide.eclipse.aop.core.model.IAnnotationAopDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IIntroductionDefinition;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

public class AdviceDeclareParentAopSourceNode implements IReferenceNode, IRevealableReferenceNode {

    private List<IAopReference> references;

    public AdviceDeclareParentAopSourceNode(List<IAopReference> reference) {
        this.references = reference;
    }

    public IReferenceNode[] getChildren() {
        if (references.get(0).getDefinition() instanceof IAnnotationAopDefinition) {
            return new IReferenceNode[] { new AdviceDeclareParentAopSourceFieldNode(references) };

        }
        else {
            return new IReferenceNode[] { new AdviceDeclareParentAopReferenceNode(references) };
        }
    }

    public Image getImage() {
        return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_INTRODUCTION);
    }

    public String getText() {
        String text = "";
        text += "declare parents:";
        text += " implements "
                + ((IIntroductionDefinition) references.get(0).getDefinition())
                        .getImplInterfaceName();
        text += " <";
        text += references.get(0).getDefinition().getAspectName();
        text += "> - ";
        text += references.get(0).getDefinition().getResource().getFullPath().toString();
        return text;
    }

    public boolean hasChildren() {
        return true;
    }

    public void openAndReveal() {
        IResource resource = references.get(0).getDefinition().getResource();
        SpringUIUtils.openInEditor((IFile) resource, references.get(0).getDefinition()
                .getAspectLineNumber());
    }

    public IAopReference getReference() {
        return this.references.get(0);
    }

    public int getLineNumber() {
        return references.get(0).getDefinition().getAspectLineNumber();
    }

    public IResource getResource() {
        return references.get(0).getResource();
    }

}
