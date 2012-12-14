/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.livegraph.model;

/**
 * @author Leo Dos Santos
 */
public class LiveBeansResource extends LiveBeansGroup {

	private String displayName;

	public LiveBeansResource(String label) {
		super(label);
	}

	@Override
	public String getDisplayName() {
		// compute the display name the first time it's needed
		if (displayName == null) {
			String label = getLabel();
			if (label.equalsIgnoreCase("null")) {
				displayName = "Container Generated";
			}
			else {
				// Expecting the label to contain some form of
				// "[file/path/to/resource.ext]" so we're going to parse out the
				// last segment of the file path.
				int indexStart = label.lastIndexOf("/");
				int indexEnd = label.lastIndexOf("]");
				if (indexStart > -1 && indexEnd > -1 && indexStart < indexEnd) {
					displayName = label.substring(indexStart + 1, indexEnd);
				}
			}
			if (displayName == null) {
				displayName = label;
			}
		}
		return displayName;
	}

}
