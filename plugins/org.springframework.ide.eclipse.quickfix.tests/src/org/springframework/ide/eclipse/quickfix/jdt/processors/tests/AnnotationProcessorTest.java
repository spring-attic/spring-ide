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
package org.springframework.ide.eclipse.quickfix.jdt.processors.tests;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModel;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springsource.ide.eclipse.commons.tests.util.StsTestCase;

/**
 * @author Terry Denney
 */
@SuppressWarnings("restriction")
public abstract class AnnotationProcessorTest extends StsTestCase {

	protected IJavaProject javaProject;

	protected IType type;

	protected ISourceViewer viewer;

	protected void setUp(String javaClassName) throws Exception {
		IProject project = createPredefinedProject("Test");

		IFile xmlFile = (IFile) project.findMember("src/jdt-processor.xml");

		((BeansModel) BeansCorePlugin.getModel()).start();

		IBeansProject springProject = BeansCorePlugin.getModel().getProject(project);
		if (springProject != null && springProject instanceof BeansProject) {
			((BeansProject) springProject).addConfig(xmlFile, IBeansConfig.Type.MANUAL);
		}

		javaProject = (IJavaProject) project.getNature(JavaCore.NATURE_ID);
		type = javaProject.findType(javaClassName);

		IFile file = (IFile) type.getResource();
		viewer = getViewer(file);
	}

	protected ASTNode getASTNode(ISourceRange sourceRange, IType type, ISourceViewer viewer) {
		AssistContext assistContext = new AssistContext(type.getCompilationUnit(), viewer, sourceRange.getOffset(),
				sourceRange.getLength());
		return assistContext.getCoveringNode();
	}

	protected ISourceViewer getViewer(final IFile file) {
		JavaEditor editor = UIThreadRunnable.syncExec(new Result<JavaEditor>() {
			public JavaEditor run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				assertNotNull("Expected active workbench window", window);
				IWorkbenchPage page = window.getActivePage();
				assertNotNull("Expected active workbench page", page);
				IEditorPart editor;
				try {
					editor = IDE.openEditor(page, file);
				}
				catch (PartInitException e) {
					throw new RuntimeException(e);
				}
				return (JavaEditor) editor;
			}
		});

		return editor.getViewer();
	}

	@Override
	protected String getBundleName() {
		return "org.springframework.ide.eclipse.quickfix.tests";
	}

	protected IInvocationContext getContext(final ISourceRange sourceRange, final IMember member, final ASTNode node) {
		return getContext(sourceRange, member, node, 0);
	}

	protected IInvocationContext getContext(final ISourceRange sourceRange, final IMember member, final ASTNode node,
			final int offset) {
		return new IInvocationContext() {

			public int getSelectionOffset() {
				return sourceRange.getOffset() + offset; // check to see if
															// really needed
			}

			public int getSelectionLength() {
				return 0;
			}

			public ASTNode getCoveringNode() {
				return node;
			}

			public ASTNode getCoveredNode() {
				return node;
			}

			public ICompilationUnit getCompilationUnit() {
				return member.getCompilationUnit();
			}

			public CompilationUnit getASTRoot() {
				return getCompilationUnit(node);
			}

			private CompilationUnit getCompilationUnit(ASTNode node) {
				if (node == null || node instanceof CompilationUnit) {
					return (CompilationUnit) node;
				}

				return getCompilationUnit(node.getParent());
			}
		};
	}

}
