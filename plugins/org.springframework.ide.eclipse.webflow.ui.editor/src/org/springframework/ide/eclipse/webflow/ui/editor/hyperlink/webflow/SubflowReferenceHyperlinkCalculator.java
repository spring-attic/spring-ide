/*******************************************************************************
 * Copyright (c) 2010 - 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.editor.hyperlink.webflow;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.IHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springsource.ide.eclipse.commons.ui.SpringUIUtils;
import org.w3c.dom.Node;

/**
 * {@link IHyperlinkCalculator} for flow references.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 2.3.4
 */
public class SubflowReferenceHyperlinkCalculator implements IHyperlinkCalculator {

	public IHyperlink createHyperlink(final String name, final String target, Node node,
			Node parentNode, IDocument document, ITextViewer textViewer,
			final IRegion hyperlinkRegion, IRegion cursor) {
		IFile file = BeansEditorUtils.getFile(document);
		if (file != null && file.exists()) {
			final IWebflowConfig config = Activator.getModel().getProject(file.getProject())
					.getConfig(target);
			if (config != null) {
				return new IHyperlink() {
	
					public IRegion getHyperlinkRegion() {
						return hyperlinkRegion;
					}
	
					public String getHyperlinkText() {
						return target;
					}
	
					public String getTypeLabel() {
						return name;
					}
	
					public void open() {
						SpringUIUtils.openInEditor(config.getResource(), -1);
					}
				};
			}
		}
		return null;
	}

}
