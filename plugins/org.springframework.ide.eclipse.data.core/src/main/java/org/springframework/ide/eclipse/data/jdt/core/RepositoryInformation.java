/*******************************************************************************
 * Copyright (c) 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.data.jdt.core;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.Repository;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.ModelUtils;
import org.springframework.ide.eclipse.data.SpringDataUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Value object to easily access information about a Spring Data repository.
 * 
 * @author Oliver Gierke
 * @author Tomasz Zarna
 */
public class RepositoryInformation {

	private static Set<String> METHOD_NAMES = new HashSet<String>();

	static {
		for (Method method : PagingAndSortingRepository.class.getMethods()) {
			METHOD_NAMES.add(method.getName());
		}
	}

	static enum Module {

		JPA("jpa"), MONGO("mongo"), GENERIC;

		public static final String BASE = "http://www.springframework.org/schema/data/";
		private String namespace;

		private Module(String namespace) {
			this.namespace = StringUtils.hasText(namespace) ? BASE + namespace : "";
		}

		private Module() {
			this(null);
		}

		public static Module getModuleOf(IBean bean) {

			for (Module candidate : values()) {
				String uri = ModelUtils.getNameSpaceURI(bean);
				if (uri != null && uri.equals(candidate.namespace)) {
					return candidate;
				}
			}

			return GENERIC;
		}
	}

	private final IType type;
	private final Class<?> repositoryInterface;
	private final Class<?> repositoryBaseInterface;

	public RepositoryInformation(IType type) {
		try {
			this.type = type;

			ClassLoader classLoader = JdtUtils.getClassLoader(type.getJavaProject().getProject(), null);
			this.repositoryInterface = classLoader.loadClass(type.getFullyQualifiedName());
			this.repositoryBaseInterface = classLoader.loadClass(Repository.class.getName());

		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Could not load class " + type.getFullyQualifiedName(), e);
		}
	}

	/**
	 * Returns a {@link RepositoryInformation} for the repository interface of the given {@link IMethod} if it is a Spring
	 * Data repository.
	 * 
	 * @param element must not be {@literal null}.
	 * @return
	 */
	public static RepositoryInformation create(IMethod element) {

		Assert.notNull(element);

		IType type = element.getDeclaringType();
		return create(type);
	}

	/**
	 * Returns a {@link RepositoryInformation} for the given {@link IType} if it is a Spring Data repository.
	 * 
	 * @param type must not be {@literal null}.
	 * @return
	 */
	public static RepositoryInformation create(IType type) {

		Assert.notNull(type);

		try {
			return type.isInterface()  && type.exists() ? new RepositoryInformation(type) : null;
		} catch (JavaModelException e) {
			return null;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public static boolean isSpringDataRepository(IType type) {
		try {
			String[] interfaces = type.getSuperInterfaceNames();
			for (String extendedInterface : interfaces) {
				if (extendedInterface.contains("Repository")) {
					String[][] resolvedInterfaceTypes = type.resolveType(extendedInterface);
					if (resolvedInterfaceTypes != null) {
						for (String[] match : resolvedInterfaceTypes) {
							if (match != null && match.length == 2 && match[0] != null && match[1] != null) {
								if ("org.springframework.data.repository".equals(match[0])
										|| "org.springframework.data.jpa.repository".equals(match[0])
										&&	match[1].contains("Repository")) {
									return true;
								}
							}
						}
					}
				}
			}

			IAnnotation[] annotations = type.getAnnotations();
			for (IAnnotation annotation : annotations) {
				if (annotation.getElementName().equals("org.springframework.data.repository.RepositoryDefinition") ||
						annotation.getElementName().equals("RepositoryDefinition")) {
					return true;
				}
			}
		} catch (JavaModelException e) {
		}
		
		return false;
	}

	/**
	 * Returns the {@link KeywordProvider} to be used for this repository.
	 * 
	 * @param project
	 * @return
	 */
	public KeywordProvider getKeywordProvider(IJavaProject project) {

		TypePredicates predicates = new DefaultTypePredicates(project);
		IBean repositoryBean = SpringDataUtils.getRepositoryBean(project.getProject(), repositoryInterface.getName());
		Module module = Module.getModuleOf(repositoryBean);

		switch (module) {
		case JPA:
			return new JpaKeywordProvider(predicates);
		case MONGO:
			return new MongoDbKeywordProvider(predicates);
		case GENERIC:
		default:
			return new KeywordProviderSupport(predicates);
		}
	}

	public Class<?> getManagedDomainClass() {
		try {
			Class<?>[] resolvedTypeArguments = GenericTypeResolver.resolveTypeArguments(this.repositoryInterface, this.repositoryBaseInterface);
			if (resolvedTypeArguments != null && resolvedTypeArguments.length > 0) {
				return resolvedTypeArguments[0];
			}
		} catch (TypeNotPresentException e) {
		}
		return null;
	}

	/**
	 * Returns all {@link IMethod}s that shall be considered query methods (which need to be validated).
	 * 
	 * @return
	 */
	public Iterable<IMethod> getMethodsToValidate() {

		Set<IMethod> result = new HashSet<IMethod>();

		try {
			for (IMethod method : type.getMethods()) {

				if (!isCrudMethod(method) && !hasQueryAnnotation(method)) {
					result.add(method);
				}
			}
		} catch (JavaModelException e) {
			SpringCore.log(e);
		}

		return result;
	}

	private boolean isCrudMethod(IMethod method) {
		return METHOD_NAMES.contains(method.getElementName());
	}

	private boolean hasQueryAnnotation(IMethod method) throws JavaModelException {

		for (IAnnotation annotation : method.getAnnotations()) {
			if (annotation.getElementName().equals("Query")) {
				return true;
			}
		}

		return false;
	}
}
