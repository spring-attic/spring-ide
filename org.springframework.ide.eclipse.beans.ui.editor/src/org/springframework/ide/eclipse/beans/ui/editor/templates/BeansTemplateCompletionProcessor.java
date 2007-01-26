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
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.editor.Activator;

/**
 * Completion processor for Spring beans config templates. Most of the work is
 * already done by the <code>BeansContentAssistProcessor</code>, so by the
 * time the <code>BeansTemplateCompletionProcessor</code> is asked for content
 * assist proposals, the <code>BeansContentAssistProcessor</code> has already
 * set the context type for templates.
 */
public class BeansTemplateCompletionProcessor extends TemplateCompletionProcessor {

    private String contextTypeId = null;

    protected ICompletionProposal createProposal(Template template,
    				  TemplateContext context, IRegion region, int relevance) {
        return new BeansCustomTemplateProposal(template, context, region,
        										getImage(template), relevance);
    }

    protected TemplateContextType getContextType(ITextViewer viewer,
    											 IRegion region) {
        ContextTypeRegistry registry = getTemplateContextRegistry();
        if (registry != null) {
            return registry.getContextType(contextTypeId);
        }
        return null;
    }

    protected Image getImage(Template template) {
        // just return the same image for now
        return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_SPRING);
    }

    private ContextTypeRegistry getTemplateContextRegistry() {
        return Activator.getDefault().getTemplateContextRegistry();
    }

    protected Template[] getTemplates(String contextTypeId) {
        TemplateStore store = getTemplateStore();
        if (store != null) {
            return store.getTemplates(contextTypeId);
        }
        return null;
    }

    private TemplateStore getTemplateStore() {
        return Activator.getDefault().getTemplateStore();
    }

    public void setContextType(String contextTypeId) {
        this.contextTypeId = contextTypeId;
    }
}
