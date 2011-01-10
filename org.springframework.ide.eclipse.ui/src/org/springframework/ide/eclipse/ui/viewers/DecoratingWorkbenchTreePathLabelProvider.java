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
package org.springframework.ide.eclipse.ui.viewers;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreePathLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * This template implementation of the {@link ITreePathLabelProvider} interface
 * provides optional callbacks for retrieving and decorating a label image and
 * text.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public class DecoratingWorkbenchTreePathLabelProvider extends LabelProvider
		implements ITreePathLabelProvider {

	private boolean isDecorating;
	private ILabelProvider wbLabelProvider;

	public DecoratingWorkbenchTreePathLabelProvider(boolean isDecorating) {
		this.isDecorating = isDecorating;
		this.wbLabelProvider = WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider();
	}

	public final boolean isDecorating() {
		return isDecorating;
	}

	@Override
	public void dispose() {
		wbLabelProvider.dispose();
		super.dispose();
	}

	@Override
	public final Image getImage(Object element) {
		return getImage(element, null);
	}

	@Override
	public final String getText(Object element) {
		return getText(element, null);
	}

	public final void updateLabel(ViewerLabel label, TreePath elementPath) {
		Object element = elementPath.getLastSegment();
		if (element != null) {
			Object parentElement;
			TreePath parentPath = elementPath.getParentPath();
			if (parentPath != null) {
				parentElement = parentPath.getLastSegment();
			}
			else {
				parentElement = null;
			}
			label.setImage(getImage(element, parentElement));
			label.setText(getText(element, parentElement));
		}
	}

	/**
	 * Returns the decorated image for the given {@link TreePath} element, it's
	 * parent element.
	 */
	protected Image getImage(Object element, Object parentElement) {
		return wbLabelProvider.getImage(element);
	}

	/**
	 * Returns the decorated text for the given {@link TreePath} element, it's
	 * parent element.
	 */
	protected String getText(Object element, Object parentElement) {
		return wbLabelProvider.getText(element);
	}
}
