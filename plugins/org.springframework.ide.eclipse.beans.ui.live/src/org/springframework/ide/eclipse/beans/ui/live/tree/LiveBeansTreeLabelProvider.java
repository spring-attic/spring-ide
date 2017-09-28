/*******************************************************************************
 * Copyright (c) 2012, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.live.tree;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.ui.live.LiveBeansUiPlugin;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBean;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeanRelation;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeansGroup;

/**
 * A label provider for the Live Beans tree display
 * 
 * @author Leo Dos Santos
 * @author Alex Boyko
 */
public class LiveBeansTreeLabelProvider extends LabelProvider {
	
	public static final LiveBeansTreeLabelProvider INSTANCE = new LiveBeansTreeLabelProvider();

	// public void decorate(Object element, IDecoration decoration) {
	// if (element instanceof LiveBean) {
	// LiveBean bean = (LiveBean) element;
	// String type = bean.getBeanType();
	// String scope = bean.getScope();
	// String text = "[type=\"" + type + "\"; scope=\"" + scope + "\"]";
	// decoration.addSuffix(text);
	// decoration.setForegroundColor(Display.getDefault().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
	// }
	// }

	@Override
	public Image getImage(Object element) {
		if (element instanceof LiveBean) {
			return LiveBeansUiPlugin.getDefault().getImageRegistry().get(LiveBeansUiPlugin.IMG_OBJS_BEAN);
		}
		else if (element instanceof LiveBeansGroup) {
			return LiveBeansUiPlugin.getDefault().getImageRegistry().get(LiveBeansUiPlugin.IMG_OBJS_CONFIG);
		}
		else if (element instanceof LiveBeanRelation) {
			// TODO: incoming/outgoing arrow images???
			return LiveBeansUiPlugin.getDefault().getImageRegistry().get(LiveBeansUiPlugin.IMG_OBJS_BEAN_REF);
		}
		return super.getImage(element);
	}

	@Override
	public String getText(Object element) {
		if (element instanceof LiveBean) {
			return ((LiveBean) element).getDisplayName();
		}
		else if (element instanceof LiveBeansGroup) {
			return ((LiveBeansGroup) element).getDisplayName();
		}
		else if (element instanceof LiveBeanRelation) {
			return ((LiveBeanRelation) element).getDisplayName();
		}
		return super.getText(element);
	}

}
