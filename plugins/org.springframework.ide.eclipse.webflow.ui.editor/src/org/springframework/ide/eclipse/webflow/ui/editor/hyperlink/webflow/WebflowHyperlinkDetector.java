/*******************************************************************************
 * Copyright (c) 2007, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.editor.hyperlink.webflow;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.ClassHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.NamespaceHyperlinkDetectorSupport;
import org.w3c.dom.Attr;

/**
 * Detects hyperlinks in XML tags. Includes detection of bean classes and bean properties in
 * attribute values. Resolves bean references (including references to parent beans or factory
 * beans).
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @author Leo Dos Santos
 */
public class WebflowHyperlinkDetector extends NamespaceHyperlinkDetectorSupport implements
		IHyperlinkDetector {

	private static final Set<String> VALID_ATTRIBUTES;

	static {
		VALID_ATTRIBUTES = new LinkedHashSet<String>();
		// web flow 1.x
		VALID_ATTRIBUTES.add("bean");
		VALID_ATTRIBUTES.add("method");
		VALID_ATTRIBUTES.add("to");
		VALID_ATTRIBUTES.add("on-exception");
		VALID_ATTRIBUTES.add("from");
		VALID_ATTRIBUTES.add("to");
		VALID_ATTRIBUTES.add("idref");
		VALID_ATTRIBUTES.add("class");
		VALID_ATTRIBUTES.add("then");
		VALID_ATTRIBUTES.add("else");
		VALID_ATTRIBUTES.add("type");
		VALID_ATTRIBUTES.add("name");
		VALID_ATTRIBUTES.add("flow");

		// web flow 2.x
		VALID_ATTRIBUTES.add("type");
		VALID_ATTRIBUTES.add("class");
		VALID_ATTRIBUTES.add("result-type");
		VALID_ATTRIBUTES.add("bean");
		VALID_ATTRIBUTES.add("subflow-attribute-mapper");

		VALID_ATTRIBUTES.add("to");
		VALID_ATTRIBUTES.add("start-state");
		VALID_ATTRIBUTES.add("then");
		VALID_ATTRIBUTES.add("else");

		VALID_ATTRIBUTES.add("subflow");
		VALID_ATTRIBUTES.add("parent");
	}

	@Override
	public void init() {
		WebflowBeanReferenceHyperlinkCalculator beanRef = new WebflowBeanReferenceHyperlinkCalculator();
		registerHyperlinkCalculator("bean", beanRef);
		registerHyperlinkCalculator("name", beanRef);
		registerHyperlinkCalculator("subflow-attribute-mapper", beanRef);
		
		StateReferenceHyperlinkCalculator stateRef = new StateReferenceHyperlinkCalculator();
		registerHyperlinkCalculator("transition", "to", stateRef);
		registerHyperlinkCalculator("then", stateRef);
		registerHyperlinkCalculator("else", stateRef);
		registerHyperlinkCalculator("idref", stateRef);
		registerHyperlinkCalculator("start-state", stateRef);
		
		ClassHyperlinkCalculator javaElement = new ClassHyperlinkCalculator();
		registerHyperlinkCalculator("to", javaElement);
		registerHyperlinkCalculator("on-exception", javaElement);
		registerHyperlinkCalculator("type", javaElement);
		registerHyperlinkCalculator("class", javaElement);
		registerHyperlinkCalculator("result-type", javaElement);
		
		registerHyperlinkCalculator("method", new WebflowActionMethodHyperlinkCalculator());
		
		SubflowReferenceHyperlinkCalculator flowRef = new SubflowReferenceHyperlinkCalculator();
		registerHyperlinkCalculator("flow", flowRef);
		registerHyperlinkCalculator("subflow", flowRef);
		registerHyperlinkCalculator("parent", flowRef);
	}
	
	/**
	 * Returns <code>true</code> if given attribute is openable.
	 * @param attr
	 * @return
	 */
	public boolean isLinkableAttr(Attr attr) {
		return VALID_ATTRIBUTES.contains(attr.getLocalName());
	}

}
