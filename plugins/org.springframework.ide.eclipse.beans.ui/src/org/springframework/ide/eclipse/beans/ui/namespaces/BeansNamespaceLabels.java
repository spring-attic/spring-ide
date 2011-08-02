/*******************************************************************************
 * Copyright (c) 2007, 2011 Spring IDE Developers
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
import org.springframework.ide.eclipse.beans.core.model.IProfileAwareBeansComponent;
import org.springframework.ide.eclipse.beans.ui.BeansUILabels;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabels;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * This class provides labels for the beans core model's {@link ISourceModelElement elements} in the namespace
 * <code>"http://www.springframework.org/schema/beans"</code>.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public final class BeansNamespaceLabels extends BeansUILabels {

	public static String getElementLabel(ISourceModelElement element, int flags) {
		StringBuffer buf = new StringBuffer(60);
		appendElementLabel(element, flags, buf);
		return buf.toString();
	}

	public static void appendElementLabel(ISourceModelElement element, int flags, StringBuffer buf) {
		if (isFlagged(flags, PREPEND_PATH)) {
			BeansModelLabels.appendElementPathLabel(element, flags, buf);
			buf.append(CONCAT_STRING);
		}
		if (element instanceof IBean) {
			appendBeanLabel((IBean) element, buf);
		}
		else if (element instanceof IProfileAwareBeansComponent) {
			IProfileAwareBeansComponent component = (IProfileAwareBeansComponent) element;
			buf.append(element.getElementName());
			if (component.getProfiles() != null && component.getProfiles().size() > 0) {
				buf.append(" profiles=\"").append(StringUtils.collectionToDelimitedString(component.getProfiles(), ", ")).append("\"");
			}
		}
		else if (element instanceof ISourceModelElement) {
			BeansModelLabels.appendElementLabel(element, buf);
		}
		else if (element != null) {
			buf.append(element.getElementName());
		}
		if (isFlagged(flags, APPEND_PATH)) {
			buf.append(CONCAT_STRING);
			BeansModelLabels.appendElementPathLabel(element, flags, buf);
		}
	}

	public static void appendBeanLabel(IBean bean, StringBuffer buf) {
		if (!bean.isInnerBean()) {
			if (bean.isGeneratedElementName() && bean.getClassName() != null) {
				buf.append("<anonymous> ").append(ClassUtils.getShortName(bean.getClassName())).append(' ');
			}
			else if (bean.isGeneratedElementName() && bean.getClassName() == null) {
				buf.append("<anonymous> ");
			}
			else {
				buf.append(bean.getElementName()).append(' ');
				if (bean.getAliases() != null && bean.getAliases().length > 0) {
					buf.append('\'');
					buf.append(StringUtils.arrayToDelimitedString(bean.getAliases(), LIST_DELIMITER_STRING));
					buf.append("' ");
				}
			}
		}
		if (bean.getClassName() != null) {
			buf.append('[').append(bean.getClassName()).append(']');
		}
		else if (bean.getParentName() != null) {
			buf.append('<').append(bean.getParentName()).append('>');
		}
	}
}
