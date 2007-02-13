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
package org.springframework.ide.eclipse.aop.core.parser;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.objectweb.asm.ClassReader;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.ThrowsAdvice;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;
import org.springframework.ide.eclipse.aop.core.model.internal.BeanAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.internal.BeanIntroductionDefinition;
import org.springframework.ide.eclipse.aop.core.model.internal.JavaAspectDefinition;
import org.springframework.ide.eclipse.aop.core.parser.asm.AdviceAnnotationVisitor;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@SuppressWarnings("restriction")
public class BeansAspectDefinitionParser {

    private static void addDocument(IDOMDocument document, List<IAspectDefinition> aspectInfos,
            IResource file) {
        for (IAspectDefinition info : aspectInfos) {
            if (info instanceof BeanAspectDefinition) {
                ((BeanAspectDefinition) info).setDocument(document);
            }
            info.setResource(file);
        }
    }

    /**
     * If no &lt;aop:include&gt; elements were used then includePatterns will be null and all beans
     * are included. If includePatterns is non-null, then one of the patterns must match.
     */
    private static boolean isIncluded(List<Pattern> includePatterns, String beanName) {
        if (includePatterns == null || includePatterns.size() == 0) {
            return true;
        }
        else {
            for (Pattern pattern : includePatterns) {
                if (pattern.matcher(beanName).matches()) {
                    return true;
                }
            }
            return false;
        }
    }

    public static List<IAspectDefinition> buildAspectDefinitions(final IDOMDocument document,
            IFile file) {
        final List<IAspectDefinition> aspectInfos = new ArrayList<IAspectDefinition>();

        parseXmlAspects(document, file, aspectInfos);
        parseAnnotationAspects(document, file, aspectInfos);

        addDocument(document, aspectInfos, file);
        return aspectInfos;
    }

    private static void parseAnnotationAspects(final IDOMDocument document, IFile file,
            final List<IAspectDefinition> aspectInfos) {
        NodeList list;
        list = document.getDocumentElement().getElementsByTagNameNS(
                "http://www.springframework.org/schema/aop", "aspectj-autoproxy");
        if (list.getLength() > 0) {
            Node item = list.item(0);
            List<Pattern> patternList = new ArrayList<Pattern>();
            NodeList include = item.getChildNodes();
            for (int j = 0; j < include.getLength(); j++) {
                if ("include".equals(include.item(j).getLocalName())) {
                    String pattern = BeansEditorUtils.getAttribute(include.item(j), "name");
                    if (StringUtils.hasText(pattern)) {
                        patternList.add(Pattern.compile(pattern));
                    }
                }
            }

            list = document.getDocumentElement().getElementsByTagNameNS(
                    "http://www.springframework.org/schema/beans", "bean");

            for (int j = 0; j < list.getLength(); j++) {
                final Node bean = list.item(j);
                final String id = BeansEditorUtils.getAttribute(bean, "id");
                final String className = BeansEditorUtils.getClassNameForBean(bean);
                if (className != null && isIncluded(patternList, id)) {
                    try {
                        if (BeansAopModelUtils.validateAspect(className)) {
                            createAnnotationAspectDefinition(document, bean, id, className,
                                    aspectInfos);
                        }
                    }
                    catch (Throwable e) {
                        Activator.log(e);
                    }
                }
            }
        }
    }

