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
import java.util.List;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.util.BeansAopUtils;
import org.springframework.ide.eclipse.aop.ui.navigator.util.BeansAopNavigatorUtils;

public class ClassMethodReferenceNode extends AbstractJavaElementReferenceNode implements
		IReferenceNode, IRevealableReferenceNode {

	private List<IReferenceNode> children;

	private List<IReferenceNode> declareParentReferences = new ArrayList<IReferenceNode>();

	private List<IAopReference> declaredOnReferences = new ArrayList<IAopReference>();

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
			nodes.add(new AdvisedDeclareParentAopReferenceNode(getDeclaredOnReferences()));
		}
		return nodes.toArray(new IReferenceNode[nodes.size()]);
	}

	public String getText() {
		if (element instanceof IType) {
			return BeansAopNavigatorUtils.JAVA_LABEL_PROVIDER.getText(element) + " - "
					+ BeansAopUtils.getPackageLinkName(element);
		} else {
			return BeansAopNavigatorUtils.JAVA_LABEL_PROVIDER.getText(element);
		}
	}

	public boolean hasChildren() {
		return (children != null && children.size() > 0) || declareParentReferences.size() > 0
				|| declaredOnReferences.size() > 0;
	}
}
