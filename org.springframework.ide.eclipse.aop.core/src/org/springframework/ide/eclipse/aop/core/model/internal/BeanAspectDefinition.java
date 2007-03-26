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
package org.springframework.ide.eclipse.aop.core.model.internal;

import java.lang.reflect.Method;

import org.eclipse.core.resources.IResource;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.beans.BeanUtils;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;
import org.springframework.ide.eclipse.aop.core.model.builder.AopReferenceModelBuilderUtils;
import org.springframework.util.StringUtils;

@SuppressWarnings("restriction")
public class BeanAspectDefinition implements IAspectDefinition {

	protected String adivceMethodName;

	protected String[] adivceMethodParameterTypes = new String[0];

	protected String[] argNames;

	protected String aspectClassName;

	protected int aspectLineNumber = -1;

	protected String aspectName;

	protected IDOMDocument document;

	protected IResource file;

	protected IDOMNode node;

	protected String returning;

	protected String throwing;

	protected String pointcutExpressionString = null;

	protected IAopReference.ADVICE_TYPES type;

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BeanAspectDefinition) {
			BeanAspectDefinition other = (BeanAspectDefinition) obj;
			return other.getNode().equals(node)
					&& other.getAdviceMethodName().equals(adivceMethodName)
					&& other.getAdviceMethodParameterTypes().equals(
							adivceMethodParameterTypes);
		}
		return false;
	}

	public String getAdviceMethodName() {
		return adivceMethodName;
	}

	public String[] getAdviceMethodParameterTypes() {
		return this.adivceMethodParameterTypes;
	}

	public String[] getArgNames() {
		return argNames;
	}

	public String getAspectClassName() {
		return aspectClassName;
	}

	public int getAspectLineNumber() {
		return aspectLineNumber;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.aop.ui.IBeanAspectDefinition#getAspectName()
	 */
	public String getAspectName() {
		return aspectName;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.aop.ui.IBeanAspectDefinition#getDocument()
	 */
	public IDOMDocument getDocument() {
		return document;
	}

	public IDOMNode getNode() {
		return node;
	}

	public Object getAspectJPointcutExpression() throws Throwable {
		return AopReferenceModelBuilderUtils
				.initAspectJExpressionPointcut(this);
	}

	public IResource getResource() {
		return file;
	}

	public String getReturning() {
		return returning;
	}

	public String getThrowing() {
		return throwing;
	}

	public IAopReference.ADVICE_TYPES getType() {
		return type;
	}

	@Override
	public int hashCode() {
		int hc = node.hashCode();
		hc = 23 * hc + type.hashCode();
		hc = 25 * hc + aspectLineNumber;
		return hc;
	}

	public void setAdviceMethodName(String adivceMethodName) {
		this.adivceMethodName = adivceMethodName;
	}

	public void setArgNames(String[] argNames) {
		this.argNames = argNames;
	}

	public void setAspectClassName(String aspectClassName) {
		this.aspectClassName = aspectClassName;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.aop.ui.IBeanAspectDefinition#setAspectName(java.lang.String)
	 */
	public void setAspectName(String aspectName) {
		if (!StringUtils.hasText(aspectName)) {
			this.aspectName = "anonymous aspect";
		}
		else {
			this.aspectName = aspectName;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.aop.ui.IBeanAspectDefinition#setDocument(org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument)
	 */
	public void setDocument(IDOMDocument document) {
		this.document = document;
		this.aspectLineNumber = this.document.getStructuredDocument()
				.getLineOfOffset(this.node.getStartOffset()) + 1;
	}

	public void setNode(IDOMNode node) {
		this.node = node;
	}

	public void setResource(IResource file) {
		this.file = file;
	}

	public void setReturning(String returning) {
		this.returning = returning;
	}

	public void setThrowing(String throwing) {
		this.throwing = throwing;
	}

	public void setType(IAopReference.ADVICE_TYPES type) {
		this.type = type;
	}

	public String getPointcutExpression() {
		return this.pointcutExpressionString;
	}

	public void setPointcutExpression(String expression) {
		this.pointcutExpressionString = expression;
	}

	public Method getAdviceMethod() {
		try {
			Class<?> aspectClass = AopReferenceModelBuilderUtils
					.loadClass(this.aspectClassName);
			Method method = BeanUtils.resolveSignature(this.adivceMethodName,
					aspectClass);
			return method;
		}
		catch (ClassNotFoundException e) {
			return null;
		}
	}

	public void setAdviceMethodParameterTypes(String[] params) {
		this.adivceMethodParameterTypes = params;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Aspect definition");
		if (this.file != null) {
			buf.append(" [");
			buf.append(this.file.getFullPath().toFile());
			buf.append(":");
			buf.append(getAspectLineNumber());
			buf.append("]");
		}
		buf.append(" advise type [");
		ADVICE_TYPES type = getType();
		if (type == ADVICE_TYPES.AFTER) {
			buf.append("after");
		}
		else if (type == ADVICE_TYPES.AFTER_RETURNING) {
			buf.append("after-returning");
		}
		else if (type == ADVICE_TYPES.AFTER_THROWING) {
			buf.append("after-throwing");
		}
		else if (type == ADVICE_TYPES.BEFORE) {
			buf.append("before");
		}
		else if (type == ADVICE_TYPES.AROUND) {
			buf.append("after");
		}
		else if (type == ADVICE_TYPES.DECLARE_PARENTS) {
			buf.append("delcare parents");
		}
		buf.append("] advise [");
		buf.append(getAspectClassName());
		if (type != ADVICE_TYPES.DECLARE_PARENTS) {
			buf.append(".");
			buf.append(getAdviceMethodName());
		}
		buf.append("]");
		return buf.toString();
	}
}
