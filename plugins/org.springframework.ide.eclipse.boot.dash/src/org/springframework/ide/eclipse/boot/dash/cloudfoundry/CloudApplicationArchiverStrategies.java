/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.CloudApplicationArchiverStrategy;

/**
 * Some utilities for creating {@link CloudApplicationArchiverStrategy} instances.
 *
 * @author Kris De Volder
 */
public class CloudApplicationArchiverStrategies {

	public static CloudApplicationArchiverStrategy justReturn(final ICloudApplicationArchiver legacyArchiver) {
		return new CloudApplicationArchiverStrategy() {
			@Override
			public ICloudApplicationArchiver getArchiver() {
				return legacyArchiver;
			}
		};
	}

}
