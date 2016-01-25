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
package org.springframework.ide.eclipse.boot.properties.editor.reconciling;

import static org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesCompletionEngine.isAssign;
import static org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertyProblem.problem;
import static org.springframework.ide.eclipse.boot.util.StringUtil.commonPrefix;

import javax.inject.Provider;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.IPropertiesFilePartitions;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.PropertiesFileEscapes;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.springframework.ide.eclipse.boot.properties.editor.FuzzyMap;
import org.springframework.ide.eclipse.boot.properties.editor.PropertyInfo;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesCompletionEngine;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesEditorPlugin;
import org.springframework.ide.eclipse.boot.properties.editor.util.DocumentUtil;
import org.springframework.ide.eclipse.boot.properties.editor.util.Type;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeParser;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil.ValueParser;
import org.springframework.ide.eclipse.editor.support.reconcile.IProblemCollector;
import org.springframework.ide.eclipse.editor.support.reconcile.IReconcileEngine;

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

	private Provider<FuzzyMap<PropertyInfo>> fIndexProvider;
	private TypeUtil typeUtil;

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
			ITypedRegion[] regions = TextUtilities.computePartitioning(doc, IPropertiesFilePartitions.PROPERTIES_FILE_PARTITIONING, 0, doc.getLength(), true);
			if (regions!=null && regions.length>0) {
				mon.beginTask("Reconciling Spring Properties", regions.length);
				for (int i = 0; i < regions.length; i++) {
					ITypedRegion r = regions[i];
					try {
						String type = r.getType();
						if (IDocument.DEFAULT_CONTENT_TYPE.equals(type)) {
							String fullName = doc.get(r.getOffset(), r.getLength()).trim();
							IRegion trimmedRegion = r;
							if (fullName.length()<r.getLength()) {
								String paddedName = doc.get(r.getOffset(), r.getLength());
								int start = paddedName.indexOf(fullName);
								trimmedRegion = new Region(r.getOffset()+start, fullName.length());
							}
							if (fullName.isEmpty()) {
								if (!isAssigned(doc, r)) {
									//empty 'properties' are okay if not being assigned to. This just means that
									// there are empty sections in the props file and this is okay.
									continue;
								}
							}
							PropertyInfo validProperty = SpringPropertiesCompletionEngine.findLongestValidProperty(index, fullName);
							if (validProperty!=null) {
								int offset = validProperty.getId().length() + trimmedRegion.getOffset();
								PropertyNavigator navigator = new PropertyNavigator(doc, problemCollector, typeUtil, trimmedRegion);
								Type valueType = navigator.navigate(offset, TypeParser.parse(validProperty.getType()));
								if (valueType!=null) {
									reconcileType(doc, valueType, regions, i, problemCollector);
								}
							} else { //validProperty==null
								//The name is invalid, with no 'prefix' of the name being a valid property name.
								PropertyInfo similarEntry = index.findLongestCommonPrefixEntry(fullName);
								String validPrefix = commonPrefix(similarEntry.getId(), fullName);
								problemCollector.accept(problemUnkownProperty(fullName, trimmedRegion, similarEntry, validPrefix));
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

	protected SpringPropertyProblem problemUnkownProperty(String fullName, IRegion trimmedRegion,
			PropertyInfo similarEntry, String validPrefix) {
		SpringPropertyProblem p = problem(SpringPropertiesProblemType.PROP_UNKNOWN_PROPERTY,
				"'"+fullName+"' is an unknown property."+suggestSimilar(similarEntry, validPrefix, fullName),
				trimmedRegion.getOffset()+validPrefix.length(), trimmedRegion.getLength()-validPrefix.length());
		p.setPropertyName(fullName);
		return p;
	}

	private FuzzyMap<PropertyInfo> getIndex() {
		return fIndexProvider.get();
	}

	private void reconcileType(IDocument doc, Type expectType, ITypedRegion[] regions, int i, IProblemCollector problems) {
		ValueParser parser = typeUtil.getValueParser(expectType);
		if (parser!=null) {
			String escapedValue = getAssignedValue(doc, regions, i);
			IRegion errorRegion = null;
			if (escapedValue==null) {
				int charPos = DocumentUtil.lastNonWhitespaceCharOfRegion(doc, regions[i]);
				if (charPos>=0) {
					errorRegion = new Region(charPos, 1);
				}
			} else { //paddedValue!=null
				try {
					String valueStr = PropertiesFileEscapes.unescape(escapedValue.trim());
					if (!valueStr.contains("${")) {
						//Don't check strings that look like they use variable substitution.
						parser.parse(valueStr);
					}
				} catch (Exception e) {
					errorRegion = regions[i+1]; //i+1 must be in range, otherwise paddedValue would be null
					//Try to shrink errorRegion to demarkate the value String more precisely
					try {
						int endChar = DocumentUtil.lastNonWhitespaceCharOfRegion(doc, errorRegion);
						if (endChar>=0) {
							int startChar = DocumentUtil.firstNonWhitespaceCharOfRegion(doc, errorRegion);
							if (startChar>=0) {
								char assign = doc.getChar(startChar);
								if (isAssign(assign)) {
									startChar = DocumentUtil.firstNonWhitespaceCharOfRegion(doc, new Region(startChar+1, endChar-startChar));
								}
							}
							if (startChar>=0) {
								errorRegion = new Region(startChar, endChar-startChar+1);
							}
						}
					} catch (Exception e2) {
						SpringPropertiesEditorPlugin.log(e2);
					}
				}
			}
			if (errorRegion!=null) {
				problems.accept(problem(SpringPropertiesProblemType.PROP_VALUE_TYPE_MISMATCH,
						"Expecting '"+typeUtil.niceTypeName(expectType)+"'",
						errorRegion.getOffset(), errorRegion.getLength()));
			}
		}
	}

	/**
	 * Extract the 'assigned' value represented as String from document.
	 *
	 * @param doc The document
	 * @param regions Regions in the document
	 * @param i Target region (i.e. points at the 'key' region for which we want to find assigned value
	 */
	private String getAssignedValue(IDocument doc, ITypedRegion[] regions, int i) {
		try {
			int valueRegionIndex = i+1;
			if (valueRegionIndex<regions.length) {
				ITypedRegion valueRegion = regions[valueRegionIndex];
				if (valueRegion.getType()==IPropertiesFilePartitions.PROPERTY_VALUE) {
					String regionText = doc.get(valueRegion.getOffset(), valueRegion.getLength());
					//region text includes
					//  potential padding with whitespace.
					//  the ':' or '=' (if its there).
					regionText = regionText.trim();
					if (!regionText.isEmpty()) {
						char assignment = regionText.charAt(0);
						if (isAssign(assignment)) {
							//strip of 'assignment' and also more whitepace which might occur
							//after it.
							regionText = regionText.substring(1).trim(); //
						}
					}
					return regionText;
				}
			}
		} catch (Exception e) {
			SpringPropertiesEditorPlugin.log(e);
		}
		return null;
	}

	private String suggestSimilar(PropertyInfo similarEntry, String validPrefix, String fullName) {
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
