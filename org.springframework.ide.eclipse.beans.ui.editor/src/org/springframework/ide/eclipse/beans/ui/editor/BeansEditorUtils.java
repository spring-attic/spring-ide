package org.springframework.ide.eclipse.beans.ui.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;

public class BeansEditorUtils {

    public static List getBeansFromConfigSets(IFile file) {
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
                    IResource resource = beansConfig.getElementResource();
                    if (!configsMap.containsKey(resource.getName()) && !resource.getFullPath().equals(file.getFullPath())
                           ) {
                        configsMap.put(resource.getName(), beansConfig);
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
}
