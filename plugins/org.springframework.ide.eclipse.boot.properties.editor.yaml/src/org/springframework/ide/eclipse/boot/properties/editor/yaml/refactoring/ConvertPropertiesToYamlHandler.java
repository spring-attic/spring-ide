/*******************************************************************************
 * Copyright (c) 2017 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.yaml.refactoring;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.handlers.HandlerUtil;
import org.springframework.ide.eclipse.boot.util.Log;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;

public class ConvertPropertiesToYamlHandler extends AbstractHandler {

	private static ITextFileBuffer getDirtyFileBuffer(IFile file) {
		ITextFileBuffer buffer = FileBuffers.getTextFileBufferManager().getTextFileBuffer(file.getFullPath(), LocationKind.IFILE);
		if (buffer!=null && buffer.isDirty()) {
			return buffer;
		}
		return null;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorInput editorInput = HandlerUtil.getActiveEditorInput(event);
		try {
			if (editorInput instanceof IFileEditorInput) {
				IFile file = ((IFileEditorInput) editorInput).getFile();
				ITextFileBuffer dirtyBuffer = getDirtyFileBuffer(file);
				if (dirtyBuffer!=null) {
					dirtyBuffer.commit(null, true);
				}
				ConvertPropertiesToYamlRefactoring refactoring = new ConvertPropertiesToYamlRefactoring(file);
				RefactoringWizard wizard = new RefactoringWizard(refactoring, 
						RefactoringWizard.DIALOG_BASED_USER_INTERFACE | 
						RefactoringWizard.CHECK_INITIAL_CONDITIONS_ON_OPEN |
						RefactoringWizard.NO_BACK_BUTTON_ON_STATUS_DIALOG
				) {
					
					@Override
					protected void addUserInputPages() {
						//no inputs required
					}
				};
				new RefactoringWizardOpenOperation(wizard).run(HandlerUtil.getActiveShell(event), "Convert '"+file.getName()+"' to .yaml");
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return null;
	}

}
