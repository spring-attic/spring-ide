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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.ChooseDependencyModel;
import org.springframework.ide.eclipse.boot.core.MavenCoordinates;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springsource.ide.eclipse.commons.completions.externaltype.ExternalType;
import org.springsource.ide.eclipse.commons.livexp.ui.CheckboxSection;
import org.springsource.ide.eclipse.commons.livexp.ui.ChooseOneSection;
import org.springsource.ide.eclipse.commons.livexp.ui.CommentSection;
import org.springsource.ide.eclipse.commons.livexp.ui.DescriptionSection;
import org.springsource.ide.eclipse.commons.livexp.ui.DialogWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.HLineSection;
import org.springsource.ide.eclipse.commons.livexp.ui.StyledCommentSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;

public class ChooseDependencyDialog extends DialogWithSections {

	private ChooseDependencyModel model;

	public ChooseDependencyDialog(ChooseDependencyModel model, Shell shell) {
		super(model.getTitle(), model, shell);
		this.model = model;
	}

	@Override
	protected List<WizardPageSection> createSections() throws CoreException {
		List<WizardPageSection> sections = new ArrayList<>();
		sections.add(new StyledCommentSection(this, "Type <b>"+model.getTypeName()+"</b> is not yet on the project classpath."));
		if (model.isShowChoices()) {
			sections.add(new HLineSection(this));
			sections.add(new ChooseOneSection<MavenCoordinates>(this, "Choose a depenency to add it:",
					model.availableChoices,
					model.selected,
					model.validator
			).vertical());
		}
		sections.add(new HLineSection(this));
		sections.add(new CommentSection(this, "Add this to your'"+model.getDependencyFileName()+"'?"));
		sections.add(new DescriptionSection(this, model.previewText) {
			@Override
			protected void configureTextWidget(Text text) {
				text.setFont(JFaceResources.getFont(PreferenceConstants.EDITOR_TEXT_FONT));
			}
		});
		sections.add(new HLineSection(this));
		sections.add(new CheckboxSection(this, model.disableJarTypeAssist));
		return sections;
	}

//	/**
//	 * Open the dialog and block until user has either made a choice or canceled.
//	 */
//	public static MavenCoordinates openOn(String title, String message,
//			Collection<MavenCoordinates> availableChoices,
//			Shell shell
//	) throws CoreException, OperationCanceledException {
//		ChooseDependencyModel model = new ChooseDependencyModel(availableChoices);
//		ChooseDependencyDialog dialog = new ChooseDependencyDialog(title, message,
//				model, shell
//		);
//		dialog.setBlockOnOpen(true);
//		dialog.open();
//		return model.getResult();
//	}

	public static MavenCoordinates openOn(ChooseDependencyModel model) {
		try {
			//TODO: Getting the shell in the manner below is iffy... and we might not even be
			// in the UI thread here!
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			return openOn(model, shell);
		} catch (Exception e) {
			Log.log(e);
		}
		return null;
	}

	public static MavenCoordinates openOn(ChooseDependencyModel model, Shell shell) {
		ChooseDependencyDialog dialog = new ChooseDependencyDialog(model, shell);
		dialog.setBlockOnOpen(true);
		dialog.open();
		return model.getResult();
	}
}
