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
package org.springframework.ide.eclipse.beans.mylyn;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.wst.xml.ui.internal.tabletree.XMLMultiPageEditorPart;
import org.osgi.framework.BundleContext;
import org.springframework.ide.eclipse.beans.mylyn.ui.BeansActiveFoldingEditorTracker;

/**
 * The activator class controls the plug-in life cycle.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.beans.ui.mylyn";

	private static Activator plugin;

	private BeansActiveFoldingEditorTracker editorTracker;

	public Activator() {
		plugin = this;
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);

		final IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getDisplay().asyncExec(new Runnable() {
			public void run() {
				installEditorTracker(workbench);
			}
		});
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static Activator getDefault() {
		return plugin;
	}

	private void installEditorTracker(IWorkbench workbench) {
		editorTracker = new BeansActiveFoldingEditorTracker();
		editorTracker.install(workbench);

		// update editors that are already opened
		for (IWorkbenchWindow w : PlatformUI.getWorkbench()
				.getWorkbenchWindows()) {
			IWorkbenchPage page = w.getActivePage();
			if (page != null) {
				IEditorReference[] references = page.getEditorReferences();
				for (int i = 0; i < references.length; i++) {
					IEditorPart part = references[i].getEditor(false);
					if (part != null && part instanceof XMLMultiPageEditorPart) {
						XMLMultiPageEditorPart editor = (XMLMultiPageEditorPart) part;
						editorTracker.registerEditor(editor);
					}
				}
			}
		}
	}
}
