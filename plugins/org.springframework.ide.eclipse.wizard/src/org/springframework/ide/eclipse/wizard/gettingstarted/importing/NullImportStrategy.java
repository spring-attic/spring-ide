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

/**
 * Import stratgety used in place of a Strategy that could not be instantiated, presumably because
 * the required Eclipse plugins are not installed.
 *
 * @author Kris De Volder
 */
public class NullImportStrategy extends ImportStrategy {

	private String name;
	private String notInstalledMessage;

	public NullImportStrategy(String name, String notInstalledMessage) {
		this.name = name;
		this.notInstalledMessage = notInstalledMessage;
	}

	@Override
	public IRunnableWithProgress createOperation(ImportConfiguration conf) {
		throw new Error("Can not import using '"+name+"' because "+notInstalledMessage);
	}

	@Override
	public boolean isSupported() {
		return false;
	}

}
