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
package org.springframework.ide.eclipse.aop.ui.navigator.action;

import org.eclipse.jface.action.Action;
import org.springframework.ide.eclipse.aop.ui.navigator.AopReferenceModelNavigator;
import org.springframework.ide.eclipse.aop.ui.navigator.util.AopReferenceModelImages;

/**
 * This action toggles whether the cross reference view displays the cross
 * references for the current selection in the active editor, or for the file
 * which is shown in the active editor.
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
public class ToggleShowBeanRefsForFileAction extends Action {

	private AopReferenceModelNavigator aopNavigator;

	/**
	 * Constructs a new action.
	 */
	public ToggleShowBeanRefsForFileAction(
			AopReferenceModelNavigator aopNavigator) {
		
		// TODO externalize strings
		super("&Show the Beans Cross References for the entire file");
		setDescription("Show the Beans Cross References for the entire file");
		setToolTipText("Show the Beans Cross References for the entire file");
		setImageDescriptor(AopReferenceModelImages.DESC_OBJS_FILE);
		setChecked(aopNavigator.isShowBeansRefsForFileEnabled());
		this.aopNavigator = aopNavigator;
	}

	/**
	 * Runs the action.
	 */
	@Override
	public void run() {
		aopNavigator.setShowBeansRefsForFileEnabled(isChecked());
	}
}
