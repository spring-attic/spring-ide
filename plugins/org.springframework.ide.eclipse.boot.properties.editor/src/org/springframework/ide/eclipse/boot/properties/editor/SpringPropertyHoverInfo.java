/*******************************************************************************
 * Copyright (c) 2014 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.ide.eclipse.boot.properties.editor.PropertyInfo.PropertySource;
import org.springframework.ide.eclipse.boot.properties.editor.util.JavaTypeLinks;
import org.springframework.ide.eclipse.editor.support.hover.HoverInfo;
import org.springframework.ide.eclipse.editor.support.util.HtmlBuffer;

import static org.springframework.ide.eclipse.boot.util.StringUtil.*;

/**
 * Information object that is displayed in SpringPropertiesTextHover's information
 * control.
 * <p>
 * Essentially this is a wrapper around {@link ConfigurationMetadataProperty}
 *
 * @author Kris De Volder
 */
@SuppressWarnings("restriction")
public class SpringPropertyHoverInfo extends HoverInfo {

	private static final String[] NO_ARGS = new String[0];

	/**
	 * Java project which is used to find declaration for 'navigate to declaration' action
	 */
	private IJavaProject javaProject;

	/**
	 * Data object to display in 'hover text'
	 */
	private PropertyInfo data;

	public SpringPropertyHoverInfo(IJavaProject project, PropertyInfo data) {
		this.javaProject = project;
		this.data = data;
	}

	@Override
	protected String renderAsHtml() {
		JavaTypeLinks jtLinks = new JavaTypeLinks(this);
		HtmlBuffer html = new HtmlBuffer();

		html.raw("<b>");
			html.text(data.getId());
		html.raw("</b>");
		html.raw("<br>");

		String type = data.getType();
		if (type==null) {
			type = Object.class.getName();
		}
		jtLinks.javaTypeLink(html, javaProject, type);

		String deflt = formatDefaultValue(data.getDefaultValue());
		if (deflt!=null) {
			html.raw("<br><br>");
			html.text("Default: ");
			html.raw("<i>");
			html.text(deflt);
			html.raw("</i>");
		}

		String description = data.getDescription();
		if (description!=null) {
			html.raw("<br><br>");
			html.text(description);
		}

		return html.toString();
	}

	public static String formatDefaultValue(Object defaultValue) {
		if (defaultValue!=null) {
			if (defaultValue instanceof String) {
				return (String) defaultValue;
			} else if (defaultValue instanceof Number) {
				return ((Number)defaultValue).toString();
			} else if (defaultValue instanceof Boolean) {
				return Boolean.toString((Boolean) defaultValue);
			} else if (defaultValue instanceof Object[]) {
				return arrayToCommaDelimitedString((Object[]) defaultValue);
			} else if (defaultValue instanceof Collection<?>) {
				return collectionToCommaDelimitedString((Collection<?>) defaultValue);
			} else {
				//no idea what it is but try 'toString' and hope for the best
				return defaultValue.toString();
			}
		}
		return null;
	}

	public PropertyInfo getElement() {
		return data;
	}

	public boolean canOpenDeclaration() {
		return getJavaElements()!=null;
	}

	/**
	 * Like 'getSources' but converts raw info into IJavaElements. Raw data which fails to be converted
	 * is silenetly ignored.
	 */
	public List<IJavaElement> getJavaElements() {
		try {
			if (javaProject!=null) {
				SpringPropertiesCompletionEngine.debug("javaProject = "+javaProject.getElementName());
				List<PropertySource> sources = getSources();
				SpringPropertiesCompletionEngine.debug("propertySources = "+sources);
				if (!sources.isEmpty()) {
					ArrayList<IJavaElement> elements = new ArrayList<IJavaElement>();
					for (PropertySource source : sources) {
						String typeName = source.getSourceType();
						if (typeName!=null) {
							IType type = javaProject.findType(typeName);
							IMethod method = null;
							if (type!=null) {
								String methodSig = source.getSourceMethod();
								if (methodSig!=null) {
									method = getMethod(type, methodSig);
								} else {
									method = getSetter(type, getElement());
								}
							}
							if (method!=null) {
								elements.add(method);
							} else if (type!=null) {
								elements.add(type);
							}
						}
					}
					return elements;
				}
			} else {
				SpringPropertiesCompletionEngine.debug("javaProject = null");
			}
		} catch (Exception e) {
			SpringPropertiesEditorPlugin.log(e);
		}
		return Collections.emptyList();
	}

	/**
	 * Attempt to find corresponding setter method for a given property.
	 * @return setter method, or null if not found.
	 */
	private IMethod getSetter(IType type, PropertyInfo propertyInfo) {
		try {
			String propName = propertyInfo.getName();
			String setterName = "set"
				+Character.toUpperCase(propName.charAt(0))
				+toCamelCase(propName.substring(1));
			String sloppySetterName = setterName.toLowerCase();

			IMethod sloppyMatch = null;
			for (IMethod m : type.getMethods()) {
				String mname = m.getElementName();
				if (setterName.equals(mname)) {
					//found 'exact' name match... done
					return m;
				} else if (mname.toLowerCase().equals(sloppySetterName)) {
					sloppyMatch = m;
				}
			}
			return sloppyMatch;
		} catch (Exception e) {
			SpringPropertiesEditorPlugin.log(e);
			return null;
		}
	}

	/**
	 * Convert hyphened name to camel case name. It is
	 * safe to call this on an already camel-cased name.
	 */
	private String toCamelCase(String name) {
		if (name.isEmpty()) {
			return name;
		} else {
			StringBuilder camel = new StringBuilder();
			char[] chars = name.toCharArray();
			for (int i = 0; i < chars.length; i++) {
				char c = chars[i];
				if (c=='-') {
					i++;
					if (i<chars.length) {
						camel.append(Character.toUpperCase(chars[i]));
					}
				} else {
					camel.append(chars[i]);
				}
			}
			return camel.toString();
		}
	}

	/**
	 * Get 'raw' info about sources that define this property.
	 */
	public List<PropertySource> getSources() {
		return data.getSources();
	}

	private IMethod getMethod(IType type, String methodSig) throws JavaModelException {
		int nameEnd = methodSig.indexOf('(');
		String name;
		if (nameEnd>=0) {
			name = methodSig.substring(0, nameEnd);
		} else {
			name = methodSig;
		}
		//TODO: This code assumes 0 arguments, which is the case currently for all
		//  'real' data in spring jars.
		IMethod m = type.getMethod(name, NO_ARGS);
		if (m!=null) {
			return m;
		}
		//try  find a method  with the same name.
		for (IMethod meth : type.getMethods()) {
			if (name.equals(meth.getElementName())) {
				return meth;
			}
		}
		return null;
	}

}
