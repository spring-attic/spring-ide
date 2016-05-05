/*******************************************************************************
 * Copyright (c) 2014-2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.reconciling;

import static org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesCompletionEngine.isAssign;
import static org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertiesProblemType.PROP_DEPRECATED;
import static org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertiesProblemType.PROP_UNKNOWN_PROPERTY;
import static org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertyProblem.problem;
import static org.springframework.ide.eclipse.boot.util.StringUtil.commonPrefix;

import java.util.Map;
import java.util.regex.Pattern;

import javax.inject.Provider;
import javax.print.Doc;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.IPropertiesFilePartitions;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.PropertiesFileEscapes;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;
import org.springframework.ide.eclipse.boot.properties.editor.FuzzyMap;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesCompletionEngine;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesEditorPlugin;
import org.springframework.ide.eclipse.boot.properties.editor.metadata.PropertyInfo;
import org.springframework.ide.eclipse.boot.properties.editor.util.Type;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeParser;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil;
import org.springframework.ide.eclipse.editor.support.reconcile.IProblemCollector;
import org.springframework.ide.eclipse.editor.support.reconcile.IReconcileEngine;
import org.springframework.ide.eclipse.editor.support.util.DocumentUtil;
import org.springframework.ide.eclipse.editor.support.util.ValueParser;

/**
 * Implements reconciling algorithm for {@link SpringPropertiesReconcileStrategy}.
 * <p>
 * The code in here could have been also part of the {@link SpringPropertiesReconcileStrategy}
 * itself, however isolating it here allows it to me more easily unit tested (no dependencies
 * on ISourceViewer which is difficult to 'mock' in testing harness.
 *
 * @author Kris De Volder
 */
@SuppressWarnings("restriction")
public class SpringPropertiesReconcileEngine implements IReconcileEngine {

	/**
	 * Regexp that matches a ',' surrounded by whitespace, including escaped whitespace / newlines
	 */
	private static final Pattern COMMA = Pattern.compile(
			"(\\s|\\\\\\s)*,(\\s|\\\\\\s)*"
	);

	private static final Pattern SPACES = Pattern.compile(
			"(\\s|\\\\\\s)*"
	);

	/**
	 * Regexp that matches a whitespace, including escaped whitespace
	 */
	private static final Pattern ASSIGN = SpringPropertiesCompletionEngine.ASSIGN;

	private Provider<FuzzyMap<PropertyInfo>> fIndexProvider;
	private TypeUtil typeUtil;
	private final DelimitedListReconciler commaListReconciler = new DelimitedListReconciler(COMMA, this::reconcileType);

	public SpringPropertiesReconcileEngine(Provider<FuzzyMap<PropertyInfo>> provider, TypeUtil typeUtil) {
		this.fIndexProvider = provider;
		this.typeUtil = typeUtil;
	}

	public void reconcile(IDocument doc, IProblemCollector problemCollector, IProgressMonitor mon) {
		FuzzyMap<PropertyInfo> index = getIndex();
		if (index==null || index.isEmpty()) {
			//don't report errors when index is empty, simply don't check (otherwise we will just reprot
			// all properties as errors, but this not really useful information since the cause is
			// some problem putting information about properties into the index.
			return;
		}
		problemCollector.beginCollecting();
		try {
			DuplicateNameChecker duplicateNameChecker = new DuplicateNameChecker(problemCollector);
			ITypedRegion[] regions = TextUtilities.computePartitioning(doc, IPropertiesFilePartitions.PROPERTIES_FILE_PARTITIONING, 0, doc.getLength(), true);
			if (regions!=null && regions.length>0) {
				mon.beginTask("Reconciling Spring Properties", regions.length);
				for (int i = 0; i < regions.length; i++) {
					ITypedRegion r = regions[i];
					try {
						String type = r.getType();
						if (IDocument.DEFAULT_CONTENT_TYPE.equals(type)) {
							DocumentRegion fullName = new DocumentRegion(doc, r).trim();
							if (fullName.isEmpty()) {
								if (!isAssigned(doc, r)) {
									//empty 'properties' are okay if not being assigned to. This just means that
									// there are empty sections in the props file and this is okay.
									continue;
								}
							}
							duplicateNameChecker.check(fullName);
							PropertyInfo validProperty = SpringPropertiesCompletionEngine.findLongestValidProperty(index, fullName.toString());
							if (validProperty!=null) {
								//TODO: Remove last remnants of 'IRegion trimmedRegion' here and replace
								// it all with just passing around 'fullName' DocumentRegion. This may require changes
								// in PropertyNavigator (probably these changes are also for the better making it simpler as well)
								IRegion trimmedRegion = fullName.asRegion();
								if (validProperty.isDeprecated()) {
									problemCollector.accept(problemDeprecated(fullName, validProperty));
								}
								int offset = validProperty.getId().length() + trimmedRegion.getOffset();
								PropertyNavigator navigator = new PropertyNavigator(doc, problemCollector, typeUtil, trimmedRegion);
								Type valueType = navigator.navigate(offset, TypeParser.parse(validProperty.getType()));
								if (valueType!=null) {
									reconcileType(doc, valueType, regions, i, problemCollector);
								}
							} else { //validProperty==null
								//The name is invalid, with no 'prefix' of the name being a valid property name.
								PropertyInfo similarEntry = index.findLongestCommonPrefixEntry(fullName.toString());
								CharSequence validPrefix = commonPrefix(similarEntry.getId(), fullName);
								problemCollector.accept(problemUnkownProperty(fullName, similarEntry, validPrefix));
							} //end: validProperty==null
						}
					} catch (Exception e) {
						SpringPropertiesEditorPlugin.log(e);
					}
				} //end: for regions
			}
		} catch (Throwable e2) {
			SpringPropertiesEditorPlugin.log(e2);
		} finally {
			problemCollector.endCollecting();
		}
	}

