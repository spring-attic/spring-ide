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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.sse.core.internal.format.IStructuredFormatProcessor;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.xml.core.internal.cleanup.CleanupProcessorXML;
import org.eclipse.wst.xml.core.internal.provisional.format.FormatProcessorXML;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansTypedString;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class converts legacy declarations of JndiObjectFactory to use the new
 * namespace syntax. The input is an error marker identifying the instance of
 * the legacy syntax to be converted. Note: This class is not currently in use
 * because the UseNameSpaceSyntaxMarkerResolution has not been released.
 * @author Wesley Coelho
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class JndiObjectFactorySyntaxConverter {

	private static final String JEE_NAMESPACE_URL_ATTRIBUTE_NAME = "xmlns:jee";

	private static final String JEE_NAMESPACE_URL = "http://www.springframework.org/schema/jee";

	private static final String SCHEMA_LOCATION_ATTRIBUTE_NAME = "xsi:schemaLocation";

	private static final String SCHEMA_LOCATION = "http://www.springframework.org/schema/jee http://www.springframework.org/schema/jee/spring-jee.xsd";

	private static final String NAMESPACE_JNDI_ELEMENT_NAME = "jee:jndi-lookup";

	private static final String ID_ATTRIBUTE_NAME = "id";

	public void convert(IMarker marker) throws CoreException {
		IStructuredModel model = null;
		try {
			model = XmlQuickFixUtil.getModel(marker);
			Element legacyXmlElement = XmlQuickFixUtil.getMarkerElement(model, marker);

			insertNamespaceDeclaration(model);

			IBean jndiObjectFactoryBean = (IBean) BeansCorePlugin.getModel().getElement(
					marker.getAttribute("elementId", ""));

			Element namespaceJndiElement = createNamespaceSyntaxXmlElement(jndiObjectFactoryBean,
					NAMESPACE_JNDI_ELEMENT_NAME, legacyXmlElement.getOwnerDocument());

			// Add the new element node
			legacyXmlElement.getParentNode().insertBefore(namespaceJndiElement, legacyXmlElement);

			// Format the new element node
			IStructuredFormatProcessor formatProcessor = new FormatProcessorXML();
			CleanupProcessorXML cleanupProcessor = new CleanupProcessorXML();
			cleanupProcessor.getCleanupPreferences().setCompressEmptyElementTags(true);

			formatProcessor.formatNode(namespaceJndiElement);
			cleanupProcessor.cleanupNode(namespaceJndiElement);

			legacyXmlElement.getParentNode().removeChild(legacyXmlElement);
		}
		finally {
			if (model != null) {
				model.releaseFromEdit();
			}
		}
	}

	private Element createNamespaceSyntaxXmlElement(IBean bean, String elementName, Document document) {
		Element jndiObjectFactoryElement = document.createElement(elementName);

		jndiObjectFactoryElement.setAttribute(ID_ATTRIBUTE_NAME, bean.getElementName());

		// Iterate over the properties and represent them as namespace-syntax
		// attributes
		for (IBeanProperty currProperty : bean.getProperties()) {
			Object value = currProperty.getValue();
			if (value instanceof BeansTypedString) {
				BeansTypedString propertyValue = (BeansTypedString) value;
				String propertyName = currProperty.getElementName();
				jndiObjectFactoryElement.setAttribute(toHyphenFormat(propertyName), propertyValue.getString());
			}
		}

		return jndiObjectFactoryElement;
	}

	private Node insertNamespaceDeclaration(IStructuredModel model) {

		IStructuredDocumentRegion beansRegion = model.getStructuredDocument().getFirstStructuredDocumentRegion()
				.getNext().getNext();

		Element beansElement = (Element) model.getIndexedRegion(beansRegion.getStartOffset());

		beansElement.setAttribute(JEE_NAMESPACE_URL_ATTRIBUTE_NAME, JEE_NAMESPACE_URL);

		String schemaLocation = beansElement.getAttribute(SCHEMA_LOCATION_ATTRIBUTE_NAME);
		if (schemaLocation != null) {
			if (schemaLocation.indexOf(SCHEMA_LOCATION) == -1) {
				schemaLocation += " \n\t\t" + SCHEMA_LOCATION;
				beansElement.setAttribute(SCHEMA_LOCATION_ATTRIBUTE_NAME, schemaLocation);
			}
		}
		else {
			beansElement.setAttribute(SCHEMA_LOCATION_ATTRIBUTE_NAME, SCHEMA_LOCATION);
		}

		return beansElement;
	}

	/**
	 * Converts the given string according to the convention: camelCase ->
	 * camel-case
	 */
	private String toHyphenFormat(String camelFormat) {
		String hyphenFormat = camelFormat.replaceAll("[A-Z]", "-$0");
		hyphenFormat = hyphenFormat.toLowerCase();
		return hyphenFormat;
	}
}
