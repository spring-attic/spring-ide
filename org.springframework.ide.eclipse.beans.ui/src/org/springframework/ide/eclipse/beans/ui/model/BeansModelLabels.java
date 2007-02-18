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

package org.springframework.ide.eclipse.beans.ui.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.ui.BeansUILabels;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.util.StringUtils;

/**
 * This class provides labels for the beans core model's
 * {@link IModelElement elements}.
 * 
 * @author Torsten Juergeleit
 */
public final class BeansModelLabels extends BeansUILabels {

	public static String getElementLabel(IModelElement element, int flags) {
		StringBuffer buf = new StringBuffer(60);
		appendElementLabel(element, flags, buf);
		return buf.toString();
	}

	public static void appendElementLabel(IModelElement element, int flags,
			StringBuffer buf) {
		if (isFlagged(flags, PREPEND_PATH)) {
			appendElementPathLabel(element, flags, buf);
			buf.append(CONCAT_STRING);
		}
		if (element instanceof IBeansConfig) {
			appendBeansConfigLabel((IBeansConfig) element, flags, buf);
		} else if (element instanceof IBean) {
			appendBeanLabel((IBean) element, buf);
		} else if (element instanceof IBeanProperty) {
			appendBeanPropertyLabel((IBeanProperty) element, buf);
		} else {
			buf.append(element.getElementName());
		}
		if (isFlagged(flags, APPEND_PATH)) {
			buf.append(CONCAT_STRING);
			appendElementPathLabel(element, flags, buf);
		}
	}

	public static void appendElementPathLabel(IModelElement element,
			int flags, StringBuffer buf) {
		if (element instanceof IResourceModelElement) {
			IResource resource = ((IResourceModelElement) element)
					.getElementResource();
			String path;
			if (element instanceof IBeansConfig
					&& isFlagged(flags, DESCRIPTION)) {
				path = resource.getProject().getName();
			} else {
				path = resource.getFullPath().makeRelative().toString();
			}
			buf.append(path);
			if (element instanceof IBeanConstructorArgument
					|| element instanceof IBeanProperty) {
				buf.append(CONCAT_STRING);
				buf.append(element.getElementParent().getElementName());
			}
		}
	}

	public static void appendBeansConfigLabel(IBeansConfig config,
			int flags, StringBuffer buf) {
		if (isFlagged(flags, DESCRIPTION)) {
			String configName = config.getElementName();
			if (config.isElementArchived()) {
				ZipEntryStorage storage = new ZipEntryStorage(config);
				buf.append(storage.getFullPath());
				buf.append(" - ");
				buf.append(storage.getFile().getName());
			} else {
				buf.append(new Path(configName).lastSegment());
			}
		} else {
			buf.append("beans");
			if (!config.getDefaultLazyInit()
					.equals(IBeansConfig.DEFAULT_LAZY_INIT)) {
				buf.append(" lazy-init=\"");
				buf.append(config.getDefaultLazyInit());
				buf.append('"');
			}
			if (!config.getDefaultAutowire()
					.equals(IBeansConfig.DEFAULT_AUTO_WIRE)) {
				buf.append(" autowire=\"");
				buf.append(config.getDefaultAutowire());
				buf.append('"');
			}
			if (!config.getDefaultDependencyCheck()
					.equals(IBeansConfig.DEFAULT_DEPENDENCY_CHECK)) {
				buf.append(" dependency-check=\"");
				buf.append(config.getDefaultDependencyCheck());
				buf.append('"');
			}
			if (!config.getDefaultInitMethod()
					.equals(IBeansConfig.DEFAULT_INIT_METHOD)) {
				buf.append(" init-method=\"");
				buf.append(config.getDefaultInitMethod());
				buf.append('"');
			}
			if (!config.getDefaultDestroyMethod()
					.equals(IBeansConfig.DEFAULT_DESTROY_METHOD)) {
				buf.append(" destroy-method=");
				buf.append(config.getDefaultDestroyMethod());
				buf.append('"');
			}
			if (!config.getDefaultMerge()
					.equals(IBeansConfig.DEFAULT_MERGE)) {
				buf.append(" merge=\"");
				buf.append(config.getDefaultMerge());
				buf.append('"');
			}
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

	public static void appendBeanPropertyLabel(IBeanProperty property,
			StringBuffer buf) {
		buf.append(property.getElementName());
		Object value = ((IBeanProperty) property).getValue();
		buf.append(": ").append(BeansModelUtils.getValueName(value));
	}
}
