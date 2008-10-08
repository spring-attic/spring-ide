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
package org.springframework.ide.eclipse.ui.navigator.actions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportContainer;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.navigator.NavigatorContentService;
import org.eclipse.ui.internal.navigator.extensions.LinkHelperService;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.ILinkHelper;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.ui.dialogs.WrappingStructuredSelection;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * {@link CommonNavigator} extension that supports a special "Link to Editor mode" that selects
 * {@link IModelElement} instances based on selections in the XML files.
 * <p>
 * Actual resolution of {@link Element} instances to {@link IModelElement} instances is delegated to
 * implementations of the {@link ILinkHelperExtension} interface. Those implementations can be
 * contributed as usual by using the link helper content contribution of the common navigator
 * framework.
 * @author Christian Dupuis
 * @since 2.2.0
 */
@SuppressWarnings("restriction")
public final class SpringNavigator extends CommonNavigator implements ISelectionListener {

	/** Mapping between working copy managers and open editors */
	private static Map<Object, Object> workingCopyManagersForEditors = new HashMap<Object, Object>();

	/**
	 * Last selected element; stored in order to prevent updating on selecting the same element
	 * again
	 */
	private ISelection lastElement;

	/** Stored {@link LinkHelperService} to resolve instances of {@link ILinkHelperExtension} */
	private LinkHelperService linkService;

	private IPropertyListener propertyListener;

	/**
	 * Register the {@link ISelectionListener} with the workbench.
	 */
	@Override
	public void createPartControl(Composite aParent) {
		super.createPartControl(aParent);
		getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener(this);
		propertyListener = new IPropertyListener() {

			public void propertyChanged(Object source, int propId) {
				if (propId == IS_LINKING_ENABLED_PROPERTY) {
					updateTreeViewer(SpringNavigator.this, lastElement, false);
				}
			}
			
		};
		addPropertyListener(propertyListener);

	}

	/**
	 * Remove the {@link ISelectionListener} from the workbench.
	 */
	@Override
	public void dispose() {
		getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
		removePropertyListener(propertyListener);
		super.dispose();
	}

