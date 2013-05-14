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
import org.springframework.ide.gettingstarted.content.importing.GradleStrategy;
import org.springframework.ide.gettingstarted.content.importing.ImportStrategy;

public enum BuildType {
	GRADLE("build.gradle", ImportStrategy.GRADLE),
	MAVEN("pom.xml", ImportStrategy.MAVEN);
//	ECLIPSE(".project", ImportStrategy.ECLIPSE);

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