	protected SpringPropertyProblem problemDeprecated(DocumentRegion trimmedRegion, PropertyInfo property) {
		SpringPropertyProblem p = problem(PROP_DEPRECATED,
				TypeUtil.deprecatedPropertyMessage(
						property.getId(), null,
						property.getDeprecationReplacement(),
						property.getDeprecationReason()
				),
				trimmedRegion
		);
		p.setPropertyName(property.getId());
		return p;
	}

	protected SpringPropertyProblem problemUnkownProperty(DocumentRegion fullNameRegion,
			PropertyInfo similarEntry, CharSequence validPrefix) {
		String fullName = fullNameRegion.toString();
		SpringPropertyProblem p = problem(PROP_UNKNOWN_PROPERTY,
				"'"+fullName+"' is an unknown property."+suggestSimilar(similarEntry, validPrefix, fullName),
				fullNameRegion.subSequence(validPrefix.length())
		);
		p.setPropertyName(fullName);
		return p;
	}

	private FuzzyMap<PropertyInfo> getIndex() {
		return fIndexProvider.get();
	}

	private void reconcileType(IDocument doc, Type expectType, ITypedRegion[] regions, int i, IProblemCollector problems) {
		DocumentRegion escapedValue = getAssignedValue(doc, regions, i);
		if (escapedValue==null) {
			int charPos = DocumentUtil.lastNonWhitespaceCharOfRegion(doc, regions[i]);
			if (charPos>=0) {
				problems.accept(problem(SpringPropertiesProblemType.PROP_VALUE_TYPE_MISMATCH,
						"Expecting '"+typeUtil.niceTypeName(expectType)+"'",
						charPos, 1));
			}
		} else {
			reconcileType(escapedValue, expectType, problems);
		}
	}

	private void reconcileType(DocumentRegion escapedValue, Type expectType, IProblemCollector problems) {
		ValueParser parser = typeUtil.getValueParser(expectType);
		if (parser!=null) {
			try {
				String valueStr = PropertiesFileEscapes.unescape(escapedValue.toString());
				if (!valueStr.contains("${")) {
					//Don't check strings that look like they use variable substitution.
					parser.parse(valueStr);
				}
			} catch (Exception e) {
				problems.accept(problem(SpringPropertiesProblemType.PROP_VALUE_TYPE_MISMATCH,
						"Expecting '"+typeUtil.niceTypeName(expectType)+"'",
						escapedValue));
			}
		} else if (TypeUtil.isList(expectType)||TypeUtil.isArray(expectType)) {
			commaListReconciler.reconcile(escapedValue, expectType, problems);
		}
	}

	private DocumentRegion getAssignedValue(IDocument doc, ITypedRegion[] regions, int i) {
		int valueRegionIndex = i+1;
		if (valueRegionIndex<regions.length) {
			String valueRegionType = regions[valueRegionIndex].getType();
			DocumentRegion valueRegion = new DocumentRegion(doc, regions[valueRegionIndex]);
			if (IPropertiesFilePartitions.PROPERTY_VALUE.equals(valueRegionType)) {
				//Need to remove the 'ASSIGN' bit from the start
				valueRegion = valueRegion.trimStart(ASSIGN).trimEnd(SPACES);
				//region text includes
				//  potential padding with whitespace.
				//  the ':' or '=' (if its there).
				return valueRegion;
			}
		}
		return null;
	}

	private String suggestSimilar(PropertyInfo similarEntry, CharSequence validPrefix, CharSequence fullName) {
		int matchedChars = validPrefix.length();
		int wrongChars = fullName.length()-matchedChars;
		if (wrongChars<matchedChars) {
			return " Did you mean '"+similarEntry.getId()+"'?";
		} else {
			return "";
		}
	}

	/**
	 * Check that there is an assignment char directly following the given region.
	 */
	private boolean isAssigned(IDocument doc, IRegion r) {
		try {
			char c = doc.getChar(r.getOffset()+r.getLength());
			//Note either a '=' or a ':' can be used to assign properties.
			return isAssign(c);
		} catch (BadLocationException e) {
			//happens if looking for assignment char outside the document
			return false;
		}
	}

}
