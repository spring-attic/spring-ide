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
package org.springframework.ide.eclipse.gettingstarted.content;

import org.springframework.ide.eclipse.gettingstarted.util.DownloadManager;

/**
 * An instance of this interface implements some method of discovering
 * content of a particular type. 
 */
public interface ContentProvider<T> {

	T[] fetch(DownloadManager downloader);

}
