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

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;
import org.springframework.ide.eclipse.aop.core.util.BeansAopUtils;
import org.springframework.ide.eclipse.aop.ui.navigator.util.BeansAopNavigatorUtils;

public class AdviceDeclareParentAopSourceFieldNode implements IReferenceNode,
        IRevealableReferenceNode {

    private List<IAopReference> references;

    public AdviceDeclareParentAopSourceFieldNode(List<IAopReference> reference) {
        this.references = reference;
    }

    public IReferenceNode[] getChildren() {
        return new IReferenceNode[] { new AdviceDeclareParentAopReferenceNode(
                references) };
    }

    public Image getImage() {
        return BeansAopNavigatorUtils.JAVA_LABEL_PROVIDER.getImage(references
                .get(0).getSource());
    }

    public String getText() {
        if (references.get(0).getAdviceType() == ADVICE_TYPES.DECLARE_PARENTS) {
            return BeansAopUtils.getJavaElementLinkName(references.get(0)
                    .getSource())
                    + " - "
                    + BeansAopUtils.getPackageLinkName(references.get(0)
                            .getSource());
        }
        else {
            return BeansAopUtils.getJavaElementLinkName(references.get(0)
                    .getSource().getParent())
                    + "."
                    + BeansAopUtils.getJavaElementLinkName(references.get(0)
                            .getSource())
                    + " - "
                    + BeansAopUtils.getPackageLinkName(references.get(0)
                            .getSource());
        }
    }

    public boolean hasChildren() {
        return false;
    }

    public void openAndReveal() {
        IEditorPart p;
        try {
            IJavaElement element = references.get(0).getSource();
            p = JavaUI.openInEditor(element);
            JavaUI.revealInEditor(p, element);
        }
        catch (Exception e) {
        }
    }

    public int getLineNumber() {
        return BeansAopNavigatorUtils.getLineNumber(references.get(0)
                .getSource());
    }

    public IResource getResource() {
        return references.get(0).getSource().getResource();
    }

}
