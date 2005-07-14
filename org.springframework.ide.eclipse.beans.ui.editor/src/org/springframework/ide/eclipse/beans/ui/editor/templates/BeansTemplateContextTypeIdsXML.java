package org.springframework.ide.eclipse.beans.ui.editor.templates;

public class BeansTemplateContextTypeIdsXML {

    public static final String ALL = getAll();

    private static String getAll() {
        return getPrefix() + "_all"; 
    }

    private static String getPrefix() {
        return "spring"; 
    }

}
