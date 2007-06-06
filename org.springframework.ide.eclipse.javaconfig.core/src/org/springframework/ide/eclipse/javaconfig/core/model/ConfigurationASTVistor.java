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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.ide.eclipse.core.model.DefaultModelSourceLocation;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;
import org.springframework.util.StringUtils;

/**
 * Eclipse AST based {@link ASTVisitor} that tries to extract
 * {@link PropertyValue}s from the given source code.
 * <p>
 * This implementation looks for {@link PropertyValue} and
 * {@link RuntimeBeanReference}s.
 * @author Christian Dupuis
 * @since 2.0
 */
public class ConfigurationASTVistor extends ASTVisitor {

	private List<BeanComponentDefinition> beanComponentDefinitions;

	private List<MethodInvocation> methodInvocations;

	private BeanComponentDefinition currentBeanComponentDefinition;

	private IModelSourceLocation currentModelSourceLocation;

	public ConfigurationASTVistor(
			List<BeanComponentDefinition> beanComponentDefinitions) {
		this.beanComponentDefinitions = beanComponentDefinitions;
	}

	private BeanComponentDefinition getBeanComponentDefinition(String methodName) {

		for (BeanComponentDefinition def : this.beanComponentDefinitions) {
			Object source = def.getBeanDefinition().getSource();
			if (source instanceof JdtModelSourceLocation) {
				// TODO CD this check is a little weak
				if (methodName.equals(((JdtModelSourceLocation) source)
						.getMethod().getElementName())) {
					return def;
				}
			}
		}
		return null;
	}

	public boolean visit(MethodDeclaration node) {
		String methodName = node.getName().getFullyQualifiedName();
		BeanComponentDefinition beanComponentDefinition = getBeanComponentDefinition(methodName);
		if (beanComponentDefinition != null) {
			currentBeanComponentDefinition = beanComponentDefinition;
			currentModelSourceLocation = (IModelSourceLocation) beanComponentDefinition
					.getBeanDefinition().getSource();
			methodInvocations = new ArrayList<MethodInvocation>();
			return true;
		}
		return false;
	}

	public boolean visit(MethodInvocation node) {
		methodInvocations.add(node);
		return true;
	}

	public boolean visit(ReturnStatement node) {
		String objName = node.getExpression().toString();
		CompilationUnit root = (CompilationUnit) node.getRoot();

		for (MethodInvocation invocation : methodInvocations) {
			// check if the recored method invocation was executed on the same
			// object
			if (invocation.getExpression() != null
					&& objName.equals(invocation.getExpression().toString())) {
				String methodName = invocation.getName()
						.getFullyQualifiedName();
				if (methodName.startsWith("set")) {
					PropertyValue property = null;
					int startLine = -1;
					int endLine = -1;

					List arguments = invocation.arguments();
					String propertyName = StringUtils.uncapitalize(methodName
							.substring(3));
					// setters must have one argument
					if (arguments != null && arguments.size() == 1) {
						if (arguments.get(0) instanceof MethodInvocation) {
							MethodInvocation nestedInvocation = (MethodInvocation) arguments
									.get(0);
							String test = nestedInvocation.getName()
									.getFullyQualifiedName();
							if (getBeanComponentDefinition(test) != null) {
								property = new PropertyValue(propertyName,
										new RuntimeBeanReference(test));
								startLine = root.getLineNumber(nestedInvocation
										.getStartPosition());
								endLine = root.getLineNumber(nestedInvocation
										.getStartPosition()
										+ nestedInvocation.getLength());
							}
						}
						else if (arguments.get(0) instanceof CastExpression) {
							CastExpression castExpression = (CastExpression) arguments
									.get(0);
							if (castExpression.getExpression() instanceof MethodInvocation) {
								MethodInvocation inv = (MethodInvocation) castExpression
										.getExpression();
								if ("getBean".equals(inv.getName()
										.getFullyQualifiedName())
										&& inv.arguments() != null
										&& inv.arguments().size() == 1
										&& inv.arguments().get(0) instanceof StringLiteral) {

									property = new PropertyValue(
											propertyName,
											new RuntimeBeanReference(
													((StringLiteral) inv
															.arguments().get(0))
															.getLiteralValue()));
									startLine = root.getLineNumber(inv
											.getStartPosition());
									endLine = root.getLineNumber(inv
											.getStartPosition()
											+ inv.getLength());
								}
							}
						}
					}

					if (property == null) {
						if (arguments != null && arguments.size() == 1) {
							property = new PropertyValue(propertyName,
									arguments.get(0));
						}
						else {
							property = new PropertyValue(propertyName,
									arguments);
						}
						startLine = root.getLineNumber(invocation
								.getStartPosition());
						endLine = root.getLineNumber(invocation
								.getStartPosition()
								+ invocation.getLength());
					}

					property.setSource(new DefaultModelSourceLocation(
							startLine, endLine, currentModelSourceLocation
									.getResource()));
					currentBeanComponentDefinition.getBeanDefinition()
							.getPropertyValues().addPropertyValue(property);
				}
			}
		}
		return true;
	}
}