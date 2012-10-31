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

/**
 * @author Kaitlin Duck Sherwood
 */
package org.springframework.ide.eclipse.quickfix.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ui.IEditorPart;
import org.springframework.ide.eclipse.quickfix.jdt.computers.ConfigurationLocationProposalComputer;
import org.springsource.ide.eclipse.commons.core.FileUtil;

// NOTE: This is not an end-to-end test.  There are some checks on the proposals *after* 
// they are generated but before they are presented to the user.  This only checks the 
// creation of the proposals.

public class ConfigurationLocationProposalComputerTest extends AbstractCompilationUnitTestCase {

	ConfigurationLocationProposalComputer computer;

	private String testFileText;

	private final int newlineLength = 2;

	private final int leadingWhitespaceLength = 1;

	private IFile testFile;

	protected IEditorPart openEditor() throws CoreException, IOException {
		return openFileInEditor(testFile);
	}

	@Override
	public void setUp() throws Exception {
		computer = new ConfigurationLocationProposalComputer();

		IProject project = createPredefinedProject("Test");
		IJavaProject javaProject = (IJavaProject) project.getNature(JavaCore.NATURE_ID);

		IType contextConfigurationTestType = javaProject.findType("com.test.ContextConfigurationTests");

		testFile = (IFile) contextConfigurationTestType.getResource();
		testFileText = FileUtil.readFile(testFile.getRawLocation().makeAbsolute().toFile(), new NullProgressMonitor());

	}

	@SuppressWarnings("restriction")
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

	public void testFrameworkSmokeTest() {
		// just want to make sure that the most basic test doesn't explode.
		createTestContext(3);
	}

	public void testConfigFilePatternMatching() {
		java.util.regex.Pattern pattern = computer.getConfigFileCompiledPattern();

		ArrayList<String> validStrings = new ArrayList<String>();
		validStrings.add("");
		validStrings.add("\"foo.xml");
		validStrings.add("\"foo.xml\"");
		validStrings.add("\"foo.xml\",");
		validStrings.add("\"foo.xml\",\"");
		validStrings.add("\"foo.xml\",\"\"");
		validStrings.add("\"foo.xml\",\"bar.xml\"");
		validStrings.add("\"foo.xml\",\"bar.xml\",");
		validStrings.add("\"foo.xml\",\"bar.xml\",");
		validStrings.add("\"foo.xml\", ");
		validStrings.add("\"foo.xml\", \"");
		validStrings.add("\"foo.xml\", \"\"");
		validStrings.add("\"foo.xml\", \"bar.xml\"");
		validStrings.add("\"foo.xml\", \"bar.xml\",");
		validStrings.add("\"foo.xml\", \"bar.xml\",");
		validStrings.add("\"src/foo-blah.xml");
		validStrings.add("\"src/foo-blah.xml\"");
		validStrings.add("\"src/foo-blah\",");
		validStrings.add("\"src/foo-blah\",\"");
		validStrings.add("\"src/foo-blah\",\"\"");
		validStrings.add("\"src/foo-blah\",\"bar07_ gorp.xml\"");
		validStrings.add("\"src/foo-blah\",\"bar07_ gorp.xml\",");
		validStrings.add("\"src/foo-blah\",\"bar07_ gorp.xml\",");
		validStrings.add("\"src/foo-blah\", ");
		validStrings.add("\"src/foo-blah\", \"");
		validStrings.add("\"src/foo-blah\", \"\"");
		validStrings.add("\"src/foo-blah\", \"bar07_ gorp.xml\"");
		validStrings.add("\"src/foo-blah\", \"bar07_@gorp.xml\",");
		validStrings.add("\"src/foo-blah\", \"bar07_/?gorp.xml\",");

		ArrayList<String> prefixes = new ArrayList<String>();
		prefixes.add("");
		prefixes.add(" ");
		prefixes.add("   ");

		ArrayList<String> postfixes = new ArrayList<String>();
		postfixes.add("");
		postfixes.add(" ");
		postfixes.add("   ");
		postfixes.add("}");
		postfixes.add(" }");
		postfixes.add(" }  ");
		postfixes.add("})");
		postfixes.add(" } )");
		postfixes.add(" }  ) ");

		// test base strings
		for (String validString : validStrings) {
			for (String prefix : prefixes) {
				for (String postfix : postfixes) {

					String testString = prefix + validString + postfix;

					Matcher matcher = pattern.matcher(testString);
					assertTrue(matcher.find());
				}
			}
		}
	}

