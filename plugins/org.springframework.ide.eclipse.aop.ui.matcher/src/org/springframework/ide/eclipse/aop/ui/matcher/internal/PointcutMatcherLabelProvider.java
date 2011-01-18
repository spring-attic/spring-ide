/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.eclipse.aop.ui.matcher.internal;

import org.eclipse.jdt.ui.ProblemsLabelDecorator;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.ui.matcher.PointcutMatcherResultPage;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBean;

/**
 * Label provider that provides label and images for pointcut matches.
 * <p>
 * Handles decoration of images by installing {@link ILabelDecorator} and
 * delegating to the {@link WorkbenchLabelProvider}.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class PointcutMatcherLabelProvider extends DecoratingLabelProvider {

	private PointcutMatcherResultPage pointcutMatcherResultPage;

	public PointcutMatcherLabelProvider(
			PointcutMatcherResultPage pointcutMatcherResultPage) {
		this(new WrappingBeansAndJavaModelLabelProvider(), PlatformUI
				.getWorkbench().getDecoratorManager().getLabelDecorator());
		this.pointcutMatcherResultPage = pointcutMatcherResultPage;
	}

	public PointcutMatcherLabelProvider(
			WrappingBeansAndJavaModelLabelProvider provider,
			ILabelDecorator decorator) {
		super(provider, decorator);
		provider.addLabelDecorator(new ProblemsLabelDecorator(null));
		provider.addLabelDecorator(new PointcutMatchLabelDecorator());
	}

	@Override
	public String getText(Object element) {
		if (element instanceof IAopReference
				&& this.pointcutMatcherResultPage.getLayout() 
				== AbstractTextSearchViewPage.FLAG_LAYOUT_FLAT) {
			IAopReference reference = (IAopReference) element;
			IBean bean = (IBean) BeansCorePlugin.getModel().getElement(
					reference.getTargetBeanId());
			String text = super.getText(reference.getTarget()) + " - "
					+ super.getText(bean);
			return text;
		}
		return super.getText(element);
	}
}
