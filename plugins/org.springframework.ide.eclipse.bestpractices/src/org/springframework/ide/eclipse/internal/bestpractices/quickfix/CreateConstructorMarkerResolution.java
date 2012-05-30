/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.internal.bestpractices.quickfix;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.w3c.dom.Element;

/**
 * Resolution for missing init method markers. Creates a corresponding method in
 * the class referenced by the bean definition.
 * @author Wesley Coelho
 * @author Leo Dos Santos
 * @author Terry Denney
 * @author Christian Dupuis
 */
public class CreateConstructorMarkerResolution extends AbstractCreateMethodMarkerResolution {

	private String targetClass = "";

	private int numConstructorArgs = 0;

	public CreateConstructorMarkerResolution(IMarker marker) throws CoreException {
		super(marker);
		targetClass = extractQuotedString("class '", getMarkerMessage());
		numConstructorArgs = getNumConstructorArgsForMarkedBean(marker);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected List<Expression> getArguments(Expression invocationExpression) {
		ClassInstanceCreation methodInvocation = (ClassInstanceCreation) invocationExpression;
		List<Expression> arguments = new ArrayList<Expression>();
		for (Iterator<Expression> argumentIter = methodInvocation.arguments().iterator(); argumentIter.hasNext();) {
			Expression argumentExpression = argumentIter.next();
			arguments.add(argumentExpression);
		}
		return arguments;
	}

	@Override
	public String getDescription() {
		return "Create matching constructor in class '" + targetClass + "'";
	}

	@Override
	public String getLabel() {
		return "Create matching constructor in class '" + targetClass + "'";
	}

	@Override
	protected String getNewMethodName() {
		return "new " + targetClass;
	}

	@Override
	protected String getNewMethodParameters() {
		String params = "";
		String paramName = "object";
		for (int i = 0; i < numConstructorArgs; i++) {
			if (i == 0) {
				params += paramName;
			}
			else {
				params += ", " + paramName;
			}
		}
		return params;
	}

	private int getNumConstructorArgsForMarkedBean(IMarker marker) throws CoreException {
		IStructuredModel model = null;
		Element beanElement = null;
		try {
			model = XmlQuickFixUtil.getModel(marker);

			if (model == null) {
				return 0;
			}

			beanElement = XmlQuickFixUtil.getMarkerElement(model, marker);

			if (beanElement == null) {
				return 0;
			}

			IBeansModel beansModel = BeansCorePlugin.getModel();
			if (beansModel == null) {
				return 0;
			}

			IBeansConfig beansConfig = beansModel.getConfig((IFile) marker.getResource(), false);
			String beanName = beanElement.getAttribute("id");
			IBean bean = BeansModelUtils.getBean(beanName, beansConfig);
			if (bean != null && bean.getConstructorArguments() != null) {
				return bean.getConstructorArguments().size();
			}
			return 0;
		}
		finally {
			if (model != null) {
				model.releaseFromEdit();
			}
		}
	}

	@Override
	protected String getTargetClass() {
		return targetClass;
	}

}
