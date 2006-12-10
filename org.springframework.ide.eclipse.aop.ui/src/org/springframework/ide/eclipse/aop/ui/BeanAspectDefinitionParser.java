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
package org.springframework.ide.eclipse.aop.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.beans.ui.editor.BeansEditorUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@SuppressWarnings("restriction")
public class BeanAspectDefinitionParser {

    public static List<BeanAspectDefinition> parse(IDOMDocument document, IFile file) {
        List<BeanAspectDefinition> aspectInfos = new ArrayList<BeanAspectDefinition>();
        NodeList list = document.getDocumentElement().getElementsByTagNameNS(
                "http://www.springframework.org/schema/aop", "config");
        for (int i = 0; i < list.getLength(); i++) {
            Map<String, String> rootPointcuts = new HashMap<String, String>();
            Node node = list.item(i);
            NodeList children = node.getChildNodes();

            parsePointcuts(rootPointcuts, children);

            for (int j = 0; j < children.getLength(); j++) {
                Node child = children.item(j);
                if ("aspect".equals(child.getLocalName())) {
                    parseAspects(file, child, rootPointcuts, aspectInfos);
                }
            }
        }
        
        addDocument(document, aspectInfos);
        
        return aspectInfos;
    }

    private static void addDocument(IDOMDocument document, List<BeanAspectDefinition> aspectInfos) {
        for (BeanAspectDefinition info : aspectInfos) {
            info.setDocument(document);
        }
    }

    private static void parsePointcuts(Map<String, String> rootPointcuts,
            NodeList children) {
        for (int j = 0; j < children.getLength(); j++) {
            Node child = children.item(j);
            if ("pointcut".equals(child.getLocalName())) {
                String id = BeansEditorUtils.getAttribute(child, "id");
                String expression = BeansEditorUtils.getAttribute(child,
                        "expression");
                if (StringUtils.hasText(id) && StringUtils.hasText(expression)) {
                    rootPointcuts.put(id, expression);
                }
            }
        }
    }

    private static void parseAspects(IFile file, Node child,
            Map<String, String> rootPointcuts, List<BeanAspectDefinition> aspectInfos) {
        String beanRef = BeansEditorUtils.getAttribute(child, "ref");
        String className = BeansEditorUtils.getClassNameForBean(file, child
                .getOwnerDocument(), beanRef);
        if (StringUtils.hasText(className)) {
            NodeList aspectChildren = child.getChildNodes();
            Map<String, String> pointcuts = new HashMap<String, String>();
            parsePointcuts(pointcuts, aspectChildren);

            for (int g = 0; g < aspectChildren.getLength(); g++) {
                Node aspectNode = aspectChildren.item(g);
                if ("before".equals(aspectNode.getLocalName())) {
                    BeanAspectDefinition info = parseAspect(pointcuts, rootPointcuts, aspectNode,
                            IAopReference.ADVICE_TYPES.BEFORE);
                    info.setClassName(className);
                    aspectInfos.add(info);
                }
                else if ("around".equals(aspectNode.getLocalName())) {
                    BeanAspectDefinition info = parseAspect(pointcuts, rootPointcuts, aspectNode,
                            IAopReference.ADVICE_TYPES.AROUND);
                    info.setClassName(className);
                    aspectInfos.add(info);
                }
                else if ("after".equals(aspectNode.getLocalName())) {
                    BeanAspectDefinition info = parseAspect(pointcuts, rootPointcuts, aspectNode,
                            IAopReference.ADVICE_TYPES.AFTER);
                    info.setClassName(className);
                    aspectInfos.add(info);
                }
                else if ("after-returning".equals(aspectNode.getLocalName())) {
                    BeanAspectDefinition info = parseAspect(pointcuts, rootPointcuts, aspectNode,
                            IAopReference.ADVICE_TYPES.AFTER_RETURNING);
                    String returning = BeansEditorUtils.getAttribute(
                            aspectNode, "returning");
                    info.setReturning(returning);
                    info.setClassName(className);
                    aspectInfos.add(info);
                }
                else if ("after-throwing".equals(aspectNode.getLocalName())) {
                    BeanAspectDefinition info = parseAspect(pointcuts, rootPointcuts, aspectNode,
                            IAopReference.ADVICE_TYPES.AFTER_THROWING);
                    String throwing = BeansEditorUtils.getAttribute(aspectNode,
                            "throwing");
                    info.setThrowing(throwing);
                    info.setClassName(className);
                    aspectInfos.add(info);
                }
                else if ("around".equals(aspectNode.getLocalName())) {
                    BeanAspectDefinition info = parseAspect(pointcuts, rootPointcuts, aspectNode,
                            IAopReference.ADVICE_TYPES.AROUND);
                    info.setClassName(className);
                    aspectInfos.add(info);
                }
            }
        }
    }

    private static BeanAspectDefinition parseAspect(Map<String, String> pointcuts, Map<String, String> rootPointcuts, 
            Node aspectNode, IAopReference.ADVICE_TYPES type) {
        BeanAspectDefinition info = new BeanAspectDefinition();
        String pointcut = BeansEditorUtils.getAttribute(aspectNode, "pointcut");
        String pointcutRef = BeansEditorUtils.getAttribute(aspectNode,
                "pointcut-ref");
        if (!StringUtils.hasText(pointcut)) {
            pointcut = pointcuts.get(pointcutRef);
            if (!StringUtils.hasText(pointcut)) {
                pointcut = rootPointcuts.get(pointcutRef);
            }
        }
        String argNames = BeansEditorUtils
                .getAttribute(aspectNode, "arg-names");
        String method = BeansEditorUtils.getAttribute(aspectNode, "method");
        String[] argNamesArray = null;
        if (argNames != null) {
            argNamesArray = StringUtils
                    .commaDelimitedListToStringArray(argNames);
        }
        info.setArgNames(argNamesArray);
        info.setNode((IDOMNode) aspectNode);
        info.setPointcut(pointcut);
        info.setType(type);
        info.setMethod(method);
        return info;
    }

}
