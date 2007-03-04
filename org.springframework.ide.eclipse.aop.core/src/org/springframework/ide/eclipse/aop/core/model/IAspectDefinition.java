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

}