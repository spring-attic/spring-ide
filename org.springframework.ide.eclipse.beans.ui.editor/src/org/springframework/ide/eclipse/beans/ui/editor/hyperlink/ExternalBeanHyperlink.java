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
package org.springframework.ide.eclipse.beans.ui.editor.hyperlink;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 */
public class ExternalBeanHyperlink implements IHyperlink {

	private final IRegion region;

	private final ISourceModelElement modelElement;

	/**
	 * Creates a new Java element hyperlink.
	 */
	public ExternalBeanHyperlink(ISourceModelElement bean, IRegion region) {
		this.region = region;
		this.modelElement = bean;
	}

	public IRegion getHyperlinkRegion() {
		return this.region;
	}

	public String getTypeLabel() {
		return null;
	}

	public String getHyperlinkText() {
		return "Open '" + modelElement.getElementName() + "'";
	}

	public void open() {
		BeansUIUtils.openInEditor(modelElement);
	}

}
