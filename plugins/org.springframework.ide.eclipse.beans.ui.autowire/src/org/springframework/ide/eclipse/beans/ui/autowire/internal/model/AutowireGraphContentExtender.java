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
package org.springframework.ide.eclipse.beans.ui.autowire.internal.model;

import java.beans.Introspector;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.ide.eclipse.beans.core.autowire.AutowireBeanReference;
import org.springframework.ide.eclipse.beans.core.autowire.internal.provider.AutowireDependencyProvider;
import org.springframework.ide.eclipse.beans.core.internal.model.BeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.internal.model.BeanProperty;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConnection.BeanType;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeanReference;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.ui.graph.model.Bean;
import org.springframework.ide.eclipse.beans.ui.graph.model.IGraphContentExtender;
import org.springframework.ide.eclipse.beans.ui.graph.model.Reference;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;

/**
 * {@link IGraphContentExtender} that adds autowired dependencies by calling into the autowire support.
 * @author Christian Dupuis
 * @since 2.2.7
 */
public class AutowireGraphContentExtender implements IGraphContentExtender {

	/**
	 * {@inheritDoc}
	 */
	public void addAdditionalBeans(Map<String, Bean> beans, List<Reference> beansReferences, IBeansModelElement root,
			IBeansModelElement context) {
		AutowireDependencyProvider provider = new AutowireDependencyProvider(root, context);
		Map<IBean, Set<IBeanReference>> autowiredReferences = provider.resolveAutowiredDependencies();

		for (Map.Entry<IBean, Set<IBeanReference>> entry : autowiredReferences.entrySet()) {
			Bean bean = beans.get(entry.getKey().getElementName());
			if (bean != null) {
				Set<String> autowiredProperties = new HashSet<String>();
				Set<IBeanReference> refs = entry.getValue();
				for (IBeanReference ref : refs) {
					Bean targetBean = beans.get(ref.getBeanName());
					if (targetBean != null) {
						beansReferences.add(new Reference(BeanType.STANDARD, bean, targetBean, null,
								!bean.isRootBean(), (IResourceModelElement) ref));

						try {
							IJavaElement source = ((AutowireBeanReference) ref).getSource();
							if (source instanceof IField) {
								String propertyName = source.getElementName();
								if (!autowiredProperties.contains(propertyName)) {
									IBeanProperty newProperty = new BeanProperty(entry.getKey(), new PropertyValue(
											propertyName, new RuntimeBeanReference(ref.getBeanName())));
									bean.addBeanProperty(newProperty);
									autowiredProperties.add(propertyName);
								}

							}
							else if (source instanceof IMethod && ((IMethod) source).isConstructor()) {
								IBeanConstructorArgument newConstructorArg = new BeanConstructorArgument(
										entry.getKey(), ((AutowireBeanReference) ref).getParameterIndex(),
										new ValueHolder(new RuntimeBeanReference(ref.getBeanName())));
								bean.addBeanConstructorArgument(newConstructorArg);
							}
							else if (source instanceof IMethod && !((IMethod) source).isConstructor()) {
								String propertyName = source.getElementName();
								if (propertyName.startsWith("set")) {
									propertyName = Introspector.decapitalize(propertyName.substring(3));
								}
								if (!autowiredProperties.contains(propertyName)) {
									IBeanProperty newProperty = new BeanProperty(entry.getKey(), new PropertyValue(
											propertyName, new RuntimeBeanReference(ref.getBeanName())));
									bean.addBeanProperty(newProperty);
									autowiredProperties.add(propertyName);
								}
							}
						}
						catch (JavaModelException e) {
							// Just ignore this
						}
					}
				}
			}
		}
	}
}
