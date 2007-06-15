/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.mylar.ui;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.mylar.context.core.ContextCorePlugin;
import org.eclipse.mylar.context.core.IMylarContext;
import org.eclipse.mylar.context.core.IMylarContextListener;
import org.eclipse.mylar.context.core.IMylarElement;
import org.eclipse.mylar.context.ui.ContextUiPlugin;
import org.eclipse.mylar.internal.context.ui.ContextUiPrefContstants;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.xml.ui.internal.tabletree.XMLMultiPageEditorPart;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.mylar.core.BeansContextStructureBridge;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * {@link IMylarContextListener} that handles collapsing and expanding of Xml
 * nodes in the {@link XMLMultiPageEditorPart}.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings( { "restriction", "deprecation" })
public class BeansActiveFoldingListener implements IMylarContextListener {

	private static BeansContextStructureBridge BRIDGE = (BeansContextStructureBridge) ContextCorePlugin
			.getDefault().getStructureBridge(
					BeansContextStructureBridge.CONTENT_TYPE);

	private final XMLMultiPageEditorPart editor;

	private boolean enabled = false;

	private IPropertyChangeListener PREFERENCE_LISTENER = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(
					ContextUiPrefContstants.ACTIVE_FOLDING_ENABLED)) {
				if (event.getNewValue().equals(Boolean.TRUE.toString())) {
					enabled = true;
				}
				else {
					enabled = false;
				}
				updateFolding();
			}
		}
	};

	public BeansActiveFoldingListener(XMLMultiPageEditorPart editor) {
		this.editor = editor;
		ContextCorePlugin.getContextManager().addListener(this);
		ContextUiPlugin.getDefault().getPluginPreferences()
				.addPropertyChangeListener(PREFERENCE_LISTENER);

		enabled = ContextUiPlugin.getDefault().getPreferenceStore().getBoolean(
				ContextUiPrefContstants.ACTIVE_FOLDING_ENABLED);

		updateFolding();
	}

	private void collapseDocument(ProjectionAnnotationModel annotationModel) {
		Iterator annotations = annotationModel.getAnnotationIterator();
		// first collapse all
		while (annotations.hasNext()) {
			Annotation annotation = (Annotation) annotations.next();
			annotationModel.collapse(annotation);
		}
	}

	public void contextActivated(IMylarContext context) {
		updateFolding();
	}

	public void contextCleared(IMylarContext context) {
		updateFolding();
	}

	public void contextDeactivated(IMylarContext context) {
		updateFolding();
	}

	public void dispose() {
		ContextCorePlugin.getContextManager().removeListener(this);
		ContextUiPlugin.getDefault().getPluginPreferences()
				.removePropertyChangeListener(PREFERENCE_LISTENER);
	}

	public void elementDeleted(IMylarElement node) {
		// ignore
	}

	private void expandDocument(ProjectionAnnotationModel annotationModel) {
		Iterator annotations = annotationModel.getAnnotationIterator();
		while (annotations.hasNext()) {
			Annotation annotation = (Annotation) annotations.next();
			annotationModel.expand(annotation);
		}
	}

	private void expandIfElementIsOfInterest(
			ProjectionAnnotationModel annotationModel,
			IStructuredDocument document, ISourceModelElement modelElement) {
		IMylarElement mylarElement = ContextCorePlugin.getContextManager()
				.getElement(BRIDGE.getHandleIdentifier(modelElement));
		if (mylarElement != null && mylarElement.getInterest().isInteresting()) {
			try {
				int startOffset = document.getLineOffset(modelElement
						.getElementStartLine());
				int endOffset = document.getLineOffset(modelElement
						.getElementEndLine());
				int length = endOffset - startOffset;
				annotationModel.expandAll(startOffset, length);
			}
			catch (BadLocationException e) {
			}
		}
	}

	public void interestChanged(List<IMylarElement> elements) {
		final ITextEditor viewer = (ITextEditor) editor
				.getAdapter(ITextEditor.class);
		final IWorkbench workbench = PlatformUI.getWorkbench();
		for (final IMylarElement element : elements) {
			workbench.getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (viewer instanceof StructuredTextEditor
							&& ((StructuredTextEditor) viewer).getTextViewer() != null) {

						IStructuredDocument document = ((StructuredTextEditor) viewer)
								.getModel().getStructuredDocument();
						ProjectionAnnotationModel annotationModel = ((StructuredTextEditor) viewer)
								.getTextViewer().getProjectionAnnotationModel();

						if (element != null) {
							Object modelElement = (Object) BRIDGE
									.getObjectForHandle(element
											.getHandleIdentifier());
							if (modelElement != null
									&& modelElement instanceof ISourceModelElement) {
								expandIfElementIsOfInterest(annotationModel,
										document,
										(ISourceModelElement) modelElement);
							}
						}
					}
				}
			});
		}
	}

	public void landmarkAdded(IMylarElement element) {
		// ignore
	}

	public void landmarkRemoved(IMylarElement element) {
		// ignore
	}

	public void relationsChanged(IMylarElement node) {
		// ignore
	}

	public void updateFolding() {
		final ITextEditor viewer = (ITextEditor) editor
				.getAdapter(ITextEditor.class);
		final IWorkbench workbench = PlatformUI.getWorkbench();

		if (!enabled
				|| !ContextCorePlugin.getContextManager().isContextActive()) {
			workbench.getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (viewer instanceof StructuredTextEditor
							&& ((StructuredTextEditor) viewer).getTextViewer() != null) {
						ProjectionAnnotationModel annotationModel = ((StructuredTextEditor) viewer)
								.getTextViewer().getProjectionAnnotationModel();
						expandDocument(annotationModel);
					}
				}
			});
		}
		else if (editor.getEditorInput() == null
				|| !(editor.getEditorInput() instanceof IFileEditorInput)) {
			return;
		}
		else {

			IFileEditorInput editorInput = (IFileEditorInput) editor
					.getEditorInput();
			IFile file = editorInput.getFile();

			if (file != null && BeansCoreUtils.isBeansConfig(file)) {

				final IBeansConfig beansConfig = BeansCorePlugin.getModel()
						.getConfig(file);

				workbench.getDisplay().asyncExec(new Runnable() {

					public void run() {
						if (viewer instanceof StructuredTextEditor
								&& ((StructuredTextEditor) viewer)
										.getTextViewer() != null) {

							ProjectionAnnotationModel annotationModel = ((StructuredTextEditor) viewer)
									.getTextViewer()
									.getProjectionAnnotationModel();
							collapseDocument(annotationModel);

							IStructuredDocument document = ((StructuredTextEditor) viewer)
									.getModel().getStructuredDocument();
							for (IBean bean : BeansModelUtils.getBeans(
									beansConfig, new NullProgressMonitor())) {
								expandIfElementIsOfInterest(annotationModel,
										document, bean);
							}
						}
					}
				});
			}
		}
	}
}