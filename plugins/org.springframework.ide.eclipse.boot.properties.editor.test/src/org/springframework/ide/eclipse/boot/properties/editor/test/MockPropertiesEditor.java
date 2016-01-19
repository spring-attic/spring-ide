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
package org.springframework.ide.eclipse.boot.properties.editor.test;

import org.eclipse.jdt.internal.ui.propertiesfileeditor.PropertiesFileDocumentSetupParticipant;

/**
 * Basic 'simulated' editor. Contains text and a cursor position / selection.
 */
@SuppressWarnings("restriction")
public class MockPropertiesEditor extends MockEditor {

	/**
	 * Create mock editor. Selection position is initialized by looking for the CURSOR string.
	 * <p>
	 * THe cursor string is not actually considered part of the text, but only a marker for
	 * the cursor position.
	 * <p>
	 * If one 'cursor' marker is present in the text the selection
	 * is length 0 and starts at the marker.
	 * <p>
	 * If two markers are present the selection is between the two
	 * markers.
	 * <p>
	 * If no markers are present the cursor is placed at the very end of the document.
	 */
	public MockPropertiesEditor(String text) {
		super(text);
		PropertiesFileDocumentSetupParticipant.setupDocument(document);
	}


}