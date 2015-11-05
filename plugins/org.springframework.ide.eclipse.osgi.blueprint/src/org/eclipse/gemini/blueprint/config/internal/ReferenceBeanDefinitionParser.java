/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc., Oracle Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   VMware Inc.
 *   Oracle Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.config.internal;

import org.eclipse.gemini.blueprint.config.internal.util.AttributeCallback;
import org.eclipse.gemini.blueprint.config.internal.util.ParserUtils;
import org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceProxyFactoryBean;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.ide.eclipse.osgi.blueprint.internal.AbstractReferenceDefinitionParser;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * &lt;osgi:reference&gt; element parser.
 * 
 * @author Andy Piper
 * @author Costin Leau
 */
public class ReferenceBeanDefinitionParser extends AbstractReferenceDefinitionParser {

	/**
	 * Reference attribute callback extension that looks for 'singular' reference attributes (such as timeout).
	 * 
	 * @author Costin Leau
	 */
	static class TimeoutAttributeCallback implements AttributeCallback {

		boolean isTimeoutSpecified = false;

		public boolean process(Element parent, Attr attribute, BeanDefinitionBuilder builder) {
			String name = attribute.getLocalName();

			if (TIMEOUT.equals(name)) {
				isTimeoutSpecified = true;
			}

			return true;
		}
	}

	// call properties
	private static final String TIMEOUT_PROP = "timeout";

	// XML attributes/elements
	protected static final String TIMEOUT = "timeout";

	protected Class getBeanClass(Element element) {
		return OsgiServiceProxyFactoryBean.class;
	}

	protected void parseAttributes(Element element, BeanDefinitionBuilder builder, AttributeCallback[] callbacks,
			OsgiDefaultsDefinition defaults) {
		// add timeout callback
		TimeoutAttributeCallback timeoutCallback = new TimeoutAttributeCallback();
		super.parseAttributes(element, builder, ParserUtils.mergeCallbacks(callbacks,
				new AttributeCallback[] { timeoutCallback }), defaults);

		// look for defaults
		if (!timeoutCallback.isTimeoutSpecified) {
			applyDefaultTimeout(builder, defaults);
		}
	}

	/**
	 * Apply default definitions to the existing bean definition. In this case, it means applying the timeout.
	 * 
	 * This method is called when a certain expected element is not present.
	 * @param builder
     * @param defaults
	 */
	protected void applyDefaultTimeout(BeanDefinitionBuilder builder, OsgiDefaultsDefinition defaults) {
		builder.addPropertyValue(TIMEOUT_PROP, new TypedStringValue(defaults.getTimeout()));
	}
}