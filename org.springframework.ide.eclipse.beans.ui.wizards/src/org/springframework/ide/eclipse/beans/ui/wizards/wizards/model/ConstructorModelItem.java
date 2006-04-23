package org.springframework.ide.eclipse.beans.ui.wizards.wizards.model;

import org.springframework.ide.eclipse.core.ui.treemodel.AbstractModelItem;

public class ConstructorModelItem extends AbstractModelItem {

	private int order;

	private String typeName;

	public ConstructorModelItem(int order, String typeName) {
		super();
		this.order = order;
		this.typeName = typeName;
	}

	public String getUID() {
		return order + "" + typeName;
	}

	public StringBuffer toStringBufferDescription() {
		return new StringBuffer(order + "-" + typeName);
	}

	public String getTypeName() {
		return typeName;
	}


}
