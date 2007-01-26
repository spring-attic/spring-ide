/*
 * Copyright 2002-2006 the original author or authors.
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
import org.springframework.ide.eclipse.beans.core.internal.Introspector;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeansJavaCompletionProposal;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeansJavaCompletionProposalCollector;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.CompilationUnitHelper;
import org.w3c.dom.Node;

@SuppressWarnings("restriction")
public class BeansJavaCompletionUtils {

	private static CompletionProposalComparator comparator = new CompletionProposalComparator();

	private static final String CLASS_SOURCE_END = "\n" + "    }\n" + "}";

	private static final String CLASS_SOURCE_START = "public class _xxx {\n"
			+ "    public void main(String[] args) {\n" + "        ";

	public static void addClassValueProposals(ContentAssistRequest request, String prefix) {
		addClassValueProposals(request, prefix, false);
	}

	public static void addClassValueProposals(ContentAssistRequest request, String prefix,
			boolean interfaceRequired) {

		if (prefix == null || prefix.length() == 0) {
			return;
		}

		try {
			IFile file = (IFile) BeansEditorUtils.getResource(request);
			IJavaProject project = JavaCore.create(file.getProject());
			IPackageFragment root = project.getPackageFragments()[0];
			ICompilationUnit unit = root.getCompilationUnit("_xxx.java").getWorkingCopy(
					CompilationUnitHelper.getInstance().getWorkingCopyOwner(),
					CompilationUnitHelper.getInstance().getProblemRequestor(),
					BeansEditorUtils.getProgressMonitor());
			String source = CLASS_SOURCE_START + prefix + CLASS_SOURCE_END;
			setContents(unit, source);

			BeansJavaCompletionProposalCollector collector = new BeansJavaCompletionProposalCollector(
					unit, interfaceRequired);
			unit.codeComplete(CLASS_SOURCE_START.length() + prefix.length(), collector,
					DefaultWorkingCopyOwner.PRIMARY);

			IJavaCompletionProposal[] props = collector.getJavaCompletionProposals();

			ICompletionProposal[] proposals = order(props);

			for (int i = 0; i < proposals.length; i++) {
				ICompletionProposal comProposal = proposals[i];
				processJavaCompletionProposal(request, comProposal);
			}
		} catch (Exception e) {
			// do nothing
		}
	}

	protected static void processJavaCompletionProposal(ContentAssistRequest request,
			ICompletionProposal comProposal) {
		if (comProposal instanceof JavaCompletionProposal) {
			JavaCompletionProposal prop = (JavaCompletionProposal) comProposal;
			BeansJavaCompletionProposal proposal = new BeansJavaCompletionProposal(prop
					.getReplacementString(), request.getReplacementBeginPosition(), request
					.getReplacementLength(), prop.getReplacementString().length(), prop.getImage(),
					prop.getDisplayString(), null, prop.getAdditionalProposalInfo(), prop
							.getRelevance());

			request.addProposal(proposal);
		} else if (comProposal instanceof LazyJavaTypeCompletionProposal) {
			LazyJavaTypeCompletionProposal prop = (LazyJavaTypeCompletionProposal) comProposal;
			BeansJavaCompletionProposal proposal = new BeansJavaCompletionProposal(prop
					.getQualifiedTypeName(), request.getReplacementBeginPosition(), request
					.getReplacementLength(), prop.getQualifiedTypeName().length(), prop.getImage(),
					prop.getDisplayString(), null, prop.getAdditionalProposalInfo(), prop
							.getRelevance());

			request.addProposal(proposal);
		}
	}

	/**
	 * Order the given proposals.
	 */
	@SuppressWarnings("unchecked")
	private static ICompletionProposal[] order(ICompletionProposal[] proposals) {
		Arrays.sort(proposals, comparator);
		return proposals;
	}

	/**
	 * Set contents of the compilation unit to the translated jsp text.
	 * 
	 * @param the
	 *            ICompilationUnit on which to set the buffer contents
	 */
	private static void setContents(ICompilationUnit cu, String source) {
		if (cu == null)
			return;

		synchronized (cu) {
			IBuffer buffer;
			try {

				buffer = cu.getBuffer();
			} catch (JavaModelException e) {
				e.printStackTrace();
				buffer = null;
			}

			if (buffer != null)
				buffer.setContents(source);
		}
	}

	public static void addTypeHierachyAttributeValueProposals(ContentAssistRequest request,
			final String prefix, String typeName) {

		if (prefix == null || prefix.length() == 0) {
			return;
		}

		IType type = BeansModelUtils.getJavaType(
				BeansEditorUtils.getResource(request).getProject(), typeName);
		try {
			if (BeansEditorUtils.getResource(request).getProject().hasNature(JavaCore.NATURE_ID)) {

				ITypeHierarchy hierachy = type.newTypeHierarchy(JavaCore.create(BeansEditorUtils
						.getResource(request).getProject()), new NullProgressMonitor());
				IType[] types = hierachy.getAllSubtypes(type);
				Map<String, IType> sortMap = new HashMap<String, IType>();
				for (IType foundType : types) {
					if (!sortMap.containsKey(foundType.getFullyQualifiedName())
							&& !Flags.isAbstract(foundType.getFlags())
							&& Flags.isPublic(foundType.getFlags())) {
						BeansJavaCompletionProposal proposal = new BeansJavaCompletionProposal(
								foundType.getFullyQualifiedName(), request
										.getReplacementBeginPosition(), request
										.getReplacementLength(), foundType.getFullyQualifiedName()
										.length(), JavaPluginImages
										.get(JavaPluginImages.IMG_OBJS_CLASS), foundType
										.getElementName()
										+ " - " + foundType.getPackageFragment().getElementName(),
								null, null, 10);
						request.addProposal(proposal);
						sortMap.put(foundType.getFullyQualifiedName(), foundType);
					}
				}
			}
		} catch (JavaModelException e) {
		} catch (CoreException e) {
		}
	}

	public static List<String> getPropertyTypes(Node node, IProject project) {
		List<String> requiredTypes = new ArrayList<String>();
		String className = BeansEditorUtils.getClassNameForBean(node.getParentNode());
		String propertyName = BeansEditorUtils.getAttribute(node, "name");
		if (className != null && propertyName != null && project != null) {
			IType type = BeansModelUtils.getJavaType(project, className);
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
							IType propertyType = BeansModelUtils.getJavaType(project,
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
				} catch (JavaModelException e) {
				}
			}
		}
		return requiredTypes;
	}

}
