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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.ui.BeansAopPlugin;

public class AdviceAopSourceMethod implements IReferenceNode, IRevealableReferenceNode {
    
    private ILabelProvider labelProvider;

    private List<IAopReference> reference;

    public AdviceAopSourceMethod(List<IAopReference> reference) {
        this.reference = reference;
        labelProvider = new DecoratingLabelProvider(
                new JavaElementLabelProvider(
                        JavaElementLabelProvider.SHOW_DEFAULT
                                | JavaElementLabelProvider.SHOW_SMALL_ICONS),
                BeansAopPlugin.getDefault().getWorkbench()
                        .getDecoratorManager().getLabelDecorator());
    }

    public IReferenceNode[] getChildren() {
        return new IReferenceNode[] { new AdviceAopReference(
                this.reference) };
    }


    public Image getImage() {
        return labelProvider.getImage(reference.get(0).getSource());
    }

    public String getNodeName() {
        return labelProvider.getText(reference.get(0).getSource());
    }
    
    public boolean hasChildren() {
        return true;
    }

    public void openAndReveal() {
        IEditorPart p;
        try {
            IJavaElement element = reference.get(0).getSource();
            p = JavaUI.openInEditor(element);
            JavaUI.revealInEditor(p, element);
        }
        catch (Exception e) {
        }        
    }

}
