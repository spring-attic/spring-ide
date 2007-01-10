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
import java.util.List;

import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IIntroductionDefinition;

public class AdvisedRootAopReferenceNode extends BeanReferenceNode implements IReferenceNode,
        IRevealableReferenceNode {

    private List<IAopReference> reference;

    private boolean isBeanConfig = false;

    public AdvisedRootAopReferenceNode(List<IAopReference> reference) {
        this(reference, false);
    }

    public AdvisedRootAopReferenceNode(List<IAopReference> reference,
            boolean isBeanConfig) {
        super(reference.get(0).getTargetBean());
        this.reference = reference;
        this.isBeanConfig = isBeanConfig;
    }

    public IReferenceNode[] getChildren() {
        if (this.isBeanConfig) {
            return new IReferenceNode[] { new AdvisedAopTargetClassNode(
                this.reference, this.isBeanConfig) };
        }
        else {
            List<IAopReference> introNodes = new ArrayList<IAopReference>();
            List<IAopReference> adviceNodes = new ArrayList<IAopReference>();
            List<IReferenceNode> nodes = new ArrayList<IReferenceNode>();
            for (IAopReference r : this.reference) {
                if (r.getDefinition() instanceof IIntroductionDefinition) {
                    introNodes.add(r);
                }
                else {
                    adviceNodes.add(r);
                }
            }
            if (introNodes.size() > 0) {
                nodes.add(new AdvisedAopReferenceNode(introNodes));
            }
            if (adviceNodes.size() > 0) {
                nodes.add(new AdvisedAopTargetMethodNode(adviceNodes, isBeanConfig));
            }
            return nodes.toArray(new IReferenceNode[nodes.size()]);
        }
    }

    public List<IAopReference> getReference() {
        return reference;
    }

    public boolean hasChildren() {
        return true;
    }
}
