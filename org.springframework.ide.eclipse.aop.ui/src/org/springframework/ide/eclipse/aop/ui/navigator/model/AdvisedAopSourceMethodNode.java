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

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;
import org.springframework.ide.eclipse.aop.core.util.BeansAopUtils;
import org.springframework.ide.eclipse.aop.ui.navigator.util.BeansAopNavigatorUtils;

public class AdvisedAopSourceMethodNode implements IReferenceNode,
        IRevealableReferenceNode {

    private IAopReference reference;

    public AdvisedAopSourceMethodNode(IAopReference reference) {
        this.reference = reference;
    }

    public IReferenceNode[] getChildren() {
        return new IReferenceNode[0];
    }

    public Image getImage() {
        return BeansAopNavigatorUtils.JAVA_LABEL_PROVIDER.getImage(reference
                .getSource());
    }

    public String getText() {
        if (reference.getAdviceType() == ADVICE_TYPES.DECLARE_PARENTS) {
            return BeansAopUtils.getJavaElementLinkName(reference.getSource())
                    + " - "
                    + BeansAopUtils.getPackageLinkName(reference.getSource());
        }
        else {
            return BeansAopUtils.getJavaElementLinkName(reference.getSource()
                    .getParent())
                    + "."
                    + BeansAopUtils.getJavaElementLinkName(reference
                            .getSource())
                    + " - "
                    + BeansAopUtils.getPackageLinkName(reference.getSource());
        }
    }

    public boolean hasChildren() {
        return false;
    }

    public void openAndReveal() {
        IEditorPart p;
        try {
            IJavaElement element = reference.getSource();
            p = JavaUI.openInEditor(element);
            JavaUI.revealInEditor(p, element);
        }
        catch (Exception e) {
        }
    }

    public int getLineNumber() {
        return BeansAopNavigatorUtils.getLineNumber(reference.getSource());
    }

    public IResource getResource() {
        return reference.getSource().getResource();
    }
    
    public IAopReference getReference() {
        return this.reference;
    }
}
