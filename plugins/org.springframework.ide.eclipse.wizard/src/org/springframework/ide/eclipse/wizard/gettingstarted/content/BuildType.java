/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.gettingstarted.content;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springframework.ide.eclipse.wizard.gettingstarted.importing.ImportConfiguration;
import org.springframework.ide.eclipse.wizard.gettingstarted.importing.ImportStrategy;
import org.springframework.ide.eclipse.wizard.gettingstarted.importing.NullImportStrategy;

public enum BuildType {
	MAVEN("pom.xml",
	      "org.springframework.ide.eclipse.wizard.gettingstarted.importing.MavenStrategy",
	      "Can not use Maven: M2E (Eclipse Maven Tooling) is not installed"
	),
	GRADLE("build.gradle",
			"org.springframework.ide.eclipse.wizard.gettingstarted.importing.GradleStrategy",
			"Can not use Gradle: STS Gradle Tooling is not installed. You can install it from the STS Dashboard."
	),
//	GROOVY_CLI("app.groovy",
//			"org.springframework.ide.eclipse.wizard.gettingstarted.importing.GeneralProjectStrategy",
//			"NA", //This message doesn't matter because
//															  // project imports as 'resources' so no
//															  // particular tooling is needed.
//			"Spring CLI"
//	),
	GENERAL(null,
			"org.springframework.ide.eclipse.wizard.gettingstarted.importing.GeneralProjectStrategy",
			"NA",
			"General"
	);

//	MAVEN("pom.xml", new NullImportStrategy("Maven"));
//	ECLIPSE(".project", ImportStrategy.ECLIPSE);

	/**
	 * Location of the 'build script' relative to codeset (project) root.
	 * This also serves as a way to recognize if a BuildType is
	 * supported by a CodeSet. I.e. if the buildScript file
	 * exists in the code set then it is assumed the codeset can
	 * be imported with the corresponding ImportStrategy.
	 */
	private Path buildScriptPath;
	private List<ImportStrategyFactory> strategies = new ArrayList<ImportStrategyFactory>();
	private String displayName;

	private BuildType(String buildScriptPath, String importStrategyClass, String notInstalledMessage) {
		if (buildScriptPath!=null) {
			this.buildScriptPath = new Path(buildScriptPath);
		}
		this.strategies.add(new ImportStrategyFactory(this,
				importStrategyClass, notInstalledMessage, "Default"
		));
	}

	private BuildType(String buildScriptPath, String importStrategyClass, String notInstalledMessage, String displayName) {
		this(buildScriptPath, importStrategyClass, notInstalledMessage);
		this.displayName = displayName;
	}


	public IPath getBuildScript() {
		return buildScriptPath;
	}

	public List<ImportStrategy> getImportStrategies() {
		ArrayList<ImportStrategy> instances = new ArrayList<ImportStrategy>(strategies.size());
		for (ImportStrategyFactory f : strategies) {
			instances.add(f.get());
		}
		return Collections.unmodifiableList(instances);
	}

	public String displayName() {
		if (displayName==null) {
			String name = name();
			displayName = name.substring(0,1) + name.substring(1).toLowerCase();
		}
		return displayName;
	}

	/**
	 * The option that is preferred by the UI as the initial selection.
	 */
	public static final BuildType DEFAULT = MAVEN;

	/**
	 * This will return the first import strategy. This method is deprecated, it is provided only
	 * to avoid completely breaking code that assumes only a single strategy per build-type is available.
	 * <p>
	 * Code using this method will work, but will only be able to use one of the import strategies.
	 * It should be rewritten to support multiple strategies (i.e. use getImportStrategies() method.
	 */
	@Deprecated
	public ImportStrategy getImportStrategy() {
		return getImportStrategies().get(0);
	}

}
