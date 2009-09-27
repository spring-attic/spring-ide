/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.osgi.ui.editor.hyperlink.blueprint;

import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.BeanHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.ClassHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.bean.BeansHyperlinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.bean.FactoryMethodHyperlinkCalculator;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Detects hyperlinks in XML tags. Includes detection of bean classes and bean properties in
 * attribute values. Resolves bean references (including references to parent beans or factory
 * beans).
 * @author Christian Dupuis
 */
public class BlueprintHyperlinkDetector extends BeansHyperlinkDetector implements
		IHyperlinkDetector {

	@Override
	public void init() {

		super.init();
		
		ClassHyperlinkCalculator javaElement = new ClassHyperlinkCalculator();
		registerHyperlinkCalculator("interface", javaElement);
		registerHyperlinkCalculator("argument", "type", javaElement);
		
		BeanHyperlinkCalculator beanRef = new BeanHyperlinkCalculator();
		registerHyperlinkCalculator("component-id", beanRef);

		registerHyperlinkCalculator("factory-method", new FactoryMethodHyperlinkCalculator() {
			@Override
			protected Node getFactoryBeanReferenceNode(NamedNodeMap attributes) {
				return attributes.getNamedItem("factory-ref");
			}
		});
		
		
	}

}