	public void testValidPatterns() {
		java.util.regex.Pattern pattern = computer.getLineCompiledPattern();

		ArrayList<String> validStrings = new ArrayList<String>();
		validStrings.add("locations=");
		validStrings.add("locations ={");
		validStrings.add("locations= {\"\"");
		validStrings.add("locations={\"");
		validStrings.add("locations ={\"foo.xml\"");
		validStrings.add("locations  =  {\"foo.xml\",");
		validStrings.add("locations={\"foo.xml\",\"\"");
		validStrings.add("locations ={\"foo.xml\", \"\"");
		validStrings.add("locations ={ \"foo.xml\",");
		validStrings.add("locations= {\"foo.xml\",\"bar.xml\"");
		validStrings.add("locations ={\"foo.xml\", \"bar.xml\"");
		validStrings.add("locations = {\"foo.xml\",\"bar.xml\",");
		validStrings.add("locations={\"foo.xml\",\"bar.xml\", ");
		validStrings.add("locations ={\"src/foo0-blah_funky.xml\"");
		validStrings.add("locations={\"src/foo0-blah_funky.xml\",");
		validStrings.add("locations= {\"src/foo0-blah_funky.xml\",\"\"");
		validStrings.add("locations  ={\"src/foo0-blah_funky.xml\", \"\"");
		validStrings.add("locations  ={ \"src/foo0-blah_funky.xml\",");
		validStrings.add("locations={\"src/foo0-blah_funky.xml\",\"bar.xml\"");
		validStrings.add("locations= {\"src/foo0-blah_funky.xml\", \"bar.xml\"");
		validStrings.add("locations ={\"src/foo0-blah_funky.xml\",\"bar.xml\",");
		validStrings.add("locations = {\"src/foo0-blah_funky.xml\",\"bar.xml\", ");

		// other options are not legal now, but they might be someday
		ArrayList<String> otherOptions = new ArrayList<String>();
		otherOptions.add("");
		otherOptions.add("x=53,");
		otherOptions.add("x=53, y={45, 25},");
		otherOptions.add("x=53, y={45, 25}, z=\"safdj.gorosflj\",");
		otherOptions.add("x=53, y={45, 25}, z={\"safdj.gorosflj\" , \"asdfl.alsdfj.asldfj\"},");

		// test base strings
		for (String validString : validStrings) {
			for (String extraneousString : otherOptions) {
				String testString = "(" + extraneousString + validString;

				Matcher matcher = pattern.matcher(testString);
				assertTrue(matcher.find());
			}
		}

		// test with leading (
		for (String validString : validStrings) {
			for (String extraneousString : otherOptions) {

				String testString = "(" + extraneousString + validString;

				Matcher matcher = pattern.matcher(testString);
				assertTrue(matcher.find());
			}
		}

		// test with } and })
		for (String validString : validStrings) {
			for (String extraneousString : otherOptions) {

				String testString = "(" + extraneousString + validString + "}";

				Matcher matcher = pattern.matcher(testString);
				assertTrue(matcher.find());

				testString = "(" + extraneousString + validString + "})";

				matcher = pattern.matcher(testString);
				assertTrue(matcher.find());
			}
		}

	}

	private ArrayList<String> getListOfPossibleClasspathConfigFiles() {
		ArrayList<String> possibleFiles = new ArrayList<String>();
		possibleFiles.add("classpath:add-constructor-arg-proposal.xml");
		possibleFiles.add("classpath:add-constructor-param-proposal.xml");
		possibleFiles.add("classpath:autowire.xml");
		possibleFiles.add("classpath:bean-ref-attribute.xml");
		possibleFiles.add("classpath:class-attribute.xml");
		possibleFiles.add("classpath:create-bean-proposal.xml");
		possibleFiles.add("classpath:create-class-proposal.xml");
		possibleFiles.add("classpath:create-constructor-proposal.xml");
		possibleFiles.add("classpath:create-method-proposal.xml");
		possibleFiles.add("classpath:factory-method-test.xml");
		possibleFiles.add("classpath:method-attribute.xml");
		possibleFiles.add("classpath:placeholder.xml");
		possibleFiles.add("classpath:property-attribute.xml");
		possibleFiles.add("classpath:quickfix-util.xml");
		possibleFiles.add("classpath:remove-constructor-arg-proposal.xml");
		possibleFiles.add("classpath:remove-constructor-param-proposal.xml");
		possibleFiles.add("classpath:rename-property-proposal.xml");
		possibleFiles.add("classpath:rename-proposal.xml");
		possibleFiles.add("classpath:namespace-elements.xml");
		possibleFiles.add("classpath:factory-bean-test.xml");
		possibleFiles.add("classpath:import-proposal-test.xml");
		possibleFiles.add("classpath:import-test.xml");
		possibleFiles.add("classpath:import-test.xml");
		possibleFiles.add("classpath:config-set-proposal-test.xml");
		possibleFiles.add("classpath:subdir/content-configuration-assist-test.xml");
		return possibleFiles;
	}

	private ArrayList<String> getListOfPossibleNonClasspathConfigFiles() {
		ArrayList<String> possibleFiles = new ArrayList<String>();
		possibleFiles.add("file:non-classpath/arbitrary.xml");
		possibleFiles.add("file:non-classpath/non-classpath.xml");
		return possibleFiles;
	}

	private ArrayList<String> getListOfPossibleConfigFiles() {
		ArrayList<String> possibleFiles = getListOfPossibleClasspathConfigFiles();
		possibleFiles.addAll(getListOfPossibleNonClasspathConfigFiles());
		return possibleFiles;
	}

	private void assertValidcontextConfigurationProposals(List<ICompletionProposal> computedProposals, String prefix,
			String postfix, String label) {
		ArrayList<String> expectedProposals = createExpectedProposals(prefix, postfix);
		assertValidcontextConfigurationProposals(computedProposals, expectedProposals, label);
	}

	private ArrayList<String> createExpectedProposals(String prefix, String postfix) {
		ArrayList<String> configFilesList = getListOfPossibleConfigFiles();
		return createAffixedProposals(prefix, postfix, configFilesList);
	}

	private ArrayList<String> createAffixedProposals(String prefix, String postfix, ArrayList<String> configFilesList) {
		ArrayList<String> expectedProposals = new ArrayList<String>();

		for (String filename : configFilesList) {
			expectedProposals.add(prefix + filename + postfix);
		}
		return expectedProposals;
	}

