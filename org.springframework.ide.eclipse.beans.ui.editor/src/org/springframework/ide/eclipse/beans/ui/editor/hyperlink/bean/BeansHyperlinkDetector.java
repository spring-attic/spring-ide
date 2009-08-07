/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.hyperlink.bean;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.BeanHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.ClassHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.NamespaceHyperlinkDetectorSupport;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * Detects hyperlinks in XML tags. Includes detection of bean classes and bean properties in
 * attribute values. Resolves bean references (including references to parent beans or factory
 * beans).
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @author Leo Dos Santos
 */
public class BeansHyperlinkDetector extends NamespaceHyperlinkDetectorSupport implements
		IHyperlinkDetector {

	/**
	 * Returns <code>true</code> if given attribute is openable.
	 */
	public boolean isLinkableAttr(Attr attr) {
		String attrName = attr.getName();
		if ("http://www.springframework.org/schema/p".equals(attr.getNamespaceURI())
				&& attrName.endsWith("-ref")) {
			return true;
		}
		return super.isLinkableAttr(attr);
	}

	@Override
	public void init() {
		ClassHyperlinkCalculator javaElement = new ClassHyperlinkCalculator();
		registerHyperlinkCalculator("class", javaElement);
		registerHyperlinkCalculator("value", javaElement);
		registerHyperlinkCalculator("match", javaElement);
		registerHyperlinkCalculator("key-type", javaElement);
		registerHyperlinkCalculator("value-type", javaElement);
		registerHyperlinkCalculator("value", "type", javaElement);
		registerHyperlinkCalculator("constructor-arg", "type", javaElement);

		BeanHyperlinkCalculator beanRef = new BeanHyperlinkCalculator();
		registerHyperlinkCalculator("parent", beanRef);
		registerHyperlinkCalculator("factory-bean", beanRef);
		registerHyperlinkCalculator("depends-on", beanRef);
		registerHyperlinkCalculator("bean", beanRef);
		registerHyperlinkCalculator("local", beanRef);
		registerHyperlinkCalculator("parent", beanRef);
		registerHyperlinkCalculator("ref", beanRef);
		registerHyperlinkCalculator("replacer", beanRef);
		registerHyperlinkCalculator("key-ref", beanRef);
		registerHyperlinkCalculator("value-ref", beanRef);
		registerHyperlinkCalculator("alias", "name", beanRef);

		InitDestroyMethodHyperlinkCalculator initDestroy = new InitDestroyMethodHyperlinkCalculator();
		registerHyperlinkCalculator("init-method", initDestroy);
		registerHyperlinkCalculator("destroy-method", initDestroy);

		LookupReplaceMethodHyperlinkCalculator lookupReplace = new LookupReplaceMethodHyperlinkCalculator();
		registerHyperlinkCalculator("lookup-method", "name", lookupReplace);
		registerHyperlinkCalculator("replaced-method", "name", lookupReplace);

		registerHyperlinkCalculator("property", "name", new PropertyNameHyperlinkCalculator());
		registerHyperlinkCalculator("factory-method", new FactoryMethodHyperlinkCalculator());
		
		registerHyperlinkCalculator("import", "resource", new ImportHyperlinkCalculator());
	}

	public IHyperlink createHyperlink(String name, String target, Node node, Node parentNode,
			IDocument document, ITextViewer textViewer, IRegion hyperlinkRegion, IRegion cursor) {
		IHyperlink hyperlink = super.createHyperlink(name, target, node, parentNode, document,
				textViewer, hyperlinkRegion, cursor);
		if (hyperlink == null && name != null) {
			String parentName = null;
			if (parentNode != null) {
				parentName = parentNode.getNodeName();
			}

			if (name.endsWith("-ref")) {
				return new BeanHyperlinkCalculator().createHyperlink(parentName, target, node,
						parentNode, document, textViewer, hyperlinkRegion, cursor);
			}
		}
		return hyperlink;
	}

}
