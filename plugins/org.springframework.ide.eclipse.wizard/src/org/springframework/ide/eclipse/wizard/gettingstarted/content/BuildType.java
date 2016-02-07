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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.springframework.ide.eclipse.wizard.gettingstarted.importing.ImportStrategies;
import org.springframework.ide.eclipse.wizard.gettingstarted.importing.ImportStrategy;
import org.springframework.ide.eclipse.wizard.gettingstarted.importing.ImportStrategyHolder;

public enum BuildType {
	MAVEN("pom.xml",
	      "org.springframework.ide.eclipse.wizard.gettingstarted.importing.MavenStrategy",
	      "Can not use Maven: M2E (Eclipse Maven Tooling) is not installed"
	),
	GRADLE("build.gradle"),
	GENERAL(null,
			"org.springframework.ide.eclipse.wizard.gettingstarted.importing.GeneralProjectStrategy",
			"NA"
	);

	private static final ContributedImportStrategies contributions = new ContributedImportStrategies();

	/**
	 * Location of the 'build script' relative to codeset (project) root.
	 * This also serves as a way to recognize if a BuildType is
	 * supported by a CodeSet. I.e. if the buildScript file
	 * exists in the code set then it is assumed the codeset can
	 * be imported with the corresponding ImportStrategy.
	 */
	private Path buildScriptPath;
	private List<ImportStrategyHolder> strategies = new ArrayList<ImportStrategyHolder>();
	private String displayName;
	private boolean hasDefaultStrategy = false;

	private BuildType(String buildScriptPath) {
		if (buildScriptPath!=null) {
			this.buildScriptPath = new Path(buildScriptPath);
		}
	}

	private BuildType(String buildScriptPath, String importStrategyClass, String notInstalledMessage) {
		this(buildScriptPath);
		hasDefaultStrategy = true;
		addStrategy(new ImportStrategyHolder(this,
				ImportStrategies.forClass(importStrategyClass),
				notInstalledMessage,
				"Default"
		));
	}


	public IPath getBuildScript() {
		return buildScriptPath;
	}

	public List<ImportStrategy> getImportStrategies() {
		contributions.initialize();
		ArrayList<ImportStrategy> instances = new ArrayList<ImportStrategy>(strategies.size());
		for (ImportStrategyHolder f : strategies) {
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

//	/**
//	 * This will return the first import strategy. This method is deprecated, it is provided only
//	 * to avoid completely breaking code that assumes only a single strategy per build-type is available.
//	 * <p>
//	 * Code using this method will work, but will only be able to use one of the import strategies.
//	 * It should be rewritten to support multiple strategies (i.e. use getImportStrategies() method.
//	 */
//	@Deprecated
//	public ImportStrategy getImportStrategy() {
//		return getImportStrategies().get(0);
//	}

	public void addStrategy(ImportStrategyHolder strategyHolder) {
		strategies.add(strategyHolder);
	}

	public ImportStrategy getDefaultStrategy() {
		Assert.isLegal(hasDefaultStrategy);
		return getImportStrategies().get(0);
	}

}
