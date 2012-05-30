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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.metadata.model.AbstractAnnotationMetadata;
import org.springframework.ide.eclipse.beans.core.metadata.model.IBeanMetadata;
import org.springframework.ide.eclipse.beans.core.metadata.model.IMethodMetadata;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.model.metadata.BeanMetadataNode;
import org.springframework.ide.eclipse.beans.ui.model.metadata.BeanMetadataReference;
import org.springframework.ide.eclipse.beans.ui.model.metadata.IBeanMetadataContentProvider;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.metadata.MetadataUIImages;
import org.springframework.ide.eclipse.metadata.core.AspectAnnotationMetadata;
import org.springframework.ide.eclipse.metadata.core.AspectMethodAnnotationMetadata;
import org.springframework.ide.eclipse.metadata.core.AutowireAnnotationMetadata;
import org.springframework.ide.eclipse.metadata.core.BeanMethodAnnotationMetadata;
import org.springframework.ide.eclipse.metadata.core.ConfigurationAnnotationMetadata;
import org.springframework.ide.eclipse.metadata.core.RequestMappingAnnotationMetadata;
import org.springframework.ide.eclipse.metadata.core.TransactionalAnnotationMetadata;
import org.springframework.ide.eclipse.metadata.core.AspectMethodAnnotationMetadata.Type;
import org.springframework.util.StringUtils;


/**
 * {@link IBeanMetadataContentProvider} extension that knows about the different kind of contributed
 * {@link IBeanMetadata} and {@link IMethodMetadata}.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 1.0.0
 */
public class AnnotationMetadataContentProvider implements IBeanMetadataContentProvider {

	public Object[] getChildren(Object element) {
		// Transactional and AspectJ annotations have some special meaning on method level. so we
		// want to show something special.
		if (element instanceof TransactionalAnnotationMetadata || element instanceof AspectAnnotationMetadata
				|| element instanceof ConfigurationAnnotationMetadata) {
			String className = BeansModelUtils.getBeanClass(((AbstractAnnotationMetadata) element).getBean(), null);
			IType type = JdtUtils.getJavaType(((AbstractAnnotationMetadata) element).getBean().getElementResource()
					.getProject(), className);
			if (type != null) {
				BeanMetadataNode node = new BeanMetadataNode(type.getHandleIdentifier());
				node.setLabel(((AbstractAnnotationMetadata) element).getValueAsText()
						+ BeansUIPlugin.getLabelProvider().getText(type));
				node.setImage(BeansUIPlugin.getLabelProvider().getImage(type));
				node.setLocation(((AbstractAnnotationMetadata) element).getElementSourceLocation());
				Set<BeanMetadataNode> children = new HashSet<BeanMetadataNode>();
				for (IMethodMetadata method : ((AbstractAnnotationMetadata) element).getMethodMetaData()) {
					BeanMetadataNode child = new BeanMetadataNode(method.getHandleIdentifier());
					IJavaElement je = JavaCore.create(method.getHandleIdentifier());
					String label = method.getValueAsText();
					if (StringUtils.hasText(label)) {
						child.setLabel(label + " " + BeansUIPlugin.getLabelProvider().getText(je)); //$NON-NLS-1$
					}
					else {
						child.setLabel(BeansUIPlugin.getLabelProvider().getText(je));
					}

					// special handling for aspect method level annotation
					if (method instanceof AspectMethodAnnotationMetadata) {
						Type annotationType = ((AspectMethodAnnotationMetadata) method).getType();
						if (annotationType == Type.AFTER) {
							child.setImage(MetadataUIImages.getImage(MetadataUIImages.IMG_OBJS_AFTER_ADVICE));
						}
						else if (annotationType == Type.AROUND) {
							child.setImage(MetadataUIImages.getImage(MetadataUIImages.IMG_OBJS_AROUND_ADVICE));
						}
						else if (annotationType == Type.BEFORE) {
							child.setImage(MetadataUIImages.getImage(MetadataUIImages.IMG_OBJS_BEFORE_ADVICE));
						}
						else if (annotationType == Type.DECLARE_ANNOTATION) {
							child.setImage(MetadataUIImages.getImage(MetadataUIImages.IMG_OBJS_DECLARE_ANNOTATION));
						}
						else if (annotationType == Type.DECLARE_ERROR) {
							child.setImage(MetadataUIImages.getImage(MetadataUIImages.IMG_OBJS_DECLARE_ERROR));
						}
						else if (annotationType == Type.DECLARE_WARNING) {
							child.setImage(MetadataUIImages.getImage(MetadataUIImages.IMG_OBJS_DECLARE_WARNING));
						}
						else if (annotationType == Type.DECLARE_PARENTS) {
							child.setImage(MetadataUIImages.getImage(MetadataUIImages.IMG_OBJS_DECLARE_PARENTS));
						}
						else if (annotationType == Type.POINTCUT) {
							child.setImage(MetadataUIImages.getImage(MetadataUIImages.IMG_OBJS_POINTCUT));
						}
					}
					else if (method instanceof BeanMethodAnnotationMetadata) {
						child.setImage(MetadataUIImages.getImage(MetadataUIImages.IMG_OBJS_ANNOTATION_BEAN));
					}
					else {
						child.setImage(BeansUIPlugin.getLabelProvider().getImage(je));
					}
					child.setLocation(method.getElementSourceLocation());
					children.add(child);
				}
				node.setChildren(children.toArray());
				return new Object[] { node };
			}
		}
		else if (element instanceof AutowireAnnotationMetadata) {
			AutowireAnnotationMetadata metadata = (AutowireAnnotationMetadata) element;
			List<Object> children = new ArrayList<Object>();
			for (IMethodMetadata methodMetadata : metadata.getMethodMetaData()) {
				IJavaElement je = JavaCore.create(methodMetadata.getHandleIdentifier());
				BeanMetadataNode node = new BeanMetadataNode(methodMetadata.getHandleIdentifier());
				node.setLabel(methodMetadata.getValueAsText() + BeansUIPlugin.getLabelProvider().getText(je));
				node.setImage(BeansUIPlugin.getLabelProvider().getImage(je));
				node.setLocation(methodMetadata.getElementSourceLocation());
				children.add(node);
			}
			return children.toArray(new Object[children.size()]);
			
		}
		else if (element instanceof AbstractAnnotationMetadata
				&& ((AbstractAnnotationMetadata) element).getBean() != null) {
			String className = BeansModelUtils.getBeanClass(((AbstractAnnotationMetadata) element).getBean(), null);

			IType type = JdtUtils.getJavaType(((AbstractAnnotationMetadata) element).getBean().getElementResource()
					.getProject(), className);
			if (type != null) {
				BeanMetadataNode node = new BeanMetadataNode(type.getHandleIdentifier());
				node.setLabel(BeansUIPlugin.getLabelProvider().getText(type));
				node.setImage(BeansUIPlugin.getLabelProvider().getImage(type));
				node.setLocation(((AbstractAnnotationMetadata) element).getElementSourceLocation());
				return new Object[] { node };
			}
		}
		return IModelElement.NO_CHILDREN;
	}

	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	public boolean supports(Object object) {
		return object instanceof AbstractAnnotationMetadata;
	}

	public BeanMetadataReference getBeanMetadataReference(IBeanMetadata metadata, IBeansProject project) {
		if (metadata instanceof RequestMappingAnnotationMetadata) {
			return new RequestMappingBeanMetadataReference(project, metadata.getKey());
		}
		return new BeanMetadataReference(project, metadata.getKey());
	}

}