	@SuppressWarnings("restriction")
	private void assertValidcontextConfigurationProposals(List<ICompletionProposal> proposals,
			ArrayList<String> expectedResultsList, String label) {

		String[] expectedResultsArray = expectedResultsList.toArray(new String[expectedResultsList.size()]);
		assertTrue(proposals.size() > 0);
		assertEquals(expectedResultsArray.length, proposals.size());
		int expectedResultsIndex = 0;
		for (Object element : proposals) {
			if (element instanceof JavaCompletionProposal) {
				JavaCompletionProposal proposal = (JavaCompletionProposal) element;

				String replacementString = proposal.getReplacementString();

				// There is always a proposal (from somewhere else) of
				// "ContextConfigurationTest", skip the one at index 0
				if (!(replacementString.equals("ContextConfigurationTest"))) {
					String expectedProposal = expectedResultsArray[expectedResultsIndex];
					String errorMessage = "";
					if (!(expectedProposal.equals(replacementString))) {
						errorMessage = "expected proposal: >" + expectedProposal + "<, got >" + replacementString + "<";
						System.err.println(errorMessage);
					}
					assertEquals(errorMessage, expectedProposal, replacementString);
				}
				expectedResultsIndex++;
			}
		}
	}

	private int getInvocationPointOffset(String referenceString, int characterOffset) {
		int referencePosition = testFileText.indexOf(referenceString);
		return referencePosition - characterOffset;
	}

	private void helper(String label, String referenceString, int characterOffset, String prefix, String postfix) {
		helper(label, referenceString, characterOffset, prefix, postfix, -1);
	}

	// NOTE: helper expects the characterOffset to say how much to go BACKWARDS
	// FIXME make characterOffset to forward for positive values
	private void helper(String label, String referenceString, int characterOffset, String prefix, String postfix,
			int offset) {
		int referencePosition = testFileText.indexOf(referenceString);

		int characterIndex = getInvocationPointOffset(referenceString, characterOffset);

		// This is purely to help with writing tests
		if ((characterIndex != offset) && (offset > 0)) {
			fail("############" + label + ": offest is " + offset + ", characterIndex is " + characterIndex
					+ " characterOffset should be " + (referencePosition - offset));
		}
		else {

			ContentAssistInvocationContext context = createTestContext(characterIndex);
			List<ICompletionProposal> proposals = computer.computeCompletionProposals(context, null);

			assertValidcontextConfigurationProposals(proposals, prefix, postfix, label);
		}
	}

	public void testFilenameCompletion() {
		// @ContextConfiguration(locations={"import^"})
		// @ContextConfiguration(locations={"importSomething.xml"^})
		int characterOffset = 12;
		String label = "testFilenameCompletion";
		String referenceString = "TestL ";

		int referencePosition = testFileText.indexOf(referenceString) - characterOffset;

		ContentAssistInvocationContext context = createTestContext(referencePosition);
		List<ICompletionProposal> proposals = computer.computeCompletionProposals(context, null);
		assertTrue(proposals.size() == 3);

	}

	public void testMultidirectoryFilenameCompletion() {
		// @ContextConfiguration(locations={ "subdir^"})
		// @ContextConfiguration(locations={ "subdir/content-assist-test.xml"^})
		String label = "testMultidirectoryFilenameCompletion";

		int position = testFileText.indexOf(label) + newlineLength + label.length()
				+ "\t@ContextConfiguration(locations = { \"subdir".length() - 1;

		ContentAssistInvocationContext context = createTestContext(position);
		List<ICompletionProposal> proposals = computer.computeCompletionProposals(context, null);
		assertTrue(proposals.size() == 1);

		String expectedString = "classpath:subdir/content-configuration-assist-test.xml";

		ICompletionProposal proposal = proposals.get(0);
		if (proposal instanceof JavaCompletionProposal) {
			String replacementString = ((JavaCompletionProposal) proposal).getReplacementString();
			if (!replacementString.equals(expectedString)) {
				System.err.println("ERROR: expected >" + expectedString + "< and got >" + replacementString + "<");
			}
			assertTrue(expectedString.equals(replacementString));
		}

	}

	public void testFilenameCompletionWithAdditionalFilename() {
		// @ContextConfiguration(locations={"add^", "foo.xml"})
		// @ContextConfiguration(locations={"addSomething.xml"^, "foo.xml"})
		int characterOffset = 23;
		String label = "testFilenameCompletionWithAdditionalFilename";
		String referenceString = "TestK ";

		int referencePosition = testFileText.indexOf(referenceString) - characterOffset;

		ContentAssistInvocationContext context = createTestContext(referencePosition);
		List<ICompletionProposal> proposals = computer.computeCompletionProposals(context, null);
		assertTrue(proposals.size() == 2);

	}

	public void testAfterQuoteNonExistentFile() {
		// @ContextConfiguration(locations={"doesNotExist^
		// NOP

		int characterOffset = 10;
		String label = "testAfterQuoteNonExistentFile";
		String referenceString = "TestB ";

		int referencePosition = testFileText.indexOf(referenceString) - characterOffset;

		ContentAssistInvocationContext context = createTestContext(referencePosition);
		List<ICompletionProposal> proposals = computer.computeCompletionProposals(context, null);
		assertTrue(proposals.size() == 0);

	}

	public void testAfterQuoteNonExistentFileWithSpace() {
		// @ContextConfiguration(locations={ "doesNotExist^
		// NOP

		int characterOffset = 11;
		String label = "testAfterQuoteNonExistentFile";
		String referenceString = "TestE ";

		int referencePosition = testFileText.indexOf(referenceString) - characterOffset;

		ContentAssistInvocationContext context = createTestContext(referencePosition);
		List<ICompletionProposal> proposals = computer.computeCompletionProposals(context, null);
		assertTrue(proposals.size() == 0);

	}

