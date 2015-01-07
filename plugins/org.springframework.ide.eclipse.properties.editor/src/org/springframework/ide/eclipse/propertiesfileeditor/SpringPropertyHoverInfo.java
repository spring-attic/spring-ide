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
package org.springframework.ide.eclipse.propertiesfileeditor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.internal.text.html.BrowserInformationControlInput;
import org.springframework.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.ide.eclipse.propertiesfileeditor.PropertyInfo.PropertySource;

/**
 * Information object that is displayed in SpringPropertiesTextHover's information
 * control.
 * <p>
 * Essentially this is a wrapper around {@link ConfigurationMetadataProperty}
 * 
 * @author Kris De Volder
 */
@SuppressWarnings("restriction")
public class SpringPropertyHoverInfo extends BrowserInformationControlInput {

	private static final String[] NO_ARGS = new String[0];
	
	private IJavaProject javaProject; // Java project which is used to find declaration for 'navigate to declaration action
	private PropertyInfo data; //Data object to display in 'hover info control'

	public SpringPropertyHoverInfo(IJavaProject project, PropertyInfo data) {
		super(null);
		this.javaProject = project;
		this.data = data;
	}

	@Override
	public String getHtml() {
		return getHtmlHoverText(data);
	}

	public static String getHtmlHoverText(PropertyInfo data) {
		HtmlBuffer html = new HtmlBuffer();
		
		html.raw("<b>");
			html.text(data.getId());
		html.raw("</b>");
		html.raw("<br>");
		
		String type = data.getType();
		if (type==null) {
			type = Object.class.getName();
		}
		html.raw("<a href=\"");
		html.url("type/"+type);
		html.raw("\">");
		html.text(type);
		html.raw("</a>");
		
		
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
			} else {
				//no idea what it is so ignore
			}
		}
		return null;
	}

	@Override
	public Object getInputElement() {
		return this;
	}

	@Override
	public String getInputName() {
		return "";
	}

	public PropertyInfo getElement() {
		return data;
	}

	public boolean canOpenDeclaration() {
		return getJavaElements()!=null;
	}
	
	/**
	 * Get IJavaElements that define this property.
	 */
	public List<IJavaElement> getJavaElements() {
		try {
			if (javaProject!=null) {
				SpringPropertiesCompletionEngine.debug("javaProject = "+javaProject.getElementName());
				List<PropertySource> sources = data.getSources();
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

	public void openDeclaration() {
		try {
			List<IJavaElement> elements = getJavaElements();
			if (elements!=null && !elements.isEmpty()) {
				JavaUI.openInEditor(elements.get(0));
			}
		} catch (Exception e) {
			SpringPropertiesEditorPlugin.log(e);
		}
	}
	

}
