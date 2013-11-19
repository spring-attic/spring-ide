/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.ui;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.ChooseDependencyModel;
import org.springframework.ide.eclipse.boot.core.MavenCoordinates;
import org.springsource.ide.eclipse.commons.livexp.ui.ChooseOneSection;
import org.springsource.ide.eclipse.commons.livexp.ui.CommentSection;
import org.springsource.ide.eclipse.commons.livexp.ui.DialogWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.StyledCommentSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;

public class ChooseDependencyDialog extends DialogWithSections {
	
	private ChooseDependencyModel model;
	private String message;

	public ChooseDependencyDialog(String title, String message, ChooseDependencyModel model, Shell shell) {
		super(title, model, shell);
		this.model = model;
		this.message = message;
	}

	@Override
	protected List<WizardPageSection> createSections() throws CoreException {
		return Arrays.asList(
				new StyledCommentSection(this, message), 
				//new CommentSection(this, message),
				new ChooseOneSection<MavenCoordinates>(this, /*label*/null,
						model.availableChoices,
						model.selected,
						model.validator
				)
		);
	}
	
	/**
	 * Open the dialog and block until user has either made a choice or canceled.
	 */
	public static MavenCoordinates openOn(String title, String message,
			Collection<MavenCoordinates> availableChoices, 
			Shell shell
	) throws CoreException, OperationCanceledException {
		ChooseDependencyModel model = new ChooseDependencyModel(availableChoices);
		ChooseDependencyDialog dialog = new ChooseDependencyDialog(title, message,
				model, shell
		);
		dialog.setBlockOnOpen(true);
		dialog.open();
		return model.getResult();
	}

	public static MavenCoordinates openOn(String title, String message,
			Collection<MavenCoordinates> availableChoices) {
		try {
			//TODO: Getting the shell in the manner below is iffy... and we might not even be
			// in the UI thread here!
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			return openOn(title, message, availableChoices, shell);
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return null;
	}
}
