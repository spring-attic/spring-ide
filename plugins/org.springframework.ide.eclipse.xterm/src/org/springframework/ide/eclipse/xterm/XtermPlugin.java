package org.springframework.ide.eclipse.xterm;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.springframework.ide.eclipse.xterm.views.TerminalView;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

public class XtermPlugin extends AbstractUIPlugin {
	
	private static XtermPlugin plugin;
	
	public static final String BG_COLOR = "org.springframework.ide.eclipse.xterm.background"; 
	public static final String FG_COLOR = "org.springframework.ide.eclipse.xterm.foreground"; 
	public static final String SELECTION_COLOR = "org.springframework.ide.eclipse.xterm.selection"; 
	public static final String CURSOR_COLOR = "org.springframework.ide.eclipse.xterm.cursor"; 
	public static final String CURSOR_ACCENT_COLOR = "org.springframework.ide.eclipse.xterm.cursorAccent";
	public static final String FONT = "org.springframework.ide.eclipse.xterm.font";
	
	public static final String PREFS_DEFAULT_SHELL_CMD = "org.springframework.ide.eclipse.xterm.defaultShellCmd";
	
	private XtermServiceProcessManager serviceManager = new XtermServiceProcessManager();
	
	@Override
	public void start(BundleContext bundle) throws Exception {
		plugin = this;
	}

	@Override
	public void stop(BundleContext bundle) throws Exception {
		serviceManager.stopService();
		plugin = null;
	}
	
	public static XtermPlugin getDefault() {
		return plugin;
	}

	public static void log(String m, Throwable t) {
		getDefault().getLog().error(m, t);
	}
	
	public void openTerminalView(String cmd, String cwd) {
		try {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			String terminalId = UUID.randomUUID().toString();
			TerminalView terminalView = (TerminalView) page.showView(TerminalView.ID, terminalId, IWorkbenchPage.VIEW_ACTIVATE);
			terminalView.startTerminal(terminalId, cmd, cwd);
		} catch (Exception e) {
			XtermPlugin.log(e);
		}
	}

	public static void log(Throwable e) {
		if (ExceptionUtil.isCancelation(e)) {
			//Don't log canceled operations, those aren't real errors.
			return;
		}
		try {
			XtermPlugin.getDefault().getLog().log(ExceptionUtil.status(e));
		} catch (NullPointerException npe) {
			//Can happen if errors are trying to be logged during Eclipse's shutdown
			e.printStackTrace();
		}
	}
	
	public CompletableFuture<String> xtermUrl() {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return  serviceManager.serviceUrl();
			} catch (Throwable t) {
				throw new IllegalStateException("Cannot determine URL for the Xterm service!");
			}
		});
	}
	
}
