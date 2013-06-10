/*******************************************************************************
 *  Copyright (c) 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.gettingstarted.content;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.springframework.ide.eclipse.gettingstarted.GettingStartedActivator;
import org.springframework.ide.eclipse.gettingstarted.importing.ImportStrategy;
import org.springframework.ide.eclipse.gettingstarted.importing.NullImportStrategy;

public enum BuildType {
	MAVEN("pom.xml", 
	      "org.springframework.ide.eclipse.gettingstarted.importing.MavenStrategy",
	      "Can not use Maven: M2E (Eclipse Maven Tooling) is not installed"
	),
	GRADLE("build.gradle", 
			"org.springframework.ide.eclipse.gettingstarted.importing.GradleStrategy", 
			"Can not use Gradle: STS Gradle Tooling is not installed"
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
	private String klass; //Class name for import strategy. May not be able to classload if requisite tooling isn't installed.
	private String notInstalledMessage; //Message tailored to the particular tooling that is needed for an 
	private ImportStrategy importStrategy;

	private BuildType(String buildScriptPath, String importStrategyClass, String notInstalledMessage) {
		this.buildScriptPath = new Path(buildScriptPath);
		this.klass = importStrategyClass;
		this.notInstalledMessage = notInstalledMessage;
	}

	public IPath getBuildScript() {
		return buildScriptPath;
	}
	
	public ImportStrategy getImportStrategy() {
		if (this.importStrategy==null) {
			try {
				this.importStrategy = (ImportStrategy) Class.forName(klass).newInstance();
			} catch (Throwable e) {
				//THe most likely cause of this error is that optional dependencies needed to support
				// this import strategy are not installed.
				GettingStartedActivator.log(e);
				this.importStrategy = new NullImportStrategy(name(), notInstalledMessage);
			}
		}
		return this.importStrategy;
	}
	
	public String displayName() {
		String name = name();
		return name.substring(0,1) + name.substring(1).toLowerCase();
	}
	
	public static final BuildType DEFAULT = MAVEN;
	
	public String getNotInstalledMessage() {
		return notInstalledMessage;
	}
}
