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
package org.springframework.ide.eclipse.gettingstarted.importing;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.springframework.ide.eclipse.gettingstarted.content.CodeSet;
import org.springframework.ide.eclipse.gettingstarted.guides.GettingStartedGuide;

/**
 * @author Kris De Volder
 */
public class ImportUtils {

	public static ImportConfiguration importConfig(final IPath location, final String projectName, final CodeSet codeset) {
		ImportConfiguration conf = new ImportConfiguration() {
	
			@Override
			public String getLocation() {
				return location.toString();
			}
	
			@Override
			public String getProjectName() {
				return projectName;
			}
	
			@Override
			public CodeSet getCodeSet() {
				return codeset;
			}
		};
		return conf;
	}

	/**
	 * Convenience method to create a import configuration that imports a particular codeset for a given guide into the 
	 * default location in the workspace.
	 */
	public static ImportConfiguration importConfig(GettingStartedGuide guide, CodeSet codeset) {
		String projectName = guide.getName()+"-"+codeset.getName();
		return importConfig(
				/*location*/
				Platform.getLocation().append(projectName),
				/*name*/
				projectName,
				/*codeset*/
				codeset
		);
	}

}
