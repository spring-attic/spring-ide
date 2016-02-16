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
package org.springframework.ide.eclipse.config.graph.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.springframework.ide.eclipse.config.core.ConfigCoreUtils;
import org.springframework.ide.eclipse.config.core.extensions.PageAdaptersExtensionPointConstants;
import org.springframework.ide.eclipse.config.graph.ConfigGraphPlugin;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * A Structured activity is an activity whose execution is determined by some
 * internal structure.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public abstract class StructuredActivity extends Activity {

	static final long serialVersionUID = 1;

	private static int count;

	protected List<Activity> children = new ArrayList<Activity>();

	public StructuredActivity() {
		super();
	}

	public StructuredActivity(IDOMElement input, AbstractConfigGraphDiagram diagram) {
		super(input, diagram);
	}

	public void addChild(Activity child) {
		addChild(child, -1);
	}

	public void addChild(Activity child, int index) {
		if (index >= 0) {
			children.add(index, child);
		}
		else {
			children.add(child);
		}
		extendedAddChild(child, index);
		fireStructureChange(CHILDREN, child);
	}

	protected void extendedAddChild(Activity child, int index) {
		IDOMDocument document = (IDOMDocument) getInput().getOwnerDocument();
		IDOMElement childElem = child.getInput();
		if (childElem == null) {
			childElem = (IDOMElement) document.createElement(child.getInputName());
			childElem.setPrefix(ConfigCoreUtils.getPrefixForNamespaceUri(document, getDiagram().getNamespaceUri()));
		}

		IDOMModel model = document.getModel();
		if (model != null) {
			model.beginRecording(this);
			getInput().appendChild(childElem);
			getDiagram().getXmlProcessor().insertDefaultAttributes(childElem);
			formatter.formatNode(childElem);
			formatter.formatNode(childElem.getParentNode());
			model.endRecording(this);
		}
	}

	protected void extendedRemoveChild(Activity child) {
		IDOMDocument document = (IDOMDocument) getInput().getOwnerDocument();
		IDOMElement childElem = child.getInput();
		if (childElem != null) {
			IDOMModel model = document.getModel();
			Node parent = childElem.getParentNode();
			if (model != null && parent != null) {
				model.beginRecording(this);
				parent.removeChild(childElem);
				formatter.formatNode(parent);
				model.endRecording(this);
			}
		}
	}

	public List<Activity> getChildren() {
		return children;
	}

	protected List<Activity> getChildrenFromXml() {
		String defaultUri = getDiagram().getNamespaceUri();
		List<Activity> list = new ArrayList<Activity>();
		NodeList children = getInput().getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child instanceof IDOMElement) {
				IDOMElement childElem = (IDOMElement) child;
				if (defaultUri != null && defaultUri.equals(child.getNamespaceURI())) {
					getDiagram().getModelFactory().getChildrenFromXml(list, childElem, this);
				}
				else {
					for (IConfigurationElement config : getDiagram().getGraphicalEditor().getAdapterDefinitions()) {
						String uri = config.getAttribute(PageAdaptersExtensionPointConstants.ATTR_NAMESPACE_URI);
						if (uri.equals(child.getNamespaceURI())) {
							try {
								Object obj = config
										.createExecutableExtension(PageAdaptersExtensionPointConstants.ATTR_MODEL_FACTORY);
								if (obj instanceof IModelFactory) {
									IModelFactory factory = (IModelFactory) obj;
									factory.getChildrenFromXml(list, childElem, this);
								}
							}
							catch (CoreException e) {
								StatusHandler.log(new Status(IStatus.ERROR, ConfigGraphPlugin.PLUGIN_ID,
										Messages.AbstractConfigFlowDiagram_ERROR_CREATING_GRAPH, e));
							}
						}
					}
				}
				getDiagram().getModelFactory().getGenericChildrenFromXml(list, childElem, this);
			}
		}
		return list;
	}

	public String getNewID() {
		return Integer.toString(count++);
	}

	public void removeChild(Activity child) {
		children.remove(child);
		extendedRemoveChild(child);
		fireStructureChange(CHILDREN, child);
	}

	protected void updateChildrenFromXml() {
		List<Activity> list = getChildrenFromXml();
		List<Activity> workingCopy = new ArrayList<Activity>();
		workingCopy.addAll(list);
		for (Activity activity : list) {
			for (int i = 0; i < children.size(); i++) {
				Activity child = children.get(i);
				if (activity.equals(child)) {
					int index = list.indexOf(activity);
					workingCopy.remove(activity);
					workingCopy.add(index, child);
				}
			}
		}
		children = workingCopy;
		getModelRegistry().addAll(children);

		for (Activity activity : children) {
			activity.incomings.clear();
			activity.outgoings.clear();
			activity.internalSetName();
			if (activity instanceof StructuredActivity) {
				((StructuredActivity) activity).updateChildrenFromXml();
			}
		}
	}

	@Override
	protected void updateTransitionsFromXml() {
		super.updateTransitionsFromXml();
		for (Activity child : children) {
			child.updateTransitionsFromXml();
		}
	}

}
