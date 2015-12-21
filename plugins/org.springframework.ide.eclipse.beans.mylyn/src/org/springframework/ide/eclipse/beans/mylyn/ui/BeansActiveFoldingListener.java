/*******************************************************************************
 * Copyright (c) 2007, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.mylyn.ui;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.mylyn.context.core.AbstractContextListener;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionContext;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.internal.context.core.ContextCorePlugin;
import org.eclipse.mylyn.internal.java.ui.JavaUiBridgePlugin;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.xml.ui.internal.tabletree.XMLMultiPageEditorPart;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.mylyn.core.BeansContextStructureBridge;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * {@link IInteractionContextListener} that handles collapsing and expanding of XML nodes in the
 * {@link XMLMultiPageEditorPart}.
 * @author Christian Dupuis
 * @author Terry Denney
 * @since 2.0.1
 */
@SuppressWarnings( { "restriction", "deprecation" })
public class BeansActiveFoldingListener extends AbstractContextListener {

	private static BeansContextStructureBridge BRIDGE = (BeansContextStructureBridge) ContextCore
			.getStructureBridge(BeansContextStructureBridge.CONTENT_TYPE);

	private final IEditorPart editor;

	private boolean enabled = false;

	private IPropertyChangeListener PREFERENCE_LISTENER = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(JavaUiBridgePlugin.AUTO_FOLDING_ENABLED)) {
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

	public BeansActiveFoldingListener(IEditorPart editor) {
		this.editor = editor;
		ContextCorePlugin.getContextManager().addListener(this);
		JavaUiBridgePlugin.getDefault().getPluginPreferences().addPropertyChangeListener(
				PREFERENCE_LISTENER);

		enabled = JavaUiBridgePlugin.getDefault().getPreferenceStore().getBoolean(
				JavaUiBridgePlugin.AUTO_FOLDING_ENABLED);

		updateFolding();
	}

	private void collapseDocument(ProjectionAnnotationModel annotationModel) {
		if (annotationModel != null) {
			Iterator annotations = annotationModel.getAnnotationIterator();
			// first collapse all
			while (annotations.hasNext()) {
				Annotation annotation = (Annotation) annotations.next();
				annotationModel.collapse(annotation);
			}
		}
	}

	public void contextActivated(IInteractionContext context) {
		updateFolding();
	}

	public void contextCleared(IInteractionContext context) {
		updateFolding();
	}

	public void contextDeactivated(IInteractionContext context) {
		updateFolding();
	}

	public void dispose() {
		ContextCorePlugin.getContextManager().removeListener(this);
		JavaUiBridgePlugin.getDefault().getPluginPreferences().removePropertyChangeListener(
				PREFERENCE_LISTENER);
	}

	public void elementDeleted(IInteractionElement node) {
		// ignore
	}

	private void expandDocument(ProjectionAnnotationModel annotationModel) {
		Iterator annotations = annotationModel.getAnnotationIterator();
		while (annotations.hasNext()) {
			Annotation annotation = (Annotation) annotations.next();
			annotationModel.expand(annotation);
		}
	}

	private void expandIfElementIsOfInterest(ProjectionAnnotationModel annotationModel,
			IStructuredDocument document, ISourceModelElement modelElement) {
		if (annotationModel != null) {
			IInteractionElement mylynElement = ContextCorePlugin.getContextManager().getElement(
					BRIDGE.getHandleIdentifier(modelElement));
			if (mylynElement != null && mylynElement.getInterest().isInteresting()) {
				try {
					int startOffset = document.getLineOffset(modelElement.getElementStartLine());
					int endOffset = document.getLineOffset(modelElement.getElementEndLine());
					int length = endOffset - startOffset;
					annotationModel.expandAll(startOffset, length);
				}
				catch (BadLocationException e) {
				}
			}
		}
	}

	public void interestChanged(List<IInteractionElement> elements) {
		final ITextEditor viewer = (ITextEditor) editor.getAdapter(ITextEditor.class);
		final IWorkbench workbench = PlatformUI.getWorkbench();
		for (final IInteractionElement element : elements) {
			workbench.getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (viewer instanceof StructuredTextEditor
							&& ((StructuredTextEditor) viewer).getTextViewer() != null) {

						IStructuredDocument document = ((StructuredTextEditor) viewer).getModel()
								.getStructuredDocument();
						ProjectionAnnotationModel annotationModel = ((StructuredTextEditor) viewer)
								.getTextViewer().getProjectionAnnotationModel();

						if (element != null) {
							Object modelElement = (Object) BRIDGE.getObjectForHandle(element
									.getHandleIdentifier());
							if (modelElement != null && modelElement instanceof ISourceModelElement) {
								expandIfElementIsOfInterest(annotationModel, document,
										(ISourceModelElement) modelElement);
							}
						}
					}
				}
			});
		}
	}

	public void landmarkAdded(IInteractionElement element) {
		// ignore
	}

	public void landmarkRemoved(IInteractionElement element) {
		// ignore
	}

	public void relationsChanged(IInteractionElement node) {
		// ignore
	}

	public void updateFolding() {
		final ITextEditor viewer = (ITextEditor) editor.getAdapter(ITextEditor.class);
		final IWorkbench workbench = PlatformUI.getWorkbench();

		if (!enabled || !ContextCorePlugin.getContextManager().isContextActive()) {
			workbench.getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (viewer instanceof StructuredTextEditor
							&& ((StructuredTextEditor) viewer).getTextViewer() != null) {
						ProjectionAnnotationModel annotationModel = ((StructuredTextEditor) viewer)
								.getTextViewer().getProjectionAnnotationModel();
						if (annotationModel != null) {
							expandDocument(annotationModel);
						}
					}
				}
			});
		}
		else if (editor.getEditorInput() == null
				|| !(editor.getEditorInput() instanceof IFileEditorInput)) {
			return;
		}
		else {

			IFileEditorInput editorInput = (IFileEditorInput) editor.getEditorInput();
			IFile file = editorInput.getFile();

			if (file != null && BeansContextStructureBridge.isBeansConfig(file)) {

				final IBeansConfig beansConfig = BeansCorePlugin.getModel().getConfig(file);

				workbench.getDisplay().asyncExec(new Runnable() {

					public void run() {
						if (viewer instanceof StructuredTextEditor
								&& ((StructuredTextEditor) viewer).getTextViewer() != null) {

							ProjectionAnnotationModel annotationModel = ((StructuredTextEditor) viewer)
									.getTextViewer().getProjectionAnnotationModel();
							collapseDocument(annotationModel);

							IStructuredDocument document = ((StructuredTextEditor) viewer)
									.getModel().getStructuredDocument();
							for (IBean bean : BeansModelUtils.getBeans(beansConfig,
									new NullProgressMonitor())) {
								expandIfElementIsOfInterest(annotationModel, document, bean);
							}
						}
					}
				});
			}
		}
	}
}