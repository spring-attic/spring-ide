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
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.EditorsUI;

public class SpringPropertiesTextHover implements ITextHover, ITextHoverExtension, ITextHoverExtension2 {

	private String contentType;
	private SpringPropertiesCompletionEngine engine;
	private ITextHover delegate;

	public SpringPropertiesTextHover(ISourceViewer sourceViewer, String contentType, SpringPropertiesCompletionEngine engine, ITextHover delegate) {
		this.contentType = contentType;
		this.engine = engine;
		this.delegate = delegate;
	}
	
    public IRegion getHoverRegion(ITextViewer tv, int offset) {
    	//Note that we ask the 'delegate' first. This is because it handles info about 
    	// error markers from annotations model. And this info should 'override'
    	// information about property
    	IRegion r = delegate.getHoverRegion(tv, offset);
    	if (r!=null) {
    		return r;
    	}
    	return engine.getHoverRegion(tv.getDocument(), offset);
     }
    
     public String getHoverInfo(ITextViewer tv, IRegion r) {
     	String s = delegate.getHoverInfo(tv, r);
     	if (s!=null) {
     		return text2html(s);
     	}
     	return engine.getHoverInfo(tv.getDocument(), r, contentType);
     }
     
 	private String text2html(String s) {
		HtmlBuffer buf = new HtmlBuffer();
		buf.text(s);
		return buf.toString();
	}

	@Override
 	public Object getHoverInfo2(ITextViewer tv, IRegion r) {
 		if (delegate instanceof ITextHoverExtension2) {
 			Object it = ((ITextHoverExtension2) delegate).getHoverInfo2(tv, r);
 			if (it!=null) {
 				if (it instanceof String) {
 					return text2html((String)it);
 				} else {
 					return it;
 				}
 			}
 		}
 		return engine.getHoverInfo(tv.getDocument(), r, contentType);
 	}
     

	@Override
	public IInformationControlCreator getHoverControlCreator() {
		return new SpringPropertiesInformationControlCreator(EditorsUI.getTooltipAffordanceString());
	}
	
}
