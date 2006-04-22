package org.springframework.ide.eclipse.beans.ui.wizards.wizards.model;

import org.springframework.ide.eclipse.core.ui.treemodel.AbstractModelItem;

public class MapModelItem extends AbstractModelItem {
	public MapModelItem() {
		
	}
	
	public String getUID() {
		return "just one";
	}

	public StringBuffer toStringBufferDescription() {
		return new StringBuffer("map");
	}

}
