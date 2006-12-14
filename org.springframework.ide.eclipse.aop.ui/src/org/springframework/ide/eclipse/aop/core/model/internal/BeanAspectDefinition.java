/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.ide.eclipse.aop.core.model.internal;

import java.lang.reflect.Method;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.beans.BeanUtils;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;

@SuppressWarnings("restriction")
public class BeanAspectDefinition implements IAspectDefinition {

    private IAopReference.ADVICE_TYPES type;

    private String pointcut;

    private String[] argNames;

    private String throwing;

    private String returning;

    private IDOMNode node;

    private IDOMDocument document;

    private String method;

    private String className;

    private int aspectLineNumber = -1;
    
    private String aspectName;

    private Class adviceClass;

    private Method adviceMethod;

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.aop.ui.IBeanAspectDefinition#getAspectName()
     */
    public String getAspectName() {
        return aspectName;
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.aop.ui.IBeanAspectDefinition#setAspectName(java.lang.String)
     */
    public void setAspectName(String aspectName) {
        this.aspectName = aspectName;
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.aop.ui.IBeanAspectDefinition#getClassName()
     */
    public String getClassName() {
        return className;
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.aop.ui.IBeanAspectDefinition#setClassName(java.lang.String)
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.aop.ui.IBeanAspectDefinition#getMethod()
     */
    public String getMethod() {
        return method;
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.aop.ui.IBeanAspectDefinition#setMethod(java.lang.String)
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.aop.ui.IBeanAspectDefinition#getArgNames()
     */
    public String[] getArgNames() {
        return argNames;
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.aop.ui.IBeanAspectDefinition#setArgNames(java.lang.String[])
     */
    public void setArgNames(String[] argNames) {
        this.argNames = argNames;
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.aop.ui.IBeanAspectDefinition#getPointcut()
     */
    public AspectJExpressionPointcut getPointcut() throws ClassNotFoundException {
        AspectJExpressionPointcut pc = new AspectJExpressionPointcut();
        pc.setPointcutDeclarationScope(getAdviceClass());
        pc.setExpression(this.pointcut);
        if (this.argNames != null) {
            pc.setParameterNames(this.argNames);
        }
        return pc;
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.aop.ui.IBeanAspectDefinition#setPointcut(java.lang.String)
     */
    public void setPointcut(String pointcut) {
        this.pointcut = pointcut;
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.aop.ui.IBeanAspectDefinition#getReturning()
     */
    public String getReturning() {
        return returning;
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.aop.ui.IBeanAspectDefinition#setReturning(java.lang.String)
     */
    public void setReturning(String returning) {
        this.returning = returning;
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.aop.ui.IBeanAspectDefinition#getThrowing()
     */
    public String getThrowing() {
        return throwing;
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.aop.ui.IBeanAspectDefinition#setThrowing(java.lang.String)
     */
    public void setThrowing(String throwing) {
        this.throwing = throwing;
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.aop.ui.IBeanAspectDefinition#getType()
     */
    public IAopReference.ADVICE_TYPES getType() {
        return type;
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.aop.ui.IBeanAspectDefinition#setType(org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES)
     */
    public void setType(IAopReference.ADVICE_TYPES type) {
        this.type = type;
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.aop.ui.IBeanAspectDefinition#getDocument()
     */
    public IDOMDocument getDocument() {
        return document;
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.aop.ui.IBeanAspectDefinition#setDocument(org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument)
     */
    public void setDocument(IDOMDocument document) {
        this.document = document;
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.aop.ui.IBeanAspectDefinition#getNode()
     */
    public IDOMNode getNode() {
        return node;
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.aop.ui.IBeanAspectDefinition#setNode(org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode)
     */
    public void setNode(IDOMNode node) {
        this.node = node;
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.aop.ui.IBeanAspectDefinition#getLineNumber()
     */
    public int getAspectLineNumber() {
        if (this.aspectLineNumber == -1) {
            this.aspectLineNumber = this.document.getStructuredDocument()
                    .getLineOfOffset(this.node.getStartOffset()) + 1;
        }
        return aspectLineNumber;
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.aop.ui.IBeanAspectDefinition#getAdviceClass()
     */
    public Class getAdviceClass() throws ClassNotFoundException {
        if (this.adviceClass == null) {
            this.adviceClass = Thread.currentThread().getContextClassLoader()
                    .loadClass(this.className);
        }
        return adviceClass;
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.aop.ui.IBeanAspectDefinition#getAdviceMethod()
     */
    public Method getAdviceMethod() throws ClassNotFoundException {
        if (this.adviceMethod == null) {
            this.adviceMethod = BeanUtils.resolveSignature(method,
                    getAdviceClass());
        }
        return adviceMethod;
    }

    public int getAspectBeanLineNumber() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getTargetBeanLineNumber() {
        // TODO Auto-generated method stub
        return 0;
    }

}