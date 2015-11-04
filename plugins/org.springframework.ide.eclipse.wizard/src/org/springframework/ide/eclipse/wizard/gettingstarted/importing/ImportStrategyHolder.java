/*******************************************************************************
 * Copyright (c) 2013, 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.gettingstarted.importing;

import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springframework.ide.eclipse.wizard.gettingstarted.content.BuildType;

/**
 * This is wrapper around a {@link ImportStrategyFactory}. It is repsonsible for creating
 * a single instance from the factory, and providing a fallback for when calling the
 * factory fails.
 *
 * @author Kris De Volder
 */
public class ImportStrategyHolder {

	private BuildType buildType;
	private ImportStrategyFactory factory;
	private String notInstalledMessage; //Message tailored to the particular tooling that is needed for an
	private String name; //Short name that can be used to identify strategy to the user (this is useful when more than one
						// strategy is available for a single build-type).

	private ImportStrategy instance = null;

	public ImportStrategyHolder(BuildType buildType, ImportStrategyFactory factory, String notInstalledMessage, String name) {
		this.buildType = buildType;
		this.factory = factory;
		this.notInstalledMessage = notInstalledMessage;
		this.name = name;
	}

	public String displayName() {
		if (buildType.getImportStrategies().size()>1) {
			//Name needs disambiguation
			return buildType.displayName() + " ("+name+")";
		} else {
			//Just the buildtype name is enough
			return buildType.displayName();
		}
	}

	public ImportStrategy get() {
		if (instance == null) {
			try {
				this.instance = factory.create(buildType, notInstalledMessage, displayName());
			} catch (Throwable e) {
				//THe most likely cause of this error is that optional dependencies needed to support
				// this import strategy are not installed.
				WizardPlugin.log(e);
				this.instance = new NullImportStrategy(displayName(), notInstalledMessage);
			}
		}
		return instance;
	}

}
