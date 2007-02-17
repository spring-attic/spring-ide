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

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.util.AopReferenceModelUtils;
import org.springframework.ide.eclipse.aop.ui.navigator.util.AopReferenceModelNavigatorUtils;

public class BeanClassTargetReferenceNode extends AbstractJavaElementReferenceNode implements
		IReferenceNode, IRevealableReferenceNode {

	protected BeanReferenceNode parent;

	public BeanClassTargetReferenceNode(IMember member, BeanReferenceNode parent) {
		super(member);
		this.parent = parent;
	}

	public IReferenceNode[] getChildren() {
		List<IReferenceNode> nodes = new ArrayList<IReferenceNode>();
		Map<IMember, MethodReference> refs = new HashMap<IMember, MethodReference>();
		for (IAopReference reference : parent.getAspectReferences()) {
			if (refs.containsKey(reference.getSource())) {
				refs.get(reference.getSource()).getAspects().add(reference);
			} else {
				MethodReference r = new MethodReference();
				r.setMember(reference.getSource());
				r.getAspects().add(reference);
				refs.put(reference.getSource(), r);
			}
		}
		for (IAopReference reference : parent.getAdviseReferences()) {
			if (refs.containsKey(reference.getTarget())) {
				refs.get(reference.getTarget()).getAdvices().add(reference);
			} else {
				MethodReference r = new MethodReference();
				r.setMember(reference.getTarget());
				r.getAdvices().add(reference);
				refs.put(reference.getTarget(), r);
			}
		}
		for (Map.Entry<IMember, MethodReference> entry : refs.entrySet()) {
			nodes.add(new BeanMethodReferenceNode(entry.getKey(), entry.getValue().getAspects(),
					entry.getValue().getAdvices()));
		}
		Map<IAspectDefinition, List<IAopReference>> dRefs = new HashMap<IAspectDefinition, List<IAopReference>>();
		for (IAopReference r : parent.getDeclareParentReferences()) {
			if (dRefs.containsKey(r.getDefinition())) {
				dRefs.get(r.getDefinition()).add(r);
			} else {
				List<IAopReference> ref = new ArrayList<IAopReference>();
				ref.add(r);
				dRefs.put(r.getDefinition(), ref);
			}
		}
		for (Map.Entry<IAspectDefinition, List<IAopReference>> entry : dRefs.entrySet()) {
			nodes.add(new AdviceDeclareParentAopSourceNode(entry.getValue()));
		}
		if (parent.getDeclaredOnReferences().size() > 0) {
			nodes.add(new AdvisedDeclareParentAopReferenceNode(parent.getDeclaredOnReferences()));
		}

		return nodes.toArray(new IReferenceNode[nodes.size()]);
	}

	public String getText() {
		if (element instanceof IType) {
			return AopReferenceModelNavigatorUtils.JAVA_LABEL_PROVIDER.getText(element) + " - "
					+ AopReferenceModelUtils.getPackageLinkName(element);
		} else {
			return AopReferenceModelNavigatorUtils.JAVA_LABEL_PROVIDER.getText(element);
		}
	}

	public boolean hasChildren() {
		return parent.getAdviseReferences().size() > 0 || parent.getAspectReferences().size() > 0;
	}

}