	public void testAfterQuoteNonExistentFileWithSpaceAndQuote() {
		// @ContextConfiguration(locations={ "doesNotExist^"
		// NOP

		int characterOffset = 12;
		String label = "testAfterQuoteNonExistentFile";
		String referenceString = "TestF ";

		int referencePosition = testFileText.indexOf(referenceString) - characterOffset;

		ContentAssistInvocationContext context = createTestContext(referencePosition);
		List<ICompletionProposal> proposals = computer.computeCompletionProposals(context, null);
		assertTrue(proposals.size() == 0);

	}

	public void testAfterQuoteNonExistentFileWithSpaceAndBrace() {
		// @ContextConfiguration(locations={"doesNotExist^}
		// NOP

		int characterOffset = 12;
		String label = "testAfterQuoteNonExistentFileWithBrace";
		String referenceString = "TestG ";

		int referencePosition = testFileText.indexOf(referenceString) - characterOffset;

		ContentAssistInvocationContext context = createTestContext(referencePosition);
		List<ICompletionProposal> proposals = computer.computeCompletionProposals(context, null);
		assertTrue(proposals.size() == 0);

	}

	public void testAfterQuoteNonExistentFileWithSpaceQuoteAndBrace() {
		// @ContextConfiguration(locations={ "doesNotExist^"}
		// NOP

		int characterOffset = 13;
		String label = "testAfterQuoteNonExistentFile";
		String referenceString = "TestH ";

		int referencePosition = testFileText.indexOf(referenceString) - characterOffset;

		ContentAssistInvocationContext context = createTestContext(referencePosition);
		List<ICompletionProposal> proposals = computer.computeCompletionProposals(context, null);
		assertTrue(proposals.size() == 0);

	}

	public void testAfterQuoteNonExistentFileWithSpaceAndFollowingFile() {
		// @ContextConfiguration(locations={ "doesNotExist^", "foo.xml"
		// NOP

		int characterOffset = 22;
		String label = "testAfterQuoteNonExistentFile";
		String referenceString = "TestI ";

		int referencePosition = testFileText.indexOf(referenceString) - characterOffset;

		ContentAssistInvocationContext context = createTestContext(referencePosition);
		List<ICompletionProposal> proposals = computer.computeCompletionProposals(context, null);
		assertTrue(proposals.size() == 0);

	}

	public void testAfterQuoteNonExistentFileWithSpaceFollowingFileAndBrace() {
		// @ContextConfiguration(locations={ "doesNotExist^", "foo.xml"}
		// NOP

		int characterOffset = 23;
		String label = "testAfterQuoteNonExistentFile";
		String referenceString = "TestI ";

		int referencePosition = testFileText.indexOf(referenceString) - characterOffset;

		ContentAssistInvocationContext context = createTestContext(referencePosition);
		List<ICompletionProposal> proposals = computer.computeCompletionProposals(context, null);
		assertTrue(proposals.size() == 0);

	}

	public void testOutsideEmptyString() {
		// @ContextConfiguration(locations=""^
		// @ContextConfiguration(locations="", "foo.xml"^

		String referenceString = "class Test2 ";
		String label = "testOutsideEmptyString";
		int characterOffset = 3;
		String prefix = ", \"";
		String postfix = "\"";

		helper(label, referenceString, characterOffset, prefix, postfix);
	}

	// optional
	public void testUnbalancedQuoteNoBrace() { //
		// @ContextConfiguration(locations="^
		// @ContextConfiguration(locations={"foo.xml^"

		String referenceString = "class Test3 ";
		String label = "testUnbalancedQuoteNoBrace";
		int characterOffset = 3;
		String prefix = "";
		String postfix = "\"";

		int referencePosition = testFileText.indexOf(referenceString) - characterOffset;

		ContentAssistInvocationContext context = createTestContext(referencePosition);
		List<ICompletionProposal> proposals = computer.computeCompletionProposals(context, null);
		// we don't care what the result is, since the missing {
		// means this is malformed, but we want it to not crash
	}

	// Redundant with testAfterBrace
	public void testEmptyBraces() {
		// @ContextConfiguration(locations={^}
		// @ContextConfiguration(locations={"foo.xml^"}
		String referenceString = "class Test4 ";
		String label = "testEmptyBraces";
		int characterOffset = 4;
		String prefix = "\"";
		String postfix = "\"";

		helper(label, referenceString, characterOffset, prefix, postfix);
	}

	public void testBracesInEmptyQuotes() {
		// @ContextConfiguration(locations={"^"}
		// @ContextConfiguration(locations={"foo.xml^"}
		String referenceString = "class Test6 ";
		String label = "testBracesInEmptyQuotes";
		int characterOffset = 5;
		String prefix = "";
		String postfix = "";

		helper(label, referenceString, characterOffset, prefix, postfix);
	}

	public void testAfterOneElement() {
		// @ContextConfiguration(locations={"one.xml"^
		// @ContextConfiguration(locations={"one.xml","foo.xml"

		String referenceString = "class Test7 ";
		String label = "testAfterOneElement";
		int characterOffset = 3;
		String prefix = ", \"";
		String postfix = "\"";

		helper(label, referenceString, characterOffset, prefix, postfix);
	}

	public void testAfterOneElementBeforeBrace() {
		// // @ContextConfiguration(locations={"one.xml"^}
		// // @ContextConfiguration(locations={"one.xml","foo.xml"^}

		String referenceString = "class Test8 ";
		String label = "testAfterOneElementBeforeBrace";
		int characterOffset = 4;
		String prefix = ", \"";
		String postfix = "\"";

		helper(label, referenceString, characterOffset, prefix, postfix);
	}

	// redundant with testAfterOneElementAndComma
	public void testAfterOneElementAndCommaBeforeBrace() {
		// @ContextConfiguration(locations={"one.xml",^}
		// @ContextConfiguration(locations={"one.xml","foo.xml"}

		String referenceString = "class Test10 ";
		String label = "testAfterOneElementAndCommaBeforeBrace";
		int characterOffset = 4;
		String prefix = "\"";
		String postfix = "\"";

		helper(label, referenceString, characterOffset, prefix, postfix);
	}

