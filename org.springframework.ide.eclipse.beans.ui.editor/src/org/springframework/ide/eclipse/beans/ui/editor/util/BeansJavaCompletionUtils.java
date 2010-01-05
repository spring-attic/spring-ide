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
package org.springframework.ide.eclipse.beans.ui.editor.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.corext.util.TypeFilter;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.text.java.CompletionProposalComparator;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistContext;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistProposalRecorder;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * Utility class provides methods to trigger content assist for package and class content assist requests.
 * @author Christian Dupuis
 * @since 1.3.6
 */
@SuppressWarnings("restriction")
public class BeansJavaCompletionUtils {

	public static final int FLAG_INTERFACE = 1 << 2;

	public static final int FLAG_CLASS = 1 << 3;

	public static final int FLAG_PACKAGE = 1 << 4;

	private static final String CLASS_NAME = "_xxx";

	private static final String CLASS_SOURCE_END = "\n" + "    }\n" + "}";

	private static final String CLASS_SOURCE_START = "public class " + CLASS_NAME + " {\n"
			+ "    public void main(String[] args) {\n" + "        ";

	private static CompletionProposalComparator COMPARATOR = new CompletionProposalComparator();

	private static ILabelProvider JAVA_LABEL_PROVIDER = new JavaElementLabelProvider(
			JavaElementLabelProvider.SHOW_DEFAULT | JavaElementLabelProvider.SHOW_POST_QUALIFIED);

	/**
	 * Add class and package content assist proposals that match the given <code>prefix</code>.
	 */
	public static void addClassValueProposals(IContentAssistContext context, IContentAssistProposalRecorder recorder) {
		addClassValueProposals(context, recorder, FLAG_PACKAGE | FLAG_CLASS);
	}

	/**
	 * Add interface content assist proposals that match the given <code>prefix</code>.
	 */
	public static void addInterfaceValueProposals(IContentAssistContext context, IContentAssistProposalRecorder recorder) {
		addClassValueProposals(context, recorder, FLAG_PACKAGE | FLAG_INTERFACE);
	}

	/**
	 * Add package content assist proposals that match the given <code>prefix</code>.
	 */
	public static void addPackageValueProposals(IContentAssistContext context, IContentAssistProposalRecorder recorder) {
		addClassValueProposals(context, recorder, FLAG_PACKAGE);
	}

	/**
	 * Add class and package content assist proposals that match the given <code>prefix</code>.
	 */
	public static void addClassValueProposals(IContentAssistContext context, IContentAssistProposalRecorder recorder,
			int flags) {
		String prefix = context.getMatchString();

		if (prefix == null || prefix.length() == 0) {
			return;
		}

		try {
			ICompilationUnit unit = createSourceCompilationUnit(context.getFile(), prefix);

			char enclosingChar = (prefix.lastIndexOf('$') > 0 ? '$' : '.');
			prefix = prefix.replace('$', '.');

			// Code completion below only provides public and protected inner classes; therefore
			// we manually add the private inner classes if possible
			if (prefix.lastIndexOf('.') > 0) {
				String rootClass = prefix.substring(0, prefix.lastIndexOf('.'));
				IType type = JdtUtils.getJavaType(context.getFile().getProject(), rootClass);
				if (type != null) {
					for (IType innerType : type.getTypes()) {
						if (Flags.isPrivate(innerType.getFlags())
								&& innerType.getFullyQualifiedName('.').startsWith(prefix)) {
							recorder.recordProposal(JAVA_LABEL_PROVIDER.getImage(innerType), 10, JAVA_LABEL_PROVIDER
									.getText(innerType), innerType.getFullyQualifiedName(enclosingChar), innerType);
						}
					}
				}
			}

			String sourceStart = CLASS_SOURCE_START + prefix;
			String packageName = null;
			int dot = prefix.lastIndexOf('.');
			if (dot > -1) {
				packageName = prefix.substring(0, dot);
				sourceStart = "package " + packageName + ";\n" + sourceStart;
			}
			String source = sourceStart + CLASS_SOURCE_END;
			setContents(unit, source);

			BeansJavaCompletionProposalCollector collector = new BeansJavaCompletionProposalCollector(unit, flags);
			unit.codeComplete(sourceStart.length(), collector, DefaultWorkingCopyOwner.PRIMARY);

			IJavaCompletionProposal[] props = collector.getJavaCompletionProposals();

			ICompletionProposal[] proposals = order(props);
			for (ICompletionProposal comProposal : proposals) {
				processJavaCompletionProposal(recorder, comProposal, packageName, enclosingChar);
			}
		}
		catch (Exception e) {
			// do nothing
		}
	}

