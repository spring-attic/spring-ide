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
 *   VMware Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.config.internal.util;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * Wrapper callback used for parsing attributes (one at a time) that are
 * non standard (ID, LAZY-INIT, DEPENDS-ON).
 * 
 * @author Costin Leau
 */
public interface AttributeCallback {

	/**
	 * Process the given attribute using the contextual element and bean
	 * builder. Normally, the callback will interact with the bean definition
	 * and set some properties. <p/> If the callback has intercepted an
	 * attribute, it can stop the invocation of the rest of the callbacks on the
	 * stack by returning false.
	 * 
	 * @param parent parent element
	 * @param attribute current intercepted attribute
	 * @param builder builder holding the current bean definition
	 * @return true if the rest of the callbacks should be called or false
	 *         otherwise.
	 */
	boolean process(Element parent, Attr attribute, BeanDefinitionBuilder builder);
}