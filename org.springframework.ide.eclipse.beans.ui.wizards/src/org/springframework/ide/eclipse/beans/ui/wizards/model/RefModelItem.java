package org.springframework.ide.eclipse.beans.ui.wizards.model;

import org.springframework.ide.eclipse.core.ui.treemodel.AbstractModelItem;

public class RefModelItem extends AbstractModelItem {
	private String beanId;

	public RefModelItem(String beanId) {
		super();
		this.beanId=beanId;
	}

	public String getBeanId() {
		return beanId;
	}

	public String getUID() {
		return beanId;
	}

	public StringBuffer toStringBufferDescription() {
		return new StringBuffer(beanId);
	}
}
