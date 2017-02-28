/*******************************************************************************
 * Copyright (c) 2017 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;

public class SectionConfiguration {
	
	private int width;
	private int height;
	private int numColumns;

	public SectionConfiguration(int width, int height, int numColumns) {
		this.width = width;
		this.height = height;
		this.numColumns = numColumns;
	}
	
	public GridData getSectionAreaLayoutData() {
		return GridDataFactory.fillDefaults().grab(true, true).hint(width, height).create();
	}
	
	public GridLayout getSectionAreaLayout() {
		return GridLayoutFactory.fillDefaults().numColumns(numColumns).create();
	}

}
