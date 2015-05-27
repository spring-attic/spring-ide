package org.springframework.ide.eclipse.boot.dash.views;

import java.util.Collection;

import org.eclipse.jface.action.Action;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;

public class AbstractBootDashAction extends Action {

	protected final BootDashView owner;

	public AbstractBootDashAction(BootDashView owner) {
		this.owner = owner;
	}

	/**
	 * Subclass can override to compuet enablement differently.
	 * The default implementation enables if a single element is selected.
	 */
	public void updateEnablement(Collection<BootDashElement> selecteds) {
		this.setEnabled(selecteds.size()==1);
	}

}
