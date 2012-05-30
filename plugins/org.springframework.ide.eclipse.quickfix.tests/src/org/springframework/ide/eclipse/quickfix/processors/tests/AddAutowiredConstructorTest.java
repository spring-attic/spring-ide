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

package org.springframework.ide.eclipse.quickfix.processors.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.springframework.ide.eclipse.quickfix.QuickfixUtils;
import org.springframework.ide.eclipse.quickfix.jdt.processors.AddAutowireConstructorQuickAssistProcessor;
import org.springsource.ide.eclipse.commons.core.FileUtil;
import org.springsource.ide.eclipse.commons.tests.util.StsTestCase;

/**
 * @author Kaitlin Duck Sherwood
 */
@SuppressWarnings("restriction")
public class AddAutowiredConstructorTest extends StsTestCase {

	@Override
	protected String getBundleName() {
		return "org.springframework.ide.eclipse.quickfix.tests"; //$NON-NLS-1$
	}

	AddAutowireConstructorQuickAssistProcessor computer;

	CompilationUnit cu;

	private String testFileText;

	private IFile testFile;

	protected CompilationUnitEditor openFileInEditor(IFile file) throws CoreException, IOException {

		if (file.exists()) {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (window != null) {
				IWorkbenchPage page = window.getActivePage();
				if (page != null) {
					IEditorPart editor = IDE.openEditor(page, file);
					if (editor != null) {
						return ((CompilationUnitEditor) editor);
					}
				}
			}
		}
		return null;
	}

	protected IEditorPart openEditor() throws CoreException, IOException {
		CompilationUnitEditor editor = openFileInEditor(testFile);

		return editor;
	}

	@Override
	public void setUp() throws Exception {
		computer = new AddAutowireConstructorQuickAssistProcessor();

		IProject project = createPredefinedProject("Test");
		IJavaProject javaProject = (IJavaProject) project.getNature(JavaCore.NATURE_ID);

		IType contextConfigurationTestType = javaProject.findType("com.test.AddAutowiredConstructorTest");

		testFile = (IFile) contextConfigurationTestType.getResource();
		testFileText = FileUtil.readFile(testFile.getRawLocation().makeAbsolute().toFile(), new NullProgressMonitor());
	}

	private JavaContentAssistInvocationContext createTestContext(int offset) {

		try {
			IEditorPart editorPart = openEditor();

			if (editorPart instanceof CompilationUnitEditor) {
				CompilationUnitEditor sourceEditor = (CompilationUnitEditor) editorPart;
				ITextViewer sourceViewer = sourceEditor.getViewer();

				JavaContentAssistInvocationContext testContext = new JavaContentAssistInvocationContext(sourceViewer,
						offset, editorPart);

				return testContext;
			}
			else {
				assertTrue(false);
			}
		}
		catch (CoreException e) {
			assertTrue(false);
		}
		catch (IOException e) {
			assertTrue(false);
		}

		assertTrue(false);
		return null;
	}

	public static String stripString(String aString) {
		String htmlText = aString.replaceAll("<br>", " "); //$NON-NLS-1$
		htmlText = htmlText.replaceAll(".*@Autowired", "@Autowired"); //$NON-NLS-1$
		htmlText = htmlText.replaceAll("\\}[^\\}]*$", ""); //$NON-NLS-1$
		htmlText = htmlText.replaceAll("<b>", ""); //$NON-NLS-1$
		htmlText = htmlText.replaceAll("</b>", ""); //$NON-NLS-1$
		htmlText = htmlText.replaceAll("\t", " "); //$NON-NLS-1$
		htmlText = htmlText.replaceAll("\n", " "); //$NON-NLS-1$
		htmlText = htmlText.replaceAll("\\.\\.\\.", ""); //$NON-NLS-1$
		htmlText = htmlText.replaceAll("\\s+", " "); //$NON-NLS-1$
		htmlText = htmlText.trim();

		return htmlText;
	}

