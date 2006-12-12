/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.ide.eclipse.aop.ui;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;

@SuppressWarnings("restriction")
public class BeanAspectDefinition {

	private IAopReference.ADVICE_TYPES type;
	
	private String pointcut;

	private String[] argNames;

	private String throwing;

	private String returning;

	private IDOMNode node;

	private IDOMDocument document;

	private String method;

	private String className;

	private int lineNumber = -1;
    
    private String aspectName;

	public String getAspectName() {
        return aspectName;
    }

    public void setAspectName(String aspectName) {
        this.aspectName = aspectName;
    }

    public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String[] getArgNames() {
		return argNames;
	}

	public void setArgNames(String[] argNames) {
		this.argNames = argNames;
	}

	public String getPointcut() {
		return pointcut;
	}

	public void setPointcut(String pointcut) {
		this.pointcut = pointcut;
	}

	public String getReturning() {
		return returning;
	}

	public void setReturning(String returning) {
		this.returning = returning;
	}

	public String getThrowing() {
		return throwing;
	}

	public void setThrowing(String throwing) {
		this.throwing = throwing;
	}

	public IAopReference.ADVICE_TYPES getType() {
		return type;
	}

	public void setType(IAopReference.ADVICE_TYPES type) {
		this.type = type;
	}

	public IDOMDocument getDocument() {
		return document;
	}

	public void setDocument(IDOMDocument document) {
		this.document = document;
	}

	public IDOMNode getNode() {
		return node;
	}

	public void setNode(IDOMNode node) {
		this.node = node;
	}

	public int getLineNumber() {
		if (this.lineNumber == -1) {
			this.lineNumber = this.document.getStructuredDocument()
					.getLineOfOffset(this.node.getStartOffset()) + 1;
		}
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

}