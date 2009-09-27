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
package org.springframework.ide.eclipse.osgi.ui.editor.contentassist.blueprint;

import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeanReferenceContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.ClassContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.PropertyBeanReferenceContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.bean.BeansContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.bean.FactoryMethodContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.bean.PropertyNameContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.bean.PropertyValueContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceContentAssistProcessor;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * {@link INamespaceContentAssistProcessor} implementation responsible for the standard <code>bluepring:*</code>
 * namespace.
 * @author Christian Dupuis
 * @since 2.2.7 
 */
public class BlueprintContentAssistProcessor extends BeansContentAssistProcessor {

	@Override
	public void init() {
		
		super.init();
		
		PropertyBeanReferenceContentAssistCalculator propertyBean = new PropertyBeanReferenceContentAssistCalculator();
		registerContentAssistCalculator("http://www.osgi.org/xmlns/blueprint/v1.0.0", "bean", "property", "ref",
				propertyBean);

		BeanReferenceContentAssistCalculator globalBean = new BeanReferenceContentAssistCalculator();
		registerContentAssistCalculator("bean", "factory-ref", globalBean);
		registerContentAssistCalculator("reference", "depends-on", globalBean);
		registerContentAssistCalculator("service", "depends-on", globalBean);
		registerContentAssistCalculator("reference-listener", "ref", globalBean);
		registerContentAssistCalculator("ref", "component-id", globalBean);
		registerContentAssistCalculator("idref", "component-id", globalBean);
		registerContentAssistCalculator("argument", "ref", globalBean);

		registerContentAssistCalculator("http://www.osgi.org/xmlns/blueprint/v1.0.0", "bean", "property", "name",
				new PropertyNameContentAssistCalculator());
		registerContentAssistCalculator("http://www.osgi.org/xmlns/blueprint/v1.0.0", "bean", "property", "value",
				new PropertyValueContentAssistCalculator());

		registerContentAssistCalculator("bean", "factory-method", new FactoryMethodContentAssistCalculator() {
			@Override
			protected Node getFactoryBeanReferenceNode(NamedNodeMap attributes) {
				return attributes.getNamedItem("factory-ref");
			}
		});
		
		ClassContentAssistCalculator clazz = new ClassContentAssistCalculator(true);
		registerContentAssistCalculator("reference-list", "interface", clazz);
		registerContentAssistCalculator("reference", "interface", clazz);
		
		ClassContentAssistCalculator interfac = new ClassContentAssistCalculator(false);
		registerContentAssistCalculator("argument", "type", interfac);


	}

}
