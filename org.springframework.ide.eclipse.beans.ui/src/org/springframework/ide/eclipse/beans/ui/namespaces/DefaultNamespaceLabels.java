/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.eclipse.beans.ui.namespaces;

import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.ui.BeansUILabels;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabels;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.ModelUtils;

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
		} else if (element instanceof IBeanProperty) {
			BeansModelLabels.appendBeanPropertyLabel((IBeanProperty) element,
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
		IModelElement parent = bean.getElementParent();
		if (parent instanceof IBeansConfig
				|| parent instanceof IBeansComponent) {
			if (appendNodeName(bean, buf)) {
				buf.append(' ');
			}
		}
		if (bean.isRootBean()) {
			buf.append('[').append(bean.getClassName()).append(']');
		} else {
			buf.append('<').append(bean.getParentName()).append('>');
		}
	}
}
