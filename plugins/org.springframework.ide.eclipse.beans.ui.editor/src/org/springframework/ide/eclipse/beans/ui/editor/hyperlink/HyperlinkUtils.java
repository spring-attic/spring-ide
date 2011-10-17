/*******************************************************************************
 * Copyright (c) 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.hyperlink;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.core.StringUtils;
import org.springframework.util.ClassUtils;
import org.w3c.dom.Node;

/**
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 2.2.1
 */
@SuppressWarnings("restriction")
public class HyperlinkUtils {

	/**
	 * Returns the text region of given node.
	 */
	public static IRegion getHyperlinkRegion(Node node) {
		if (node != null) {
			switch (node.getNodeType()) {
			case Node.DOCUMENT_TYPE_NODE:
			case Node.TEXT_NODE:
				IDOMNode docNode = (IDOMNode) node;
				return new Region(docNode.getStartOffset(), docNode.getEndOffset()
						- docNode.getStartOffset());

			case Node.ELEMENT_NODE:
				IDOMElement element = (IDOMElement) node;
				int endOffset;
				if (element.hasEndTag() && element.isClosed()) {
					endOffset = element.getStartEndOffset();
				}
				else {
					endOffset = element.getEndOffset();
				}
				return new Region(element.getStartOffset(), endOffset - element.getStartOffset());

			case Node.ATTRIBUTE_NODE:
				IDOMAttr att = (IDOMAttr) node;
				// do not include quotes in attribute value region
				int regOffset = att.getValueRegionStartOffset();
				int regLength = att.getValueRegionText().length();
				String attValue = att.getValueRegionText();
				if (StringUtils.isQuoted(attValue)) {
					regOffset += 1;
					regLength = regLength - 2;
				}
				return new Region(regOffset, regLength);
			}
		}
		return null;
	}

	/**
	 * Check to make sure org.eclipse.jst.jsp.ui.internal.hyperlink.XMLJavaHyperlinkDetector
	 * does not return any hyperlinks to avoid duplicate hyperlinks shown
	 * 
	 * @param textViewer
	 * @param hyperlinkRegion
	 * @return
	 */
	public static IHyperlink[] getXmlJavaHyperlinks(ITextViewer textViewer, IRegion hyperlinkRegion) {
		try {
			Class<?> clazz = Class.forName("org.eclipse.jst.jsp.ui.internal.hyperlink.XMLJavaHyperlinkDetector", false, ClassUtils.getDefaultClassLoader());
			Object target = clazz.getConstructor().newInstance();
			Method method = clazz.getDeclaredMethod("detectHyperlinks", ITextViewer.class, IRegion.class, boolean.class);
			return (IHyperlink[]) method.invoke(target, textViewer, hyperlinkRegion, true);
		} catch (ClassNotFoundException e) {
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		} catch (InstantiationException e) {
		}
		
		return null;
	}

	
}
