/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.ide.eclipse.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

public class SpringUIUtils {

	/**
	 * Returns the standard display to be used. The method first checks, if
	 * the thread calling this method has an associated display. If so, this
	 * display is returned. Otherwise the method returns the default display.
	 */
	public static final Display getStandardDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}

	/**
	 * Returns a button with the given label, id, enablement and selection
	 * listener.
	 */
	public static final Button createButton(Composite parent, String label,
							boolean enabled, SelectionListener buttonListener) {
		Button button = new Button(parent, SWT.PUSH);
		button.setFont(parent.getFont());
		button.setText(label);
		button.setEnabled(enabled);
		button.addSelectionListener(buttonListener);

		// calculate button layout
		FontMetrics fontMetrics = getFontMetrics(button);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		int widthHint = Dialog.convertHorizontalDLUsToPixels(fontMetrics,
												 IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT,
														  SWT.DEFAULT, true).x);
		data.heightHint = Dialog.convertVerticalDLUsToPixels(fontMetrics,
												IDialogConstants.BUTTON_HEIGHT);
		button.setLayoutData(data);
		return button;
	}

	/**
	 * Returns the font metrics for given control.
	 */
	public static final FontMetrics getFontMetrics(Control control) {
		FontMetrics fontMetrics = null;
		GC gc = new GC(control);
		try {
			gc.setFont(control.getFont());
			fontMetrics = gc.getFontMetrics();
		} finally {
			gc.dispose();
		}
		return fontMetrics;
	}

	/**
	 * Displays specified preferences or property page and returns
	 * <code>true</code> if <code>PreferenceDialog.OK</code> was selected.
	 */
	public static final boolean showPreferencePage(String id,
									IPreferencePage page, final String title) {
		final IPreferenceNode targetNode = new PreferenceNode(id, page);
		
		PreferenceManager manager = new PreferenceManager();
		manager.addToRoot(targetNode);
		final PreferenceDialog dialog = new PreferenceDialog(
							 SpringUIPlugin.getActiveWorkbenchShell(), manager);
		final boolean [] result = new boolean[] { false };
		BusyIndicator.showWhile(getStandardDisplay(), new Runnable() {
			public void run() {
				dialog.create();
				dialog.setMessage(targetNode.getLabelText());
				dialog.getShell().setText(title);
				result[0] = (dialog.open() == PreferenceDialog.OK);
			}
		});
		return result[0];		
	}

    public static final IEditorPart getActiveEditor() {
        IWorkbenchWindow window = SpringUIPlugin.getActiveWorkbenchWindow();
        if (window != null) {
            IWorkbenchPage page = window.getActivePage();
            if (page != null) {
                return page.getActiveEditor();
            }
        }
        return null;
    }    

	/**
	 * Opens given file in associated editor and go to specified line (if > 0).
	 */
	public static final IEditorPart openInEditor(IFile file, int line) {
		IEditorPart editor = null;
		IWorkbenchPage page = SpringUIPlugin.getActiveWorkbenchPage();
		try {
			if (line > 0) {
				IMarker marker = file.createMarker(IMarker.TEXT);
				marker.setAttribute(IMarker.LINE_NUMBER, line);
				editor = IDE.openEditor(page, marker);
			} else {
				editor = IDE.openEditor(page, file);
			}
		} catch (CoreException e) {
			SpringUIPlugin.log(e);
		}
	    return editor;
	}

	public static final IEditorPart openInEditor(IEditorInput input,
												 String editorId) {
		IWorkbenchPage page = SpringUIPlugin.getActiveWorkbenchPage();
		try {
			IEditorPart editPart = page.openEditor(input, editorId);
			if (editPart != null) {
				editPart.setFocus();
				return editPart;
			}
		} catch (PartInitException e) {
			SpringUIPlugin.log(e);
		}
		return null;
	}

	public static final IEditorPart openInEditor(IType type) {
		try {
			return JavaUI.openInEditor(type);
		} catch (PartInitException e) {
			SpringUIPlugin.log(e);
		} catch (JavaModelException e) {
			SpringUIPlugin.log(e);
		}
		return null;
	}

	public static final int getCaretOffset(ITextEditor editor) {
		ISelection selection = editor.getSelectionProvider().getSelection();
		if (selection instanceof ITextSelection) {
			return ((ITextSelection) selection).getOffset();
		}
		return -1;
	}

	public static final String getSelectedText(ITextEditor editor) {
		ISelection selection = editor.getSelectionProvider().getSelection();
		if (selection instanceof ITextSelection) {
			return ((ITextSelection) selection).getText().trim();
		}
		return null;
	}
}
