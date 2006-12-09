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
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.ChildBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelSource;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.xml.XmlSource;
import org.springframework.util.StringUtils;

/**
 * This class provides images for the beans core model's
 * <code>IModelElement</code>s.
 * 
 * @author Torsten Juergeleit
 */
public class BeansModelLabels {

	/** String for separating post qualified names (" - ") */
	public final static String CONCAT_STRING = " - ";

	/** String for separating list items (", ") */
	public final static String COMMA_STRING = ", ";

	/** String for ellipsis ("...") */
	public final static String ELLIPSIS_STRING = "...";

	/** Add full path name and concat string */
	public final static int PREPEND_PATH = 1 << 0;

	/** Add concat string and full path name */
	public static final int APPEND_PATH = 1 << 1;

	public static String getElementLabel(IModelElement element, int flags) {
		StringBuffer buf = new StringBuffer(60);
		getElementLabel(element, flags, buf);
		return buf.toString();
	}

	public static String getElementLabel(IModelElement element, int flags,
			StringBuffer buf) {
		if (isFlagged(flags, PREPEND_PATH)) {
			appendPathLabel(element, buf);
			buf.append(CONCAT_STRING);
		}
		if (element instanceof IBeansConfig) {
//			IBeansConfig config = (IBeansConfig) element;
//			String configName = element.getElementName();
//			if (config.isElementArchived()) {
//				ZipEntryStorage storage = new ZipEntryStorage(config);
//				if (configName.charAt(0) == IBeansConfig
//						.EXTERNAL_FILE_NAME_PREFIX) {
//					buf.append(storage.getZipResource()
//							.getFullPath().toString());
//				} else {
//					buf.append(storage.getZipResource()
//							.getProjectRelativePath().toString());
//				}
//				buf.append(" - ");
//				buf.append(storage.getFullPath().toString());
//			} else {
//				buf.append(configName);
//			}
			buf.append("beans");
		} else if (element instanceof IBeansComponent) {
			appendBeansComponentLabel((IBeansComponent) element, buf);
		} else if (element instanceof IBean) {
			appendBeanLabel((IBean) element, buf);
		} else if (element instanceof IBeanProperty) {
			appendBeanPropertyLabel((IBeanProperty) element, buf);
		} else {
			buf.append(element.getElementName());
		}
		if (isFlagged(flags, APPEND_PATH)) {
			buf.append(CONCAT_STRING);
			appendPathLabel(element, buf);
		}
		return buf.toString();
	}

	protected static void appendPathLabel(IModelElement element,
			StringBuffer buf) {
		if (element instanceof IResourceModelElement) {
			IResource resource = ((IResourceModelElement) element)
					.getElementResource();
			buf.append(resource.getFullPath().makeRelative());
			if (element instanceof IBeanConstructorArgument ||
					element instanceof IBeanProperty) {
				buf.append(CONCAT_STRING);
				buf.append(element.getElementParent().getElementName());
			}
		}
	}

	protected static void appendBeansComponentLabel(IBeansComponent component,
			StringBuffer buf) {
		IModelSource source = component.getElementSource();
		if (source instanceof XmlSource) {
			String nodename = ((XmlSource) source).getNodeName();
			if (!nodename.equals(component.getElementName())) {
				buf.append(((XmlSource) source).getNodeName()).append(' ');
			}
		}
		buf.append(component.getElementName());
	}

	protected static void appendBeanLabel(IBean bean, StringBuffer buf) {
		if (bean.getElementParent() instanceof IBeansConfig) {
			IModelSource source = bean.getElementSource();
			if (source instanceof XmlSource) {
				String prefix = ((XmlSource) source).getPrefix();
				if (prefix != null && prefix.length() > 0) {
					buf.append(((XmlSource) source).getNodeName()).append(' ');
				}
			}
		}
		buf.append(bean.getElementName());
		if (bean.getAliases() != null && bean.getAliases().length > 0) {
			buf.append(" '");
			buf.append(StringUtils.arrayToDelimitedString(bean.getAliases(),
					COMMA_STRING));
			buf.append('\'');
		}
		if (bean.getClassName() != null) {
			buf.append(" [").append(bean.getClassName()).append(']');
		} else if (bean.getParentName() != null) {
			buf.append(" <").append(bean.getParentName()).append('>');
		}
	}

	protected static void appendBeanPropertyLabel(IBeanProperty property,
			StringBuffer buf) {
		buf.append(property.getElementName());
		Object value = ((IBeanProperty) property).getValue();
		if (value instanceof String) {
			buf.append(" \"").append(value).append('"');
		} else if (value instanceof BeanDefinitionHolder) {
			BeanDefinition beanDef = ((BeanDefinitionHolder) value)
					.getBeanDefinition();
			buf.append(" {");
			if (beanDef instanceof RootBeanDefinition) {
				buf.append('[');
				buf.append(((RootBeanDefinition) beanDef).getBeanClassName());
				buf.append(']');
			} else {
				buf.append('<');
				buf.append(((ChildBeanDefinition) beanDef).getParentName());
				buf.append('>');
			}
			buf.append('}');
		} else {
			buf.append(' ').append(value);
		}
	}

	protected static final boolean isFlagged(int flags, int flag) {
		return (flags & flag) != 0;
	}
}
