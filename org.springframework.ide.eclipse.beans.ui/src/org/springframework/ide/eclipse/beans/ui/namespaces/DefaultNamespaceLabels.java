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
package org.springframework.ide.eclipse.beans.ui.namespaces;

import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.ui.BeansUILabels;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabels;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.ModelUtils;
import org.springframework.util.StringUtils;

/**
 * This class provides labels for the beans core model's
 * {@link ISourceModelElement elements} which belong to a namespace.
 * 
 * @author Torsten Juergeleit
 */
public final class DefaultNamespaceLabels extends BeansUILabels {

	public static String getElementLabel(IModelElement element, int flags) {
		StringBuffer buf = new StringBuffer(60);
		appendElementLabel(element, flags, buf);
		return buf.toString();
	}

	public static void appendElementLabel(IModelElement element, int flags,
			StringBuffer buf) {
		if (isFlagged(flags, PREPEND_PATH)) {
			BeansModelLabels.appendElementPathLabel(element, flags, buf);
			buf.append(CONCAT_STRING);
		}
		if (element instanceof IBeansComponent) {
			appendBeansComponentLabel((IBeansComponent) element, buf);
		} else if (element instanceof IBean) {
			appendBeanLabel((IBean) element, buf);
		} else if (element instanceof ISourceModelElement) {
			BeansModelLabels.appendElementLabel((ISourceModelElement) element,
					buf);
		} else {
			buf.append(element.getElementName());
		}
		if (isFlagged(flags, APPEND_PATH)) {
			buf.append(CONCAT_STRING);
			BeansModelLabels.appendElementPathLabel(element, flags, buf);
		}
	}

	public static void appendBeansComponentLabel(IBeansComponent component,
			StringBuffer buf) {
		String compName = component.getElementName();
		if (appendNodeName(component, buf)) {
			if (compName.equals(ModelUtils.getNodeName(component))) {

				// Don't use the node name twice
				return;
			}
			buf.append(' ');
		}
		buf.append(component.getElementName());
	}

	public static void appendBeanLabel(IBean bean, StringBuffer buf) {
		if (!bean.isInnerBean()) {
			if (appendNodeName(bean, buf)) {
				buf.append(' ');
			}
			if (StringUtils.hasText(bean.getElementName())) {
				buf.append(bean.getElementName()).append(' ');
			}
		}
		if (bean.isRootBean()) {
			buf.append('[').append(bean.getClassName()).append(']');
		} else {
			buf.append('<').append(bean.getParentName()).append('>');
		}
	}
}
