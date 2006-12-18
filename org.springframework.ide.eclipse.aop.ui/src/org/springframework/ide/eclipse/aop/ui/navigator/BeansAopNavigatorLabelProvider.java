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
package org.springframework.ide.eclipse.aop.ui.navigator;

import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;
import org.springframework.ide.eclipse.aop.ui.BeansAopPlugin;
import org.springframework.ide.eclipse.aop.ui.BeansAopUtils;
import org.springframework.ide.eclipse.aop.ui.navigator.BeansAopNavigatorContentProvider.AopReference;
import org.springframework.ide.eclipse.aop.ui.navigator.util.JavaElementWrapper;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.AopUIImages;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelProvider;

/**
 */
public class BeansAopNavigatorLabelProvider
        extends BeansModelLabelProvider implements ICommonLabelProvider {

    public ILabelProvider labelProvider;

    public BeansAopNavigatorLabelProvider() {
        labelProvider = new DecoratingLabelProvider(
                new JavaElementLabelProvider(
                        JavaElementLabelProvider.SHOW_DEFAULT
                                | JavaElementLabelProvider.SHOW_SMALL_ICONS),
                BeansAopPlugin.getDefault().getWorkbench()
                        .getDecoratorManager().getLabelDecorator());
    }

    public String getDescription(Object element) {
        // TODO add descrption here
        return null;
    }

    public Image getImage(Object element) {
        if (element instanceof AopReference) {
            return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_REFERENCE);
        }
        else if (element instanceof IAopReference) {
            IAopReference ref = (IAopReference) element;
            ADVICE_TYPES type = ref.getAdviceType();
            if (type == ADVICE_TYPES.AFTER
                    || type == ADVICE_TYPES.AFTER_RETURNING
                    || type == ADVICE_TYPES.AFTER_THROWING) {
                return AopUIImages.getImage(AopUIImages.IMG_OBJS_AFTER_ADVICE);
            }
            else if (type == ADVICE_TYPES.BEFORE) {
                return AopUIImages.getImage(AopUIImages.IMG_OBJS_BEFORE_ADVICE);
            }
            else if (type == ADVICE_TYPES.AROUND) {
                return AopUIImages.getImage(AopUIImages.IMG_OBJS_ASPECT);
            }
        }
        else if (element instanceof JavaElementWrapper) {
            return labelProvider.getImage(((JavaElementWrapper) element)
                    .getJavaElement());
        }
        return super.getImage(element);
    }

    public String getText(Object element) {
        if (element instanceof AopReference) {
            return ((AopReference) element).getElementName();
        }
        else if (element instanceof IAopReference) {
            IAopReference ref = (IAopReference) element;
            ADVICE_TYPES type = ref.getAdviceType();
            String text = "";
            if (type == ADVICE_TYPES.AFTER) {
                text += "after()";
            }
            else if (type == ADVICE_TYPES.AFTER_RETURNING) {
                text += "after-returning()";
            }
            else if (type == ADVICE_TYPES.AFTER_THROWING) {
                text += "after-throwing()";
            }
            else if (type == ADVICE_TYPES.BEFORE) {
                text += "before()";
            }
            else if (type == ADVICE_TYPES.AROUND) {
                text += "around()";
            }
            text += " <";
            text += ref.getDefinition().getAspectName();
            text += "> [";
            text += ref.getResource().getProjectRelativePath().toString();
            text += "]";
            return text;
        }
        else if (element instanceof JavaElementWrapper) {
            JavaElementWrapper wrapper = (JavaElementWrapper) element;
            if (wrapper.isRoot()) {
                return super.getText(wrapper.getJavaElement());
            }
            else {
                return BeansAopUtils
                        .getJavaElementLinkName(((JavaElementWrapper) element)
                                .getJavaElement());
            }
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