	public void testFrameworkSmokeTest() {
		// just want to make sure that the most basic test doesn't explode.
		createTestContext(3);
	}

	private int computeInvocationPointOffset(String referenceString, int characterOffset) {
		int referencePosition = testFileText.indexOf(referenceString);
		assertTrue(referencePosition >= 0);
		return referencePosition - characterOffset;
	}

	private void checkResults(String returnedString, String expectedString) {
		if (!returnedString.equals(expectedString)) {
			assertTrue(checkPositionIndependentResults(returnedString, expectedString));
		}
	}

	// typeBinding.getDeclaredFields() claims that it returns fields
	// in no particular order; in fact, they are returned in name-alpha order.
	// However, this might change someday, at which point we need to make sure
	// that the strings are the same even out of order.
	// You can construct pathological strings which pass this test but
	// which are not correct, but it is unlikely that
	// StubUtility2.createConstructorStub() will be so wrong;
	// if StubUtility2.createConstructorStub() *is* that wrong, we have
	// bigger problems.
	private boolean checkPositionIndependentResults(String returnedString, String expectedString) {
		returnedString = returnedString.replaceAll("\\(", "( "); //$NON-NLS-1$
		expectedString = expectedString.replaceAll("\\(", "( "); //$NON-NLS-1$
		returnedString = returnedString.replaceAll(",", " ,"); //$NON-NLS-1$
		expectedString = expectedString.replaceAll(",", " ,"); //$NON-NLS-1$
		returnedString = returnedString.replaceAll("\\)", " )"); //$NON-NLS-1$
		expectedString = expectedString.replaceAll("\\)", " )"); //$NON-NLS-1$

		String[] returnedSubstrings = returnedString.split(" ");
		String[] expectedSubstrings = expectedString.split(" ");

		assertTrue(returnedSubstrings.length == expectedSubstrings.length);

		Arrays.sort(returnedSubstrings);
		Arrays.sort(expectedSubstrings);

		for (int i = 0; i < expectedSubstrings.length; i++) {
			if (!returnedSubstrings[i].equals(expectedSubstrings[i])) {
				System.err.println("ERROR: expected >" + expectedSubstrings[i] + "< but got >" + returnedSubstrings[i]
						+ "<"); //$NON-NLS-1$
				return false;
			}
		}

		return true;
	}

	private List<IJavaCompletionProposal> getProposals(String label, int invocationOffset) {
		ContentAssistInvocationContext context = createTestContext(invocationOffset);
		if (context instanceof JavaContentAssistInvocationContext) {
			ICompilationUnit cu = ((JavaContentAssistInvocationContext) context).getCompilationUnit();
			BodyDeclaration bodyDecl = QuickfixUtils.getTypeDecl(label, cu);
			if (bodyDecl instanceof TypeDeclaration) {
				TypeDeclaration typeDecl = (TypeDeclaration) bodyDecl;

				IInvocationContext invocationContext = new AssistContext(cu, invocationOffset, 0);
				if (computer.isQuickfixAvailable(typeDecl, invocationContext)) {
					SimpleName name = typeDecl.getName();
					List<IJavaCompletionProposal> proposals = computer.getAssistsForType(typeDecl, name, cu);
					return proposals;
				}
				else {
					return new ArrayList<IJavaCompletionProposal>();
				}

			}
		}

		return new ArrayList<IJavaCompletionProposal>();
	}

	private void helperForTests(String label, int expectedProposalCount, String expectedString) {
		String referenceString = "class " + label; //$NON-NLS-1$
		int characterOffset = label.length() - 3;

		int invocationOffset = computeInvocationPointOffset(referenceString, characterOffset);
		List<IJavaCompletionProposal> proposals = getProposals(label, invocationOffset);
		assertTrue(proposals.size() == expectedProposalCount);

		if (proposals.size() > 0) {
			assertTrue(expectedString != null);
			IJavaCompletionProposal proposal = proposals.get(0);
			String returnedString = stripString(proposal.getAdditionalProposalInfo());
			checkResults(returnedString, expectedString);
		}

	}

