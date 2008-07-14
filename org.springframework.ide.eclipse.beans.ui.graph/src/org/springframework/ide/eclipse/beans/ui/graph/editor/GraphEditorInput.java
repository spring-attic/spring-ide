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
package org.springframework.ide.eclipse.beans.ui.graph.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConnection;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConnection.BeanType;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.graph.BeansGraphPlugin;
import org.springframework.ide.eclipse.beans.ui.graph.model.Bean;
import org.springframework.ide.eclipse.beans.ui.graph.model.ConstructorArgument;
import org.springframework.ide.eclipse.beans.ui.graph.model.IGraphContentExtender;
import org.springframework.ide.eclipse.beans.ui.graph.model.Property;
import org.springframework.ide.eclipse.beans.ui.graph.model.Reference;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.util.ObjectUtils;

/**
 * This editor input is used to specify the list of {@link IBean}s which should be displayed in the
 * beans graph editor. Therefore a source model model element ({@link IBean}>, {@link IBeanConfig}
 * or {@link IBeanConfigSet}) has to be specified. For a given bean it's parent bean (for child
 * beans only), constructor argument values and property values are checked. {@link IBean} look-up
 * is done from the specified context ({@link IBeanConfig} or {@link IBeanConfigSet}). This list of
 * beans is accessible via {@link #getBeans()}. This context used for bean look-up is accessible via
 * {@link #getContext()}.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class GraphEditorInput implements IEditorInput, IPersistableElement {

	private static final String CLASS_ATTRIBUTE = "class";

	private static final String GRAPH_CONTENT_EXTENDER_EXTENSION_POINT = BeansGraphPlugin.PLUGIN_ID
			+ ".graphContentExtender";

	private String elementId;

	private String contextId;

	private String name;

	private String toolTip;

	private Map<String, Bean> beans;

	private List<Reference> beanReferences;

	private boolean hasError;

	/**
	 * Creates a list with all beans which are referenced from the model element defined by given
	 * ID.
	 * @param elementID the model element's ID
	 * @throws IllegalArgumentException if unsupported model element is specified
	 */
	public GraphEditorInput(String elementId) {
		this(elementId, getContext(elementId));
	}

	/**
	 * Creates a list with all beans which are referenced from the model element defined by given
	 * ID.
	 * @param elementId the model element's ID
	 * @param contextId the context's ID
	 * @throws IllegalArgumentException if unsupported model element or context is specified
	 */
	public GraphEditorInput(String elementId, String contextId) {
		this.elementId = elementId;
		this.contextId = contextId;
		init();
	}

	protected void init() {
		IModelElement element = BeansCorePlugin.getModel().getElement(elementId);
		IModelElement context = BeansCorePlugin.getModel().getElement(contextId);

		// Prepare name and tooltip for given element and context
		if (element instanceof IBeansConfig) {
			String toolTipPrefix = BeansGraphPlugin
					.getResourceString("ShowGraphAction.name.config");
			IBeansConfig config = (IBeansConfig) element;
			IResource resource = config.getElementResource();
			if (resource != null) {
				if (config.isElementArchived()) {
					ZipEntryStorage storage = new ZipEntryStorage(config);
					name = storage.getName();
					toolTip = toolTipPrefix + storage.getFile().getProjectRelativePath().toString()
							+ " - " + storage.getFullPath().toString();
				}
				else {
					name = resource.getName();
					toolTip = toolTipPrefix + resource.getFullPath().toString();
				}
			}
			else {
				name = BeansGraphPlugin.getResourceString("ShowGraphAction.name.undefined");
				toolTip = BeansGraphPlugin.getResourceString("ShowGraphAction.name.config") + name;
			}
		}
		else if (element instanceof IBeansConfigSet) {
			IModelElement parent = ((IBeansConfigSet) element).getElementParent();
			name = element.getElementName();
			toolTip = BeansGraphPlugin.getResourceString("ShowGraphAction.name.configSet")
					+ parent.getElementName() + '/' + element.getElementName();
		}
		else if (element instanceof IBeansComponent || element instanceof IBean) {
			name = element.getElementName();
			StringBuffer buffer = new StringBuffer();
			if (element instanceof IBeansComponent) {
				buffer.append(BeansGraphPlugin.getResourceString("ShowGraphAction.name.component"));
			}
			else {
				buffer.append(BeansGraphPlugin.getResourceString("ShowGraphAction.name.bean"));
			}
			if (context instanceof IBeansConfig) {
				buffer.append(BeansGraphPlugin.getResourceString("ShowGraphAction.name.config"));
				buffer.append(context.getElementName());
				buffer.append(": ");
			}
			else if (context instanceof IBeansConfigSet) {
				buffer.append(BeansGraphPlugin.getResourceString("ShowGraphAction.name.configSet"));
				buffer.append(context.getElementParent().getElementName());
				buffer.append('/');
				buffer.append(context.getElementName());
				buffer.append(": ");
			}
			buffer.append(element.getElementName());
			toolTip = buffer.toString();
		}
		else {
			throw new IllegalArgumentException("Unsupported model element " + element);
		}
		createBeansMap();
		createReferences();
		extendGraphContent();
	}

	protected void extendGraphContent() {
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(
				GRAPH_CONTENT_EXTENDER_EXTENSION_POINT);
		if (point != null) {
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension.getConfigurationElements()) {
					if (config.getAttribute(CLASS_ATTRIBUTE) != null) {
						try {
							Object provider = config.createExecutableExtension(CLASS_ATTRIBUTE);
							if (provider instanceof IGraphContentExtender) {
								((IGraphContentExtender) provider).addAdditionalBeans(getBeans(),
										getBeansReferences(),
										(IBeansModelElement) getElement(elementId),
										(IBeansModelElement) getElement(contextId));
							}
						}
						catch (CoreException e) {
							BeansGraphPlugin.log(e);
						}
					}
				}
			}
		}
	}

	/**
	 * Creates a list with all beans belonging to the specified config / config set or being
	 * connected with the specified bean.
	 */
	protected void createBeansMap() {
		Set<IBean> list = new LinkedHashSet<IBean>();
		if (getElement(elementId) instanceof IBeansConfig) {
			IBeansConfig bc = (IBeansConfig) getElement(elementId);
			list.addAll(bc.getBeans());
			// add component registered beans
			addBeansFromComponents(list, bc.getComponents());
		}
		else if (getElement(elementId) instanceof IBeansConfigSet) {
			IBeansConfigSet bcs = (IBeansConfigSet) getElement(elementId);
			list.addAll(bcs.getBeans());
			// add component registered beans
			addBeansFromComponents(list, bcs.getComponents());
		}
		else if (getElement(elementId) instanceof IBean) {
			list.add((IBean) getElement(elementId));
			for (BeansConnection beanRef : BeansModelUtils.getBeanReferences(getElement(elementId),
					getElement(contextId), true)) {
				if (beanRef.getType() != BeanType.INNER) {
					list.add(beanRef.getTarget());
				}
			}
		}

		// Marshall all beans into a graph bean node
		beans = new LinkedHashMap<String, Bean>();
		for (IBean bean : list) {
			if (shouldAddBean(bean)) {
				beans.put(bean.getElementName(), new Bean(bean));
			}
		}
	}

	private boolean shouldAddBean(IBean bean) {
		return !bean.isInfrastructure()
				|| (bean.isInfrastructure() && BeansUIPlugin.getDefault().getPluginPreferences()
						.getBoolean(BeansUIPlugin.SHOULD_SHOW_INFRASTRUCTURE_BEANS_PREFERENCE_ID));
	}

	private void addBeansFromComponents(Set<IBean> beans, Set<IBeansComponent> components) {
		for (IBeansComponent component : components) {
			Set<IBean> nestedBeans = component.getBeans();
			for (IBean nestedBean : nestedBeans) {
				if (shouldAddBean(nestedBean)) {
					beans.add(nestedBean);
				}
			}
			addBeansFromComponents(beans, component.getComponents());
		}
	}

	protected void createReferences() {
		beanReferences = new ArrayList<Reference>();
		// Add all beans defined in GraphEditorInput as nodes to the graph
		Iterator beans = this.beans.values().iterator();
		while (beans.hasNext()) {
			Bean bean = (Bean) beans.next();

			// Add all beans references from bean (parent, factory or
			// depends-on beans) to list of graph edges
			Iterator beanRefs = BeansModelUtils.getBeanReferences(bean.getBean(),
					BeansCorePlugin.getModel().getElement(getContextId()), false).iterator();
			while (beanRefs.hasNext()) {
				BeansConnection beanRef = (BeansConnection) beanRefs.next();
				Bean targetBean = this.beans.get(beanRef.getTarget().getElementName());
				if (targetBean != null && targetBean != bean
						&& beanRef.getSource() instanceof IBean) {
					beanReferences.add(new Reference(beanRef.getType(), bean, targetBean, bean,
							beanRef.isInner()));
				}
			}

			// Add all bean references in bean's constructor arguments to list
			// of graph edges
			ConstructorArgument[] cargs = bean.getConstructorArguments();
			for (ConstructorArgument carg : cargs) {
				Iterator cargRefs = BeansModelUtils.getBeanReferences(
						carg.getBeanConstructorArgument(),
						BeansCorePlugin.getModel().getElement(getContextId()), false).iterator();
				while (cargRefs.hasNext()) {
					BeansConnection beanRef = (BeansConnection) cargRefs.next();
					Bean targetBean = this.beans.get(beanRef.getTarget().getElementName());
					if (targetBean != null && targetBean != bean) {
						beanReferences.add(new Reference(beanRef.getType(), bean, targetBean, carg,
								beanRef.isInner()));
					}
				}
			}

			// Add all bean references in properties to list of graph edges
			Property[] properties = bean.getProperties();
			for (Property property : properties) {
				Iterator propRefs = BeansModelUtils.getBeanReferences(property.getBeanProperty(),
						BeansCorePlugin.getModel().getElement(getContextId()), false).iterator();
				while (propRefs.hasNext()) {
					BeansConnection beanRef = (BeansConnection) propRefs.next();
					Bean targetBean = this.beans.get(beanRef.getTarget().getElementName());
					if (targetBean != null && targetBean != bean) {
						beanReferences.add(new Reference(beanRef.getType(), bean, targetBean,
								property, beanRef.isInner()));
					}
				}
			}
		}
	}

	private IModelElement getElement(String elementId) {
		return BeansCorePlugin.getModel().getElement(elementId);
	}

	public String getName() {
		return name;
	}

	public String getElementId() {
		return elementId;
	}

	public String getContextId() {
		return contextId;
	}

	public Map<String, Bean> getBeans() {
		return beans;
	}

	public List<Reference> getBeansReferences() {
		return beanReferences;
	}

	public void setHasError(boolean hasError) {
		this.hasError = hasError;
	}

	public Object getAdapter(Class adapter) {
		if (adapter == IModelElement.class) {
			return getElement(elementId);
		}
		return null;
	}

	public boolean exists() {
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		return BeansUIImages.DESC_OBJS_SPRING;
	}

	public IPersistableElement getPersistable() {
		return (hasError ? null : this);
	}

	public String getToolTipText() {
		return toolTip;
	}

	public String getFactoryId() {
		return GraphEditorInputFactory.getFactoryId();
	}

	public void saveState(IMemento memento) {
		GraphEditorInputFactory.saveState(memento, this);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof GraphEditorInput)) {
			return false;
		}
		GraphEditorInput that = (GraphEditorInput) other;
		if (!ObjectUtils.nullSafeEquals(this.elementId, that.elementId))
			return false;
		return ObjectUtils.nullSafeEquals(this.contextId, that.contextId);
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(elementId);
		return 29 * hashCode + ObjectUtils.nullSafeHashCode(contextId);
	}

	private static IModelElement getContext(IModelElement element) {
		if (element instanceof IBean) {
			return element.getElementParent();
		}
		else if (element instanceof IBeanConstructorArgument || element instanceof IBeanProperty) {
			return element.getElementParent().getElementParent();
		}
		return element;
	}

	private static String getContext(String elementId) {
		IModelElement element = BeansCorePlugin.getModel().getElement(elementId);
		return getContext(element).getElementID();
	}
}
