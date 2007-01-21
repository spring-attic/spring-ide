/*
 * Copyright 2004 DekaBank Deutsche Girozentrale. All rights reserved.
 */
package org.springframework.ide.eclipse.aop.core.model.internal;

import java.lang.reflect.Method;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.aspectj.TypePatternClassFilter;
import org.springframework.aop.support.ClassFilters;
import org.springframework.ide.eclipse.aop.core.model.IIntroductionDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;

public class BeanIntroductionDefinition
        extends BeanAspectDefinition implements IIntroductionDefinition {

    private Class<?> introducedInterface;

    private final String introducedInterfaceName;

    private final ClassFilter typePatternClassFilter;

    private Class<?> defaultImpl;

    private final String defaultImplName;
    
    private final String typePattern;

    public BeanIntroductionDefinition(String interfaceTypeName,
            String typePattern, String defaultImplName) {
        ClassFilter typePatternFilter = new TypePatternClassFilter(typePattern);

        // Excludes methods implemented.
        ClassFilter exclusion = new ClassFilter() {
            @SuppressWarnings("unchecked")
			public boolean matches(Class clazz) {
                try {
                    return !(getImplInterfaceClass().isAssignableFrom(clazz));
                }
                catch (ClassNotFoundException e) {
                    return false;
                }
            }
        };

        this.typePatternClassFilter = ClassFilters.intersection(
                typePatternFilter, exclusion);
        this.defaultImplName = defaultImplName;
        this.introducedInterfaceName = interfaceTypeName;
        this.typePattern = typePattern;
        setType(ADVICE_TYPES.DECLARE_PARENTS);
    }

    public ClassFilter getTypeMatcher() {
        return this.typePatternClassFilter;
    }

    public String getTypePattern() {
        return this.typePattern;
    }

    public Method getAdviceMethod() throws ClassNotFoundException {
        throw new IllegalArgumentException();
    }

    public String getMethod() {
        throw new IllegalArgumentException();
    }

    public AspectJExpressionPointcut getPointcut()
            throws ClassNotFoundException {
        throw new IllegalArgumentException();
    }

    public String getReturning() {
        throw new IllegalArgumentException();
    }

    public String getThrowing() {
        throw new IllegalArgumentException();
    }

    public ADVICE_TYPES getType() {
        return ADVICE_TYPES.DECLARE_PARENTS;
    }

    public void setArgNames(String[] argNames) {
        throw new IllegalArgumentException();
    }

    public void setMethod(String method) {
        throw new IllegalArgumentException();
    }

    public void setPointcut(String pointcut) {
        throw new IllegalArgumentException();
    }

    public void setReturning(String returning) {
        throw new IllegalArgumentException();
    }

    public void setThrowing(String throwing) {
        throw new IllegalArgumentException();
    }

    public Class<?> getDefaultImplClass() throws ClassNotFoundException {
        if (this.defaultImpl == null) {
            this.defaultImpl = Thread.currentThread().getContextClassLoader()
                    .loadClass(this.defaultImplName);
        }
        return this.defaultImpl;
    }

    public String getDefaultImplName() {
        return this.defaultImplName;
    }

    public Class<?> getImplInterfaceClass() throws ClassNotFoundException {
        if (this.introducedInterface == null) {
            this.introducedInterface = Thread.currentThread()
                    .getContextClassLoader().loadClass(
                            this.introducedInterfaceName);

        }
        return this.introducedInterface;
    }

    public String getImplInterfaceName() {
        return this.introducedInterfaceName;
    }

}
