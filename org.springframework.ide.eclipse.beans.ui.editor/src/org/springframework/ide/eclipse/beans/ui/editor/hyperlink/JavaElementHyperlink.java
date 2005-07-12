package org.springframework.ide.eclipse.beans.ui.editor.hyperlink;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.PartInitException;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * Java element hyperlink.
 */
public class JavaElementHyperlink implements IHyperlink {

    private final IRegion region;

    private final IJavaElement[] element;

    /**
     * Creates a new Java element hyperlink.
     */
    public JavaElementHyperlink(IRegion region, IJavaElement element) {
        this.region = region;
        this.element = new IJavaElement[] { element };
    }

    /**
     * Creates a new Java element hyperlink.
     */
    public JavaElementHyperlink(IRegion region, IJavaElement[] element) {
        this.region = region;
        this.element = element;
    }

    public IRegion getHyperlinkRegion() {
        return this.region;
    }

    public void open() {
        if (element != null && element.length > 0) {
            if (element[0] instanceof IType) {
                SpringUIUtils.openInEditor((IType) element[0]);
            }
            else if (element[0] instanceof IJavaElement) {
                try {
                    JavaUI.revealInEditor(JavaUI.openInEditor((IJavaElement) element[0]),
                            (IJavaElement) element[0]);
                }
                catch (PartInitException e) {
                }
                catch (JavaModelException e) {
                }
            }
        }
    }

   
    public String getTypeLabel() {
        return null;
    }

    
    public String getHyperlinkText() {
        return null;
    }
}
