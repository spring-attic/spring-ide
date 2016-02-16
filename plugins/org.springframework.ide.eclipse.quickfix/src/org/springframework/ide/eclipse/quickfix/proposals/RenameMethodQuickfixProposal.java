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
package org.springframework.ide.eclipse.quickfix.proposals;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.refactoring.RenameSupport;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.xml.ui.internal.tabletree.XMLMultiPageEditorPart;
import org.springframework.ide.eclipse.config.core.IConfigEditor;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.java.Introspector.Public;
import org.springframework.ide.eclipse.core.java.Introspector.Static;


/**
 * @author Terry Denney
 */
public class RenameMethodQuickfixProposal extends BeanAttributeQuickFixProposal implements ICompletionProposal {

	private final String className;

	private final String methodName;

	private final IFile beanFile;

	private final IProject project;

	private IEditorPart editor;

	public RenameMethodQuickfixProposal(int offset, int length, String text, String className, boolean missingEndQuote,
			IFile beanFile, IProject project) {
		super(offset, length, missingEndQuote);
		this.className = className;
		this.beanFile = beanFile;
		this.methodName = text;
		this.project = project;
	}

	@Override
	public void applyQuickFix(final IDocument document) {
		final LinkedPosition position = new LinkedPosition(document, getOffset(), getLength());
		LinkedPositionGroup group = new LinkedPositionGroup();

		LinkedModeModel model = new LinkedModeModel();
		try {
			group.addPosition(position);
			model.addGroup(group);
			model.forceInstall();

			ITextViewer viewer = getViewer(document);
			if (viewer == null) {
				return;
			}

			Point originalSelection = viewer.getSelectedRange();
			LinkedModeUI ui = new LinkedModeUI(model, viewer);
			ui.setExitPosition(viewer, getOffset(), 0, Integer.MAX_VALUE);

			model.addLinkingListener(new ILinkedModeListener() {

				public void left(LinkedModeModel model, int flags) {
					if ((flags & ILinkedModeListener.UPDATE_CARET) > 0) {
						try {
							String newName = position.getContent();
							Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
							doRename(newName, shell);
						}
						catch (BadLocationException e) {
						}
					}
				}

				public void resume(LinkedModeModel model, int flags) {
				}

				public void suspend(LinkedModeModel model) {
				}
			});

			ui.enter();

			viewer.setSelectedRange(originalSelection.x, originalSelection.y);
		}
		catch (BadLocationException e) {
		}
	}

	// public for testing
	public void doRename(String newMethodName, Shell shell) {
		try {
			IType type = JdtUtils.getJavaType(project, className);
			if (type != null) {
				IMethod method = Introspector.findMethod(type, methodName, 0, Public.DONT_CARE, Static.DONT_CARE);
				if (method != null) {
					RenameSupport renameSupport = RenameSupport.create(method, newMethodName,
							RenameSupport.UPDATE_REFERENCES);

					renameSupport.perform(shell, PlatformUI.getWorkbench().getActiveWorkbenchWindow());

					if (editor instanceof EditorPart) {
						((EditorPart) editor).doSave(new NullProgressMonitor());
					}
				}
			}
		}
		catch (JavaModelException e) {
		}
		catch (CoreException e) {
		}
		catch (InterruptedException e) {
		}
		catch (InvocationTargetException e) {
		}
	}

	public String getDisplayString() {
		return "Rename method in file";
	}

	public Image getImage() {
		return JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_LINKED_RENAME);
	}

	private ITextViewer getViewer(IDocument document) {
		try {
			if (editor == null) {
				editor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), beanFile);
			}

			return getViewer(editor);
		}
		catch (PartInitException e) {
		}
		return null;
	}

	private ITextViewer getViewer(IEditorPart editorPart) {
		if (editorPart instanceof IConfigEditor) {
			IConfigEditor configEditor = (IConfigEditor) editorPart;
			return configEditor.getTextViewer();
		}
		if (editorPart instanceof StructuredTextEditor) {
			return ((StructuredTextEditor) editorPart).getTextViewer();
		}
		if (editorPart instanceof XMLMultiPageEditorPart) {
			Object result = QuickfixReflectionUtils.callProtectedMethod(editorPart, "getTextEditor");
			return getViewer((IEditorPart) result);
		}

		return null;
	}
}
