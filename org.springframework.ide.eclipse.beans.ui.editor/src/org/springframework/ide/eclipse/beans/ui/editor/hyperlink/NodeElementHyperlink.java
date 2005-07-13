package org.springframework.ide.eclipse.beans.ui.editor.hyperlink;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;

public class NodeElementHyperlink implements IHyperlink {

    private final IRegion region;

    private final IRegion targetRegion;
    
    private final ITextViewer viewer;

    /**
     * Creates a new Java element hyperlink.
     */
    public NodeElementHyperlink(IRegion region, IRegion targetRegion, ITextViewer viewer) {
        this.region = region;
        this.targetRegion = targetRegion;
        this.viewer = viewer;
    }
    
    public IRegion getHyperlinkRegion() {
        return this.region;
    }

    public String getTypeLabel() {
        return null;
    }

    public String getHyperlinkText() {
        return null;
    }

    public void open() {
        //viewer.removeRangeIndication();
        //viewer.setRangeIndication(this.targetRegion.getOffset(), this.targetRegion.getLength(), true);
        viewer.setSelectedRange(this.targetRegion.getOffset(), 0);
        viewer.revealRange(this.targetRegion.getOffset(), this.targetRegion.getLength());
    }

}
