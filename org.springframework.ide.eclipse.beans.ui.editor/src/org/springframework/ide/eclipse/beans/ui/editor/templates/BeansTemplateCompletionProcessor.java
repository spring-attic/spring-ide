/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
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
public class BeansTemplateCompletionProcessor extends
		TemplateCompletionProcessor {

	private String contextTypeId = null;

	@Override
	protected ICompletionProposal createProposal(Template template,
			TemplateContext context, IRegion region, int relevance) {
		return new BeansCustomTemplateProposal(template, context, region,
				getImage(template), relevance);
	}

	@Override
	protected TemplateContextType getContextType(ITextViewer viewer,
			IRegion region) {
		ContextTypeRegistry registry = getTemplateContextRegistry();
		if (registry != null) {
			return registry.getContextType(contextTypeId);
		}
		return null;
	}

	@Override
	protected Image getImage(Template template) {
		// just return the same image for now
		return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_SPRING);
	}

	private ContextTypeRegistry getTemplateContextRegistry() {
		return Activator.getDefault().getTemplateContextRegistry();
	}

	@Override
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
