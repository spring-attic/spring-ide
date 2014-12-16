package org.springframework.ide.eclipse.propertiesfileeditor;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.DefaultTextHover;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ISourceViewer;

public class SpringPropertiesTextHover implements ITextHover {

	private String contentType;

	public SpringPropertiesTextHover(ISourceViewer sourceViewer, String contentType) {
		this.contentType = contentType;
	}
	
    public IRegion getHoverRegion(ITextViewer tv, int off) {
        return new Region(off, 0);
     }
     public String getHoverInfo(ITextViewer tv, IRegion r) {
        try {
           IDocument doc = tv.getDocument();
           return contentType + "@" + r.getOffset() + ", "+r.getLength(); 
        }
        catch (Exception e) {            
           return ""; 
        }
     }

}
