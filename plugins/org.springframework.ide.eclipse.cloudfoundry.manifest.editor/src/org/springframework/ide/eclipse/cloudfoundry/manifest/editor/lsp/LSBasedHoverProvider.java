/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.cloudfoundry.manifest.editor.lsp;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.lsp4e.operations.hover.LSBasedHover;
import org.springframework.ide.eclipse.editor.support.hover.HoverInfo;
import org.springframework.ide.eclipse.editor.support.hover.HoverInfoProvider;

/**
 * @author Martin Lippert
 */
@SuppressWarnings("restriction")
public class LSBasedHoverProvider implements HoverInfoProvider {

	private ISourceViewer viewer;
	private LSBasedHover lsBasedHover;

	public LSBasedHoverProvider(ISourceViewer viewer) {
		this.viewer = viewer;
		this.lsBasedHover = new LSBasedHover();
	}

	@Override
	public HoverInfo getHoverInfo(IDocument doc, IRegion r) {
		String hoverInfo = this.lsBasedHover.getHoverInfo(viewer, r);
		return new HoverInfo() {
			@Override
			protected String renderAsHtml() {
				return hoverInfo;
			}
		};
	}

	@Override
	public IRegion getHoverRegion(IDocument document, int offset) {
		IRegion region = this.lsBasedHover.getHoverRegion(viewer, offset);
		return region;
	}

}
