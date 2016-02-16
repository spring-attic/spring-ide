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
package org.springframework.ide.eclipse.aop.ui.matcher.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelProvider;

/**
 * Simple wrapper for a {@link JavaElementLabelProvider} and a
 * {@link BeansModelLabelProvider}.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class WrappingBeansAndJavaModelLabelProvider extends LabelProvider {

	private JavaElementLabelProvider javaLabelProvider = new JavaElementLabelProvider(
			JavaElementLabelProvider.SHOW_OVERLAY_ICONS
					| JavaElementLabelProvider.SHOW_SMALL_ICONS
					| JavaElementLabelProvider.SHOW_PARAMETERS
					| JavaElementLabelProvider.SHOW_RETURN_TYPE
					| JavaElementLabelProvider.SHOW_TYPE);

	private BeansModelLabelProvider beansLabelProvider = new BeansModelLabelProvider();

	private List<ILabelDecorator> labelDecorators;

	protected ListenerList listeners = new ListenerList();

	public void addLabelDecorator(ILabelDecorator decorator) {
		if (labelDecorators == null) {
			labelDecorators = new ArrayList<ILabelDecorator>(2);
		}
		labelDecorators.add(decorator);
	}

	public void addListener(ILabelProviderListener listener) {
		if (labelDecorators != null) {
			for (ILabelDecorator decorator : labelDecorators) {
				decorator.addListener(listener);
			}
		}
		listeners.add(listener);
	}

	protected Image decorateImage(Image image, Object element) {
		if (labelDecorators != null && image != null) {
			for (ILabelDecorator decorator : labelDecorators) {
				image = decorator.decorateImage(image, element);
			}
		}
		return image;
	}

	protected String decorateText(String text, Object element) {
		if (labelDecorators != null && text.length() > 0) {
			for (ILabelDecorator decorator : labelDecorators) {
				String decorated = decorator.decorateText(text, element);
				if (decorated != null) {
					text = decorated;
				}
			}
		}
		return text;
	}

	public void dispose() {
		javaLabelProvider.dispose();
		beansLabelProvider.dispose();
		if (labelDecorators != null) {
			for (int i = 0; i < labelDecorators.size(); i++) {
				ILabelDecorator decorator = (ILabelDecorator) labelDecorators
						.get(i);
				decorator.dispose();
			}
			labelDecorators = null;
		}
		super.dispose();
	}

	@Override
	public Image getImage(Object element) {
		Image image = null;
		if (element instanceof IAopReference) {
			image = javaLabelProvider.getImage(((IAopReference) element)
					.getTarget());
		}
		else if (element instanceof IBeansModelElement) {
			image = beansLabelProvider.getImage(element);
		}
		else if (element instanceof IResource) {
			image = beansLabelProvider.getImage(element);
		}
		if (image != null) {
			return decorateImage(image, element);
		}
		return super.getImage(element);
	}

	@Override
	public String getText(Object element) {
		String text = null;
		if (element instanceof IAopReference) {
			text = javaLabelProvider.getText(((IAopReference) element)
					.getTarget());
		}
		else if (element instanceof IMethod) {
			text = javaLabelProvider.getText(element);
		}
		else if (element instanceof IBeansModelElement) {
			text = beansLabelProvider.getText(element);
		}
		else if (element instanceof IResource) {
			text = ((IResource) element).getProjectRelativePath().toString();
		}
		if (text != null) {
			return decorateText(text, element);
		}
		return super.getText(element);
	}

	public void removeListener(ILabelProviderListener listener) {
		if (labelDecorators != null) {
			for (ILabelDecorator decorator : labelDecorators) {
				decorator.removeListener(listener);
			}
		}
		listeners.remove(listener);
	}
}