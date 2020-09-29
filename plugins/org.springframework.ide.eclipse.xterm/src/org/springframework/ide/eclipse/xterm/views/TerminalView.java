/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.xterm.views;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.RGBA;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.themes.ITheme;
import org.springframework.ide.eclipse.xterm.XtermPlugin;

public class TerminalView extends ViewPart {
	
	private static final String DEFAULT_TERMINAL_ID = "default";
	private static String ERROR_DIALOG_TITLE = "Error Opening Xterm";
	private static String ERROR_DIALOG_MESSAGE = "Failed to determine if Xterm service is running.\n";

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "org.springframework.ide.eclipse.xterm.views.TerminalView";

	@Inject IWorkbench workbench;
	
	private Action refreshAction;

	private Browser browser;
	
	private String terminalId = DEFAULT_TERMINAL_ID;
	
	private final IPropertyChangeListener PROPERTY_LISTENER = new IPropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent event) {
			switch (event.getProperty()) {
			case XtermPlugin.BG_COLOR:
			case XtermPlugin.FG_COLOR:
			case XtermPlugin.SELECTION_COLOR:
			case XtermPlugin.CURSOR_COLOR:
			case XtermPlugin.CURSOR_ACCENT_COLOR:
			case XtermPlugin.FONT:
				refresh();
				break;
			default:
			}
		}

	};

	@Override
	public void createPartControl(Composite parent) {
		PlatformUI.getWorkbench().getThemeManager().addPropertyChangeListener(PROPERTY_LISTENER);
		browser = new Browser(parent, SWT.CHROMIUM);
		makeActions();
		contributeToActionBars();
		// Secondary id is present only for non-default Terminal views which are initialized with #startTerminal(...) call
		if (getViewSite().getSecondaryId() == null) {
			navigateToTerminal(terminalId, null, ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
		}
	}
	
	public CompletableFuture<Void> refresh() {
		return navigateToTerminal(terminalId, null, ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
	}
	
	private CompletableFuture<Void> navigateToTerminal(final String terminalId, final String cmd, final String cwd) {
		return CompletableFuture.runAsync(() -> {
			try {
				String serviceUrl = XtermPlugin.getDefault().xtermUrl(10_000).get();
				if (Display.getCurrent() != null) {
					if (browser != null && !browser.isDisposed() && terminalId.equals(TerminalView.this.terminalId)) {
						browser.setUrl(createUrl(serviceUrl, terminalId, cmd, cwd));
					}
				} else {
					Display display = PlatformUI.getWorkbench().getDisplay();
					if (display != null && !display.isDisposed()) {
						display.asyncExec(() -> {
							if (browser != null && !browser.isDisposed() && terminalId.equals(TerminalView.this.terminalId)) {
								String url = createUrl(serviceUrl, terminalId, cmd, cwd);
								browser.setUrl(url);
							}
						});
					}
				}
			} catch (ExecutionException e) {
				// TODO show error page in the browser
				XtermPlugin.log(e);
				if (e.getCause() != null) {
					StringBuilder errorMessage = new StringBuilder(ERROR_DIALOG_MESSAGE);
					errorMessage.append('\n');
					errorMessage.append(e.getCause().getMessage());
					if (Display.getCurrent() != null) {
						MessageDialog.openError(Display.getCurrent().getActiveShell(), ERROR_DIALOG_TITLE, errorMessage.toString());
					} else {
						Display display = PlatformUI.getWorkbench().getDisplay();
						if (display != null && !display.isDisposed()) {
							display.asyncExec(() -> 
								MessageDialog.openError(Display.getCurrent().getActiveShell(), ERROR_DIALOG_TITLE, errorMessage.toString())
							);
						}
					}
				}
			} catch (InterruptedException e) {
				XtermPlugin.log(e);
			}
		});
	}
	
	public CompletableFuture<Void> startTerminal(String terminalId, String cmd, String cwd) {
		this.terminalId = terminalId;
		return navigateToTerminal(terminalId, cmd, cwd);
	}
	
	private String createUrl(String serviceUrl, String terminalId, String cmd, String cwd) {
		SimpleUriBuilder urlBuilder = new SimpleUriBuilder(serviceUrl + "/terminal/" + terminalId);
		
		ITheme theme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
		ColorRegistry colorRegistry = theme.getColorRegistry();
		Font font = theme.getFontRegistry().get(XtermPlugin.FONT);
		
		urlBuilder.addParameter("bg", rgbToUrlParameter(colorRegistry.get(XtermPlugin.BG_COLOR).getRGB()));
		urlBuilder.addParameter("fg", rgbToUrlParameter(colorRegistry.get(XtermPlugin.FG_COLOR).getRGB()));
		RGB selectionColor = colorRegistry.get(XtermPlugin.SELECTION_COLOR).getRGB();
		// add transparency to selection color
		urlBuilder.addParameter("selection", rgbaToUrlParameter(new RGBA(selectionColor.red, selectionColor.green, selectionColor.blue, 51)));
		urlBuilder.addParameter("cursor", rgbToUrlParameter(colorRegistry.get(XtermPlugin.CURSOR_COLOR).getRGB()));
		urlBuilder.addParameter("cursorAccent", rgbToUrlParameter(colorRegistry.get(XtermPlugin.CURSOR_ACCENT_COLOR).getRGB()));

		urlBuilder.addParameter("fontFamily", font.getFontData()[0].getName());
		urlBuilder.addParameter("fontSize", String.valueOf(font.getFontData()[0].getHeight()));
		
		if (cmd != null && !cmd.isEmpty()) {
			urlBuilder.addParameter("cmd", cmd);
		} else {
			urlBuilder.addParameter("cmd",
					XtermPlugin.getDefault().getPreferenceStore().getString(XtermPlugin.PREFS_DEFAULT_SHELL_CMD));
		}
		if (cwd != null && !cwd.isEmpty()) {
			urlBuilder.addParameter("cwd", cwd);
		}
		
		return urlBuilder.toString();
			
	}
	
	private static String rgbToUrlParameter(RGB rgb) {
		StringBuilder sb = new StringBuilder("rgb(");
		sb.append(rgb.red);
		sb.append(",");
		sb.append(rgb.green);
		sb.append(",");
		sb.append(rgb.blue);
		sb.append(")");
		return sb.toString();
	}

	private static String rgbaToUrlParameter(RGBA rgba) {
		StringBuilder sb = new StringBuilder("rgba(");
		sb.append(rgba.rgb.red);
		sb.append(",");
		sb.append(rgba.rgb.green);
		sb.append(",");
		sb.append(rgba.rgb.blue);
		sb.append(",");
		sb.append(rgba.alpha / 255.0);
		sb.append(")");
		return sb.toString();
	}
	
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(refreshAction);
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(refreshAction);
	}

	private void makeActions() {
		refreshAction = new Action() {
			public void run() {
				refresh();
			}
		};
		refreshAction.setText("Refresh");
		refreshAction.setToolTipText("Refresh Terminal");
		refreshAction.setImageDescriptor(XtermPlugin.imageDescriptorFromPlugin(XtermPlugin.getDefault().getBundle().getSymbolicName(), "icons/refresh.png"));		
	}

	@Override
	public void setFocus() {
		browser.setFocus();
	}

	@Override
	public void dispose() {
		PlatformUI.getWorkbench().getThemeManager().removePropertyChangeListener(PROPERTY_LISTENER);
	}
	
}
