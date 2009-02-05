/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.springframework.beans.BeanMetadataAttribute;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;
import org.springframework.ide.eclipse.core.model.ModelUtils;

/**
 * {@link BeanNameGenerator} which creates a bean name which is unique within the beans core model.
 * This name consists of the bean class or parent name or object identity, the project name, the
 * config file name and the start line number delimited by '#'.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public class UniqueBeanNameGenerator implements BeanNameGenerator {

	public static final String GENERATED_BEAN_NAME_PROPERTY = BeansCorePlugin.PLUGIN_ID
			+ ".GENERATED_BEAN_NAME";

	private IBeansConfig config;

	public UniqueBeanNameGenerator(IBeansConfig config) {
		this.config = config;
	}

	public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
		String name = generateBeanName(definition, config);

		// Store a maker that the bean name was auto-generated
		if (definition instanceof AbstractBeanDefinition) {
			BeanMetadataAttribute attribute = new BeanMetadataAttribute(
					GENERATED_BEAN_NAME_PROPERTY, Boolean.TRUE);
			attribute.setSource(this);
			((AbstractBeanDefinition) definition).addMetadataAttribute(attribute);
		}

		return name;
	}

	public static String generateBeanName(BeanDefinition definition, IBeansConfig config) {
		StringBuilder name = new StringBuilder();
		if (definition.getBeanClassName() != null) {
			name.append(definition.getBeanClassName());
		}
		else {
			if (definition.getParentName() != null) {
				name.append(definition.getParentName());
				name.append("$child");
			}
			else if (definition.getFactoryBeanName() != null) {
				name.append(definition.getFactoryBeanName());
				name.append("$created");
			}
			else {
				name.append("!!!invalid name!!!");
			}
		}
		IModelSourceLocation location = ModelUtils.getSourceLocation(definition);
		if (location != null) {
			name.append(BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR);
			name.append(config.getElementParent().getElementName());
			name.append(BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR);

			// Make sure file name actually comes from the correct resource and always from the
			// passed config. Important in case of imported beans configs.
			name.append(generateFilename(config, location));

			name.append(BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR);
			name.append(location.getStartLine());
		}

		return name.toString();
	}

	/**
	 * Returns the name of the originating file that contains the bean definition.
	 * @since 2.0.5
	 */
	private static String generateFilename(IBeansConfig config, IModelSourceLocation location) {
		String fileName = config.getElementName();
		if (location.getResource() instanceof IAdaptable) {
			IResource resource = (IResource) ((IAdaptable) location.getResource())
					.getAdapter(IResource.class);
			if (resource != null) {
				fileName = resource.getProjectRelativePath().toString();
			}
		}
		return fileName;
	}

}
