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

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.zest.core.viewers.EntityConnectionData;
import org.springframework.ide.eclipse.beans.ui.livegraph.model.LiveBean;

/**
 * A label provider for the Live Beans Graph
 * 
 * @author Leo Dos Santos
 */
public class LiveBeansGraphLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof LiveBean) {
			LiveBean bean = (LiveBean) element;
			return bean.getId();
		}
		else if (element instanceof EntityConnectionData) {
			return "";
		}
		return super.getText(element);
	}

}
