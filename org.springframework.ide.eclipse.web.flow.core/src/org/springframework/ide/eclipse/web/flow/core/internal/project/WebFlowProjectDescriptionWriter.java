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

package org.springframework.ide.eclipse.web.flow.core.internal.project;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.springframework.ide.eclipse.web.flow.core.WebFlowCorePlugin;
import org.springframework.ide.eclipse.web.flow.core.internal.XmlWriter;
import org.springframework.ide.eclipse.web.flow.core.internal.model.WebFlowConfigSet;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfigSet;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowProject;

public class WebFlowProjectDescriptionWriter implements
        IWebFlowProjectDescriptionConstants {

    public static final String DEBUG_OPTION = WebFlowCorePlugin.PLUGIN_ID
            + "/project/description/debug";

    public static boolean DEBUG = WebFlowCorePlugin.isDebug(DEBUG_OPTION);

    public static void write(IProject project,
            WebFlowProjectDescription description) {
        IFile file = project
                .getFile(new Path(IWebFlowProject.DESCRIPTION_FILE));
        if (DEBUG) {
            System.out.println("Writing project description to "
                    + file.getLocation().toString());
        }
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                XmlWriter writer = new XmlWriter(os);
                write(description, writer);
                writer.flush();
                writer.close();
            }
            finally {
                os.close();
            }
            if (!file.exists()) {
                file.create(new ByteArrayInputStream(os.toByteArray()),
                        IResource.NONE, null);
            }
            else {
                file.setContents(new ByteArrayInputStream(os.toByteArray()),
                        IResource.NONE, null);
            }
        }
        catch (IOException e) {
            WebFlowCorePlugin.log("Error writing " + file.getFullPath(), e);
        }
        catch (CoreException e) {
            WebFlowCorePlugin.log(e.getStatus());
        }
    }

    protected static void write(WebFlowProjectDescription description,
            XmlWriter writer) throws IOException {
        writer.startTag(PROJECT_DESCRIPTION, null);
        write(CONFIGS, CONFIG, description.getConfigNames(), writer);
        write(CONFIG_SETS, description.getConfigSets(), writer);
        writer.endTag(PROJECT_DESCRIPTION);
    }

    protected static void write(IWebFlowConfigSet configSet, XmlWriter writer)
            throws IOException {
        writer.startTag(CONFIG_SET, null);
        writer.printSimpleTag(NAME, configSet.getElementName());
        if (configSet.getBeansConfigSet() != null)
            writer.printSimpleTag(BEANS_CONFIG_NAME, configSet
                    .getBeansConfigSet().getElementName());
        write(CONFIGS, CONFIG, configSet.getConfigs(), writer);
        writer.endTag(CONFIG_SET);
    }

    protected static void write(String name, Collection collection,
            XmlWriter writer) throws IOException {
        writer.startTag(name, null);
        for (Iterator iter = collection.iterator(); iter.hasNext();) {
            write(iter.next(), writer);
        }
        writer.endTag(name);
    }

    protected static void write(Object obj, XmlWriter writer)
            throws IOException {
        if (obj instanceof IWebFlowConfigSet) {
            write((WebFlowConfigSet) obj, writer);
        }
    }

    protected static void write(String name, String elementTagName,
            String[] array, XmlWriter writer) throws IOException {
        writer.startTag(name, null);
        for (int i = 0; i < array.length; i++) {
            writer.printSimpleTag(elementTagName, array[i]);
        }
        writer.endTag(name);
    }

    protected static void write(String name, String elementTagName,
            Collection collection, XmlWriter writer) throws IOException {
        writer.startTag(name, null);
        for (Iterator iter = collection.iterator(); iter.hasNext();) {
            writer.printSimpleTag(elementTagName, iter.next());
        }
        writer.endTag(name);
    }
}