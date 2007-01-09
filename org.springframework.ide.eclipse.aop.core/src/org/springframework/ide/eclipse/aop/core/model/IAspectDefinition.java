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
package org.springframework.ide.eclipse.aop.core.model;

import java.lang.reflect.Method;

import org.eclipse.core.resources.IResource;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;

public interface IAspectDefinition {

    Class getAdviceClass() throws ClassNotFoundException;

    Method getAdviceMethod() throws ClassNotFoundException;

    String getAspectName();

    String getClassName();

    int getAspectLineNumber();
    
    String getReturning();

    String getThrowing();
    
    String getMethod();
    
    IResource getResource();

    IAopReference.ADVICE_TYPES getType();

    void setArgNames(String[] argNames);

    void setAspectName(String aspectName);

    void setClassName(String className);

    void setMethod(String method);

    void setPointcut(String pointcut);

    void setReturning(String returning);

    void setThrowing(String throwing);

    void setType(IAopReference.ADVICE_TYPES type);
    
    void setResource(IResource file);
    
    AspectJExpressionPointcut getPointcut() throws ClassNotFoundException;

}