	// redundant with testAfterTwoElements
	public void testAfterOneElementAndOutsideQuotes() {
		// @ContextConfiguration(locations={"one.xml",""^
		// @ContextConfiguration(locations={"one.xml","", "foo.xml"^

		String referenceString = "class Test11 ";
		String label = "testAfterOneElementAndOutsideQuotes";
		int characterOffset = 3;
		String prefix = ", \"";
		String postfix = "\"";

		helper(label, referenceString, characterOffset, prefix, postfix);
	}

	// redundant with testAfterTwoElements
	public void testAfterOneElementAndOutsideQuotesBeforeBrace() {
		// @ContextConfiguration(locations={"one.xml",""^}
		// @ContextConfiguration(locations={"one.xml","","foo.xml"^}

		String referenceString = "class Test12 ";
		String label = "testAfterOneElementAndOutsideQuotesBeforeBrace";
		int characterOffset = 4;
		String prefix = ", \"";
		String postfix = "\"";

		helper(label, referenceString, characterOffset, prefix, postfix);
	}

	public void testAfterTwoElementsBeforeBrace() {
		// @ContextConfiguration(locations={"one.xml", "two.xml"^}
		// @ContextConfiguration(locations={"one.xml", "two.xml", "foo.xml"^}

		String referenceString = "class Test13b ";
		String label = "testAfterTwoElementsBeforeBrace";
		int characterOffset = 4;
		String prefix = ", \"";
		String postfix = "\"";

		helper(label, referenceString, characterOffset, prefix, postfix);
	}

	public void testAfterTwoElementsOutsideQuotes() {
		// @ContextConfiguration(locations={"one.xml", "two.xml",""^
		// @ContextConfiguration(locations={"one.xml", "two.xml","",
		// "foo.xml"^})

		String referenceString = "class Test14b ";
		String label = "testAfterTwoElementsOutsideQuotes";
		int characterOffset = 3;
		String prefix = ", \"";
		String postfix = "\"";

		helper(label, referenceString, characterOffset, prefix, postfix);
	}

	public void testAfterTwoElementsOutsideQuotesBeforeBrace() {
		// @ContextConfiguration(locations={"one.xml", "two.xml",""^})
		// @ContextConfiguration(locations={"one.xml", "two.xml","",
		// "foo.xml"^})

		int offset = 1632;

		String referenceString = "class Test15 ";
		String label = "testAfterTwoElementsOutsideQuotesBeforeBrace";
		int characterOffset = 4;
		String prefix = ", \"";
		String postfix = "\"";

		helper(label, referenceString, characterOffset, prefix, postfix);
	}

	// Don't really care what proposals are given; just don't want it to crash
	public void testAfterGarbage1() {
		// @ContextConfiguration(locations={asldfkjs; alsdkfjaslfj^}

		int characterOffset = 9;
		String label = "testAfterGarbage1";

		String referenceString = "Test18 ";

		int referencePosition = testFileText.indexOf(referenceString) - characterOffset;

		ContentAssistInvocationContext context = createTestContext(referencePosition);
		List<ICompletionProposal> proposals = computer.computeCompletionProposals(context, null);
		assertTrue(proposals.size() == 0);

	}

	public void testAfterGarbage2() {
		// @ContextConfiguration(locations={)^}
		// Don't care

		int characterOffset = 10;
		String label = "testAfterGarbage2";
		String referenceString = "TestA ";

		int referencePosition = testFileText.indexOf(referenceString) - characterOffset;

		ContentAssistInvocationContext context = createTestContext(referencePosition);
		List<ICompletionProposal> proposals = computer.computeCompletionProposals(context, null);
		assertTrue(proposals.size() == 0);

	}

	public void testAfterGarbage3() {
		// @ContextConfiguration(locations={alskdfjaslfdj^}

		int characterOffset = 10;
		String label = "testAfterGarbage3";
		String referenceString = "Test19 ";

		int referencePosition = testFileText.indexOf(referenceString) - characterOffset;
		ContentAssistInvocationContext context = createTestContext(referencePosition);
		List<ICompletionProposal> proposals = computer.computeCompletionProposals(context, null);
		// we don't care what the outcome is, we just don't want it to crash
	}

	public void testAfterGarbage4() {
		// @ContextConfiguration(locations={locations={

		int characterOffset = 9;
		String label = "testAfterGarbage4";

		String referenceString = "TestC ";

		int referencePosition = testFileText.indexOf(referenceString) - characterOffset;
		ContentAssistInvocationContext context = createTestContext(referencePosition);
		List<ICompletionProposal> proposals = computer.computeCompletionProposals(context, null);
		// we don't care what the outcome is, we just don't want it to crash

	}

	public void testWithIrrelevantParameters() {
		// @ContextConfiguration(x=47, locations={"foo.xml"^}, y=52
		// @ContextConfiguration(x=47, locations={"foo.xml", "bar.xml"^}, y=52

		String label = "testWithIrrelevantParameters";
		int characterOffset = 11;
		String prefix = ", \"";
		String postfix = "\"";
		String referenceString = "class Test30 ";

		helper(label, referenceString, characterOffset, prefix, postfix);
	}

	public void testWithComplexIrrelevantParameters() {
		// @ContextConfiguration(x=47, locations={"foo.xml"^}, y=52
		// @ContextConfiguration(x=47, locations={"foo.xml", "bar.xml"^}, y=52

		String label = "testWithIrrelevantParameters";
		int characterOffset = 11;
		String prefix = ", \"";
		String postfix = "\"";
		String referenceString = "class Test31 ";

		helper(label, referenceString, characterOffset, prefix, postfix);
	}

