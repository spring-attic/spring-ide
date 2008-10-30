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
package org.springframework.ide.eclipse.beans.ui.editor.contentassist;

import org.eclipse.core.resources.IFile;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.w3c.dom.Node;

/**
 * Default implementation of {@link IContentAssistContext} that is used on the WTP XML editor
 * environment.
 * <p>
 * This implementation wraps the WTP internal class {@link ContentAssistRequest}.
 * @author Christian Dupuis
 * @since 2.2.1
 */
@SuppressWarnings("restriction")
public class DefaultContentAssistContext implements IContentAssistContext {

	private final String attributeName;

	private final String matchString;

	private final ContentAssistRequest request;
	
	/**
	 * Creates a new {@link DefaultContentAssistContext}
	 */
	public DefaultContentAssistContext(ContentAssistRequest request, String attributeName,
			String matchString) {
		this.request = request;
		this.attributeName = attributeName;
		this.matchString = matchString;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getAttributeName() {
		return attributeName;
	}

	/**
	 * {@inheritDoc}
	 */
	public IFile getFile() {
		return BeansEditorUtils.getFile(request);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMatchString() {
		return matchString;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public Node getNode() {
		return request.getNode();
	}

	/**
	 * {@inheritDoc}
	 */
	public Node getParentNode() {
		return request.getParent();
	}

}
