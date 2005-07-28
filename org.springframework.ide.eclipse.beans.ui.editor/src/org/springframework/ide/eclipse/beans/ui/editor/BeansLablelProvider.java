package org.springframework.ide.eclipse.beans.ui.editor;

import org.eclipse.core.resources.IFile;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class BeansLablelProvider {
    
    public static String createAdditionalProposalInfo(Node bean, IFile file) {
            NamedNodeMap attributes = bean.getAttributes();
            StringBuffer buf = new StringBuffer();
            buf.append("<b>id: </b>");
            if (attributes.getNamedItem("id") != null) {
                buf.append(attributes.getNamedItem("id").getNodeValue());
            }
            if (attributes.getNamedItem("name") != null) {
                buf.append("<br><b>alias: </b>");
                buf.append(attributes.getNamedItem("name").getNodeValue());
            }
            buf.append("<br><b>class: </b>");
            if (attributes.getNamedItem("class") != null) {
                buf.append(attributes.getNamedItem("class").getNodeValue());
            }
            buf.append("<br><b>singleton: </b>");
            if (attributes.getNamedItem("singleton") != null) {
                buf.append(attributes.getNamedItem("singleton").getNodeValue());
            }
            else {
                buf.append("true");
            }
            buf.append("<br><b>abstract: </b>");
            if (attributes.getNamedItem("abstract") != null) {
                buf.append(attributes.getNamedItem("abstract").getNodeValue());
            }
            else {
                buf.append("false");
            }
            buf.append("<br><b>lazy-init: </b>");
            if (attributes.getNamedItem("lazy-init") != null) {
                buf.append(attributes.getNamedItem("lazy-init").getNodeValue());
            }
            else {
                buf.append("default");
            }
            buf.append("<br><b>filename: </b>");
            buf.append(file.getProjectRelativePath());
            return buf.toString();
        }


        public static String createAdditionalProposalInfo(IBean bean) {
            StringBuffer buf = new StringBuffer();
            buf.append("<b>id: </b>");
            buf.append(bean.getElementName());
            if (bean.getAliases() != null && bean.getAliases().length > 0) {
                buf.append("<br><b>alias: </b>");
                for (int i = 0; i < bean.getAliases().length; i++) {
                    buf.append(bean.getAliases()[i]);
                    if (i < bean.getAliases().length - 1) {
                        buf.append(", ");
                    }
                }
            }
            buf.append("<br><b>class: </b>");
            buf.append(bean.getClassName());
            buf.append("<br><b>singleton: </b>");
            buf.append(bean.isSingleton());
            buf.append("<br><b>abstract: </b>");
            buf.append(bean.isAbstract());
            buf.append("<br><b>lazy-init: </b>");
            buf.append(bean.isLazyInit());
            buf.append("<br><b>filename: </b>");
            buf.append(bean.getElementResource().getProjectRelativePath());
            return buf.toString();
        }
    
}
