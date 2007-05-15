/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
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
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * This template implementation of the {@link ITreePathLabelProvider} interface
 * provides optional callbacks for retrieving and decorating a label image and
 * text.
 * 
 * @author Torsten Juergeleit
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
		int severity = (isDecorating ? getSeverity(element, null) : 0);
		return getImage(element, null, severity);
	}

	@Override
	public final String getText(Object element) {
		int severity = (isDecorating ? getSeverity(element, null) : 0);
		return getText(element, null, severity);
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
			int severity = (isDecorating ? getSeverity(element, parentElement)
					: 0);
			label.setImage(getImage(element, parentElement, severity));
			label.setText(getText(element, parentElement, severity));
		}
	}

	/**
	 * Returns one of the <code>IMarker.SEVERITY_xxx</code> constants or
	 * <code>0</code> for the given {@link TreePath} element and it's parent
	 * element.
	 */
	protected int getSeverity(Object element, Object parentElement) {
		return 0;
	}

	/**
	 * Returns the decorated image for the given {@link TreePath} element, it's
	 * parent element and the corresponding the
	 * <code>IMarker.SEVERITY_xxx</code> constant.
	 */
	protected Image getImage(Object element, Object parentElement,
			int severity) {
		Image image = wbLabelProvider.getImage(element);
		if (isDecorating) {
			image = SpringUIUtils.getDecoratedImage(image, severity);
		}
		return image;
	}

	/**
	 * Returns the decorated text for the given {@link TreePath} element, it's
	 * parent element and the corresponding the
	 * <code>IMarker.SEVERITY_xxx</code> constant.
	 */
	protected String getText(Object element, Object parentElement,
			int severity) {
		return wbLabelProvider.getText(element);
	}
}
