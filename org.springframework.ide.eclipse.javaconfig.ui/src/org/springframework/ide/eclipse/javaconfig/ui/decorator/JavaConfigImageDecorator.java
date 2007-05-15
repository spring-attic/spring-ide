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
package org.springframework.ide.eclipse.javaconfig.ui.decorator;

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.aop.ui.navigator.model.AdviceAopTargetBeanNode;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.model.DefaultModelSourceLocation;
import org.springframework.ide.eclipse.core.model.IModelChangeListener;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent;
import org.springframework.ide.eclipse.javaconfig.ui.Activator;
import org.springframework.ide.eclipse.javaconfig.ui.util.JavaConfigUIImages;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * {@link ILightweightLabelDecorator} that decorates {@link IBean} instances
 * that are created by the Spring JavaConfig support.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class JavaConfigImageDecorator extends LabelProvider implements
		ILightweightLabelDecorator {

	public static final String DECORATOR_ID = Activator.PLUGIN_ID
			+ ".decorator.javaconfiguidecorator";

	private IModelChangeListener listener;

	public JavaConfigImageDecorator() {
		listener = new IModelChangeListener() {
			public void elementChanged(ModelChangeEvent event) {
				if (event.getElement() instanceof IBeansProject
						&& event.getType() != ModelChangeEvent.Type.REMOVED) {
					update();
				}
			}
		};
		BeansCorePlugin.getModel().addChangeListener(listener);
	}

	public void decorate(Object element, IDecoration decoration) {
		// decorate every instance of IBean in the workbench
		if (element instanceof IBean) {
			IBean bean = (IBean) element;
			calculateDecorationForBean(decoration, bean);
		}
		// Decorate bean nodes in the Beans Cross References View
		else if (element instanceof AdviceAopTargetBeanNode) {
			AdviceAopTargetBeanNode node = (AdviceAopTargetBeanNode) element;
			IBean bean = node.getTargetBean();
			calculateDecorationForBean(decoration, bean);
		}
	}

	private void calculateDecorationForBean(IDecoration decoration, IBean bean) {
		// the check isn't very safe, but for now only the JavaConfig support
		// uses the DefaultModelSourceLocation and therefore it is enough to
		// check if the source location is of that type
		// TODO revisit
		if (bean.getElementSourceLocation() != null
				&& bean.getElementSourceLocation() instanceof DefaultModelSourceLocation) {
			decoration.addOverlay(JavaConfigUIImages.DESC_OVR_ANNOTATION,
					IDecoration.BOTTOM_LEFT);
		}
	}

	@Override
	public void dispose() {
		BeansCorePlugin.getModel().removeChangeListener(listener);
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public static final void update() {
		SpringUIUtils.getStandardDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				workbench.getDecoratorManager().update(DECORATOR_ID);
			}
		});
	}
}
