package org.springframework.ide.eclipse.gettingstarted.importing;

import org.springframework.ide.eclipse.gettingstarted.content.CodeSet;
import org.springsource.ide.eclipse.gradle.core.util.expression.LiveExpression;

/**
 * An object that contains the required data to import getting started contents
 * into the workspace using a particular build system.
 * <p>
 * The values are represented as LiveExpression. This makes it relatively easy
 * to connect them to a 'live' UI or a mock UI.
 */
public interface ImportConfiguration {

	/**
	 * Location of where the project root should be placed or created in the file system.
	 */
	public LiveExpression<String> getLocationField();

	/**
	 * The name of the project in the workspace.
	 */
	public LiveExpression<String> getProjectNameField();

	/**
	 * The data used to populate the project.
	 */
	public LiveExpression<CodeSet> getCodeSetField();
	

}
