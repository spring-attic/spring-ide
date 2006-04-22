package org.springframework.ide.eclipse.beans.ui.wizards.wizards.model;

import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.core.ui.treemodel.AbstractModelItem;

public class PropertyModelItem extends AbstractModelItem {
	public PropertyModelItem(String propertyName) {
		super();
		this.name = propertyName;
	}

	private String name;

	private IType type;

	private boolean primitive = false;

	private String primitiveTypeName;

	public boolean isPrimitive() {
		return primitive;
	}

	public void setPrimitive(boolean primitive) {
		this.primitive = primitive;
	}

	public String getPrimitiveTypeName() {
		return primitiveTypeName;
	}

	public void setPrimitiveTypeName(String primitiveTypeName) {
		this.primitiveTypeName = primitiveTypeName;
	}

	public String getName() {
		return name;
	}

	public IType getType() {
		return type;
	}

	public void setType(IType type) {
		this.type = type;
	}

	public String getUID() {
		return name;
	}

	public StringBuffer toStringBufferDescription() {
		return new StringBuffer(name);
	}
	
}
