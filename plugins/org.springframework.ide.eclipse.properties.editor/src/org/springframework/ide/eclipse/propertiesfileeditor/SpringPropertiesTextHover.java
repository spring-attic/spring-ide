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
package org.springframework.ide.eclipse.propertiesfileeditor;

import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.ISourceViewer;

@SuppressWarnings("restriction")
public class SpringPropertiesTextHover implements ITextHover, ITextHoverExtension {

	private String contentType;
	private SpringPropertiesCompletionEngine engine;

	public SpringPropertiesTextHover(ISourceViewer sourceViewer, String contentType, SpringPropertiesCompletionEngine engine) {
		this.contentType = contentType;
		this.engine = engine;
	}
	
    public IRegion getHoverRegion(ITextViewer tv, int offset) {
    	return engine.getHoverRegion(tv.getDocument(), offset);
     }
    
     public String getHoverInfo(ITextViewer tv, IRegion r) {
    	 return engine.getHoverInfo(tv.getDocument(), r, contentType);
     }

	@Override
	public IInformationControlCreator getHoverControlCreator() {
		return new SpringPropertiesInformationControlCreator();
	}

}
