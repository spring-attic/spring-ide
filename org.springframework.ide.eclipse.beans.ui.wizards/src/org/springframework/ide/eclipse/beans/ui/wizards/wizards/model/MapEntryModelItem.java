package org.springframework.ide.eclipse.beans.ui.wizards.wizards.model;

import org.springframework.ide.eclipse.core.ui.treemodel.AbstractModelItem;

public class MapEntryModelItem extends AbstractModelItem {
	
	private String keyValue;
	
	public MapEntryModelItem(String keyValue) {
		this.keyValue=keyValue;
	}

	public String getUID() {
		return keyValue;
	}

	public StringBuffer toStringBufferDescription() {
		return new StringBuffer(keyValue);
	}

	public String getKeyValue() {
		return keyValue;
	}

	public void setKeyValue(String keyValue) {
		this.keyValue = keyValue;
	}

}
