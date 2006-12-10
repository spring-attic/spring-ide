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

package org.springframework.ide.eclipse.aop.ui.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.aopalliance.aop.Advice;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.weaver.tools.JoinPointMatch;
import org.aspectj.weaver.tools.PointcutParameter;
import org.springframework.aop.aspectj.AspectInstanceFactory;
import org.springframework.aop.aspectj.AspectJAdviceParameterNameDiscoverer;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.aspectj.AspectJPrecedenceInformation;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.PrioritizedParameterNameDiscoverer;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * Superclass for Spring Advices wrapping an AspectJ aspect
 * or annotated advice method.
 *
 * @author Rod Johnson
 * @author Adrian Colyer
 * @author Juergen Hoeller
 * @since 2.0
 */
public abstract class AbstractAspectJAdvice implements Advice, AspectJPrecedenceInformation, InitializingBean {
	
	/**
	 * Key used in ReflectiveMethodInvocation userAtributes map for the current joinpoint.
	 */
	protected final static String JOIN_POINT_KEY = JoinPoint.class.getName();


	/**
	 * Lazily instantiate joinpoint for the current invocation.
	 * Requires MethodInvocation to be bound with ExposeInvocationInterceptor.
	 * <p>Do not use if access is available to the current ReflectiveMethodInvocation
	 * (in an around advice).
	 * @return current AspectJ joinpoint, or through an exception if we're not in a
	 * Spring AOP invocation.
	 */
	public static JoinPoint currentJoinPoint() {
		ReflectiveMethodInvocation rmi = (ReflectiveMethodInvocation) ExposeInvocationInterceptor.currentInvocation();
		JoinPoint jp = (JoinPoint) rmi.getUserAttribute(JOIN_POINT_KEY);
		if (jp == null) {
			jp = new MethodInvocationProceedingJoinPoint(rmi);
			rmi.setUserAttribute(JOIN_POINT_KEY, jp);
		}
		return jp;
	}


	protected final Method aspectJAdviceMethod;
	
	private final AspectJExpressionPointcut pointcutExpression;
	
	private final AspectInstanceFactory aspectInstanceFactory;

	/**
	 * The name of the aspect (ref bean) in which this advice was defined (used
	 * when determining advice precedence so that we can determine
	 * whether two pieces of advice come from the same aspect).
	 */
	private String aspectName;
	
	/**
	 * The order of declaration of this advice within the aspect.
	 */
	private int declarationOrder;
	
	/**
	 * This will be non-null if the creator of this advice object knows the argument names
	 * and sets them explicitly
	 */
	private String[] argumentNames = null;
	
	/** non-null if after throwing advice binds the thrown value */
	private String throwingName = null;
	
	/** non-null if after returning advice binds the return value */
	private String returningName = null;
	
	private Class discoveredReturningType = Object.class;

	private Class discoveredThrowingType = Object.class;
	
	/** 
	 * the total number of arguments we have to populate on
	 * advice dispatch
	 */
	private final int numAdviceInvocationArguments;
	
	/**
	 * index for thisJoinPoint argument (currently only
	 * supported at index 0 if present at all)
	 */
	private int joinPointArgumentIndex = -1;

	/**
	 * index for thisJoinPointStaticPart argument (currently only
	 * supported at index 0 if present at all)
	 */
	private int joinPointStaticPartArgumentIndex = -1;
	
	private final Map argumentBindings = new HashMap();


	public AbstractAspectJAdvice(
			Method aspectJAdviceMethod, AspectJExpressionPointcut pointcutExpression, AspectInstanceFactory aif) {

		this.aspectJAdviceMethod = aspectJAdviceMethod;
		if (!aspectJAdviceMethod.isAccessible()) {
			aspectJAdviceMethod.setAccessible(true);
		}
		this.numAdviceInvocationArguments = this.aspectJAdviceMethod.getParameterTypes().length;
		this.pointcutExpression = pointcutExpression;
		this.aspectInstanceFactory = aif;
	}


	public final Method getAspectJAdviceMethod() {
		return this.aspectJAdviceMethod;
	}

