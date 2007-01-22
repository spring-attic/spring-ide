package org.springframework.ide.eclipse.beans.ui.properties.model;

import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelProvider;

public class PropertiesModelLabelProvider extends BeansModelLabelProvider {

	public String getText(Object element) {
		if (element instanceof IBeansConfig) {
			return ((IBeansConfig) element).getElementName();
		}
		return super.getText(element);
	}
}
