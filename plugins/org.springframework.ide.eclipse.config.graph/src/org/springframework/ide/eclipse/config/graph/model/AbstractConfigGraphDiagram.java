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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.config.core.contentassist.SpringConfigContentAssistProcessor;
import org.springframework.ide.eclipse.config.core.extensions.PageAdaptersExtensionPointConstants;
import org.springframework.ide.eclipse.config.graph.AbstractConfigGraphicalEditor;
import org.springframework.ide.eclipse.config.graph.ConfigGraphPlugin;
import org.springframework.ide.eclipse.core.SpringCorePreferences;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public abstract class AbstractConfigGraphDiagram extends ParallelActivity {

	private static String KEY_COORDINATES = "coordinates"; //$NON-NLS-1$

	private static String KEY_ELEMENT = "element"; //$NON-NLS-1$

	private static String KEY_ELEMENT_TYPE = "type"; //$NON-NLS-1$

	private static String KEY_STRUCTURE = "structure"; //$NON-NLS-1$

	private static String KEY_STRUCTURE_START = "start"; //$NON-NLS-1$

	private static String KEY_STRUCTURE_STARTEND = "startend"; //$NON-NLS-1$

	private static String KEY_STRUCTURE_ENDSTART = "endstart"; //$NON-NLS-1$

	private static String KEY_STRUCTURE_END = "end"; //$NON-NLS-1$

	private static String KEY_BOUNDS = "bounds"; //$NON-NLS-1$

	private static String KEY_BOUNDS_X = "x"; //$NON-NLS-1$

	private static String KEY_BOUNDS_Y = "y"; //$NON-NLS-1$

	private static String KEY_BOUNDS_HEIGHT = "height"; //$NON-NLS-1$

	private static String KEY_BOUNDS_WIDTH = "width"; //$NON-NLS-1$

	private static String KEY_MODEL_CLASS = "clazz"; //$NON-NLS-1$

	private final AbstractConfigGraphicalEditor editor;

	private final List<Activity> modelRegistry;

	private Map<String, Node> refNodeRegistry;

	public AbstractConfigGraphDiagram(AbstractConfigGraphicalEditor editor) {
		super();
		this.editor = editor;
		modelRegistry = new ArrayList<Activity>();
		refNodeRegistry = new HashMap<String, Node>();
	}

	@Override
	protected void createInput(String uri) {
		// no-op
	}

	public Rectangle doReadCoordinates(Activity activity) {
		String xml = getPreferenceLocation().getString(getGraphCoordsKey(), ""); //$NON-NLS-1$
		Rectangle savedBounds = null;
		if (xml != null && xml.length() > 0) {
			try {
				IMemento memento = XMLMemento.createReadRoot(new StringReader(xml));
				IMemento[] elementMementos = memento.getChildren(KEY_ELEMENT);
				for (IMemento element : elementMementos) {
					IDOMElement input = activity.getInput();
					if (activity.getInputName().equals(element.getString(KEY_ELEMENT_TYPE))
							&& activity.getClass().getSimpleName().equals(element.getString(KEY_MODEL_CLASS))) {
						IMemento structureMemento = element.getChild(KEY_STRUCTURE);
						if (structureMemento != null
								&& structureMemento.getInteger(KEY_STRUCTURE_START).equals(input.getStartOffset())
								&& structureMemento.getInteger(KEY_STRUCTURE_STARTEND)
										.equals(input.getStartEndOffset())
								&& structureMemento.getInteger(KEY_STRUCTURE_ENDSTART)
										.equals(input.getEndStartOffset())
								&& structureMemento.getInteger(KEY_STRUCTURE_END).equals(input.getEndOffset())) {
							IMemento boundsMemento = element.getChild(KEY_BOUNDS);
							savedBounds = new Rectangle(boundsMemento.getInteger(KEY_BOUNDS_X),
									boundsMemento.getInteger(KEY_BOUNDS_Y), boundsMemento.getInteger(KEY_BOUNDS_WIDTH),
									boundsMemento.getInteger(KEY_BOUNDS_HEIGHT));
						}
					}
				}
			}
			catch (WorkbenchException e) {
				StatusHandler.log(new Status(IStatus.ERROR, ConfigGraphPlugin.PLUGIN_ID,
						Messages.AbstractConfigFlowDiagram_ERROR_READING_COORDINATES));
			}
		}
		return savedBounds;
	}

	public void doResetCoordinates() {
		getPreferenceLocation().putString(getGraphCoordsKey(), "");
	}

	public void doSaveCoordinates() {
		String xml = null;
		XMLMemento memento = XMLMemento.createWriteRoot("graph"); //$NON-NLS-1$
		for (Activity child : children) {
			IMemento childMemento = memento.createChild(KEY_ELEMENT);
			childMemento.putString(KEY_ELEMENT_TYPE, child.getInputName());
			childMemento.putString(KEY_MODEL_CLASS, child.getClass().getSimpleName());

			IDOMElement input = child.getInput();
			IMemento structureMemento = childMemento.createChild(KEY_STRUCTURE);
			structureMemento.putInteger(KEY_STRUCTURE_START, input.getStartOffset());
			structureMemento.putInteger(KEY_STRUCTURE_STARTEND, input.getStartEndOffset());
			structureMemento.putInteger(KEY_STRUCTURE_ENDSTART, input.getEndStartOffset());
			structureMemento.putInteger(KEY_STRUCTURE_END, input.getEndOffset());

			Rectangle bounds = child.getBounds();
			IMemento boundsMemento = childMemento.createChild(KEY_BOUNDS);
			boundsMemento.putInteger(KEY_BOUNDS_X, bounds.x);
			boundsMemento.putInteger(KEY_BOUNDS_Y, bounds.y);
			boundsMemento.putInteger(KEY_BOUNDS_HEIGHT, bounds.height);
			boundsMemento.putInteger(KEY_BOUNDS_WIDTH, bounds.width);
		}

		try {
			StringWriter writer = new StringWriter();
			memento.save(writer);
			xml = writer.getBuffer().toString();
			getPreferenceLocation().putString(getGraphCoordsKey(), xml);
		}
		catch (IOException e) {
			StatusHandler.log(new Status(IStatus.ERROR, ConfigGraphPlugin.PLUGIN_ID,
					Messages.AbstractConfigFlowDiagram_ERROR_SAVING_COORDINATES));
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (IPropertySource.class == adapter) {
			return null;
		}
		return super.getAdapter(adapter);
	}

	@Override
	protected List<Activity> getChildrenFromXml() {
		List<Activity> list = super.getChildrenFromXml();
		list.addAll(getNestedChildrenFromXml(getInput()));
		return list;
	}

	@Override
	public AbstractConfigGraphDiagram getDiagram() {
		return this;
	}

	public IDOMDocument getDomDocument() {
		return editor.getDomDocument();
	}

	private String getGraphCoordsKey() {
		return KEY_COORDINATES + ":" + getNamespaceUri() + ":" + getResourceFile().getFullPath().toString(); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public AbstractConfigGraphicalEditor getGraphicalEditor() {
		return editor;
	}

	@Override
	public IDOMElement getInput() {
		return (IDOMElement) getDomDocument().getDocumentElement();
	}

	@Override
	public String getInputName() {
		return ""; //$NON-NLS-1$
	}

	protected abstract IDiagramModelFactory getModelFactory();

	@Override
	public List<Activity> getModelRegistry() {
		return modelRegistry;
	}

	public String getNamespaceUri() {
		return editor.getNamespaceUri();
	}

	private List<Activity> getNestedChildrenFromXml(IDOMElement parent) {
		List<Activity> list = new ArrayList<Activity>();
		NodeList children = parent.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child instanceof IDOMElement) {
				IDOMElement childElem = (IDOMElement) child;
				if (getNamespaceUri() != null && getNamespaceUri().equals(child.getNamespaceURI())) {
					getModelFactory().getNestedChildrenFromXml(list, childElem, this);
				}
				else {
					for (IConfigurationElement config : editor.getAdapterDefinitions()) {
						String uri = config.getAttribute(PageAdaptersExtensionPointConstants.ATTR_NAMESPACE_URI);
						if (uri.equals(child.getNamespaceURI())) {
							try {
								Object obj = config
										.createExecutableExtension(PageAdaptersExtensionPointConstants.ATTR_MODEL_FACTORY);
								if (obj instanceof IModelFactory) {
									IModelFactory factory = (IModelFactory) obj;
									factory.getNestedChildrenFromXml(list, childElem, this);
								}
							}
							catch (CoreException e) {
								StatusHandler.log(new Status(IStatus.ERROR, ConfigGraphPlugin.PLUGIN_ID,
										Messages.AbstractConfigFlowDiagram_ERROR_CREATING_GRAPH, e));
							}
						}
					}
				}
				if (child.hasChildNodes()) {
					list.addAll(getNestedChildrenFromXml(childElem));
				}
			}
		}
		return list;
	}

	private SpringCorePreferences getPreferenceLocation() {
		IFile resource = getResourceFile();
		SpringCorePreferences prefs = SpringCorePreferences.getProjectPreferences(resource.getProject(),
				ConfigGraphPlugin.LEGACY_ID);
		return prefs;
	}

	public Node getReferencedNode(String ref) {
		if (ref == null || ref.length() <= 0) {
			return null;
		}
		return refNodeRegistry.get(ref);
	}

	public IFile getResourceFile() {
		return editor.getResourceFile();
	}

	public StructuredTextEditor getTextEditor() {
		return editor.getEditor().getSourcePage();
	}

	protected void getTransitionsFromXml(Activity source, Activity target, List<Transition> transitions,
			List<String> attrs, boolean incoming, boolean primary) {
		for (String label : attrs) {
			Attr attr = source.getInput().getAttributeNode(label);
			if (attr instanceof IDOMAttr) {
				Node ref = getReferencedNode(attr.getValue());
				if (ref instanceof IDOMElement && target.getInput().equals(ref)) {
					Transition trans;
					if (incoming) {
						trans = new Transition(target, source, (IDOMAttr) attr);
					}
					else {
						trans = new Transition(source, target, (IDOMAttr) attr);
					}
					if (!primary) {
						trans.setLineStyle(Transition.DASHED_CONNECTION);
					}
					transitions.add(trans);
				}
			}
		}
	}

	protected SpringConfigContentAssistProcessor getXmlProcessor() {
		return editor.getXmlProcessor();
	}

	@Override
	protected void internalSetName() {
		setName("diagram"); //$NON-NLS-1$
	}

	// Move this into ConfigCoreUtils??
	public boolean listContainsElement(List<Activity> list, Activity element) {
		for (Activity activity : list) {
			if (activity.getClass().equals(element.getClass()) && activity.getInput() != null
					&& activity.getInput().equals(element.getInput())) {
				return true;
			}
		}
		return false;
	}

	public void refreshModelFromXml() {
		modelRegistry.clear();
		updateRefNodeRegistry();
		updateChildrenFromXml();
		updateTransitionsFromXml();
	}

	@Override
	public void setInput(IDOMElement input) {
		// no-op
	}

	private void updateRefNodeRegistry() {
		refNodeRegistry = BeansEditorUtils.getReferenceableNodes(getDomDocument(), getResourceFile());
	}

}
