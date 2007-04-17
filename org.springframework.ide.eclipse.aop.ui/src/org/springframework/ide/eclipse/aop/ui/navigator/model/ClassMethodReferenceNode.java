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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.util.AopReferenceModelUtils;
import org.springframework.ide.eclipse.aop.ui.navigator.util.AopReferenceModelNavigatorUtils;

public class ClassMethodReferenceNode extends AbstractJavaElementReferenceNode
		implements IReferenceNode, IRevealableReferenceNode {

	private List<IReferenceNode> children;

	private List<IReferenceNode> declareParentReferences = new ArrayList<IReferenceNode>();

	private List<IAopReference> declaredOnReferences = new ArrayList<IAopReference>();

	private Set<String> beans = new HashSet<String>();

	public List<IAopReference> getDeclaredOnReferences() {
		return declaredOnReferences;
	}

	public List<IReferenceNode> getDeclareParentReferences() {
		return declareParentReferences;
	}

	@SuppressWarnings("unchecked")
	public ClassMethodReferenceNode(IMember member, List<?> children) {
		super(member);
		this.children = (List<IReferenceNode>) children;
	}

	public IReferenceNode[] getChildren() {
		List<IReferenceNode> nodes = new ArrayList<IReferenceNode>();
		// add method children
		if (this.children != null && this.children.size() > 0) {
			nodes.addAll(this.children);
		}

		if (getDeclareParentReferences().size() > 0) {
			nodes.addAll(getDeclareParentReferences());
		}

		if (getDeclaredOnReferences().size() > 0) {
			nodes.add(new AdvisedDeclareParentAopReferenceNode(
					getDeclaredOnReferences()));
		}
		if (beans.size() > 0) {
			nodes.add(new ClassBeanReferenceNode(beans));
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
		return (children != null && children.size() > 0)
				|| declareParentReferences.size() > 0
				|| declaredOnReferences.size() > 0 || this.beans.size() > 0;
	}

	public void setBeans(Set<String> beans) {
		this.beans = beans;
	}
}
