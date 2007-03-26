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
package org.springframework.ide.eclipse.aop.core.model.internal;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.aspectj.TypePatternClassFilter;
import org.springframework.aop.support.ClassFilters;
import org.springframework.ide.eclipse.aop.core.model.IIntroductionDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;

public class BeanIntroductionDefinition extends BeanAspectDefinition implements
		IIntroductionDefinition {

	private String introducedInterfaceName;

	private ClassFilter typePatternClassFilter;

	private String defaultImplName;

	private String typePattern;

	public BeanIntroductionDefinition() {
		setType(ADVICE_TYPES.DECLARE_PARENTS);
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

	@Override
	public String getAdviceMethodName() {
		throw new IllegalArgumentException();
	}

	@Override
	public ADVICE_TYPES getType() {
		return ADVICE_TYPES.DECLARE_PARENTS;
	}

	public String getDefaultImplName() {
		return this.defaultImplName;
	}

	public String getImplInterfaceName() {
		return this.introducedInterfaceName;
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
