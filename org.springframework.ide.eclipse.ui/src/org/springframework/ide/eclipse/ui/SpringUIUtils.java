/*
 * Copyright 2002-2006 the original author or authors.
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
import org.eclipse.core.runtime.IAdaptable;
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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;

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
	 * Returns a button with the given label and selection listener.
	 */
	public static final Button createButton(Composite parent, String labelText,
											SelectionListener listener) {
		return createButton(parent, labelText, listener, 0, true);
	}

	/**
	 * Returns a button with the given label, indentation, enablement and
	 * selection listener.
	 */
	public static final Button createButton(Composite parent, String labelText,
				SelectionListener listener, int indentation, boolean enabled) {
		Button button = new Button(parent, SWT.PUSH);
		button.setFont(parent.getFont());
		button.setText(labelText);
		button.addSelectionListener(listener);
		button.setEnabled(enabled);

		FontMetrics fontMetrics = getFontMetrics(button);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		int widthHint = Dialog.convertHorizontalDLUsToPixels(fontMetrics,
												 IDialogConstants.BUTTON_WIDTH);
		gd.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT,
														  SWT.DEFAULT, true).x);
		gd.horizontalIndent = indentation;
		button.setLayoutData(gd);
		return button;
	}
	
	/**
	 * Returns a check box with the given label.
	 */
	public static final Button createCheckBox(Composite parent,
											  String labelText) {
		Button button = new Button(parent, SWT.CHECK);
		button.setFont(parent.getFont());
		button.setText(labelText);
		button.setLayoutData(new GridData(
										 GridData.HORIZONTAL_ALIGN_BEGINNING));
		return button;
	}

	/**
	 * Returns a text field with the given label.
	 */
	public static final Text createTextField(Composite parent,
							  				 String labelText) {
		return createTextField(parent, labelText, 0, 0);
	}

	/**
	 * Returns a text field with the given label and horizontal indentation.
	 */
	public static final Text createTextField(Composite parent,
							  			   String labelText, int indentation) {
		return createTextField(parent, labelText, indentation, 0);
	}

	/**
	 * Returns a text field with the given label, horizontal indentation and
	 * width hint.
	 */
	public static final Text createTextField(Composite parent,
							String labelText, int indentation, int widthHint) {
		Composite textArea = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		textArea.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = indentation;
		textArea.setLayoutData(gd);

		Label label = new Label(textArea, SWT.NONE);
		label.setText(labelText);
		label.setFont(parent.getFont());
		
		Text text = new Text(textArea, SWT.BORDER | SWT.SINGLE);
		text.setFont(parent.getFont());
		if (widthHint > 0) {
			gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
			gd.widthHint = widthHint;
		} else {
			gd = new GridData(GridData.FILL_HORIZONTAL);
		}
		text.setLayoutData(gd);
		return text;
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
     * Returns the <code>ItextEditor</code> instance for given
     * <code>IEditorPart</code> or <code>null</code> for any non text editor.
     */
    public static final ITextEditor getTextEditor(IEditorPart part) {
		if (part instanceof ITextEditor) {
			return (ITextEditor) part;
		} else if (part instanceof IAdaptable) {
			return (ITextEditor)
							 ((IAdaptable) part).getAdapter(ITextEditor.class);
		}
		return null;
    }

	/**
	 * Opens given model element in associated editor.
	 */
	public static final IEditorPart openInEditor(ISourceModelElement element) {
		IFile file = (IFile) element.getElementResource();
		if (file != null) {
			return openInEditor(file, element.getElementStartLine());
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
