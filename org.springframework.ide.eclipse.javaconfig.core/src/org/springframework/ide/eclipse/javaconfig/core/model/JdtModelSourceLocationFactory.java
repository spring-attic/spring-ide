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
package org.springframework.ide.eclipse.javaconfig.core.model;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.ide.eclipse.aop.core.util.AopReferenceModelUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.io.FileResource;
import org.springframework.ide.eclipse.core.model.DefaultModelSourceLocation;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;

/**
 * Factory implementation that creates {@link IModelSourceLocation} instances
 * based on {@link IBean} and JDT core java model elements.
 * @author Christian Dupuis
 * @since 2.0
 */
public class JdtModelSourceLocationFactory {

	/**
	 * Create a {@link IModelSourceLocation} based on the given
	 * <code>bean</bean> and <code>beanCreationMethod</code>.
	 * @param bean the bean that is the source
	 * @param beanCreationMethod the method to should be used to extract the 
	 * source location from.
	 * @return a new {@link IModelSourceLocation}
	 */
	public static IModelSourceLocation getModelSourceLocation(IBean bean,
			BeanCreationMethod beanCreationMethod) {

		String className = beanCreationMethod.getOwningClassName();
		String methodName = beanCreationMethod.getName();
		List<String> parameterTypeNames = beanCreationMethod
				.getParameterTypes();
		IType type = BeansModelUtils.getJavaType(bean.getElementResource()
				.getProject(), className);

		if (type != null) {
			IFile file = null;
			try {
				file = (IFile) type.getUnderlyingResource();
			}
			catch (JavaModelException e) {
				// don't care about that here
			}
			// TODO refactor AopReferenceModelUtils.getMethod to eclipse.core
			IMethod method = AopReferenceModelUtils.getMethod(type, methodName,
					parameterTypeNames.toArray(new String[parameterTypeNames
							.size()]));
			if (method != null) {
				int l = AopReferenceModelUtils.getLineNumber(method);
				return new DefaultModelSourceLocation(l, l, new FileResource(
						file));
			}
		}
		return null;
	}
}
