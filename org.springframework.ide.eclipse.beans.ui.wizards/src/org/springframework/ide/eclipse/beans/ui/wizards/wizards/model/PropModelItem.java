package org.springframework.ide.eclipse.beans.ui.wizards.wizards.model;

import org.springframework.ide.eclipse.core.ui.treemodel.AbstractModelItem;


public class PropModelItem extends AbstractModelItem {
	
	private String key;
	private String value;
	
	public PropModelItem(String key,String value) {
		this.key=key;
		this.value=value;
	}
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getUID() {
		return key;
	}

	public StringBuffer toStringBufferDescription() {
		return new StringBuffer(key+"="+value);
	}

}
