/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui.namespaces.aop;

import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.ui.BeansUILabels;
import org.springframework.ide.eclipse.beans.ui.namespaces.beans.BeansNamespaceLabels;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.ModelUtils;

/**
 * This class provides labels for the beans core model's
 * {@link ISourceModelElement elements} in the namespace
 * <code>"http://www.springframework.org/schema/aop"</code>.
 * 
 * @author Torsten Juergeleit
 */
public final class AopNamespaceLabels extends BeansUILabels {

	public static String getElementLabel(ISourceModelElement element,
			int flags) {
		StringBuffer buf = new StringBuffer(60);
		appendElementLabel(element, flags, buf);
		return buf.toString();
	}

	public static void appendElementLabel(ISourceModelElement element,
			int flags, StringBuffer buf) {
		if (isFlagged(flags, PREPEND_PATH)) {
			BeansNamespaceLabels.appendElementPathLabel(element, buf);
			buf.append(CONCAT_STRING);
		}
		if (element instanceof IBeansComponent) {
			appendBeansComponentLabel((IBeansComponent) element, buf);
		} else if (element instanceof IBean) {
			appendBeanLabel((IBean) element, buf);
		} else {
			BeansNamespaceLabels.appendElementLabel(element, flags, buf);
		}
		if (isFlagged(flags, APPEND_PATH)) {
			buf.append(CONCAT_STRING);
			BeansNamespaceLabels.appendElementPathLabel(element, buf);
		}
	}

	protected static void appendBeansComponentLabel(IBeansComponent component,
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

	protected static void appendBeanLabel(IBean bean, StringBuffer buf) {
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
