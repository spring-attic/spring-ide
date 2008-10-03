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
package org.springframework.ide.eclipse.core.java;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.springframework.ide.eclipse.core.SpringCore;

/**
 * Object that caches instances of {@link TypeStructure}. Furthermore this implementation is able to
 * answer if a given {@link IResource} which represents a class file has structural changes.
 * <p>
 * For this implementation a change of class and method level annotation is considered a structural
 * change.
 * @author Christian Dupuis
 * @since 2.2.0
 */
@SuppressWarnings("restriction")
class TypeStructureCache {

	private static final char[][] EMPTY_CHAR_ARRAY = new char[0][];

	private static final String FILE_SCHEME = "file";

	/** {@link TypeStructure} instances keyed by full-qualified class names */
	private static Map<IProject, Map<String, TypeStructure>> typeStructuresByProject = new ConcurrentHashMap<IProject, Map<String, TypeStructure>>();

	/**
	 * Removes {@link TypeStructure}s for a given project.
	 */
	public static void clearStateForProject(IProject project) {
		typeStructuresByProject.remove(project);
	}

	/**
	 * Checks if {@link TypeStructure} instances exist for a given project.
	 */
	public static boolean hasRecoredTypeStructures(IProject project) {
		return typeStructuresByProject.containsKey(project);
	}

	/**
	 * Record {@link TypeStructure} instances of the given <code>resources</code>.
	 */
	public static void recordTypeStructures(IProject project, IResource... resources) {
		Map<String, TypeStructure> typeStructures = null;
		if (!typeStructuresByProject.containsKey(project)) {
			typeStructures = new ConcurrentHashMap<String, TypeStructure>();
			typeStructuresByProject.put(project, typeStructures);
		}
		else {
			typeStructures = typeStructuresByProject.get(project);
		}

		for (IResource resource : resources) {
			if (resource.getFileExtension().equals("class") && resource instanceof IFile) {
				InputStream input = null;
				try {
					input = ((IFile) resource).getContents();
					ClassFileReader reader = ClassFileReader.read(input, resource.getName());
					TypeStructure typeStructure = new TypeStructure(reader);
					typeStructures.put(new String(reader.getName()).replace('/', '.'),
							typeStructure);
				}
				catch (CoreException e) {
				}
				catch (ClassFormatException e) {
				}
				catch (IOException e) {
				}
				finally {
					if (input != null) {
						try {
							input.close();
						}
						catch (IOException e) {
						}
					}
				}
			}
		}
	}

	/**
	 * Check if a given {@link IResource} representing a class file has structural changes.
	 */
	public static boolean hasStructuralChanges(IResource resource) {
		if (!hasRecoredTypeStructures(resource.getProject())) {
			return true;
		}

		Map<String, TypeStructure> typeStructures = typeStructuresByProject.get(resource
				.getProject());

		if (resource != null && resource.getFileExtension() != null
				&& resource.getFileExtension().equals("java")) {
			IJavaElement element = JavaCore.create(resource);
			if (element instanceof ICompilationUnit && ((ICompilationUnit) element).isOpen()) {
				try {
					IType[] types = ((ICompilationUnit) element).getAllTypes();
					for (IType type : types) {
						String fqn = type.getFullyQualifiedName();
						TypeStructure typeStructure = typeStructures.get(fqn);
						if (typeStructure == null) {
							return true;
						}
						ClassFileReader reader = getClassFileReaderForClassName(type
								.getFullyQualifiedName(), resource.getProject());
						if (reader != null && hasStructuralChanges(reader, typeStructure)) {
							return true;
						}
					}
					return false;
				}
				catch (JavaModelException e) {
					SpringCore.log(e);
				}
				catch (MalformedURLException e) {
					SpringCore.log(e);
				}
			}
		}
		return true;
	}

	private static ClassFileReader getClassFileReaderForClassName(String className, IProject project)
			throws JavaModelException, MalformedURLException {
		IJavaProject jp = JavaCore.create(project);

		File outputDirectory = convertPathToFile(project, jp.getOutputLocation());
		File classFile = new File(outputDirectory, ClassUtils.getClassFileName(className));
		if (classFile.exists() && classFile.canRead()) {
			try {
				return ClassFileReader.read(classFile);
			}
			catch (ClassFormatException e) {
			}
			catch (IOException e) {
			}
		}

		IClasspathEntry[] classpath = jp.getResolvedClasspath(true);
		for (int i = 0; i < classpath.length; i++) {
			IClasspathEntry path = classpath[i];
			if (path.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				outputDirectory = convertPathToFile(project, path.getOutputLocation());
				classFile = new File(outputDirectory, ClassUtils.getClassFileName(className));
				if (classFile.exists() && classFile.canRead()) {
					try {
						return ClassFileReader.read(classFile);
					}
					catch (ClassFormatException e) {
					}
					catch (IOException e) {
					}
				}
			}
		}
		return null;
	}

