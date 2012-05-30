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
package org.springframework.ide.eclipse.metadata.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.springframework.ide.eclipse.metadata.MetadataUIImages;
import org.springframework.ide.eclipse.metadata.ui.RequestMappingView;


/**
 * @author Leo Dos Santos
 */
public class ToggleOrientationAction extends Action {

	private RequestMappingView view;

	private int orientation;

	public ToggleOrientationAction(RequestMappingView view, int orientation) {
		super("", AS_RADIO_BUTTON); //$NON-NLS-1$
		if (orientation == SWT.HORIZONTAL) {
			setText(Messages.ToggleOrientationAction_DESCRIPTION_HORIZONTAL);
			setDescription(Messages.ToggleOrientationAction_DESCRIPTION_HORIZONTAL);
			setToolTipText(Messages.ToggleOrientationAction_DESCRIPTION_HORIZONTAL);
			setImageDescriptor(MetadataUIImages.DESC_OBJS_ORIENTATION_HORIZONTAL);
		} else if (orientation == SWT.VERTICAL) {
			setText(Messages.ToggleOrientationAction_DESCRIPTION_VERTICAL);
			setDescription(Messages.ToggleOrientationAction_DESCRIPTION_VERTICAL);
			setToolTipText(Messages.ToggleOrientationAction_DESCRIPTION_VERTICAL);
			setImageDescriptor(MetadataUIImages.DESC_OBJS_ORIENTATION_VERTICAL);
		}
		this.view = view;
		this.orientation = orientation;
	}

	@Override
	public void run() {
		if (isChecked()) {
			view.setOrientation(orientation);
		}
	}

	public int getOrientation() {
		return orientation;
	}

}
