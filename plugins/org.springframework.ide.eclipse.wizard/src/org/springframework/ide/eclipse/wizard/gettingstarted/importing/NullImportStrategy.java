/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.gettingstarted.importing;

import org.eclipse.jface.operation.IRunnableWithProgress;

public class NullImportStrategy extends ImportStrategy {

	private String buildType;
	private String notInstalledMessage;

	public NullImportStrategy(String buildType, String notInstalledMessage) {
		this.buildType = buildType;
		this.notInstalledMessage = notInstalledMessage;
	}

	@Override
	public IRunnableWithProgress createOperation(ImportConfiguration conf) {
		throw new Error("Can not import using '"+buildType+"' because "+notInstalledMessage);
	}

	@Override
	public boolean isSupported() {
		return false;
	}
	
}
