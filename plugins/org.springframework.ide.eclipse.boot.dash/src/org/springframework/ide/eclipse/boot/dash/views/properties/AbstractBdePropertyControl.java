/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.properties;

import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.views.BootDashLabels;

/**
 * Abstract common implementation for the Boot Dash Element feature control for
 * the properties view section
 *
 * @author Alex Boyko
 * @author Kris De Volder
 */
public abstract class AbstractBdePropertyControl implements BootDashElementPropertyControl {

	private BootDashElement bde = null;
	private BootDashLabels bdeLabels = new BootDashLabels();

	public void setInput(BootDashElement bde) {
		if (bde != this.bde) {
			this.bde = bde;
		}
	}

	final protected BootDashElement getBootDashElement() {
		return bde;
	}

	final protected BootDashLabels getLabels() {
		return bdeLabels;
	}

	@Override
	public void dispose() {
		bdeLabels.dispose();
	}

}
