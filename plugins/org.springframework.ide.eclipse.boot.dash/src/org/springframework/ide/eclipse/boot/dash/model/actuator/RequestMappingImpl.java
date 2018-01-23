/*******************************************************************************
 * Copyright (c) 2015, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model.actuator;

import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.beans.ui.live.model.TypeLookup;
import org.springframework.ide.eclipse.boot.dash.model.actuator.JLRMethodParser.JLRMethod;
import org.springframework.ide.eclipse.boot.util.Log;

/**
 * @author Kris De Volder
 */
public class RequestMappingImpl implements RequestMapping {

	protected final TypeLookup typeLookup;
	private JLRMethod methodData;

	private String path;
	private String handler;

	protected RequestMappingImpl(String path, String handler, TypeLookup typeLookup) {
		this.typeLookup = typeLookup;
		this.path = path;
		this.handler = handler;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public String toString() {
		return "RequestMapping("+path+")";
	}

	@Override
	public IType getType() {
		String fqName = getFullyQualifiedClassName();
		if (fqName!=null) {
			return typeLookup.findType(fqName);
		}
		return null;
	}

	@Override
	public String getFullyQualifiedClassName() {
		JLRMethod m = getMethodData();
		if (m!=null) {
			return m.getFQClassName();
		}
		return null;
	}

	/**
	 * Returns the raw string found in the requestmapping info. This is a 'toString' value
	 * of java.lang.reflect.Method object.
	 */
	public String getMethodString() {
		try {
			if (handler!=null) {
				return handler; //Note: Boot 2.0 handler isn't always a method, but kind of hard to
					// recognize. Since we don't do anything meaningfull if its not a method...
					// its fine to treat everything as a method, as long as we don't make stuff
					// 'explode' if we cannot find the corresponding method in classpath.
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return null;
	}

	public IMethod getMethod() {
		try {
			IType type = getType();
			if (type!=null) {
				String mName = getMethodName();
				if (mName!=null) {
					IMethod[] methods = type.getMethods();
					if (methods!=null && methods.length>0) {
						for (IMethod m : methods) {
							//TODO: handle method overloading
							if (mName.equals(m.getElementName())) {
								return m;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return null;
	}

	@Override
	public boolean isUserDefined() {
		try {
			IType type = getType();
			if (type!=null) {
				IPackageFragmentRoot pfr = (IPackageFragmentRoot)type.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
				if (pfr!=null) {
					return pfr.getKind()==IPackageFragmentRoot.K_SOURCE;
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return false;
	}

	@Override
	public String getMethodName() {
		JLRMethod m = getMethodData();
		if (m!=null) {
			return m.getMethodName();
		}
		return null;
	}

	protected JLRMethod getMethodData() {
		if (methodData==null) {
			methodData = JLRMethodParser.parse(getMethodString());
		}
		return methodData;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((handler == null) ? 0 : handler.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RequestMappingImpl other = (RequestMappingImpl) obj;
		if (handler == null) {
			if (other.handler != null)
				return false;
		} else if (!handler.equals(other.handler))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

	private static Stream<String> processOrPaths(String pathExp) {
		if (pathExp.contains("||")) {
			String[] paths = pathExp.split(Pattern.quote("||"));
			return Stream.of(paths).map(String::trim);
		} else {
			return Stream.of(pathExp);
		}
	}


	private static String extractPath(String key) {
		if (key.startsWith("{[")) {
			//An almost json string. Unfortunately not really json so we can't
			//use org.json or jackson Mapper to properly parse this.
			int start = 2; //right after first '['
			int end = key.indexOf(']');
			if (end>=2) {
				return key.substring(start, end);
			}
		}
		//Case 1, or some unanticipated stuff.
		//Assume the key is the path, which is right for Case 1
		// and  probably more useful than null for 'unanticipated stuff'.
		return key;
	}

	public static Collection<RequestMappingImpl> create(String predicate, String handler, TypeLookup typeLookup) {
		return processOrPaths(extractPath(predicate))
				.map(path -> new RequestMappingImpl(path, handler, typeLookup))
				.collect(Collectors.toList());
	}


}
