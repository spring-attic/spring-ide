package org.springframework.ide.eclipse.beans.ui.wizards.model;

import org.springframework.ide.eclipse.core.ui.treemodel.AbstractModelItem;

public class ListModelItem extends AbstractModelItem {
	
	public ListModelItem() {
	}
	public String getUID() {
		return "just one";
	}
	public StringBuffer toStringBufferDescription() {
		return new StringBuffer("list");
	}
}
