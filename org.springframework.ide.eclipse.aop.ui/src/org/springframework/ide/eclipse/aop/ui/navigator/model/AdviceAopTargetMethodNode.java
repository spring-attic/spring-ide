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

import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;
import org.springframework.ide.eclipse.aop.core.util.BeansAopUtils;

public class AdviceAopTargetMethodNode extends AbstractJavaElementRefeerenceNode implements IReferenceNode,
        IRevealableReferenceNode {

    private IAopReference reference;

    public AdviceAopTargetMethodNode(IAopReference reference) {
        super(reference.getTarget());
    	this.reference = reference;
    }

    public IReferenceNode[] getChildren() {
        return new IReferenceNode[0];
    }

    public String getText() {
        if (reference.getAdviceType() == ADVICE_TYPES.DECLARE_PARENTS) {
            return BeansAopUtils.getJavaElementLinkName(reference.getTarget())
                    + " - "
                    + BeansAopUtils.getPackageLinkName(reference.getTarget());
        }
        else {
            return BeansAopUtils.getJavaElementLinkName(reference.getTarget()
                    .getParent())
                    + "."
                    + BeansAopUtils.getJavaElementLinkName(reference
                            .getTarget())
                    + " - "
                    + BeansAopUtils.getPackageLinkName(reference.getTarget());
        }
    }

    public boolean hasChildren() {
        return false;
    }
}
