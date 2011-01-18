/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.graph.actions;

import org.eclipse.ui.IEditorPart;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;

/**
 * @author Christian Dupuis
 */
public class OpenBeansConfigAction extends AbstractBeansEditorPartAction {

	/**
	 * 
	 */
	public static final String OPEN_FILE_REQUEST = "Open_beans_config";

	/**
	 * 
	 */
	public static final String OPEN_FILE = "Open_beans_config";

	/**
	 * 
	 * 
	 * @param editor
	 */
	public OpenBeansConfigAction(IEditorPart editor) {
		super(editor);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#init()
	 */
	protected void init() {
		setId(OpenBeansConfigAction.OPEN_FILE);
		setText("Open Beans Config");
		setToolTipText("Open element in beans config");
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		IBean bean = getBean();
		if (bean != null) {
			BeansUIUtils.openInEditor(bean);
		}
	}
}
