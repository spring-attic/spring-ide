package org.springframework.ide.eclipse.boot.properties.editor.metadata;

import static org.eclipse.jdt.internal.ui.text.javadoc.JavadocContentAccess2.getHTMLContent;

import javax.inject.Provider;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.springframework.boot.configurationmetadata.Deprecation;
import org.springframework.boot.configurationmetadata.ValueHint;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springframework.ide.eclipse.editor.support.util.HtmlSnippet;
import org.springframework.ide.eclipse.editor.support.util.StringUtil;

import static org.springframework.ide.eclipse.boot.properties.editor.metadata.DeprecationUtil.*;

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

	public static StsValueHint create(String value) {
		return new StsValueHint(value, EMPTY_DESCRIPTION_PROVIDER, null);
	}

	public static StsValueHint create(ValueHint hint) {
		return new StsValueHint(""+hint.getValue(), textSnippet(hint.getDescription()), null);
	}

	public static StsValueHint create(String value, IField enumField) {
		return new StsValueHint(value, javaDocSnippet(enumField), DeprecationUtil.extract(enumField)) {
			@Override
			public IJavaElement getJavaElement() {
				return enumField;
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

	public static Provider<HtmlSnippet> javaDocSnippet(IField f) {
		return () -> {
			try {
				@SuppressWarnings("restriction")
				String htmlText = getHTMLContent(f, true);
				if (StringUtil.hasText(htmlText)) {
					return HtmlSnippet.raw(htmlText);
				}
			} catch (CoreException e) {
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


}
