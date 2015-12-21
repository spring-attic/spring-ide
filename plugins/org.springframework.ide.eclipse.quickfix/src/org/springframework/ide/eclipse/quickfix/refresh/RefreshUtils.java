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
package org.springframework.ide.eclipse.quickfix.refresh;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.sse.ui.internal.reconcile.DirtyRegionProcessor;
import org.eclipse.wst.xml.ui.internal.tabletree.XMLMultiPageEditorPart;
import org.springframework.ide.eclipse.config.core.IConfigEditor;
import org.springframework.util.ReflectionUtils;


/**
 * @author Terry Denney
 * @author Christian Dupuis
 * @author Leo Dos Santos
 */
public class RefreshUtils {

	public static void refreshCurrentEditor(IResource resource) {
		Set<IResource> resources = new HashSet<IResource>();
		resources.add(resource);
		refreshEditors(resources);
	}

	public static void refreshEditors(Set<IResource> resources) {
		IWorkbenchWindow[] workbenchWindows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (IWorkbenchWindow workbenchWindow : workbenchWindows) {
			IWorkbenchPage[] pages = workbenchWindow.getPages();
			for (IWorkbenchPage page : pages) {
				IEditorReference[] editorReferences = page.getEditorReferences();
				for (IEditorReference editorReference : editorReferences) {
					IWorkbenchPart part = editorReference.getPart(false);
					if (part != null && !part.getSite().getShell().isDisposed()) {
						refreshEditor(part, resources);
					}
				}
			}
		}
	}

	private static void refreshEditor(final IWorkbenchPart part, final Set<IResource> resources) {
		part.getSite().getWorkbenchWindow().getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (!(part instanceof IConfigEditor || part instanceof XMLMultiPageEditorPart)) {
					return;
				}

				// Check that we only update editors that have Spring config
				// files open
				IEditorInput input = ((IEditorPart) part).getEditorInput();
				if (input instanceof IFileEditorInput) {
					IFile file = ((IFileEditorInput) input).getFile();
					if (!resources.contains(file) || resources.size() == 0) {
						return;
					}
				}

				StructuredTextEditor textEditor = null;
				if (part instanceof IConfigEditor) {
					IConfigEditor editor = (IConfigEditor) part;
					textEditor = editor.getSourcePage();
				}
				else if (part instanceof XMLMultiPageEditorPart) {
					XMLMultiPageEditorPart xmlEditor = (XMLMultiPageEditorPart) part;
					IEditorPart[] editors = xmlEditor.findEditors(xmlEditor.getEditorInput());
					for (IEditorPart editor : editors) {
						if (editor instanceof StructuredTextEditor) {
							textEditor = ((StructuredTextEditor) editor);
						}
					}
				}

				if (textEditor != null) {
					SourceViewerConfiguration configuration = null;
					try {
						Method getSourceViewerConfigurationMethod = ReflectionUtils.findMethod(textEditor.getClass(),
								"getSourceViewerConfiguration");
						getSourceViewerConfigurationMethod.setAccessible(true);
						configuration = (SourceViewerConfiguration) getSourceViewerConfigurationMethod
								.invoke(textEditor);
					}
					catch (Exception e) {
						// TODO CD add logging here
					}
					if (configuration != null) {
						ISourceViewer textViewer = textEditor.getTextViewer();
						IReconciler reconciler = configuration.getReconciler(textViewer);
						if (reconciler instanceof DirtyRegionProcessor) {
							((DirtyRegionProcessor) reconciler).setDocument(textViewer.getDocument());
						}
					}
				}
			}
		});
	}
}
