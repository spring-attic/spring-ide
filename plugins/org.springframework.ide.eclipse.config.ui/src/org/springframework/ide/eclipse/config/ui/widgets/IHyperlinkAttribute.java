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
package org.springframework.ide.eclipse.config.ui.widgets;

import org.eclipse.ui.forms.widgets.Hyperlink;

/**
 * Interface for collecting common functionality between
 * {@link AbstractAttributeWidget} classes with hyperlink controls.
 * @author Leo Dos Santos
 * @since 2.1.0
 */
public interface IHyperlinkAttribute {

	/**
	 * Returns the hyperlink displaying the label of attribute, typically the
	 * attribute's name.
	 * 
	 * @return hyperlink displaying the label of attribute, typically the
	 * attribute's name
	 */
	public Hyperlink getHyperlinkControl();

	/**
	 * Clients must implement this method to perform an appropriate action based
	 * on the content of the attribute. This method is called automatically when
	 * the hyperlink is clicked.
	 */
	public void openHyperlink();

}
