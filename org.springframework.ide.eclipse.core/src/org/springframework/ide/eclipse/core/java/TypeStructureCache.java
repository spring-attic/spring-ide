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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.ClassSignature;
import org.eclipse.jdt.internal.compiler.env.EnumConstantSignature;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryElementValuePair;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.impl.Constant;
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
public class TypeStructureCache implements ITypeStructureCache {

	private static final char[][] EMPTY_CHAR_ARRAY = new char[0][];

	private static final String FILE_SCHEME = "file";

	private IElementChangedListener changedListener = null;

	/** {@link TypeStructure} instances keyed by full-qualified class names */
	private Map<IProject, Map<String, TypeStructure>> typeStructuresByProject = new ConcurrentHashMap<IProject, Map<String, TypeStructure>>();

	protected final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

	protected final Lock r = rwl.readLock();

	protected final Lock w = rwl.writeLock();

	public void startup() {
		changedListener = new TypeRemovingJavaElementChangeListener();
		JavaCore.addElementChangedListener(changedListener);
	}

	public void shutdown() {
		JavaCore.removeElementChangedListener(changedListener);
		changedListener = null;
		typeStructuresByProject = null;
	}

	/**
	 * Removes {@link TypeStructure}s for a given project.
	 */
	public void clearStateForProject(IProject project) {
		try {
			w.lock();
			typeStructuresByProject.remove(project);
		}
		finally {
			w.unlock();
		}
	}

	/**
	 * Checks if {@link TypeStructure} instances exist for a given project.
	 */
	public boolean hasRecordedTypeStructures(IProject project) {
		try {
			r.lock();
			return typeStructuresByProject.containsKey(project);
		}
		finally {
			r.unlock();
		}
	}

