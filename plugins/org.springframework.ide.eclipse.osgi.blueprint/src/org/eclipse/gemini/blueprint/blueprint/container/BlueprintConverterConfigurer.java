/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   VMware Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.blueprint.container;

import java.util.List;

import org.osgi.service.blueprint.container.Converter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.core.convert.ConversionService;

/**
 * Dedicated class for registering (in a declarative way) the adapter between Blueprint and Spring 3.0 converters.
 * 
 * @author Costin Leau
 */
public class BlueprintConverterConfigurer implements BeanFactoryAware {

	private final List<Converter> converters;

	public BlueprintConverterConfigurer(List<Converter> converters) {
		this.converters = converters;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if (beanFactory instanceof AbstractBeanFactory) {
			AbstractBeanFactory bf = ((AbstractBeanFactory) beanFactory);
			ConversionService cs = bf.getConversionService();
			if (cs instanceof SpringBlueprintConverterService) {
				cs = null;
			}
			SpringBlueprintConverterService sbc = new SpringBlueprintConverterService(cs, bf);
			sbc.add(converters);
			bf.setConversionService(sbc);
		}
	}
}