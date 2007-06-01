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

import org.eclipse.ui.IMemento;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.aspectj.TypePatternClassFilter;
import org.springframework.aop.support.ClassFilters;
import org.springframework.ide.eclipse.aop.core.model.IIntroductionDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeanIntroductionDefinition extends BeanAspectDefinition implements
		IIntroductionDefinition {

	private String defaultImplName;

	private String introducedInterfaceName;

	private String typePattern;

	private ClassFilter typePatternClassFilter;

	public BeanIntroductionDefinition() {
		setType(ADVICE_TYPES.DECLARE_PARENTS);
	}

	@Override
	public String getAdviceMethodName() {
		throw new IllegalArgumentException();
	}

	public String getDefaultImplName() {
		return this.defaultImplName;
	}

	public String getFactoryId() {
		return BeanIntroductionDefinitionElementFactory.FACTORY_ID;
	}

	public String getImplInterfaceName() {
		return this.introducedInterfaceName;
	}

	@Override
	public ADVICE_TYPES getType() {
		return ADVICE_TYPES.DECLARE_PARENTS;
	}

	public ClassFilter getTypeMatcher() {
		if (this.typePatternClassFilter == null) {
			ClassFilter typePatternFilter = new TypePatternClassFilter(
					typePattern);

			// Excludes methods implemented.
			ClassFilter exclusion = new ClassFilter() {
				@SuppressWarnings("unchecked")
				public boolean matches(Class clazz) {
					try {
						Class<?> implInterfaceClass = Thread.currentThread()
								.getContextClassLoader().loadClass(
										introducedInterfaceName);
						return !(implInterfaceClass.isAssignableFrom(clazz));
					}
					catch (ClassNotFoundException e) {
						return false;
					}
				}
			};
			this.typePatternClassFilter = ClassFilters.intersection(
					typePatternFilter, exclusion);
		}
		return this.typePatternClassFilter;
	}

	public String getTypePattern() {
		return this.typePattern;
	}

	public void saveState(IMemento memento) {
		super.saveState(memento);
		memento.putString("introduced-interface-name", this.introducedInterfaceName);
		memento.putString("default-impl-name", this.defaultImplName);
		memento.putString("type-pattern", this.typePattern);
	}

	public void setDefaultImplName(String defaultImplName) {
		this.defaultImplName = defaultImplName;
	}
	
	public void setIntroducedInterfaceName(String introducedInterfaceName) {
		this.introducedInterfaceName = introducedInterfaceName;
	}

	public void setTypePattern(String typePattern) {
		this.typePattern = typePattern;
	}
}
