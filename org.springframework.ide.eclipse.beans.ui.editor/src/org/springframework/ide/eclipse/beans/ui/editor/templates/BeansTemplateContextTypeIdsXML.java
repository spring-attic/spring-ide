package org.springframework.ide.eclipse.beans.ui.editor.templates;

public class BeansTemplateContextTypeIdsXML {

    public static final String ALL = getAll();

    public static final String BEAN = getBean();
    
    public static final String PROPERTY = getProperty();

    private static String getAll() {
        return getPrefix() + "_all"; 
    }
    
    private static String getProperty() {
        return getPrefix() + "_property"; 
    }

    private static String getBean() {
        return getPrefix() + "_bean"; 
    }
    
    private static String getPrefix() {
        return "spring"; 
    }

}
