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
package org.springframework.ide.eclipse.gettingstarted.tests;

import org.eclipse.core.runtime.IPath;
import org.springframework.ide.eclipse.gettingstarted.content.CodeSet;
import org.springframework.ide.eclipse.gettingstarted.importing.ImportConfiguration;
import org.springsource.ide.eclipse.gradle.core.util.expression.LiveExpression;

/**
 * @author Kris De Volder
 */
public class ImportUtils {

	public static ImportConfiguration importConfig(final IPath location, final String projectName, final CodeSet codeset) {
		ImportConfiguration conf = new ImportConfiguration() {
	
			@Override
			public LiveExpression<String> getLocationField() {
				return LiveExpression.constant(location.toString());
			}
	
			@Override
			public LiveExpression<String> getProjectNameField() {
				return LiveExpression.constant(projectName);
			}
	
			@Override
			public LiveExpression<CodeSet> getCodeSetField() {
				return LiveExpression.constant(codeset);
			}
		};
		return conf;
	}

}