	/**
	 * Record {@link TypeStructure} instances of the given <code>resources</code>.
	 */
	public void recordTypeStructures(IProject project, IResource... resources) {
		try {
			w.lock();
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
		finally {
			w.unlock();
		}
	}

	/**
	 * Check if a given {@link IResource} representing a class file has structural changes.
	 */
	public boolean hasStructuralChanges(IResource resource, int flags) {
		try {
			r.lock();
			if (!hasRecordedTypeStructures(resource.getProject())) {
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
							if (reader != null
									&& hasStructuralChanges(reader, typeStructure, flags)) {
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
		finally {
			r.unlock();
		}
	}

	/**
	 * Removes cached type structures by the given className.
	 */
	protected void removeRecordedTyeStructures(IProject project, String className) {
		try {
			w.lock();
			if (!hasRecordedTypeStructures(project)) {
				return;
			}

			String innerClassName = className + "$";
			List<String> typeStructuresToRemove = new ArrayList<String>();

			Map<String, TypeStructure> typeStructures = typeStructuresByProject.get(project);
			for (String recordedClassName : typeStructures.keySet()) {
				if (className.equals(recordedClassName)
						|| recordedClassName.startsWith(innerClassName)) {
					typeStructuresToRemove.add(recordedClassName);
				}
			}
			for (String recordedClassName : typeStructuresToRemove) {
				typeStructures.remove(recordedClassName);
			}
		}
		finally {
			w.unlock();
		}
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

	private boolean hasStructuralChanges(ClassFileReader reader, TypeStructure existingType,
			int flags) {
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
		if ((flags & FLAG_ANNOTATION) != 0) {
			IBinaryAnnotation[] existingAnnotations = existingType.getAnnotations();
			IBinaryAnnotation[] newAnnotations = reader.getAnnotations();
			if (!annotationsEqual(existingAnnotations, newAnnotations, flags)) {
				return true;
			}
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
						if ((flags & FLAG_ANNOTATION) != 0) {
							if (!annotationsEqual(method.getAnnotations(), existingMs[j]
									.getAnnotations(), flags)) {
								return true;
							}
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
			IBinaryAnnotation[] newAnnotations, int flags) {
		if (existingAnnotations == null) {
			existingAnnotations = TypeStructure.NoAnnotation;
		}
		if (newAnnotations == null) {
			newAnnotations = TypeStructure.NoAnnotation;
		}
		if (existingAnnotations.length != newAnnotations.length) {
			return false;
		}

		new_annotation_loop: for (int i = 0; i < newAnnotations.length; i++) {
			for (int j = 0; j < existingAnnotations.length; j++) {
				if (CharOperation.equals(newAnnotations[j].getTypeName(), existingAnnotations[i]
						.getTypeName())) {
					// compare annotation parameters
					if ((flags & FLAG_ANNOTATION_VALUE) != 0) {
						IBinaryElementValuePair[] newParameters = newAnnotations[j]
								.getElementValuePairs();
						IBinaryElementValuePair[] existingParameters = existingAnnotations[j]
								.getElementValuePairs();
						if (newParameters == null) {
							newParameters = TypeStructure.NoElement;
						}
						if (existingParameters == null) {
							existingParameters = TypeStructure.NoElement;
						}
						if (existingParameters.length != newParameters.length) {
							return false;
						}
						for (int k = 0; k < newParameters.length; k++) {
							for (int l = 0; l < existingParameters.length; l++) {
								char[] newName = newParameters[l].getName();
								char[] existingName = existingParameters[l].getName();
								Object newValue = newParameters[l].getValue();
								Object existingValue = existingParameters[l].getValue();

								if (!CharOperation.equals(newName, existingName)) {
									return false;
								}

								if (!parameterValuesEquals(flags, newValue, existingValue)) {
									return false;
								}
							}
						}
					}
					continue new_annotation_loop;
				}
			}
			return false;
		}
		return true;
	}

	private static boolean parameterValuesEquals(int flags, Object newValue, Object existingValue) {
		if (newValue.getClass().isArray() && existingValue.getClass().isArray()) {
			Object[] newValueArray = (Object[]) newValue;
			Object[] existingValueArray = (Object[]) existingValue;
			if (newValueArray.length != existingValueArray.length) {
				return false;
			}
			for (int i = 0; i < newValueArray.length; i++) {
				if (!parameterValuesEquals(flags, newValueArray[i], existingValueArray[i])) {
					return false;
				}
			}
		}
		else if (newValue instanceof ClassSignature) {
			if (existingValue instanceof ClassSignature) {
				if (!CharOperation.equals(((ClassSignature) newValue).getTypeName(),
						((ClassSignature) existingValue).getTypeName())) {
					return false;
				}
			}
			else {
				return false;
			}
		}
		else if (newValue instanceof Constant) {
			if (existingValue instanceof Constant) {
				if (!((Constant) newValue).hasSameValue((Constant) existingValue)) {
					return false;
				}
			}
			else {
				return false;
			}
		}
		else if (newValue instanceof EnumConstantSignature) {
			if (existingValue instanceof EnumConstantSignature) {
				if (!(CharOperation.equals(((EnumConstantSignature) newValue).getTypeName(),
						((EnumConstantSignature) existingValue).getTypeName()) && CharOperation
						.equals(((EnumConstantSignature) newValue).getEnumConstantName(),
								((EnumConstantSignature) existingValue).getEnumConstantName()))) {
					return false;
				}
			}
			else {
				return false;
			}
		}
		else if (newValue instanceof IBinaryAnnotation) {
			if (existingValue instanceof EnumConstantSignature) {
				if (!annotationsEqual(new IBinaryAnnotation[] { (IBinaryAnnotation) newValue },
						new IBinaryAnnotation[] { (IBinaryAnnotation) existingValue }, flags)) {
					return false;
				}
			}
			else {
				return false;
			}
		}
		return true;
	}

	private static boolean modifiersEqual(int eclipseModifiers, int resolvedTypeModifiers) {
		resolvedTypeModifiers = resolvedTypeModifiers & ExtraCompilerModifiers.AccJustFlag;
		eclipseModifiers = eclipseModifiers & ExtraCompilerModifiers.AccJustFlag;
		return (eclipseModifiers == resolvedTypeModifiers);
	}

	private class TypeRemovingJavaElementChangeListener implements IElementChangedListener {

		public void elementChanged(ElementChangedEvent event) {
			if (event.getType() == ElementChangedEvent.POST_CHANGE) {
				Object obj = event.getSource();
				if (obj instanceof IJavaElementDelta) {
					IJavaElementDelta delta = (IJavaElementDelta) obj;
					iterateChildren(new IJavaElementDelta[] { delta }, new IJavaProject[1]);
				}
			}
		}

		private void iterateChildren(IJavaElementDelta[] deltas, IJavaProject[] javaProject) {
			for (IJavaElementDelta delta : deltas) {
				if (delta.getElement() instanceof IJavaProject) {
					javaProject[0] = (IJavaProject) delta.getElement();
				}
				// process removed element
				IJavaElementDelta[] removedDeltas = delta.getRemovedChildren();
				for (IJavaElementDelta removedDelta : removedDeltas) {
					IJavaElement je = removedDelta.getElement();
					if (je instanceof ICompilationUnit) {
						StringBuilder sb = new StringBuilder();
						guessClassName(je, sb);
						if (javaProject[0] != null) {
							removeRecordedTyeStructures(javaProject[0].getProject(), sb.toString());
						}
					}
				}
				iterateChildren(delta.getAffectedChildren(), javaProject);
			}
		}

		private void guessClassName(IJavaElement cu, StringBuilder sb) {
			if (cu instanceof IPackageFragment) {
				sb.insert(0, cu.getElementName() + ".");
			}
			else if (cu != null) {
				if (cu instanceof ICompilationUnit) {
					String name = cu.getElementName()
							.substring(0, cu.getElementName().length() - 5);
					sb.insert(0, name);
				}
				else {
					sb.insert(0, cu.getElementName());
				}
				guessClassName(cu.getParent(), sb);
			}
		}
	}

}
