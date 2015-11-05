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
import org.springframework.ide.eclipse.wizard.gettingstarted.content.BuildType;

/**
 * Import stratgety used in place of a Strategy that could not be instantiated, presumably because
 * the required Eclipse plugins are not installed.
 *
 * @author Kris De Volder
 */
public class NullImportStrategy extends ImportStrategy {

	private String name;
	private String notInstalledMessage;
	private BuildType buildType;

	public NullImportStrategy(BuildType bt, String name, String notInstalledMessage) {
		this.buildType = bt;
		this.name = name;
		this.notInstalledMessage = notInstalledMessage;
	}

	@Override
	public IRunnableWithProgress createOperation(ImportConfiguration conf) {
		throw new Error(getNotInstalledMessage());
	}

	@Override
	public boolean isSupported() {
		return false;
	}

	@Override
	public String getNotInstalledMessage() {
		return "Can not import using "+displayName()+" because "+notInstalledMessage;
	}

	private String displayName() {
		if (buildType.getImportStrategies().size()>1) {
			return buildType.displayName() + " ("+name+")";
		}
		return buildType.displayName();
	}

}
