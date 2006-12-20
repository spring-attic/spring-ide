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

import java.util.List;

import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.ui.BeansAopUtils;
import org.springframework.ide.eclipse.aop.ui.navigator.util.BeansAopNavigatorUtils;

public class AdvisedAopTargetMethodNode
        extends BeanJavaElementReferenceNode implements IReferenceNode,
        IRevealableReferenceNode {

    private List<IAopReference> reference;

    private boolean isBeanConfig = false;

    public AdvisedAopTargetMethodNode(List<IAopReference> reference,
            boolean isBeanConfig) {
        super(reference.get(0).getTarget());
        this.reference = reference;
        this.isBeanConfig = isBeanConfig;
    }

    public IReferenceNode[] getChildren() {
        return new IReferenceNode[] { new AdvisedAopReferenceNode(
                this.reference) };
    }

    public String getText() {
        if (isBeanConfig) {
            return BeansAopUtils.getJavaElementLinkName(reference.get(0)
                    .getTarget())
                    + " - "
                    + BeansAopUtils.getPackageLinkName(reference.get(0)
                            .getTarget());
        }
        else {
            return BeansAopNavigatorUtils.JAVA_LABEL_PROVIDER.getText(reference
                    .get(0).getTarget());
        }
    }

    public boolean hasChildren() {
        return true;
    }
}
