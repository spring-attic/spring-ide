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
 * @author Christian Dupuis
 * @author Torsten Juergeleit
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