	private static File convertPathToFile(IProject project, IPath path)
			throws MalformedURLException {
		if (path != null && project != null && path.removeFirstSegments(1) != null) {

			URI uri = project.findMember(path.removeFirstSegments(1)).getRawLocationURI();

			if (uri != null) {
				String scheme = uri.getScheme();
				if (FILE_SCHEME.equalsIgnoreCase(scheme)) {
					return new File(uri);
				}
				else {
					IPathVariableManager variableManager = ResourcesPlugin.getWorkspace()
							.getPathVariableManager();
					return new File(variableManager.resolveURI(uri));
				}
			}
		}
		return null;
	}

	private static boolean hasStructuralChanges(ClassFileReader reader, TypeStructure existingType) {
		if (existingType == null) {
			return true;
		}

		// modifiers
		if (!modifiersEqual(reader.getModifiers(), existingType.modifiers)) {
			return true;
		}

		// generic signature
		if (!CharOperation.equals(reader.getGenericSignature(), existingType.genericSignature)) {
			return true;
		}

		// superclass name
		if (!CharOperation.equals(reader.getSuperclassName(), existingType.superclassName)) {
			return true;
		}

		// class level annotations
		IBinaryAnnotation[] existingAnnotations = existingType.getAnnotations();
		IBinaryAnnotation[] newAnnotations = reader.getAnnotations();
		if (!annotationsEqual(existingAnnotations, newAnnotations)) {
			return true;
		}

		// interfaces
		char[][] existingIfs = existingType.interfaces;
		char[][] newIfsAsChars = reader.getInterfaceNames();
		if (newIfsAsChars == null) {
			newIfsAsChars = EMPTY_CHAR_ARRAY;
		} // damn I'm lazy...
		if (existingIfs == null) {
			existingIfs = EMPTY_CHAR_ARRAY;
		}
		if (existingIfs.length != newIfsAsChars.length)
			return true;
		new_interface_loop: for (int i = 0; i < newIfsAsChars.length; i++) {
			for (int j = 0; j < existingIfs.length; j++) {
				if (CharOperation.equals(existingIfs[j], newIfsAsChars[i])) {
					continue new_interface_loop;
				}
			}
			return true;
		}

		// methods
		IBinaryMethod[] newMethods = reader.getMethods();
		if (newMethods == null) {
			newMethods = TypeStructure.NoMethod;
		}

		IBinaryMethod[] existingMs = existingType.binMethods;
		if (newMethods.length != existingMs.length)
			return true;
		new_method_loop: for (int i = 0; i < newMethods.length; i++) {
			IBinaryMethod method = newMethods[i];
			char[] methodName = method.getSelector();
			for (int j = 0; j < existingMs.length; j++) {
				if (CharOperation.equals(existingMs[j].getSelector(), methodName)) {
					// candidate match
					if (!CharOperation.equals(method.getMethodDescriptor(), existingMs[j]
							.getMethodDescriptor())) {
						continue; // might be overloading
					}
					else {
						// matching sigs
						if (!modifiersEqual(method.getModifiers(), existingMs[j].getModifiers())) {
							return true;
						}
						if (!annotationsEqual(method.getAnnotations(), existingMs[j]
								.getAnnotations())) {
							return true;
						}
						continue new_method_loop;
					}
				}
			}
			return true; // (no match found)
		}

		return false;
	}

	private static boolean annotationsEqual(IBinaryAnnotation[] existingAnnotations,
			IBinaryAnnotation[] newAnnotations) {
		if (existingAnnotations == null) {
			existingAnnotations = TypeStructure.NoAnnotation;
		}
		if (newAnnotations == null) {
			newAnnotations = TypeStructure.NoAnnotation;
		}
		if (existingAnnotations.length != newAnnotations.length)
			return false;

		new_annotation_loop: for (int i = 0; i < newAnnotations.length; i++) {
			for (int j = 0; j < existingAnnotations.length; j++) {
				if (CharOperation.equals(newAnnotations[j].getTypeName(), existingAnnotations[i]
						.getTypeName())) {
					continue new_annotation_loop;
				}
			}
			return false;
		}
		return true;
	}

	private static boolean modifiersEqual(int eclipseModifiers, int resolvedTypeModifiers) {
		resolvedTypeModifiers = resolvedTypeModifiers & ExtraCompilerModifiers.AccJustFlag;
		eclipseModifiers = eclipseModifiers & ExtraCompilerModifiers.AccJustFlag;
		return (eclipseModifiers == resolvedTypeModifiers);
	}

}
