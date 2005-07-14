package org.springframework.ide.eclipse.beans.ui.editor.templates;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.sse.core.internal.util.StringUtils;
import org.eclipse.wst.sse.ui.internal.contentassist.IRelevanceCompletionProposal;

/**
 * Purpose of this class is to make the additional proposal info into content
 * fit for an HTML viewer (by escaping characters)
 */
public class BeansCustomTemplateProposal extends TemplateProposal implements IRelevanceCompletionProposal {
    // copies of this class exist in:
    // org.eclipse.jst.jsp.ui.internal.contentassist
    // org.eclipse.wst.html.ui.internal.contentassist
    // org.eclipse.wst.xml.ui.internal.contentassist

    public BeansCustomTemplateProposal(Template template, TemplateContext context, IRegion region, Image image, int relevance) {
        super(template, context, region, image, relevance);
    }

    public String getAdditionalProposalInfo() {
        String additionalInfo = super.getAdditionalProposalInfo();
        return StringUtils.convertToHTMLContent(additionalInfo);
    }
}
