package org.springframework.ide.eclipse.beans.ui.wizards.model;

import org.springframework.ide.eclipse.core.ui.treemodel.AbstractModelItem;


public class PropsModelItem extends AbstractModelItem {

	public String getUID() {
		return "props";
	}

	public StringBuffer toStringBufferDescription() {
		return new StringBuffer("props");
	}

}
