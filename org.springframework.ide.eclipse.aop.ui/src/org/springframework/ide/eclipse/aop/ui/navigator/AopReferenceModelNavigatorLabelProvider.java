/*
 * Copyright 2002-2007 the original author or authors.
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
package org.springframework.ide.eclipse.aop.ui.navigator;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;
import org.springframework.ide.eclipse.aop.core.util.AopReferenceModelUtils;
import org.springframework.ide.eclipse.aop.ui.navigator.model.IReferenceNode;
import org.springframework.ide.eclipse.aop.ui.navigator.model.JavaElementReferenceNode;
import org.springframework.ide.eclipse.aop.ui.navigator.util.AopReferenceModelNavigatorUtils;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelProvider;

/**
 */
public class AopReferenceModelNavigatorLabelProvider
        extends BeansModelLabelProvider implements ICommonLabelProvider {

    public String getDescription(Object element) {
        // TODO add descrption here
        return null;
    }

    public Image getImage(Object element) {
        if (element instanceof IReferenceNode) {
            return ((IReferenceNode) element).getImage();
        }
        else if (element instanceof IJavaElement) {
            return AopReferenceModelNavigatorUtils.JAVA_LABEL_PROVIDER.getImage(element);
        }
        else if (element instanceof JavaElementReferenceNode) {
            return AopReferenceModelNavigatorUtils.JAVA_LABEL_PROVIDER
                    .getImage(((JavaElementReferenceNode) element)
                            .getJavaElement());
        }
        return super.getImage(element);
    }

    public String getText(Object element) {
        if (element instanceof IReferenceNode) {
            return ((IReferenceNode) element).getText();
        }
        else if (element instanceof IJavaElement) {
            return AopReferenceModelNavigatorUtils.JAVA_LABEL_PROVIDER.getText(element)
                    + " - "
                    + AopReferenceModelUtils.getPackageLinkName((IJavaElement) element);
        }
        else if (element instanceof JavaElementReferenceNode) {
            return AopReferenceModelNavigatorUtils.JAVA_LABEL_PROVIDER
                    .getText(((JavaElementReferenceNode) element)
                            .getJavaElement())
                    + " - "
                    + AopReferenceModelUtils
                            .getPackageLinkName(((JavaElementReferenceNode) element)
                                    .getJavaElement());
        }
        return super.getText(element);
    }

    public void init(ICommonContentExtensionSite config) {
    }

    public void restoreState(IMemento memento) {
    }

    public void saveState(IMemento memento) {
    }
}
