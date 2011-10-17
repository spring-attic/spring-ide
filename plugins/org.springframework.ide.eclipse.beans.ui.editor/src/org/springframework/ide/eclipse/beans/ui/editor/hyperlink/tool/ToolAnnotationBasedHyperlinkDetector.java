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
package org.springframework.ide.eclipse.beans.ui.editor.hyperlink.tool;

import java.util.Iterator;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.namespaces.ToolAnnotationUtils;
import org.springframework.ide.eclipse.beans.core.namespaces.ToolAnnotationUtils.ToolAnnotationData;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.AbstractAnnotationBasedHyperlinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.ExternalBeanHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.HyperlinkUtils;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.IAnnotationBasedHyperlinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.JavaElementHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.NodeElementHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.java.Introspector.Public;
import org.springframework.ide.eclipse.core.java.Introspector.Static;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * {@link IAnnotationBasedHyperlinkDetector} that reads out the tool annotations and offers bean reference and java
 * hyperlinks.
 * @author Christian Dupuis
 * @since 2.0.3
 */
public class ToolAnnotationBasedHyperlinkDetector extends AbstractAnnotationBasedHyperlinkDetector {

	private static final String REF_ATTRIBUTE = "ref";

	@Override
	public IHyperlink createHyperlink(String name, String target, Node node, Node parentNode, IDocument document,
			ITextViewer textViewer, IRegion hyperlinkRegion, IRegion cursor, Node annotation) {

		IFile file = BeansEditorUtils.getFile(document);
		ToolAnnotationData annotationData = ToolAnnotationUtils.getToolAnnotationData(annotation);

		if (REF_ATTRIBUTE.equals(annotationData.getKind())) {
			Node bean = BeansEditorUtils.getFirstReferenceableNodeById(node.getOwnerDocument(), target, file);
			if (bean != null) {
				IRegion region = getHyperlinkRegion(bean);
				return new NodeElementHyperlink(hyperlinkRegion, region, textViewer);
			}
			else {
				// assume this is an external reference
				Iterator<?> beans = BeansEditorUtils.getBeansFromConfigSets(file).iterator();
				while (beans.hasNext()) {
					IBean modelBean = (IBean) beans.next();
					if (modelBean.getElementName().equals(target)) {
						return new ExternalBeanHyperlink(modelBean, hyperlinkRegion);
					}
				}
			}
		}
		else if (Class.class.getName().equals(annotationData.getExpectedType())) {
			IHyperlink[] detectedHyperlinks = HyperlinkUtils.getXmlJavaHyperlinks(textViewer, hyperlinkRegion);
			
			// only return hyperlink if no xml Java hyperlink will be created to avoid duplicates
			if (detectedHyperlinks == null || detectedHyperlinks.length == 0) {
				IType type = JdtUtils.getJavaType(file.getProject(), target);
				if (type != null) {
					return new JavaElementHyperlink(hyperlinkRegion, type);
				}
			}
		}
		if (annotationData.getExpectedMethodType() != null) {
			String className = evaluateXPathExpression(annotationData.getExpectedMethodType(), node);
			return createMethodHyperlink(target, hyperlinkRegion, file, className);
		}
		else if (annotationData.getExpectedMethodRef() != null) {
			String typeName = evaluateXPathExpression(annotationData.getExpectedMethodRef(), node);
			String className = BeansEditorUtils.getClassNameForBean(file, node.getOwnerDocument(), typeName);
			return createMethodHyperlink(target, hyperlinkRegion, file, className);
		}
		return null;
	}

	private IHyperlink createMethodHyperlink(String target, IRegion hyperlinkRegion, IFile file, String className) {
		IType type = JdtUtils.getJavaType(file.getProject(), className);
		try {
			IMethod method = Introspector.findMethod(type, target, -1, Public.DONT_CARE, Static.DONT_CARE);
			if (method != null) {
				return new JavaElementHyperlink(hyperlinkRegion, method);
			}
		}
		catch (JavaModelException e) {
			// ignore this here
		}
		return null;
	}

	@Override
	public boolean isLinkableAttr(Attr attr, Node annotation) {
		if (ToolAnnotationUtils.ANNOTATION_ELEMENT.equals(annotation.getLocalName())
				&& ToolAnnotationUtils.TOOL_NAMESPACE_URI.equals(annotation.getNamespaceURI())) {
			ToolAnnotationData annotationData = ToolAnnotationUtils.getToolAnnotationData(annotation);
			return annotationData != null
					&& (REF_ATTRIBUTE.equals(annotationData.getKind())
							|| Class.class.getName().equals(annotationData.getExpectedType())
							|| annotationData.getExpectedMethodRef() != null || annotationData.getExpectedMethodType() != null);
		}
		return false;
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

}
