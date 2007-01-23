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
package org.springframework.ide.eclipse.aop.core.parser;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.aopalliance.intercept.MethodInterceptor;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.DeclareParents;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.AjTypeSystem;
import org.aspectj.lang.reflect.PerClauseKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.ThrowsAdvice;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;
import org.springframework.ide.eclipse.aop.core.model.internal.AnnotationAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.internal.AnnotationIntroductionDefinition;
import org.springframework.ide.eclipse.aop.core.model.internal.BeanAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.internal.BeanIntroductionDefinition;
import org.springframework.ide.eclipse.aop.core.model.internal.JavaAspectDefinition;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.ui.editor.BeansEditorUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@SuppressWarnings("restriction")
public class BeanAspectDefinitionParser {

	/**
	 * Class modelling an AspectJ annotation, exposing its type enumeration and pointcut String.
	 */
	protected static class AspectJAnnotation<A extends Annotation> {

		private static Map<Class<?>, AspectJAnnotationType> annotationTypes = null;

		static {
			annotationTypes = new HashMap<Class<?>, AspectJAnnotationType>();
			annotationTypes.put(Pointcut.class, AspectJAnnotationType.AtPointcut);
			annotationTypes.put(After.class, AspectJAnnotationType.AtAfter);
			annotationTypes.put(AfterReturning.class, AspectJAnnotationType.AtAfterReturning);
			annotationTypes.put(AfterThrowing.class, AspectJAnnotationType.AtAfterThrowing);
			annotationTypes.put(Around.class, AspectJAnnotationType.AtAround);
			annotationTypes.put(Before.class, AspectJAnnotationType.AtBefore);
		}

		private static final String[] EXPRESSION_PROPERTIES = new String[] { "value", "pointcut" };

		private final A annotation;

		private AspectJAnnotationType annotationType;

		private final String argNames;

		private final String expression;

		public AspectJAnnotation(A aspectjAnnotation) {
			this.annotation = aspectjAnnotation;
			for (Class<?> c : annotationTypes.keySet()) {
				if (c.isInstance(this.annotation)) {
					this.annotationType = annotationTypes.get(c);
					break;
				}
			}
			if (this.annotationType == null) {
				throw new IllegalStateException("unknown annotation type: "
						+ this.annotation.toString());
			}

			// We know these methods exist with the same name on each object,
			// but need to invoke them reflectively as there isn't a common
			// interfaces
			try {
				this.expression = resolveExpression();
				this.argNames = (String) annotation.getClass()
						.getMethod("argNames", (Class[]) null).invoke(this.annotation);
			} catch (Exception ex) {
				throw new IllegalArgumentException(aspectjAnnotation
						+ " cannot be an AspectJ annotation", ex);
			}
		}

		public A getAnnotation() {
			return this.annotation;
		}

		public AspectJAnnotationType getAnnotationType() {
			return this.annotationType;
		}

		public String getArgNames() {
			return this.argNames;
		}

		public String getPointcutExpression() {
			return this.expression;
		}

		private String resolveExpression() throws IllegalAccessException,
				InvocationTargetException, NoSuchMethodException {
			String expression = null;
			for (int i = 0; i < EXPRESSION_PROPERTIES.length; i++) {
				String methodName = EXPRESSION_PROPERTIES[i];
				Method method;
				try {
					method = annotation.getClass().getDeclaredMethod(methodName);
				} catch (NoSuchMethodException ex) {
					method = null;
				}

				if (method != null) {
					String candidate = (String) method.invoke(this.annotation);

					if (StringUtils.hasText(candidate)) {
						expression = candidate;
					}
				}
			}
			return expression;
		}

		public String toString() {
			return this.annotation.toString();
		}
	}

	protected enum AspectJAnnotationType {
		AtAfter, AtAfterReturning, AtAfterThrowing, AtAround, AtBefore, AtPointcut
	}

	private static final String AJC_MAGIC = "ajc$";

	private static void addDocument(IDOMDocument document, List<IAspectDefinition> aspectInfos,
			IResource file) {
		for (IAspectDefinition info : aspectInfos) {
			if (info instanceof BeanAspectDefinition) {
				((BeanAspectDefinition) info).setDocument(document);
			}
			info.setResource(file);
		}
	}

	/**
	 * Find and return the first AspectJ annotation on the given method (there <i>should</i> only
	 * be one anyway...)
	 */
	@SuppressWarnings("unchecked")
	protected static AspectJAnnotation<?> findAspectJAnnotationOnMethod(Method aMethod) {
		Class<? extends Annotation>[] classesToLookFor = (Class<? extends Annotation>[]) new Class[] {
				Before.class, Around.class, After.class, AfterReturning.class, AfterThrowing.class,
				Pointcut.class };
		for (Class<? extends Annotation> c : classesToLookFor) {
			AspectJAnnotation foundAnnotation = findAnnotation(aMethod, c);
			if (foundAnnotation != null) {
				return foundAnnotation;
			}
		}
		return null;
	}

