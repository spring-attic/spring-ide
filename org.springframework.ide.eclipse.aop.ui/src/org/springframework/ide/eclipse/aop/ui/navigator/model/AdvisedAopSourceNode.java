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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IIntroductionDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.aop.AopUIImages;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

public class AdvisedAopSourceNode implements IReferenceNode,
        IRevealableReferenceNode {

    private IAopReference references;

    public AdvisedAopSourceNode(IAopReference reference) {
        this.references = reference;
    }

    public IReferenceNode[] getChildren() {
        return new IReferenceNode[] { new AdvisedAopSourceMethodNode(references) };
    }

    public Image getImage() {
        ADVICE_TYPES type = references.getAdviceType();
        if (type == ADVICE_TYPES.AFTER || type == ADVICE_TYPES.AFTER_RETURNING
                || type == ADVICE_TYPES.AFTER_THROWING) {
            return AopUIImages.getImage(AopUIImages.IMG_OBJS_AFTER_ADVICE);
        }
        else if (type == ADVICE_TYPES.BEFORE) {
            return AopUIImages.getImage(AopUIImages.IMG_OBJS_BEFORE_ADVICE);
        }
        else if (type == ADVICE_TYPES.AROUND) {
            return AopUIImages.getImage(AopUIImages.IMG_OBJS_AROUND_ADVICE);
        }
        else if (type == ADVICE_TYPES.DECLARE_PARENTS) {
            return AopUIImages.getImage(AopUIImages.IMG_OBJS_INTRODUCTION);
        }
        return null;
    }

    public String getText() {
        ADVICE_TYPES type = references.getAdviceType();
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
        else if (type == ADVICE_TYPES.DECLARE_PARENTS) {
            try {
                text += "declare parents: implements "
                        + ((IIntroductionDefinition) references
                                .getDefinition()).getImplInterfaceClass()
                                .getSimpleName();
            }
            catch (ClassNotFoundException e) {
                text += "declare parents";
            }
        }
        text += " <";
        text += references.getDefinition().getAspectName();
        text += "> [";
        text += references.getDefinition().getResource()
                .getProjectRelativePath().toString();
        text += "]";
        return text;
    }

    public boolean hasChildren() {
        return references.getSource() != null;
    }

    public void openAndReveal() {
        IResource resource = references.getDefinition().getResource();
        SpringUIUtils.openInEditor((IFile) resource, references.getDefinition().getAspectLineNumber());
    }

    public IAopReference getReference() {
        return this.references;
    }

    public int getLineNumber() {
        return references.getDefinition().getAspectLineNumber();
    }

    public IResource getResource() {
        return references.getResource();
    }

}
