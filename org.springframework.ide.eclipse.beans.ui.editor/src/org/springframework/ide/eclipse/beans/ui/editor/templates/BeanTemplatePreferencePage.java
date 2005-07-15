package org.springframework.ide.eclipse.beans.ui.editor.templates;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;
import org.eclipse.wst.xml.ui.internal.XMLUIPlugin;
import org.eclipse.wst.xml.ui.internal.editor.IHelpContextIds;
import org.springframework.ide.eclipse.beans.ui.editor.BeansEditorPlugin;


/**
 * Preference page for XML templates
 */
public class BeanTemplatePreferencePage extends TemplatePreferencePage {

    public BeanTemplatePreferencePage() {
        BeansEditorPlugin xmlEditorPlugin = BeansEditorPlugin.getDefault();

        setPreferenceStore(xmlEditorPlugin.getPreferenceStore());
        setTemplateStore(xmlEditorPlugin.getTemplateStore());
        setContextTypeRegistry(xmlEditorPlugin.getTemplateContextRegistry());
        
        setMessage("Spring Beans XML Templates");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite ancestor) {
        Control c = super.createContents(ancestor);
        WorkbenchHelp.setHelp(c, IHelpContextIds.XML_PREFWEBX_TEMPLATES_HELPID);
        return c;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#isShowFormatterSetting()
     */
    protected boolean isShowFormatterSetting() {
        // template formatting has not been implemented
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    public boolean performOk() {
        boolean ok = super.performOk();
        BeansEditorPlugin.getDefault().savePluginPreferences();
        return ok;
    }
}
