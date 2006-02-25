/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.ide.eclipse.beans.ui.search.actions;

import org.eclipse.jface.action.Action;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.model.BeanNode;
import org.springframework.ide.eclipse.beans.ui.model.INode;
import org.springframework.ide.eclipse.beans.ui.views.BeansView;

/**
 * @author Torsten Juergeleit
 */
public class FindBeansForClassAction extends Action {

	private static final String PREFIX = "SearchBeanReferencesAction.";

    private BeansView view;

	public FindBeansForClassAction(BeansView view) {
		super(BeansUIPlugin.getResourceString(PREFIX + "label"));
		setToolTipText(BeansUIPlugin.getResourceString(PREFIX + "tooltip"));
		this.view = view;
    }

	public boolean isEnabled() {
		INode node = view.getSelectedNode();
		return (node instanceof BeanNode);
	}

	public void run() {
		INode node = view.getSelectedNode();
		if (node instanceof BeanNode) {
			IBean bean = ((BeanNode) node).getBean();
			// TODO implement search view
			BeansModelUtils.getBeanReferences(bean.getElementName(), bean.getElementParent());
		}
	}
}
