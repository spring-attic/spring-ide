package org.springframework.ide.eclipse.gettingstarted.importing;

import org.springframework.ide.eclipse.gettingstarted.content.CodeSet;

/**
 * An object that contains the required data to import getting started contents
 * into the workspace using a particular build system.
 */
public interface ImportConfiguration {

	/**
	 * Location of where the project root should be placed or created in the file system.
	 */
	public String getLocation();

	/**
	 * The name of the project in the workspace.
	 */
	public String getProjectName();

	/**
	 * The data used to populate the project.
	 */
	public CodeSet getCodeSet();
	

}
