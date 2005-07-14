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
import org.eclipse.wst.xml.ui.internal.XMLUIPlugin;
import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImageHelper;
import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImages;
import org.springframework.ide.eclipse.beans.ui.editor.BeansEditorPlugin;


/**
 * Completion processor for XML Templates. Most of the work is already done
 * by the XML Content Assist processor, so by the time the
 * XMLTemplateCompletionProcessor is asked for content assist proposals, the
 * XML content assist processor has already set the context type for
 * templates.
 */
public class BeansTemplateCompletionProcessor extends TemplateCompletionProcessor {
    private String fContextTypeId = null;

    protected ICompletionProposal createProposal(Template template, TemplateContext context, IRegion region, int relevance) {
        return new BeansCustomTemplateProposal(template, context, region, getImage(template), relevance);
    }

    protected TemplateContextType getContextType(ITextViewer viewer, IRegion region) {
        TemplateContextType type = null;

        ContextTypeRegistry registry = getTemplateContextRegistry();
        if (registry != null)
            type = registry.getContextType(fContextTypeId);

        return type;
    }

    protected Image getImage(Template template) {
        // just return the same image for now
        return XMLEditorPluginImageHelper.getInstance().getImage(XMLEditorPluginImages.IMG_OBJ_TAG_MACRO);
    }

    private ContextTypeRegistry getTemplateContextRegistry() {
        return BeansEditorPlugin.getDefault().getTemplateContextRegistry();
    }

    protected Template[] getTemplates(String contextTypeId) {
        Template templates[] = null;

        TemplateStore store = getTemplateStore();
        if (store != null)
            templates = store.getTemplates(contextTypeId);

        return templates;
    }

    private TemplateStore getTemplateStore() {
        return BeansEditorPlugin.getDefault().getTemplateStore();
    }

    public void setContextType(String contextTypeId) {
        fContextTypeId = contextTypeId;
    }
}
