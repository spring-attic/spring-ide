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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.beans.PropertyValue;
import org.springframework.core.io.Resource;
import org.springframework.ide.eclipse.beans.core.internal.model.BeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.core.io.FileResource;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.w3c.dom.Node;


/**
 * @author Terry Denney
 */
public class BeanPropertyHelper extends BeanProperty implements IBeanProperty {

	private static String getElementName(IDOMNode n) {
		Node id = n.getAttributes().getNamedItem(BeansSchemaConstants.ATTR_NAME);
		if (id != null) {
			return id.getNodeValue();
		}
		return null;
	}

	private static String getElementValue(IDOMNode n) {
		Node value = n.getAttributes().getNamedItem(BeansSchemaConstants.ATTR_VALUE);
		if (value != null) {
			return value.getNodeValue();
		}

		value = n.getAttributes().getNamedItem(BeansSchemaConstants.ATTR_REF);
		if (value != null) {
			return value.getNodeValue();
		}
		return null;
	}

	private final IBean parentBean;

	private final IDOMNode propNode;

	private final IFile file;

	public BeanPropertyHelper(IDOMNode propNode, IFile file, IBean parentBean) {
		super(parentBean, new PropertyValue(getElementName(propNode), getElementValue(propNode)));
		this.parentBean = parentBean;
		this.file = file;
		this.propNode = propNode;

		setElementSourceLocation(new IModelSourceLocation() {

			public int getEndLine() {
				// TODO Auto-generated method stub
				return 0;
			}

			public Resource getResource() {
				return new FileResource(BeanPropertyHelper.this.file);
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

	@Override
	public IModelElement[] getElementChildren() {
		// TODO Auto-generated method stub
		return new IModelElement[0];
	}

	@Override
	public String getElementName() {
		return getElementName(propNode);
	}

	@Override
	public IModelElement getElementParent() {
		return parentBean;
	}

	@Override
	public IResource getElementResource() {
		return file;
	}

	@Override
	public IResourceModelElement getElementSourceElement() {
		return parentBean;
	}

	@Override
	public int getElementType() {
		return IBeansModelElementTypes.PROPERTY_TYPE;
	}

	@Override
	public Object getValue() {
		return getElementValue(propNode);
	}

}
