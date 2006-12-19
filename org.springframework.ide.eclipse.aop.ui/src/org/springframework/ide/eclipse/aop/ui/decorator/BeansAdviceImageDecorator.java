/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.ide.eclipse.aop.ui.decorator;

import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.aop.core.model.IAdviceChangedListener;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.internal.AnnotationAspectDefinition;
import org.springframework.ide.eclipse.aop.ui.BeansAopPlugin;
import org.springframework.ide.eclipse.aop.ui.navigator.model.AdviceRootAopReference;
import org.springframework.ide.eclipse.aop.ui.navigator.model.AdvisedAopSource;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

@SuppressWarnings("restriction")
public class BeansAdviceImageDecorator implements ILightweightLabelDecorator {

    public static final String DECORATOR_ID = BeansAopPlugin.PLUGIN_ID
            + ".decorator.adviceimagedecorator";

    private ListenerList fListeners;

    private IAdviceChangedListener fAdviceChangedListener;

    public void decorate(Object element, IDecoration decoration) {
        // add the orange triangle to the icon if this method,
        // class or aspect is advised
        if ((element instanceof IMethod || element instanceof SourceType)) {
            IJavaElement je = (IJavaElement) element;
            IJavaProject jp = je.getJavaProject();
            // only query the model if the element is in an Spring project

            if ((jp != null)
                    && SpringCoreUtils.isSpringProject(jp.getProject())) {
                if (BeansAopPlugin.getModel().isAdvised(je)) {
                    decoration.addOverlay(BeansAopUIImages.DESC_OVR_ADVICE,
                            IDecoration.TOP_LEFT);
                }
                /*else if (BeansAopPlugin.getModel().isAdvice(je)) {
                    decoration.addOverlay(BeansAopUIImages.DESC_OVR_SPRING,
                            IDecoration.TOP_LEFT);
                }*/
            }
        }
        else if (element instanceof AdviceRootAopReference) {
            List<IAopReference> references = ((AdviceRootAopReference) element)
                    .getReference();
            for (IAopReference reference : references) {
                if (reference.getDefinition() instanceof AnnotationAspectDefinition) {
                    decoration.addOverlay(BeansAopUIImages.DESC_OVR_ANNOTATION,
                            IDecoration.TOP_LEFT);
                    break;
                }
            }
        }
        else if (element instanceof AdvisedAopSource) {
            IAopReference reference = ((AdvisedAopSource) element)
                    .getReference();
            if (reference.getDefinition() instanceof AnnotationAspectDefinition) {
                decoration.addOverlay(BeansAopUIImages.DESC_OVR_ANNOTATION,
                        IDecoration.TOP_LEFT);
            }
        }
    }

    public void addListener(ILabelProviderListener listener) {
        if (fListeners == null) {
            fListeners = new ListenerList();
        }
        fListeners.add(listener);
        if (fAdviceChangedListener == null) {
            fAdviceChangedListener = new IAdviceChangedListener() {
                public void adviceChanged() {
                    fireAdviceChanged();
                }
            };
            BeansAopPlugin.getModel().registerAdivceListener(
                    fAdviceChangedListener);
        }
    }

    private void fireAdviceChanged() {
        if (fListeners != null && !fListeners.isEmpty()) {
            LabelProviderChangedEvent event = new LabelProviderChangedEvent(
                    this);
            Object[] listeners = fListeners.getListeners();
            for (int i = 0; i < listeners.length; i++) {
                ((ILabelProviderListener) listeners[i])
                        .labelProviderChanged(event);
            }
        }
    }

    public void dispose() {
        if (fAdviceChangedListener != null) {
            BeansAopPlugin.getModel().unregisterAdivceListener(
                    fAdviceChangedListener);
            fAdviceChangedListener = null;
        }
    }

    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    public void removeListener(ILabelProviderListener listener) {
        if (fListeners != null) {
            fListeners.remove(listener);
            if (fListeners.isEmpty() && fAdviceChangedListener != null) {
                BeansAopPlugin.getModel().unregisterAdivceListener(
                        fAdviceChangedListener);
                fAdviceChangedListener = null;
            }
        }
    }

    public static final void update() {
        SpringUIUtils.getStandardDisplay().asyncExec(new Runnable() {
            public void run() {
                IWorkbench workbench = PlatformUI.getWorkbench();
                workbench.getDecoratorManager().update(DECORATOR_ID);
            }
        });
    }

}
