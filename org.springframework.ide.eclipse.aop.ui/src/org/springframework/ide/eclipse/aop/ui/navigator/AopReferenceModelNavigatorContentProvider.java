/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.ui.navigator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonContentProvider;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.xml.core.internal.document.ElementImpl;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.aop.core.internal.model.AopReferenceModel;
import org.springframework.ide.eclipse.aop.core.internal.model.builder.AopReferenceModelBuilder;
import org.springframework.ide.eclipse.aop.core.model.IAopModelChangedListener;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;
import org.springframework.ide.eclipse.aop.core.util.AopReferenceModelUtils;
import org.springframework.ide.eclipse.aop.ui.navigator.model.AdviceDeclareParentAopSourceNode;
import org.springframework.ide.eclipse.aop.ui.navigator.model.BeanReferenceNode;
import org.springframework.ide.eclipse.aop.ui.navigator.model.ClassMethodReferenceNode;
import org.springframework.ide.eclipse.aop.ui.navigator.model.IReferenceNode;
import org.springframework.ide.eclipse.aop.ui.navigator.model.JavaElementReferenceNode;
import org.springframework.ide.eclipse.aop.ui.navigator.model.MethodBeanReferenceNode;
import org.springframework.ide.eclipse.aop.ui.navigator.model.MethodReference;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.model.IModelChangeListener;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent.Type;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * {@link ICommonContentProvider} that contributes elements from the
 * {@link AopReferenceModel} created by {@link AopReferenceModelBuilder}.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class AopReferenceModelNavigatorContentProvider implements
		ICommonContentProvider, IAopModelChangedListener, IModelChangeListener {

	private StructuredViewer viewer;

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (viewer instanceof StructuredViewer) {
			this.viewer = (StructuredViewer) viewer;
		}
		else {
			this.viewer = null;
		}
	}

	public void dispose() {
		if (viewer != null && viewer.getInput() != null) {
			this.viewer = null;
		}
		Activator.getModel().unregisterAopModelChangedListener(this);
		BeansCorePlugin.getModel().removeChangeListener(this);
	}

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof IType) {
			return getChildren(inputElement);
		}
		else if (inputElement instanceof IMethod) {
			return getChildren(inputElement);
		}
		else if (inputElement instanceof IField) {
			return getChildren(inputElement);
		}
		else if (inputElement instanceof JavaElementReferenceNode) {
			return getChildren(((JavaElementReferenceNode) inputElement)
					.getJavaElement());
		}
		else if (inputElement instanceof ElementImpl) {
			return getChildren(inputElement);
		}
		return IModelElement.NO_CHILDREN;
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IReferenceNode) {
			return ((IReferenceNode) parentElement).getChildren();
		}
		else if (parentElement instanceof JavaElementReferenceNode
				&& ((JavaElementReferenceNode) parentElement).isRoot()) {
			return getChildren(((JavaElementReferenceNode) parentElement)
					.getJavaElement());
		}
		else if (parentElement instanceof IType) {
			IType type = (IType) parentElement;
			List<Object> me = new ArrayList<Object>();
			try {
				Set<IMethod> methods = Introspector.getAllMethods(type);
				for (IMethod method : methods) {
					if (Activator.getModel().isAdvice(method)
							|| Activator.getModel().isAdvised(method)) {
						me.addAll(Arrays.asList(getChildren(method)));
					}
				}
			}
			catch (JavaModelException e) {
			}
			ClassMethodReferenceNode node = new ClassMethodReferenceNode(type,
					me);

			List<IAopReference> references = Activator.getModel()
					.getAllReferences();

			// fields
			try {
				for (IField field : type.getFields()) {
					Object[] obj = getChildren(field);
					if (obj != null && obj.length > 0) {
						for (Object o : obj) {
							node.getDeclareParentReferences().add(
									(IReferenceNode) o);
						}
					}
				}
			}
			catch (JavaModelException e) {
			}

			for (IAopReference reference : references) {
				if (reference.getAdviceType() == ADVICE_TYPES.DECLARE_PARENTS) {
					if (reference.getTarget().equals(type)) {
						node.getDeclaredOnReferences().add(reference);
					}
				}
			}
			// add bean references
			Set<IBeansConfig> configs = BeansCorePlugin.getModel().getConfigs(
					type.getFullyQualifiedName());
			Set<String> beans = new HashSet<String>();
			for (IBeansConfig config : configs) {
				List<IBean> pBeans = new ArrayList<IBean>();
				pBeans.addAll(config.getBeans());
				pBeans.addAll(BeansModelUtils.getInnerBeans(config));
				for (IBean b : pBeans) {
					if (type.getFullyQualifiedName().equals(
							BeansModelUtils.getBeanClass(b, config))) {
						beans.add(b.getElementID());
					}
				}
			}
			node.setBeans(beans);
			return new Object[] { node };
		}
		else if (parentElement instanceof IMethod) {
			IMethod method = (IMethod) parentElement;
			List<IAopReference> references = Activator.getModel()
					.getAllReferences();
			List<IAopReference> foundSourceReferences = new ArrayList<IAopReference>();
			List<IAopReference> foundTargetReferences = new ArrayList<IAopReference>();
			for (IAopReference reference : references) {
				if (reference.getTarget().equals(method)) {
					foundTargetReferences.add(reference);
				}
				if (reference.getSource() != null
						&& reference.getSource().equals(method)) {
					foundSourceReferences.add(reference);
				}
			}
			List<IReferenceNode> nodes = new ArrayList<IReferenceNode>();
			Map<IMember, MethodReference> refs = new HashMap<IMember, MethodReference>();
			for (IAopReference reference : foundSourceReferences) {
				if (refs.containsKey(reference.getSource())) {
					refs.get(reference.getSource()).getAspects().add(reference);
				}
				else {
					MethodReference r = new MethodReference();
					r.setMember(reference.getSource());
					r.getAspects().add(reference);
					refs.put(reference.getSource(), r);
				}
			}
			for (IAopReference reference : foundTargetReferences) {
				if (refs.containsKey(reference.getTarget())) {
					refs.get(reference.getTarget()).getAdvices().add(reference);
				}
				else {
					MethodReference r = new MethodReference();
					r.setMember(reference.getTarget());
					r.getAdvices().add(reference);
					refs.put(reference.getTarget(), r);
				}
			}
			for (Map.Entry<IMember, MethodReference> entry : refs.entrySet()) {
				nodes
						.add(new MethodBeanReferenceNode(entry.getKey(), entry
								.getValue().getAspects(), entry.getValue()
								.getAdvices()));
			}
			return nodes.toArray();
		}
		else if (parentElement instanceof IField
				&& parentElement instanceof SourceField) {
			IField method = (IField) parentElement;
			List<IAopReference> references = Activator.getModel()
					.getAllReferences();
			List<IAopReference> foundSourceReferences = new ArrayList<IAopReference>();
			for (IAopReference reference : references) {
				if (reference.getSource() != null
						&& reference.getSource().equals(method)) {
					foundSourceReferences.add(reference);
				}
			}
			List<IReferenceNode> nodes = new ArrayList<IReferenceNode>();
			Map<IMember, List<IAopReference>> refs = new HashMap<IMember, List<IAopReference>>();
			for (IAopReference reference : foundSourceReferences) {
				if (refs.containsKey(reference.getSource())) {
					refs.get(reference.getSource()).add(reference);
				}
				else {
					List<IAopReference> f = new ArrayList<IAopReference>();
					f.add(reference);
					refs.put(reference.getSource(), f);
				}
			}
			for (Map.Entry<IMember, List<IAopReference>> entry : refs
					.entrySet()) {
				nodes
						.add(new AdviceDeclareParentAopSourceNode(entry
								.getValue()));
			}
			return nodes.toArray();
		}
		else if (parentElement instanceof ElementImpl) {
			ElementImpl element = (ElementImpl) parentElement;
			IStructuredDocument document = element.getStructuredDocument();
			List<IReferenceNode> nodes = new ArrayList<IReferenceNode>();

			IResource resource = SpringUIUtils.getFile(document);
			// check if resource is a Beans Config
			if (!BeansCoreUtils.isBeansConfig(resource, true) || document == null) {
				return nodes.toArray();
			}
			IBeansConfig beansConfig = BeansCorePlugin.getModel().getConfig((IFile) resource, true);
			int startLine = document.getLineOfOffset(element.getStartOffset()) + 1;
			int endLine = document.getLineOfOffset(element.getEndOffset()) + 1;
			String id = BeansEditorUtils.getAttribute(element, "id");

			nodes.addAll(getChildrenFromXmlLocation(resource, startLine,
					endLine, id, beansConfig.getBeans()));

			// add inner beans
			if (nodes.size() == 0) {
				nodes
						.addAll(getChildrenFromXmlLocation(resource, startLine,
								endLine, id, BeansModelUtils
										.getInnerBeans(beansConfig)));
			}

			return nodes.toArray();
		}
		return IModelElement.NO_CHILDREN;
	}

	private List<IReferenceNode> getChildrenFromXmlLocation(IResource resource,
			int startLine, int endLine, String id, Set<IBean> beans) {
		List<IReferenceNode> nodes = new ArrayList<IReferenceNode>();
		List<IAopReference> references = Activator.getModel()
				.getAllReferencesForResource(resource);

		Map<IAspectDefinition, List<IAopReference>> foundSourceReferences = new HashMap<IAspectDefinition, List<IAopReference>>();
		Map<IAspectDefinition, List<IAopReference>> foundIntroductionSourceReferences = new HashMap<IAspectDefinition, List<IAopReference>>();
		Map<IBean, List<IAopReference>> foundTargetReferences = new HashMap<IBean, List<IAopReference>>();
		Map<IBean, List<IAopReference>> foundIntroductionTargetReferences = new HashMap<IBean, List<IAopReference>>();

		for (IAopReference reference : references) {
			if (reference.getDefinition().getAspectName().equals(id)
					|| (reference.getDefinition().getAspectStartLineNumber() >= startLine
							&& reference.getDefinition().getAspectStartLineNumber() <= endLine && resource
							.equals(reference.getDefinition().getResource()))) {
				if (reference.getAdviceType() == ADVICE_TYPES.DECLARE_PARENTS) {
					if (foundIntroductionSourceReferences.containsKey(reference
							.getDefinition())) {
						foundIntroductionSourceReferences.get(
								reference.getDefinition()).add(reference);
					}
					else {
						List<IAopReference> tmp = new ArrayList<IAopReference>();
						tmp.add(reference);
						foundIntroductionSourceReferences.put(reference
								.getDefinition(), tmp);
					}

				}
				else {
					if (foundSourceReferences.containsKey(reference
							.getDefinition())) {
						foundSourceReferences.get(reference.getDefinition())
								.add(reference);
					}
					else {
						List<IAopReference> tmp = new ArrayList<IAopReference>();
						tmp.add(reference);
						foundSourceReferences.put(reference.getDefinition(),
								tmp);
					}
				}
			}
			IBean targetBean = AopReferenceModelUtils
					.getBeanFromElementId(reference.getTargetBeanId());
			if (targetBean != null) {
				if (reference.getDefinition().getAspectName().equals(id)
						|| (targetBean.getElementStartLine() >= startLine
								&& targetBean.getElementEndLine() <= endLine && resource
								.equals(targetBean.getElementResource()))) {
					if (reference.getAdviceType() == ADVICE_TYPES.DECLARE_PARENTS) {
						if (foundIntroductionTargetReferences
								.containsKey(targetBean)) {
							foundIntroductionTargetReferences.get(targetBean)
									.add(reference);
						}
						else {
							List<IAopReference> tmp = new ArrayList<IAopReference>();
							tmp.add(reference);
							foundIntroductionTargetReferences.put(targetBean,
									tmp);
						}
					}
					else {
						if (foundTargetReferences.containsKey(targetBean)) {
							foundTargetReferences.get(targetBean)
									.add(reference);
						}
						else {
							List<IAopReference> tmp = new ArrayList<IAopReference>();
							tmp.add(reference);
							foundTargetReferences.put(targetBean, tmp);
						}
					}
				}
			}
		}

		// add normal beans
		Map<IBean, BeanReferenceNode> beansRefs = new HashMap<IBean, BeanReferenceNode>();
		for (IBean bean : beans) {
			if (bean.getElementStartLine() >= startLine
					&& bean.getElementEndLine() <= endLine
					&& BeansCorePlugin.getModel().getElement(
							bean.getElementID()) != null) {
				BeanReferenceNode rn = new BeanReferenceNode(bean
						.getElementID());
				nodes.add(rn);
				beansRefs.put(bean, rn);
			}
		}
		// add found references
		if (foundTargetReferences.size() > 0) {
			for (Map.Entry<IBean, List<IAopReference>> entry : foundTargetReferences
					.entrySet()) {
				if (beansRefs.containsKey(entry.getKey())) {
					beansRefs.get(entry.getKey()).getAdviseReferences().addAll(
							entry.getValue());
				}
			}
		}
		if (foundIntroductionTargetReferences.size() > 0) {
			for (Map.Entry<IBean, List<IAopReference>> entry : foundIntroductionTargetReferences
					.entrySet()) {
				if (beansRefs.containsKey(entry.getKey())) {
					beansRefs.get(entry.getKey()).getDeclaredOnReferences()
							.addAll(entry.getValue());
				}
			}
		}
		if (foundSourceReferences.size() > 0) {
			for (Map.Entry<IAspectDefinition, List<IAopReference>> entry : foundSourceReferences
					.entrySet()) {
				for (Map.Entry<IBean, BeanReferenceNode> n : beansRefs
						.entrySet()) {
					if (n.getKey().getElementStartLine() == entry.getKey()
							.getAspectStartLineNumber()
							|| entry.getKey().getAspectName().equals(
									n.getKey().getElementName())) {
						beansRefs.get(n.getKey()).getAspectReferences().addAll(
								entry.getValue());
					}
				}
			}
		}
		if (foundIntroductionSourceReferences.size() > 0) {
			for (Map.Entry<IAspectDefinition, List<IAopReference>> entry : foundIntroductionSourceReferences
					.entrySet()) {
				for (Map.Entry<IBean, BeanReferenceNode> n : beansRefs
						.entrySet()) {
					if (n.getKey().getElementStartLine() == entry.getKey()
							.getAspectStartLineNumber()
							|| entry.getKey().getAspectName().equals(
									n.getKey().getElementName())) {
						beansRefs.get(n.getKey()).getDeclareParentReferences()
								.addAll(entry.getValue());
					}
				}
			}
		}
		return nodes;
	}

	public Object getParent(Object element) {
		if (element instanceof IModelElement) {
			return ((IModelElement) element).getElementParent();
		}
		else if (element instanceof IFile) {
			IBeansConfig config = BeansCorePlugin.getModel().getConfig(
					(IFile) element);
			if (config != null) {
				return config.getElementParent();
			}
		}
		else if (element instanceof ZipEntryStorage) {
			IBeansConfig config = BeansCorePlugin.getModel().getConfig(
					((ZipEntryStorage) element).getFullName());
			if (config != null) {
				return config.getElementParent();
			}
		}
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof IReferenceNode) {
			return ((IReferenceNode) element).hasChildren();
		}
		else if (element instanceof IType) {
			IType type = (IType) element;
			try {
				IMethod[] methods = type.getMethods();
				for (IMethod method : methods) {
					if (Activator.getModel().isAdvised(method)
							|| Activator.getModel().isAdvice(method)) {
						return true;
					}
				}
			}
			catch (JavaModelException e) {
			}
		}
		else if (element instanceof IMethod) {
			IMethod method = (IMethod) element;
			List<IAopReference> references = Activator.getModel()
					.getAllReferences();
			for (IAopReference reference : references) {
				if (reference.getTarget().equals(method)) {
					return true;
				}
				if (reference.getSource().equals(method)) {
					return true;
				}
			}
		}
		return false;
	}

	protected void refreshViewer(final Object element) {
		if (viewer instanceof TreeViewer) {
			Control ctrl = viewer.getControl();

			// Are we in the UI thread?
			if (ctrl.getDisplay().getThread() == Thread.currentThread()) {
				AopReferenceModelNavigator.refreshViewer((TreeViewer) viewer,
						AopReferenceModelNavigator
								.calculateRootElement(element), element);
			}
			else {
				ctrl.getDisplay().asyncExec(new Runnable() {
					public void run() {

						// Abort if this happens after disposes
						Control ctrl = viewer.getControl();
						if (ctrl == null || ctrl.isDisposed()) {
							return;
						}
						AopReferenceModelNavigator
								.refreshViewer((TreeViewer) viewer,
										AopReferenceModelNavigator
												.calculateRootElement(element),
										element);
					}
				});
			}
		}
	}

	public void init(ICommonContentExtensionSite config) {
		Activator.getModel().registerAopModelChangedListener(this);
		BeansCorePlugin.getModel().addChangeListener(this);
	}

	public void saveState(IMemento aMemento) {
	}

	public void restoreState(IMemento aMemento) {
	}

	public void changed() {
		Object obj = viewer.getInput();
		refreshViewer(obj);
	}

	public void elementChanged(ModelChangeEvent event) {
		if (event.getType() == Type.CHANGED
				&& event.getSource() instanceof IBeansProject) {
			changed();
		}
	}
}
