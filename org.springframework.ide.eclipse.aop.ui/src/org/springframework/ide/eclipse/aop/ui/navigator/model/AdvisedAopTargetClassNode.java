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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IMember;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IIntroductionDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;
import org.springframework.ide.eclipse.aop.core.util.BeansAopUtils;
import org.springframework.ide.eclipse.aop.ui.navigator.util.BeansAopNavigatorUtils;

public class AdvisedAopTargetClassNode
        extends BeanClassReferenceNode implements IReferenceNode, IRevealableReferenceNode {

    private List<IAopReference> references;

    private boolean isBeanConfig = false;

    public AdvisedAopTargetClassNode(List<IAopReference> reference, boolean isBeanConfig) {
        super((reference.get(0).getAdviceType() == ADVICE_TYPES.DECLARE_PARENTS ? reference.get(0)
                .getTarget() : (IMember) reference.get(0).getTarget().getParent()));
        this.references = reference;
        this.isBeanConfig = isBeanConfig;
    }

    public IReferenceNode[] getChildren() {
        List<IAopReference> introNodes = new ArrayList<IAopReference>();
        Map<IMember, List<IAopReference>> adviceNodes = new HashMap<IMember, List<IAopReference>>();
        List<IReferenceNode> nodes = new ArrayList<IReferenceNode>();
        for (IAopReference reference : this.references) {
            if (reference.getDefinition() instanceof IIntroductionDefinition) {
                introNodes.add(reference);
            }
            else {
                if (adviceNodes.containsKey(reference.getTarget())) {
                    adviceNodes.get(reference.getTarget()).add(reference);
                }
                else {
                    List<IAopReference> refs = new ArrayList<IAopReference>();
                    refs.add(reference);
                    adviceNodes.put(reference.getTarget(), refs);
                }
            }
        }
        if (introNodes.size() > 0) {
            nodes.add(new AdvisedAopReferenceNode(introNodes));
        }
        if (adviceNodes.size() > 0) {
            for (Map.Entry<IMember, List<IAopReference>> entry : adviceNodes.entrySet()) {
                nodes.add(new AdvisedAopTargetMethodNode(entry.getValue(), isBeanConfig));
            }
        }
        return nodes.toArray(new IReferenceNode[nodes.size()]);
    }

    public String getText() {
        if (isBeanConfig) {
            return BeansAopUtils.getJavaElementLinkName(element) + " -  "
                    + BeansAopUtils.getPackageLinkName(element);
        }
        else {
            return BeansAopNavigatorUtils.JAVA_LABEL_PROVIDER.getText(element);
        }
    }

    public boolean hasChildren() {
        return true;
    }
}
