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
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonContentProvider;
import org.eclipse.ui.navigator.INavigatorContentExtension;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.xml.core.internal.document.ElementImpl;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.aop.core.model.IAopProject;
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
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.model.IModelChangeListener;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent;

/**
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class AopReferenceModelNavigatorContentProvider implements
		ICommonContentProvider, IModelChangeListener {

	@SuppressWarnings("unused")
	private INavigatorContentExtension contentExtension;

	private StructuredViewer viewer;

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (viewer instanceof StructuredViewer) {
			this.viewer = (StructuredViewer) viewer;

			if (oldInput == null && newInput != null) {
				BeansCorePlugin.getModel().addChangeListener(this);
			}
			else if (oldInput != null && newInput == null) {
				BeansCorePlugin.getModel().removeChangeListener(this);
			}
		}
		else {
			this.viewer = null;
		}
	}

	public void dispose() {
		if (viewer != null && viewer.getInput() != null) {
			BeansCorePlugin.getModel().removeChangeListener(this);
		}
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

	@SuppressWarnings("restriction")
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
				IMethod[] methods = type.getMethods();
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
					.getAllReferences(type.getJavaProject());

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
			Set<IBean> beans = new HashSet<IBean>();
			for (IBeansConfig config : configs) {
				List<IBean> pBeans = new ArrayList<IBean>();
				pBeans.addAll(config.getBeans());
				pBeans.addAll(BeansModelUtils.getInnerBeans(config));				
				for (IBean b : pBeans) {
					if (type.getFullyQualifiedName().equals(
							BeansModelUtils.getBeanClass(b, config))) {
						beans.add(b);
					}
				}
			}
			node.setBeans(beans);
			return new Object[] { node };
		}
		else if (parentElement instanceof IMethod
				&& parentElement instanceof SourceMethod) {
			IMethod method = (IMethod) parentElement;
			List<IAopReference> references = Activator.getModel()
					.getAllReferences(method.getJavaProject());
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
					.getAllReferences(method.getJavaProject());
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

			IResource resource = getResource(document);
			// check if resource is a Beans Config
			if (!BeansCoreUtils.isBeansConfig(resource)) {
				return nodes.toArray();
			}
			IBeansConfig beansConfig = BeansCorePlugin.getModel().getProject(
					resource.getProject()).getConfig((IFile) resource);

			int startLine = document.getLineOfOffset(element.getStartOffset()) + 1;
			int endLine = document.getLineOfOffset(element.getEndOffset()) + 1;
			String id = BeansEditorUtils.getAttribute(element, "id");

			nodes.addAll(getChildrenFromXmlLocation(resource, startLine,
					endLine, id, beansConfig.getBeans()));

			// add inner beans
			if (nodes.size() == 0) {
				nodes.addAll(getChildrenFromXmlLocation(resource, startLine,
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
		IAopProject project = Activator.getModel().getProject(
				AopReferenceModelUtils.getJavaProject(resource));
		List<IAopReference> references = new ArrayList<IAopReference>();
		if (project != null) {
			references = project.getReferencesForResource(resource);

		}
		Map<IAspectDefinition, List<IAopReference>> foundSourceReferences = new HashMap<IAspectDefinition, List<IAopReference>>();
		Map<IAspectDefinition, List<IAopReference>> foundIntroductionSourceReferences = new HashMap<IAspectDefinition, List<IAopReference>>();
		Map<IBean, List<IAopReference>> foundTargetReferences = new HashMap<IBean, List<IAopReference>>();
		Map<IBean, List<IAopReference>> foundIntroductionTargetReferences = new HashMap<IBean, List<IAopReference>>();

		for (IAopReference reference : references) {
			if (reference.getDefinition().getAspectName().equals(id)
					|| (reference.getDefinition().getAspectLineNumber() >= startLine
							&& reference.getDefinition().getAspectLineNumber() <= endLine && resource
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
			if (reference.getDefinition().getAspectName().equals(id)
					|| (reference.getTargetBean().getElementStartLine() >= startLine
							&& reference.getTargetBean().getElementEndLine() <= endLine && resource
							.equals(reference.getResource()))) {
				if (reference.getAdviceType() == ADVICE_TYPES.DECLARE_PARENTS) {
					if (foundIntroductionTargetReferences.containsKey(reference
							.getTargetBean())) {
						foundIntroductionTargetReferences.get(
								reference.getTargetBean()).add(reference);
					}
					else {
						List<IAopReference> tmp = new ArrayList<IAopReference>();
						tmp.add(reference);
						foundIntroductionTargetReferences.put(reference
								.getTargetBean(), tmp);
					}
				}
				else {
					if (foundTargetReferences.containsKey(reference
							.getTargetBean())) {
						foundTargetReferences.get(reference.getTargetBean())
								.add(reference);
					}
					else {
						List<IAopReference> tmp = new ArrayList<IAopReference>();
						tmp.add(reference);
						foundTargetReferences.put(reference.getTargetBean(),
								tmp);
					}
				}
			}
		}

		// add normal beans
		Map<IBean, BeanReferenceNode> beansRefs = new HashMap<IBean, BeanReferenceNode>();
		for (IBean bean : beans) {
			if (bean.getElementStartLine() >= startLine
					&& bean.getElementEndLine() <= endLine) {
				BeanReferenceNode rn = new BeanReferenceNode(bean);
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
							.getAspectLineNumber()
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
							.getAspectLineNumber()
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

	private IResource getResource(IStructuredDocument document) {
		IStructuredModel model = StructuredModelManager.getModelManager()
				.getModelForRead(document);
		IResource resource = null;
		try {
			String baselocation = model.getBaseLocation();
			if (baselocation != null) {
				// copied from JSPTranslationAdapter#getJavaProject
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				IPath filePath = new Path(baselocation);
				if (filePath.segmentCount() > 0) {
					resource = root.getFile(filePath);
				}
			}
		}
		finally {
			if (model != null) {
				model.releaseFromRead();
			}
		}
		return resource;
	}

	public Object getParent(Object element) {
		if (element instanceof IModelElement) {
			return ((IModelElement) element).getElementParent();
		}
		else if (element instanceof IFile) {
			return BeansCorePlugin.getModel().getConfig((IFile) element)
					.getElementParent();
		}
		if (element instanceof ZipEntryStorage) {
			return BeansCorePlugin.getModel().getConfig(
					((ZipEntryStorage) element).getFullName())
					.getElementParent();
		}
		return null;
	}

	@SuppressWarnings("restriction")
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
					.getAllReferences(method.getJavaProject());
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

	public void elementChanged(ModelChangeEvent event) {
		IModelElement element = event.getElement();
		if (element instanceof IBeansConfig) {
			IBeansConfig config = (IBeansConfig) element;
			Set<String> classes = config.getBeanClasses();
			for (String clz : classes) {
				IType type = BeansModelUtils.getJavaType(config
						.getElementResource().getProject(), clz);
				if (type != null && type instanceof SourceType) {
					// refreshViewer(type);
				}
			}
		}
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
		contentExtension = config.getExtension();
	}

	public void saveState(IMemento aMemento) {
	}

	public void restoreState(IMemento aMemento) {
	}
}