	public final AspectJExpressionPointcut getPointcut() {
		return this.pointcutExpression;
	}

	public final AspectInstanceFactory getAspectInstanceFactory() {
		return this.aspectInstanceFactory;
	}

	public int getOrder() {
		return this.aspectInstanceFactory.getOrder();
	}


	public void setAspectName(String name) {
		this.aspectName = name;
	}
	
	public String getAspectName() {
		return this.aspectName;
	}

	/**
	 * Sets the <b>declaration order</b> of this advice within the aspect
	 */
	public void setDeclarationOrder(int order) {
		this.declarationOrder = order;
	}

	public int getDeclarationOrder() {
		return this.declarationOrder;
	}

	/**
	 * Set by creator of this advice object if the argument names are known.
	 * This could be for example because they have been explicitly specified in XML,
	 * or in an advice annotation.
	 * @param argNames comma delimited list of arg names
	 */
	public void setArgumentNames(String argNames) {
		String[] tokens = StringUtils.commaDelimitedListToStringArray(argNames);
		setArgumentNamesFromStringArray(tokens);
	}

	public void setArgumentNamesFromStringArray(String[] args) {
		this.argumentNames = new String[args.length];
		for (int i = 0; i < args.length; i++) {
			this.argumentNames[i] = StringUtils.trimWhitespace(args[i]);
			if (!isVariableName(this.argumentNames[i])) {
				throw new IllegalArgumentException(
						"argumentNames property of AbstractAspectJAdvice " + 
						"contains an argument name '" +
						this.argumentNames[i] + "' that is not a valid Java identifier");
			}
		}		
	}

	public void setReturningName(String name) {
		throw new UnsupportedOperationException("Only afterReturning advice can be used to bind a return value");
	}

	/** 
	 * We need to hold the returning name at this level for argument binding calculations,
	 * this method allows the afterReturning advice subclass to set the name.
	 */
	protected void setReturningNameNoCheck(String name) {
		// name could be a variable or a type...
		if (isVariableName(name)) {
			this.returningName = name;
		}
		else {
			// assume a type
			try {
				this.discoveredReturningType = ClassUtils.forName(name);
			}
			catch (Throwable ex) {
				throw new IllegalArgumentException("Returning name '" + name  +
						"' is neither a valid argument name nor the fully-qualified name of a Java type on the classpath. " +
						"Root cause: " + ex);
			}
		}
	}
	
	protected Class getDiscoveredReturningType() {
		return this.discoveredReturningType;
	}
	
	public void setThrowingName(String name) {
		throw new UnsupportedOperationException("Only afterThrowing advice can be used to bind a thrown exception");
	}
	
	/** 
	 * We need to hold the throwing name at this level for argument binding calculations,
	 * this method allows the afterThrowing advice subclass to set the name.
	 */
	protected void setThrowingNameNoCheck(String name) {
		// name could be a variable or a type...
		if (isVariableName(name)) {
			this.throwingName = name;
		}
		else {
			// assume a type
			try {
				this.discoveredThrowingType = ClassUtils.forName(name);
			}
			catch (Throwable ex) {
				throw new IllegalArgumentException("Throwing name '" + name  +
						"' is neither a valid argument name nor the fully-qualified name of a Java type on the classpath. " +
						"Root cause: " + ex);
			}
		}
	}

	protected Class getDiscoveredThrowingType() {
		return this.discoveredThrowingType;
	}
	
	private boolean isVariableName(String name) {
		char[] chars = name.toCharArray();
		if (!Character.isJavaIdentifierStart(chars[0])) {
			return false;
		}
		for (int i = 1; i < chars.length; i++) {
			if (!Character.isJavaIdentifierPart(chars[i])) {
				return false;
			}
		}
		return true;
	}


	/**
	 * Argument names have to be discovered and set on the associated pointcut expression,
	 * and we also calculate argument bindings for advice invocation so that actual dispatch
	 * can be as fast as possible.
	 */
	public void afterPropertiesSet() throws Exception {
		calculateArgumentBindings();
	}	
	
