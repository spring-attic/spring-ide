/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   VMware Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.service.importer.support.internal.controller;

import org.eclipse.gemini.blueprint.service.importer.support.internal.dependency.ImporterStateListener;
import org.springframework.util.Assert;

/**
 * @author Costin Leau
 * 
 */
public class ImporterController implements ImporterInternalActions {

	private ImporterInternalActions executor;


	public ImporterController(ImporterInternalActions executor) {
		Assert.notNull(executor);
		this.executor = executor;
	}

	public void addStateListener(ImporterStateListener stateListener) {
		executor.addStateListener(stateListener);
	}

	public void removeStateListener(ImporterStateListener stateListener) {
		executor.removeStateListener(stateListener);
	}

	public boolean isSatisfied() {
		return executor.isSatisfied();
	}
}