	/**
	 * {@inheritDoc}
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			selection = new WrappingStructuredSelection((IStructuredSelection) selection);
		}
		updateTreeViewer(part, selection, true);
	}

	/**
	 * Computes and returns the source reference. This is taken from the
	 * computeHighlightRangeSourceReference() method in the JavaEditor class which is used to
	 * populate the outline view
	 * @return the computed source reference
	 */
	private ISourceReference computeHighlightRangeSourceReference(JavaEditor editor) {
		ISourceViewer sourceViewer = editor.getViewer();
		if (sourceViewer == null)
			return null;

		StyledText styledText = sourceViewer.getTextWidget();
		if (styledText == null)
			return null;

		int caret = 0;
		if (sourceViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension = (ITextViewerExtension5) sourceViewer;
			caret = extension.widgetOffset2ModelOffset(styledText.getCaretOffset());
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
			IImportContainer container = (IImportContainer) declaration.getParent();
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

	private void determineAndRefreshViewer(IWorkbenchPart part, ISelection selection,
			boolean ignoreSameSelection) {
		final Object element = getSelectedElement(part, selection);
		if (element == null || (element.equals(lastElement) && ignoreSameSelection)) {
			return;
		}
		if ((element instanceof IType || element instanceof IMethod || element instanceof IField
				|| element instanceof Element || element instanceof IResource)
				&& isLinkingEnabled()) {
			selectReveal(getCommonViewer(), element);
		}
		lastElement = selection;
	}

	/**
	 * Returns the most narrow java element including the given offset. This is taken from the
	 * getElementAt(int offset, boolean reconcile) method in the CompilationUnitEditor class.
	 */
	private IJavaElement getElementAt(JavaEditor editor, int offset, boolean reconcile) {
		IWorkingCopyManager manager;
		if (workingCopyManagersForEditors.get(editor) instanceof IWorkingCopyManager) {
			manager = (IWorkingCopyManager) workingCopyManagersForEditors.get(editor);
		}
		else {
			manager = JavaPlugin.getDefault().getWorkingCopyManager();
		}
		ICompilationUnit unit = manager.getWorkingCopy(editor.getEditorInput());

		if (unit != null) {
			try {
				if (reconcile) {
					synchronized (unit) {
						unit.reconcile(ICompilationUnit.NO_AST, false, null, null);
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
						IMarker[] javaModelMarkers = containingProject.findMarkers(
								IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false,
								IResource.DEPTH_INFINITE);
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

	/**
	 * Store the {@link LinkHelperService}.
	 */
	private synchronized LinkHelperService getLinkHelperService() {
		if (linkService == null) {
			linkService = new LinkHelperService((NavigatorContentService) getCommonViewer()
					.getNavigatorContentService());
		}
		return linkService;
	}

	/**
	 * Retrieves the element as represented by the given <code>selection</code>.
	 */
	private Object getSelectedElement(IWorkbenchPart part, ISelection selection) {
		Object selectedElement = getSelectedJavaElement(part, selection);

		if (selectedElement == null) {
			selectedElement = getSelectedXmlElement(selection);

		}
		return selectedElement;
	}

	private IJavaElement getSelectedJavaElement(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object first = structuredSelection.getFirstElement();
			if (first instanceof IJavaElement) {
				if (!(first instanceof IJavaProject)) {
					return (IJavaElement) first;
				}
			}
		}
		else if (part instanceof IEditorPart && selection instanceof ITextSelection) {
			if (part instanceof JavaEditor) {
				JavaEditor je = (JavaEditor) part;
				ISourceReference sourceRef = computeHighlightRangeSourceReference(je);
				IJavaElement javaElement = (IJavaElement) sourceRef;
				return javaElement;
			}
		}
		return null;
	}

	private Object getSelectedXmlElement(ISelection selection) {
		Object selectedElement = null;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structSelection = (IStructuredSelection) selection;
			Object obj = structSelection.getFirstElement();
			if (obj instanceof Element) {
				selectedElement = obj;
			}
			else if (obj instanceof Text) {
				Node parent = ((Text) obj).getParentNode();
				if (parent instanceof Element) {
					selectedElement = parent;
				}
			}
			else if (obj instanceof Comment) {
				Node parent = ((Comment) obj).getParentNode();
				if (parent instanceof Element) {
					selectedElement = parent;
				}
			}
		}
		return selectedElement;
	}

	private void selectReveal(TreeViewer viewer, Object element) {
		ILinkHelper[] helpers = getLinkHelperService().getLinkHelpersFor(element);
		for (ILinkHelper helper : helpers) {
			if (helper instanceof ILinkHelperExtension) {
				ISelection selection = ((ILinkHelperExtension) helper).findSelection(element);
				if (selection != null) {
					viewer.getTree().setRedraw(false);
					super.selectReveal(selection);
					viewer.getTree().setRedraw(true);
					break;
				}
			}
		}
	}

	private void updateTreeViewer(final IWorkbenchPart part, final ISelection selection,
			final boolean ignoreSameSelection) {
		// Abort if this happens after disposes
		Control ctrl = getCommonViewer().getControl();
		if (ctrl == null || ctrl.isDisposed()) {
			return;
		}

		// Are we in the UI thread?
		if (ctrl.getDisplay().getThread() == Thread.currentThread()) {
			determineAndRefreshViewer(part, selection, ignoreSameSelection);
		}
		else {
			ctrl.getDisplay().asyncExec(new Runnable() {
				public void run() {

					// Abort if this happens after disposes
					Control ctrl = getCommonViewer().getControl();
					if (ctrl == null || ctrl.isDisposed()) {
						return;
					}
					determineAndRefreshViewer(part, selection, ignoreSameSelection);
				}
			});
		}
	}

}
