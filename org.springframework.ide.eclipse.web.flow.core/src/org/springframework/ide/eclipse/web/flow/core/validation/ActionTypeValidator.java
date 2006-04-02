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

package org.springframework.ide.eclipse.web.flow.core.validation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.web.flow.core.IWebFlowProjectMarker;
import org.springframework.ide.eclipse.web.flow.core.internal.model.WebFlowModelUtils;
import org.springframework.ide.eclipse.web.flow.core.model.IAction;
import org.springframework.ide.eclipse.web.flow.core.model.IActionState;
import org.springframework.ide.eclipse.web.flow.core.model.IBeanReference;
import org.springframework.ide.eclipse.web.flow.core.model.IState;
import org.springframework.ide.eclipse.web.flow.core.model.IStateTransition;
import org.springframework.ide.eclipse.web.flow.core.model.ITransition;
import org.springframework.ide.eclipse.web.flow.core.model.ITransitionableFrom;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfig;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfigSet;

public class ActionTypeValidator implements IWebFlowConfigValidator {

    private static final String ACTION_INTERFACE = "org.springframework.webflow.Action";

    private static final String EVENT_CLASS = "org.springframework.webflow.Event";

    private static final String REQUEST_CONTEXT_INTERFACE = "org.springframework.webflow.RequestContext";

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.validation.IWebFlowConfigValidator#validate(org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfig,
     *      org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfigSet,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public void validate(IWebFlowConfig config, IWebFlowConfigSet configSet,
            IProgressMonitor monitor) {

        if (configSet != null) {
            List states = config.getState().getStates();

            if (states != null && states.size() > 0) {
                for (int i = 0; i < states.size(); i++) {
                    IState state = (IState) states.get(i);

                    if (state instanceof IActionState) {
                        List actions = ((IActionState) state).getActions();
                        this.validateActions(actions, config, configSet);
                    } 

                    if (state instanceof ITransitionableFrom) {
                        List transitions = ((ITransitionableFrom) state)
                                .getOutputTransitions();
                        for (int j = 0; j < transitions.size(); j++) {
                            ITransition transition = (ITransition) transitions
                                    .get(j);
                            if (transition instanceof IStateTransition
                                    && ((IStateTransition) transition)
                                            .getActions() != null
                                    && ((IStateTransition) transition)
                                            .getActions().size() > 0) {
                                this.validateActions(
                                        ((IStateTransition) transition)
                                                .getActions(), config,
                                        configSet);
                            }
                        }
                    }
                }
            }
        }
    }

    private void validateActions(List actions, IWebFlowConfig config,
            IWebFlowConfigSet configSet) {
        for (int j = 0; j < actions.size(); j++) {
            IAction action = (IAction) actions.get(j);
            this.validateAction((IBeanReference) action, config, configSet);
        }
    }

    private void validateAction(IBeanReference reference,
            IWebFlowConfig config, IWebFlowConfigSet configSet) {
        if (reference.hasBeanReference()) {
            boolean valid = true;

            // check if all fields are filled correctly
            String bean = reference.getBean();
            String method = reference.getMethod();

            if (configSet.getBeansConfigSet() != null) {

                IBeansConfigSet beansConfigSet = configSet.getBeansConfigSet();
                String className = null;

                if (bean != null) {
                    IBean b = beansConfigSet.getBean(bean);
                    if (b != null) {
                        className = b.getClassName();
                    }
                }
                if (className != null) {
                    this.validateActionClass(reference, className, config,
                            configSet);
                }

                if (method != null) {
                    this.validateActionMethod(reference, className, method,
                            config, configSet);
                }
            }
        }
    }

    private void validateActionClass(IBeanReference reference,
            String className, IWebFlowConfig config, IWebFlowConfigSet configSet) {
        IType type = BeansModelUtils.getJavaType(config.getConfigFile()
                .getProject(), className);
        if (type != null) {

            List interfaces = new ArrayList();
            this.getAllInterfaces(type, interfaces);
            if (!interfaces.contains(ACTION_INTERFACE)) {
                WebFlowModelUtils.createProblemMarker(config,
                        "Referenced Bean or class '" + className
                                + "' does not implement Action Interface",
                        IMarker.SEVERITY_ERROR,
                        reference.getElementStartLine(),
                        IWebFlowProjectMarker.ERROR_CODE_PARSING_FAILED);
            }
        }
    }

