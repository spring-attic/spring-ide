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
package org.springframework.ide.eclipse.aop.core.model;

import java.lang.reflect.Method;

import org.eclipse.core.resources.IResource;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;

@SuppressWarnings("restriction")
public interface IAspectDefinition {

	Method getAdviceMethod();

	String getAdviceMethodName();

	String[] getAdviceMethodParameterTypes();

	String[] getArgNames();

	String getAspectClassName();

	Object getAspectJPointcutExpression() throws Throwable;

	int getAspectLineNumber();

	String getAspectName();

	String getPointcutExpression();

	IResource getResource();

	String getReturning();

	String getThrowing();
	
	boolean isProxyTargetClass();

	IAopReference.ADVICE_TYPES getType();

	void setAdviceMethodName(String methodname);

	void setAdviceMethodParameterTypes(String[] params);

	void setArgNames(String[] argNames);

	void setAspectClassName(String className);

	void setAspectName(String aspectName);

	void setDocument(IDOMDocument document);

	void setResource(IResource file);

	void setReturning(String returning);

	void setThrowing(String throwable);

	void setType(IAopReference.ADVICE_TYPES type);
	
	void setProxyTargetClass(boolean proxyTargetClass);

}
