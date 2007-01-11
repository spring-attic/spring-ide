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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.util.BeansAopUtils;
import org.springframework.ide.eclipse.aop.ui.navigator.util.BeansAopNavigatorUtils;

public class ClassMethodReferenceNode implements IReferenceNode, IRevealableReferenceNode {

    protected IJavaElement element;

    private List<IReferenceNode> children;

    private List<IAopReference> declareParentReferences = new ArrayList<IAopReference>();

    private List<IAopReference> declaredOnReferences = new ArrayList<IAopReference>();

    public List<IAopReference> getDeclaredOnReferences() {
        return declaredOnReferences;
    }

    public List<IAopReference> getDeclareParentReferences() {
        return declareParentReferences;
    }

    @SuppressWarnings("unchecked")
    public ClassMethodReferenceNode(IMember member, List<?> children) {
        this.element = member;
        this.children = (List<IReferenceNode>) children;
    }

    public IReferenceNode[] getChildren() {
        List<IReferenceNode> nodes = new ArrayList<IReferenceNode>();
        // add method children
        if (this.children != null && this.children.size() > 0) {
            nodes.addAll(this.children);
        }
        Map<IAspectDefinition, List<IAopReference>> dRefs = new HashMap<IAspectDefinition, List<IAopReference>>();
        for (IAopReference r : getDeclareParentReferences()) {
            if (dRefs.containsKey(r.getDefinition())) {
                dRefs.get(r.getDefinition()).add(r);
            }
            else {
                List<IAopReference> ref = new ArrayList<IAopReference>();
                ref.add(r);
                dRefs.put(r.getDefinition(), ref);
            }
        }
        for (Map.Entry<IAspectDefinition, List<IAopReference>> entry : dRefs.entrySet()) {
            nodes.add(new AdviceDeclareParentAopSourceNode(entry.getValue()));
        }
        if (getDeclaredOnReferences().size() > 0) {
            nodes.add(new AdvisedDeclareParentAopReferenceNode(getDeclaredOnReferences()));
        }
        return nodes.toArray(new IReferenceNode[nodes.size()]);
    }

    public Image getImage() {
        return BeansAopNavigatorUtils.JAVA_LABEL_PROVIDER.getImage(element);
    }

    public String getText() {
        if (element instanceof IType) {
            return BeansAopNavigatorUtils.JAVA_LABEL_PROVIDER.getText(element) + " - "
                    + BeansAopUtils.getPackageLinkName(element);
        }
        else {
            return BeansAopNavigatorUtils.JAVA_LABEL_PROVIDER.getText(element);
        }
    }

    public boolean hasChildren() {
        return (children != null && children.size() > 0) || declareParentReferences.size() > 0
                || declaredOnReferences.size() > 0;
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
