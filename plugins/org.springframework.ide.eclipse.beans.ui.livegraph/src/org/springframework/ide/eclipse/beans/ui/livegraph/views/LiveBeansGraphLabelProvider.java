/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.livegraph.views;

import org.eclipse.draw2d.Label;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.zest.core.viewers.EntityConnectionData;
import org.eclipse.zest.core.viewers.ISelfStyleProvider;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;
import org.springframework.ide.eclipse.beans.ui.livegraph.model.LiveBean;

/**
 * A label provider for the Live Beans Graph
 * 
 * @author Leo Dos Santos
 */
public class LiveBeansGraphLabelProvider extends LabelProvider implements ISelfStyleProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof LiveBean) {
			LiveBean bean = (LiveBean) element;
			return bean.getDisplayName();
		}
		else if (element instanceof EntityConnectionData) {
			return "";
		}
		return super.getText(element);
	}

	public void selfStyleConnection(Object element, GraphConnection connection) {
		// TODO Auto-generated method stub

	}

	public void selfStyleNode(Object element, GraphNode node) {
		setTooltip(element, node);
	}

	private void setTooltip(Object element, GraphNode node) {
		if (element instanceof LiveBean) {
			String id = ((LiveBean) element).getId();
			Label tooltip = new Label();
			tooltip.setText(id);
			node.setTooltip(tooltip);
		}
	}

}
