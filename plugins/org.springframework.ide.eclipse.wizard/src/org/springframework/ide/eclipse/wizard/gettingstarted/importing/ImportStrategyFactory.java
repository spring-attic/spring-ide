/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.gettingstarted.importing;

import org.springframework.ide.eclipse.wizard.gettingstarted.content.BuildType;

/**
 * @author Kris De Volder
 */
public interface ImportStrategyFactory {
	ImportStrategy create(BuildType buildType,String name,  String notInstalledMessage) throws Exception;
}
