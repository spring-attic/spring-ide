/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   VMware Inc.		   - initial API and implementation
 *   Spring IDE Developers 
 *****************************************************************************/

package org.springframework.ide.eclipse.osgi.blueprint.internal.util;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.ide.eclipse.osgi.blueprint.internal.jaxb.TautoExportModes;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * &lt;service&gt; attribute callback.
 * 
 * @author Costin Leau
 * @author Arnaud Mergey
 * 
 * @since 3.7.2
 */
public class ServiceAttributeCallback implements AttributeCallback {

	private static final String AUTOEXPORT = "auto-export";
	private static final String AUTOEXPORT_PROP = "autoExport";
	private static final String INTERFACE = "interface";
	private static final String INTERFACES_PROP = "interfaces";
	private static final String REF = "ref";

	public boolean process(Element parent, Attr attribute, BeanDefinitionBuilder bldr) {
		String name = attribute.getLocalName();

		if (INTERFACE.equals(name)) {
			bldr.addPropertyValue(INTERFACES_PROP, attribute.getValue());
			return false;
		} else if (REF.equals(name)) {
			return false;
		}

		else if (AUTOEXPORT.equals(name)) {

			bldr.addPropertyValue(AUTOEXPORT_PROP, Enum.valueOf(TautoExportModes.class, attribute.getValue()));
			return false;
		}

		return true;
	}
}