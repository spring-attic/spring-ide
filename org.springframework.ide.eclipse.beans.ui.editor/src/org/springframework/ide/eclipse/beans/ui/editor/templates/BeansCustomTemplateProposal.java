/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

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
