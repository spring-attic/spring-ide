/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.eclipse.beans.ui.refactoring.util;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.eclipse.wst.xml.core.internal.document.DOMModelImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@SuppressWarnings("restriction")
public class BeansRefactoringChangeUtils {

	public static Change createMethodRenameChange(IFile file,
			IJavaElement[] affectedElements, String[] newNames,
			IProgressMonitor pm) throws CoreException {
		IStructuredModel model = null;
		try {
			model = StructuredModelManager.getModelManager().getModelForRead(
					file);

			if (model == null) {
				return null;
			}
			IDOMDocument document = ((DOMModelImpl) model).getDocument();
			MultiTextEdit multiEdit = new MultiTextEdit();
			NodeList nodes = document.getElementsByTagName("bean");
			for (int j = 0; j < affectedElements.length; j++) {
				for (int i = 0; i < nodes.getLength(); i++) {
					TextEdit edit = createMethodTextEdit(nodes.item(i),
							affectedElements[j], newNames[j], file);
					if (edit != null)
						multiEdit.addChild(edit);
				}
			}
			if (multiEdit.hasChildren()) {
				TextFileChange change = new TextFileChange("", file);
				change.setEdit(multiEdit);
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

	@SuppressWarnings( { "restriction", "unchecked" })
	private static TextEdit createMethodTextEdit(Node node,
			IJavaElement element, String newName, IFile file) {
		if (node == null)
			return null;

		if (element instanceof IMethod
				&& element.getElementName().startsWith("set")) {
			String methodName = StringUtils.uncapitalize(element
					.getElementName().substring(3));
			NodeList nodes = node.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node child = nodes.item(i);
				if ("property".equals(child.getLocalName())
						&& BeansEditorUtils.hasAttribute(child, "name")) {
					String propertyName = BeansEditorUtils.getAttribute(child,
							"name");
					if (methodName.equals(propertyName)) {
						List<IType> types = BeansEditorUtils
								.getClassNamesOfBean(file, node);
						if (types.contains(((IMethod) element)
								.getDeclaringType())) {
							AttrImpl attr = (AttrImpl) child.getAttributes()
									.getNamedItem("name");
							int offset = attr.getValueRegionStartOffset() + 1;
							if (offset >= 0)
								return new ReplaceEdit(offset, propertyName
										.length(), newName);
						}
					}
				}
			}
		}
		return null;
	}

	public static Change createRenameChange(IFile file,
			IJavaElement[] affectedElements, String[] newNames,
			IProgressMonitor monitor) throws CoreException {
		IStructuredModel model = null;
		try {
			model = StructuredModelManager.getModelManager().getModelForRead(
					file);

			if (model == null) {
				return null;
			}
			IDOMDocument document = ((DOMModelImpl) model).getDocument();
			MultiTextEdit multiEdit = new MultiTextEdit();
			NodeList nodes = document.getElementsByTagName("bean");
			for (int j = 0; j < affectedElements.length; j++) {
				for (int i = 0; i < nodes.getLength(); i++) {
					TextEdit edit = createTextEdit(nodes.item(i),
							affectedElements[j], newNames[j]);
					if (edit != null)
						multiEdit.addChild(edit);
				}
			}
			if (multiEdit.hasChildren()) {
				TextFileChange change = new TextFileChange("", file);
				change.setEdit(multiEdit);
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

	@SuppressWarnings("restriction")
	private static TextEdit createTextEdit(Node node, IJavaElement element,
			String newName) {
		if (node == null)
			return null;

		String oldName = (element instanceof IType) ? ((IType) element)
				.getFullyQualifiedName('$') : element.getElementName();
		String value = BeansEditorUtils.getAttribute(node, "class");
		if (oldName.equals(value)
				|| isGoodMatch(value, oldName,
						element instanceof IPackageFragment)) {
			AttrImpl attr = (AttrImpl) node.getAttributes().getNamedItem(
					"class");
			int offset = attr.getValueRegionStartOffset() + 1;
			if (offset >= 0)
				return new ReplaceEdit(offset, oldName.length(), newName);
		}
		return null;
	}

	private static boolean isGoodMatch(String value, String oldName,
			boolean isPackage) {
		if (value == null || value.length() <= oldName.length())
			return false;
		boolean goodLengthMatch = isPackage ? value.lastIndexOf('.') <= oldName
				.length() : value.charAt(oldName.length()) == '$';
		return value.startsWith(oldName) && goodLengthMatch;
	}
}
