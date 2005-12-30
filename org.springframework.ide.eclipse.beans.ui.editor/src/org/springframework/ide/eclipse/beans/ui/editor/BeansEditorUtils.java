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

package org.springframework.ide.eclipse.beans.ui.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class BeansEditorUtils {

    public static final List getBeansFromConfigSets(IFile file) {
        List beans = new ArrayList();
        Map configsMap = new HashMap();
        IBeansProject project = BeansCorePlugin.getModel().getProject(file.getProject());
        Iterator configSets = project.getConfigSets().iterator();

        while (configSets.hasNext()) {
            IBeansConfigSet configSet = (IBeansConfigSet) configSets.next();
            if (configSet.hasConfig(file)) {
                Iterator configs = configSet.getConfigs().iterator();
                while (configs.hasNext()) {
                    String beansConfigName = (String) configs.next();
                    IBeansConfig beansConfig = project.getConfig(beansConfigName);
                    if (beansConfig != null) {
	                    IResource resource = beansConfig.getElementResource();
	                    if (!configsMap.containsKey(resource.getName()) &&
	                    		!resource.getFullPath().equals(file.getFullPath())) {
	                        configsMap.put(resource.getName(), beansConfig);
	                    }
                    }
                }
            }
        }
        
        Iterator paths = configsMap.keySet().iterator();
        while (paths.hasNext()) {
            IBeansConfig beansConfig = (IBeansConfig) configsMap.get((String) paths.next());
            beans.addAll(beansConfig.getBeans());
        }
        return beans;
    }

	public static final boolean isSpringStyleOutline() {
		return BeansEditorPlugin.getDefault().getPreferenceStore()
					.getBoolean(IPreferencesConstants.OUTLINE_SPRING);
	}

	public static final String createAdditionalProposalInfo(Node bean, IFile file) {
	    NamedNodeMap attributes = bean.getAttributes();
	    StringBuffer buf = new StringBuffer();
	    buf.append("<b>id:</b> ");
	    if (attributes.getNamedItem("id") != null) {
	        buf.append(attributes.getNamedItem("id").getNodeValue());
	    }
	    if (attributes.getNamedItem("name") != null) {
	        buf.append("<br><b>alias:</b> ");
	        buf.append(attributes.getNamedItem("name").getNodeValue());
	    }
	    buf.append("<br><b>class:</b> ");
	    if (attributes.getNamedItem("class") != null) {
	        buf.append(attributes.getNamedItem("class").getNodeValue());
	    }
	    buf.append("<br><b>singleton:</b> ");
	    if (attributes.getNamedItem("singleton") != null) {
	        buf.append(attributes.getNamedItem("singleton").getNodeValue());
	    } else {
	        buf.append("true");
	    }
	    buf.append("<br><b>abstract:</b> ");
	    if (attributes.getNamedItem("abstract") != null) {
	        buf.append(attributes.getNamedItem("abstract").getNodeValue());
	    } else {
	        buf.append("false");
	    }
	    buf.append("<br><b>lazy-init:</b> ");
	    if (attributes.getNamedItem("lazy-init") != null) {
	        buf.append(attributes.getNamedItem("lazy-init").getNodeValue());
	    } else {
	        buf.append("default");
	    }
	    buf.append("<br><b>filename:</b> ");
	    buf.append(file.getProjectRelativePath());
	    return buf.toString();
	}

	public static final String createAdditionalProposalInfo(IBean bean) {
	    StringBuffer buf = new StringBuffer();
	    buf.append("<b>id:</b> ");
	    buf.append(bean.getElementName());
	    if (bean.getAliases() != null && bean.getAliases().length > 0) {
	        buf.append("<br><b>alias:</b> ");
	        for (int i = 0; i < bean.getAliases().length; i++) {
	            buf.append(bean.getAliases()[i]);
	            if (i < bean.getAliases().length - 1) {
	                buf.append(", ");
	            }
	        }
	    }
	    buf.append("<br><b>class:</b> ");
	    buf.append(bean.getClassName());
	    buf.append("<br><b>singleton:</b> ");
	    buf.append(bean.isSingleton());
	    buf.append("<br><b>abstract:</b> ");
	    buf.append(bean.isAbstract());
	    buf.append("<br><b>lazy-init:</b> ");
	    buf.append(bean.isLazyInit());
	    buf.append("<br><b>filename:</b> ");
	    buf.append(bean.getElementResource().getProjectRelativePath());
	    return buf.toString();
	}
}
