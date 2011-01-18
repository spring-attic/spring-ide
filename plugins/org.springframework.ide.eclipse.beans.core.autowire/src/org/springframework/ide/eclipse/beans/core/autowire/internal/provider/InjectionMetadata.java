/*******************************************************************************
 * Copyright (c) 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.autowire.internal.provider;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.PropertyValues;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.ide.eclipse.beans.core.autowire.AutowireBeanReference;
import org.springframework.ide.eclipse.beans.core.autowire.IAutowireDependencyResolver;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanReference;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;

/**
 * Internal class for managing injection meta data.
 * @author Christian Dupuis
 * @since 2.2.7
 */
public class InjectionMetadata {

	private final Set<InjectedElement> injectedFields = new LinkedHashSet<InjectedElement>();

	private final Set<InjectedElement> injectedMethods = new LinkedHashSet<InjectedElement>();

	private final Set<InjectedElement> injectedConstructors = new LinkedHashSet<InjectedElement>();

	public void addInjectedField(InjectedElement element) {
		this.injectedFields.add(element);
	}

	public void addInjectedMethod(InjectedElement element) {
		this.injectedMethods.add(element);
	}

	public void addInjectedConstructor(InjectedElement element) {
		this.injectedConstructors.add(element);
	}

	public Set<InjectedElement> getInjectedFields() {
		return injectedFields;
	}

	public Set<InjectedElement> getInjectedMethods() {
		return injectedMethods;
	}

	public Set<InjectedElement> getInjectedConstructors() {
		return injectedConstructors;
	}

	public static abstract class InjectedElement {

		protected final Member member;

		protected final boolean isField;

		protected final PropertyDescriptor pd;

		protected volatile Boolean skip;

		protected InjectedElement(Member member, PropertyDescriptor pd) {
			this.member = member;
			this.isField = (member instanceof Field);
			this.pd = pd;
		}

		public final Member getMember() {
			return this.member;
		}

		protected final Class<?> getResourceType() {
			if (this.isField) {
				return ((Field) this.member).getType();
			}
			else if (this.pd != null) {
				return this.pd.getPropertyType();
			}
			else {
				return ((Method) this.member).getParameterTypes()[0];
			}
		}

		protected final void checkResourceType(Class<?> resourceType) {
			if (this.isField) {
				Class<?> fieldType = ((Field) this.member).getType();
				if (!(resourceType.isAssignableFrom(fieldType) || fieldType.isAssignableFrom(resourceType))) {
					throw new IllegalStateException("Specified field type [" + fieldType
							+ "] is incompatible with resource type [" + resourceType.getName() + "]");
				}
			}
			else {
				Class<?> paramType = (this.pd != null ? this.pd.getPropertyType() : ((Method) this.member)
						.getParameterTypes()[0]);
				if (!(resourceType.isAssignableFrom(paramType) || paramType.isAssignableFrom(resourceType))) {
					throw new IllegalStateException("Specified parameter type [" + paramType
							+ "] is incompatible with resource type [" + resourceType.getName() + "]");
				}
			}
		}

		protected abstract DependencyDescriptor[] getDependencyDescriptor(IAutowireDependencyResolver resolver);

		public Set<IBeanReference> getBeanReferences(IBean bean, IBeansModelElement context,
				IAutowireDependencyResolver resolver) {
			Set<IBeanReference> autowiredReferences = new HashSet<IBeanReference>();

			BeanDefinition db = BeansModelUtils.getMergedBeanDefinition(bean, context);

			if (!shouldSkip(db)) {

				for (DependencyDescriptor dependencyDescriptor : getDependencyDescriptor(resolver)) {
					Set<String> autowiredBeanNames = new HashSet<String>();
					resolver.resolveDependency(dependencyDescriptor, dependencyDescriptor.getDependencyType(), bean
							.getElementName(), autowiredBeanNames, new SimpleTypeConverter());

					for (String autowiredBeanName : autowiredBeanNames) {
						AutowireBeanReference ref = new AutowireBeanReference(bean, new RuntimeBeanReference(
								autowiredBeanName));
						if (dependencyDescriptor.getField() != null) {
							ref.setSource(dependencyDescriptor.getField());
						}
						else if (dependencyDescriptor.getMethodParameter() != null
								&& dependencyDescriptor.getMethodParameter().getMethod() != null) {
							ref.setSource(dependencyDescriptor.getMethodParameter().getMethod(), dependencyDescriptor
									.getMethodParameter().getParameterIndex());
						}
						else if (dependencyDescriptor.getMethodParameter() != null
								&& dependencyDescriptor.getMethodParameter().getConstructor() != null) {
							ref.setSource(dependencyDescriptor.getMethodParameter().getConstructor(),
									dependencyDescriptor.getMethodParameter().getParameterIndex());
						}
						autowiredReferences.add(ref);
					}
				}
			}
			return autowiredReferences;
		}

		public boolean shouldSkip(BeanDefinition bd) {
			return false;
		}

		/**
		 * Checks whether this injector's property needs to be skipped due to an explicit property value having been
		 * specified. Also marks the affected property as processed for other processors to ignore it.
		 */
		protected boolean checkPropertySkipping(PropertyValues pvs) {
			if (this.pd != null && pvs != null) {
				if (pvs.contains(this.pd.getName())) {
					// Explicit value provided as part of the bean definition.
					return true;
				}
				// else if (pvs instanceof MutablePropertyValues) {
				// ((MutablePropertyValues) pvs).registerProcessedProperty(this.pd.getName());
				// }
			}
			return false;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof InjectedElement)) {
				return false;
			}
			InjectedElement otherElement = (InjectedElement) other;
			return this.member.equals(otherElement.member);
		}

		@Override
		public int hashCode() {
			return this.member.getClass().hashCode() * 29 + this.member.getName().hashCode();
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + " for " + this.member;
		}
	}

}
