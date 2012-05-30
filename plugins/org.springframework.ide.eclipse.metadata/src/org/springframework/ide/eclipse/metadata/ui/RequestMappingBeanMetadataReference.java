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
package org.springframework.ide.eclipse.metadata.ui;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.springframework.ide.eclipse.beans.core.metadata.model.IMethodMetadata;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.model.metadata.BeanMetadataNode;
import org.springframework.ide.eclipse.beans.ui.model.metadata.BeanMetadataReference;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.metadata.core.RequestMappingAnnotationMetadata;
import org.springframework.ide.eclipse.metadata.core.RequestMappingMethodAnnotationMetadata;
import org.springframework.util.ObjectUtils;


/**
 * {@link BeanMetadataReference} specifically tailored of {@link RequestMappingAnnotationMetadata}.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @author Martin Lippert
 * @since 1.0.0
 */
public class RequestMappingBeanMetadataReference extends BeanMetadataReference {

	public RequestMappingBeanMetadataReference(IBeansProject project, String key) {
		super(project, key);
	}

	@Override
	public Object[] getChildren() {
		Set<BeanMetadataNode> nodes = new LinkedHashSet<BeanMetadataNode>();

		Object[] children = super.getChildren();
		for (Object child : children) {
			if (child instanceof RequestMappingAnnotationMetadata) {
				Set<BeanMetadataNode> childNodes = nodes;
				BeanMetadataNode parent = null;
				RequestMappingAnnotationMetadata metadata = (RequestMappingAnnotationMetadata) child;

				// Check if there is a class level RequestMapping defined. If so install a class
				// level meta data node.
				if (metadata.getValue() != null) {
					parent = new BeanMetadataNode(metadata.getHandleIdentifier());
					IType type = (IType) JavaCore.create(metadata.getClassHandle());
					parent.setLabel(metadata.getValueAsText()
							+ BeansUIPlugin.getLabelProvider().getText(type));
					parent.setImage(BeansUIPlugin.getLabelProvider().getImage(type));
					parent.setLocation(metadata.getElementSourceLocation());
					nodes.add(parent);
					childNodes = new LinkedHashSet<BeanMetadataNode>();
				}

				// Install a meta data node for every method level RequestMappings
				for (IMethodMetadata mmd : metadata.getMethodMetaData()) {
					RequestMappingMethodAnnotationMetadata requestMapping = (RequestMappingMethodAnnotationMetadata) mmd;
					
					IMethod method = (IMethod) JdtUtils.getByHandle(requestMapping.getMethodHandle());
					BeanMetadataNode node = new BeanMetadataNode(requestMapping.getMethodHandle());
					node.setLabel(requestMapping.getValueAsText()
							+ BeansUIPlugin.getLabelProvider().getText(method.getDeclaringType())
							+ "." + BeansUIPlugin.getLabelProvider().getText(method)); //$NON-NLS-1$
					node.setImage(BeansUIPlugin.getLabelProvider().getImage(method));
					node.setLocation(requestMapping.getElementSourceLocation());
					childNodes.add(node);
				}

				if (parent != null) {
					parent.setChildren(childNodes.toArray());
				}
			}
		}
		return nodes.toArray();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof RequestMappingBeanMetadataReference)) {
			return false;
		}
		RequestMappingBeanMetadataReference that = (RequestMappingBeanMetadataReference) other;
		return ObjectUtils.nullSafeEquals(this.beansProject, that.beansProject);
	}

	@Override
	public int hashCode() {
		return 33 * this.beansProject.hashCode();
	}
}