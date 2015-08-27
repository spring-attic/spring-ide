package org.springframework.ide.eclipse.boot.properties.editor.reconciling;

import org.eclipse.core.resources.IContainer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.springframework.ide.eclipse.boot.properties.editor.ui.UserInteractions;

public class DefaultUserInteractions implements UserInteractions {

	private Shell shell;

	public DefaultUserInteractions(Shell shell) {
		this.shell = shell;
	}

	@Override
	public IContainer chooseOne(String title, String message, IContainer[] options) {
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(shell, new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IContainer) {
					IContainer c = (IContainer) element;
					return c.getFullPath().toString();
				}
				return element.toString();
			}
		});
		dialog.setElements(options);
		dialog.setBlockOnOpen(true);
		dialog.setMultipleSelection(false);
		int code = dialog.open();
		if (code == Window.OK) {
			return (IContainer) dialog.getFirstResult();
		}
		return null;
	}

}
