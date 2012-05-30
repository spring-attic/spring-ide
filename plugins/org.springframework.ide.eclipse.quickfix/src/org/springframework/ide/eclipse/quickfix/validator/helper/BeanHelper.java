/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.quickfix.validator.helper;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.ChildBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.io.Resource;
import org.springframework.ide.eclipse.beans.core.internal.model.Bean;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanMethodOverride;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.beans.ui.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.internal.model.SpringProject;
import org.springframework.ide.eclipse.core.io.FileResource;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @author Terry Denney
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @author Martin Lippert
 */
public class BeanHelper extends Bean implements IBean {

	private final IDOMNode beanNode;

	private final IFile file;

	private final SpringProject springProject;

	public BeanHelper(IDOMNode beanNode, IFile file, IProject project) {
		super(new SpringProject(SpringCore.getModel(), project), new BeanDefinitionHolder(getBeanDefinition(beanNode),
				getElementName(beanNode)));
		this.beanNode = beanNode;
		this.file = file;
		this.springProject = new SpringProject(SpringCore.getModel(), project);

		AbstractBeanDefinition beanDefinition = (AbstractBeanDefinition) getBeanDefinition();
		ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
		Set<IBeanConstructorArgument> constructorArgs = getConstructorArguments();
		int index = 0;
		for (IBeanConstructorArgument constructorArg : constructorArgs) {
			constructorArgumentValues.addIndexedArgumentValue(index, constructorArg.getValue());
			index++;
		}
		beanDefinition.setConstructorArgumentValues(constructorArgumentValues);

		MutablePropertyValues propertyValues = new MutablePropertyValues();
		Set<IBeanProperty> properties = getProperties();
		for (IBeanProperty property : properties) {
			propertyValues.add(property.getElementName(), property.getValue());
		}
		beanDefinition.setPropertyValues(propertyValues);

		setElementSourceLocation(new IModelSourceLocation() {

			public int getEndLine() {
				// TODO Auto-generated method stub
				return 0;
			}

			public Resource getResource() {
				return new FileResource(BeanHelper.this.file);
			}

			public int getStartLine() {
				// TODO Auto-generated method stub
				return 0;
			}
		});
	}

	@Override
	public void accept(IModelElementVisitor visitor, IProgressMonitor monitor) {
		if (!monitor.isCanceled()) {
			visitor.visit(this, monitor);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getAliases() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getClassName() {
		return getClassName(beanNode);
	}

	@Override
	public Set<IBeanConstructorArgument> getConstructorArguments() {
		Set<IBeanConstructorArgument> arguments = new HashSet<IBeanConstructorArgument>();
		NodeList childNodes = beanNode.getChildNodes();
		NamedNodeMap attrs = beanNode.getAttributes();

		int counter = 0;
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			String localName = child.getLocalName();
			if (localName != null && localName.equals(BeansSchemaConstants.ELEM_CONSTRUCTOR_ARG)) {
				String name = null;
				NamedNodeMap attributes = child.getAttributes();
				Node attribute = attributes.getNamedItem(BeansSchemaConstants.ATTR_NAME);
				if (attribute != null) {
					name = attribute.getNodeValue();
				}
				arguments.add(new BeanConstructorArgumentHelper(counter, name, (IDOMNode) child, file, this));
				counter++;
			}
		}

		// If no <constructor-arg> children found, look for c: prefixed
		// attributes on the <bean>
		if (counter == 0 && arguments.isEmpty() && attrs != null) {
			for (int i = 0; i < attrs.getLength(); i++) {
				Node child = attrs.item(i);
				if (NamespaceUtils.C_NAMESPACE_URI.equals(child.getNamespaceURI())) {
					String localName = child.getLocalName();
					if (localName != null) {
						String name = null;
						if (localName.endsWith("-ref")) {
							name = localName.substring(0, localName.length() - 4);
						}
						else {
							name = localName;
						}
						arguments.add(new BeanConstructorArgumentHelper(counter, name, (IDOMNode) child, file, this));
						counter++;
					}
				}
			}
		}
		return arguments;
	}

	@Override
	public IModelElement[] getElementChildren() {
		return getConstructorArguments().toArray(new IModelElement[0]);
	}

	@Override
	public int getElementEndLine() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getElementName() {
		return getElementName(beanNode);
	}

	@Override
	public IResource getElementResource() {
		return file;
	}

	@Override
	public IResourceModelElement getElementSourceElement() {
		return springProject;
	}

	@Override
	public int getElementStartLine() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getElementType() {
		return IBeansModelElementTypes.BEAN_TYPE;
	}

	@Override
	public Set<IBeanMethodOverride> getMethodOverrides() {
		// TODO Auto-generated method stub
		return new HashSet<IBeanMethodOverride>();
	}

	@Override
	public String getParentName() {
		return getBeanDefinition().getParentName();
	}

	@Override
	public Set<IBeanProperty> getProperties() {
		Set<IBeanProperty> properties = new HashSet<IBeanProperty>();
		NodeList childNodes = beanNode.getChildNodes();

		int counter = 0;
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			String localName = child.getLocalName();
			if (localName != null && localName.equals(BeansSchemaConstants.ELEM_PROPERTY)) {
				properties.add(new BeanPropertyHelper((IDOMNode) child, file, this));
				counter++;
			}
		}
		return properties;
	}

	@Override
	public IBeanProperty getProperty(String name) {
		Set<IBeanProperty> properties = getProperties();
		for (IBeanProperty property : properties) {
			if (name != null && name.equals(property.getElementName())) {
				return property;
			}
		}
		return null;
	}

	@Override
	public boolean isAbstract() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChildBean() {
		return getParentName() != null;
	}

	@Override
	public boolean isElementArchived() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isExternal() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFactory() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isGeneratedElementName() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isInfrastructure() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isInnerBean() {
		Node parentNode = beanNode.getParentNode();
		if (parentNode != null) {
			String localName = parentNode.getLocalName();
			if (localName != null && localName.equals(BeansSchemaConstants.ELEM_BEANS)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isLazyInit() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRootBean() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSingleton() {
		// TODO Auto-generated method stub
		return false;
	}

	private static BeanDefinition getBeanDefinition(IDOMNode node) {
		AbstractBeanDefinition bd = null;
		if (BeansEditorUtils.getAttribute(node, "parent") != null) {
			bd = new ChildBeanDefinition(BeansEditorUtils.getAttribute(node, "parent"));
			bd.setBeanClassName(getClassName(node));
		}
		else {
			bd = new RootBeanDefinition(getClassName(node));
		}

		String factoryBean = BeansEditorUtils.getAttribute(node, "factory-bean");
		String factoryMethod = BeansEditorUtils.getAttribute(node, "factory-method");

		if (org.springframework.util.StringUtils.hasText(factoryBean)) {
			bd.setFactoryBeanName(factoryBean);
		}
		if (org.springframework.util.StringUtils.hasText(factoryMethod)) {
			bd.setFactoryMethodName(factoryMethod);
		}

		return bd;
	}

	private static String getClassName(IDOMNode node) {
		return BeansEditorUtils.getClassNameForBean(node);
	}

	protected static String getElementName(IDOMNode n) {
		NamedNodeMap attributes = n.getAttributes();
		if (attributes != null) {
			Node id = attributes.getNamedItem(BeansSchemaConstants.ATTR_NAME);
			if (id != null) {
				return id.getNodeValue();
			}
			else {
				id = attributes.getNamedItem(BeansSchemaConstants.ATTR_ID);
				if (id != null) {
					return id.getNodeValue();
				}
			}
		}
		// TODO: figure out what to do when there's no name
		return "beanid";
	}

}
