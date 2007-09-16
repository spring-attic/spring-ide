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
package org.springframework.ide.eclipse.beans.core.model;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementTypes;

/**
 * Constants for {@link IModelElement} types defined by the beans model.
 * 
 * @author Torsten Juergeleit
 */
public interface IBeansModelElementTypes extends IModelElementTypes {

	/**
	 * Constant representing a Spring project.
	 * A model element with this type can be safely cast to
	 * {@link IBeansProject}.
	 */
	int PROJECT_TYPE = 2; // starts with 2 because 1 is reserved for the model

	/**
	 * Constant representing a Spring beans config.
	 * A model element with this type can be safely cast to
	 * {@link IBeansConfig}.
	 */
	int CONFIG_TYPE = 3;

	/**
	 * Constant representing a Spring beans config set.
	 * A model element with this type can be safely cast to
	 * {@link IBeansConfigSet}.
	 */
	int CONFIG_SET_TYPE = 4;

	/**
	 * Constant representing a Spring beans import.
	 * A model element with this type can be safely cast to
	 * {@link IBeansImport}.
	 */
	int IMPORT_TYPE = 5;

	/**
	 * Constant representing a Spring bean alias.
	 * A model element with this type can be safely cast to
	 * {@link IBeanReference}.
	 */
	int ALIAS_TYPE = 6;

	/**
	 * Constant representing a Spring beans component.
	 * A model element with this type can be safely cast to
	 * {@link IBeansComponent}.
	 */
	int COMPONENT_TYPE = 7;

	/**
	 * Constant representing a Spring bean.
	 * A model element with this type can be safely cast to
	 * {@link IBean}.
	 */
	int BEAN_TYPE = 8;

	/**
	 * Constant representing a Spring project bean's property.
	 * A model element with this type can be safely cast to
	 * {@link IBeanProperty}.
	 */
	int PROPERTY_TYPE = 9;

	/**
	 * Constant representing a Spring project bean's constructor argument.
	 * A model element with this type can be safely cast to
	 * {@link IBeanConstructorArgument}.
	 */
	int CONSTRUCTOR_ARGUMENT_TYPE = 10;

	/**
	 * Constant representing a Spring bean reference.
	 * A model element with this type can be safely cast to
	 * {@link IBeanReference}.
	 */
	int BEAN_REFERENCE_TYPE = 11;

	/**
	 * Constant representing a managed {@link List}.
	 * A model element with this type can be safely cast to
	 * {@link IBeansList}.
	 */
	int LIST_TYPE = 12;

	/**
	 * Constant representing a managed {@link Set}.
	 * A model element with this type can be safely cast to
	 * {@link IBeansSet}.
	 */
	int SET_TYPE = 13;

	/**
	 * Constant representing a managed {@link Map}.
	 * A model element with this type can be safely cast to
	 * {@link IBeansMap}.
	 */
	int MAP_TYPE = 14;

	/**
	 * Constant representing the content of a {@link Map.Entry}.
	 * A model element with this type can be safely cast to
	 * {@link IBeansMapEntry}.
	 */
	int MAP_ENTRY_TYPE = 15;

	/**
	 * Constant representing a managed {@link Properties}.
	 * A model element with this type can be safely cast to
	 * {@link IBeansProperties}.
	 */
	int PROPERTIES_TYPE = 16;

	/**
	 * Constant representing a {@link TypedStringValue}.
	 * A model element with this type can be safely cast to
	 * {@link IBeansTypedString}.
	 */
	int TYPED_STRING_TYPE = 17;

	/**
	 * Constant representing a {@link MethodOverride}.
	 * A model element with this type can be safely cast to
	 * {@link IBeanMethodOverride}.
	 */
	int METHOD_OVERRIDE_TYPE = 18;
}
