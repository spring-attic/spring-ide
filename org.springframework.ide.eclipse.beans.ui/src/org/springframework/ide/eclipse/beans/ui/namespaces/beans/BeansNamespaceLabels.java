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

package org.springframework.ide.eclipse.beans.ui.namespaces.beans;

import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.ui.BeansUILabels;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabels;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.util.StringUtils;

/**
 * This class provides labels for the beans core model's
 * {@link ISourceModelElement elements} in the namespace
 * <code>"http://www.springframework.org/schema/beans"</code>.
 * 
 * @author Torsten Juergeleit
 */
public final class BeansNamespaceLabels extends BeansUILabels {

	public static String getElementLabel(ISourceModelElement element,
			int flags) {
		StringBuffer buf = new StringBuffer(60);
		appendElementLabel(element, flags, buf);
		return buf.toString();
	}

	public static void appendElementLabel(ISourceModelElement element,
			int flags, StringBuffer buf) {
		if (isFlagged(flags, PREPEND_PATH)) {
			BeansModelLabels.appendElementPathLabel(element, flags, buf);
			buf.append(CONCAT_STRING);
		}
		if (element instanceof IBean) {
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

	public static void appendBeanLabel(IBean bean, StringBuffer buf) {
		buf.append(bean.getElementName());
		if (bean.getAliases() != null && bean.getAliases().length > 0) {
			buf.append(" '");
			buf.append(StringUtils.arrayToDelimitedString(bean.getAliases(),
					LIST_DELIMITER_STRING));
			buf.append('\'');
		}
		if (bean.getClassName() != null) {
			buf.append(" [").append(bean.getClassName()).append(']');
		} else if (bean.getParentName() != null) {
			buf.append(" <").append(bean.getParentName()).append('>');
		}
	}
}
