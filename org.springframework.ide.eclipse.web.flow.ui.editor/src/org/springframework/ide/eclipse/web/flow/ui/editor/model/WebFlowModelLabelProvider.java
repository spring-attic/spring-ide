/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.ide.eclipse.web.flow.ui.editor.model;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.web.flow.core.model.IAction;
import org.springframework.ide.eclipse.web.flow.core.model.IActionState;
import org.springframework.ide.eclipse.web.flow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.web.flow.core.model.IBeanReference;
import org.springframework.ide.eclipse.web.flow.core.model.IDecisionState;
import org.springframework.ide.eclipse.web.flow.core.model.IEndState;
import org.springframework.ide.eclipse.web.flow.core.model.IIf;
import org.springframework.ide.eclipse.web.flow.core.model.IProperty;
import org.springframework.ide.eclipse.web.flow.core.model.IState;
import org.springframework.ide.eclipse.web.flow.core.model.ISubFlowState;
import org.springframework.ide.eclipse.web.flow.core.model.IViewState;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowImages;

public class WebFlowModelLabelProvider extends LabelProvider {

    private boolean longDescription = false;

    public Image getImage(Object obj) {
        if (obj instanceof IActionState) {
            return WebFlowImages.getImage(WebFlowImages.IMG_OBJS_ACTION_STATE);
        } else if (obj instanceof IViewState) {
            return WebFlowImages.getImage(WebFlowImages.IMG_OBJS_VIEW_STATE);
        } else if (obj instanceof IEndState) {
            return WebFlowImages.getImage(WebFlowImages.IMG_OBJS_END_STATE);
        } else if (obj instanceof ISubFlowState) {
            return WebFlowImages.getImage(WebFlowImages.IMG_OBJS_SUBFLOW_STATE);
        } else if (obj instanceof IAction) {
            return WebFlowImages.getImage(WebFlowImages.IMG_OBJS_ACTION);
        } else if (obj instanceof IAttributeMapper) {
            return WebFlowImages
                    .getImage(WebFlowImages.IMG_OBJS_ATTRIBUTE_MAPPER);
        } else if (obj instanceof IDecisionState) {
            return WebFlowImages
                    .getImage(WebFlowImages.IMG_OBJS_DECISION_STATE);
        } else if (obj instanceof IIf) {
            return WebFlowImages.getImage(WebFlowImages.IMG_OBJS_IF);
        } else if (obj instanceof IProperty) {
            return WebFlowImages.getImage(WebFlowImages.IMG_OBJS_PROPERTIES);
        } else if (obj instanceof IBean) {
            return WebFlowImages.getImage(WebFlowImages.IMG_OBJS_JAVABEAN);
        }
        return null;
    }

    public String getText(Object element) {
        return this.getText(element, false, true);
    }