	// ^ shows where the invocation point is, i.e. where
	// the cursor would be in the file
	public void testEmptyLocations() {
		// @ContextConfiguration(locations=^)
		// @ContextConfiguration(locations={"foo.xml"^}

		int characterOffset = 3;
		String referenceString = "class Test1 ";
		String label = "testEmptyLocations";
		String prefix = "{\"";
		String postfix = "\"}";
		helper(label, referenceString, characterOffset, prefix, postfix);

	}

	// optional
	public void testEmptyString() {
		// @ContextConfiguration(locations="^"
		// @ContextConfiguration(locations={"foo.xml^"
		int offset = 472;

		String referenceString = "class Test2 ";
		int characterOffset = 4;
		String label = "testEmptyString";
		String prefix = "";
		String postfix = "";

		helper(label, referenceString, characterOffset, prefix, postfix);

	}

	// most likely case
	public void testAfterBrace() {
		// @ContextConfiguration(locations={^
		// @ContextConfiguration(locations={"foo.xml^"
		String prefix = "\"";
		String postfix = "\"";
		String label = "testAfterBrace";
		int characterOffset = 2;
		String referenceString = "class Test3c ";

		helper(label, referenceString, characterOffset, prefix, postfix);

	}

	// one brace
	public void testBraceInEmptyQuotes() {
		// @ContextConfiguration(locations={"^"
		// @ContextConfiguration(locations={"foo.xml^"
		String label = "testBraceInEmptyQuotes";
		int characterOffset = 4;
		String prefix = "";
		String postfix = "";

		String referenceString = "class Test5 ";
		helper(label, referenceString, characterOffset, prefix, postfix);
	}

	public void testBraceAfterEmptyQuotes() {
		// @ContextConfiguration(locations={""^
		// @ContextConfiguration(locations={"", "foo.xml"

		String label = "testBraceAfterEmptyQuotes";
		String prefix = ", \"";
		String postfix = "\"";
		int characterOffset = 3;
		String referenceString = "class Test5 ";

		helper(label, referenceString, characterOffset, prefix, postfix);

	}

	public void testBracesAfterEmptyQuotesAndSpace() {
		// @ContextConfiguration(locations={"" ^}
		// @ContextConfiguration(locations={"", "foo.xml"^}

		String label = "testBracesAfterEmptyQuotesAndSpace";
		int characterOffset = 4;
		String prefix = ", \"";
		String postfix = "\"";
		String referenceString = "class Test6 ";

		helper(label, referenceString, characterOffset, prefix, postfix);
	}

	public void testInsideOneElementBeforeBrace() {
		// @ContextConfiguration(locations={"one^.xml"}
		// NOP

		String label = "testInsideOneElementBeforeBrace";
		int characterOffset = 9;
		String referenceString = "class Test8";

		int referencePosition = testFileText.indexOf(referenceString) - characterOffset;

		ContentAssistInvocationContext context = createTestContext(referencePosition);
		List<ICompletionProposal> proposals = computer.computeCompletionProposals(context, null);

		assertTrue(proposals.size() == 0);
	}

	public void testAfterOneElementAndComma() {
		// @ContextConfiguration(locations={"one.xml",^
		// @ContextConfiguration(locations={"one.xml","foo.xml"^
		String label = "testAfterOneElementAndComma";
		int characterOffset = 3;
		String prefix = "\"";
		String postfix = "\"";
		String referenceString = "class Test9 ";

		helper(label, referenceString, characterOffset, prefix, postfix);
	}

	public void testAfterOneElementAndInsideQuotes() {
		// @ContextConfiguration(locations={"one.xml","^"
		// @ContextConfiguration(locations={"one.xml","foo.xml^"
		String label = "testAfterOneElementAndInsideQuotes";
		int characterOffset = 4;
		String referenceString = "class Test11 ";
		String prefix = "";
		String postfix = "";

		helper(label, referenceString, characterOffset, prefix, postfix);
	}

	public void testAfterOneElementAndInsideQuotesBeforeBrace() {
		// @ContextConfiguration(locations={"one.xml","^"}
		// @ContextConfiguration(locations={"one.xml","foo.xml}

		String label = "testAfterOneElementAndInsideQuotesBeforeBrace";
		int characterOffset = 5;
		String prefix = "";
		String postfix = "";
		String referenceString = "class Test12 ";

		helper(label, referenceString, characterOffset, prefix, postfix);
	}

	// This would be the same as testAfterOneElementAndComma
	public void testBetweenTwoElements() {
		// @ContextConfiguration(locations={"one.xml",^ "two.xml"
		// @ContextConfiguration(locations={"one.xml","foo.xml",^ "two.xml"})

		String label = "testBetweenTwoElements";
		int characterOffset = 13;
		String prefix = "\"";
		String postfix = "\"";
		String referenceString = "class Test13 ";

		helper(label, referenceString, characterOffset, prefix, postfix);

	}

	public void testInsideSecondElement() {
		// @ContextConfiguration(locations={"one.xml", "t^wo.xml")
		// NOP
		String label = "testInsideSecondElement";
		int characterOffset = 10;
		String referenceString = "class Test13 ";

		int referencePosition = testFileText.indexOf(referenceString) - characterOffset;

		ContentAssistInvocationContext context = createTestContext(referencePosition);
		List<ICompletionProposal> proposals = computer.computeCompletionProposals(context, null);
		assertTrue(proposals.size() == 0);

	}

