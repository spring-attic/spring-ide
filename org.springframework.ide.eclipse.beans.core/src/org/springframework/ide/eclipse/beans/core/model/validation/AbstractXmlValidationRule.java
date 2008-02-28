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
package org.springframework.ide.eclipse.beans.core.model.validation;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.document.DOMModelImpl;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * {@link IValidationRule} implementation that allows to validate on the raw XML
 * content in the context of the bean validation request.
 * @author Christian Dupuis
 * @since 2.0.4
 * @see #supports(Node)
 * @see #validate(Node, IBeansValidationContext)
 */
@SuppressWarnings("restriction")
public abstract class AbstractXmlValidationRule implements
		IValidationRule<IBeansConfig, IBeansValidationContext> {

	/**
	 * This rule support <strong>only</strong> {@link IBeansConfig} elements as
	 * this is the model element representing a actual file.
	 */
	public final boolean supports(IModelElement element, IValidationContext context) {
		return element instanceof IBeansConfig;
	}

	/**
	 * Validates the {@link IBeansConfig} by creating a {@link DomVisitor} and
	 * visiting the entire dom model.
	 * <p>
	 * Every element will be visited and depending on the return of
	 * {@link #supports(Node)} the
	 * {@link #validate(Node, IBeansValidationContext)} will be called for the
	 * node.
	 * @see #supports(Node)
	 * @see #validate(Node, IBeansValidationContext)
	 */
	public final void validate(IBeansConfig element, final IBeansValidationContext context,
			IProgressMonitor monitor) {
		IStructuredModel model = null;
		try {
			model = StructuredModelManager.getModelManager().getExistingModelForRead(
					element.getElementResource());
			if (model == null) {
				model = StructuredModelManager.getModelManager().getModelForRead(
						(IFile) element.getElementResource());
			}
			if (model != null) {
				Document document = ((DOMModelImpl) model).getDocument();
				if (document != null && document.getDocumentElement() != null) {
					DomVisitor visitor = new DomVisitor() {

						@Override
						public void validateNode(Node n) {
							validate(n, context);
						}

						@Override
						public boolean supportsNode(Node n) {
							return supports(n);
						}

					};
					visitor.visit(document);
				}
			}
		}
		catch (IOException e) {
			BeansCorePlugin.log(e);
		}
		catch (CoreException e) {
			BeansCorePlugin.log(e);
		}
		finally {
			if (model != null) {
				model.releaseFromRead();
			}
		}
	}

	/**
	 * Returns <code>true</code> if given {@link Node n} is supported to be
	 * validated.
	 * @param n the node to validate
	 * @return true if validation should be called
	 */
	protected abstract boolean supports(Node n);
	
	/**
	 * Validates the given {@link Node n}
	 * @param n the node to validate
	 * @param context the current validation context
	 */
	protected abstract void validate(Node n, IBeansValidationContext context);

	/**
	 * Internal visitor implementation that visits an entire {@link Document}.
	 */
	private abstract static class DomVisitor {

		public void visit(Document dom) {
			if (supportsNode(dom)) {
				validateNode(dom);
			}
			visit(dom.getDocumentElement());
		}

		public void visit(Element e) {
			NodeList lst = e.getChildNodes();
			int len = lst.getLength();
			for (int i = 0; i < len; i++) {
				Node n = lst.item(i);
				if (supportsNode(n)) {
					validateNode(n);
				}
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					visit((Element) n);
				}
			}
		}

		public abstract void validateNode(Node n);

		public abstract boolean supportsNode(Node n);

	}

}
