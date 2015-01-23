/*******************************************************************************
 * Copyright (c) 2015 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.launch;

import static org.springframework.ide.eclipse.boot.ui.BootUIImages.BOOT_ICON;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.boot.ui.BootUIImages;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.SelectionModel;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;

/**
 * @author Kris De Volder
 */
public class BootMainTab extends LaunchConfigurationTabWithSections implements IPageWithSections {

	@Override
	public String getName() {
		return "Spring Boot";
	}

	@Override
	public Image getImage() {
		return BootUIImages.getImage(BOOT_ICON);
	}

	private static SelectionModel<IProject> createProjectSelectionModel() {
		LiveVariable<IProject> s = new LiveVariable<IProject>();
		ExistingBootProjectSelectionValidator v = new ExistingBootProjectSelectionValidator(s);
		return new SelectionModel<IProject>(s,v);
	}

	@Override
	protected List<IPageSection> createSections() {
		MainTypeSelectionModel model = new MainTypeSelectionModel();
		return Arrays.asList(new IPageSection[] {
				new SelectProjectLaunchTabSection(this, model.project),
				new MainTypeLaunchTabSection(this, model),
				//new JavaMainSection(this),
				new EnableDebugSection(this),
				new PropertiesTableSection(this)
		});
	}

}
