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
package org.springframework.ide.eclipse.beans.ui.editor.hyperlink.bean;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansImport;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.IMultiHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.w3c.dom.Node;

/**
 * {@link IMultiHyperlinkCalculator} for hyperlinking to imported resources.
 * @author Christian Dupuis
 * @since 2.2.7
 */
@SuppressWarnings("restriction")
public class ImportHyperlinkCalculator implements IMultiHyperlinkCalculator {

	public IHyperlink[] createHyperlinks(String name, String target, Node node, Node parentNode, IDocument document,
			ITextViewer textViewer, IRegion hyperlinkRegion, IRegion cursor) {
		Set<IHyperlink> hyperlinks = new LinkedHashSet<IHyperlink>();

		try {
			int startOffset = ((IDOMNode) node).getStartOffset();
			int endOffset = ((IDOMNode) node).getEndOffset();
			int start = document.getLineOfOffset(startOffset) + 1;
			int end = document.getLineOfOffset(endOffset) + 1;
			IFile file = BeansEditorUtils.getFile(document);
			IModelElement element = BeansModelUtils.getMostSpecificModelElement(start, end, file, null);
			
			if (element instanceof IBeansImport) {
				for (IBeansConfig config : ((IBeansImport) element).getImportedBeansConfigs()) {
					hyperlinks.add(new WorkspaceFileHyperlink(hyperlinkRegion, (IFile) config.getElementResource()));
				}
			}
			
		}
		catch (BadLocationException e) {
		}

		return hyperlinks.toArray(new IHyperlink[hyperlinks.size()]);
	}

	public IHyperlink createHyperlink(String name, String target, Node node, Node parentNode, IDocument document,
			ITextViewer textViewer, IRegion hyperlinkRegion, IRegion cursor) {
		IHyperlink[] hyperlinks = createHyperlinks(name, target, node, parentNode, document, textViewer,
				hyperlinkRegion, cursor);
 		if (hyperlinks != null && hyperlinks.length > 0) {
 			return hyperlinks[0];
 		}
		return null;
	}

	/**
	 * Hyperlink for files within the workspace. (As long as there is an IFile, this can be used) Opens the default
	 * editor for the file.
	 */
	class WorkspaceFileHyperlink implements IHyperlink {

		private IRegion region;

		private IFile file;

		public WorkspaceFileHyperlink(IRegion region, IFile file) {
			this.region = region;
			this.file = file;
		}

		public IRegion getHyperlinkRegion() {
			return region;
		}

		public String getTypeLabel() {
			return null;
		}

		public String getHyperlinkText() {
			String path = file.getFullPath().toString();
			if (path.length() > 60) {
				path = path.substring(0, 25) + "..." + path.substring(path.length() - 25, path.length());
			}
			return "Open '" + path + "'";
		}

		public void open() {
			if ((file != null) && file.exists()) {
				SpringUIUtils.openInEditor(file, 1);
			}
		}
	}

}
