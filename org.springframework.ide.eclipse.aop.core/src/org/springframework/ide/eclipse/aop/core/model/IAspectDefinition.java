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
package org.springframework.ide.eclipse.aop.core.model;

import java.lang.reflect.Method;

import org.eclipse.core.resources.IResource;
import org.springframework.ide.eclipse.aop.core.internal.model.AopReferenceModel;

/**
 * Central interface of the {@link AopReferenceModel}.
 * <p>
 * A implementation of {@link IAspectDefinition} contains all meta data required
 * identify an aspect definition
 * @author Christian Dupuis
 * @since 2.0
 */
public interface IAspectDefinition {

	Method getAdviceMethod();

	String getAdviceMethodName();

	String[] getAdviceMethodParameterTypes();

	String[] getArgNames();

	String getAspectClassName();

	int getAspectEndLineNumber();
	
	String getAspectName();

	int getAspectStartLineNumber();

	String getPointcutExpression();

	IResource getResource();

	String getReturning();

	String getThrowing();
	
	IAopReference.ADVICE_TYPE getType();

	boolean isProxyTargetClass();
	
	// TODO CD move into other interface
	void setResource(IResource file);
}