    private static void parseXmlAspects(final IDOMDocument document, IFile file,
            final List<IAspectDefinition> aspectInfos) {
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
                else if ("advisor".equals(child.getLocalName())) {
                    parseAdvisors(file, child, rootPointcuts, aspectInfos);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void createAnnotationAspectDefinition(final IDOMDocument document,
            final Node bean, final String id, final String className,
            final List<IAspectDefinition> aspectInfos) throws Throwable {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        ClassReader reader = new ClassReader(classLoader.getResourceAsStream(BeansAopModelUtils
                .getClassFileName(className)));
        AdviceAnnotationVisitor v = new AdviceAnnotationVisitor((IDOMNode) bean, id, className);
        reader.accept(v, false);

        List<IAspectDefinition> aspectDefinitions = v.getAspectDefinitions();
        aspectInfos.addAll(aspectDefinitions);
    }

    private static BeanAspectDefinition parseAspect(Map<String, String> pointcuts,
            Map<String, String> rootPointcuts, Node aspectNode, IAopReference.ADVICE_TYPES type) {
        BeanAspectDefinition info = new BeanAspectDefinition();
        String pointcut = BeansEditorUtils.getAttribute(aspectNode, "pointcut");
        String pointcutRef = BeansEditorUtils.getAttribute(aspectNode, "pointcut-ref");
        if (!StringUtils.hasText(pointcut)) {
            pointcut = pointcuts.get(pointcutRef);
            if (!StringUtils.hasText(pointcut)) {
                pointcut = rootPointcuts.get(pointcutRef);
            }
        }
        String argNames = BeansEditorUtils.getAttribute(aspectNode, "arg-names");
        String method = BeansEditorUtils.getAttribute(aspectNode, "method");
        String[] argNamesArray = null;
        if (argNames != null) {
            argNamesArray = StringUtils.commaDelimitedListToStringArray(argNames);
        }
        info.setArgNames(argNamesArray);
        info.setNode((IDOMNode) aspectNode);
        info.setPointcutExpression(pointcut);
        info.setType(type);
        info.setAdviceMethodName(method);
        return info;
    }

    private static void parseAspects(IFile file, Node child, Map<String, String> rootPointcuts,
            List<IAspectDefinition> aspectInfos) {
        String beanRef = BeansEditorUtils.getAttribute(child, "ref");
        String className = BeansEditorUtils.getClassNameForBean(file, child.getOwnerDocument(),
                beanRef);
        if (StringUtils.hasText(className)) {
            NodeList aspectChildren = child.getChildNodes();
            Map<String, String> pointcuts = new HashMap<String, String>();
            parsePointcuts(pointcuts, aspectChildren);

            for (int g = 0; g < aspectChildren.getLength(); g++) {
                Node aspectNode = aspectChildren.item(g);
                if ("declare-parents".equals(aspectNode.getLocalName())) {
                    String typesMatching = BeansEditorUtils.getAttribute(aspectNode,
                            "types-matching");
                    String defaultImpl = BeansEditorUtils.getAttribute(aspectNode, "default-impl");
                    String implementInterface = BeansEditorUtils.getAttribute(aspectNode,
                            "implement-interface");
                    if (StringUtils.hasText(typesMatching) && StringUtils.hasText(defaultImpl)
                            && StringUtils.hasText(implementInterface)) {
                        BeanIntroductionDefinition info = new BeanIntroductionDefinition();
                        info.setIntroducedInterfaceName(implementInterface);
                        info.setTypePattern(typesMatching);
                        info.setDefaultImplName(defaultImpl);
                        info.setAspectClassName(defaultImpl);
                        info.setAspectName(beanRef);
                        info.setNode((IDOMNode) aspectNode);
                        aspectInfos.add(info);
                    }
                }
                else if ("before".equals(aspectNode.getLocalName())) {
                    BeanAspectDefinition info = parseAspect(pointcuts, rootPointcuts, aspectNode,
                            IAopReference.ADVICE_TYPES.BEFORE);
                    info.setAspectClassName(className);
                    info.setAspectName(beanRef);
                    aspectInfos.add(info);
                }
                else if ("around".equals(aspectNode.getLocalName())) {
                    BeanAspectDefinition info = parseAspect(pointcuts, rootPointcuts, aspectNode,
                            IAopReference.ADVICE_TYPES.AROUND);
                    info.setAspectClassName(className);
                    info.setAspectName(beanRef);
                    aspectInfos.add(info);
                }
                else if ("after".equals(aspectNode.getLocalName())) {
                    BeanAspectDefinition info = parseAspect(pointcuts, rootPointcuts, aspectNode,
                            IAopReference.ADVICE_TYPES.AFTER);
                    info.setAspectClassName(className);
                    info.setAspectName(beanRef);
                    aspectInfos.add(info);
                }
                else if ("after-returning".equals(aspectNode.getLocalName())) {
                    BeanAspectDefinition info = parseAspect(pointcuts, rootPointcuts, aspectNode,
                            IAopReference.ADVICE_TYPES.AFTER_RETURNING);
                    String returning = BeansEditorUtils.getAttribute(aspectNode, "returning");
                    info.setReturning(returning);
                    info.setAspectClassName(className);
                    info.setAspectName(beanRef);
                    aspectInfos.add(info);
                }
                else if ("after-throwing".equals(aspectNode.getLocalName())) {
                    BeanAspectDefinition info = parseAspect(pointcuts, rootPointcuts, aspectNode,
                            IAopReference.ADVICE_TYPES.AFTER_THROWING);
                    String throwing = BeansEditorUtils.getAttribute(aspectNode, "throwing");
                    info.setThrowing(throwing);
                    info.setAspectClassName(className);
                    info.setAspectName(beanRef);
                    aspectInfos.add(info);
                }
                else if ("around".equals(aspectNode.getLocalName())) {
                    BeanAspectDefinition info = parseAspect(pointcuts, rootPointcuts, aspectNode,
                            IAopReference.ADVICE_TYPES.AROUND);
                    info.setAspectClassName(className);
                    info.setAspectName(beanRef);
                    aspectInfos.add(info);
                }
            }
        }
    }

    private static void parseAdvisors(IFile file, Node aspectNode,
            Map<String, String> rootPointcuts, List<IAspectDefinition> aspectInfos) {
        String beanRef = BeansEditorUtils.getAttribute(aspectNode, "advice-ref");
        String className = BeansEditorUtils.getClassNameForBean(file,
                aspectNode.getOwnerDocument(), beanRef);
        if (StringUtils.hasText(className)) {
            NodeList aspectChildren = aspectNode.getParentNode().getChildNodes();
            Map<String, String> pointcuts = new HashMap<String, String>();
            parsePointcuts(pointcuts, aspectChildren);

            String pointcut = BeansEditorUtils.getAttribute(aspectNode, "pointcut");
            String pointcutRef = BeansEditorUtils.getAttribute(aspectNode, "pointcut-ref");
            if (!StringUtils.hasText(pointcut)) {
                pointcut = pointcuts.get(pointcutRef);
                if (!StringUtils.hasText(pointcut)) {
                    pointcut = rootPointcuts.get(pointcutRef);
                }
            }

            try {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                Class<?> advisorClass = classLoader.loadClass(className);

                if (classLoader.loadClass(MethodInterceptor.class.getName()).isAssignableFrom(
                        advisorClass)) {
                    JavaAspectDefinition info = new JavaAspectDefinition();
                    info.setNode((IDOMNode) aspectNode);
                    info.setPointcutExpression(pointcut);
                    info.setAspectClassName(className);
                    info.setAspectName(beanRef);
                    info.setType(ADVICE_TYPES.AROUND);
                    info.setAspectClassName(className);
                    info.setAdviceMethodName("invoke");
                    info.setAdviceMethodParameterTypes(new String[] { MethodInvocation.class
                            .getName() });
                    aspectInfos.add(info);
                }
                if (classLoader.loadClass(MethodBeforeAdvice.class.getName()).isAssignableFrom(
                        advisorClass)) {
                    JavaAspectDefinition info = new JavaAspectDefinition();
                    info.setNode((IDOMNode) aspectNode);
                    info.setPointcutExpression(pointcut);
                    info.setAspectClassName(className);
                    info.setAspectName(beanRef);
                    info.setType(ADVICE_TYPES.BEFORE);
                    info.setAdviceMethodName("before");
                    info.setAspectClassName(className);
                    info.setAdviceMethodParameterTypes(new String[] { Method.class.getName(),
                            Object[].class.getName(), Object.class.getName() });
                    aspectInfos.add(info);
                }
                if (classLoader.loadClass(ThrowsAdvice.class.getName()).isAssignableFrom(
                        advisorClass)) {
                    JavaAspectDefinition info = new JavaAspectDefinition();
                    info.setNode((IDOMNode) aspectNode);
                    info.setPointcutExpression(pointcut);
                    info.setAspectClassName(className);
                    info.setAspectName(beanRef);
                    info.setType(ADVICE_TYPES.AFTER_THROWING);
                    info.setAdviceMethodName("afterThrowing");
                    info.setAspectClassName(className);
                    info.setAdviceMethodParameterTypes(new String[] { Method.class.getName(),
                            Object[].class.getName(), Object.class.getName(),
                            Exception.class.getName() });
                    aspectInfos.add(info);
                }
                if (classLoader.loadClass(AfterReturningAdvice.class.getName()).isAssignableFrom(
                        advisorClass)) {
                    JavaAspectDefinition info = new JavaAspectDefinition();
                    info.setNode((IDOMNode) aspectNode);
                    info.setPointcutExpression(pointcut);
                    info.setAspectClassName(className);
                    info.setAspectName(beanRef);
                    info.setType(ADVICE_TYPES.AFTER_RETURNING);
                    info.setAdviceMethodName("afterReturning");
                    info.setAspectClassName(className);
                    info.setAdviceMethodParameterTypes(new String[] { Object.class.getName(),
                            Method.class.getName(), Object[].class.getName(),
                            Object.class.getName() });
                    aspectInfos.add(info);
                }
            }
            catch (Throwable e) {
                Activator.log(e);
            }
        }
    }

    private static void parsePointcuts(Map<String, String> rootPointcuts, NodeList children) {
        for (int j = 0; j < children.getLength(); j++) {
            Node child = children.item(j);
            if ("pointcut".equals(child.getLocalName())) {
                String id = BeansEditorUtils.getAttribute(child, "id");
                String expression = BeansEditorUtils.getAttribute(child, "expression");
                if (StringUtils.hasText(id) && StringUtils.hasText(expression)) {
                    rootPointcuts.put(id, expression);
                }
            }
        }
    }
}
