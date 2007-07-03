/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * @author Christian Dupuis
 * @since 2.0.1
 */
public class BeansUIActivationHistory {

	private static List<String> HISTORY = new CopyOnWriteArrayList<String>();

	public static void clearHistory() {
		HISTORY = new CopyOnWriteArrayList<String>();
	}

	public static void addToHistory(IModelElement modelElement) {
		if (modelElement != null && modelElement.getElementID() != null) {
			HISTORY.add(modelElement.getElementID());
		}
	}
	
	public static Set<IModelElement> getActivationHistory() {
		Set<IModelElement> history = new LinkedHashSet<IModelElement>();
		for (String elementId : HISTORY) {
			IModelElement element = BeansCorePlugin.getModel().getElement(elementId);
			if (element != null) {
				history.add(element);
			}
		}
		return history;
	}

	public static List<IBean> getBeanActivationHistory() {
		List<IBean> history = new ArrayList<IBean>();
		for (String elementId : HISTORY) {
			IModelElement element = BeansCorePlugin.getModel().getElement(elementId);
			if (element instanceof IBean) {
				history.add((IBean) element);
			}
		}
		return history;
	}
}
