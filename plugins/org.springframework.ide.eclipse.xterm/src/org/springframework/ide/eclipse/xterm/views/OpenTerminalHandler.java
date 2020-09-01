package org.springframework.ide.eclipse.xterm.views;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.handlers.HandlerUtil;
import org.springframework.ide.eclipse.xterm.XtermPlugin;

public class OpenTerminalHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			Object obj =  HandlerUtil.getCurrentStructuredSelection(event).getFirstElement();
			IProject project = null;
			if (obj instanceof IProject) {
				project = (IProject) obj;
			} else if (obj instanceof IAdaptable) {
				project = ((IAdaptable)obj).getAdapter(IProject.class);
			}
			if (project == null) {
				throw new ExecutionException("Cannot find folder for element " + obj);
			}
			String cwd = project.getLocation().toOSString();
			XtermPlugin.getDefault().openTerminalView(null, cwd);
		} catch (Exception e) {
			throw new ExecutionException("Failed to open terminal", e);
		}
		return null;
	}


}
