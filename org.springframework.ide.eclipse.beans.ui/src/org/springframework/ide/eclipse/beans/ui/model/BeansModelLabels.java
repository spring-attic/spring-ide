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
package org.springframework.ide.eclipse.beans.ui.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeanReference;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansList;
import org.springframework.ide.eclipse.beans.core.model.IBeansMap;
import org.springframework.ide.eclipse.beans.core.model.IBeansMapEntry;
import org.springframework.ide.eclipse.beans.core.model.IBeansProperties;
import org.springframework.ide.eclipse.beans.core.model.IBeansSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansTypedString;
import org.springframework.ide.eclipse.beans.ui.BeansUILabels;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.util.StringUtils;

/**
 * This class provides labels for the beans core model's {@link IModelElement elements}.
 * 
 * @author Torsten Juergeleit
 */
public final class BeansModelLabels extends BeansUILabels {

	public static String getElementLabel(IModelElement element, int flags) {
		StringBuffer buf = new StringBuffer(60);
		appendElementLabel(element, flags, buf);
		return buf.toString();
	}

	public static void appendElementLabel(IModelElement element, int flags, StringBuffer buf) {
		if (isFlagged(flags, PREPEND_PATH)) {
			appendElementPathLabel(element, flags, buf);
			buf.append(CONCAT_STRING);
		}
		if (element instanceof IBeansConfig) {
			appendBeansConfigLabel((IBeansConfig) element, flags, buf);
		}
		else if (element instanceof ISourceModelElement) {
			appendElementLabel((ISourceModelElement) element, buf);
		}
		else {
			buf.append(element.getElementName());
		}
		if (isFlagged(flags, APPEND_PATH)) {
			buf.append(CONCAT_STRING);
			appendElementPathLabel(element, flags, buf);
		}
	}

	public static void appendElementPathLabel(IModelElement element, int flags, StringBuffer buf) {
		if (element instanceof IResourceModelElement) {
			IResource resource = ((IResourceModelElement) element).getElementResource();
			String path;
			if (element instanceof IBeansConfig && isFlagged(flags, DESCRIPTION)) {
				path = resource.getFullPath().makeRelative().removeLastSegments(1).toString();
			}
			else {
				path = resource.getProjectRelativePath().makeRelative().toString();
			}
			buf.append(path);
			if (element instanceof IBeanConstructorArgument || element instanceof IBeanProperty) {
				buf.append(CONCAT_STRING);
				buf.append(element.getElementParent().getElementName());
			}
		}
	}

	public static void appendBeansConfigLabel(IBeansConfig config, int flags, StringBuffer buf) {
		if (isFlagged(flags, DESCRIPTION)) {
			String configName = config.getElementName();
			if (config.isElementArchived()) {
				ZipEntryStorage storage = new ZipEntryStorage(config);
				buf.append(storage.getFullPath());
				buf.append(" - ");
				buf.append(storage.getFile().getName());
			}
			else {
				buf.append(new Path(configName).lastSegment());
			}
		}
		else {
			buf.append("beans");
			if (StringUtils.hasLength(config.getDefaultLazyInit())
					&& !config.getDefaultLazyInit().equals(IBeansConfig.DEFAULT_LAZY_INIT)) {
				buf.append(" lazy-init=\"");
				buf.append(config.getDefaultLazyInit());
				buf.append('"');
			}
			if (StringUtils.hasLength(config.getDefaultAutowire())
					&& !config.getDefaultAutowire().equals(IBeansConfig.DEFAULT_AUTO_WIRE)) {
				buf.append(" autowire=\"");
				buf.append(config.getDefaultAutowire());
				buf.append('"');
			}
			if (StringUtils.hasLength(config.getDefaultDependencyCheck())
					&& !config.getDefaultDependencyCheck().equals(IBeansConfig.DEFAULT_DEPENDENCY_CHECK)) {
				buf.append(" dependency-check=\"");
				buf.append(config.getDefaultDependencyCheck());
				buf.append('"');
			}
			if (StringUtils.hasLength(config.getDefaultInitMethod())
					&& !config.getDefaultInitMethod().equals(IBeansConfig.DEFAULT_INIT_METHOD)) {
				buf.append(" init-method=\"");
				buf.append(config.getDefaultInitMethod());
				buf.append('"');
			}
			if (StringUtils.hasLength(config.getDefaultDestroyMethod())
					&& !config.getDefaultDestroyMethod().equals(IBeansConfig.DEFAULT_DESTROY_METHOD)) {
				buf.append(" destroy-method=");
				buf.append(config.getDefaultDestroyMethod());
				buf.append('"');
			}
			if (StringUtils.hasLength(config.getDefaultMerge())
					&& !config.getDefaultMerge().equals(IBeansConfig.DEFAULT_MERGE)) {
				buf.append(" merge=\"");
				buf.append(config.getDefaultMerge());
				buf.append('"');
			}
		}
	}

	public static void appendBeanLabel(IBean bean, StringBuffer buf) {
		if (!bean.isInnerBean()) {
			buf.append(bean.getElementName()).append(' ');
		}
		if (bean.getAliases() != null && bean.getAliases().length > 0) {
			buf.append('\'');
			buf.append(StringUtils.arrayToDelimitedString(bean.getAliases(), LIST_DELIMITER_STRING));
			buf.append('\'');
		}
		if (bean.getClassName() != null) {
			buf.append('[').append(bean.getClassName()).append(']');
		}
		else if (bean.getParentName() != null) {
			buf.append('<').append(bean.getParentName()).append('>');
		}
	}

	public static void appendElementLabel(IModelElement element, StringBuffer buf) {
		if (element instanceof IBeansList) {
			buf.append("list");
		}
		else if (element instanceof IBeansSet) {
			buf.append("set");
		}
		else if (element instanceof IBeansMap) {
			buf.append("map");
		}
		else if (element instanceof IBeansMapEntry) {
			buf.append("entry");
		}
		else if (element instanceof IBeansProperties) {
			buf.append("props");
		}
		else if (element instanceof IBeanReference) {
			buf.append('<');
			buf.append(((IBeanReference) element).getBeanName());
			buf.append('>');
		}
		else if (element instanceof IBeansTypedString) {
			buf.append(((IBeansTypedString) element).getString());
		}
		else {
			buf.append(element.getElementName());
		}
	}
}
