package org.springframework.ide.eclipse.boot.properties.editor.metadata;

import javax.inject.Provider;

import org.springframework.boot.configurationmetadata.ValueHint;
import org.springframework.ide.eclipse.boot.util.StringUtil;
import org.springframework.ide.eclipse.editor.support.util.HtmlSnippet;

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

	private static final Provider<HtmlSnippet> EMPTY_DESCRIPTION = () -> HtmlSnippet.italic("No description");

	private final String value;
	private final Provider<HtmlSnippet> description;

	/**
	 * Create a hint with a textual description.
	 * <p>
	 * This constructor is private. Use one of the provided
	 * static 'create' methods instead.
	 */
	private StsValueHint(Object value, Provider<HtmlSnippet> description) {
		this.value = value==null?"null":value.toString();
		this.description = description;
	}

	public static StsValueHint create(ValueHint hint) {
		return create(hint.getValue(), hint.getDescription());
	}

	public static StsValueHint create(Object value, String description) {
		return new StsValueHint(value, textSnippet(description));
	}

	/**
	 * Create a html snippet from a text snippet.
	 */
	private static Provider<HtmlSnippet> textSnippet(String description) {
		if (StringUtil.hasText(description)) {
			return () -> HtmlSnippet.text(description);
		}
		return EMPTY_DESCRIPTION;
	}

	public String getValue() {
		return value;
	}

	public HtmlSnippet getDescription() {
		return description.get();
	}

}