	/**
	 * Add class assist proposals that match the given <code>prefix</code> and are part of the sub class hierarchy of
	 * the given <code>typeName</code>.
	 * @param request the {@link ContentAssistRequest} to add the proposals
	 * @param prefix the prefix
	 * @param typeName the super class of the request proposals
	 */
	public static void addTypeHierachyAttributeValueProposals(IContentAssistContext context,
			IContentAssistProposalRecorder recorder, String typeName, int flags) {
		final String prefix = context.getMatchString();
		if (prefix == null || prefix.length() == 0) {
			return;
		}

		IType type = JdtUtils.getJavaType(context.getFile().getProject(), typeName);
		try {
			if (type != null && context.getFile().getProject().hasNature(JavaCore.NATURE_ID)) {

				// Make sure that JDT's type filter preferences are applied
				if (!TypeFilter.isFiltered(type)) {
					ITypeHierarchy hierachy = type.newTypeHierarchy(JavaCore.create(context.getFile().getProject()),
							new NullProgressMonitor());
					IType[] types = hierachy.getAllSubtypes(type);
					Map<String, IType> sortMap = new HashMap<String, IType>();
					for (IType foundType : types) {
						if ((foundType.getFullyQualifiedName().startsWith(prefix) || foundType.getElementName()
								.startsWith(prefix))
								&& !sortMap.containsKey(foundType.getFullyQualifiedName())
								&& !Flags.isAbstract(foundType.getFlags())) {

							boolean accepted = false;
							if ((flags & BeansJavaCompletionUtils.FLAG_CLASS) != 0
									&& !Flags.isInterface(foundType.getFlags())) {
								accepted = true;
							}
							else if ((flags & BeansJavaCompletionUtils.FLAG_INTERFACE) != 0
									&& Flags.isInterface(foundType.getFlags())) {
								accepted = true;
							}
							if (accepted) {
								recorder.recordProposal(JavaPluginImages.get(JavaPluginImages.IMG_OBJS_CLASS), 10,
										foundType.getElementName() + " - "
												+ foundType.getPackageFragment().getElementName(), foundType
												.getFullyQualifiedName(), foundType);
								sortMap.put(foundType.getFullyQualifiedName(), foundType);
							}
						}
					}
				}
			}
		}
		catch (JavaModelException e) {
		}
		catch (CoreException e) {
		}
	}

	@SuppressWarnings("deprecation")
	private static ICompilationUnit createSourceCompilationUnit(IFile file, String prefix) throws JavaModelException {
		IProgressMonitor progressMonitor = BeansEditorUtils.getProgressMonitor();
		IJavaProject project = JavaCore.create(file.getProject());
		IPackageFragment root = getPackageFragment(project, prefix);
		ICompilationUnit unit = root.getCompilationUnit("_xxx.java").getWorkingCopy(
				CompilationUnitHelper.getInstance().getWorkingCopyOwner(),
				CompilationUnitHelper.getInstance().getProblemRequestor(), progressMonitor);
		progressMonitor.done();
		return unit;
	}

	private static IPackageFragment getPackageFragment(IJavaProject project, String prefix) throws JavaModelException {
		int dot = prefix.lastIndexOf('.');
		if (dot > -1) {
			String packageName = prefix.substring(0, dot);
			for (IPackageFragmentRoot root : project.getPackageFragmentRoots()) {
				IPackageFragment p = root.getPackageFragment(packageName);
				if (p != null && p.exists()) {
					return p;
				}
			}
			IPackageFragment[] packages = project.getPackageFragments();
			for (IPackageFragment p : packages) {
				if (p.getElementName().equals(packageName))
					return p;
			}
		}
		else {
			for (IPackageFragmentRoot p : project.getAllPackageFragmentRoots()) {
				if (p.getKind() == IPackageFragmentRoot.K_SOURCE) {
					return p.getPackageFragment("");
				}
			}
		}
		return project.getPackageFragments()[0];
	}

	/**
	 * Order the given proposals.
	 */
	@SuppressWarnings("unchecked")
	private static ICompletionProposal[] order(ICompletionProposal[] proposals) {
		Arrays.sort(proposals, COMPARATOR);
		return proposals;
	}

	private static void processJavaCompletionProposal(IContentAssistProposalRecorder recorder,
			ICompletionProposal comProposal, String packageName, char enclosingChar) {
		if (comProposal instanceof JavaCompletionProposal) {
			JavaCompletionProposal prop = (JavaCompletionProposal) comProposal;
			recorder.recordProposal(prop.getImage(), prop.getRelevance(), prop.getDisplayString(), prop
					.getReplacementString(), prop.getJavaElement());
		}
		else if (comProposal instanceof LazyJavaTypeCompletionProposal) {
			LazyJavaTypeCompletionProposal prop = (LazyJavaTypeCompletionProposal) comProposal;

			if (prop.getQualifiedTypeName().equals(packageName + "." + CLASS_NAME)
					|| prop.getQualifiedTypeName().equals(CLASS_NAME)) {
				return;
			}

			if (prop.getJavaElement() instanceof IType) {
				// Make sure that JDT's type filter preferences are applied
				if (TypeFilter.isFiltered((IType) prop.getJavaElement())) {
					return;
				}

				String replacementString = ((IType) prop.getJavaElement()).getFullyQualifiedName(enclosingChar);

				recorder.recordProposal(prop.getImage(), prop.getRelevance(), prop.getDisplayString(),
						replacementString, prop.getJavaElement());
			}
		}
	}

	/**
	 * Set contents of the compilation unit to the translated jsp text.
	 * @param the ICompilationUnit on which to set the buffer contents
	 */
	private static void setContents(ICompilationUnit cu, String source) {
		if (cu == null)
			return;

		synchronized (cu) {
			IBuffer buffer;
			try {

				buffer = cu.getBuffer();
			}
			catch (JavaModelException e) {
				e.printStackTrace();
				buffer = null;
			}

			if (buffer != null)
				buffer.setContents(source);
		}
	}

}
