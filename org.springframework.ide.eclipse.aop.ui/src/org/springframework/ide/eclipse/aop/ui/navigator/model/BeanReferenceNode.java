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
import org.springframework.ide.eclipse.aop.core.util.AopReferenceModelUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeanReferenceNode implements IReferenceNode,
		IRevealableReferenceNode {

	private String beanId;

	private boolean showChildren = true;

	private List<IAopReference> aspectReferences = new ArrayList<IAopReference>();

	private List<IAopReference> adviseReferences = new ArrayList<IAopReference>();

	private List<IAopReference> declareParentReferences = new ArrayList<IAopReference>();

	private List<IAopReference> declaredOnReferences = new ArrayList<IAopReference>();

	private List<IReferenceNode> innerBeanNodes = new ArrayList<IReferenceNode>();

	public BeanReferenceNode(String beanId, boolean showChildren) {
		this.beanId = beanId;
		this.showChildren = showChildren;

		IBean bean = AopReferenceModelUtils.getBeanFromElementId(beanId);

		IType type = BeansModelUtils.getBeanType(bean, null);
		if (type != null) {
			List<IAopReference> references = Activator.getModel()
					.getAllReferences();

			Set<IBean> innerBeans = BeansModelUtils.getInnerBeans(bean);

			Map<String, BeanReferenceNode> refs = new HashMap<String, BeanReferenceNode>();
			for (IBean innerBean : innerBeans) {
				BeanReferenceNode n = new BeanReferenceNode(innerBean
						.getElementID(), true);
				refs.put(innerBean.getElementID(), n);

				for (IAopReference r : references) {
					if (innerBean.getElementID().equals(r.getTargetBeanId())) {
						refs.get(r.getTargetBeanId()).getAdviseReferences()
								.add(r);
					}
				}
			}
			for (Map.Entry<String, BeanReferenceNode> e : refs.entrySet()) {
				innerBeanNodes.add(e.getValue());
			}
		}
	}

	public BeanReferenceNode(String bean) {
		this(bean, true);
	}

	public int getLineNumber() {
		IBean bean = AopReferenceModelUtils.getBeanFromElementId(this.beanId);
		if (bean != null) {
			return bean.getElementStartLine();
		}
		else {
			return -1;
		}
	}

	public IResource getResource() {
		IBean bean = AopReferenceModelUtils.getBeanFromElementId(this.beanId);
		if (bean != null) {
			return bean.getElementResource();
		}
		else {
			return null;
		}
	}

	public void openAndReveal() {
		IBean bean = AopReferenceModelUtils.getBeanFromElementId(this.beanId);
		if (bean != null) {
			IResource resource = bean.getElementResource();
			SpringUIUtils.openInEditor((IFile) resource, bean
					.getElementStartLine());
		}
	}

	public IReferenceNode[] getChildren() {
		IBean bean = AopReferenceModelUtils.getBeanFromElementId(this.beanId);
		if (bean != null && BeansModelUtils.getBeanClass(bean, null) != null
				&& this.showChildren) {

			List<IReferenceNode> children = new ArrayList<IReferenceNode>();
			children.add(new BeanClassReferenceNode(
					new BeanClassTargetReferenceNode(BeansModelUtils
							.getBeanType(bean, null), this)));
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
		IBean bean = getBean();
		if (bean != null) {
			return BeansUIPlugin.getLabelProvider().getImage(bean);
		}
		else {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_ERROR);
		}
	}

	public String getText() {
		IBean bean = getBean();
		if (bean != null) {
			return BeansUIPlugin.getLabelProvider().getText(bean) + " - "
					+ bean.getElementResource().getFullPath().toString();
		}
		else {
			return "<bean cannot be found>";
		}
	}

	public boolean hasChildren() {
		return getChildren() != null && getChildren().length > 0;
	}

	public IBean getBean() {
		return AopReferenceModelUtils.getBeanFromElementId(this.beanId);
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

	public Object getReferenceParticipant() {
		return getBean();
	}
}