	public void testAfterTwoElements() {
		// @ContextConfiguration(locations={"one.xml", "two.xml"^
		// @ContextConfiguration(locations={"one.xml", "two.xml", "foo.xml"^

		String label = "testAfterTwoElements";
		int characterOffset = 3;
		String referenceString = "class Test13 ";
		String prefix = ", \"";
		String postfix = "\"";

		helper(label, referenceString, characterOffset, prefix, postfix);

	}

	public void testAfterTwoElementsAfterCommaBeforeBrace() {
		// @ContextConfiguration(locations={"one.xml", "two.xml",^}
		// @ContextConfiguration(locations={"one.xml", "two.xml","foo.xml"^})

		String label = "testAfterTwoElementsAfterCommaBeforeBrace";
		int characterOffset = 4;
		String prefix = "\"";
		String postfix = "\"";
		String referenceString = "class Test13c ";

		helper(label, referenceString, characterOffset, prefix, postfix);

	}

	// Note that there is no space between the second and third element
	public void testAfterTwoElementsAfterCommaAndQuote() {
		// @ContextConfiguration(locations={"one.xml", "two.xml","^
		// @ContextConfiguration(locations={"one.xml", "two.xml","foo.xml^})

		String label = "testAfterTwoElementsAfterCommaAndQuote";
		int characterOffset = 3;
		String referenceString = "class Test14 ";
		String prefix = "";
		String postfix = "";

		helper(label, referenceString, characterOffset, prefix, postfix);

	}

	public void testAfterTwoElementsInsideQuotesBeforeBrace() {
		// @ContextConfiguration(locations={"one.xml", "two.xml","^"}
		// @ContextConfiguration(locations={"one.xml", "two.xml","foo.xml"^})

		String label = "testAfterTwoElementsInsideQuotesBeforeBrace";
		int characterOffset = 5;
		String referenceString = "class Test15 ";
		String prefix = "";
		String postfix = "";

		helper(label, referenceString, characterOffset, prefix, postfix);

	}

	// Note that there is no space between second and third,
	// unlike between first and second, so this test is different from
	// testBetweenTwoElements
	public void testBetweenSecondAndThirdElements() {
		// @ContextConfiguration(locations={"one.xml", "two.xml",^"three.xml"
		// @ContextConfiguration(locations={"one.xml",
		// "two.xml","foo.xml",^"three.xml"})
		String label = "testBetweenSecondAndThirdElements";
		String referenceString = "Test16 ";
		int characterOffset = 20;
		String prefix = "\"";
		String postfix = "\"";

		helper(label, referenceString, characterOffset, prefix, postfix);
	}

	public void testAfterThreeElements() {
		// @ContextConfiguration(locations={"one.xml", "two.xml","three.xml"^
		// @ContextConfiguration(locations={"one.xml",
		// "two.xml","three.xml", "foo.xml"^})
		int characterOffset = 9;
		String label = "testAfterThreeElements";
		String prefix = ", \"";
		String postfix = "\"";
		String referenceString = "Test16 ";

		helper(label, referenceString, characterOffset, prefix, postfix);

	}

	public void testTypingStartOfClasspath() {
		// @ContextConfiguration(locations = { "src^"
		// @ContextConfiguration(locations = { "classpath:src/foo.xml"
		String label = "testTypingStartOfClasspath";
		String startOfAnnotationLine = "\t@ContextConfiguration(value = { \"src";
		int characterOffset = newlineLength + label.length() + startOfAnnotationLine.length();
		String prefix = "";
		String postfix = "";

		int position = testFileText.indexOf(label) + characterOffset - 1;

		ContentAssistInvocationContext context = createTestContext(position);
		List<ICompletionProposal> proposals = computer.computeCompletionProposals(context, null);
		int countOfClasspathFiles = getListOfPossibleClasspathConfigFiles().size();
		String errorMessage = "Wrong number of proposals: expecting " + getListOfPossibleClasspathConfigFiles().size()
				+ " and got " + proposals.size();
		assertTrue(errorMessage, proposals.size() == getListOfPossibleClasspathConfigFiles().size());
	}

	// "value" is an alias for "locations"
	public void testValueCompletion() {
		// @ContextConfiguration( value = { "subdir^"})
		// @ContextConfiguration( value = { "subdir/content-assist-test.xml"^})
		String label = "testValueCompletion";

		int position = testFileText.indexOf(label) + newlineLength + label.length()
				+ "\t@ContextConfiguration(value = { \"subdir".length() - 1;

		ContentAssistInvocationContext context = createTestContext(position);
		List<ICompletionProposal> proposals = computer.computeCompletionProposals(context, null);
		assertTrue(proposals.size() == 1);

		String expectedString = "classpath:subdir/content-configuration-assist-test.xml";

		ICompletionProposal proposal = proposals.get(0);
		if (proposal instanceof JavaCompletionProposal) {
			String replacementString = ((JavaCompletionProposal) proposal).getReplacementString();
			if (!replacementString.equals(expectedString)) {
				System.err.println("ERROR: expected >" + expectedString + "< and got >" + replacementString);
			}
			assertTrue(expectedString.equals(replacementString));
		}
	}

	public void testNonClasspathTypeFullPathAmbiguous() {
		// @ContextConfiguration(locations = {"non-classpath^"
		// @ContextConfiguration(locations = {"file:non-classpath/foo.xml"
		String label = "testNonClasspathTypeFullPathAmbiguous";
		String annotationLineStart = "\t@ContextConfiguration(value = { \"non-classpath";
		String prefix = "";
		String postfix = "";

		int position = testFileText.indexOf(label) + newlineLength + label.length() + annotationLineStart.length() - 1;

		ContentAssistInvocationContext context = createTestContext(position);
		List<ICompletionProposal> proposals = computer.computeCompletionProposals(context, null);

		ArrayList<String> expectedProposals = createAffixedProposals(prefix, postfix,
				getListOfPossibleNonClasspathConfigFiles());

		assertTrue(proposals.size() == getListOfPossibleNonClasspathConfigFiles().size());

		assertValidcontextConfigurationProposals(proposals, expectedProposals, label);

	}

