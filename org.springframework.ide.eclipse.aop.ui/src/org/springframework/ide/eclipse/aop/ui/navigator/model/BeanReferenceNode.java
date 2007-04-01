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
package org.springframework.ide.eclipse.aop.ui.navigator.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IType;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeanReferenceNode implements IReferenceNode,
		IRevealableReferenceNode {

	private IBean bean;

	private boolean showChildren = true;

	private List<IAopReference> aspectReferences = new ArrayList<IAopReference>();

	private List<IAopReference> adviseReferences = new ArrayList<IAopReference>();

	private List<IAopReference> declareParentReferences = new ArrayList<IAopReference>();

	private List<IAopReference> declaredOnReferences = new ArrayList<IAopReference>();

	private List<IReferenceNode> innerBeanNodes = new ArrayList<IReferenceNode>();

	public BeanReferenceNode(IBean bean, boolean showChildren) {
		this.bean = bean;
		this.showChildren = showChildren;

		IType type = BeansModelUtils.getJavaType(this.bean.getElementResource()
				.getProject(), BeansModelUtils.getBeanClass(this.bean,
				null));
		if (type != null) {
			List<IAopReference> references = Activator.getModel()
					.getAllReferences(type.getJavaProject());

			Set<IBean> innerBeans = BeansModelUtils.getInnerBeans(bean);

			Map<IBean, BeanReferenceNode> refs = new HashMap<IBean, BeanReferenceNode>();
			for (IBean innerBean : innerBeans) {
				BeanReferenceNode n = new BeanReferenceNode(innerBean, true);
				refs.put(innerBean, n);

				for (IAopReference r : references) {
					if (innerBean.equals(r.getTargetBean())) {
						refs.get(r.getTargetBean()).getAdviseReferences()
								.add(r);
					}
				}
			}
			for (Map.Entry<IBean, BeanReferenceNode> e : refs.entrySet()) {
				innerBeanNodes.add(e.getValue());
			}
		}
	}

	public BeanReferenceNode(IBean bean) {
		this(bean, true);
	}

	public int getLineNumber() {
		return this.bean.getElementStartLine();
	}

	public IResource getResource() {
		return this.bean.getElementResource();
	}

	public void openAndReveal() {
		IResource resource = this.bean.getElementResource();
		SpringUIUtils.openInEditor((IFile) resource, this.bean
				.getElementStartLine());
	}

	public IReferenceNode[] getChildren() {
		if (BeansModelUtils.getBeanClass(bean, null) != null
				&& this.showChildren) {

			List<IReferenceNode> children = new ArrayList<IReferenceNode>();
			children.add(new BeanClassReferenceNode(
					new BeanClassTargetReferenceNode(BeansModelUtils
							.getJavaType(this.bean.getElementResource()
									.getProject(),
									BeansModelUtils.getBeanClass(bean, null)), this)));
			if (this.innerBeanNodes.size() > 0) {
				children.add(new InnerBeansReferenceNode(this.innerBeanNodes));
			}
			return children.toArray(new IReferenceNode[children.size()]);
		}
		else {
			return new IReferenceNode[0];
		}
	}

	public Image getImage() {
		return BeansUIPlugin.getLabelProvider().getImage(this.bean);
	}

	public String getText() {
		return BeansUIPlugin.getLabelProvider().getText(this.bean) + " - "
				+ this.bean.getElementResource().getFullPath().toString();
	}

	public boolean hasChildren() {
		return getChildren() != null && getChildren().length > 0;
	}

	public IBean getBean() {
		return bean;
	}

	public List<IAopReference> getAdviseReferences() {
		return adviseReferences;
	}

	public List<IAopReference> getAspectReferences() {
		return aspectReferences;
	}

	public List<IAopReference> getDeclaredOnReferences() {
		return declaredOnReferences;
	}

	public List<IAopReference> getDeclareParentReferences() {
		return declareParentReferences;
	}
}
