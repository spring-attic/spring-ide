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
