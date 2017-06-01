/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.completions;

import static org.springsource.ide.eclipse.commons.core.util.StringUtil.arrayToCommaDelimitedString;
import static org.springsource.ide.eclipse.commons.core.util.StringUtil.collectionToCommaDelimitedString;

import java.util.Collection;

import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.boot.properties.editor.util.JavaTypeLinks;
import org.springframework.ide.eclipse.editor.support.hover.HoverInfo;
import org.springframework.ide.eclipse.editor.support.util.HtmlBuffer;
import org.springframework.ide.eclipse.editor.support.util.HtmlSnippet;
import org.springsource.ide.eclipse.commons.core.util.StringUtil;

/**
 * @author Kris De Volder
 */
public abstract class AbstractPropertyHoverInfo extends HoverInfo {

	@Override
	final protected String renderAsHtml() {
		JavaTypeLinks jtLinks = new JavaTypeLinks(this);
		HtmlBuffer html = new HtmlBuffer();

		renderId(html);

		String type = getType();
		if (type==null) {
			type = Object.class.getName();
		}
		jtLinks.javaTypeLink(html, getJavaProject(), type);

		String deflt = formatDefaultValue(getDefaultValue());
		if (deflt!=null) {
			html.raw("<br><br>");
			html.text("Default: ");
			html.raw("<i>");
			html.text(deflt);
			html.raw("</i>");
		}

		if (isDeprecated()) {
			html.raw("<br><br>");
			String reason = getDeprecationReason();
			if (StringUtil.hasText(reason)) {
				html.bold("Deprecated: ");
				html.text(reason);
			} else {
				html.bold("Deprecated!");
			}
		}

		HtmlSnippet description = getDescription();
		if (description!=null) {
			html.raw("<br><br>");
			html.snippet(description);
		}

		return html.toString();
	}


	final protected void renderId(HtmlBuffer html) {
		boolean deprecated = isDeprecated();
		String tag = deprecated ? "s" : "b";
		String replacement = getDeprecationReplacement();

		html.raw("<"+tag+">");
			html.text(getId());
		html.raw("</"+tag+">");
		if (StringUtil.hasText(replacement)) {
			html.text(" -> "+ replacement);
		}
		html.raw("<br>");
	}

	protected abstract Object getDefaultValue();
	protected abstract IJavaProject getJavaProject();
	protected abstract HtmlSnippet getDescription();
	protected abstract String getType();
	protected abstract String getDeprecationReason();
	protected abstract String getId();
	protected abstract String getDeprecationReplacement();
	protected abstract boolean isDeprecated();

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


}
