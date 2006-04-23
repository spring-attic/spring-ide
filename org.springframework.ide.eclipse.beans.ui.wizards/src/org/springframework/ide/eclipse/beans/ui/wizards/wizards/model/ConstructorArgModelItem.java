package org.springframework.ide.eclipse.beans.ui.wizards.wizards.model;

import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.core.ui.treemodel.AbstractModelItem;

public class ConstructorArgModelItem extends AbstractModelItem {
	private int order;

	private IType type;

	private String name;

	private boolean primitive;

	private String primitiveTypeName;

	public ConstructorArgModelItem(int order) {
		super();
		this.order = order;
	}

	public String getUID() {
		return "" + order;
	}

	public StringBuffer toStringBufferDescription() {
		StringBuffer result = new StringBuffer("" + this.order);
		if (primitive) {
			result.append("-" + this.primitiveTypeName);
		} else {
			result.append("-" + this.type.getElementName());
		}
		result.append("-" + this.name);
		return result;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public IType getType() {
		return type;
	}

	public void setType(IType type) {
		this.type = type;
	}

	public void setPrimitive(boolean primitive) {
		this.primitive = primitive;
	}

	public void setPrimitiveTypeName(String primitiveTypeName) {
		this.primitiveTypeName = primitiveTypeName;
	}

	public boolean isPrimitive() {
		return primitive;
	}

	public String getPrimitiveTypeName() {
		return primitiveTypeName;
	}

}
