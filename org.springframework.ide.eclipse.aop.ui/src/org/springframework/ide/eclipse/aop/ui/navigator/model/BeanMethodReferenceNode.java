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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.util.BeansAopUtils;
import org.springframework.ide.eclipse.aop.ui.navigator.util.BeansAopNavigatorUtils;

public class BeanMethodReferenceNode extends AbstractJavaElementRefeerenceNode implements IReferenceNode,
        IRevealableReferenceNode {

    private List<IAopReference> aspectReferences = new ArrayList<IAopReference>();

    private List<IAopReference> adviseReferences = new ArrayList<IAopReference>();

    public BeanMethodReferenceNode(IMember member,
            List<IAopReference> aspectReferences,
            List<IAopReference> adviseReferences) {
    	super(member);
    	this.aspectReferences = aspectReferences;
        this.adviseReferences = adviseReferences;
    }

    public IReferenceNode[] getChildren() {
        List<IReferenceNode> nodes = new ArrayList<IReferenceNode>();
        if (this.aspectReferences.size() > 0) {
            Map<IAspectDefinition, List<IAopReference>> refs = new HashMap<IAspectDefinition, List<IAopReference>>();
            for (IAopReference r : this.aspectReferences) {
                if (refs.containsKey(r.getDefinition())) {
                    refs.get(r.getDefinition()).add(r);
                }
                else {
                    List<IAopReference> ref = new ArrayList<IAopReference>();
                    ref.add(r);
                    refs.put(r.getDefinition(), ref);
                }
            }
            for (Map.Entry<IAspectDefinition, List<IAopReference>> entry : refs.entrySet()) {
                nodes.add(new AdviceAopTargetNode(entry.getValue()));
            }
        }
        if (this.adviseReferences.size() > 0) {
            nodes.add(new AdvisedAopReferenceNode(this.adviseReferences));
        }
        return nodes.toArray(new IReferenceNode[nodes.size()]);
    }

    public String getText() {
        if (element instanceof IType) {
            return BeansAopNavigatorUtils.JAVA_LABEL_PROVIDER.getText(element)
                    + " - " + BeansAopUtils.getPackageLinkName(element);
        }
        else {
            return BeansAopNavigatorUtils.JAVA_LABEL_PROVIDER.getText(element);
        }
    }

    public boolean hasChildren() {
        return this.aspectReferences.size() > 0
                || this.adviseReferences.size() > 0;
    }

    public IJavaElement getJavaElement() {
        return this.element;
    }
}
