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

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AopReferenceModelNode) {
			AopReferenceModelNode other = (AopReferenceModelNode) obj;
			return getLabel().equals(other.getLabel());
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(getLabel());
		return hashCode;
	}
}
