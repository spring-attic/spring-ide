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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.util.AopReferenceModelUtils;
import org.springframework.ide.eclipse.aop.ui.navigator.util.AopReferenceModelNavigatorUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;

public class ClassMethodReferenceNode extends AbstractJavaElementReferenceNode
		implements IReferenceNode, IRevealableReferenceNode {

	private List<IReferenceNode> children;

	private List<IReferenceNode> declareParentReferences = new ArrayList<IReferenceNode>();

	private List<IAopReference> declaredOnReferences = new ArrayList<IAopReference>();

	private Set<IBean> beans = new HashSet<IBean>();

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

	public void setBeans(Set<IBean> beans) {
		this.beans = beans;
	}
}
