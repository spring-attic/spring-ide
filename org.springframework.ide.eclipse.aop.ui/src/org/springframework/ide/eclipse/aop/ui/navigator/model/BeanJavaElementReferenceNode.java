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

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.springframework.ide.eclipse.aop.core.util.BeansAopUtils;
import org.springframework.ide.eclipse.aop.ui.navigator.util.BeansAopNavigatorUtils;

public class BeanJavaElementReferenceNode implements IReferenceNode,
        IRevealableReferenceNode {

    private IJavaElement element;

    public BeanJavaElementReferenceNode(IMember member) {
        this.element = member;
    }

    public IReferenceNode[] getChildren() {
        return new IReferenceNode[0];
    }

    public Image getImage() {
        return BeansAopNavigatorUtils.JAVA_LABEL_PROVIDER.getImage(element);
    }

    public String getText() {
        if (element instanceof IType) {
            return BeansAopNavigatorUtils.JAVA_LABEL_PROVIDER.getText(element)
                    + " - "
                    + BeansAopUtils.getPackageLinkName(element);
        }
        else {
            return BeansAopNavigatorUtils.JAVA_LABEL_PROVIDER.getText(element);
        }
    }

    public boolean hasChildren() {
        return false;
    }

    public void openAndReveal() {
        IEditorPart p;
        try {
            p = JavaUI.openInEditor(element);
            JavaUI.revealInEditor(p, element);
        }
        catch (Exception e) {
        }
    }

    public int getLineNumber() {
        return BeansAopNavigatorUtils.getLineNumber((IMember) element);
    }

    public IResource getResource() {
        return element.getResource();
    }

}
