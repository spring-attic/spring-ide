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
package org.springframework.ide.eclipse.core.model.validation;

import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * Marker interface that allows for property injection on configured property
 * values.
 * <p>
 * The syntax for configuring property values in <code>plugin.xml</code> is as
 * follows:
 * 
 * <pre>
 * &lt;rule id=&quot;methodOverride&quot;
 *      class=&quot;FooRule&quot;
 *      name=&quot;Foo Rule&quot;
 *      description=&quot;Bar&quot; &gt;
 *      &lt;property name=&quot;threshold&quot; value=&quot;50&quot; /&gt;
 *  &lt;/rule&gt;
 * </pre>
 * 
 * @author Christian Dupuis
 * @since 2.0.4
 */
public interface IConfigurableValidationRule<E extends IModelElement, C extends IValidationContext>
		extends IValidationRule<E, C> {
}
