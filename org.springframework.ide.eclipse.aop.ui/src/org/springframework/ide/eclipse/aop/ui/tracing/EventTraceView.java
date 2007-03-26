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
package org.springframework.ide.eclipse.aop.ui.tracing;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.springframework.ide.eclipse.aop.core.logging.AopLog;
import org.springframework.ide.eclipse.aop.ui.Activator;

/**
 * Displays and configures debug tracing for AJDT
 */
public class EventTraceView extends ViewPart implements
		EventTrace.EventListener {

	StyledText text;

	private ClearEventTraceAction clearEventTraceAction;

	private Font font = JFaceResources.getFont(JFaceResources.TEXT_FONT);

	/**
	 * Constructor for AJDTEventTraceView.
	 */
	public EventTraceView() {
		super();
	}

	@Override
	public void dispose() {
		EventTrace.removeListener(this);
		DebugTracing.DEBUG = false;
	}

	/**
	 * @see IWorkbenchPart#createPartControl(Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		text = new StyledText(parent, SWT.MULTI | SWT.READ_ONLY | SWT.VERTICAL
				| SWT.HORIZONTAL);
		startup();
		EventTrace.addListener(this);

		makeActions();
		contributeToActionBars();

		// Add an empty ISelectionProvider so that this view works with dynamic
		// help (bug 104331)
		getSite().setSelectionProvider(new ISelectionProvider() {
			public void addSelectionChangedListener(
					ISelectionChangedListener listener) {
			}

			public ISelection getSelection() {
				return null;
			}

			public void removeSelectionChangedListener(
					ISelectionChangedListener listener) {
			}

			public void setSelection(ISelection selection) {
			}
		});
	}

	/**
	 * record version information & content of the preference store
	 */
	private void startup() {
		DebugTracing.DEBUG = true;
		aopEvent(DebugTracing.startupInfo(), AopLog.DEFAULT, new Date());
	}

	/**
	 * @see IWorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		text.setFocus();
	}

	public void aopEvent(String msg, final int category, Date time) {
		final String txt = DateFormat.getTimeInstance().format(time) + " "
				+ msg + "\n";
		Activator.getDefault().getWorkbench().getDisplay().asyncExec(
				new Runnable() {
					public void run() {
						appendEventText(txt, category);
					}
				});
	}

	private void appendEventText(String msg, int category) {
		IViewSite site = getViewSite();
		if (site == null) {
			return;
		}
		Shell shell = site.getShell();
		if (shell == null) {
			return;
		}
		Display display = shell.getDisplay();
		if (display == null) {
			return;
		}

		StyleRange styleRange = new StyleRange();
		styleRange.font = font;
		styleRange.start = text.getText().length();
		styleRange.length = msg.length();
		if (category == AopLog.BUILDER) {
			styleRange.foreground = display.getSystemColor(SWT.COLOR_DARK_BLUE);
		}
		else if (category == AopLog.BUILDER_CLASSPATH) {
			styleRange.foreground = display.getSystemColor(SWT.COLOR_DARK_RED);
		}
		else if ((category == AopLog.BUILDER)
				|| (category == AopLog.BUILDER_PROGRESS)
				|| (category == AopLog.BUILDER_MESSAGES)) {
			styleRange.foreground = display
					.getSystemColor(SWT.COLOR_DARK_GREEN);
		}
		else {
			styleRange.foreground = display.getSystemColor(SWT.COLOR_BLACK);
		}

		text.append(msg);
		text.setStyleRange(styleRange);
		text.setTopIndex(text.getLineCount() - 1);
	}

	private void makeActions() {
		clearEventTraceAction = new ClearEventTraceAction(text);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(clearEventTraceAction);
	}
}
