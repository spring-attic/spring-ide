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

package org.springframework.ide.eclipse.beans.ui;


import org.eclipse.core.resources.IFile;
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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
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
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.properties.FilePropertySource;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.ResourcePropertySource;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.model.properties.ChildBeanProperties;
import org.springframework.ide.eclipse.beans.ui.model.properties.ConfigSetProperties;
import org.springframework.ide.eclipse.beans.ui.model.properties.ConstructorArgumentProperties;
import org.springframework.ide.eclipse.beans.ui.model.properties.PropertyProperties;
import org.springframework.ide.eclipse.beans.ui.model.properties.RootBeanProperties;

public class BeansUIUtils {

	/**
	 * Returns the standard display to be used. The method first checks, if
	 * the thread calling this method has an associated display. If so, this
	 * display is returned. Otherwise the method returns the default display.
	 */
	public static Display getStandardDisplay() {
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
	public static Button createButton(Composite parent, String label,
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
	public static FontMetrics getFontMetrics(Control control) {
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
	public static boolean showPreferencePage(String id, IPreferencePage page,
											 final String title) {
		final IPreferenceNode targetNode = new PreferenceNode(id, page);
		
		PreferenceManager manager = new PreferenceManager();
		manager.addToRoot(targetNode);
		final PreferenceDialog dialog = new PreferenceDialog(
							 BeansUIPlugin.getActiveWorkbenchShell(), manager);
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

	/**
	 * Returns edited file from given editor if it's a Spring bean config file.
	 */
	public static IFile getConfigFile(IEditorPart editor) {
		if (editor != null) {
			IEditorInput input = editor.getEditorInput();
			if (input instanceof IFileEditorInput) {
				IFile file = ((IFileEditorInput) input).getFile();
				IBeansProject project = BeansCorePlugin.getModel().getProject(
															 file.getProject());
				if (project != null && project.hasConfig(file)) {
					return file;
				}
			}
		}
		return null;
	}

    public static IEditorPart getActiveEditor() {
        IWorkbenchWindow window = BeansUIPlugin.getActiveWorkbenchWindow();
        if (window != null) {
            IWorkbenchPage page = window.getActivePage();
            if (page != null) {
                return page.getActiveEditor();
            }
        }
        return null;
    }    

	/**
	 * Opens given file in associated editor and go to specified line
	 * (if not -1).
	 * TODO Obsolete with Eclipse 3: Use IDE.openEditor() with an instance of
	 * IMarker holding the line number instead
	 */
	public static IEditorPart openInEditor(IFile file, int line) {
		IEditorInput input = new FileEditorInput(file);
		IEditorPart editPart = openInEditor(input);
		if (editPart != null) {
			if (line != -1 && editPart instanceof AbstractTextEditor) {
				// go to specified line
				AbstractTextEditor editor = (AbstractTextEditor) editPart;
				IDocument doc = editor.getDocumentProvider().getDocument(input);
				if (doc != null) {
					try {
						int start = doc.getLineOffset(line - 1);
						editor.selectAndReveal(start, 0);
					} catch (BadLocationException e) {
						BeansUIPlugin.log(e);
					}
				}
			}
		}
		return editPart;
	}

	public static IEditorPart openInEditor(IEditorInput input) {
		return openInEditor(input, getEditorID(input));
	}

	public static IEditorPart openInEditor(IEditorInput input,
										   String editorId) {
		IWorkbenchPage page = BeansUIPlugin.getActiveWorkbenchPage();
		try {
			IEditorPart editPart = page.openEditor(input, editorId);
			if (editPart != null) {
				editPart.setFocus();
				return editPart;
			}
		} catch (PartInitException e) {
			BeansUIPlugin.log(e);
		}
		return null;
	}

	public static IEditorPart openInEditor(IType type) {
		try {
			return JavaUI.openInEditor(type);
		} catch (PartInitException e) {
			BeansUIPlugin.log(e);
		} catch (JavaModelException e) {
			BeansUIPlugin.log(e);
		}
		return null;
	}

	/**
	 * Returns ID of editor which is associated with given editor input. 
	 */
	public static String getEditorID(IEditorInput input) {
		IEditorRegistry reg = PlatformUI.getWorkbench().getEditorRegistry();
		IEditorDescriptor desc = reg.getDefaultEditor(input.getName());
		if (desc != null) {
			return desc.getId();
		}
		return reg.getDefaultEditor().getId();
	}

	public static int getCaretOffset(ITextEditor editor) {
		ISelection selection = editor.getSelectionProvider().getSelection();
		if (selection instanceof ITextSelection) {
			return ((ITextSelection) selection).getOffset();
		}
		return -1;
	}

	public static String getSelectedText(ITextEditor editor) {
		ISelection selection = editor.getSelectionProvider().getSelection();
		if (selection instanceof ITextSelection) {
			return ((ITextSelection) selection).getText().trim();
		}
		return null;
	}

	/**
	 * Returns a corresponding instance of <code>IPropertySource</code> for the
	 * given <code>IBeansModelElement</code> or null.
	 */
	public static IPropertySource getPropertySource(
												   IBeansModelElement element) {
		if (element instanceof IBeansProject) {
			return new ResourcePropertySource(
										((IBeansProject) element).getProject());
		} else if (element instanceof IBeansConfig) {
			IFile file = ((IBeansConfig) element).getConfigFile();
			if (file != null && file.exists()) {
				return new FilePropertySource(file);
			}
		} else if (element instanceof IBeansConfigSet) {
			return new ConfigSetProperties(((IBeansConfigSet) element));
			
		} else if (element instanceof IBean) {
			IBean bean = ((IBean) element);
			if (bean.isRootBean()) {
				return new RootBeanProperties(bean);
			} else {
				return new ChildBeanProperties(bean);
			}
		} else if (element instanceof IBeanConstructorArgument) {
			return new ConstructorArgumentProperties(
											(IBeanConstructorArgument) element);
		} else if (element instanceof IBeanProperty) {
			return new PropertyProperties((IBeanProperty) element);
		}
		return null;
	}
}
