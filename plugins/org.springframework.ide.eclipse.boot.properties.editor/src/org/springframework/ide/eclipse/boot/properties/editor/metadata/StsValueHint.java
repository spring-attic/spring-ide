/*******************************************************************************
 * Copyright (c) 2015, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.metadata;

import javax.inject.Provider;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.ui.text.java.hover.JavadocHover;
import org.springframework.boot.configurationmetadata.Deprecation;
import org.springframework.boot.configurationmetadata.ValueHint;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springframework.ide.eclipse.editor.support.util.HtmlSnippet;
import org.springsource.ide.eclipse.commons.core.util.StringUtil;

/**
 * Sts version of {@link ValueHint} contains similar data, but accomoates
 * a html snippet to be computed lazyly for the description.
 * <p>
 * This is meant to support using data pulled from JavaDoc in enums as description.
 * This data is a html snippet, whereas the data derived from spring-boot metadata is
 * just plain text.
 *
 * @author Kris De Volder
 */
@SuppressWarnings("restriction")
public class StsValueHint {

	private static final HtmlSnippet EMPTY_DESCRIPTION = HtmlSnippet.italic("No description");

	private static final Provider<HtmlSnippet> EMPTY_DESCRIPTION_PROVIDER = () -> EMPTY_DESCRIPTION;

	private final String value;
	private final Provider<HtmlSnippet> description;
	private final Deprecation deprecation;

	/**
	 * Create a hint with a textual description.
	 * <p>
	 * This constructor is private. Use one of the provided
	 * static 'create' methods instead.
	 */
	private StsValueHint(String value, Provider<HtmlSnippet> description, Deprecation deprecation) {
		this.value = value==null?"null":value.toString();
		Assert.isLegal(!this.value.startsWith("StsValueHint"));
		this.description = description;
		this.deprecation = deprecation;
	}

	/**
	 * Creates a hint out of an IJavaElement.
	 */
	public static StsValueHint create(String value, IJavaElement javaElement) {
		return new StsValueHint(value, javaDocSnippet(javaElement), DeprecationUtil.extract(javaElement)) {
			@Override
			public IJavaElement getJavaElement() {
				return javaElement;
			}
		};
	}

	public static StsValueHint create(String value) {
		return new StsValueHint(value, EMPTY_DESCRIPTION_PROVIDER, null);
	}

	public static StsValueHint create(ValueHint hint) {
		return new StsValueHint(""+hint.getValue(), textSnippet(hint.getDescription()), null);
	}

	public static StsValueHint className(String fqName, TypeUtil typeUtil) {
		try {
			IJavaProject jp = typeUtil.getJavaProject();
			if (jp!=null) {
				IType type = jp.findType(fqName, new NullProgressMonitor());
				if (type!=null) {
					return create(type);
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return null;
	}

	public static StsValueHint create(IType klass) {
		return new StsValueHint(klass.getFullyQualifiedName(), javaDocSnippet(klass), DeprecationUtil.extract(klass)) {
			@Override
			public IJavaElement getJavaElement() {
				return klass;
			}
		};
	}

	/**
	 * Create a html snippet from a text snippet.
	 */
	private static Provider<HtmlSnippet> textSnippet(String description) {
		if (StringUtil.hasText(description)) {
			return () -> HtmlSnippet.text(description);
		}
		return EMPTY_DESCRIPTION_PROVIDER;
	}

	public String getValue() {
		return value;
	}

	public HtmlSnippet getDescription() {
		return description.get();
	}
	public Provider<HtmlSnippet> getDescriptionProvider() {
		return description;
	}

	public static Provider<HtmlSnippet> javaDocSnippet(IJavaElement je) {
		return () -> {
			try {
				@SuppressWarnings("restriction")
				String htmlText = JavadocHover.getHoverInfo(new IJavaElement[] { je }, null, null, null).getHtml();
				if (StringUtil.hasText(htmlText)) {
					return HtmlSnippet.raw(htmlText);
				}
			} catch (Exception e) {
				Log.log(e);
			}
			return EMPTY_DESCRIPTION;
		};
	}

	@Override
	public String toString() {
		return "StsValueHint("+value+")";
	}

	public Deprecation getDeprecation() {
		return deprecation;
	}

	public IJavaElement getJavaElement() {
		return null;
	}

	public StsValueHint prefixWith(String prefix) {
		StsValueHint it = this;
		return new StsValueHint(prefix+getValue(), description, deprecation) {
			@Override
			public IJavaElement getJavaElement() {
				return it.getJavaElement();
			}
		};
	}

}
