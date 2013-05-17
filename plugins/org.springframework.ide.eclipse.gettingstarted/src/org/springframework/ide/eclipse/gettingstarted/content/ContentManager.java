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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.springframework.ide.eclipse.gettingstarted.util.DownloadManager;

/**
 * An instance of the class manages lists of content of different types.
 * The idea is to create a subclass that provides all the concrete
 * details on how different types of content are discovered, downloaded
 * and cached on the local file system.
 * <p>
 * But the infrastructure for managing/downloading the content is shared.
 * 
 * @author Kris De Volder
 */
public abstract class ContentManager {
	
	private Map<Class<?>, TypedContentManager<?>>  byType = new HashMap<Class<?>, TypedContentManager<?>>();
	
	public <T> void register(Class<T> type, ContentProvider<T> provider) {
		Assert.isLegal(!byType.containsKey(type), "A content provider for "+type+" is already registered");
		DownloadManager downloader = downloadManagerFor(type);
		byType.put(type, new TypedContentManager<T>(downloader, provider));
	}
	
	public abstract DownloadManager downloadManagerFor(Class<?> contentType);
	
	/**
	 * Fetch the content of a given type. May return null but only if no content provider has been 
	 * registered for the type.
	 */
	@SuppressWarnings("unchecked")
	public <T> T[] get(Class<T> type) {
		TypedContentManager<T> man = (TypedContentManager<T>) byType.get(type);
		if (man!=null) {
			return man.getAll();
		}
		return null; 
	}

}
