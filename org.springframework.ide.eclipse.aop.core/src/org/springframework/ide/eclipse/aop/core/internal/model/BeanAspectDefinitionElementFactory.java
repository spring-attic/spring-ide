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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;
import org.springframework.util.StringUtils;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeanAspectDefinitionElementFactory implements IElementFactory {

	public static String FACTORY_ID = Activator.PLUGIN_ID
			+ ".beanAspectDefinitionElementFactory";

	protected BeanAspectDefinition createAspectDefinition() {
		return new BeanAspectDefinition();
	}

	public final IAdaptable createElement(IMemento memento) {
		BeanAspectDefinition definition = createAspectDefinition();
		populateAspectDefinition(definition, memento);
		return definition;
	}

	protected final void populateAspectDefinition(
			BeanAspectDefinition definition, IMemento memento) {
		String adivceMethodName = memento.getString("advice-method-name");
		String aspectClassName = memento.getString("advice-class-name");
		String adivceMethodParameterTypesString = memento
				.getString("adivce-method-parameter-types");
		String[] adivceMethodParameterTypes = null;
		if (adivceMethodParameterTypesString != null) {
			adivceMethodParameterTypes = StringUtils
					.delimitedListToStringArray(
							adivceMethodParameterTypesString, ",");
		}
		String aspectName = memento.getString("aspect-name");
		String pointcutExpressionString = memento
				.getString("pointcut-expression");
		String returning = memento.getString("returning");
		String throwing = memento.getString("throwing");
		String argNamesString = memento.getString("arg-names");
		String[] argNames = null;
		if (argNamesString != null) {
			argNames = StringUtils.delimitedListToStringArray(argNamesString,
					",");
		}
		int aspectLineNumber = memento.getInteger("aspect-line-number");
		String fileName = memento.getString("file");
		boolean proxyTargetClass = Boolean.valueOf(memento
				.getString("proxy-target-class"));
		IAopReference.ADVICE_TYPES type = ADVICE_TYPES.valueOf(memento
				.getString("advice-type"));

		definition.setAdviceMethodName(adivceMethodName);
		definition.setAspectClassName(aspectClassName);
		definition.setAdviceMethodParameterTypes(adivceMethodParameterTypes);
		definition.setAspectName(aspectName);
		definition.setPointcutExpression(pointcutExpressionString);
		definition.setReturning(returning);
		definition.setThrowing(throwing);
		definition.setArgNames(argNames);
		definition.setAspectLineNumber(aspectLineNumber);
		definition.setProxyTargetClass(proxyTargetClass);
		definition.setType(type);

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource member = root.findMember(fileName);
		if (member != null) {
			definition.setResource(member);
		}

		postPopulateAspectDefinition(definition, memento);
	}

	protected void postPopulateAspectDefinition(
			BeanAspectDefinition definition, IMemento memento) {

	}
}
