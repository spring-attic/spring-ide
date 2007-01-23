package org.springframework.ide.eclipse.aop.core.model.internal;

import org.springframework.aop.aspectj.AspectJExpressionPointcut;

public class JavaAspectDefinition extends BeanAspectDefinition {
	
	private AspectJExpressionPointcut pointcut;

	public AspectJExpressionPointcut getPointcut() {
		return pointcut;
	}

	public void setPointcut(AspectJExpressionPointcut pointcut) {
		this.pointcut = pointcut;
	}
}
