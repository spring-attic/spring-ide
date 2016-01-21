/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IViewerCreator;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.springframework.ide.eclipse.cloudfoundry.manifest.editor.ManifestYamlSourceViewerConfiguration;

/**
 * Create compare and merge viewer for YAML content
 *
 * @author Alex Boyko
 *
 */
public class YamlMergeViewerCreator implements IViewerCreator {

	@Override
	public Viewer createViewer(Composite parent, CompareConfiguration config) {
		return new TextMergeViewer(parent, SWT.NONE, config) {

			@Override
			protected SourceViewer createSourceViewer(Composite parent, int textOrientation) {
				SourceViewer viewer = new SourceViewer(parent, null, null, true, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);;
				viewer.configure(new ManifestYamlSourceViewerConfiguration());
				return viewer;
			}

			@Override
			protected void configureTextViewer(TextViewer textViewer) {
				/*
				 * Nothing to do
				 */
			}

		};
	}

}
