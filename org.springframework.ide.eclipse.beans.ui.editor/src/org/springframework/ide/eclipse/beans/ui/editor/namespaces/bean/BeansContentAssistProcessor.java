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

package org.springframework.ide.eclipse.beans.ui.editor.namespaces.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.core.internal.Introspector;
import org.springframework.ide.eclipse.beans.core.internal.Introspector.Statics;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.AbstractContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.requestor.BeanReferenceSearchRequestor;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.requestor.FactoryMethodSearchRequestor;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.requestor.PropertyNameSearchRequestor;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.requestor.PropertyValueSearchRequestor;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.requestor.VoidMethodSearchRequestor;
import org.springframework.ide.eclipse.beans.ui.editor.templates.BeansTemplateContextTypeIds;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansJavaCompletionUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Main entry point for the Spring beans xml editor's content assist.
 * 
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 */
@SuppressWarnings( { "restriction", "unchecked" })
public class BeansContentAssistProcessor
        extends AbstractContentAssistProcessor {

    private void addBeanReferenceProposals(ContentAssistRequest request, String prefix, Node node,
            boolean showExternal) {
        if (prefix == null) {
            prefix = "";
        }

        IFile file = (IFile) BeansEditorUtils.getResource(request);
        if (node.getOwnerDocument() != null) {
            BeanReferenceSearchRequestor requestor = new BeanReferenceSearchRequestor(request,
                    BeansJavaCompletionUtils.getPropertyTypes(node, file.getProject()));
            Map<String, Node> beanNodes = BeansEditorUtils.getReferenceableNodes(node
                    .getOwnerDocument());
            for (Map.Entry<String, Node> n : beanNodes.entrySet()) {
                Node beanNode = n.getValue();
                requestor.acceptSearchMatch(n.getKey(), beanNode, file, prefix);
            }
            if (showExternal) {
                List<?> beans = BeansEditorUtils.getBeansFromConfigSets(file);
                for (int i = 0; i < beans.size(); i++) {
                    IBean bean = (IBean) beans.get(i);
                    requestor.acceptSearchMatch(bean, file, prefix);
                }
            }
        }
    }

    private void addClassAttributeValueProposals(ContentAssistRequest request, String prefix) {
        BeansJavaCompletionUtils.addClassValueProposals(request, prefix);
    }

    private void addFactoryMethodAttributeValueProposals(ContentAssistRequest request,
            String prefix, String factoryClassName, boolean isStatic) {
        if (BeansEditorUtils.getResource(request) instanceof IFile) {
            IFile file = (IFile) BeansEditorUtils.getResource(request);
            IType type = BeansModelUtils.getJavaType(file.getProject(), factoryClassName);
            if (type != null) {
                try {
                    Collection<?> methods = Introspector.findAllMethods(type, prefix, -1, true,
                            (isStatic ? Statics.YES : Statics.DONT_CARE));
                    if (methods != null && methods.size() > 0) {
                        FactoryMethodSearchRequestor requestor = new FactoryMethodSearchRequestor(
                                request);
                        Iterator<?> iterator = methods.iterator();
                        while (iterator.hasNext()) {
                            requestor.acceptSearchMatch((IMethod) iterator.next());
                        }
                    }
                }
                catch (JavaModelException e1) {
                    // do nothing
                }
                catch (CoreException e) {
                    // // do nothing
                }
            }
        }
    }

    private void addInitDestroyAttributeValueProposals(ContentAssistRequest request, String prefix,
            String className) {
        if (BeansEditorUtils.getResource(request) instanceof IFile) {
            IFile file = (IFile) BeansEditorUtils.getResource(request);
            IType type = BeansModelUtils.getJavaType(file.getProject(), className);
            if (type != null) {
                try {
                    Collection<?> methods = Introspector.findAllNoParameterMethods(type, prefix);
                    if (methods != null && methods.size() > 0) {
                        VoidMethodSearchRequestor requestor = new VoidMethodSearchRequestor(request);
                        Iterator<?> iterator = methods.iterator();
                        while (iterator.hasNext()) {
                            requestor.acceptSearchMatch((IMethod) iterator.next());
                        }
                    }
                }
                catch (JavaModelException e1) {
                    // do nothing
                }
                catch (CoreException e) {
                    // // do nothing
                }
            }
        }
    }

    private void addPropertyNameAttributeValueProposals(ContentAssistRequest request,
            String prefix, String oldPrefix, Node node, List<IType> classNames) {

        PropertyValueSearchRequestor requestor = new PropertyValueSearchRequestor(request,
                oldPrefix);
        if (prefix.lastIndexOf(".") >= 0) {
            int firstIndex = prefix.indexOf(".");
            String firstPrefix = prefix.substring(0, firstIndex);
            String lastPrefix = prefix.substring(firstIndex);
            if (".".equals(lastPrefix)) {
                lastPrefix = "";
            }
            else if (lastPrefix.startsWith(".")) {
                lastPrefix = lastPrefix.substring(1);
            }
            for (int i = 0; i < classNames.size(); i++) {
                IType type = (IType) classNames.get(i);
                try {
                    Collection<?> methods = Introspector.findReadableProperties(type, firstPrefix,
                            true);
                    if (methods != null && methods.size() == 1) {

                        Iterator<?> iterator = methods.iterator();
                        while (iterator.hasNext()) {
                            IMethod method = (IMethod) iterator.next();
                            IType returnType = BeansEditorUtils.getTypeForMethodReturnType(method,
                                    type, (IFile) BeansEditorUtils.getResource(request));
                            if (returnType != null) {
                                List<IType> typesTemp = new ArrayList<IType>();
                                typesTemp.add(returnType);

                                String newPrefix = oldPrefix + firstPrefix + ".";

                                addPropertyNameAttributeValueProposals(request, lastPrefix,
                                        newPrefix, node, typesTemp);
                            }
                            return;
                        }
                    }
                }
                catch (CoreException e) {
                    // // do nothing
                }
            }
        }
        else {
            for (int i = 0; i < classNames.size(); i++) {
                IType type = (IType) classNames.get(i);
                try {
                    Collection<?> methods = Introspector.findWritableProperties(type, prefix, true);
                    if (methods != null && methods.size() > 0) {

                        Iterator<?> iterator = methods.iterator();
                        while (iterator.hasNext()) {
                            requestor.acceptSearchMatch((IMethod) iterator.next(), false);
                        }
                    }
                }
                catch (JavaModelException e1) {
                    // do nothing
                }
                catch (CoreException e) {
                    // // do nothing
                }
            }
        }
    }

    protected void computeAttributeValueProposals(ContentAssistRequest request, IDOMNode node,
            String matchString, String attributeName) {

        if ("bean".equals(node.getNodeName())) {
            if ("class".equals(attributeName)) {
                addClassAttributeValueProposals(request, matchString);
            }
            else if ("init-method".equals(attributeName) || "destroy-method".equals(attributeName)) {
                // TODO add support for parent bean
                String className = BeansEditorUtils.getAttribute(node, "class");
                if (className != null) {
                    addInitDestroyAttributeValueProposals(request, matchString, className);
                }
            }
            else if ("factory-method".equals(attributeName)) {
                NamedNodeMap attributes = node.getAttributes();
                Node factoryBean = attributes.getNamedItem("factory-bean");
                String factoryClassName = null;
                boolean isStatic;
                if (factoryBean != null) {
                    // instance factory method
                    factoryClassName = BeansEditorUtils.getClassNameForBean(
                            (IFile) BeansEditorUtils.getResource(request), node.getOwnerDocument(),
                            factoryBean.getNodeValue());
                    isStatic = false;
                }
                else {
                    // static factory method
                    List<?> list = BeansEditorUtils.getClassNamesOfBean((IFile) BeansEditorUtils
                            .getResource(request), node);
                    factoryClassName = (list.size() != 0 ? ((IType) list.get(0))
                            .getFullyQualifiedName() : null);
                    isStatic = true;
                }

                if (factoryClassName != null) {
                    addFactoryMethodAttributeValueProposals(request, matchString, factoryClassName,
                            isStatic);
                }
            }
            else if ("parent".equals(attributeName) || "depends-on".equals(attributeName)
                    || "factory-bean".equals(attributeName)) {
                addBeanReferenceProposals(request, matchString, node, true);
            }
        }
        else if ("property".equals(node.getNodeName())) {
            Node parentNode = node.getParentNode();
            NamedNodeMap parentAttributes = parentNode.getAttributes();

            if ("name".equals(attributeName) && parentAttributes != null) {
                List classNames = BeansEditorUtils.getClassNamesOfBean((IFile) BeansEditorUtils
                        .getResource(request), parentNode);
                addPropertyNameAttributeValueProposals(request, matchString, "", parentNode,
                        classNames);
            }
            else if ("ref".equals(attributeName)) {
                addBeanReferenceProposals(request, matchString, node, true);
            }
        }
        else if ("ref".equals(node.getNodeName()) || "idref".equals(node.getNodeName())) {
            if ("local".equals(attributeName)) {
                addBeanReferenceProposals(request, matchString, node, false);
            }
            else if ("bean".equals(attributeName)) {
                addBeanReferenceProposals(request, matchString, node, true);
            }
        }
        else if ("constructor-arg".equals(node.getNodeName())) {
            if ("type".equals(attributeName)) {
                addClassAttributeValueProposals(request, matchString);
            }
            else if ("ref".equals(attributeName)) {
                addBeanReferenceProposals(request, matchString, node, true);
            }
        }
        else if ("alias".equals(node.getNodeName())) {
            if ("name".equals(attributeName)) {
                addBeanReferenceProposals(request, matchString, node, true);
            }
        }
        else if ("entry".equals(node.getNodeName())) {
            if ("key-ref".equals(attributeName) || "value-ref".equals(attributeName)) {
                addBeanReferenceProposals(request, matchString, node, true);
            }
        }
        else if ("value".equals(node.getNodeName())) {
            if ("type".equals(attributeName)) {
                addClassAttributeValueProposals(request, matchString);
            }
        }
    }

    @Override
    protected void computeAttributeNameProposals(ContentAssistRequest request, String prefix,
            String namespace, String namespacePrefix, Node attributeNode) {
        if ("http://www.springframework.org/schema/p".equals(namespace)) {
            // check whether an attribute really exists for the replacement
            // offsets AND if it possesses a value
            IStructuredDocumentRegion sdRegion = request.getDocumentRegion();
            boolean attrAtLocationHasValue = false;
            NamedNodeMap attrs = attributeNode.getAttributes();
            for (int i = 0; i < attrs.getLength(); i++) {
                AttrImpl existingAttr = (AttrImpl) attrs.item(i);
                ITextRegion name = existingAttr.getNameRegion();
                if (sdRegion.getStartOffset(name) <= request.getReplacementBeginPosition()
                        && sdRegion.getStartOffset(name) + name.getLength() >= request
                                .getReplacementBeginPosition()
                                + request.getReplacementLength()
                        && existingAttr.getValueRegion() != null) {
                    attrAtLocationHasValue = true;
                    break;
                }
            }

            if (prefix != null) {
                prefix = BeansEditorUtils.attributeNameToPropertyName(prefix);
            }

            List classNames = BeansEditorUtils.getClassNamesOfBean(BeansEditorUtils
                    .getResource(request), attributeNode);
            addPropertyNameAttributeNameProposals(request, prefix, "", attributeNode, classNames,
                    attrAtLocationHasValue, namespacePrefix);
        }
    }

    private void addPropertyNameAttributeNameProposals(ContentAssistRequest request, String prefix,
            String oldPrefix, Node node, List classNames, boolean attrAtLocationHasValue,
            String nameSpacePrefix) {

        PropertyNameSearchRequestor requestor = new PropertyNameSearchRequestor(request, oldPrefix,
                attrAtLocationHasValue, nameSpacePrefix);
        if (prefix.lastIndexOf(".") >= 0) {
            int firstIndex = prefix.indexOf(".");
            String firstPrefix = prefix.substring(0, firstIndex);
            String lastPrefix = prefix.substring(firstIndex);
            if (".".equals(lastPrefix)) {
                lastPrefix = "";
            }
            else if (lastPrefix.startsWith(".")) {
                lastPrefix = lastPrefix.substring(1);
            }
            for (int i = 0; i < classNames.size(); i++) {
                IType type = (IType) classNames.get(i);
                try {
                    Collection methods = Introspector.findReadableProperties(type, firstPrefix);
                    if (methods != null && methods.size() == 1) {

                        Iterator iterator = methods.iterator();
                        while (iterator.hasNext()) {
                            IMethod method = (IMethod) iterator.next();
                            IType returnType = BeansEditorUtils.getTypeForMethodReturnType(method,
                                    type, BeansEditorUtils.getResource(request));

                            if (returnType != null) {
                                List<IType> typesTemp = new ArrayList<IType>();
                                typesTemp.add(returnType);

                                String newPrefix = oldPrefix + firstPrefix + ".";
                                ;

                                addPropertyNameAttributeNameProposals(request, lastPrefix,
                                        newPrefix, node, typesTemp, attrAtLocationHasValue,
                                        nameSpacePrefix);
                            }
                            return;
                        }
                    }
                }
                catch (JavaModelException e1) {
                    // do nothing
                }
            }
        }
        else {
            for (int i = 0; i < classNames.size(); i++) {
                IType type = (IType) classNames.get(i);
                try {
                    Collection methods = Introspector.findWritableProperties(type, prefix);
                    if (methods != null && methods.size() > 0) {

                        Iterator iterator = methods.iterator();
                        while (iterator.hasNext()) {
                            requestor.acceptSearchMatch((IMethod) iterator.next(), false);
                        }
                    }
                }
                catch (JavaModelException e1) {
                    // do nothing
                }
                catch (CoreException e) {
                    // // do nothing
                }
            }
        }
    }

    @Override
    protected void computeTagInsertionProposals(ContentAssistRequest request, IDOMNode node) {
        if (node != null && node.getParentNode() != null) {
            Node parentNode = node.getParentNode();
            if ("bean".equals(parentNode.getNodeName())) {
                addTemplates(request, BeansTemplateContextTypeIds.BEAN);
            }
            else if ("beans".equals(parentNode.getNodeName())) {
                addTemplates(request, BeansTemplateContextTypeIds.ALL);
            }
            else if ("property".equals(parentNode.getNodeName())) {
                addTemplates(request, BeansTemplateContextTypeIds.PROPERTY);
                addTemplates(request, BeansTemplateContextTypeIds.ALL);
            }
        }
    }
}
