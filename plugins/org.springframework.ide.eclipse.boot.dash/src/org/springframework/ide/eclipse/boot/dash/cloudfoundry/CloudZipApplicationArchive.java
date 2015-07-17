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

import java.util.zip.ZipFile;

import org.cloudfoundry.client.lib.archive.ZipApplicationArchive;

public class CloudZipApplicationArchive extends ZipApplicationArchive {

	protected final ZipFile zipFile;

	public CloudZipApplicationArchive(ZipFile zipFile) {
		super(zipFile);
		this.zipFile = zipFile;
	}

	public void close() throws Exception {
		if (zipFile != null) {
			zipFile.close();
		}
	}
}
