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
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.AopUIImages;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

public class AdviceRootAopReferenceNode implements IReferenceNode,
        IRevealableReferenceNode {

    private List<IAopReference> reference;

    private boolean isBeanConfig = false;

    public AdviceRootAopReferenceNode(List<IAopReference> reference) {
        this(reference, false);
    }

    public AdviceRootAopReferenceNode(List<IAopReference> reference,
            boolean isBeanConfig) {
        this.reference = reference;
        this.isBeanConfig = isBeanConfig;
    }

    public IReferenceNode[] getChildren() {
        return new IReferenceNode[] { new AdviceAopSourceMethodNode(this.reference,
                this.isBeanConfig) };
    }

    public Image getImage() {
        ADVICE_TYPES type = reference.get(0).getAdviceType();
        if (type == ADVICE_TYPES.AFTER || type == ADVICE_TYPES.AFTER_RETURNING
                || type == ADVICE_TYPES.AFTER_THROWING) {
            return AopUIImages.getImage(AopUIImages.IMG_OBJS_AFTER_ADVICE);
        }
        else if (type == ADVICE_TYPES.BEFORE) {
            return AopUIImages.getImage(AopUIImages.IMG_OBJS_BEFORE_ADVICE);
        }
        else if (type == ADVICE_TYPES.AROUND) {
            return AopUIImages.getImage(AopUIImages.IMG_OBJS_ASPECT);
        }
        return null;
    }

    public String getText() {
        ADVICE_TYPES type = reference.get(0).getAdviceType();
        String text = "";
        if (type == ADVICE_TYPES.AFTER) {
            text += "after()";
        }
        else if (type == ADVICE_TYPES.AFTER_RETURNING) {
            text += "after-returning()";
        }
        else if (type == ADVICE_TYPES.AFTER_THROWING) {
            text += "after-throwing()";
        }
        else if (type == ADVICE_TYPES.BEFORE) {
            text += "before()";
        }
        else if (type == ADVICE_TYPES.AROUND) {
            text += "around()";
        }
        text += " <";
        text += reference.get(0).getDefinition().getAspectName();
        text += "> [";
        text += reference.get(0).getResource().getProjectRelativePath()
                .toString();
        text += "]";
        return text;
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
                .getDefinition().getAspectLineNumber());
    }

    public int getLineNumber() {
        return reference.get(0).getDefinition().getAspectLineNumber();
    }

    public IResource getResource() {
        return reference.get(0).getResource();
    }

}
