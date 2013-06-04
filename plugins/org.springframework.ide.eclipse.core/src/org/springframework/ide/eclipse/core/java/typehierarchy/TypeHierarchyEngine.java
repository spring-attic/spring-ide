/*******************************************************************************
 * Copyright (c) 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java.typehierarchy;

import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.compiler.CharOperation;

/**
 * @author Martin Lippert
 * @since 3.3.0
 */
public class TypeHierarchyEngine {
	
	public static final String ENABLE_PROPERTY = "org.springframework.ide.eclipse.core.java.enableTypeHierarchyEngine";
	
	private TypeHierarchyClassReaderFactory classReaderFactory;
	private Map<IProject, TypeHierarchyElementCache> cache;
	private Map<IProject, TypeHierarchyClassReader> readers;
	
	public TypeHierarchyEngine() {
		this.cache = new ConcurrentHashMap<IProject, TypeHierarchyElementCache>();
		this.readers = new ConcurrentHashMap<IProject, TypeHierarchyClassReader>();
	}

	public void setClassReaderFactory(TypeHierarchyClassReaderFactory classReaderFactory) {
		this.classReaderFactory = classReaderFactory;
	}
	
	public void clearCache(IProject project) {
		this.readers.remove(project);
		this.cache.remove(project);
	}
	
	public String getSupertype(IType type) {
		IJavaElement ancestor = type.getAncestor(IJavaElement.JAVA_PROJECT);
		if (ancestor != null && ancestor instanceof IJavaProject) {
			IProject project = ((IJavaProject)ancestor).getProject();
			return this.getSupertype(project, type.getFullyQualifiedName());
		}
		return null;
	}
	
	public String getSupertype(IProject project, String className) {
		char[] typeName = className.replace('.', '/').toCharArray();
		TypeHierarchyElement typeElement = getTypeElement(typeName, project);
		if (typeElement != null && typeElement.superclassName != null) {
			return new String(typeElement.superclassName).replace("/", ".");
		}
		return null;
	}
	
	public boolean doesExtend(IType type, String className) {
		IJavaElement ancestor = type.getAncestor(IJavaElement.JAVA_PROJECT);
		if (ancestor != null && ancestor instanceof IJavaProject) {
			IProject project = ((IJavaProject)ancestor).getProject();
		
			char[] typeName = type.getFullyQualifiedName().replace('.', '/').toCharArray();
			char[] superTypeName = className.replace('.',  '/').toCharArray();
		
			do {
				if (CharOperation.equals(typeName, superTypeName)) {
					return true;
				}
				else {
					TypeHierarchyElement typeElement = getTypeElement(typeName, project);
					if (typeElement != null) {
						typeName = typeElement.superclassName;
					}
					else {
						typeName = null;
					}
				}
			} while (typeName != null);
		}
		return false;
	}
	
	public boolean doesImplement(final IType type, final String interfaceName) {
		IJavaElement ancestor = type.getAncestor(IJavaElement.JAVA_PROJECT);
		if (ancestor != null && ancestor instanceof IJavaProject) {
			IProject project = ((IJavaProject)ancestor).getProject();
		
			char[] classTypeName = type.getFullyQualifiedName().replace('.', '/').toCharArray();
			char[] interfaceTypeName = interfaceName.replace('.',  '/').toCharArray();
			
			// cached items first
			boolean result = doesImplement(project, classTypeName, interfaceTypeName, true)
					|| doesImplement(project, classTypeName, interfaceTypeName, false);
			
			return result;
		}
		return false;
	}

	protected boolean doesImplement(IProject project, char[] classTypeName, char[] interfaceTypeName, boolean cachedItemsOnly) {
		do {
			TypeHierarchyElement classTypeElement = getTypeElement(classTypeName, project);
			if (classTypeElement != null) {
				char[][] implementedInterfaces = classTypeElement.interfaces;
				if (implementedInterfaces != null) {
					Stack<char[][]> interfaceStack = new Stack<char[][]>();
					interfaceStack.add(implementedInterfaces);
					
					while (!interfaceStack.isEmpty()) {
						char[][] interfacesToAnalyze = interfaceStack.pop();
						for (char[] interfaceToAnalyze : interfacesToAnalyze) {
							if (CharOperation.equals(interfaceToAnalyze, interfaceTypeName)) {
								return true;
							}
						}
						for (char[] interfaceToAnalyze : interfacesToAnalyze) {
							if (!cachedItemsOnly || this.hasCacheElementFor(interfaceToAnalyze, project)) {
								TypeHierarchyElement interfaceTypeElement = getTypeElement(interfaceToAnalyze, project);
								if (interfaceTypeElement != null) {
									char[][] superInterfaces = interfaceTypeElement.interfaces;
									if (superInterfaces != null) {
										interfaceStack.add(superInterfaces);
									}
								}
							}
						}
					}
				}
				classTypeName = classTypeElement.superclassName;
				if (cachedItemsOnly && classTypeName != null && !hasCacheElementFor(classTypeName, project)) {
					classTypeName = null;
				}
			}
			else {
				classTypeName = null;
			}
		} while (classTypeName != null);
		return false;
	}
	
	private boolean hasCacheElementFor(char[] fullyQualifiedClassName, IProject project) {
		return this.cache.containsKey(project)
				&& this.cache.get(project).get(fullyQualifiedClassName) != null;
	}
	
	private TypeHierarchyElement getTypeElement(char[] fullyQualifiedClassName, IProject project) {
		TypeHierarchyElementCache elementCache = this.cache.get(project);
		if (elementCache == null) {
			elementCache = new TypeHierarchyElementCache();
			this.cache.put(project, elementCache);
			
		}
		
		TypeHierarchyElement result = elementCache.get(fullyQualifiedClassName);
		
		if (result == null) {
			result = getClassReader(project).readTypeHierarchyInformation(fullyQualifiedClassName, project);
			if (result != null) {
				elementCache.put(fullyQualifiedClassName, result);
			}
		}
		
		return result;
	}

	private TypeHierarchyClassReader getClassReader(IProject project) {
		TypeHierarchyClassReader result = this.readers.get(project);
		if (result == null) {
			result = classReaderFactory.createClassReader(project);
			this.readers.put(project, result);
		}
		return result;
	}

}