	/**
	 * Do as much work as we can as part of the set-up so that argument binding
	 * on subsequent advice invocations can be as fast as possible.
	 * <p>If the first argument is of type JoinPoint or ProceedingJoinPoint then we
	 * pass a JoinPoint in that position (ProceedingJoinPoint for around advice).
	 * <p>If the first argument is of type <code>JoinPoint.StaticPart</code>
	 * then we pass a <code>JoinPoint.StaticPart</code> in that position.
	 * <p>Remaining arguments have to be bound by pointcut evaluation at
	 * a given join point. We will get back a map from argument name to
	 * value. We need to calculate which advice parameter needs to be bound
	 * to which argument name. There are multiple strategies for determining
	 * this binding, which are arranged in a ChainOfResponsibility.
	 */
	private void calculateArgumentBindings() {
		// The simple case... nothing to bind.
		if (this.numAdviceInvocationArguments == 0) {
			return;
		}
		
		int numUnboundArgs = this.numAdviceInvocationArguments;
		
		Class[] parameterTypes = this.aspectJAdviceMethod.getParameterTypes();
		if (maybeBindJoinPoint(parameterTypes[0])) {
			numUnboundArgs--;
		} 
		else if (maybeBindJoinPointStaticPart(parameterTypes[0])) {
			numUnboundArgs--;
		}
			
		if (numUnboundArgs > 0) {
			// need to bind arguments by name as returned from the pointcut match
			bindArgumentsByName(numUnboundArgs);
		}
	}
	
	private boolean maybeBindJoinPoint(Class candidateParameterType) {
		if ((candidateParameterType.equals(JoinPoint.class)) ||
			(candidateParameterType.equals(ProceedingJoinPoint.class))) {
			this.joinPointArgumentIndex = 0;
			return true;
		}
		else {
			return false;
		}
	}
	
	private boolean maybeBindJoinPointStaticPart(Class candidateParameterType) {
		if (candidateParameterType.equals(JoinPoint.StaticPart.class)) {
			this.joinPointStaticPartArgumentIndex = 0;
			return true;
		} 
		else {
			return false;
		}
	}
	
	private void bindArgumentsByName(int numArgumentsExpectingToBind) {
		if (this.argumentNames == null) {
			// We need to discover them, or if that fails, guess,
			// and if we can't guess with 100% accuracy, fail.
			PrioritizedParameterNameDiscoverer discoverer = new PrioritizedParameterNameDiscoverer();
			discoverer.addDiscoverer(createParameterNameDiscoverer());
			AspectJAdviceParameterNameDiscoverer adviceParameterNameDiscoverer = 
				new AspectJAdviceParameterNameDiscoverer(this.pointcutExpression.getExpression());
			adviceParameterNameDiscoverer.setReturningName(this.returningName);
			adviceParameterNameDiscoverer.setThrowingName(this.throwingName);
			// last in chain, so if we're called and we fail, that's bad...
			adviceParameterNameDiscoverer.setRaiseExceptions(true);
			discoverer.addDiscoverer(adviceParameterNameDiscoverer);
			
			this.argumentNames = discoverer.getParameterNames(this.aspectJAdviceMethod);
		}
		
		if (this.argumentNames != null) {
			// We have been able to determine the arg names.
			bindExplicitArguments(numArgumentsExpectingToBind);
		} 
		else {
			throw new IllegalStateException("Advice method [" + this.aspectJAdviceMethod.getName() + "] " +
					"requires " + numArgumentsExpectingToBind + " arguments to be bound by name, but " +
					"the argument names were not specified and could not be discovered.");
		}
	}


	protected ParameterNameDiscoverer createParameterNameDiscoverer() {
		return new LocalVariableTableParameterNameDiscoverer();
	}

