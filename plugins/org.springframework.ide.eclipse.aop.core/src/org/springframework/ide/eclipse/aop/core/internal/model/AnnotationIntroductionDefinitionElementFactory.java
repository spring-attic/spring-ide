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
public class AnnotationIntroductionDefinitionElementFactory extends
		BeanAspectDefinitionElementFactory implements IElementFactory {

	protected static final String DEFINING_FIELD_ATTRIBUTE = "defining-field";
	
	public static String FACTORY_ID = Activator.PLUGIN_ID
			+ ".annotationIntroductionDefinitionElementFactory";

	protected BeanAspectDefinition createAspectDefinition() {
		return new AnnotationIntroductionDefinition();
	}

	protected void postPopulateAspectDefinition(
			BeanAspectDefinition definition, IMemento memento) {
		String definingField = memento.getString(DEFINING_FIELD_ATTRIBUTE);

		AnnotationIntroductionDefinition def = (AnnotationIntroductionDefinition) definition;
		def.setDefiningField(definingField);
	}
}
