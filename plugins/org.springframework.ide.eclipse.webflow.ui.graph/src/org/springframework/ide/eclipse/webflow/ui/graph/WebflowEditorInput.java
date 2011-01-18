/*******************************************************************************
 * Copyright (c) 2007, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.graph;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelXmlUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;

/**
 * {@link IEditorInput} implementation that takes care of locating the
 * {@link IWebflowConfig} for a given {@link IFile}.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class WebflowEditorInput implements IEditorInput, IPersistableElement {

	private IWebflowConfig config;

	private boolean isValid = true;

	private String name;

	private String tooltip;

	private Map<IDOMNode, Integer> nodesToLineNumbers;

	public WebflowEditorInput(IWebflowConfig config) {
		this.config = config;
		this.tooltip = config.getResource().getFullPath().makeRelative()
				.toString();
		this.name = config.getResource().getFullPath().makeRelative()
				.toString();
	}

	public boolean equals(Object obj) {
		if (obj instanceof WebflowEditorInput) {
			return ((WebflowEditorInput) obj).getFile().equals(this.getFile());
		}
		return false;
	}

	public boolean exists() {
		return true;
	}

	public Object getAdapter(Class adapter) {
		return this.config.getResource().getAdapter(adapter);
	}

	public IWebflowConfig getConfig() {
		return config;
	}

	public String getFactoryId() {
		return WebflowEditorInputFactory.getFactoryId();
	}

	public IFile getFile() {
		return this.config.getResource();
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		return name;
	}

	public IPersistableElement getPersistable() {
		return this;
	}

	public String getToolTipText() {
		return tooltip;
	}

	public int hashCode() {
		return this.getFile().hashCode();
	}

	public boolean isValid() {
		return isValid;
	}

	public void saveState(IMemento memento) {
		if (this.isValid) {
			WebflowEditorInputFactory.saveState(memento, this);
		}
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	public int getElementStartLine(IDOMNode node) {
		if (this.nodesToLineNumbers.containsKey(node)) {
			return this.nodesToLineNumbers.get(node);
		}
		else {
			return 1;
		}
	}

	public void initLineNumbers(IDOMNode root, IDOMNode clone) {
		this.nodesToLineNumbers = WebflowModelXmlUtils.getNodeLineNumbers(root,
				clone);
	}
}
