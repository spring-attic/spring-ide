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

import java.util.ArrayDeque;
import java.util.Map;
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
	private TypeHierarchyElementCacheFactory elementCacheFactory;

	private Map<IProject, TypeHierarchyElementCache> cache;
	private Map<IProject, TypeHierarchyClassReader> readers;
	
	public TypeHierarchyEngine() {
		this.cache = new ConcurrentHashMap<IProject, TypeHierarchyElementCache>();
		this.readers = new ConcurrentHashMap<IProject, TypeHierarchyClassReader>();
	}

	public void setClassReaderFactory(TypeHierarchyClassReaderFactory classReaderFactory) {
		this.classReaderFactory = classReaderFactory;
	}
	
	public void setTypeHierarchyElementCacheFactory(TypeHierarchyElementCacheFactory elementCacheFactory) {
		this.elementCacheFactory = elementCacheFactory;
	}
	
	public void cleanup(IProject project) {
		TypeHierarchyClassReader reader = this.readers.get(project);
		if (reader != null) {
			reader.cleanup();
		}
	}
	
	public void cleanup() {
		for (IProject project : this.readers.keySet()) {
			clearCache(project);
		}
	}

	public void clearCache(IProject project) {
		cleanup(project);
		this.readers.remove(project);
		this.cache.remove(project);
	}
	
	public String getSupertype(IType type, boolean cleanup) {
		IJavaElement ancestor = type.getAncestor(IJavaElement.JAVA_PROJECT);
		if (ancestor != null && ancestor instanceof IJavaProject) {
			IProject project = ((IJavaProject)ancestor).getProject();
			return this.getSupertype(project, type.getFullyQualifiedName(), cleanup);
		}
		return null;
	}
	
	public String getSupertype(IProject project, String className, boolean cleanup) {
		char[] typeName = className.replace('.', '/').toCharArray();
		try {
			TypeHierarchyElementCache elementCache = getTypeHierarchyElementCache(project);
			TypeHierarchyElement typeElement = getTypeElement(typeName, project, elementCache);
			if (typeElement != null && typeElement.superclassName != null) {
				return new String(typeElement.superclassName).replace("/", ".");
			}
		}
		finally {
			if (cleanup) cleanup(project);
		}
		return null;
	}
	
	public String[] getInterfaces(IProject project, String className, boolean cleanup) {
		char[] typeName = className.replace('.', '/').toCharArray();
		try {
			TypeHierarchyElementCache elementCache = getTypeHierarchyElementCache(project);
			TypeHierarchyElement typeElement = getTypeElement(typeName, project, elementCache);
			if (typeElement != null && typeElement.interfaces != null) {
				String[] result = new String[typeElement.interfaces.length];
				for (int i = 0; i < result.length; i++) {
					result[i] = new String(typeElement.interfaces[i]).replace("/", ".");;
				}
				return result;
			}
		}
		finally {
			if (cleanup) cleanup(project);
		}
		return null;
	}

	public boolean doesExtend(IType type, String className, boolean cleanup) {
		IJavaElement ancestor = type.getAncestor(IJavaElement.JAVA_PROJECT);
		if (ancestor != null && ancestor instanceof IJavaProject) {
			IProject project = ((IJavaProject)ancestor).getProject();
			return doesExtend(type.getFullyQualifiedName(), className, project, cleanup);
		}
		return false;
	}
	
	public boolean doesExtend(String type, String className, IProject project, boolean cleanup) {
		char[] typeName = type.replace('.', '/').toCharArray();
		char[] superTypeName = className.replace('.',  '/').toCharArray();
	
		TypeHierarchyElementCache elementCache = getTypeHierarchyElementCache(project);
		
		try {
			TypeHierarchyElement typeElement = null;
			TypeHierarchyElement previousTypeElement = null;

			do {
				if (CharOperation.equals(typeName, superTypeName)) {
					return true;
				}
				else {
					if (typeElement == null) {
						typeElement = getTypeElement(typeName, project, elementCache);
						if (previousTypeElement != null) {
							previousTypeElement.superclassElement = typeElement;
						}
					}
					previousTypeElement = typeElement;

					if (typeElement != null) {
						typeName = typeElement.superclassName;
						typeElement = typeElement.superclassElement;
					}
					else {
						typeName = null;
					}
				}
			} while (typeName != null);
		}
		finally {
			if (cleanup) cleanup(project);
		}
		return false;
	}
	
	public boolean doesImplement(final IType type, final String interfaceName, boolean cleanup) {
		IJavaElement ancestor = type.getAncestor(IJavaElement.JAVA_PROJECT);
		if (ancestor != null && ancestor instanceof IJavaProject) {
			IProject project = ((IJavaProject)ancestor).getProject();
			return doesImplement(type.getFullyQualifiedName(), interfaceName, project, cleanup);
		}
		return false;
	}
	
	public boolean doesImplement(final String type, final String interfaceName, IProject project, boolean cleanup) {
		char[] classTypeName = type.replace('.', '/').toCharArray();
		char[] interfaceTypeName = interfaceName.replace('.',  '/').toCharArray();

		try {
			TypeHierarchyElementCache elementCache = getTypeHierarchyElementCache(project);
			
			// cached items first
			boolean result = doesImplement(project, classTypeName, interfaceTypeName, true, elementCache)
					|| doesImplement(project, classTypeName, interfaceTypeName, false, elementCache);
			return result;
		}
		finally {
			if (cleanup) cleanup(project);
		}
	}

	protected boolean doesImplement(final IProject project, char[] classTypeName, final char[] interfaceTypeName,
			final boolean cachedItemsOnly, TypeHierarchyElementCache elementCache) {
		
		TypeHierarchyElement classTypeElement = getTypeElement(classTypeName, project, elementCache);
		do {
			if (classTypeElement != null) {
				if (classTypeElement.interfaces != null) {
					ArrayDeque<TypeHierarchyElement> elementStack = new ArrayDeque<TypeHierarchyElement>();
					elementStack.add(classTypeElement);

					while (!elementStack.isEmpty()) {
						TypeHierarchyElement element = elementStack.pop();
						for (char[] interfaceToAnalyze : element.interfaces) {
							if (CharOperation.equals(interfaceToAnalyze, interfaceTypeName)) {
								return true;
							}
						}

						for (int i = 0; i < element.interfaces.length; i++) {
							char[] interfaceToAnalyze = element.interfaces[i];
							TypeHierarchyElement interfaceToAnalyzeElement = element.interfacesElements[i];

							if (!cachedItemsOnly || interfaceToAnalyzeElement != null || elementCache.get(interfaceToAnalyze) != null) {
								if (interfaceToAnalyzeElement == null) {
									interfaceToAnalyzeElement = getTypeElement(interfaceToAnalyze, project, elementCache);
									element.interfacesElements[i] = interfaceToAnalyzeElement;
								}
								if (interfaceToAnalyzeElement != null && interfaceToAnalyzeElement.interfaces != null) {
									elementStack.add(interfaceToAnalyzeElement);
								}
							}
						}
					}
				}
				
				classTypeName = classTypeElement.superclassName;
				
				TypeHierarchyElement superClassTypeElement = classTypeElement.superclassElement;
				if (superClassTypeElement == null && classTypeName != null && (!cachedItemsOnly || elementCache.get(classTypeName) != null)) {
					superClassTypeElement = getTypeElement(classTypeName, project, elementCache);
					classTypeElement.superclassElement = superClassTypeElement;
				}
				
				classTypeElement = superClassTypeElement;
				
				if (cachedItemsOnly && classTypeName != null && classTypeElement == null && elementCache.get(classTypeName) == null) {
					classTypeName = null;
				}
			}
			else {
				classTypeName = null;
			}
		} while (classTypeName != null);
		return false;
	}
	
	private TypeHierarchyElement getTypeElement(char[] fullyQualifiedClassName, IProject project, TypeHierarchyElementCache elementCache) {
		TypeHierarchyElement result = elementCache.get(fullyQualifiedClassName);
		if (result == null) {
			result = getClassReader(project).readTypeHierarchyInformation(fullyQualifiedClassName, project);
			if (result != null) {
				elementCache.put(fullyQualifiedClassName, result);
			}
		}
		return result;
	}

	protected TypeHierarchyElementCache getTypeHierarchyElementCache(IProject project) {
		TypeHierarchyElementCache elementCache = this.cache.get(project);
		if (elementCache == null) {
			elementCache = this.elementCacheFactory.createTypeHierarchyElementCache();
			this.cache.put(project, elementCache);
		}
		return elementCache;
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
