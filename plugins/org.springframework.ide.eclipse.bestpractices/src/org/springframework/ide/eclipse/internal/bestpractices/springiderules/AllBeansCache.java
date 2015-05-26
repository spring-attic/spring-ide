/*******************************************************************************
 *  Copyright (c) 2015 Pivotal Software, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.internal.bestpractices.springiderules;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * @author Martin Lippert
 */
public class AllBeansCache {

	private final Map<IModelElement, IBean[]> allBeansCache;

	public AllBeansCache() {
		allBeansCache = new ConcurrentHashMap<IModelElement, IBean[]>();
	}

	public IBean[] getAllBeans(IModelElement rootElement) {
		IBean[] allBeans = allBeansCache.get(rootElement);
		if (allBeans == null) {
			Set<IBean> beans = BeansModelUtils.getBeans(rootElement);
			allBeans = beans.toArray(new IBean[beans.size()]);
			allBeansCache.put(rootElement, allBeans);
		}

		return allBeans;
	}

}
