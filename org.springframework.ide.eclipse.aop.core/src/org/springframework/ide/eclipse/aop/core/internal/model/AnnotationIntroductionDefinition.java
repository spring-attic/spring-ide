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

import org.eclipse.ui.IMemento;
import org.springframework.ide.eclipse.aop.core.model.IAnnotationAopDefinition;
import org.springframework.ide.eclipse.aop.core.model.IIntroductionDefinition;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class AnnotationIntroductionDefinition extends
		BeanIntroductionDefinition implements IIntroductionDefinition,
		IAnnotationAopDefinition {

	private String definingField;

	public String getDefiningField() {
		return definingField;
	}

	public String getFactoryId() {
		return AnnotationIntroductionDefinitionElementFactory.FACTORY_ID;
	}

	public void saveState(IMemento memento) {
		super.saveState(memento);
		memento.putString(
			AnnotationIntroductionDefinitionElementFactory.DEFINING_FIELD_ATTRIBUTE,
			this.definingField);
	}

	public void setDefiningField(String definingField) {
		this.definingField = definingField;
	}
}
