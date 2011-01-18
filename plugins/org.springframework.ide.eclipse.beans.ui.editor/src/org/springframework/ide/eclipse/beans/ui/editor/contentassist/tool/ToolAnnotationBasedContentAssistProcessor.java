/*******************************************************************************
 * Copyright (c) 2008, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.contentassist.tool;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.beans.core.namespaces.ToolAnnotationUtils;
import org.springframework.ide.eclipse.beans.core.namespaces.ToolAnnotationUtils.ToolAnnotationData;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeanReferenceContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.ClassContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.ClassHierachyContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IAnnotationBasedContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistContext;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistProposalRecorder;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.MethodContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansJavaCompletionUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.w3c.dom.Node;

/**
 * {@link IAnnotationBasedContentAssistProcessor} that calculates content assist proposals based on Spring core's tool
 * namespace annotations.
 * <p>
 * Adding the following annotation will trigger a bean reference content assist search:
 * <pre>
 * &lt;tool:annotation kind=“ref”&gt;
 * &lt;tool:expected-type type=“org.springframework.aop.Pointcut”/&gt;
 * &lt;/tool:annotation&gt;
 * </pre>
 * <p>
 * The following will launch the class, package and interface content assist. Depending on the assignable-to value that
 * proposals might be further narrowed to those implementing the interface specified:
 * <pre>
 * &lt;tool:annotation&gt;
 * &lt;tool:expected-type type=“java.lang.Class”/&gt;
 * &lt;tool:assignable-to type=“java.util.List”/&gt;
 * &lt;/tool:annotation&gt;
 * </pre>
 * @author Christian Dupuis
 * @since 2.0.3
 */

// TODO CD add support for restriction (class, interface, both)
public class ToolAnnotationBasedContentAssistProcessor implements IAnnotationBasedContentAssistProcessor {

	private static final String REF_ATTRIBUTE = "ref";

	private static final IContentAssistCalculator BEAN_REFERENCE_CALCULATOR = new BeanReferenceContentAssistCalculator(
			true);

	/**
	 * {@inheritDoc}
	 */
	public void addAttributeValueProposals(IContentAssistContext context, IContentAssistProposalRecorder recorder,
			Node annotation) {
		if (ToolAnnotationUtils.ANNOTATION_ELEMENT.equals(annotation.getLocalName())
				&& ToolAnnotationUtils.TOOL_NAMESPACE_URI.equals(annotation.getNamespaceURI())) {

			ToolAnnotationData annotationData = ToolAnnotationUtils.getToolAnnotationData(annotation);

			if (REF_ATTRIBUTE.equals(annotationData.getKind())) {
				// bean reference content assist
				// TODO CD: add support for typed reference content assist
				BEAN_REFERENCE_CALCULATOR.computeProposals(context, recorder);
			}
			if (Class.class.getName().equals(annotationData.getExpectedType())) {
				// class content assist
				if (annotationData.getAssignableTo() == null) {
					getClassContentAssistCalculator(annotationData).computeProposals(context, recorder);
				}
				else {
					getClassHierachyContentAssistCalculator(annotationData).computeProposals(context, recorder);
				}
			}
			if (annotationData.getExpectedMethodType() != null) {
				String className = evaluateXPathExpression(annotationData.getExpectedMethodType(), context.getNode());
				new NonFilteringMethodContentAssistCalculator(className).computeProposals(context, recorder);
			}
			else if (annotationData.getExpectedMethodRef() != null) {
				String typeName = evaluateXPathExpression(annotationData.getExpectedMethodRef(), context.getNode());
				String className = BeansEditorUtils.getClassNameForBean(context.getFile(), context.getDocument(),
						typeName);
				new NonFilteringMethodContentAssistCalculator(className).computeProposals(context, recorder);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void init() {
		// nothing to do
	}

	protected String evaluateXPathExpression(String xpath, Node node) {
		XPathFactory factory = XPathFactory.newInstance();
		XPath path = factory.newXPath();
		try {
			return path.evaluate(xpath, node);
		}
		catch (XPathExpressionException e) {
			return null;
		}
	}

	protected IContentAssistCalculator getClassContentAssistCalculator(ToolAnnotationData annotationData) {
		if ("class-only".equals(annotationData.getAssignableToRestriction())) {
			return new ClassContentAssistCalculator(false);
		}
		else if ("interface-only".equals(annotationData.getAssignableToRestriction())) {
			return new ClassContentAssistCalculator(true);
		}
		return new ClassContentAssistCalculator();
	}

	protected IContentAssistCalculator getClassHierachyContentAssistCalculator(ToolAnnotationData annotationData) {
		if ("class-only".equals(annotationData.getAssignableToRestriction())) {
			return new ClassHierachyContentAssistCalculator(annotationData.getAssignableTo(),
					BeansJavaCompletionUtils.FLAG_CLASS);
		}
		else if ("interface-only".equals(annotationData.getAssignableToRestriction())) {
			return new ClassHierachyContentAssistCalculator(annotationData.getAssignableTo(),
					BeansJavaCompletionUtils.FLAG_INTERFACE);
		}
		return new ClassHierachyContentAssistCalculator(annotationData.getAssignableTo());
	}

	class NonFilteringMethodContentAssistCalculator extends MethodContentAssistCalculator {

		private final String className;

		public NonFilteringMethodContentAssistCalculator(String className) {
			super(null);
			this.className = className;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected IType calculateType(IContentAssistContext context) {
			return JdtUtils.getJavaType(context.getFile().getProject(), className);
		}
	}
}
