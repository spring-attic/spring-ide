/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
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

	public static String FACTORY_ID = Activator.PLUGIN_ID
			+ ".beanIntroductionDefinitionElementFactory";

	protected BeanAspectDefinition createAspectDefinition() {
		return new BeanIntroductionDefinition();
	}

	protected void postPopulateAspectDefinition(
			BeanAspectDefinition definition, IMemento memento) {
		String introducedInterfaceName = memento
				.getString("introduced-interface-name");
		String defaultImplName = memento.getString("default-impl-name");
		String typePattern = memento.getString("type-pattern");
		
		BeanIntroductionDefinition def = (BeanIntroductionDefinition) definition;
		def.setIntroducedInterfaceName(introducedInterfaceName);
		def.setDefaultImplName(defaultImplName);
		def.setTypePattern(typePattern);
	}
}
