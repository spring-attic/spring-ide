package org.springframework.ide.eclipse.beans.ui.wizards.model;

import org.springframework.ide.eclipse.core.ui.treemodel.AbstractModelItem;

public class SetModelItem extends AbstractModelItem {

	public String getUID() {
		return "set";
	}

	public StringBuffer toStringBufferDescription() {
		return new StringBuffer("set");
	}

}
