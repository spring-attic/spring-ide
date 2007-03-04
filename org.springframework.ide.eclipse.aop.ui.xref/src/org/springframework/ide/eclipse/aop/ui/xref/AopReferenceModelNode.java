/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.ide.eclipse.aop.ui.xref;

import org.eclipse.contribution.xref.core.IXReferenceNode;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.util.AopReferenceModelUtils;
import org.springframework.util.ObjectUtils;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class AopReferenceModelNode implements IAdaptable, IXReferenceNode {

	public enum TYPE {
		SOURCE, TARGET
	};

	private String label;

	private IAopReference reference;

	private TYPE type;

	public AopReferenceModelNode(TYPE type, IAopReference reference) {
		this.reference = reference;
		this.type = type;
		computeLabel();
	}

	private void computeLabel() {
		if (getJavaElement() != null) {
			if (getJavaElement() instanceof IMethod) {
				this.label = getJavaElement().getParent().getElementName()
						+ '.'
						+ AopReferenceModelUtils
								.readableName((IMethod) getJavaElement());
			}
			else {
				this.label = getJavaElement().getElementName();
			}
		}
		else {
			this.label = reference.getDefinition().getAspectName();
		}
	}

	/**
	 * @see IAdaptable#getAdapter(Class)
	 */
	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) {
			return AopReferenceModelNodeAdapter.getDefault();
		}
		return null;
	}

	public String getLabel() {
		return this.label;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.contribution.xref.core.IXReferenceNode#getJavaElement()
	 */
	public IJavaElement getJavaElement() {
		if (this.type.equals(TYPE.SOURCE)) {
			return this.reference.getSource();
		}
		else if (this.type.equals(TYPE.TARGET)) {
			return this.reference.getTarget();
		}
		return null;
	}

	public IAspectDefinition getDefinition() {
		return this.reference.getDefinition();
	}

	public IResource getResouce() {
		return this.reference.getResource();
	}

	public boolean equals(Object obj) {
		if (obj instanceof AopReferenceModelNode) {
			AopReferenceModelNode other = (AopReferenceModelNode) obj;
			return getLabel().equals(other.getLabel());
		}
		return false;
	}

	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(getLabel());
		return hashCode;
	}
}