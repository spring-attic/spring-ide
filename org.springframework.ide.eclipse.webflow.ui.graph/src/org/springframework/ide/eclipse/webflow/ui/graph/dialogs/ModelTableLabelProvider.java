/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.eclipse.webflow.ui.graph.dialogs;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.webflow.core.model.IArgument;
import org.springframework.ide.eclipse.webflow.core.model.IAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IInputAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IMapping;

/**
 * 
 */
public class ModelTableLabelProvider implements ITableLabelProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof IAttribute) {
			if (columnIndex == 0)
				return ((IAttribute) element).getName();
			else if (columnIndex == 1)
				return ((IAttribute) element).getValue();
			else if (columnIndex == 2) {
				if (((IAttribute) element).getType() != null)
					return ((IAttribute) element).getType();
				else
					return "";
			}
		}
		else if (element instanceof IArgument) {
			if (columnIndex == 0) {
				if (((IArgument) element).getExpression() != null)
					return ((IArgument) element).getExpression();
				else
					return "";
			}
			else if (columnIndex == 1) {
				if (((IArgument) element).getParameterType() != null)
					return ((IArgument) element).getParameterType();
				else
					return "";
			}
		}
		else if (element instanceof IInputAttribute) {
			if (columnIndex == 0) {
				if (((IInputAttribute) element).getName() != null)
					return ((IInputAttribute) element).getName();
				else
					return "";
			}
			else if (columnIndex == 1) {
				if (((IInputAttribute) element).getScope() != null)
					return ((IInputAttribute) element).getScope();
				else
					return "";
			}
			else if (columnIndex == 2) {
				return Boolean.toString(((IInputAttribute) element)
						.getRequired());
			}
		}
		else if (element instanceof IMapping) {
			if (columnIndex == 0) {
				if (((IMapping) element).getSource() != null)
					return ((IMapping) element).getSource();
				else
					return "";
			}
		}
		/*
		 * else if (element instanceof IOutputMapping) { if (columnIndex == 0) {
		 * if (((IOutputMapping) element).getName() != null) return
		 * ((IOutputMapping) element).getName(); else return ""; } else if
		 * (columnIndex == 1) { if (((IOutputMapping) element).getValue() !=
		 * null) return ((IOutputMapping) element).getValue(); else return ""; }
		 * else if (columnIndex == 2) { if (((IOutputMapping) element).getAs() !=
		 * null) return ((IOutputMapping) element).getAs(); else return ""; }
		 * else if (columnIndex == 3) { if (((IOutputMapping) element).getType() !=
		 * null) return ((IOutputMapping) element).getType(); else return ""; } }
		 */
		return "";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
	}
}