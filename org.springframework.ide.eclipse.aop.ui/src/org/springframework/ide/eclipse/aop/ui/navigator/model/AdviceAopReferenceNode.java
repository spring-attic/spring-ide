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

import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;

public class AdviceAopReferenceNode implements IReferenceNode {

    private List<IAopReference> references;

    public AdviceAopReferenceNode(List<IAopReference> reference) {
        this.references = reference;
    }

    public IReferenceNode[] getChildren() {
        List<IReferenceNode> nodes = new ArrayList<IReferenceNode>();
        Map<IBean, List<IAopReference>> refs = new HashMap<IBean, List<IAopReference>>();
        for (IAopReference r : this.references) {
            if (refs.containsKey(r.getTargetBean())) {
                refs.get(r.getTargetBean()).add(r);
            }
            else {
                List<IAopReference> ref = new ArrayList<IAopReference>();
                ref.add(r);
                refs.put(r.getTargetBean(), ref);
            }
        }
        for (Map.Entry<IBean, List<IAopReference>> entry : refs.entrySet()) {
            nodes.add(new AdviceAopTargetBeanNode(entry.getValue()));
        }
        return nodes.toArray(new IReferenceNode[nodes.size()]);
    }

    public Image getImage() {
        return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_REFERENCE);
    }

    public String getText() {
        return "advises";
    }

    public boolean hasChildren() {
        return true;
    }

}
