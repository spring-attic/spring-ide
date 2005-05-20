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

package org.springframework.ide.eclipse.web.flow.ui.model;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.web.flow.ui.WebFlowUIImages;

public class ModelLabelProvider extends LabelProvider {

    public Image getImage(Object obj) {
        if (obj instanceof ProjectNode) {
            return WebFlowUIImages.getImage(WebFlowUIImages.IMG_OBJS_PROJECT);
        }
        else if (obj instanceof ConfigSetNode) {
            return WebFlowUIImages
                    .getImage(WebFlowUIImages.IMG_OBJS_SPRING_WEBFLOW);
        }
        else if (obj instanceof ConfigNode) {
            return WebFlowUIImages.getImage(WebFlowUIImages.IMG_OBJS_CONFIG);
        }
        else if (obj instanceof IBeansConfigSet) {
            return WebFlowUIImages.getImage(WebFlowUIImages.IMG_OBJS_SPRING_BEANS);
        }
        else {
            return WebFlowUIImages
                    .getImage(WebFlowUIImages.IMG_OBJS_CONFIG_SET);
        }
    }

    public String getText(Object element) {
        if (element instanceof ConfigSetNode) {
            StringBuffer label = new StringBuffer();
            label.append(((ConfigSetNode) element).getName());
            label.append(" [WebFlow Config Set]");
            return label.toString();
        }
        if (element instanceof INode) {
            StringBuffer label = new StringBuffer();
            label.append(((INode) element).getName());
            return label.toString();
        }
        else if (element instanceof IBeansConfigSet) {
            StringBuffer label = new StringBuffer();
            label.append(((IBeansConfigSet) element).getElementName());
            label.append(" [Beans Config Set]");
            return label.toString();
        }
        return super.getText(element);
    }
}
