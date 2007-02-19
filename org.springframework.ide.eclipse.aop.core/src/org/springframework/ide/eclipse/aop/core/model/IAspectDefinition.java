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

    //Class<?> getAdviceClass() throws ClassNotFoundException;

    Method getAdviceMethod();

    String getAspectName();

    String getAspectClassName();

    int getAspectLineNumber();
    
    String getAdviceMethodName();
    
    String[] getAdviceMethodParameterTypes();
    
    IResource getResource();

    IAopReference.ADVICE_TYPES getType();

    void setAspectName(String aspectName);

    void setAspectClassName(String className);
    
    void setAdviceMethodName(String methodname);
    
    void setAdviceMethodParameterTypes(String[] params);

    void setType(IAopReference.ADVICE_TYPES type);
    
    void setResource(IResource file);
    
    void setDocument(IDOMDocument document);
    
    Object getAspectJPointcutExpression() throws Throwable;
    
    String getReturning();
    
    void setReturning(String returning);
    
    String getThrowing();
    
    void setThrowing(String throwable);

    String[] getArgNames();
    
    void setArgNames(String[] argNames);
    
    String getPointcutExpression();
    
}