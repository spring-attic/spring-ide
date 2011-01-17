/*******************************************************************************
 * Copyright (c) 2007, 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.refactoring.util;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.eclipse.wst.xml.core.internal.document.DOMModelImpl;
import org.eclipse.wst.xml.core.internal.document.TextImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.beans.ui.refactoring.ltk.RenameIdType;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @author Martin Lippert
 * @author Terry Denney
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class BeansRefactoringChangeUtils {

	public static Change createMethodRenameChange(IFile file, IJavaElement[] affectedElements, String[] newNames,
			IProgressMonitor pm) throws CoreException {
		IStructuredModel model = null;
		try {
			model = StructuredModelManager.getModelManager().getModelForRead(file);

			if (model == null) {
				return null;
			}
			IDOMDocument document = ((DOMModelImpl) model).getDocument();
			MultiTextEdit multiEdit = new MultiTextEdit();
			NodeList nodes = document.getElementsByTagName("bean");
			for (int j = 0; j < affectedElements.length; j++) {
				for (int i = 0; i < nodes.getLength(); i++) {
					Set<TextEdit> edits = createMethodTextEdits(nodes.item(i), affectedElements[j], newNames[j], file);
					if (edits != null) {
						multiEdit.addChildren(edits.toArray(new TextEdit[edits.size()]));
					}
				}
			}
			if (multiEdit.hasChildren()) {
				TextFileChange change = new TextFileChange("", file);
				change.setEdit(multiEdit);
				for (TextEdit e : multiEdit.getChildren()) {
					change.addTextEditGroup(new TextEditGroup("Rename Bean property name", e));
				}
				return change;
			}
		}
		catch (IOException e) {
		}
		finally {
			if (model != null) {
				model.releaseFromRead();
			}
		}
		return null;
	}

	private static Set<TextEdit> createMethodTextEdits(Node node, IJavaElement element, String newName, IFile file) {
		if (node == null) {
			return null;
		}

		Set<TextEdit> result = new HashSet<TextEdit>();

		if (element instanceof IMethod) {
			if (element.getElementName().startsWith("set")) {
				String methodName = StringUtils.uncapitalize(element.getElementName().substring(3));
				
				// change those child notes that are property childs and refer to the changed method name
				NodeList nodes = node.getChildNodes();
				for (int i = 0; i < nodes.getLength(); i++) {
					Node child = nodes.item(i);
					if ("property".equals(child.getLocalName()) && BeansEditorUtils.hasAttribute(child, "name")) {
						String propertyName = BeansEditorUtils.getAttribute(child, "name");
						if (methodName.equals(propertyName)) {
							List<IType> types = BeansEditorUtils.getClassNamesOfBean(file, node);
							if (types.contains(((IMethod) element).getDeclaringType())) {
								AttrImpl attr = (AttrImpl) child.getAttributes().getNamedItem("name");
								int offset = attr.getValueRegionStartOffset() + 1;
								if (offset >= 0) {
									result.add(new ReplaceEdit(offset, propertyName.length(), newName));
								}
							}
						}
					}
				}
				
				// p-namespace references to the properties that belong to the changed method
				if (node.hasAttributes()) {
					String attributeNameStart = "p:";
					String optionalAttributeNameEnd = "-ref";
					
					NamedNodeMap attributes = node.getAttributes();
					int attributeCount = attributes.getLength();
					
					for (int i = 0; i < attributeCount; i++) {
						AttrImpl attribute = (AttrImpl) attributes.item(i);
						String attributeName = attribute.getNodeName();
						if (attributeName != null && attributeName.startsWith(attributeNameStart)) {
							if (attributeName.equals(attributeNameStart + methodName)
									|| attributeName.equals(attributeNameStart + methodName + optionalAttributeNameEnd)) {
								int offset = attribute.getNameRegionStartOffset() + attributeNameStart.length();
								result.add(new ReplaceEdit(offset, methodName.length(), newName));
							}
						}
					}
				}

			}
			else {
				TextEdit edit = null;
				edit = createMethodTextEditForAttribute(node, element, newName, file, "init-method");
				if (edit != null) {
					result.add(edit);
				}

				edit = createMethodTextEditForAttribute(node, element, newName, file, "destroy-method");
				if (edit != null) {
					result.add(edit);
				}

				edit = createMethodTextEditForAttribute(node, element, newName, file, "factory-method");
				if (edit != null) {
					result.add(edit);
				}
			}
		}
		return result;
	}

	private static TextEdit createMethodTextEditForAttribute(Node node, IJavaElement element, String newName,
			IFile file, String attrName) {
		String methodName = element.getElementName();
		if (BeansEditorUtils.hasAttribute(node, attrName)) {
			String attrMethodName = BeansEditorUtils.getAttribute(node, attrName);
			if (methodName.equals(attrMethodName)) {
				List<IType> types = BeansEditorUtils.getClassNamesOfBean(file, node);
				if (types.contains(((IMethod) element).getDeclaringType())) {
					AttrImpl attr = (AttrImpl) node.getAttributes().getNamedItem(attrName);
					int offset = attr.getValueRegionStartOffset() + 1;
					if (offset >= 0) {
						return new ReplaceEdit(offset, attrMethodName.length(), newName);
					}
				}
			}
		}
		return null;
	}

	public static Change createRenameBeanIdChange(IFile file, RenameIdType descriptor, String oldBeanId, String newBeanId,
			boolean updateReferences, IProgressMonitor monitor) throws CoreException {
		IStructuredModel model = null;
		try {
			model = StructuredModelManager.getModelManager().getModelForRead(file);

			if (model == null) {
				return null;
			}
			IDOMDocument document = ((DOMModelImpl) model).getDocument();
			MultiTextEdit multiEdit = new MultiTextEdit();
			NodeList nodes = document.getElementsByTagName(descriptor.getElementName());
			for (int i = 0; i < nodes.getLength(); i++) {
				TextEdit edit = createRenameBeanIdTextEdit(nodes.item(i), oldBeanId, newBeanId);
				if (edit != null) {
					multiEdit.addChild(edit);
				}
			}

			if (model != null) {
				model.releaseFromRead();
				model = null;
			}

			TextFileChange refChanges = null;
			if (updateReferences) {
				refChanges = createRenameBeanRefsChange(file, descriptor, oldBeanId, newBeanId, monitor);
			}

			if (multiEdit.hasChildren()) {
				TextFileChange change = new TextFileChange("", file);
				change.setEdit(multiEdit);
				for (TextEdit e : multiEdit.getChildren()) {
					change.addTextEditGroup(new TextEditGroup("Rename " + descriptor.getType() + " id", e));
				}
				if (refChanges != null) {
					MultiTextEdit edit = (MultiTextEdit) refChanges.getEdit();
					if (edit.hasChildren()) {
						for (TextEdit e : edit.getChildren()) {
							edit.removeChild(e);
							multiEdit.addChild(e);
							change.addTextEditGroup(new TextEditGroup("Rename " + descriptor.getType() + " reference", e));
						}
					}
				}
				return change;
			}
		}
		catch (IOException e) {
		}
		finally {
			if (model != null) {
				model.releaseFromRead();
			}
		}
		return null;
	}

	private static TextEdit createRenameBeanIdTextEdit(Node node, String oldBeanId, String newBeanId) {
		if (node == null) {
			return null;
		}

		String id = BeansEditorUtils.getAttribute(node, "id");
		if (oldBeanId.equals(id)) {
			AttrImpl attr = (AttrImpl) node.getAttributes().getNamedItem("id");
			int offset = attr.getValueRegionStartOffset() + 1;
			if (offset >= 0) {
				return new ReplaceEdit(offset, oldBeanId.length(), newBeanId);
			}
		}
		return null;
	}

	public static TextFileChange createRenameBeanRefsChange(IFile file,
			String oldBeanId, String newBeanId, IProgressMonitor monitor) throws CoreException {
		RenameIdType descriptor = RenameIdType.BEAN;
		return createRenameBeanRefsChange(file, descriptor, oldBeanId, newBeanId, monitor);
	}

	public static TextFileChange createRenameBeanRefsChange(IFile file, RenameIdType descriptor,
			String oldBeanId, String newBeanId, IProgressMonitor monitor) throws CoreException {
		IStructuredModel model = null;
		try {
			model = StructuredModelManager.getModelManager().getModelForRead(file);

			if (model == null) {
				return null;
			}
			
			IDOMDocument document = ((DOMModelImpl) model).getDocument();
			MultiTextEdit multiEdit = new MultiTextEdit();
			NodeList nodes = document.getDocumentElement().getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				createRenameBeanRefsTextEdit(nodes.item(i), descriptor, oldBeanId, newBeanId, multiEdit);
			}
			
			if (multiEdit.hasChildren()) {
				TextFileChange change = new TextFileChange("", file);
				change.setEdit(multiEdit);
				for (TextEdit e : multiEdit.getChildren()) {
					change.addTextEditGroup(new TextEditGroup("Rename " + descriptor.getType() + " reference", e));
				}
				return change;
			}
		}
		catch (IOException e) {
		}
		finally {
			if (model != null) {
				model.releaseFromRead();
			}
		}
		return null;
	}

	private static void createRenameBeanRefsTextEdit(Node node, RenameIdType descriptor, String oldBeanId,
			String newBeanId, MultiTextEdit multiEdit) {
		if (node == null) {
			return;
		}
		
		String[] attributeNames = descriptor.getReferenceAttributeNames();
		for (String attributeName : attributeNames) {
			createRenameBeanRefTextEditForAttribute(attributeName, node, oldBeanId, newBeanId, multiEdit);
		}
		
		String[] attributeNameStarts = descriptor.getReferenceAttributeStarts();
		String[] attributeNameEnds = descriptor.getReferenceAttributeEnds();
		if (attributeNameStarts != null && attributeNameEnds != null && attributeNameStarts.length == attributeNameEnds.length) {
			for(int i = 0; i < attributeNameStarts.length; i++) {
				String attributeStart = attributeNameStarts[i];
				String attributeEnd = attributeNameEnds[i];
				createRenameBeanRefTextEditForAttribute(attributeStart, attributeEnd, node, oldBeanId, newBeanId, multiEdit);
			}
		}

//		createRenameBeanRefTextEditForAttribute("depends-on", node, oldBeanId, newBeanId, multiEdit);
//		createRenameBeanRefTextEditForAttribute("bean", node, oldBeanId, newBeanId, multiEdit);
//		createRenameBeanRefTextEditForAttribute("local", node, oldBeanId, newBeanId, multiEdit);
//		createRenameBeanRefTextEditForAttribute("parent", node, oldBeanId, newBeanId, multiEdit);
//		createRenameBeanRefTextEditForAttribute("ref", node, oldBeanId, newBeanId, multiEdit);
//		createRenameBeanRefTextEditForAttribute("key-ref", node, oldBeanId, newBeanId, multiEdit);
//		createRenameBeanRefTextEditForAttribute("value-ref", node, oldBeanId, newBeanId, multiEdit);
//		createRenameBeanRefTextEditForAttribute("p:", "-ref", node, oldBeanId, newBeanId, multiEdit);

		NodeList nodes = node.getChildNodes();
		if (nodes != null && nodes.getLength() > 0) {
			for (int i = 0; i < nodes.getLength(); i++) {
				createRenameBeanRefsTextEdit(nodes.item(i), descriptor, oldBeanId, newBeanId, multiEdit);
			}
		}
	}

	private static void createRenameBeanRefTextEditForAttribute(String attributeNameStart, String attributeNameEnd, Node node,
			String oldBeanId, String newBeanId, MultiTextEdit multiEdit) {
		if (!node.hasAttributes()) return;
		
		NamedNodeMap attributes = node.getAttributes();
		int attributeCount = attributes.getLength();
		
		for (int i = 0; i < attributeCount; i++) {
			Node attribute = attributes.item(i);
			String attributeName = attribute.getNodeName();
			if (attributeName != null && attributeName.startsWith(attributeNameStart)
						&& attributeName.endsWith(attributeNameEnd)) {
				String beanRef = attribute.getNodeValue();
				if (beanRef != null && beanRef.equals(oldBeanId)) {
					int offset = ((AttrImpl) attribute).getValueRegionStartOffset() + 1;
					if (offset >= 0) {
						multiEdit.addChild(new ReplaceEdit(offset, beanRef.length(), newBeanId));
					}
				}
			}
		}
	}

	private static void createRenameBeanRefTextEditForAttribute(String attributeName, Node node, String oldBeanId,
			String newBeanId, MultiTextEdit multiEdit) {
		if (BeansEditorUtils.hasAttribute(node, attributeName)) {
			String beanRef = BeansEditorUtils.getAttribute(node, attributeName);
			if (beanRef != null && beanRef.equals(oldBeanId)) {
				AttrImpl attr = (AttrImpl) node.getAttributes().getNamedItem(attributeName);
				int offset = attr.getValueRegionStartOffset() + 1;
				if (offset >= 0) {
					multiEdit.addChild(new ReplaceEdit(offset, beanRef.length(), newBeanId));
				}
			}
		}
	}

	public static Change createRenameChange(IFile file, IJavaElement[] affectedElements, String[] newNames,
			IProgressMonitor monitor) throws CoreException {
		IJavaProject jp = JdtUtils.getJavaProject(file.getProject());
		IStructuredModel model = null;
		try {
			model = StructuredModelManager.getModelManager().getModelForRead(file);

			if (model == null) {
				return null;
			}
			IDOMDocument document = ((DOMModelImpl) model).getDocument();
			MultiTextEdit multiEdit = new MultiTextEdit();
			NodeList nodes = document.getChildNodes();
			for (int j = 0; j < affectedElements.length; j++) {

				IJavaElement je = affectedElements[j];

				// check that the element we are about to change is on the file's classpath
				if (jp == null || (jp != null && jp.isOnClasspath(je))) {
					for (int i = 0; i < nodes.getLength(); i++) {
						Node n = nodes.item(i);
						recursiveCreateTextEdit(newNames[j], multiEdit, je, n);
					}
				}
			}
			if (multiEdit.hasChildren()) {
				TextFileChange change = new TextFileChange("", file);
				change.setEdit(multiEdit);
				for (TextEdit e : multiEdit.getChildren()) {
					change.addTextEditGroup(new TextEditGroup("Rename class name", e));
				}
				return change;
			}
		}
		catch (IOException e) {
		}
		finally {
			if (model != null) {
				model.releaseFromRead();
			}
		}
		return null;
	}

	private static void recursiveCreateTextEdit(String newName, MultiTextEdit multiEdit, IJavaElement je, Node n) {
		createTextEdit(n, je, newName, multiEdit);
		NodeList children = n.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			recursiveCreateTextEdit(newName, multiEdit, je, children.item(i));
		}
	}

	private static void createTextEdit(Node node, IJavaElement element, String newName, MultiTextEdit multiEdit) {
		if (node == null) {
			return;
		}

		String oldName = (element instanceof IType) ? ((IType) element).getFullyQualifiedName('$') : element
				.getElementName();

		// creating replace edits for attributes
		NamedNodeMap attributes = node.getAttributes();
		if (attributes != null) {
			for (int i = 0; i < attributes.getLength(); i++) {
				String attribute = attributes.item(i).getNodeValue();
				if (oldName.equals(attribute) || isGoodMatch(attribute, oldName, element instanceof IPackageFragment)) {
					AttrImpl attr = (AttrImpl) attributes.getNamedItem(attributes.item(i).getNodeName());
					int offset = attr.getValueRegionStartOffset() + 1;
					if (offset >= 0) {
						multiEdit.addChild(new ReplaceEdit(offset, oldName.length(), newName));
					}
				}
			}
		}
		
		// creating replace edits for value strings
		if (node instanceof TextImpl) {
			String value = node.getNodeValue();
			if (value != null) {
				if (oldName.equals(value) || isGoodMatch(value, oldName, element instanceof IPackageFragment)) {
					int offset = ((TextImpl)node).getStartOffset();
					if (offset >= 0) {
						multiEdit.addChild(new ReplaceEdit(offset, oldName.length(), newName));
					}
				}
			}
		}
	}

	private static boolean isGoodMatch(String value, String oldName, boolean isPackage) {
		if (value == null || value.length() <= oldName.length()) {
			return false;
		}
		boolean goodLengthMatch = isPackage ? value.lastIndexOf('.') <= oldName.length() : value.charAt(oldName
				.length()) == '$';
		return value.startsWith(oldName) && goodLengthMatch;
	}
}
