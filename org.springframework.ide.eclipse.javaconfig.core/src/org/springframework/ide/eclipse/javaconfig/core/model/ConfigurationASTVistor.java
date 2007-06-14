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
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.ide.eclipse.core.model.DefaultModelSourceLocation;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;
import org.springframework.util.StringUtils;

/**
 * Eclipse AST based {@link ASTVisitor} that tries to extract
 * {@link PropertyValue}s from the given source code.
 * <p>
 * This implementation looks for {@link PropertyValue} and
 * {@link RuntimeBeanReference}s and {@link ValueHolder} that go into
 * {@link ConstructorArgumentValues}.
 * @author Christian Dupuis
 * @since 2.0
 */
public class ConfigurationASTVistor extends ASTVisitor {

	private List<BeanComponentDefinition> beanComponentDefinitions;

	private List<BeanAnnotationMetaData> externalBeanAnnotationMetaData;

	private List<MethodInvocation> methodInvocations;

	private List<VariableDeclarationFragment> instanceCreations;

	private BeanComponentDefinition currentBeanComponentDefinition;

	private IModelSourceLocation currentModelSourceLocation;

	public ConfigurationASTVistor(
			List<BeanComponentDefinition> beanComponentDefinitions,
			List<BeanAnnotationMetaData> externalBeanAnnotationMetaData) {
		this.beanComponentDefinitions = beanComponentDefinitions;
		this.externalBeanAnnotationMetaData = externalBeanAnnotationMetaData;
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

	private boolean isExternalBeanReference(String methodName) {
		for (BeanAnnotationMetaData def : this.externalBeanAnnotationMetaData) {
			if (methodName.equals(def.getName())) {
				return true;
			}
		}
		return false;
	}

	public boolean visit(VariableDeclarationFragment node) {
		if (node.getInitializer() instanceof ClassInstanceCreation) {
			instanceCreations.add(node);
			return true;
		}
		return false;
	}

	public boolean visit(MethodDeclaration node) {
		String methodName = node.getName().getFullyQualifiedName();
		BeanComponentDefinition beanComponentDefinition = getBeanComponentDefinition(methodName);
		if (beanComponentDefinition != null) {
			currentBeanComponentDefinition = beanComponentDefinition;
			currentModelSourceLocation = (IModelSourceLocation) beanComponentDefinition
					.getBeanDefinition().getSource();
			methodInvocations = new ArrayList<MethodInvocation>();
			instanceCreations = new ArrayList<VariableDeclarationFragment>();
			return true;
		}
		return false;
	}

	public boolean visit(MethodInvocation node) {
		methodInvocations.add(node);
		return true;
	}

	public boolean visit(ReturnStatement node) {
		CompilationUnit root = (CompilationUnit) node.getRoot();
		if (node.getExpression() instanceof SimpleName) {
			String objName = node.getExpression().toString();
			processSimpleReturnStatementForPropertyValues(root, objName);
			processSimpleReturnStatementForConstructorArguments(root, objName);
		}
		else if (node.getExpression() instanceof ClassInstanceCreation) {
			processConstructorArgumentValue(root, (ClassInstanceCreation) node
					.getExpression());
		}

		return true;
	}

	private void processSimpleReturnStatementForConstructorArguments(
			CompilationUnit root, String objName) {
		for (VariableDeclarationFragment variableDeclaration : instanceCreations) {
			if (variableDeclaration.getInitializer() != null
					&& variableDeclaration.getInitializer() instanceof ClassInstanceCreation
					&& objName.equals(variableDeclaration.getName().toString())) {
				ClassInstanceCreation instanceCreation = (ClassInstanceCreation) variableDeclaration
						.getInitializer();
				processConstructorArgumentValue(root, instanceCreation);
			}
		}
	}

	private void processConstructorArgumentValue(CompilationUnit root,
			ClassInstanceCreation instanceCreation) {
		for (Object obj : instanceCreation.arguments()) {

			ValueHolder value = null;
			int startLine = -1;
			int endLine = -1;

			if (obj instanceof MethodInvocation) {
				String calledMethodName = ((MethodInvocation) obj).getName()
						.getFullyQualifiedName();
				if (getBeanComponentDefinition(calledMethodName) != null
						|| isExternalBeanReference(calledMethodName)) {
					value = new ValueHolder(new RuntimeBeanReference(
							calledMethodName));
					startLine = root.getLineNumber(((MethodInvocation) obj)
							.getStartPosition());
					endLine = root.getLineNumber(((MethodInvocation) obj)
							.getStartPosition()
							+ ((MethodInvocation) obj).getLength());
				}
			}

			if (value == null) {
				value = new ValueHolder(obj.toString());
				startLine = root.getLineNumber(((Expression) obj)
						.getStartPosition());
				endLine = root.getLineNumber(((Expression) obj)
						.getStartPosition()
						+ ((Expression) obj).getLength());
			}

			value.setSource(new DefaultModelSourceLocation(startLine, endLine,
					currentModelSourceLocation.getResource()));
			currentBeanComponentDefinition.getBeanDefinition()
					.getConstructorArgumentValues().addGenericArgumentValue(
							value);
		}
	}

	private void processSimpleReturnStatementForPropertyValues(
			CompilationUnit root, String objName) {
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
							String calledMethodName = nestedInvocation
									.getName().getFullyQualifiedName();
							if (getBeanComponentDefinition(calledMethodName) != null
									|| isExternalBeanReference(calledMethodName)) {
								property = new PropertyValue(propertyName,
										new RuntimeBeanReference(
												calledMethodName));
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
	}
}