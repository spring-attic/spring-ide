package org.springframework.ide.eclipse.beans.ui.wizards.wizards.model;

import org.springframework.ide.eclipse.core.ui.treemodel.AbstractModelItem;

public class ValueModelItem extends AbstractModelItem{
	private String value;

	public ValueModelItem(String value) {
		super();
		this.value=value;
	}

	public String getValue() {
		return value;
	}

	public String getUID() {
		return value;
	}

	public StringBuffer toStringBufferDescription(){
		return new StringBuffer(value);
	}
	
}
