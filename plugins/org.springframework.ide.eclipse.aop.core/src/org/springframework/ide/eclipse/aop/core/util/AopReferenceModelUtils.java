/*******************************************************************************
 * Copyright (c) 2007, 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.core.util;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.BeansTypeHierachyState;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansImport;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.IImportedBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigFactory;
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigId;
import org.springframework.ide.eclipse.core.java.ITypeStructureCache;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.java.TypeStructureState;
import org.springframework.ide.eclipse.core.project.IProjectContributorState;

/**
 * Some helper methods.
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 */
public class AopReferenceModelUtils {

	private static final String JAVA_FILE_EXTENSION = ".java";

	public static String getJavaElementLinkNameForMarker(IMember je) {
		if (je == null) {
			return "";
		}
		// use element name instead, qualified with parent
		if (je instanceof IMethod) {
			return ((IMethod) je).getDeclaringType().getFullyQualifiedName() + '.' + readableName((IMethod) je);
		}
		return getJavaElementLinkName(je);
	}

	public static String getJavaElementLinkName(IJavaElement je) {
		if (je == null) {
			return "";
		}
		// use element name instead, qualified with parent
		if (je instanceof IMethod) {
			// return je.getParent().getElementName() + '.' +
			return readableName((IMethod) je);
		}
		else if (je instanceof IType) {
			return je.getElementName();
		}
		else if (je instanceof IField) {
			return je.getElementName() + " - " + ((IType) je.getParent()).getFullyQualifiedName();
		}
		else if (je.getParent() != null) {
			return je.getParent().getElementName() + '.' + je.getElementName();
		}
		return je.getElementName();
	}

	public static String getPackageLinkName(IJavaElement je) {
		if (je instanceof IMethod) {
			return ((IMethod) je).getDeclaringType().getPackageFragment().getElementName();
		}
		else if (je instanceof IType) {
			return ((IType) je).getPackageFragment().getElementName();
		}
		return je.getElementName();
	}

	public static String readableName(IMethod method) {

		StringBuffer buffer = new StringBuffer(method.getElementName());
		buffer.append('(');
		String[] parameterTypes = method.getParameterTypes();
		int length;
		if (parameterTypes != null && (length = parameterTypes.length) > 0) {
			for (int i = 0; i < length; i++) {
				buffer.append(Signature.toString(parameterTypes[i]));
				if (i < length - 1) {
					buffer.append(", ");
				}
			}
		}
		buffer.append(')');
		return buffer.toString();
	}

	public static String getElementDescription(IAopReference reference) {
		StringBuffer buf = new StringBuffer(": <");
		buf.append(reference.getDefinition().getAspectName());
		buf.append("> [");
		buf.append(reference.getDefinition().getResource().getProjectRelativePath().toString());
		buf.append("]");
		return buf.toString();
	}

	public static Set<IFile> getAffectedFilesFromBeansProject(IProject file) {
		Set<IFile> affectedFiles = new HashSet<IFile>();
		IBeansProject bp = BeansCorePlugin.getModel().getProject(file.getProject());
		if (bp != null && bp.getConfigs() != null && bp.getConfigs().size() > 0) {
			for (IBeansConfig config : bp.getConfigs()) {
				affectedFiles.add((IFile) config.getElementResource());
			}
		}
		return affectedFiles;
	}

	public static Set<IResource> getAffectedFiles(int kind, int deltaKind, IResource resource,
			IProjectContributorState context) {
		Set<IBeansConfig> configs = new HashSet<IBeansConfig>();

		if (kind != IncrementalProjectBuilder.FULL_BUILD && resource instanceof IFile
				&& resource.getName().endsWith(JAVA_FILE_EXTENSION)) {

			// Make sure that the aop model is only reprocessed if a java structural change happens
			TypeStructureState structureState = context.get(TypeStructureState.class);
			BeansTypeHierachyState hierachyState = context.get(BeansTypeHierachyState.class);

			if (structureState == null
					|| structureState.hasStructuralChanges(resource, ITypeStructureCache.FLAG_ANNOTATION
							| ITypeStructureCache.FLAG_ANNOTATION_VALUE)) {
				if (deltaKind == IResourceDelta.REMOVED) {
					IBeansProject beansProject = BeansCorePlugin.getModel().getProject(resource.getProject());
					if (beansProject != null) {
						for (IBeansConfig beansConfig : beansProject.getConfigs()) {
							configs.add(beansConfig);
						}
					}
				}
				else {
					for (IBeansConfig config : hierachyState.getConfigsByContainingTypes(resource)) {
						configs.add(config);
					}
				}
			}

		}
		else if (BeansCoreUtils.isBeansConfig(BeansConfigId.create(resource, resource.getProject()), true)) {
			IBeansConfig beansConfig = (IBeansConfig) BeansModelUtils.getResourceModelElement(resource);
			if (beansConfig instanceof IImportedBeansConfig) {
				beansConfig = BeansModelUtils.getParentOfClass(beansConfig, IBeansConfig.class);
			}
			configs.add(beansConfig);
			
			// Capture imports from other projects -> add importing config
			for (IBeansProject bp : BeansCorePlugin.getModel().getProjects()) {
				for (IBeansConfig bc : bp.getConfigs()) {
					for (IBeansImport bi : bc.getImports()) {
						for (IImportedBeansConfig ibc : bi.getImportedBeansConfigs()) {
							if (ibc.getElementResource().equals(resource)) {
								configs.add(bc);
							}
						}
					}
				}
			}
		}
		// If the .classpath file is updated redo for every beans config
		else if (JdtUtils.isClassPathFile(resource)) {
			IBeansProject beansProject = BeansCorePlugin.getModel().getProject(resource.getProject());
			if (beansProject != null) {
				for (IBeansConfig beansConfig : beansProject.getConfigs()) {
					configs.add(beansConfig);
				}
			}
		}

		Set<IResource> files = new LinkedHashSet<IResource>();
		for (IBeansConfig config : configs) {
			files.add(config.getElementResource());
		}

		return files;
	}

	public static Set<IResource> getAffectedFilesFromBeansConfig(Set<IResource> files) {
		Set<IResource> newResources = new LinkedHashSet<IResource>();
		for (IResource resource : files) {
			// add confis from config set
			IBeansProject project = BeansCorePlugin.getModel().getProject(resource.getProject());
			IBeansConfig beansConfig = project.getConfig(BeansConfigId.create((IFile) resource));
			Set<IBeansConfigSet> configSets = project.getConfigSets();
			for (IBeansConfigSet configSet : configSets) {
				if (configSet.getConfigs().contains(beansConfig)) {
					Set<IBeansConfig> bcs = configSet.getConfigs();
					for (IBeansConfig bc : bcs) {
						newResources.add(bc.getElementResource());
					}
				}
			}
		}
		newResources.addAll(files);
		return newResources;
	}

	public static IBean getBeanFromElementId(String elementId) {
		IBeansModel model = BeansCorePlugin.getModel();
		return (IBean) model.getElement(elementId);
	}

}
