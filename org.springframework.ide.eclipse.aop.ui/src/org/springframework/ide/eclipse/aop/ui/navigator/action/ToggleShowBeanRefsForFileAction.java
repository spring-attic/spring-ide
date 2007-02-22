/*
 * Copyright 2002-2007 the original author or authors.
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
package org.springframework.ide.eclipse.aop.ui.navigator.action;

import org.eclipse.jface.action.Action;
import org.springframework.ide.eclipse.aop.ui.AopReferenceModelImages;
import org.springframework.ide.eclipse.aop.ui.navigator.AopReferenceModelNavigator;

/**
 * This action toggles whether the cross reference view displays the cross
 * references for the current selection in the active editor, or for the file
 * which is shown in the active editor.
 */
public class ToggleShowBeanRefsForFileAction extends Action {

	private AopReferenceModelNavigator aopNavigator;

	/**
	 * Constructs a new action.
	 */
	public ToggleShowBeanRefsForFileAction(
			AopReferenceModelNavigator aopNavigator) {
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
	public void run() {
		aopNavigator.setShowBeansRefsForFileEnabled(isChecked());
	}
}
