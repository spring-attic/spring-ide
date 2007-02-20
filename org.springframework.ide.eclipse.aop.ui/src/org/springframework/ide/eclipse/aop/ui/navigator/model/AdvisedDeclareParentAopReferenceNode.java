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
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;

public class AdvisedDeclareParentAopReferenceNode implements IReferenceNode {

	private List<IAopReference> references;

	public AdvisedDeclareParentAopReferenceNode(List<IAopReference> reference) {
		this.references = reference;
	}

	public IReferenceNode[] getChildren() {
		List<IReferenceNode> nodes = new ArrayList<IReferenceNode>();
		Map<IAspectDefinition, List<IAopReference>> dRefs = new HashMap<IAspectDefinition, List<IAopReference>>();
		for (IAopReference r : references) {
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
			nodes.add(new AdvisedDeclareParentAopSourceNode(entry.getValue()));
		}
		return nodes.toArray(new IReferenceNode[nodes.size()]);
	}

	public Image getImage() {
		return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_REFERENCE);
	}

	public String getText() {
		return "aspect declarations";
	}

	public boolean hasChildren() {
		return true;
	}

}