    public String getText(Object element, boolean showElementType,
            boolean showBean) {
        StringBuffer buf = new StringBuffer();
        if (element instanceof IState) {
            buf.append(((IState) element).getId());
        } else if (element instanceof IAction) {
            IAction action = (IAction) element;
            if (action.getBean() != null) {
                buf.append("Bean: ");
                buf.append(action.getBean());
            } else if (action.getBeanClass() != null) {
                buf.append("Class: ");
                buf.append(action.getBeanClass().substring(
                        action.getBeanClass().lastIndexOf(".") + 1,
                        action.getBeanClass().length()));
            } else if (action.getClassRef() != null) {
                buf.append("ClassRef: ");
                buf.append(action.getClassRef().substring(
                        action.getClassRef().lastIndexOf(".") + 1,
                        action.getClassRef().length()));
            }
            if (action.getMethod() != null) {
                buf.append(".");
                buf.append(action.getMethod());
                buf.append("()");
            }
        } else if (element instanceof IAttributeMapper) {
            IAttributeMapper attributeMapper = (IAttributeMapper) element;
            if (attributeMapper.getBean() != null) {
                buf.append(attributeMapper.getBean());
            } else if (attributeMapper.getBeanClass() != null) {
                buf.append(attributeMapper.getBeanClass().substring(
                        attributeMapper.getBeanClass().lastIndexOf(".") + 1,
                        attributeMapper.getBeanClass().length()));
            } else if (attributeMapper.getClassRef() != null) {
                buf.append(attributeMapper.getClassRef().substring(
                        attributeMapper.getClassRef().lastIndexOf(".") + 1,
                        attributeMapper.getClassRef().length()));
            }
        } else if (element instanceof IProperty) {
            IProperty property = (IProperty) element;
            buf.append(property.getName());
            buf.append("=");
            buf.append(property.getValue());
        } else if (element instanceof IIf) {
            IIf theIf = (IIf) element;
            //int index = ((IDecisionState)
            // theIf.getElementParent()).getIfs().indexOf(theIf) + 1;
            //buf.append(index);
            //buf.append(": ");
            buf.append(theIf.getTest());
        } else if (element instanceof IBean) {
            IBean bean = (IBean) element;
            buf.append(bean.getElementName());
            if (bean.getClassName() != null) {
                buf.append(" [");
                buf.append(bean.getClassName());
                buf.append(']');
            }
        } else {
            buf.append(super.getText(element));
        }

        if (showBean
                && (element instanceof IBeanReference && !(element instanceof IAction))) {
            IBeanReference action = (IBeanReference) element;
            if (action.getBean() != null || action.getBeanClass() != null
                    || action.getClassRef() != null) {
                buf.append("\n");

                if (action.getBean() != null) {
                    buf.append("Bean: ");
                    buf.append(action.getBean());
                } else if (action.getBeanClass() != null) {
                    buf.append("Class: ");
                    buf.append(action.getBeanClass().substring(
                            action.getBeanClass().lastIndexOf(".") + 1,
                            action.getBeanClass().length()));
                } else if (action.getClassRef() != null) {
                    buf.append("ClassRef: ");
                    buf.append(action.getClassRef().substring(
                            action.getClassRef().lastIndexOf(".") + 1,
                            action.getClassRef().length()));
                }
                if (action.getMethod() != null) {
                    buf.append(".");
                    buf.append(action.getMethod());
                    buf.append("()");
                }
            }
        }

        if (showElementType) {
            buf.append(" [");
            if (element instanceof IEndState) {
                buf.append("End State");
            } else if (element instanceof IViewState) {
                buf.append("View State");
            } else if (element instanceof ISubFlowState) {
                buf.append("Sub Flow State");
            } else if (element instanceof IActionState) {
                buf.append("Action State");
            } else if (element instanceof IAction) {
                buf.append("Action");
            } else if (element instanceof IAttributeMapper) {
                buf.append("Attribute Mapper");
            } else if (element instanceof IProperty) {
                buf.append("Property");
            } else if (element instanceof IIf) {
                buf.append("If");
            } else if (element instanceof IDecisionState) {
                buf.append("Decision State");
            }
            buf.append("]");
        }

        return buf.toString();
    }

    public String getLongText(Object element) {
        StringBuffer buf = new StringBuffer();
        if (element instanceof IState) {
            buf.append(((IState) element).getId());
        } else if (element instanceof IAction) {
            IAction action = (IAction) element;
            if (action.getBean() != null) {
                buf.append(action.getBean());
            } else if (action.getBeanClass() != null) {
                buf.append(action.getBeanClass().substring(
                        action.getBeanClass().lastIndexOf(".") + 1,
                        action.getBeanClass().length()));
            } else if (action.getClassRef() != null) {
                buf.append(action.getClassRef().substring(
                        action.getClassRef().lastIndexOf(".") + 1,
                        action.getClassRef().length()));
            }
        } else if (element instanceof IAttributeMapper) {
            IAttributeMapper attributeMapper = (IAttributeMapper) element;
            if (attributeMapper.getBean() != null) {
                buf.append(attributeMapper.getBean());
            } else if (attributeMapper.getBeanClass() != null) {
                buf.append(attributeMapper.getBeanClass().substring(
                        attributeMapper.getBeanClass().lastIndexOf(".") + 1,
                        attributeMapper.getBeanClass().length()));
            } else if (attributeMapper.getClassRef() != null) {
                buf.append(attributeMapper.getClassRef().substring(
                        attributeMapper.getClassRef().lastIndexOf(".") + 1,
                        attributeMapper.getClassRef().length()));
            }
        } else {
            buf.append(super.getText(element));
        }

        return buf.toString();
    }
}