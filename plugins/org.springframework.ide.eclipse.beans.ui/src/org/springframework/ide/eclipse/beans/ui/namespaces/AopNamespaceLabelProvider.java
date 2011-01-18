/*******************************************************************************
 * Copyright (c) 2006, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.namespaces;

import org.eclipse.jdt.core.IType;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.ModelUtils;

/**
 * This class is a label provider which knows about the beans core model's
 * {@link ISourceModelElement source elements} in the namespace
 * <code>"http://www.springframework.org/schema/aop"</code>.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class AopNamespaceLabelProvider extends DefaultNamespaceLabelProvider {

	@Override
	public Image getImage(ISourceModelElement element, IModelElement context,
			boolean isDecorating) {
		if (element instanceof IBeansComponent) {
			String localName = ModelUtils
					.getLocalName((IBeansComponent) element);
			if ("config".equals(localName)) {
				return BeansUIImages
						.getImage(BeansUIImages.IMG_OBJS_AOP_CONFIG);
			}
			if ("advisor".equals(localName)) {
				return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_ADVICE);
			}
			if ("aspect".equals(localName)) {
				return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_ASPECT);
			}
			if ("pointcut".equals(localName)) {
				return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_POINTCUT);
			}
		}
		else if (element instanceof IBean) {
			String localName = ModelUtils
					.getLocalName((ISourceModelElement) element);
			String contextLocalName = ModelUtils
					.getLocalName((IBeansComponent) context);
			if (localName.equals(contextLocalName)) {
				IType type = BeansModelUtils.getBeanType((IBean) element, null);
				if (type != null) {
					if (Introspector.doesImplement(type,
							"org.springframework.aop.Pointcut")) {
						return BeansUIImages
								.getImage(BeansUIImages.IMG_OBJS_POINTCUT);
					}
					else if (Introspector.doesImplement(type,
							"org.springframework.aop.Advisor")) {
						return BeansUIImages
								.getImage(BeansUIImages.IMG_OBJS_ADVICE);
					}
				}
			}
			else {
				if ("before".equals(localName)) {
					return BeansUIImages
							.getImage(BeansUIImages.IMG_OBJS_BEFORE_ADVICE);
				}
				if ("after".equals(localName)) {
					return BeansUIImages
							.getImage(BeansUIImages.IMG_OBJS_AFTER_ADVICE);
				}
				if ("around".equals(localName)) {
					return BeansUIImages
							.getImage(BeansUIImages.IMG_OBJS_AROUND_ADVICE);
				}
				if ("after-returning".equals(localName)) {
					return BeansUIImages
							.getImage(BeansUIImages.IMG_OBJS_AFTER_ADVICE);
				}
				if ("after-throwing".equals(localName)) {
					return BeansUIImages
							.getImage(BeansUIImages.IMG_OBJS_AFTER_ADVICE);
				}
				if ("pointcut".equals(localName)) {
					return BeansUIImages
							.getImage(BeansUIImages.IMG_OBJS_POINTCUT);
				}
				if ("aspectj-autoproxy".equals(localName)) {
					return BeansUIImages
							.getImage(BeansUIImages.IMG_OBJS_AOP_CONFIG);
				}
				if ("include".equals(localName)) {
					return BeansUIImages
							.getImage(BeansUIImages.IMG_OBJS_AOP_CONFIG);
				}
				if ("declare-parents".equals(localName)) {
					return BeansUIImages
							.getImage(BeansUIImages.IMG_OBJS_INTRODUCTION);
				}
			}
		}
		return super.getImage(element, context, isDecorating);
	}
}
