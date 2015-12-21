/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.core.internal.model;

import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.springframework.ide.eclipse.aop.core.Activator;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeanIntroductionDefinitionElementFactory extends
		BeanAspectDefinitionElementFactory implements IElementFactory {

	protected static final String TYPE_PATTERN_ATTRIBUTE = "type-pattern";

	protected static final String DEFAULT_IMPL_NAME_ATTRIBUTE = "default-impl-name";

	protected static final String INTRODUCED_INTERFACE_NAME_ATTRIBUTE = "introduced-interface-name";

	public static String FACTORY_ID = Activator.PLUGIN_ID
			+ ".beanIntroductionDefinitionElementFactory";

	protected BeanAspectDefinition createAspectDefinition() {
		return new BeanIntroductionDefinition();
	}

	protected void postPopulateAspectDefinition(
			BeanAspectDefinition definition, IMemento memento) {
		String introducedInterfaceName = memento
				.getString(INTRODUCED_INTERFACE_NAME_ATTRIBUTE);
		String defaultImplName = memento.getString(DEFAULT_IMPL_NAME_ATTRIBUTE);
		String typePattern = memento.getString(TYPE_PATTERN_ATTRIBUTE);

		BeanIntroductionDefinition def = (BeanIntroductionDefinition) definition;
		def.setIntroducedInterfaceName(introducedInterfaceName);
		def.setDefaultImplName(defaultImplName);
		def.setTypePattern(typePattern);
	}
}