    private void validateActionMethod(IBeanReference reference,
            String className, String methodName, IWebFlowConfig config,
            IWebFlowConfigSet configSet) {
        IType type = BeansModelUtils.getJavaType(config.getConfigFile()
                .getProject(), className);
        if (type != null) {
            try {
                IMethod method = this.findMethod(type, methodName, 1, true);
                if (method == null) {
                    WebFlowModelUtils.createProblemMarker(config,
                            "Referenced method '" + methodName
                                    + "' in bean or class '" + className
                                    + "' does not exist",
                            IMarker.SEVERITY_ERROR, reference
                                    .getElementStartLine(),
                            IWebFlowProjectMarker.ERROR_CODE_PARSING_FAILED);
                } else {
                    String returnTypeString = Signature.toString(
                            method.getReturnType()).replace('$', '.');
                    String parameterTypeString = Signature.toString(
                            method.getParameterTypes()[0]).replace('$', '.');
                    IType returnType = BeansModelUtils.getJavaType(config
                            .getConfigFile().getProject(), resolveClassName(
                            returnTypeString, type));
                    IType parameterType = BeansModelUtils.getJavaType(config
                            .getConfigFile().getProject(), resolveClassName(
                            parameterTypeString, type));
                    if (returnType != null && parameterType != null) {
                        List interfaces = new ArrayList();
                        this.getAllInterfaces(parameterType, interfaces);
                        List superClasses = new ArrayList();
                        this.getAllSuperTypes(returnType, superClasses);
                        if ((!interfaces.contains(REQUEST_CONTEXT_INTERFACE) && !REQUEST_CONTEXT_INTERFACE
                                .equals(parameterType.getFullyQualifiedName()))
                                || (!superClasses.contains(EVENT_CLASS) && (!EVENT_CLASS
                                        .equals(returnType
                                                .getFullyQualifiedName())))) {
                            WebFlowModelUtils
                                    .createProblemMarker(
                                            config,
                                            "Referenced method '"
                                                    + methodName
                                                    + "' in bean or class '"
                                                    + className
                                                    + "' does not have correct signature",
                                            IMarker.SEVERITY_ERROR,
                                            reference.getElementStartLine(),
                                            IWebFlowProjectMarker.ERROR_CODE_PARSING_FAILED);
                        }

                    }

                }
            } catch (JavaModelException e) {
                // do nothing
            }
        }
    }

    private void getAllInterfaces(IType type, List interfaces) {
        if (type != null) {

            try {
                if (type.getSuperInterfaceNames().length > 0) {
                    String[] inter = type.getSuperInterfaceNames();
                    for (int i = 0; i < inter.length; i++) {
                        interfaces.add(this.resolveClassName(inter[i], type));
                    }
                }
                IType supertype = this.getSuperType(type);
                if (supertype != null) {
                    this.getAllInterfaces(supertype, interfaces);
                }
            } catch (JavaModelException e) {

            }
        }
    }

    private void getAllSuperTypes(IType type, List interfaces) {
        if (type != null) {

            try {
                if (type.getSuperclassName() != null) {
                    String superClass = type.getSuperclassName();
                    interfaces.add(this.resolveClassName(superClass, type));

                }
                IType supertype = this.getSuperType(type);
                if (supertype != null) {
                    this.getAllSuperTypes(supertype, interfaces);
                }
            } catch (JavaModelException e) {

            }
        }
    }

    /**
     * Returns super type of given type.
     */
    protected IType getSuperType(IType type) throws JavaModelException {
        String name = type.getSuperclassName();
        if (name != null) {
            if (type.isBinary()) {
                return type.getJavaProject().findType(name);
            } else {
                String[][] resolvedNames = type.resolveType(name);
                if (resolvedNames != null && resolvedNames.length > 0) {
                    String resolvedName = concatenate(resolvedNames[0][0],
                            resolvedNames[0][1], ".");
                    return type.getJavaProject().findType(resolvedName);
                }
            }
        }
        return null;
    }

    /**
     * Returns concatenated text from given two texts delimited by given
     * delimiter. Both texts can be empty or <code>null</code>.
     */
    protected String concatenate(String text1, String text2, String delimiter) {
        StringBuffer buf = new StringBuffer();
        if (text1 != null && text1.length() > 0) {
            buf.append(text1);
        }
        if (text2 != null && text2.length() > 0) {
            if (buf.length() > 0) {
                buf.append(delimiter);
            }
            buf.append(text2);
        }
        return buf.toString();
    }

    /**
     * Finds a target methodName with specific number of arguments on the type
     * hierarchy of given type.
     * 
     * @param type
     *            The Java type object on which to retrieve the method
     * @param methodName
     *            Name of the method
     * @param argCount
     *            Number of arguments for the desired method
     * @param isPublic
     *            true if public method is requested
     * @param isStatic
     *            true if static method is requested
     */
    public IMethod findMethod(IType type, String methodName, int argCount,
            boolean isPublic) throws JavaModelException {
        while (type != null) {
            IMethod[] methods = type.getMethods();
            for (int i = 0; i < methods.length; i++) {
                IMethod method = methods[i];
                int flags = method.getFlags();
                if (Flags.isPublic(flags) == isPublic
                        && (argCount == -1 || method.getNumberOfParameters() == argCount)
                        && methodName.equals(method.getElementName())) {
                    return method;
                }
            }
            type = getSuperType(type);
        }
        return null;
    }

    private String resolveClassName(String className, IType type) {
        try {
            String[][] fullInter = type.resolveType(className);
            if (fullInter != null && fullInter.length > 0) {
                return fullInter[0][0] + "." + fullInter[0][1];
            }
        } catch (JavaModelException e) {
        }

        return className;
    }
}
