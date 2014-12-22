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
package org.springframework.ide.eclipse.propertiesfileeditor.reconciling;

import static org.springframework.ide.eclipse.propertiesfileeditor.SpringPropertiesCompletionEngine.ASSIGNABLE_TYPES;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.IPropertiesFilePartitions;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.springframework.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.ide.eclipse.propertiesfileeditor.FuzzyMap;
import org.springframework.ide.eclipse.propertiesfileeditor.SpringPropertiesEditorPlugin;
import org.springframework.ide.eclipse.propertiesfileeditor.util.StringUtil;

/**
 * Implements reconciling algorithm for {@link SpringPropertiesReconcileStrategy}.
 * <p>
 * The code in here could have been also part of the {@link SpringPropertiesReconcileStrategy}
 * itself, however isolating it here allows it to me more easily unit tested (no dependencies
 * on ISourceViewer which is difficult to 'mock' in testing harness.
 * 
 * @author Kris De Volder
 */
public class SpringPropertiesReconcileEngine {

	private FuzzyMap<ConfigurationMetadataProperty> fIndex;

	public interface IProblemCollector {

		void beginCollecting();
		void endCollecting();
		void accept(SpringPropertyProblem springPropertyProblem);

	}

	
	public SpringPropertiesReconcileEngine(FuzzyMap<ConfigurationMetadataProperty> index) {
		fIndex = index;
	}

	/**
	 * Used by Reconciling to scan document regions for invalid propery names and report them as problems.
	 */
	public void reconcile(IDocument doc, IProblemCollector problemCollector, IProgressMonitor mon) {
		if (fIndex==null || fIndex.isEmpty()) {
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
				for (ITypedRegion r : regions) {
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
							ConfigurationMetadataProperty validProperty = findLongestValidProperty(fullName);
							if (validProperty!=null) {
								if (validProperty.getId().length()==fullName.length()) {
									//exact match. Do not complain.
								} else { //found a 'validPrefix' which is shorter than the fullName.
									//check if it looks okay to continue with sub-properties at based on property type
									String validPrefix = validProperty.getId();
									if (ASSIGNABLE_TYPES.contains(validProperty.getType())) {
										problemCollector.accept(new SpringPropertyProblem(
												"Supbproperties are invalid for property "+
														"'"+validPrefix+"' with type '"+validProperty.getType()+"'", 
														trimmedRegion.getOffset()+validPrefix.length(),
														trimmedRegion.getLength()-validPrefix.length()
												));
									} else { //type is not a known directly assignable type
										//accessing sub-properties with '.' is probably ok in this case. 
										// So do not complain
									}
								}
							} else { //validProperty==null
								//The name is invalid, with no 'prefix' of the name being a valid property name.
								ConfigurationMetadataProperty similarEntry = fIndex.findLongestCommonPrefixEntry(fullName);
								String validPrefix = StringUtil.commonPrefix(similarEntry.getId(), fullName);
								problemCollector.accept(new SpringPropertyProblem("'"+fullName+"' is an unknown property."+suggestSimilar(similarEntry, validPrefix, fullName), 
										trimmedRegion.getOffset()+validPrefix.length(), trimmedRegion.getLength()-validPrefix.length()));
							} //end: validProperty==null
						}
						//TODO: check value types.
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

	private String suggestSimilar(ConfigurationMetadataProperty similarEntry, String validPrefix, String fullName) {
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
			return c==':' || c=='=';
		} catch (BadLocationException e) {
			//happens if looking for assignment char outside the document
			return false;
		}
	}

	/**
	 * Find the longest known property that is a prefix of the given name. Here prefix does not mean
	 * 'string prefix' but a prefix in the sense of treating '.' as a kind of separators. So 
	 * 'prefix' is not allowed to end in the middle of a 'segment'.
	 */
	private ConfigurationMetadataProperty findLongestValidProperty(String name) {
		int endPos = name.length();
		ConfigurationMetadataProperty prop = null;
		while (endPos>0 && prop==null) {
			prop = fIndex.get(name.substring(0, endPos));
			if (prop==null) {
				endPos = name.lastIndexOf('.', endPos-1);
			}
		}
		return prop;
	}


}