	private void bindExplicitArguments(int numArgumentsLeftToBind) {
		int numExpectedArgumentNames = this.aspectJAdviceMethod.getParameterTypes().length;
		if (this.argumentNames.length != numExpectedArgumentNames) {
			throw new IllegalStateException("Expecting to find " + numExpectedArgumentNames
					+ " arguments to bind by name in advice, but actually found " +
					this.argumentNames.length + " arguments.");
		}
		
		// so we match in number...
		int argumentIndexOffset = this.numAdviceInvocationArguments - numArgumentsLeftToBind;
		for (int i = argumentIndexOffset; i < this.argumentNames.length; i++) {
			this.argumentBindings.put(this.argumentNames[i],new Integer(i));
		}
		
		// check that returning and throwing were in the argument names list if
		// specified, and find the discovered argument types
		if (this.returningName != null) {
			if (!this.argumentBindings.containsKey(this.returningName)) {
				throw new IllegalStateException("Returning argument name '" 
						+ this.returningName + "' was not bound in advice arguments");
			} 
			else {
				Integer index = (Integer) this.argumentBindings.get(this.returningName);
				this.discoveredReturningType = this.aspectJAdviceMethod.getParameterTypes()[index.intValue()];
			}
		}
		if (this.throwingName != null) {
			if (!this.argumentBindings.containsKey(this.throwingName)) {
				throw new IllegalStateException("Throwing argument name '" 
						+ this.throwingName + "' was not bound in advice arguments");
			} 
			else {
				Integer index = (Integer) this.argumentBindings.get(this.throwingName);
				this.discoveredThrowingType = this.aspectJAdviceMethod.getParameterTypes()[index.intValue()];				
			}
		}
		
		// configure the pointcut expression accordingly.
		configurePointcutParameters(argumentIndexOffset);
	}

	/**
	 * All parameters from argumentIndexOffset onwards are candidates for
	 * pointcut parameters - but returning and throwing vars are handled differently
	 * and must be removed from the list if present.
	 * @param argumentIndexOffset
	 */
	private void configurePointcutParameters(int argumentIndexOffset) {
		int numParametersToRemove = argumentIndexOffset;
		if (returningName != null) {
			numParametersToRemove++;
		}
		if (throwingName != null) {
			numParametersToRemove++;
		}
		String[] pointcutParameterNames = new String[this.argumentNames.length - numParametersToRemove];
		Class[] pointcutParameterTypes = new Class[pointcutParameterNames.length];
		Class[] methodParameterTypes = this.aspectJAdviceMethod.getParameterTypes();

		int index = 0;
		for (int i = 0; i < this.argumentNames.length; i++) {
			if (i < argumentIndexOffset) {
				continue;
			}
			if (this.argumentNames[i].equals(this.returningName) ||
				this.argumentNames[i].equals(this.throwingName)) {
				continue;
			}
			pointcutParameterNames[index] = this.argumentNames[i];
			pointcutParameterTypes[index] = methodParameterTypes[i];
			index++;
		}
		
		this.pointcutExpression.setParameterNames(pointcutParameterNames);
		this.pointcutExpression.setParameterTypes(pointcutParameterTypes);
	}
	
	/**
	 * Take the arguments at the method execution join point and output a set of arguments
	 * to the advice method
	 * @param jpMatch the join point match that matched this execution join point
	 * @param returnValue the return value from the method execution (may be null)
	 * @param t the exception thrown by the method execution (may be null)
	 * @return the empty array if there are no arguments
	 */
	protected Object[] argBinding(JoinPoint jp, JoinPointMatch jpMatch, Object returnValue, Throwable t) {
		// AMC start
		Object[] adviceInvocationArgs = new Object[this.numAdviceInvocationArguments];
		int numBound = 0;
		
		if (this.joinPointArgumentIndex != -1) {
			adviceInvocationArgs[this.joinPointArgumentIndex] = jp;
			numBound++;
		} 
		else if (this.joinPointStaticPartArgumentIndex != -1) {
			adviceInvocationArgs[this.joinPointStaticPartArgumentIndex] = jp.getStaticPart();
			numBound++;
		}
		
		if (!this.argumentBindings.isEmpty()) {
			// binding from pointcut match
			if (jpMatch != null) {
				PointcutParameter[] parameterBindings = jpMatch.getParameterBindings();
				for (int i = 0; i < parameterBindings.length; i++) {
					PointcutParameter parameter = parameterBindings[i];
					String name = parameter.getName();
					Integer index = (Integer) this.argumentBindings.get(name);
					adviceInvocationArgs[index.intValue()] = parameter.getBinding();
					numBound++;
				}
			}
			// binding from returning clause
			if (this.returningName != null) {
				Integer index = (Integer) this.argumentBindings.get(this.returningName);
				adviceInvocationArgs[index.intValue()] = returnValue;
				numBound++;
			}
			// binding from thrown exception
			if (this.throwingName != null) {
				Integer index = (Integer) this.argumentBindings.get(this.throwingName);
				adviceInvocationArgs[index.intValue()] = t;
				numBound++;
			}
		}

		if (numBound != this.numAdviceInvocationArguments) {
			throw new IllegalStateException("Required to bind " + this.numAdviceInvocationArguments
					+ " arguments, but only bound " + numBound + " (JoinPointMatch " + 
					(jpMatch == null ? "was NOT" : "WAS") + 
					" bound in invocation)");
		}
		
		return adviceInvocationArgs;
	}


