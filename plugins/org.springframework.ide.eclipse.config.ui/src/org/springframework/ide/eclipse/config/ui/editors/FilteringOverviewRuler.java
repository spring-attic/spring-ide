/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.ui.editors;

import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.OverviewRuler;

/**
 * This is a specialized overview ruler that filters out certain annotations
 * from being shown using a specialized annotation model.
 *
 * @author Martin Lippert
 */
public class FilteringOverviewRuler extends OverviewRuler {

	public FilteringOverviewRuler(IAnnotationAccess annotationAccess, int width, ISharedTextColors sharedColors) {
		super(annotationAccess, width, sharedColors);
	}

	@Override
	public void setModel(IAnnotationModel model) {
		super.setModel(new FilteringAnnotationModel(model));
	}

}
