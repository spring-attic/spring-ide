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

package org.springframework.ide.eclipse.aop.ui.tracing;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.internal.console.ConsoleMessages;
import org.eclipse.ui.internal.console.ConsolePluginImages;
import org.eclipse.ui.internal.console.IConsoleHelpContextIds;
import org.eclipse.ui.internal.console.IInternalConsoleConstants;

/**
 * Clears the output of text.
 */
@SuppressWarnings("restriction")
public class ClearEventTraceAction extends Action {

	private StyledText fText;

	/**
	 * Constructs a clear output action.
	 * 
	 * @since 3.1
	 */
	private ClearEventTraceAction() {
		super(ConsoleMessages.ClearOutputAction_title); 
		setToolTipText(ConsoleMessages.ClearOutputAction_toolTipText); 
		setHoverImageDescriptor(ConsolePluginImages
				.getImageDescriptor(IConsoleConstants.IMG_LCL_CLEAR));
		setDisabledImageDescriptor(ConsolePluginImages
				.getImageDescriptor(IInternalConsoleConstants.IMG_DLCL_CLEAR));
		setImageDescriptor(ConsolePluginImages
				.getImageDescriptor(IInternalConsoleConstants.IMG_ELCL_CLEAR));
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IConsoleHelpContextIds.CLEAR_CONSOLE_ACTION);
	}

	/**
	 * Constructs an action to clear the document associated with text.
	 */
	public ClearEventTraceAction(StyledText text) {
		this();
		fText = text;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run() {
		BusyIndicator.showWhile(ConsolePlugin.getStandardDisplay(),
				new Runnable() {
					public void run() {
						fText.setText(""); //$NON-NLS-1$
					}
				});
	}
}