	@SuppressWarnings("restriction")
	public void testNonClasspathTypeFullPath() {
		// @ContextConfiguration( value = { "non-classpath/arb^"})
		// @ContextConfiguration( value = {
		// "file:non-classpath/arbitrary.xml"^})
		String label = "testNonClasspathTypeFullPath";
		String annotationLineStart = "\t@ContextConfiguration(value = { \"non-classpath/arb";

		int position = testFileText.indexOf(label) + newlineLength + label.length() + annotationLineStart.length() - 1;

		ContentAssistInvocationContext context = createTestContext(position);
		List<ICompletionProposal> proposals = computer.computeCompletionProposals(context, null);
		String errorMessage = "Expecting 1 proposal, but got " + proposals.size();
		assertTrue(errorMessage, proposals.size() == 1);

		String expectedString = "file:non-classpath/arbitrary.xml";

		ICompletionProposal proposal = proposals.get(0);
		if (proposal instanceof JavaCompletionProposal) {
			String replacementString = ((JavaCompletionProposal) proposal).getReplacementString();
			if (!replacementString.equals(expectedString)) {
				System.err.println("ERROR: expected >" + expectedString + "< and got >" + replacementString);
			}
			assertTrue(expectedString.equals(replacementString));
		}
	}

	@SuppressWarnings("restriction")
	public void testNonClasspathTypeBasename() {
		// @ContextConfiguration( value = { "arbitrary^"})
		// @ContextConfiguration( value = {
		// "file:non-classpath/arbitrary.xml"^})
		String label = "testNonClasspathTypeBasename";
		String annotationLineStart = "\t@ContextConfiguration(value = { \"arbitrary";

		int position = testFileText.indexOf(label) + newlineLength + label.length() + annotationLineStart.length() - 1;

		ContentAssistInvocationContext context = createTestContext(position);
		List<ICompletionProposal> proposals = computer.computeCompletionProposals(context, null);
		String errorMessage = "Expecting 1 proposal, but got " + proposals.size();
		assertTrue(errorMessage, proposals.size() == 1);

		String expectedString = "file:non-classpath/arbitrary.xml";

		ICompletionProposal proposal = proposals.get(0);
		if (proposal instanceof JavaCompletionProposal) {
			String replacementString = ((JavaCompletionProposal) proposal).getReplacementString();
			if (!replacementString.equals(expectedString)) {
				System.err.println("ERROR: expected >" + expectedString + "< and got >" + replacementString);
			}
			assertTrue(expectedString.equals(replacementString));
		}
	}

	@SuppressWarnings("restriction")
	public void testFileColon() {
		// @ContextConfiguration( value = { "file:^"})
		// @ContextConfiguration( value = {
		// "file:non-classpath/arbitrary.xml"^})
		String label = "testFileColon";
		String annotationLineStart = "\t@ContextConfiguration(value = { \"file:";

		int position = testFileText.indexOf(label) + newlineLength + label.length() + annotationLineStart.length() - 1;

		ContentAssistInvocationContext context = createTestContext(position);
		List<ICompletionProposal> proposals = computer.computeCompletionProposals(context, null);
		String errorMessage = "Expecting " + getListOfPossibleNonClasspathConfigFiles().size() + " proposals, but got "
				+ proposals.size();
		assertTrue(errorMessage, proposals.size() == getListOfPossibleNonClasspathConfigFiles().size());

		// check the format of the first proposal -- this is somewhat fragile
		String expectedString = "non-classpath/arbitrary.xml";

		ICompletionProposal proposal = proposals.get(0);
		if (proposal instanceof JavaCompletionProposal) {
			String replacementString = ((JavaCompletionProposal) proposal).getReplacementString();
			errorMessage = "ERROR: expected >" + expectedString + "< and got >" + replacementString;
			assertTrue(errorMessage, expectedString.equals(replacementString));
		}

	}

	public void testClasspathColon() {
		// @ContextConfiguration( value = { "classpath:^"})
		// @ContextConfiguration( value = {
		// "classpath:add-constructor-arg-proposal.xml^"})
		String label = "testClasspathColon";
		String annotationLineStart = "\t@ContextConfiguration(value = { \"classpath:";

		int position = testFileText.indexOf(label) + newlineLength + label.length() + annotationLineStart.length() - 1;

		ContentAssistInvocationContext context = createTestContext(position);
		List<ICompletionProposal> proposals = computer.computeCompletionProposals(context, null);
		String errorMessage = "Expecting " + getListOfPossibleClasspathConfigFiles().size() + " proposals, but got "
				+ proposals.size();
		assertTrue(errorMessage, proposals.size() == getListOfPossibleClasspathConfigFiles().size());

		// check the format of the first proposal -- this is somewhat fragile
		String expectedString = "add-constructor-arg-proposal.xml";

		ICompletionProposal proposal = proposals.get(0);
		if (proposal instanceof JavaCompletionProposal) {
			String replacementString = ((JavaCompletionProposal) proposal).getReplacementString();
			if (!replacementString.equals(expectedString)) {
				System.err.println("ERROR: expected >" + expectedString + "< and got >" + replacementString);
			}
			errorMessage = "ERROR: expected >" + expectedString + "< and got >" + replacementString;
			assertTrue(errorMessage, expectedString.equals(replacementString));
		}

	}

}
