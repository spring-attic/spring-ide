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

package org.springframework.ide.eclipse.web.flow.core.internal.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.springframework.ide.eclipse.core.io.xml.LineNumberPreservingDOMParser;
import org.springframework.ide.eclipse.web.flow.core.internal.model.Action;
import org.springframework.ide.eclipse.web.flow.core.internal.model.ActionState;
import org.springframework.ide.eclipse.web.flow.core.internal.model.AttributeMapper;
import org.springframework.ide.eclipse.web.flow.core.internal.model.DecisionState;
import org.springframework.ide.eclipse.web.flow.core.internal.model.EndState;
import org.springframework.ide.eclipse.web.flow.core.internal.model.If;
import org.springframework.ide.eclipse.web.flow.core.internal.model.IfTransition;
import org.springframework.ide.eclipse.web.flow.core.internal.model.Property;
import org.springframework.ide.eclipse.web.flow.core.internal.model.StateTransition;
import org.springframework.ide.eclipse.web.flow.core.internal.model.SubFlowState;
import org.springframework.ide.eclipse.web.flow.core.internal.model.Transition;
import org.springframework.ide.eclipse.web.flow.core.internal.model.ViewState;
import org.springframework.ide.eclipse.web.flow.core.internal.model.WebFlowState;
import org.springframework.ide.eclipse.web.flow.core.model.IIf;
import org.springframework.ide.eclipse.web.flow.core.model.IState;
import org.springframework.ide.eclipse.web.flow.core.model.ITransitionableFrom;
import org.springframework.ide.eclipse.web.flow.core.model.ITransitionableTo;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfig;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlFlowParser {

    private static final String ID_ATTRIBUTE = "id";

    private static final String START_STATE_ELEMENT_ATTRIBUTE = "start-state";

    private static final String ACTION_STATE_ELEMENT = "action-state";

    private static final String ACTION_ELEMENT = "action";

    private static final String BEAN_ATTRIBUTE = "bean";

    private static final String CLASS_ATTRIBUTE = "class";

    private static final String AUTOWIRE_ATTRIBUTE = "autowire";

    private static final String CLASSREF_ATTRIBUTE = "classref";

    private static final String NAME_ATTRIBUTE = "name";

    private static final String METHOD_ATTRIBUTE = "method";

    private static final String VIEW_STATE_ELEMENT = "view-state";

    private static final String VIEW_ATTRIBUTE = "view";

    private static final String SUBFLOW_STATE_ELEMENT = "subflow-state";

    private static final String FLOW_ATTRIBUTE = "flow";

    private static final String ATTRIBUTE_MAPPER_ELEMENT = "attribute-mapper";

    private static final String END_STATE_ELEMENT = "end-state";

    private static final String TRANSITION_ELEMENT = "transition";

    private static final String EVENT_ATTRIBUTE = "on";

    private static final String TO_ATTRIBUTE = "to";

    private static final String PROPERTY_ELEMENT = "property";

    private static final String VALUE_ELEMENT = "value";

    private static final String VALUE_ATTRIBUTE = "value";

    private static final String DECISION_STATE_ELEMENT = "decision-state";

    private static final String IF_ELEMENT = "if";

    private static final String TEST_ATTRIBUTE = "test";

    private static final String THEN_ATTRIBUTE = "then";

    private static final String ELSE_ATTRIBUTE = "else";

    private IFile resource;

    private Document doc;

    /**
     * Create a new XML flow builder.
     */
    public XmlFlowParser() {
    }

    /**
     * Create a new XML flow builder.
     * 
     * @param resource
     *            Resource to read XML flow definitions from
     */
    public XmlFlowParser(IFile resource) {
        this.resource = resource;
    }

    /**
     * Set the resource from which XML flow definitions will be read.
     */
    public void setResource(IFile resource) {
        this.resource = resource;
    }

    public void buildStates(IWebFlowConfig config, Document doc)
            throws WebFlowBuilderException {
        parseStateDefinitions(config, doc);
    }

    /**
     * Parse the state definitions in the XML file and add them to the flow
     * object we're constructing.
     */
    protected void parseStateDefinitions(IWebFlowConfig config, Document doc) {
        Element root = doc.getDocumentElement();
        String startStateId = root.getAttribute(START_STATE_ELEMENT_ATTRIBUTE);
        String id = root.getAttribute(ID_ATTRIBUTE);
        // get the flow under construction
        NodeList nodeList = root.getChildNodes();
        Map states = new HashMap();
        config.setState(new WebFlowState(config, id));
        List transitions = new ArrayList();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                Element element = (Element) node;
                IState state = null;
                if (ACTION_STATE_ELEMENT.equals(element.getNodeName())) {
                    state = parseAndAddActionState(config.getState(), element,
                            transitions);
                }
                else if (VIEW_STATE_ELEMENT.equals(element.getNodeName())) {
                    state = parseAndAddViewState(config.getState(), element,
                            transitions);
                }
                else if (SUBFLOW_STATE_ELEMENT.equals(element.getNodeName())) {
                    state = parseAndAddSubFlowState(config.getState(), element,
                            transitions);
                }
                else if (END_STATE_ELEMENT.equals(element.getNodeName())) {
                    state = parseAndAddEndState(config.getState(), element);
                }
                else if (DECISION_STATE_ELEMENT.equals(element.getNodeName())) {
                    state = parseAndAddDecisionState(config.getState(),
                            element, transitions);
                }
                if (state != null) {
                    states.put(state.getId(), state);
                }
            }
        }

        linkTransitions(transitions, states);
        config.getState().setStartState((IState) states.get(startStateId));
    }

    protected void linkTransitions(List transitions, Map states) {
        for (int i = 0; i < transitions.size(); i++) {
            Transition transition = (Transition) transitions.get(i);
            transition.setToState((ITransitionableTo) states.get(transition
                    .getToStateId()));
        }
    }

    /**
     * Parse given action state definition and add a corresponding state to
     * given flow.
     */
    protected ActionState parseAndAddActionState(IWebFlowModelElement parent,
            Element element, List transitionList) {
        String id = element.getAttribute(ID_ATTRIBUTE);
        ActionState state = new ActionState(parent, id, null);
        List actions = parseActions(state, element);
        state.getActions().addAll(actions);
        state.getProperties().addAll(parseProperties(state, element));
        List transitions = parseTransitions(element, state);
        transitionList.addAll(transitions);

        state.setElementStartLine(LineNumberPreservingDOMParser
                .getStartLineNumber(element));
        state.setElementEndLine(LineNumberPreservingDOMParser
                .getEndLineNumber(element));

        return state;

    }

    /**
     * @param state
     * @param element
     * @return
     */
    protected IState parseAndAddDecisionState(IWebFlowModelElement parent,
            Element element, List transitionList) {
        String id = element.getAttribute(ID_ATTRIBUTE);
        DecisionState state = new DecisionState(parent, id, null);
        List ifs = parseIfs(state, element, transitionList);
        state.getIfs().addAll(ifs);
        state.getProperties().addAll(parseProperties(state, element));

        state.setElementStartLine(LineNumberPreservingDOMParser
                .getStartLineNumber(element));
        state.setElementEndLine(LineNumberPreservingDOMParser
                .getEndLineNumber(element));

        return state;
    }

    /**
     * Parse given view state definition and add a corresponding state to given
     * flow.
     */
    protected ViewState parseAndAddViewState(IWebFlowModelElement parent,
            Element element, List transitionList) {
        String id = element.getAttribute(ID_ATTRIBUTE);
        ViewState state = null;
        if (element.hasAttribute(VIEW_ATTRIBUTE)) {
            String viewName = element.getAttribute(VIEW_ATTRIBUTE);
            state = new ViewState(parent, id, viewName);
        }
        else {
            state = new ViewState(parent, id);
        }
        List transitions = parseTransitions(element, state);
        transitionList.addAll(transitions);
        state.getProperties().addAll(parseProperties(state, element));
        state.setElementStartLine(LineNumberPreservingDOMParser
                .getStartLineNumber(element));
        state.setElementEndLine(LineNumberPreservingDOMParser
                .getEndLineNumber(element));

        return state;
    }

    /**
     * Parse given sub flow state definition and add a corresponding state to
     * given flow.
     */
    protected SubFlowState parseAndAddSubFlowState(IWebFlowModelElement parent,
            Element element, List transitionList) {
        String id = element.getAttribute(ID_ATTRIBUTE);
        String flowName = element.getAttribute(FLOW_ATTRIBUTE);
        String mapper = null;
        SubFlowState state = new SubFlowState(parent, id, flowName);
        state.setAttributeMapper(parseAttributeMapper(state, element));
        List transitions = parseTransitions(element, state);
        transitionList.addAll(transitions);
        state.getProperties().addAll(parseProperties(state, element));
        state.setElementStartLine(LineNumberPreservingDOMParser
                .getStartLineNumber(element));
        state.setElementEndLine(LineNumberPreservingDOMParser
                .getEndLineNumber(element));

        return state;
    }

    protected AttributeMapper parseAttributeMapper(IWebFlowModelElement parent,
            Element element) {
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (ATTRIBUTE_MAPPER_ELEMENT
                    .equals(children.item(i).getLocalName())) {
                Element child = (Element) children.item(i);
                String beanId = child.getAttribute(BEAN_ATTRIBUTE);
                AttributeMapper mapper = new AttributeMapper(parent, null);
                if (StringUtils.hasText(beanId)) {
                    mapper.setBean(beanId);
                }
                else {
                    String actionClassName = child
                            .getAttribute(CLASS_ATTRIBUTE);
                    if (StringUtils.hasText(actionClassName)) {
                        String autowireLabel = child
                                .getAttribute(AUTOWIRE_ATTRIBUTE);
                        mapper.setAutowire(autowireLabel);
                        mapper.setBeanClass(actionClassName);
                    }
                    else {
                        actionClassName = child
                                .getAttribute(CLASSREF_ATTRIBUTE);
                        Assert
                                .hasText(
                                        actionClassName,
                                        "Exactly one of the action id, class, or classref attributes "
                                                + "are required for this action definition");
                        mapper.setClassRef(actionClassName);
                    }
                }
                mapper.setElementStartLine(LineNumberPreservingDOMParser
                        .getStartLineNumber(child));
                mapper.setElementEndLine(LineNumberPreservingDOMParser
                        .getEndLineNumber(child));

                return mapper;
            }
        }
        return null;
    }

    /**
     * Parse given end state definition and add a corresponding state to given
     * flow.
     */
    protected EndState parseAndAddEndState(IWebFlowModelElement parent,
            Element element) {
        String id = element.getAttribute(ID_ATTRIBUTE);
        EndState state = null;
        if (element.hasAttribute(VIEW_ATTRIBUTE)) {
            String viewName = element.getAttribute(VIEW_ATTRIBUTE);
            state = new EndState(parent, id, viewName);
        }
        else {
            state = new EndState(parent, id);

        }
        state.getProperties().addAll(parseProperties(state, element));
        state.setElementStartLine(LineNumberPreservingDOMParser
                .getStartLineNumber(element));
        state.setElementEndLine(LineNumberPreservingDOMParser
                .getEndLineNumber(element));

        return state;
    }

    /**
     * Find all action definitions in given action state definition and obtain
     * corresponding Action objects.
     */
    protected List parseActions(IWebFlowModelElement parent, Element element) {
        List actions = new LinkedList();
        NodeList childNodeList = element.getChildNodes();
        for (int i = 0; i < childNodeList.getLength(); i++) {
            Node childNode = childNodeList.item(i);
            if (childNode instanceof Element) {
                Element childElement = (Element) childNode;
                if (ACTION_ELEMENT.equals(childElement.getNodeName())) {
                    actions.add(parseAction(parent, childElement));
                }
            }
        }
        return actions;
    }

    /**
     * Find all action definitions in given action state definition and obtain
     * corresponding Action objects.
     */
    protected List parseIfs(IWebFlowModelElement parent, Element element,
            List transitionList) {
        List ifs = new LinkedList();
        NodeList childNodeList = element.getChildNodes();
        for (int i = 0; i < childNodeList.getLength(); i++) {
            Node childNode = childNodeList.item(i);
            if (childNode instanceof Element) {
                Element childElement = (Element) childNode;
                if (IF_ELEMENT.equals(childElement.getNodeName())) {
                    ifs.add(parseIf(parent, childElement, transitionList));
                }
            }
        }
        return ifs;
    }

    /**
     * Parse given action definition and return a corresponding Action object.
     */
    protected IIf parseIf(IWebFlowModelElement parent, Element element,
            List transitionList) {
        String test = element.getAttribute(TEST_ATTRIBUTE);
        String then = element.getAttribute(THEN_ATTRIBUTE);
        String theElse = element.getAttribute(ELSE_ATTRIBUTE);

        If theIf = new If();
        theIf.setElementParent(parent);

        IfTransition transThen = new IfTransition(then, theIf, true);
        transThen.setElementStartLine(LineNumberPreservingDOMParser
                .getStartLineNumber(element));
        transThen.setElementEndLine(LineNumberPreservingDOMParser
                .getEndLineNumber(element));
        theIf.setThenTransition(transThen);
        transitionList.add(transThen);

        if (theElse != null && !"".equals(theElse)) {
            IfTransition transElse = new IfTransition(theElse, theIf, false);
            transElse.setElementStartLine(LineNumberPreservingDOMParser
                    .getStartLineNumber(element));
            transElse.setElementEndLine(LineNumberPreservingDOMParser
                    .getEndLineNumber(element));
            theIf.setElseTransition(transElse);
            transitionList.add(transElse);
        }
        theIf.setTest(test);
        theIf.setElementStartLine(LineNumberPreservingDOMParser
                .getStartLineNumber(element));
        theIf.setElementEndLine(LineNumberPreservingDOMParser
                .getEndLineNumber(element));

        return theIf;
    }

    /**
     * Find all action definitions in given action state definition and obtain
     * corresponding Action objects.
     */
    protected List parseProperties(IWebFlowModelElement parent, Element element) {
        List properties = new LinkedList();
        NodeList childNodeList = element.getChildNodes();
        for (int i = 0; i < childNodeList.getLength(); i++) {
            Node childNode = childNodeList.item(i);
            if (childNode instanceof Element) {
                Element childElement = (Element) childNode;
                if (PROPERTY_ELEMENT.equals(childElement.getNodeName())) {
                    properties.add(parseProperty(parent, childElement));
                }
            }
        }
        return properties;
    }

    /**
     * Parse given action definition and return a corresponding Action object.
     */
    protected Property parseProperty(IWebFlowModelElement parent,
            Element element) {
        String name = element.getAttribute(NAME_ATTRIBUTE);
        Property property = new Property();
        if (StringUtils.hasText(name)) {
            property.setName(name);
        }
        String value = element.getAttribute(VALUE_ELEMENT);
        if (StringUtils.hasText(value)) {
            property.setValue(value);
        }
        else {
            NodeList childNodes = element.getChildNodes();
            if (childNodes.getLength() > 0) {
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node node = childNodes.item(i);
                    if (VALUE_ELEMENT.equals(node.getNodeName())) {
                        property.setValue(node.getFirstChild().getNodeValue());
                    }
                }
            }
        }

        property.setElementStartLine(LineNumberPreservingDOMParser
                .getStartLineNumber(element));
        property.setElementEndLine(LineNumberPreservingDOMParser
                .getEndLineNumber(element));

        return property;
    }

    /**
     * Parse given action definition and return a corresponding Action object.
     */
    protected Action parseAction(IWebFlowModelElement parent, Element element) {
        String actionId = element.getAttribute(BEAN_ATTRIBUTE);
        String name = element.getAttribute(NAME_ATTRIBUTE);
        String method = element.getAttribute(METHOD_ATTRIBUTE);
        Action action = new Action(parent, null);
        if (StringUtils.hasText(actionId)) {
            action.setBean(actionId);
        }
        else {
            String actionClassName = element.getAttribute(CLASS_ATTRIBUTE);
            if (StringUtils.hasText(actionClassName)) {
                String autowireLabel = element.getAttribute(AUTOWIRE_ATTRIBUTE);
                action.setAutowire(autowireLabel);
                action.setBeanClass(actionClassName);
            }
            else {
                actionClassName = element.getAttribute(CLASSREF_ATTRIBUTE);
                Assert.hasText(actionClassName,
                        "Exactly one of the action id, class, or classref attributes "
                                + "are required for this action definition");
                action.setClassRef(actionClassName);
            }
        }
        if (!"".equals(name)) {
            action.setName(name);
        }
        if (!"".equals(method)) {
            action.setMethod(method);
        }
        action.getProperties().addAll(parseProperties(action, element));

        action.setElementStartLine(LineNumberPreservingDOMParser
                .getStartLineNumber(element));
        action.setElementEndLine(LineNumberPreservingDOMParser
                .getEndLineNumber(element));

        return action;
    }

    /**
     * Find all transition definitions in given state definition and return a
     * list of corresponding Transition objects.
     */
    protected List parseTransitions(Element element, IState state) {
        List transitions = new LinkedList();
        NodeList childNodeList = element.getChildNodes();
        for (int i = 0; i < childNodeList.getLength(); i++) {
            Node childNode = childNodeList.item(i);
            if (childNode instanceof Element) {
                Element childElement = (Element) childNode;
                if (TRANSITION_ELEMENT.equals(childElement.getNodeName())) {
                    transitions.add(parseTransition(childElement, state));
                }
            }
        }

        return transitions;
    }

    /**
     * Parse a transition definition and return a corresponding Transition
     * object.
     */
    protected Transition parseTransition(Element element, IState state) {
        String event = element.getAttribute(EVENT_ATTRIBUTE);
        String to = element.getAttribute(TO_ATTRIBUTE);
        StateTransition trans = new StateTransition(to,
                (ITransitionableFrom) state, event);

        trans.getActions().addAll(parseActions(trans, element));

        trans.setElementStartLine(LineNumberPreservingDOMParser
                .getStartLineNumber(element));
        trans.setElementEndLine(LineNumberPreservingDOMParser
                .getEndLineNumber(element));

        return trans;
    }
}