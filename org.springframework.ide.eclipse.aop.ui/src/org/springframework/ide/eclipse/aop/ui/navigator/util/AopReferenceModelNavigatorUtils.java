/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.ui.navigator.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportContainer;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.springframework.ide.eclipse.aop.ui.Activator;
import org.springframework.ide.eclipse.aop.ui.navigator.AopReferenceModelNavigator;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Helper methods for {@link AopReferenceModelNavigator}.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class AopReferenceModelNavigatorUtils {

	public static ILabelProvider JAVA_LABEL_PROVIDER = new DecoratingLabelProvider(
			new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT
					| JavaElementLabelProvider.SHOW_SMALL_ICONS), Activator
					.getDefault().getWorkbench().getDecoratorManager()
					.getLabelDecorator());

	public static Object getSelectedElement(IWorkbenchPart part,
			ISelection selection) {
		Object selectedElement = getSelectedJavaElement(part, selection);

		if (selectedElement == null) {
			selectedElement = getSelectedXmlElement(selection);

		}
		return selectedElement;
	}

	private static Object getSelectedXmlElement(ISelection selection) {
		Object selectedElement = null;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structSelection = (IStructuredSelection) selection;
			Object obj = structSelection.getFirstElement();
			if (obj instanceof Attr) {
				obj = ((Attr) obj).getOwnerElement();
			}
			if (obj instanceof Element) {
				Element elem = (Element) obj;
				selectedElement = obj;

				if ("http://www.springframework.org/schema/aop".equals(elem
						.getNamespaceURI())) {
					if ("aspect".equals(elem.getLocalName())) {
						selectedElement = locateAspectReference(elem,
								BeansEditorUtils.getAttribute(elem, "ref"));
					}
					else if ("advisor".equals(elem.getLocalName())) {
						selectedElement = locateAspectReference(elem,
								BeansEditorUtils.getAttribute(elem,
										"advice-ref"));
					}
					else if (elem.getParentNode() != null
							&& "aspect".equals(elem.getParentNode()
									.getLocalName())) {
						selectedElement = locateAspectReference(elem
								.getParentNode(), BeansEditorUtils
								.getAttribute(elem.getParentNode(), "ref"));
					}
					else if (!"config".equals(elem.getLocalName())) {
						selectedElement = locateAspectReference(elem,
								BeansEditorUtils.getAttribute(elem, "ref"));
					}
				}
				else if ("".equals(elem.getNamespaceURI())
						|| "http://www.springframework.org/schema/beans"
								.equals(elem.getNamespaceURI())) {
					// go up until a bean is reached
					Object parentBean = getBeanElement(elem, "bean");
					if (parentBean != null) {
						selectedElement = parentBean;
					}
				}

			}
			else if (obj instanceof Text) {
				Node parent = ((Text) obj).getParentNode();
				if (parent instanceof Element) {
					selectedElement = getSelectedXmlElement(new StructuredSelection(parent));
				}
			}
		}
		return selectedElement;
	}

	private static Node getBeanElement(Node elem, String nodeName) {
		if (!nodeName.equals(elem.getLocalName())
				&& !elem.getOwnerDocument().equals(elem.getParentNode())) {
			return getBeanElement(elem.getParentNode(), nodeName);
		}
		else {
			return elem;
		}
	}

	private static Object locateAspectReference(Node elem, String ref) {
		Object selectedElement = null;
		if (StringUtils.hasText(ref)) {
			NodeList beans = elem.getOwnerDocument().getElementsByTagName(
					"bean");
			if (beans != null && beans.getLength() > 0) {
				for (int i = 0; i < beans.getLength(); i++) {
					Node node = beans.item(i);
					if (ref.equals(BeansEditorUtils.getAttribute(node, "id"))) {
						selectedElement = node;
						break;
					}
				}
			}

			if (selectedElement == null) {
				selectedElement = elem.getOwnerDocument().getElementById(ref);
			}
		}
		return selectedElement;
	}

	public static IJavaElement getSelectedJavaElement(IWorkbenchPart part,
			ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object first = structuredSelection.getFirstElement();
			if (first instanceof IJavaElement) {
				if (!(first instanceof IJavaProject)) {
					return (IJavaElement) first;
				}
			}
		}
		else if (part instanceof IEditorPart
				&& selection instanceof ITextSelection) {
			if (part instanceof JavaEditor) {
				JavaEditor je = (JavaEditor) part;
				ISourceReference sourceRef = computeHighlightRangeSourceReference(je);
				IJavaElement javaElement = (IJavaElement) sourceRef;
				return javaElement;
			}
		}
		return null;
	}

	/**
	 * Computes and returns the source reference. This is taken from the
	 * computeHighlightRangeSourceReference() method in the JavaEditor class
	 * which is used to populate the outline view
	 * @return the computed source reference
	 */
	public static ISourceReference computeHighlightRangeSourceReference(
			JavaEditor editor) {
		ISourceViewer sourceViewer = editor.getViewer();
		if (sourceViewer == null)
			return null;

		StyledText styledText = sourceViewer.getTextWidget();
		if (styledText == null)
			return null;

		int caret = 0;
		if (sourceViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension = (ITextViewerExtension5) sourceViewer;
			caret = extension.widgetOffset2ModelOffset(styledText
					.getCaretOffset());
		}
		else {
			int offset = sourceViewer.getVisibleRegion().getOffset();
			caret = offset + styledText.getCaretOffset();
		}

		IJavaElement element = getElementAt(editor, caret, false);

		if (!(element instanceof ISourceReference))
			return null;

		if (element.getElementType() == IJavaElement.IMPORT_DECLARATION) {

			IImportDeclaration declaration = (IImportDeclaration) element;
			IImportContainer container = (IImportContainer) declaration
					.getParent();
			ISourceRange srcRange = null;
			try {
				srcRange = container.getSourceRange();
			}
			catch (JavaModelException e) {
			}

			if (srcRange != null && srcRange.getOffset() == caret)
				return container;
		}

		return (ISourceReference) element;
	}

	private static Map<Object, Object> workingCopyManagersForEditors = new HashMap<Object, Object>();

	@SuppressWarnings("unused")
	private static boolean selectedOutsideJavaElement = false;

	/**
	 * Returns the most narrow java element including the given offset. This is
	 * taken from the getElementAt(int offset, boolean reconcile) method in the
	 * CompilationUnitEditor class.
	 */
	private static IJavaElement getElementAt(JavaEditor editor, int offset,
			boolean reconcile) {
		IWorkingCopyManager manager;
		if (workingCopyManagersForEditors.get(editor) instanceof IWorkingCopyManager) {
			manager = (IWorkingCopyManager) workingCopyManagersForEditors
					.get(editor);
		}
		else {
			manager = JavaPlugin.getDefault().getWorkingCopyManager();
		}
		ICompilationUnit unit = manager.getWorkingCopy(editor.getEditorInput());

		if (unit != null) {
			try {
				if (reconcile) {
					synchronized (unit) {
						unit.reconcile(ICompilationUnit.NO_AST, false, null,
								null);
					}
					IJavaElement elementAt = unit.getElementAt(offset);
					if (elementAt != null) {
						return elementAt;
					}
					// this is if the selection in the editor
					// is outside the {} of the class or aspect
					IJavaElement[] children = unit.getChildren();
					for (IJavaElement element : children) {
						if (element instanceof SourceType) {
							return element;
						}
					}
				}
				else if (unit.isConsistent()) {
					// Bug 96313 - if there is no IJavaElement for the
					// given offset, then check whether there are any
					// children for this CU. There are if you've selected
					// somewhere in the file and there aren't if there are
					// compilation errors. Therefore, return one of these
					// children and calculate the xrefs as though the user
					// wants to display the xrefs for the entire file
					IJavaElement elementAt = unit.getElementAt(offset);
					if (elementAt != null) {
						// a javaElement has been selected, therefore
						// no need to go any further
						return elementAt;
					}
					IResource res = unit.getCorrespondingResource();
					if (res instanceof IFile) {
						IFile file = (IFile) res;
						IProject containingProject = file.getProject();
						IMarker[] javaModelMarkers = containingProject
								.findMarkers(
										IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER,
										false, IResource.DEPTH_INFINITE);
						for (IMarker marker : javaModelMarkers) {
							if (marker.getResource().equals(file)) {
								// there is an error in the file, therefore
								// we don't want to return any xrefs
								return null;
							}
						}
					}
					// the selection was outside an IJavaElement, however, there
					// are children for this compilation unit so we think you've
					// selected outside of a java element.
					if (elementAt == null && unit.getChildren().length != 0) {
						selectedOutsideJavaElement = true;
						return unit.getChildren()[0];
					}
				}

			}
			catch (JavaModelException x) {
				if (!x.isDoesNotExist())
					JavaPlugin.log(x.getStatus());
				// nothing found, be tolerant and go on
			}
			catch (CoreException e) {
			}
		}

		return null;
	}

	public static int getLineNumber(IMember member) {
		try {
			ICompilationUnit compUnit = member.getCompilationUnit();
			if (compUnit != null) {
				Document document = new Document(compUnit.getBuffer()
						.getContents());
				return document.getLineOfOffset(member.getSourceRange()
						.getOffset());
			}
		}
		catch (JavaModelException e) {
		}
		catch (BadLocationException e) {
		}
		return -1;
	}
}
