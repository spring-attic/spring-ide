package org.springframework.ide.eclipse.beans.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.dialogs.BeanListSelectionDialog;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelProvider;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * Action that opens the Bean SelectionDialog
 * 
 * @author Christian Dupuis
 */
public class OpenBeanSelectionDialogAction extends AbstractBeansConfigEditorAction {
	
	public void run(IAction action) {

		BeanListSelectionDialog dialog = new BeanListSelectionDialog(
				getWindow().getShell(),
				new BeansModelLabelProvider());

		
		if (Dialog.OK == dialog.open()) {
			IBean bean = (IBean) dialog.getFirstResult();
			SpringUIUtils.openInEditor(bean);
		}
	}
}