	private static <A extends Annotation> AspectJAnnotation<A> findAnnotation(Method method,
			Class<A> toLookFor) {
		A result = AnnotationUtils.findAnnotation(method, toLookFor);
		if (result != null) {
			return new AspectJAnnotation<A>(result);
		} else {
			return null;
		}
	}

	private static AspectJExpressionPointcut getPointcut(Class<?> candidateAspectClass,
			String pointcut) {
		AspectJExpressionPointcut ajexp = new AspectJExpressionPointcut(candidateAspectClass,
				new String[0], new Class[0]);
		ajexp.setExpression(pointcut);
		return ajexp;
	};

	private static AspectJExpressionPointcut getPointcut(Method candidateAspectJAdviceMethod,
			Class<?> candidateAspectClass) {
		AspectJAnnotation<?> aspectJAnnotation = findAspectJAnnotationOnMethod(candidateAspectJAdviceMethod);
		if (aspectJAnnotation == null) {
			return null;
		}
		AspectJExpressionPointcut ajexp = new AspectJExpressionPointcut(candidateAspectClass,
				new String[0], new Class[0]);
		ajexp.setExpression(aspectJAnnotation.getPointcutExpression());
		return ajexp;
	};

	private static boolean isAspect(Class<?> clazz) {
		boolean couldBeAtAspectJAspect = AjTypeSystem.getAjType(clazz).isAspect();
		if (!couldBeAtAspectJAspect) {
			return false;
		} else {
			// we know it's an aspect, but we don't know whether it is an
			// @AspectJ aspect or a code style aspect.
			// This is an *unclean* test whilst waiting for AspectJ to provide
			// us with something better
			Method[] methods = clazz.getDeclaredMethods();
			for (Method m : methods) {
				if (m.getName().startsWith(AJC_MAGIC)) {
					// must be a code style aspect
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * If no &lt;aop:include&gt; elements were used then includePatterns will be null and all beans
	 * are included. If includePatterns is non-null, then one of the patterns must match.
	 */
	private static boolean isIncluded(List<Pattern> includePatterns, String beanName) {
		if (includePatterns == null || includePatterns.size() == 0) {
			return true;
		} else {
			for (Pattern pattern : includePatterns) {
				if (pattern.matcher(beanName).matches()) {
					return true;
				}
			}
			return false;
		}
	}

	public static List<IAspectDefinition> buildAspectDefinitions(final IDOMDocument document,
			IFile file) {
		final List<IAspectDefinition> aspectInfos = new ArrayList<IAspectDefinition>();

		parseXmlAspects(document, file, aspectInfos);
		parseAnnotationAspects(document, file, aspectInfos);

		addDocument(document, aspectInfos, file);
		return aspectInfos;
	}

	private static void parseAnnotationAspects(final IDOMDocument document, IFile file,
			final List<IAspectDefinition> aspectInfos) {
		NodeList list;
		list = document.getDocumentElement().getElementsByTagNameNS(
				"http://www.springframework.org/schema/aop", "aspectj-autoproxy");
		if (list.getLength() > 0) {
			Node item = list.item(0);
			List<Pattern> patternList = new ArrayList<Pattern>();
			NodeList include = item.getChildNodes();
			for (int j = 0; j < include.getLength(); j++) {
				if ("include".equals(include.item(j).getLocalName())) {
					String pattern = BeansEditorUtils.getAttribute(include.item(j), "name");
					if (StringUtils.hasText(pattern)) {
						patternList.add(Pattern.compile(pattern));
					}
				}
			}

			list = document.getDocumentElement().getElementsByTagNameNS(
					"http://www.springframework.org/schema/beans", "bean");

			for (int j = 0; j < list.getLength(); j++) {
				final Node bean = list.item(j);
				final String id = BeansEditorUtils.getAttribute(bean, "id");
				final String className = BeansEditorUtils.getClassNameForBean(bean);
				if (className != null && isIncluded(patternList, id)) {
					try {
						final Class<?> aspectClass = Thread.currentThread().getContextClassLoader()
								.loadClass(className);
						if (isAspect(aspectClass) && validate(aspectClass)) {
							createAnnotationAspectDefinition(document, bean, id, className,
									aspectClass, aspectInfos);
						}
					} catch (Throwable e) {
						BeansCorePlugin.log(e);
					}
				}
			}
		}
	}

	private static void parseXmlAspects(final IDOMDocument document, IFile file,
			final List<IAspectDefinition> aspectInfos) {
		NodeList list = document.getDocumentElement().getElementsByTagNameNS(
				"http://www.springframework.org/schema/aop", "config");

		for (int i = 0; i < list.getLength(); i++) {
			Map<String, String> rootPointcuts = new HashMap<String, String>();
			Node node = list.item(i);
			NodeList children = node.getChildNodes();

			parsePointcuts(rootPointcuts, children);

			for (int j = 0; j < children.getLength(); j++) {
				Node child = children.item(j);
				if ("aspect".equals(child.getLocalName())) {
					parseAspects(file, child, rootPointcuts, aspectInfos);
				} else if ("advisor".equals(child.getLocalName())) {
					parseAdvisors(file, child, rootPointcuts, aspectInfos);
				}
			}
		}
	}

	private static void createAnnotationAspectDefinition(final IDOMDocument document,
			final Node bean, final String id, final String className, final Class<?> aspectClass,
			final List<IAspectDefinition> aspectInfos) {
		ReflectionUtils.doWithMethods(aspectClass, new ReflectionUtils.MethodCallback() {
			public void doWith(Method method) throws IllegalArgumentException {
				// Exclude pointcuts
				if (AnnotationUtils.getAnnotation(method, Pointcut.class) == null) {
					AspectJExpressionPointcut ajexp = getPointcut(method, aspectClass);
					if (ajexp == null) {
						return;
					}

					AnnotationAspectDefinition def = new AnnotationAspectDefinition();
					def.setPointcut(ajexp);
					def.setAspectName(id);
					def.setClassName(className);
					def.setNode((IDOMNode) bean);
					def.setMethod(method.getName());

					AspectJAnnotation<?> aspectJAnnotation = findAspectJAnnotationOnMethod(method);
					if (aspectJAnnotation.getArgNames() != null) {
						def.setArgNames(StringUtils
								.commaDelimitedListToStringArray(aspectJAnnotation.getArgNames()));
					}
					switch (aspectJAnnotation.getAnnotationType()) {
					case AtBefore:
						def.setType(ADVICE_TYPES.BEFORE);
						break;
					case AtAfter:
						def.setType(ADVICE_TYPES.AFTER);
						break;
					case AtAfterReturning:
						def.setType(ADVICE_TYPES.AFTER_RETURNING);
						AfterReturning afterReturningAnnotation = (AfterReturning) aspectJAnnotation
								.getAnnotation();
						if (StringUtils.hasText(afterReturningAnnotation.returning())) {
							def.setReturning(afterReturningAnnotation.returning());
						}
						break;
					case AtAfterThrowing:
						def.setType(ADVICE_TYPES.AFTER_THROWING);
						AfterThrowing afterThrowingAnnotation = (AfterThrowing) aspectJAnnotation
								.getAnnotation();
						if (StringUtils.hasText(afterThrowingAnnotation.throwing())) {
							def.setThrowing(afterThrowingAnnotation.throwing());
						}
						break;
					case AtAround:
						def.setType(ADVICE_TYPES.AROUND);
						break;
					default:
					}
					aspectInfos.add(def);
				}
			}
		});

		for (Field field : aspectClass.getDeclaredFields()) {
			DeclareParents declareParents = (DeclareParents) field
					.getAnnotation(DeclareParents.class);
			if (declareParents != null) {
				if (declareParents.defaultImpl() != DeclareParents.class) {
					AnnotationIntroductionDefinition def = new AnnotationIntroductionDefinition(
							field.getType(), declareParents.value(), declareParents.defaultImpl(),
							field);
					def.setAspectName(id);
					def.setClassName(className);
					def.setNode((IDOMNode) bean);
					aspectInfos.add(def);
				}
			}
		}
	}

	private static BeanAspectDefinition parseAspect(Map<String, String> pointcuts,
			Map<String, String> rootPointcuts, Node aspectNode, IAopReference.ADVICE_TYPES type) {
		BeanAspectDefinition info = new BeanAspectDefinition();
		String pointcut = BeansEditorUtils.getAttribute(aspectNode, "pointcut");
		String pointcutRef = BeansEditorUtils.getAttribute(aspectNode, "pointcut-ref");
		if (!StringUtils.hasText(pointcut)) {
			pointcut = pointcuts.get(pointcutRef);
			if (!StringUtils.hasText(pointcut)) {
				pointcut = rootPointcuts.get(pointcutRef);
			}
		}
		String argNames = BeansEditorUtils.getAttribute(aspectNode, "arg-names");
		String method = BeansEditorUtils.getAttribute(aspectNode, "method");
		String[] argNamesArray = null;
		if (argNames != null) {
			argNamesArray = StringUtils.commaDelimitedListToStringArray(argNames);
		}
		info.setArgNames(argNamesArray);
		info.setNode((IDOMNode) aspectNode);
		info.setPointcut(pointcut);
		info.setType(type);
		info.setMethod(method);
		return info;
	}

	private static void parseAspects(IFile file, Node child, Map<String, String> rootPointcuts,
			List<IAspectDefinition> aspectInfos) {
		String beanRef = BeansEditorUtils.getAttribute(child, "ref");
		String className = BeansEditorUtils.getClassNameForBean(file, child.getOwnerDocument(),
				beanRef);
		if (StringUtils.hasText(className)) {
			NodeList aspectChildren = child.getChildNodes();
			Map<String, String> pointcuts = new HashMap<String, String>();
			parsePointcuts(pointcuts, aspectChildren);

			for (int g = 0; g < aspectChildren.getLength(); g++) {
				Node aspectNode = aspectChildren.item(g);
				if ("declare-parents".equals(aspectNode.getLocalName())) {
					String typesMatching = BeansEditorUtils.getAttribute(aspectNode,
							"types-matching");
					String defaultImpl = BeansEditorUtils.getAttribute(aspectNode, "default-impl");
					String implementInterface = BeansEditorUtils.getAttribute(aspectNode,
							"implement-interface");
					if (StringUtils.hasText(typesMatching) && StringUtils.hasText(defaultImpl)
							&& StringUtils.hasText(implementInterface)) {
						BeanIntroductionDefinition info = new BeanIntroductionDefinition(
								implementInterface, typesMatching, defaultImpl);
						// info.setClassName(className);
						info.setAspectName(beanRef);
						info.setNode((IDOMNode) aspectNode);
						aspectInfos.add(info);
					}
				} else if ("before".equals(aspectNode.getLocalName())) {
					BeanAspectDefinition info = parseAspect(pointcuts, rootPointcuts, aspectNode,
							IAopReference.ADVICE_TYPES.BEFORE);
					info.setClassName(className);
					info.setAspectName(beanRef);
					aspectInfos.add(info);
				} else if ("around".equals(aspectNode.getLocalName())) {
					BeanAspectDefinition info = parseAspect(pointcuts, rootPointcuts, aspectNode,
							IAopReference.ADVICE_TYPES.AROUND);
					info.setClassName(className);
					info.setAspectName(beanRef);
					aspectInfos.add(info);
				} else if ("after".equals(aspectNode.getLocalName())) {
					BeanAspectDefinition info = parseAspect(pointcuts, rootPointcuts, aspectNode,
							IAopReference.ADVICE_TYPES.AFTER);
					info.setClassName(className);
					info.setAspectName(beanRef);
					aspectInfos.add(info);
				} else if ("after-returning".equals(aspectNode.getLocalName())) {
					BeanAspectDefinition info = parseAspect(pointcuts, rootPointcuts, aspectNode,
							IAopReference.ADVICE_TYPES.AFTER_RETURNING);
					String returning = BeansEditorUtils.getAttribute(aspectNode, "returning");
					info.setReturning(returning);
					info.setClassName(className);
					info.setAspectName(beanRef);
					aspectInfos.add(info);
				} else if ("after-throwing".equals(aspectNode.getLocalName())) {
					BeanAspectDefinition info = parseAspect(pointcuts, rootPointcuts, aspectNode,
							IAopReference.ADVICE_TYPES.AFTER_THROWING);
					String throwing = BeansEditorUtils.getAttribute(aspectNode, "throwing");
					info.setThrowing(throwing);
					info.setClassName(className);
					info.setAspectName(beanRef);
					aspectInfos.add(info);
				} else if ("around".equals(aspectNode.getLocalName())) {
					BeanAspectDefinition info = parseAspect(pointcuts, rootPointcuts, aspectNode,
							IAopReference.ADVICE_TYPES.AROUND);
					info.setClassName(className);
					info.setAspectName(beanRef);
					aspectInfos.add(info);
				}
			}
		}
	}

	private static void parseAdvisors(IFile file, Node aspectNode,
			Map<String, String> rootPointcuts, List<IAspectDefinition> aspectInfos) {
		String beanRef = BeansEditorUtils.getAttribute(aspectNode, "advice-ref");
		String className = BeansEditorUtils.getClassNameForBean(file,
				aspectNode.getOwnerDocument(), beanRef);
		if (StringUtils.hasText(className)) {
			NodeList aspectChildren = aspectNode.getParentNode().getChildNodes();
			Map<String, String> pointcuts = new HashMap<String, String>();
			parsePointcuts(pointcuts, aspectChildren);

			String pointcut = BeansEditorUtils.getAttribute(aspectNode, "pointcut");
			String pointcutRef = BeansEditorUtils.getAttribute(aspectNode, "pointcut-ref");
			if (!StringUtils.hasText(pointcut)) {
				pointcut = pointcuts.get(pointcutRef);
				if (!StringUtils.hasText(pointcut)) {
					pointcut = rootPointcuts.get(pointcutRef);
				}
			}

			try {
				Class<?> advisorClass = Thread.currentThread().getContextClassLoader().loadClass(
						className);
				
				if (MethodInterceptor.class.isAssignableFrom(advisorClass)) {
					JavaAspectDefinition info = new JavaAspectDefinition();
					info.setNode((IDOMNode) aspectNode);
					info.setPointcut(pointcut);
					info.setClassName(className);
					info.setAspectName(beanRef);
					info.setType(ADVICE_TYPES.AROUND);
					aspectInfos.add(info);
					info.setPointcut(getPointcut(advisorClass, pointcut));
					info.setMethod("invoke");
				}
				if (MethodBeforeAdvice.class.isAssignableFrom(advisorClass)) {
					JavaAspectDefinition info = new JavaAspectDefinition();
					info.setNode((IDOMNode) aspectNode);
					info.setPointcut(pointcut);
					info.setClassName(className);
					info.setAspectName(beanRef);
					info.setType(ADVICE_TYPES.BEFORE);
					info.setMethod("before");
					info.setPointcut(getPointcut(advisorClass, pointcut));
					aspectInfos.add(info);
				}
				if (ThrowsAdvice.class.isAssignableFrom(advisorClass)) {
					JavaAspectDefinition info = new JavaAspectDefinition();
					info.setNode((IDOMNode) aspectNode);
					info.setPointcut(pointcut);
					info.setClassName(className);
					info.setAspectName(beanRef);
					info.setType(ADVICE_TYPES.AFTER_THROWING);
					info.setMethod("afterThrowing");
					info.setPointcut(getPointcut(advisorClass, pointcut));
					aspectInfos.add(info);
				}
				if (AfterReturningAdvice.class.isAssignableFrom(advisorClass)) {
					JavaAspectDefinition info = new JavaAspectDefinition();
					info.setNode((IDOMNode) aspectNode);
					info.setPointcut(pointcut);
					info.setClassName(className);
					info.setAspectName(beanRef);
					info.setType(ADVICE_TYPES.AFTER_RETURNING);
					info.setMethod("afterReturning");
					info.setPointcut(getPointcut(advisorClass, pointcut));
					aspectInfos.add(info);
				}
			} catch (Throwable e) {
				BeansCorePlugin.log(e);
			}
		}
	}

	private static void parsePointcuts(Map<String, String> rootPointcuts, NodeList children) {
		for (int j = 0; j < children.getLength(); j++) {
			Node child = children.item(j);
			if ("pointcut".equals(child.getLocalName())) {
				String id = BeansEditorUtils.getAttribute(child, "id");
				String expression = BeansEditorUtils.getAttribute(child, "expression");
				if (StringUtils.hasText(id) && StringUtils.hasText(expression)) {
					rootPointcuts.put(id, expression);
				}
			}
		}
	}

	private static boolean validate(Class<?> aspectClass) throws AopConfigException {
		// If the parent has the annotation and isn't abstract it's an error
		if (aspectClass.getSuperclass().getAnnotation(Aspect.class) != null
				&& !Modifier.isAbstract(aspectClass.getSuperclass().getModifiers())) {
			return false;
			// throw new AopConfigException(aspectClass.getName() + " cannot
			// extend concrete aspect " +
			// aspectClass.getSuperclass().getName());
		}

		AjType<?> ajType = AjTypeSystem.getAjType(aspectClass);
		if (!ajType.isAspect()) {
			return false;
			// throw new NotAnAtAspectException(aspectClass);
		}
		if (ajType.getPerClause().getKind() == PerClauseKind.PERCFLOW) {
			return false;
			// throw new AopConfigException(aspectClass.getName() + " uses
			// percflow instantiation model: " +
			// "This is not supported in Spring AOP");
		}
		if (ajType.getPerClause().getKind() == PerClauseKind.PERCFLOWBELOW) {
			return false;
			// throw new AopConfigException(aspectClass.getName() + " uses
			// percflowbelow instantiation model: " +
			// "This is not supported in Spring AOP");
		}
		return true;
	}

}
