/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.reconciling;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.properties.editor.ui.UserInteractions;
import org.springframework.ide.eclipse.boot.properties.editor.util.DocumentUtil;

/**
 * Default implementation of {@link QuickfixContext} that derives context information for
 * quickfixes from ISourceViewer.
 *
 * @author Kris De Volder
 */
public class DefaultQuickfixContext implements QuickfixContext {

	private IPreferenceStore preferences;
	private ISourceViewer sourceViever;
	private UserInteractions ui;

	public DefaultQuickfixContext(IPreferenceStore pluginPreferences, ISourceViewer sourceViewer, UserInteractions ui) {
		this.preferences = pluginPreferences;
		this.sourceViever = sourceViewer;
		this.ui = ui;
	}

	public IPreferenceStore getPreferences() {
		return preferences;
	}

	public IProject getProject() {
		IDocument doc = sourceViever.getDocument();
		if (doc!=null) {
			return DocumentUtil.getProject(doc);
		}
		return null;
	}

	@Override
	public IJavaProject getJavaProject() {
		try {
			IProject p = getProject();
			if (p!=null && p.isAccessible() && p.hasNature(JavaCore.NATURE_ID)) {
				return JavaCore.create(p);
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return null;
	}

	@Override
	public UserInteractions getUI() {
		return ui;
	}

}
