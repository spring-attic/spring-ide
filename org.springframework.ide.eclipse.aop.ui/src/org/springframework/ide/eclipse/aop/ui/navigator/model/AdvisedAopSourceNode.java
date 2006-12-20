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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.AopUIImages;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

public class AdvisedAopSourceNode implements IReferenceNode,
        IRevealableReferenceNode {

    private IAopReference reference;

    public AdvisedAopSourceNode(IAopReference reference) {
        this.reference = reference;
    }

    public IReferenceNode[] getChildren() {
        return new IReferenceNode[] { new AdvisedAopSourceMethodNode(reference) };
    }

    public Image getImage() {
        ADVICE_TYPES type = reference.getAdviceType();
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
        ADVICE_TYPES type = reference.getAdviceType();
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
        text += reference.getDefinition().getAspectName();
        text += "> [";
        text += reference.getResource().getProjectRelativePath().toString();
        text += "]";
        return text;
    }

    public boolean hasChildren() {
        return true;
    }

    public void openAndReveal() {
        IResource resource = reference.getResource();
        SpringUIUtils.openInEditor((IFile) resource, reference.getDefinition()
                .getAspectLineNumber());
    }

    public IAopReference getReference() {
        return this.reference;
    }

    public int getLineNumber() {
        return reference.getDefinition().getAspectLineNumber();
    }

    public IResource getResource() {
        return reference.getResource();
    }

}
