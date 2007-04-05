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
package org.springframework.ide.eclipse.ui;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreePathLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.springframework.ide.eclipse.core.MarkerUtils;
import org.springframework.ide.eclipse.core.model.ISpringProject;

/**
 * This {@link ILabelProvider} knows about Spring projects.
 * 
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class SpringUILabelProvider extends LabelProvider implements
		ITreePathLabelProvider {

	private boolean isDecorating;
	private WorkbenchLabelProvider wbLabelProvider;

	public SpringUILabelProvider() {
		this(true);
	}

	public SpringUILabelProvider(boolean isDecorating) {
		this.isDecorating = isDecorating;
		this.wbLabelProvider = new WorkbenchLabelProvider();
	}

	public boolean isDecorating() {
		return isDecorating;
	}

	@Override
	public void dispose() {
		wbLabelProvider.dispose();
		super.dispose();
	}

	@Override
	public final Image getImage(Object element) {
		Image image = getBaseImage(element);
		if (isDecorating) {
			return getDecoratedImage(element, image);
		}
		return image;
	}

	protected Image getBaseImage(Object element) {
		if (element instanceof ISpringProject) {
			return SpringUIImages.getImage(SpringUIImages.IMG_OBJS_PROJECT);
		}
		return wbLabelProvider.getImage(element);
	}

	protected final Image getDecoratedImage(Object element, Image image) {
		int severity = getSeverity(element);
		if (severity == IMarker.SEVERITY_WARNING) {
			return SpringUIImages.getDecoratedImage(image,
					SpringUIImages.FLAG_WARNING);
		} else if (severity == IMarker.SEVERITY_ERROR) {
			return SpringUIImages.getDecoratedImage(image,
					SpringUIImages.FLAG_ERROR);
		}
		return image;
	}

	protected int getSeverity(Object element) {
		int severity = 0;
		if (element instanceof ISpringProject) {
			severity = MarkerUtils.getHighestSeverityFromMarkersInRange(
					((ISpringProject) element).getProject(), -1, -1);
		}
		return severity;
	}

	@Override
	public final String getText(Object element) {
		String text = getBaseText(element);
		if (isDecorating) {
			return getDecoratedText(element, text);
		}
		return text;
	}

	protected String getBaseText(Object element) {
		if (element instanceof ISpringProject) {
			return ((ISpringProject) element).getElementName();
		}
		return wbLabelProvider.getText(element);
	}

	protected String getDecoratedText(Object element, String text) {
		return text;
	}

	public void updateLabel(ViewerLabel label, TreePath elementPath) {
		Object element = elementPath.getLastSegment();
		if (element != null) {
			label.setImage(getImage(element));
			label.setText(getText(element));
		}
	}
}
