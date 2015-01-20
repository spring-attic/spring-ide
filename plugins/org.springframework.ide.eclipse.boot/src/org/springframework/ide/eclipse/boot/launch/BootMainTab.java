/*******************************************************************************
 * Copyright (c) 2005, 2012,2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Kris De Volder - Copied and repurposed for Spring Boot
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.launch;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;

/**
 * A launch configuration tab that displays and edits project and
 * main type name launch configuration attributes.
 * <p>
 * This class may be instantiated.
 * </p>
 * @since 3.2
 * @noextend This class is not intended to be subclassed by clients.
 */
public class BootMainTab extends LaunchConfigurationTabWithSections implements IPageWithSections {

	@Override
	public String getName() {
		return "Spring Boot";
	}

	@Override
	public Image getImage() {
		return super.getImage();
	}
	
	@Override
	protected List<IPageSection> createSections() {
		return Arrays.asList(new IPageSection[] {
//				new ExistingTabAsSection(new JavaMainTab()),
				new PropertiesTableSection(this)
		});
	}
	
}
