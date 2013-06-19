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
package org.springframework.ide.eclipse.config.ui.actions;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.eclipse.wst.xml.core.internal.document.DOMModelImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.BeansValidationContext;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigFactory;
import org.springframework.ide.eclipse.beans.core.namespaces.ToolAnnotationUtils.ToolAnnotationData;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.config.ui.ConfigUiPlugin;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigEditor;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.internal.model.SpringProject;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This action toggles the mark occurrences for bean references.
 * @author Terry Denney
 * @author Christian Dupuis
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class ToggleMarkOccurrencesAction extends Action implements IPropertyChangeListener, KeyListener, MouseListener {

	public static class OccurrenceLocation {

		private final int offset;

		private final int length;

		public OccurrenceLocation(int offset, int length) {
			this.offset = offset;
			this.length = length;
		}

		public int getLength() {
			return length;
		}

		public int getOffset() {
			return offset;
		}
	}

	private final IPreferenceStore prefStore;

	private AbstractConfigEditor editor;

	private final Set<Annotation> annotations;

	public static String STS_MARK_OCCURRENCES = "stsMarkOccurrences"; //$NON-NLS-1$

	private final String MARK_PATH = "icons/mark_occurrences.gif"; //$NON-NLS-1$

	private final String MARK_DISABLED_PATH = "icons/mark_occurrences_disabled.gif"; //$NON-NLS-1$

	private static final String[] ATTRIBUTES_TO_CHECK = new String[] { BeansSchemaConstants.ATTR_ID,
			BeansSchemaConstants.ATTR_NAME, BeansSchemaConstants.ATTR_PARENT, BeansSchemaConstants.ATTR_REF,
			BeansSchemaConstants.ATTR_DEPENDS_ON, BeansSchemaConstants.ATTR_BEAN, BeansSchemaConstants.ATTR_LOCAL,
			BeansSchemaConstants.ATTR_KEY_REF, BeansSchemaConstants.ATTR_VALUE_REF };

	public ToggleMarkOccurrencesAction() {
		super(Messages.getString("ToggleMarkOccurrencesAction.ACTION_LABEL"), Action.AS_CHECK_BOX); //$NON-NLS-1$
		ImageDescriptor imageDesc = ConfigUiPlugin.imageDescriptorFromPlugin(ConfigUiPlugin.PLUGIN_ID, MARK_PATH);
		ImageDescriptor disabledDesc = ConfigUiPlugin.imageDescriptorFromPlugin(ConfigUiPlugin.PLUGIN_ID,
				MARK_DISABLED_PATH);
		setImageDescriptor(imageDesc);
		setDisabledImageDescriptor(disabledDesc);
		annotations = new HashSet<Annotation>();
		prefStore = ConfigUiPlugin.getDefault().getPreferenceStore();
		prefStore.addPropertyChangeListener(this);
		setChecked(prefStore.getBoolean(STS_MARK_OCCURRENCES));
	}

	private Set<OccurrenceLocation> findBeanReference(String beanName, BeansValidationContext context)
			throws BadLocationException {
		IFile file = editor.getResourceFile();
		Set<OccurrenceLocation> result = new HashSet<OccurrenceLocation>();

		IStructuredModel model = null;
		try {
			model = StructuredModelManager.getModelManager().getModelForRead(file);

			if (model != null) {
				IDOMDocument document = ((DOMModelImpl) model).getDocument();
				if (document != null) {
					NodeList nodes = document.getDocumentElement().getChildNodes();
					for (int i = 0; i < nodes.getLength(); i++) {
						result.addAll(findBeanReference(beanName, nodes.item(i), context));
					}
				}

			}
		}
		catch (IOException e) {
		}
		catch (CoreException e) {
		}
		finally {
			if (model != null) {
				model.releaseFromRead();
			}
		}

		return result;
	}

	private Set<OccurrenceLocation> findBeanReference(String beanName, Node node, BeansValidationContext context)
			throws BadLocationException {
		Set<OccurrenceLocation> result = new HashSet<OccurrenceLocation>();

		NamedNodeMap attributes = node.getAttributes();
		if (attributes != null) {
			for (int i = 0; i < attributes.getLength(); i++) {
				AttrImpl attribute = (AttrImpl) attributes.item(i);
				result.addAll(findBeanReferences(beanName, attribute, node, context));
			}
		}

		NodeList nodes = node.getChildNodes();
		if (nodes != null && nodes.getLength() > 0) {
			for (int i = 0; i < nodes.getLength(); i++) {
				Node child = nodes.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE) {
					result.addAll(findBeanReference(beanName, child, context));
				}
			}
		}

		return result;
	}

	private Set<OccurrenceLocation> findBeanReferences(String beanName, AttrImpl attribute, Node node,
			BeansValidationContext context) {
		Set<OccurrenceLocation> result = new HashSet<OccurrenceLocation>();

		String attrName = attribute.getName();
		if (attrName != null) {
			boolean found = false;

			for (String attributeName : ATTRIBUTES_TO_CHECK) {
				if (attributeName.equals(attrName)) {
					found = true;
					break;
				}
			}

			if (context != null) {
				for (ToolAnnotationData annotationData : context.getToolAnnotation(node, attribute.getLocalName())) {
					if ("ref".equals(annotationData.getKind())) { //$NON-NLS-1$
						found = true;
					}
				}
			}

			if (found) {
				if (beanName.equals(attribute.getValue())) {
					try {
						int offset = attribute.getValueRegionStartOffset();
						int length = beanName.length();

						// check if the value starts with quote
						if (editor.getTextViewer().getDocument().getChar(offset) == '"') {
							offset++;
						}
						result.add(new OccurrenceLocation(offset, length));
					}
					catch (BadLocationException e) {
					}
				}
			}
		}
		return result;

	}

	private Set<OccurrenceLocation> findLocations(IndexedRegion region, int offset) {
		if (region instanceof IDOMNode) {
			IDOMNode node = (IDOMNode) region;
			IFile file = editor.getResourceFile();

			BeansValidationContext context = null;
			IBeansConfig config = BeansCorePlugin.getModel().getConfig(BeansConfigFactory.getConfigId(file));
			if (config != null) {
				context = new BeansValidationContext(config,
						new SpringProject(SpringCore.getModel(), file.getProject()));
			}

			String beanName = getBeanName(node, offset, context);
			if (beanName != null) {
				try {
					return findBeanReference(beanName, context);
				}
				catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return new HashSet<OccurrenceLocation>();
	}

	private String getBeanName(IDOMNode node, int offset, BeansValidationContext context) {
		NamedNodeMap attributes = node.getAttributes();
		if (attributes != null) {
			for (int i = 0; i < attributes.getLength(); i++) {
				AttrImpl attribute = (AttrImpl) attributes.item(i);

				if (attribute != null && attribute.getStartOffset() <= offset && attribute.getEndOffset() >= offset) {
					if (context != null) {
						List<ToolAnnotationData> toolAnnotations = context.getToolAnnotation(node,
								attribute.getLocalName());
						for (ToolAnnotationData toolAnnotation : toolAnnotations) {
							if ("ref".equals(toolAnnotation.getKind())) { //$NON-NLS-1$
								return attribute.getNodeValue();
							}
						}
					}

					for (String attributeName : ATTRIBUTES_TO_CHECK) {
						if (attributeName.equals(attribute.getNodeName())) {
							return attribute.getNodeValue();
						}
					}
				}
			}
		}
		return null;
	}

	public void keyPressed(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
		selectionChanged();
	}

	public void mouseDoubleClick(MouseEvent e) {
	}

	public void mouseDown(MouseEvent e) {
		selectionChanged();
	}

	public void mouseUp(MouseEvent e) {
	}

	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if (STS_MARK_OCCURRENCES.equals(property)) {
			setChecked(prefStore.getBoolean(STS_MARK_OCCURRENCES));
		}
	}

	@Override
	public void run() {
		prefStore.setValue(STS_MARK_OCCURRENCES, isChecked());
		selectionChanged();
	}

	private void selectionChanged() {
		if (editor != null) {
			if (prefStore.getBoolean(STS_MARK_OCCURRENCES)) {
				ISelection selection = editor.getTextViewer().getSelection();
				if (selection instanceof ITextSelection) {
					ITextSelection textSelection = (ITextSelection) selection;
					int offset = textSelection.getOffset();
					IndexedRegion node = BeansEditorUtils.getNodeAt(editor.getTextViewer(), offset);

					Set<OccurrenceLocation> locations = findLocations(node, offset);
					updateAnnotations(locations);
				}
			}
			else if (annotations.size() > 0) {
				updateAnnotations(new HashSet<OccurrenceLocation>());
				annotations.clear();
			}
		}
	}

	public void selectionChanged(SelectionChangedEvent event) {
		selectionChanged();
	}

	public void setActiveEditor(AbstractConfigEditor targetEditor) {
		if (this.editor != null && this.editor.getTextViewer() != null
				&& this.editor.getTextViewer().getTextWidget() != null) {
			StyledText textWidget = this.editor.getTextViewer().getTextWidget();
			textWidget.removeMouseListener(this);
			textWidget.removeKeyListener(this);
			updateAnnotations(new HashSet<OccurrenceLocation>());
		}
		this.editor = targetEditor;
		setChecked(prefStore.getBoolean(STS_MARK_OCCURRENCES));
		if (this.editor != null && this.editor.getTextViewer() != null
				&& this.editor.getTextViewer().getTextWidget() != null) {
			StyledText textWidget = this.editor.getTextViewer().getTextWidget();
			textWidget.addMouseListener(this);
			textWidget.addKeyListener(this);
		}
		selectionChanged();
	}

	private void updateAnnotations(Set<OccurrenceLocation> locations) {
		IAnnotationModel annotationModel = editor.getSourcePage().getDocumentProvider()
				.getAnnotationModel(editor.getEditorInput());
		if (annotationModel == null) {
			return;
		}

		Map<Annotation, Position> newAnnotations = new HashMap<Annotation, Position>();
		for (OccurrenceLocation location : locations) {
			Annotation annotation = new Annotation("org.eclipse.jdt.ui.occurrences", false, ""); //$NON-NLS-1$ //$NON-NLS-2$
			Position position = new Position(location.getOffset(), location.getLength());
			newAnnotations.put(annotation, position);
		}

		if (annotationModel instanceof IAnnotationModelExtension) {
			((IAnnotationModelExtension) annotationModel).replaceAnnotations(
					annotations.toArray(new Annotation[annotations.size()]), newAnnotations);
		}
		else {
			for (Annotation annotation : annotations) {
				annotationModel.removeAnnotation(annotation);
			}

			for (Annotation annotation : newAnnotations.keySet()) {
				annotationModel.addAnnotation(annotation, newAnnotations.get(annotation));
			}
		}

		annotations.clear();
		annotations.addAll(newAnnotations.keySet());
	}

}
