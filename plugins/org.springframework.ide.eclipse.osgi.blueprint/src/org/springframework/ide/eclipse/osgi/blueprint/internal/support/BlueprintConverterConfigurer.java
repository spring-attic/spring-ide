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
 *   Spring IDE Developers 
 *****************************************************************************/

package org.springframework.ide.eclipse.osgi.blueprint.internal.support;

import java.util.List;

import org.osgi.service.blueprint.container.Converter;

/**
 * 
 * @author Costin Leau
 * @author Arnaud Mergey
 * 
 * @since 3.7.2
 */
public class BlueprintConverterConfigurer {

	private List<Converter> converters;

	public List<Converter> getConverters() {
		return converters;
	}

	public void setConverters(List<Converter> converters) {
		this.converters = converters;
	}
}