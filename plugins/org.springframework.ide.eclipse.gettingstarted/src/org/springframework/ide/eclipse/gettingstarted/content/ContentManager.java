/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.gettingstarted.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	
	private Map<Class<?>, TypedContentManager<?>>  byClass = new HashMap<Class<?>, TypedContentManager<?>>();
	private List<ContentType<?>> types = new ArrayList<ContentType<?>>();
	
	public <T extends GSContent> void register(Class<T> klass, String description, ContentProvider<T> provider) {
		Assert.isLegal(!byClass.containsKey(klass), "A content provider for "+klass+" is already registered");
		
		ContentType<T> ctype = new ContentType<T>(klass, description);
		types.add(ctype);
		DownloadManager downloader = downloadManagerFor(klass);
		byClass.put(klass, new TypedContentManager<T>(downloader, provider));
	}
	
	public abstract DownloadManager downloadManagerFor(Class<?> contentType);
	
	/**
	 * Fetch the content of a given type. May return null but only if no content provider has been 
	 * registered for the type.
	 */
	@SuppressWarnings("unchecked")
	public <T> T[] get(Class<T> type) {
		TypedContentManager<T> man = (TypedContentManager<T>) byClass.get(type);
		if (man!=null) {
			return man.getAll();
		}
		return null; 
	}

	public ContentType<?>[] getTypes() {
		return types.toArray(new ContentType<?>[types.size()]);
	}

	public Object[] get(ContentType<?> ct) {
		if (ct!=null) {
			return get(ct.getKlass());
		}
		return null;
	}

}