	public void testOuterClass() {
		String label = "AddAutowiredConstructorTest"; //$NON-NLS-1$
		int expectedProposalCount = 1;
		// This is not the string returned, but is useful for testing
		// checkPositionIndependentResults()
		String expectedString = "@Autowired public AddAutowiredConstructorTest(String privateFinalString, int privateFinalInt) { this.privateFinalString = privateFinalString; this.privateFinalInt = privateFinalInt;"; //$NON-NLS-1$

		// This is the string returned:
		// String expectedString =
		// "@Autowired public AddAutowiredConstructorTest(int privateFinalInt, String privateFinalString) { this.privateFinalInt = privateFinalInt; this.privateFinalString = privateFinalString;";

		helperForTests(label, expectedProposalCount, expectedString);
	}

	public void testFinalsNoConstructor() {
		String label = "FinalsNoConstructor"; //$NON-NLS-1$
		int expectedProposalCount = 1;
		String expectedString = "@Autowired public FinalsNoConstructor(int privateFinalInt1, String privateFinalString1, String publicFinalString1) { this.privateFinalInt1 = privateFinalInt1; this.privateFinalString1 = privateFinalString1; this.publicFinalString1 = publicFinalString1;"; //$NON-NLS-1$

		helperForTests(label, expectedProposalCount, expectedString);
	}

	public void testFinalsDefaultConstructor() {
		String expectedString = "@Autowired public FinalsDefaultConstructor(int privateFinalInt2, String privateFinalString2, String publicFinalString2) { this.privateFinalInt2 = privateFinalInt2; this.privateFinalString2 = privateFinalString2; this.publicFinalString2 = publicFinalString2;"; //$NON-NLS-1$
		String label = "FinalsDefaultConstructor"; //$NON-NLS-1$
		int expectedProposalCount = 1;

		helperForTests(label, expectedProposalCount, expectedString);

	}

	public void testFinalsConstructor() {
		String label = "FinalsConstructor"; //$NON-NLS-1$
		String expectedString = null;
		int expectedProposalCount = 0;

		helperForTests(label, expectedProposalCount, expectedString);

	}

	public void testNoFinalsConstructor() {
		String label = "NoFinalsConstructor"; //$NON-NLS-1$
		String expectedString = null;
		int expectedProposalCount = 0;

		helperForTests(label, expectedProposalCount, expectedString);

	}

	// NOTE: the content assist framework blocks giving this as an option
	// (isQuickfixAvailable never gets called) but we can still get a
	// proposal out of it.
	public void testExtendingNoFinalsConstructor() {
		String label = "ExtendingFinalsNoConstructor"; //$NON-NLS-1$
		String expectedString = "@Autowired public ExtendingFinalsNoConstructor(int privateFinalInt5, String privateFinalString5, String publicFinalString5) { this.privateFinalInt5 = privateFinalInt5; this.privateFinalString5 = privateFinalString5; this.publicFinalString5 = publicFinalString5;"; //$NON-NLS-1$
		int expectedProposalCount = 1;

		helperForTests(label, expectedProposalCount, expectedString);

	}

	// NOTE: the content assist framework blocks giving this as an option
	// (isQuickfixAvailable never gets called) but we can still get a
	// proposal out of it.
	public void testImplementingFinalsNoConstructor() {
		String label = "ImplementingFinalsNoConstructor"; //$NON-NLS-1$
		String expectedString = "@Autowired public ImplementingFinalsNoConstructor(int privateFinalInt6, String privateFinalString6, String publicFinalString6) { this.privateFinalInt6 = privateFinalInt6; this.privateFinalString6 = privateFinalString6; this.publicFinalString6 = publicFinalString6;"; //$NON-NLS-1$
		int expectedProposalCount = 1;

		helperForTests(label, expectedProposalCount, expectedString);

	}
}
