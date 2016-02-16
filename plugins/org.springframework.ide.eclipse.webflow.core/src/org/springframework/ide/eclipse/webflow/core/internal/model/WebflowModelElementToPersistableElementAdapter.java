/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.core.internal.model;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;

/**
 * Utility class tha adapts a given {@link IPersistableWebflowModelElement} to
 * be {@link IPersistableElement}.
 * @author Christian Dupuis
 * @since 2.0
 */
public class WebflowModelElementToPersistableElementAdapter implements
		IPersistableElement {

	private final IWebflowModelElement webflowModelElement;

	public WebflowModelElementToPersistableElementAdapter(
			final IWebflowModelElement webflowModelElement) {
		this.webflowModelElement = webflowModelElement;
	}

	public String getFactoryId() {
		return WebflowModelElementFactory.FACTORY_ID;
	}

	public void saveState(IMemento memento) {
		memento.putString(WebflowModelElementFactory.ELEMENT_ID,
				webflowModelElement.getElementID());
	}
}
