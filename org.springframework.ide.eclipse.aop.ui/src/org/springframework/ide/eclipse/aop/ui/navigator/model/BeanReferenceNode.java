/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.ide.eclipse.aop.ui.navigator.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IType;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.aop.ui.navigator.util.BeansAopNavigatorUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

public class BeanReferenceNode implements IReferenceNode,
        IRevealableReferenceNode {

    private IBean bean;

    private boolean showChildren = true;

    public BeanReferenceNode(IBean bean) {
        this(bean, true);
    }

    public BeanReferenceNode(IBean bean, boolean showChildren) {
        this.bean = bean;
        this.showChildren = showChildren;
    }

    public int getLineNumber() {
        return this.bean.getElementStartLine();
    }

    public IResource getResource() {
        return this.bean.getElementResource();
    }

    public void openAndReveal() {
        IResource resource = this.bean.getElementResource();
        SpringUIUtils.openInEditor((IFile) resource, this.bean
                .getElementStartLine());
    }

    public IReferenceNode[] getChildren() {
        if (showChildren) {
            IType type = BeansModelUtils.getJavaType(
                    getResource().getProject(), bean.getClassName());
            if (type != null) {
                return new IReferenceNode[] { new BeanJavaElementReferenceNode(
                        type) };
            }
        }
        return new IReferenceNode[0];
    }

    public Image getImage() {
        return BeansAopNavigatorUtils.BEAN_LABEL_PROVIDER.getImage(this.bean);
    }

    public String getText() {
        return BeansAopNavigatorUtils.BEAN_LABEL_PROVIDER.getText(this.bean)
                + " - "
                + this.bean.getElementResource().getProjectRelativePath()
                        .toString();
    }

    public boolean hasChildren() {
        return getChildren() != null && getChildren().length > 0;
    }

    public IBean getBean() {
        return bean;
    }

}
