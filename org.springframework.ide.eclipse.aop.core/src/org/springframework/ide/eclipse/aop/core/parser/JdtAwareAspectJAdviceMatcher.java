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
package org.springframework.ide.eclipse.aop.core.parser;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.aop.aspectj.AbstractAspectJAdvice;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.ide.eclipse.aop.core.util.BeansAopUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;

class JdtAwareAspectJAdviceMatcher
        extends AbstractAspectJAdvice {

    private IType type;

    private IProject project;

    public JdtAwareAspectJAdviceMatcher(IProject project,
            Method aspectJAdviceMethod,
            AspectJExpressionPointcut pointcutExpression) throws Exception {
        super(aspectJAdviceMethod, pointcutExpression, null);
        String name = aspectJAdviceMethod.getDeclaringClass().getName();
        this.type = BeansModelUtils.getJavaType(project, name);
        this.project = project;
    }

    public List<IMethod> getMatches(Class<?> clazz) {
        IType jdtTargetClass = BeansModelUtils.getJavaType(project, clazz
                .getName());
        Method[] methods = clazz.getDeclaredMethods();
        List<IMethod> matchingMethod = new ArrayList<IMethod>();
        for (Method method : methods) {
            if (getPointcut().matches(method, clazz)) {
                IMethod jdtMethod = BeansAopUtils
                        .getMethod(jdtTargetClass, method.getName(), method
                                .getParameterTypes().length);
                if (jdtMethod != null) {
                    matchingMethod.add(jdtMethod);
                }
            }
        }
        return matchingMethod;
    }

    public boolean isAfterAdvice() {
        return true;
    }

    public boolean isBeforeAdvice() {
        return true;
    }

    protected ParameterNameDiscoverer createParameterNameDiscoverer() {
        return new JdtParameterNameDiscoverer(this.type);
    }

    public void setReturningName(String name) {
        setReturningNameNoCheck(name);
    }

    public void setThrowingName(String name) {
        setThrowingNameNoCheck(name);
    }
    
    class JdtParameterNameDiscoverer implements ParameterNameDiscoverer {

        private IType type;

        public JdtParameterNameDiscoverer(IType type) {
            this.type = type;
        }

        public String[] getParameterNames(Method method) {
            String methodName = method.getName();
            int argCount = method.getParameterTypes().length;
            IMethod jdtMethod;
            try {
                jdtMethod = BeansAopUtils.getMethod(type, methodName, argCount);
                return jdtMethod.getParameterNames();
            }
            catch (JavaModelException e) {
                // suppress this
            }
            return null;
        }

        @SuppressWarnings("unchecked")
		public String[] getParameterNames(Constructor ctor) {
            return null;
        }
    }
}