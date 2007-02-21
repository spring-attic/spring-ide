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

package org.springframework.ide.eclipse.beans.ui.namespaces.aop;

import org.eclipse.jdt.core.IType;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.core.internal.Introspector;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelImages;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.ModelUtils;

/**
 * This class provides images for the beans core model's
 * {@link ISourceModelElement elements} in the namespace
 * <code>"http://www.springframework.org/schema/aop"</code>.
 * 
 * @author Torsten Juergeleit
 */
public final class AopNamespaceImages {

	public static Image getImage(ISourceModelElement element) {
		return getImage(element, null);
	}

	public static Image getImage(ISourceModelElement element,
			IModelElement context) {
		if (element instanceof IBeansComponent) {
			String localName = ModelUtils
					.getLocalName((IBeansComponent) element);
			if ("config".equals(localName)) {
				return BeansUIImages
						.getImage(BeansUIImages.IMG_OBJS_AOP_CONFIG);
			} else if ("advisor".equals(localName)) {
				return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_ADVICE);
			} else if ("aspect".equals(localName)) {
				return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_ASPECT);
			} else if ("pointcut".equals(localName)) {
				return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_POINTCUT);
			}
		} else if (element instanceof IBean
				&& context instanceof IBeansComponent) {
			String localName = ModelUtils.getLocalName((IBean) element);
			String contextLocalName = ModelUtils
					.getLocalName((IBeansComponent) context);
			if (localName.equals(contextLocalName)) {
				IType type = BeansModelUtils.getBeanType((IBean) element, null);
				if (type != null) {
					if (Introspector.doesImplement(type,
							"org.springframework.aop.Pointcut")) {
						return BeansUIImages
								.getImage(BeansUIImages.IMG_OBJS_POINTCUT);
					} else if (Introspector.doesImplement(type,
							"org.springframework.aop.Advisor")) {
						return BeansUIImages
								.getImage(BeansUIImages.IMG_OBJS_ADVICE);
					}
				}
			} else {
				if ("before".equals(localName)) {
					return BeansUIImages
							.getImage(BeansUIImages.IMG_OBJS_BEFORE_ADVICE);
				} else if ("after".equals(localName)) {
					return BeansUIImages
							.getImage(BeansUIImages.IMG_OBJS_AFTER_ADVICE);
				} else if ("around".equals(localName)) {
					return BeansUIImages
							.getImage(BeansUIImages.IMG_OBJS_AROUND_ADVICE);
				} else if ("pointcut".equals(localName)) {
					return BeansUIImages
							.getImage(BeansUIImages.IMG_OBJS_POINTCUT);
				}
			}
			return BeansModelImages.getDecoratedImage(BeansUIImages
					.getImage(BeansUIImages.IMG_OBJS_NAMESPACE), element,
					context);
		}
		return BeansModelImages.getImage(element, context);
	}
}
