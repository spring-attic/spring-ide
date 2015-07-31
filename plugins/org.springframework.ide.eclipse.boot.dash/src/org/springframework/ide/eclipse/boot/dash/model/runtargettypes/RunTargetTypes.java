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
package org.springframework.ide.eclipse.boot.dash.model.runtargettypes;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryRunTargetType;
import org.springframework.ide.eclipse.boot.dash.lattice.LatticeRunTargetType;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.TargetProperties;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSet;

public class RunTargetTypes {

	public static final RunTargetType LOCAL = new AbstractRunTargetType("Local") {

		@Override
		public boolean canInstantiate() {
			return false;
		}

		public String toString() {
			return "RunTargetType(LOCAL)";
		}

		@Override
		public void openTargetCreationUi(LiveSet<RunTarget> targets) {
			throw new UnsupportedOperationException(
					this + " is a Singleton, it is not possible to create additional targets of this type.");
		}

		@Override
		public RunTarget createRunTarget(TargetProperties properties) {
			return null;
		}

		@Override
		public ImageDescriptor getIcon() {
			return BootDashActivator.getImageDescriptor("icons/boot-icon.png");
		};
	};

	public static final RunTargetType CLOUDFOUNDRY = new CloudFoundryRunTargetType();

	public static final RunTargetType LATTICE = new LatticeRunTargetType();

	public static final RunTargetType[] ALL = {
			LOCAL,
			CLOUDFOUNDRY,
			LATTICE
	};

}
