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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal;
import org.eclipse.jdt.ui.text.java.CompletionProposalComparator;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistContext;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistProposalRecorder;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.w3c.dom.Node;

/**
 * Utility class provides methods to trigger content assist for package and class content assist
 * requests.
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

	private static CompletionProposalComparator comparator = new CompletionProposalComparator();

	/**
	 * Add class and package content assist proposals that match the given <code>prefix</code>.
	 */
	public static void addClassValueProposals(IContentAssistContext context,
			IContentAssistProposalRecorder recorder) {
		addClassValueProposals(context, recorder, FLAG_PACKAGE | FLAG_CLASS);
	}

	/**
	 * Add interface content assist proposals that match the given <code>prefix</code>.
	 */
	public static void addInterfaceValueProposals(IContentAssistContext context,
			IContentAssistProposalRecorder recorder) {
		addClassValueProposals(context, recorder, FLAG_PACKAGE | FLAG_INTERFACE);
	}

	/**
	 * Add package content assist proposals that match the given <code>prefix</code>.
	 */
	public static void addPackageValueProposals(IContentAssistContext context,
			IContentAssistProposalRecorder recorder) {
		addClassValueProposals(context, recorder, FLAG_PACKAGE);
	}

	/**
	 * Add class and package content assist proposals that match the given <code>prefix</code>.
	 */
	public static void addClassValueProposals(IContentAssistContext context,
			IContentAssistProposalRecorder recorder, int flags) {
		String prefix = context.getMatchString();

		if (prefix == null || prefix.length() == 0) {
			return;
		}

		try {
			ICompilationUnit unit = createSourceCompilationUnit(context.getFile(), prefix);
			// Special handling of inner classes
			boolean innerClass = prefix.indexOf('$') > prefix.lastIndexOf('.');
			prefix = prefix.replace('$', '.');

			String sourceStart = CLASS_SOURCE_START + prefix;
			String packageName = null;
			int dot = prefix.lastIndexOf('.');
			if (dot > -1) {
				packageName = prefix.substring(0, dot);
				sourceStart = "package " + packageName + ";\n" + sourceStart;
			}
			String source = sourceStart + CLASS_SOURCE_END;
			setContents(unit, source);

			BeansJavaCompletionProposalCollector collector = new BeansJavaCompletionProposalCollector(
					unit, flags);
			unit.codeComplete(sourceStart.length(), collector, DefaultWorkingCopyOwner.PRIMARY);

			IJavaCompletionProposal[] props = collector.getJavaCompletionProposals();

			ICompletionProposal[] proposals = order(props);
			for (ICompletionProposal comProposal : proposals) {
				processJavaCompletionProposal(recorder, comProposal, packageName, innerClass);
			}
		}
		catch (Exception e) {
			// do nothing
		}
	}

	/**
	 * Add class assist proposals that match the given <code>prefix</code> and are part of the sub
	 * class hierarchy of the given <code>typeName</code>.
	 * @param request the {@link ContentAssistRequest} to add the proposals
	 * @param prefix the prefix
	 * @param typeName the super class of the request proposals
	 */
	public static void addTypeHierachyAttributeValueProposals(IContentAssistContext context,
			IContentAssistProposalRecorder recorder, String typeName) {
		final String prefix = context.getMatchString();
		if (prefix == null || prefix.length() == 0) {
			return;
		}

		IType type = JdtUtils.getJavaType(context.getFile().getProject(), typeName);
		try {
			if (type != null && context.getFile().getProject().hasNature(JavaCore.NATURE_ID)) {

				ITypeHierarchy hierachy = type.newTypeHierarchy(JavaCore.create(context.getFile()
						.getProject()), new NullProgressMonitor());
				IType[] types = hierachy.getAllSubtypes(type);
				Map<String, IType> sortMap = new HashMap<String, IType>();
				for (IType foundType : types) {
					if ((foundType.getFullyQualifiedName().startsWith(prefix) || foundType
							.getElementName().startsWith(prefix))
							&& !sortMap.containsKey(foundType.getFullyQualifiedName())
							&& !Flags.isAbstract(foundType.getFlags())
							&& Flags.isPublic(foundType.getFlags())) {
						recorder.recordProposal(JavaPluginImages
								.get(JavaPluginImages.IMG_OBJS_CLASS), 10, foundType
								.getElementName()
								+ " - " + foundType.getPackageFragment().getElementName(),
								foundType.getFullyQualifiedName(), foundType);
						sortMap.put(foundType.getFullyQualifiedName(), foundType);
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
	private static ICompilationUnit createSourceCompilationUnit(IFile file, String prefix)
			throws JavaModelException {
		IJavaProject project = JavaCore.create(file.getProject());
		IPackageFragment root = getPackageFragment(project, prefix);
		ICompilationUnit unit = root.getCompilationUnit("_xxx.java").getWorkingCopy(
				CompilationUnitHelper.getInstance().getWorkingCopyOwner(),
				CompilationUnitHelper.getInstance().getProblemRequestor(),
				BeansEditorUtils.getProgressMonitor());
		return unit;
	}

	private static IPackageFragment getPackageFragment(IJavaProject project, String prefix)
			throws JavaModelException {
		int dot = prefix.lastIndexOf('.');
		if (dot > -1) {
			String packageName = prefix.substring(0, dot);
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

	public static List<String> getPropertyTypes(Node node, IProject project) {
		List<String> requiredTypes = new ArrayList<String>();
		String className = BeansEditorUtils.getClassNameForBean(node.getParentNode());
		String propertyName = BeansEditorUtils.getAttribute(node, "name");
		if (className != null && propertyName != null && project != null) {
			IType type = JdtUtils.getJavaType(project, className);
			if (type != null) {
				try {
					IMethod method = Introspector.getWritableProperty(type, propertyName);
					if (method != null) {
						String signature = method.getParameterTypes()[0];
						String packageName = Signature.getSignatureQualifier(signature);
						String fullName = (packageName.trim().equals("") ? "" : packageName + ".")
								+ Signature.getSignatureSimpleName(signature);
						String[][] resolvedTypeNames = type.resolveType(fullName);

						if (resolvedTypeNames != null && resolvedTypeNames.length > 0
								&& resolvedTypeNames[0].length > 0) {
							IType propertyType = JdtUtils.getJavaType(project,
									resolvedTypeNames[0][0] + "." + resolvedTypeNames[0][1]);
							ITypeHierarchy hierachy = propertyType.newTypeHierarchy(JavaCore
									.create(project), new NullProgressMonitor());

							requiredTypes.add(propertyType.getFullyQualifiedName());
							IType[] subTypes = hierachy.getAllSubtypes(propertyType);
							for (IType subType : subTypes) {
								requiredTypes.add(subType.getFullyQualifiedName());
							}
						}
					}
				}
				catch (JavaModelException e) {
				}
			}
		}
		return requiredTypes;
	}

	/**
	 * Order the given proposals.
	 */
	@SuppressWarnings("unchecked")
	private static ICompletionProposal[] order(ICompletionProposal[] proposals) {
		Arrays.sort(proposals, comparator);
		return proposals;
	}

	private static void processJavaCompletionProposal(IContentAssistProposalRecorder recorder,
			ICompletionProposal comProposal, String packageName, boolean innerClass) {
		if (comProposal instanceof JavaCompletionProposal) {
			JavaCompletionProposal prop = (JavaCompletionProposal) comProposal;
			recorder.recordProposal(prop.getImage(), prop.getRelevance(), prop.getDisplayString(),
					prop.getReplacementString(), prop.getJavaElement());
		}
		else if (comProposal instanceof LazyJavaTypeCompletionProposal) {
			LazyJavaTypeCompletionProposal prop = (LazyJavaTypeCompletionProposal) comProposal;

			if (prop.getQualifiedTypeName().equals(packageName + "." + CLASS_NAME)
					|| prop.getQualifiedTypeName().equals(CLASS_NAME)) {
				return;
			}

			if (prop.getJavaElement() instanceof IType) {
				if (!((((IType) prop.getJavaElement()).getDeclaringType() == null && !innerClass) || (((IType) prop
						.getJavaElement()).getDeclaringType() != null && innerClass))) {
					return;
				}
			}

			String replacementString = prop.getQualifiedTypeName();
			if (innerClass) {
				replacementString = getClassName(((IType) prop.getJavaElement()));
			}

			recorder.recordProposal(prop.getImage(), prop.getRelevance(), prop.getDisplayString(),
					replacementString, prop.getJavaElement());
			// TODO CD remoe when refactored
			// BeansJavaCompletionProposal proposal = new BeansJavaCompletionProposal(
			// replacementString, request.getReplacementBeginPosition(), request
			// .getReplacementLength(), replacementString.length(), prop.getImage(),
			// prop.getDisplayString(), null, prop.getRelevance(), prop.getJavaElement());

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

	private static String getClassName(IType type) {
		if (type.getDeclaringType() == null) {
			return type.getFullyQualifiedName();
		}
		else {
			return getClassName(type.getDeclaringType()) + "$" + type.getElementName();
		}
	}
}
