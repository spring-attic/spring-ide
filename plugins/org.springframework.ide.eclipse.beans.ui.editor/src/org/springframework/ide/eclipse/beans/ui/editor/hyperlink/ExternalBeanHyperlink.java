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

import java.util.Set;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.springframework.ide.eclipse.beans.core.internal.model.ProfileAwareBeansComponent;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
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
		if (modelElement != null) {
			StringBuilder str = new StringBuilder();
			str.append("Navigate to ");
			str.append(modelElement.getElementName());
			IModelElement parent = modelElement.getElementParent();
			if (parent instanceof ProfileAwareBeansComponent) {
				ProfileAwareBeansComponent beans = (ProfileAwareBeansComponent) parent;
				Set<String> profiles = beans.getProfiles();

				str.append(" in profile");
				if (profiles.size() > 1) {
					str.append("s");
				}
				str.append(" ");

				boolean first = true;
				for(String profile: profiles) {
					if (! first) {
						str.append(", ");
					}
					str.append("\"");
					str.append(profile);
					str.append("\"");
					first = false;
				}
				
				str.append(" - ");
				str.append(modelElement.getElementResource().getName());
				return str.toString();
			}
		}
		return "Navigate to " + modelElement.getElementName();
	}

	public void open() {
		BeansUIUtils.openInEditor(modelElement);
	}

}
