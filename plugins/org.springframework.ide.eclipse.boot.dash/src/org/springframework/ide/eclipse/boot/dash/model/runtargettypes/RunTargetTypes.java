/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model.runtargettypes;

import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import com.google.common.collect.ImmutableList;

import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetTypeFactory;

public class RunTargetTypes {

	private static final String EXTENSION_ID = "org.springframework.ide.eclipse.boot.dash.runtargettype";

	//TODO: Get rid of the 'LOCAL' contstants in this class.
	// The existence of this class and all the references littered around the
	// code pointing to its constants makes it rather hard in some cases to
	// mock things out for unit testing.
	public static final RunTargetType LOCAL = new LocalRunTargetType("Local");

	public static List<RunTargetType> loadFromExtensionPoint(BootDashModelContext context) {
		ImmutableList.Builder<RunTargetType> contributions = ImmutableList.builder();

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] elements = registry.getConfigurationElementsFor(EXTENSION_ID);
		for (IConfigurationElement ce : elements) {
			try {
				RunTargetTypeFactory f = (RunTargetTypeFactory) ce.createExecutableExtension("class");
				contributions.add(f.create(context));
			} catch (Exception e) {
				Log.log(e);
			}
		}
		return contributions.build();
	}
}
