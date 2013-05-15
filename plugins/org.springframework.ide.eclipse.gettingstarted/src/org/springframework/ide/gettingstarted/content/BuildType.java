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
package org.springframework.ide.gettingstarted.content;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.springframework.ide.gettingstarted.content.importing.ImportStrategy;
import org.springframework.ide.gettingstarted.content.importing.NullImportStrategy;

public enum BuildType {
	GRADLE("build.gradle", ImportStrategy.GRADLE),
//	MAVEN("pom.xml", ImportStrategy.MAVEN);
	MAVEN("pom.xml", new NullImportStrategy("Maven"));
//	ECLIPSE(".project", ImportStrategy.ECLIPSE);

	/**
	 * Location of the 'build script' relative to codeset (project) root.
	 * This also serves as a way to recognize if a BuildType is
	 * supported by a CodeSet. I.e. if the buildScript file
	 * exists in the code set then it is assumed the codeset can
	 * be imported with the corresponding ImportStrategy.
	 */
	private Path buildScriptPath; 
	private ImportStrategy importStrategy;

	private BuildType(String buildScriptPath, ImportStrategy importStrategy) {
		this.buildScriptPath = new Path(buildScriptPath);
		this.importStrategy = importStrategy;
	}

	public IPath getBuildScript() {
		return buildScriptPath;
	}
	
	public ImportStrategy getImportStrategy() {
		return importStrategy;
	}
}
