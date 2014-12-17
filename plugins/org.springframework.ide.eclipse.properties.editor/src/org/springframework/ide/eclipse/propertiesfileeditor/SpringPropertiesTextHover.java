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

import org.eclipse.jdt.internal.ui.propertiesfileeditor.IPropertiesFilePartitions;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.ISourceViewer;

@SuppressWarnings("restriction")
public class SpringPropertiesTextHover implements ITextHover {

	private String contentType;

	public SpringPropertiesTextHover(ISourceViewer sourceViewer, String contentType) {
		this.contentType = contentType;
	}
	
    public IRegion getHoverRegion(ITextViewer tv, int offset) {
    	try {
//    		return tv.getDocument().getPartition(offset);
    		return TextUtilities.getPartition(tv.getDocument(), IPropertiesFilePartitions.PROPERTIES_FILE_PARTITIONING, offset, true);
    	} catch (Exception e) {
    		return null;
    	}
     }
    
     public String getHoverInfo(ITextViewer tv, IRegion r) {
        try {
           return contentType + "@" + r.getOffset() + ", "+r.getLength(); 
        }
        catch (Exception e) {            
           return ""; 
        }
     }

}
