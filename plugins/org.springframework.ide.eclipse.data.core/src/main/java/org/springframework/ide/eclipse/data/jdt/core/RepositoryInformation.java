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

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.repository.Repository;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.ModelUtils;
import org.springframework.ide.eclipse.data.SpringDataUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 
 * @author Oliver Gierke
 */
public class RepositoryInformation {

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

	private static final String REPOSITORY_INTERFACE_NAME = "org.springframework.data.repository.Repository";

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
			return type.isInterface() ? new RepositoryInformation(type) : null;
		} catch (JavaModelException e) {
			return null;
		}
	}

	public boolean isSpringDataRepository() {

		try {
			List<String> interfaces = Arrays.asList(type.getSuperInterfaceNames());
			if (interfaces.contains(REPOSITORY_INTERFACE_NAME)) {
				return true;
			}
		} catch (JavaModelException e) {
			return false;
		}

		IAnnotation repoDefinitionAnnotation = type.getAnnotation("RepositoryDefinition");
		return repoDefinitionAnnotation != null;
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
		return GenericTypeResolver.resolveTypeArguments(this.repositoryInterface, this.repositoryBaseInterface)[0];
	}
}
