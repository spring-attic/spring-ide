package org.springframework.ide.eclipse.xterm.views;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
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
import org.springframework.ide.eclipse.boot.core.SimpleUriBuilder;
import org.springframework.ide.eclipse.xterm.XtermPlugin;

public class TerminalView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "org.springframework.ide.eclipse.xterm.views.TerminalView";

	@Inject IWorkbench workbench;
	
	private Action refreshAction;

	private Browser browser;
	
	private String terminalId = "default";
	
	private final IPropertyChangeListener PROPERTY_LISTENER = new IPropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent event) {
			switch (event.getProperty()) {
			case XtermPlugin.BG_COLOR:
			case XtermPlugin.FG_COLOR:
			case XtermPlugin.SELECTION_COLOR:
			case XtermPlugin.CURSOR_COLOR:
			case XtermPlugin.CURSOR_ACCENT_COLOR:
				refresh();
				break;
			default:
			}
		}

	};

	@Override
	public void createPartControl(Composite parent) {
		PlatformUI.getWorkbench().getThemeManager().addPropertyChangeListener(PROPERTY_LISTENER);
		browser = new Browser(parent, SWT.NONE);
		makeActions();
		contributeToActionBars();
		navigateToTerminal(terminalId, null, ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
	}
	
	public CompletableFuture<Void> refresh() {
		return navigateToTerminal(terminalId, null, ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
	}
	
	private CompletableFuture<Void> navigateToTerminal(final String terminalId, final String cmd, final String cwd) {
		return CompletableFuture.runAsync(() -> {
			try {
				String serviceUrl = XtermPlugin.getDefault().xtermUrl().get(10, TimeUnit.SECONDS);
				if (Display.getCurrent() != null) {
					if (browser != null && !browser.isDisposed()) {
						browser.setUrl(createUrl(serviceUrl, terminalId, cmd, cwd));
					}
				} else {
					Display display = PlatformUI.getWorkbench().getDisplay();
					if (display != null && !display.isDisposed()) {
						display.asyncExec(() -> {
							if (browser != null && !browser.isDisposed()) {
								String url = createUrl(serviceUrl, terminalId, cmd, cwd);
								browser.setUrl(url);
							}
						});
					}
				}
			} catch (Exception e) {
				// TODO show error page in the browser
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
