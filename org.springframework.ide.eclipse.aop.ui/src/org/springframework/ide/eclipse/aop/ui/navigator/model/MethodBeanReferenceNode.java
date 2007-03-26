/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
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
import org.springframework.ide.eclipse.beans.core.model.IBean;

public class MethodBeanReferenceNode extends AbstractJavaElementReferenceNode
		implements IReferenceNode, IRevealableReferenceNode {

	private List<IAopReference> aspectReferences = new ArrayList<IAopReference>();

	private List<IAopReference> adviseReferences = new ArrayList<IAopReference>();

	public MethodBeanReferenceNode(IMember member,
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
			for (Map.Entry<IAspectDefinition, List<IAopReference>> entry : refs
					.entrySet()) {
				nodes.add(new AdviceAopTargetNode(entry.getValue()));
			}
		}
		if (this.adviseReferences.size() > 0) {
			Map<IBean, List<IAopReference>> refs = new HashMap<IBean, List<IAopReference>>();
			for (IAopReference r : this.adviseReferences) {
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
				nodes.add(new AdvisedAopTargetBeanNode(entry.getValue()));
			}
		}
		return nodes.toArray(new IReferenceNode[nodes.size()]);
	}

	@Override
	public String getText() {
		if (element instanceof IType) {
			return AopReferenceModelNavigatorUtils.JAVA_LABEL_PROVIDER
					.getText(element)
					+ " - "
					+ AopReferenceModelUtils.getPackageLinkName(element);
		}
		else {
			return AopReferenceModelNavigatorUtils.JAVA_LABEL_PROVIDER
					.getText(element);
		}
	}

	@Override
	public boolean hasChildren() {
		return this.aspectReferences.size() > 0
				|| this.adviseReferences.size() > 0;
	}
}
