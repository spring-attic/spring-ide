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
package org.springframework.ide.eclipse.aop.core.model.internal;

import java.lang.reflect.Method;

import org.eclipse.core.resources.IResource;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.beans.BeanUtils;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.parser.BeansAopModelUtils;
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

    public boolean equals(Object obj) {
        if (obj instanceof BeanAspectDefinition) {
            BeanAspectDefinition other = (BeanAspectDefinition) obj;
            return other.getNode().equals(node)
                    && other.getAdviceMethodName().equals(adivceMethodName)
                    && other.getAdviceMethodParameterTypes().equals(adivceMethodParameterTypes);
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
        return BeansAopModelUtils.initAspectJExpressionPointcut(this);
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
        this.aspectLineNumber = this.document.getStructuredDocument().getLineOfOffset(
                this.node.getStartOffset()) + 1;
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
            Class<?> aspectClass = BeansAopModelUtils.loadClass(this.aspectClassName);
            Method method = BeanUtils.resolveSignature(this.adivceMethodName, aspectClass);
            return method;
        }
        catch (ClassNotFoundException e) {
            return null;
        }
    }

    public void setAdviceMethodParameterTypes(String[] params) {
        this.adivceMethodParameterTypes = params;
    }
}