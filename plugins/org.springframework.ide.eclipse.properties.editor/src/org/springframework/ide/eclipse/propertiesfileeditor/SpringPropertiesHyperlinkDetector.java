/*******************************************************************************
 * Copyright (c) 2014 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.propertiesfileeditor;

import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.PartInitException;

public class SpringPropertiesHyperlinkDetector extends AbstractHyperlinkDetector {

	public class JavaElementLink implements IHyperlink {

		private final IRegion region;

		private final IJavaElement element;

		public JavaElementLink(IRegion region, IJavaElement element) {
			this.region = region;
			this.element = element;
		}

		public IRegion getHyperlinkRegion() {
			return this.region;
		}

		public void open() {
			if (element != null) {
				try {
					JavaUI.openInEditor(element);
				}
				catch (PartInitException e) {
				}
				catch (JavaModelException e) {
				}
			}
		}

		public String getTypeLabel() {
			return null;
		}

		public String getHyperlinkText() {
			return JavaElementLabels.getElementLabel(element, JavaElementLabels.ALL_POST_QUALIFIED);
		}

	}

	private static final IHyperlink[] NO_LINKS = null; //Caller expects null rather than empty array
	private SpringPropertiesCompletionEngine engine;

	public SpringPropertiesHyperlinkDetector(SpringPropertiesCompletionEngine engine) {
		this.engine = engine;
	}

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		IDocument doc = textViewer.getDocument();
		if (doc!=null) {
			ITypedRegion linkRegion = engine.getHoverRegion(doc, region.getOffset());
			if (linkRegion.getType()==IDocument.DEFAULT_CONTENT_TYPE) {
				List<IJavaElement> targets = engine.getSourceElements(doc, linkRegion.getOffset());
				if (!targets.isEmpty()) {
					IHyperlink[] links = new IHyperlink[targets.size()];
					for (int i = 0; i < links.length; i++) {
						links[i] = new JavaElementLink(linkRegion, targets.get(i));
					}
					return links;
				}
			}
		}
		return NO_LINKS;
	}

}
