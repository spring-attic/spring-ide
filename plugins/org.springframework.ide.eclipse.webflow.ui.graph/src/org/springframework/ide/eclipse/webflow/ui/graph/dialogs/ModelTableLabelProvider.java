/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.graph.dialogs;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.webflow.core.model.IArgument;
import org.springframework.ide.eclipse.webflow.core.model.IAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IImport;
import org.springframework.ide.eclipse.webflow.core.model.IInputAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IMapping;
import org.springframework.ide.eclipse.webflow.core.model.IStateTransition;
import org.springframework.ide.eclipse.webflow.core.model.IVar;

/**
 * @author Christian Dupuis
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
		else if (element instanceof IVar) {
			if (columnIndex == 0) {
				if (((IVar) element).getName() != null)
					return ((IVar) element).getName();
				else
					return "";
			}
			else if (columnIndex == 1) {
				if (((IVar) element).getScope() != null)
					return ((IVar) element).getScope();
				else
					return "";
			}
			else if (columnIndex == 2) {
				if (((IVar) element).getClazz() != null)
					return ((IVar) element).getClazz();
				else
					return "";
			}
			else if (columnIndex == 3) {
				if (((IVar) element).getBean()!= null)
					return ((IVar) element).getBean();
				else
					return "";
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
		else if (element instanceof IImport) {
			if (columnIndex == 0) {
				if (((IImport) element).getResource() != null)
					return ((IImport) element).getResource();
				else
					return "";
			}
		}
		else if (element instanceof IStateTransition) {
			if (columnIndex == 0) {
				if (((IStateTransition) element).getOn() != null)
					return ((IStateTransition) element).getOn();
				else
					return "";
			}
			else if (columnIndex == 1) {
				if (((IStateTransition) element).getToStateId() != null)
					return ((IStateTransition) element).getToStateId();
				else
					return "";
			}
			else if (columnIndex == 2) {
				if (((IStateTransition) element).getOnException() != null)
					return ((IStateTransition) element).getOnException();
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
