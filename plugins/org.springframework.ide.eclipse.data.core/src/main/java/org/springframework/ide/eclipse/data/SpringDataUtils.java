/*
 * Copyright 2012 the original author or authors.
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
package org.springframework.ide.eclipse.data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IType;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansTypedString;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Utility methods to work with Spring Data configuration.
 * 
 * @author Oliver Gierke
 */
public abstract class SpringDataUtils {

	public static final String NAMESPACE_URI_BASE = "http://www.springframework.org/schema/data";
	private static final Class<?> FACTORY_CLASS = RepositoryFactoryBeanSupport.class;

	/**
	 * Returns the name of the factory class.
	 * 
	 * @return
	 */
	public static String getFactoryName() {
		return FACTORY_CLASS.getName();
	}

	/**
	 * Returns the repository interface name for the given bean.
	 * 
	 * @param bean
	 * @return
	 */
	public static String getRepositoryInterfaceName(IBean bean) {

		IBeansTypedString property = (IBeansTypedString) bean.getProperty("repositoryInterface").getValue();
		return property.getString();
	}

	/**
	 * Returns all bean ids.
	 * 
	 * @return
	 */
	public static Set<String> getRepositoryBeanIds(IProject project) {

		Set<String> result = new HashSet<String>();

		for (IBean bean : getRepositoryBeansFor(project)) {
			result.add(bean.getElementName());
		}

		return result;
	}

	public static IBean getRepositoryBean(IProject project, String repositoryInterface) {

		for (IBean bean : getRepositoryBeansFor(project)) {
			if (repositoryInterface.equals(getRepositoryInterfaceName(bean))) {
				return bean;
			}
		}

		return null;
	}

	/**
	 * Returns whether the given {@link Node} is from Spring Data namespace.
	 * 
	 * @param node
	 * @return
	 */
	public static boolean isSpringDataElement(Node node) {

		if (Node.ELEMENT_NODE == node.getNodeType()) {
			String namespaceUri = NamespaceUtils.getNamespaceUri((Element) node);
			return namespaceUri.startsWith(NAMESPACE_URI_BASE);
		}

		return false;
	}

	/**
	 * Returns, whether the given bean is a Spring Data factory bean.
	 * 
	 * @param bean
	 * @return
	 */
	public static boolean isRepositoryBean(IBean bean) {
		IBeansProject project = BeansModelUtils.getProject(bean);
		return getRepositoryBeansFor(project.getProject()).contains(bean);
	}

	/**
	 * Returns all repository beans for the given {@link IProject project}.
	 * 
	 * @param project
	 * @return
	 */
	public static Set<IBean> getRepositoryBeansFor(IProject project) {

		IBeansProject beansProject = BeansCorePlugin.getModel().getProject(project);

		if (beansProject == null) {
			return Collections.emptySet();
		}

		Set<IBean> result = new HashSet<IBean>();

		for (IBean bean : BeansModelUtils.getBeans(beansProject)) {
			IType type = JdtUtils.getJavaType(project, bean.getClassName());
			if (JdtUtils.doesImplement(project, type, getFactoryName())) {
				result.add(bean);
			}
		}

		return result;
	}

	/**
	 * Returns all repository beans of the fiven {@link IProject project} of the given interface type.
	 * 
	 * @param project
	 * @param type
	 * @return
	 */
	public static Set<IBean> getRepositoryBeansFor(IProject project, IType type) {

		Set<IBean> result = new HashSet<IBean>();

		for (IBean bean : getRepositoryBeansFor(project)) {
			if (type.getFullyQualifiedName().equals(getRepositoryInterfaceName(bean))) {
				result.add(bean);
			}
		}

		return result;
	}

	/**
	 * Returns whether the given project contains a repository bean for the given interface type.
	 * 
	 * @param project
	 * @param type
	 * @return
	 */
	public static boolean hasRepositoryBeanFor(IProject project, IType type) {

		for (IBean bean : getRepositoryBeansFor(project)) {
			if (type.getFullyQualifiedName().equals(getRepositoryInterfaceName(bean))) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns a one line {@link String} of the given bean in the format of {@code $ beanId} [${repositoryInterface}]}.
	 * 
	 * @param bean
	 * @return
	 */
	public static String asText(IBean bean) {

		String typeName = isRepositoryBean(bean) ? getRepositoryInterfaceName(bean) : bean.getClassName();

		return String.format("%s [%s]", bean.getElementName(), typeName);
	}
}