	// Overridden in around advice to return proceeding join point.
	protected JoinPoint getJoinPoint() {
		return currentJoinPoint();
	}

	// Get the current join point match at the join point we are being dispatched on.
	protected JoinPointMatch getJoinPointMatch() {
		ReflectiveMethodInvocation rmi = (ReflectiveMethodInvocation) ExposeInvocationInterceptor.currentInvocation();
		return getJoinPointMatch(rmi);
	}

	// Note - can't use JoinPointMatch.getClass().getName() as the key, since
	// Spring AOP does all the matching at a join point, and then all the invocations.
	// Under this scenario, if we just use JoinPointMatch as the key, then
	// 'last man wins' which is not what we want at all.
	// Using the expression is guaranteed to be safe, since 2 identical expressions
	// are guaranteed to bind in exactly the same way.
	protected JoinPointMatch getJoinPointMatch(ReflectiveMethodInvocation rmi) {
		JoinPointMatch jpm = (JoinPointMatch) rmi.getUserAttribute(this.pointcutExpression.getExpression());
		return jpm;		
	}

	/**
	 * Invoke the advice method.
	 * @param jpMatch the JoinPointMatch that matched this execution join point
	 * @param returnValue the return value from the method execution (may be null)
	 * @param t the exception thrown by the method execution (may be null)
	 */
	protected Object invokeAdviceMethod(JoinPointMatch jpMatch, Object returnValue, Throwable t) throws Throwable {
		return invokeAdviceMethodWithGivenArgs(argBinding(getJoinPoint(), jpMatch, returnValue, t));
	}

	// As above, but in this case we are given the join point.
	protected Object invokeAdviceMethod(JoinPoint jp, JoinPointMatch jpMatch, Object returnValue, Throwable t)
			throws Throwable {

		return invokeAdviceMethodWithGivenArgs(argBinding(jp, jpMatch, returnValue, t));
	}

	protected Object invokeAdviceMethodWithGivenArgs(Object[] args) throws Throwable {
		Object[] actualArgs = args;
		if (this.aspectJAdviceMethod.getParameterTypes().length == 0) {
			actualArgs = null;
		}
		
		try {
			// TODO AopUtils.invokeJoinpointUsingReflection
			return this.aspectJAdviceMethod.invoke(this.aspectInstanceFactory.getAspectInstance(), actualArgs);
		}
		catch (IllegalArgumentException ex) {
			throw new AopConfigException("Mismatch on arguments to advice method [" + this.aspectJAdviceMethod + "]; " +
					"pointcut expression = [" + this.pointcutExpression.getPointcutExpression() + "]", ex);
		}
		catch (InvocationTargetException ex) {
			throw ex.getTargetException();
		}
	}


	public String toString() {
		return getClass().getName() + ": adviceMethod=" + this.aspectJAdviceMethod + "; " +
				"aspectName='" + this.aspectName + "'";
	}

}
