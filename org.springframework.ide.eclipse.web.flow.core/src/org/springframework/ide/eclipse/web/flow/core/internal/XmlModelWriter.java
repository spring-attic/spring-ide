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

package org.springframework.ide.eclipse.web.flow.core.internal;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.springframework.ide.eclipse.web.flow.core.internal.parser.WebFlowDtdResolver;
import org.springframework.ide.eclipse.web.flow.core.model.IAction;
import org.springframework.ide.eclipse.web.flow.core.model.IActionState;
import org.springframework.ide.eclipse.web.flow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.web.flow.core.model.IDecisionState;
import org.springframework.ide.eclipse.web.flow.core.model.IEndState;
import org.springframework.ide.eclipse.web.flow.core.model.IIf;
import org.springframework.ide.eclipse.web.flow.core.model.IModelWriter;
import org.springframework.ide.eclipse.web.flow.core.model.IProperty;
import org.springframework.ide.eclipse.web.flow.core.model.IStateTransition;
import org.springframework.ide.eclipse.web.flow.core.model.ISubFlowState;
import org.springframework.ide.eclipse.web.flow.core.model.IViewState;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowState;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Xml Implementation of saving Spring Web Flow definition files to xml
 * 
 * @author Christian Dupuis
 */
public class XmlModelWriter implements IModelWriter {

    private static final String FLOW_DTD = "<!DOCTYPE webflow PUBLIC \"-//SPRING//DTD WEBFLOW//EN\" \"http://www.springframework.org/dtd/spring-webflow.dtd\">";

    private static final String COMMENT = "<!-- File generated with Spring IDE Web Flow Editor. Visit http://springide.org/ -->";

    private static final String ID_ATTRIBUTE = "id";

    private static final String FLOW_ELEMENT = "webflow";

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

    XmlWriter writer = null;

    OutputStream stream = null;

    ByteArrayOutputStream tempStream = null;

    public XmlModelWriter(OutputStream stream)
            throws UnsupportedEncodingException {
        this.stream = stream;

        tempStream = new ByteArrayOutputStream();

        writer = new XmlWriter(tempStream);
        writer.println(COMMENT);
        writer.println(FLOW_DTD);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IModelWriter#doStart(org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement)
     */
    public void doStart(IWebFlowModelElement element) {
        int type = element.getElementType();
        switch (type) {
            case IWebFlowModelElement.WEBFLOW_STATE:
                doStartWebFlow(element);
                break;
            case IWebFlowModelElement.ACTION_STATE:
                doStartActionState(element);
                break;
            case IWebFlowModelElement.ACTION:
                doStartAction(element);
                break;
            case IWebFlowModelElement.PROPERTY:
                doStartProperty(element);
                break;
            case IWebFlowModelElement.VIEW_STATE:
                doStartViewState(element);
                break;
            case IWebFlowModelElement.SUBFLOW_STATE:
                doStartSubFlowState(element);
                break;
            case IWebFlowModelElement.END_STATE:
                doStartEndState(element);
                break;
            case IWebFlowModelElement.ATTRIBUTEMAPPER:
                doStartAttributeMapper(element);
                break;
            case IWebFlowModelElement.STATE_TRANSITION:
                doStartStateTransition(element);
                break;
            case IWebFlowModelElement.DECISION_STATE:
                doStartDecisionState(element);
                break;
            case IWebFlowModelElement.IF:
                doStartIf(element);
                break;
            default:
                break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IModelWriter#doEnd(org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement)
     */
    public void doEnd(IWebFlowModelElement element) {
        int type = element.getElementType();
        switch (type) {
            case IWebFlowModelElement.WEBFLOW_STATE:
                doEndWebFlow(element);
                break;
            case IWebFlowModelElement.ACTION_STATE:
                doEndActionState(element);
                break;
            case IWebFlowModelElement.ACTION:
                doEndAction(element);
                break;
            case IWebFlowModelElement.PROPERTY:
                doEndProperty(element);
                break;
            case IWebFlowModelElement.VIEW_STATE:
                doEndViewState(element);
                break;
            case IWebFlowModelElement.SUBFLOW_STATE:
                doEndSubFlowState(element);
                break;
            case IWebFlowModelElement.END_STATE:
                doEndEndState(element);
                break;
            case IWebFlowModelElement.ATTRIBUTEMAPPER:
                doEndAttributeMapper(element);
                break;
            case IWebFlowModelElement.DECISION_STATE:
                doEndDecisionState(element);
                break;
            case IWebFlowModelElement.STATE_TRANSITION:
                doEndStateTransition(element);
                break;
            case IWebFlowModelElement.IF:
                doEndIf(element);
                break;
            default:
                break;
        }
    }

    private void doStartWebFlow(IWebFlowModelElement element) {
        IWebFlowState state = (IWebFlowState) element;
        HashMap parameters = new HashMap();
        parameters.put(ID_ATTRIBUTE, state.getId());
        parameters.put(START_STATE_ELEMENT_ATTRIBUTE, state.getStartState()
                .getId());
        writer.startTag(FLOW_ELEMENT, parameters);
    }

    private void doEndWebFlow(IWebFlowModelElement element) {
        writer.endTag(FLOW_ELEMENT);
    }

    private void doStartActionState(IWebFlowModelElement element) {
        IActionState state = (IActionState) element;
        HashMap parameters = new HashMap();
        parameters.put(ID_ATTRIBUTE, state.getId());
        writer.startTag(ACTION_STATE_ELEMENT, parameters);
    }

    private void doEndActionState(IWebFlowModelElement element) {
        writer.endTag(ACTION_STATE_ELEMENT);
    }

    private void doStartAction(IWebFlowModelElement element) {
        IAction state = (IAction) element;
        HashMap parameters = new HashMap();
        parameters.put(AUTOWIRE_ATTRIBUTE, state.getAutowire());
        parameters.put(BEAN_ATTRIBUTE, state.getBean());
        parameters.put(CLASS_ATTRIBUTE, state.getBeanClass());
        parameters.put(CLASSREF_ATTRIBUTE, state.getClassRef());
        parameters.put(METHOD_ATTRIBUTE, state.getMethod());
        parameters.put(NAME_ATTRIBUTE, state.getName());
        writer.startTag(ACTION_ELEMENT, parameters);
    }

    private void doEndAction(IWebFlowModelElement element) {
        writer.endTag(ACTION_ELEMENT);
    }

    private void doStartViewState(IWebFlowModelElement element) {
        IViewState state = (IViewState) element;
        HashMap parameters = new HashMap();
        parameters.put(ID_ATTRIBUTE, state.getId());
        parameters.put(VIEW_ATTRIBUTE, state.getView());
        writer.startTag(VIEW_STATE_ELEMENT, parameters);
    }

    private void doEndViewState(IWebFlowModelElement element) {
        writer.endTag(VIEW_STATE_ELEMENT);
    }

    private void doStartSubFlowState(IWebFlowModelElement element) {
        ISubFlowState state = (ISubFlowState) element;
        HashMap parameters = new HashMap();
        parameters.put(ID_ATTRIBUTE, state.getId());
        parameters.put(FLOW_ATTRIBUTE, state.getFlow());
        writer.startTag(SUBFLOW_STATE_ELEMENT, parameters);
    }

    private void doEndSubFlowState(IWebFlowModelElement element) {
        writer.endTag(SUBFLOW_STATE_ELEMENT);
    }

    private void doStartEndState(IWebFlowModelElement element) {
        IEndState state = (IEndState) element;
        HashMap parameters = new HashMap();
        parameters.put(ID_ATTRIBUTE, state.getId());
        parameters.put(VIEW_ATTRIBUTE, state.getView());
        writer.startTag(END_STATE_ELEMENT, parameters, true);
    }

    private void doEndEndState(IWebFlowModelElement element) {
        writer.endTag(END_STATE_ELEMENT);
    }

    private void doStartAttributeMapper(IWebFlowModelElement element) {
        IAttributeMapper state = (IAttributeMapper) element;
        HashMap parameters = new HashMap();
        parameters.put(AUTOWIRE_ATTRIBUTE, state.getAutowire());
        parameters.put(BEAN_ATTRIBUTE, state.getBean());
        parameters.put(CLASS_ATTRIBUTE, state.getBeanClass());
        parameters.put(CLASSREF_ATTRIBUTE, state.getClassRef());
        parameters.put(METHOD_ATTRIBUTE, state.getMethod());
        parameters.put(NAME_ATTRIBUTE, state.getName());
        writer.printTag(ATTRIBUTE_MAPPER_ELEMENT, parameters, true, true, true);
    }

    private void doEndAttributeMapper(IWebFlowModelElement element) {
    }

    private void doStartProperty(IWebFlowModelElement element) {
        IProperty state = (IProperty) element;
        HashMap parameters = new HashMap();
        parameters.put(NAME_ATTRIBUTE, state.getName());
        writer.startTag(PROPERTY_ELEMENT, parameters);
        writer.printSimpleTag(VALUE_ELEMENT, state.getValue());
    }

    private void doEndProperty(IWebFlowModelElement element) {
        writer.endTag(PROPERTY_ELEMENT);
    }

    private void doStartStateTransition(IWebFlowModelElement element) {
        IStateTransition state = (IStateTransition) element;
        HashMap parameters = new HashMap();
        parameters.put(EVENT_ATTRIBUTE, state.getOn());
        parameters.put(TO_ATTRIBUTE, state.getToState().getId());
        writer.startTag(TRANSITION_ELEMENT, parameters, true);
    }

    private void doEndStateTransition(IWebFlowModelElement element) {
        writer.endTag(TRANSITION_ELEMENT);
    }

    private void doStartDecisionState(IWebFlowModelElement element) {
        IDecisionState state = (IDecisionState) element;
        HashMap parameters = new HashMap();
        parameters.put(ID_ATTRIBUTE, state.getId());
        writer.startTag(DECISION_STATE_ELEMENT, parameters, true);
    }

    private void doStartIf(IWebFlowModelElement element) {
        IIf state = (IIf) element;
        HashMap parameters = new HashMap();
        parameters.put(TEST_ATTRIBUTE, state.getTest());
        parameters.put(THEN_ATTRIBUTE, state.getThenTransition().getToState()
                .getId());
        if (state.getElseTransition() != null) {
            parameters.put(ELSE_ATTRIBUTE, state.getElseTransition()
                    .getToState().getId());
        }
        writer.printTag(IF_ELEMENT, parameters, true, true, true);
    }

    private void doEndDecisionState(IWebFlowModelElement element) {
        writer.endTag(DECISION_STATE_ELEMENT);
    }

    private void doEndIf(IWebFlowModelElement element) {

    }

    public void close() throws Exception {
        writer.flush();
        String output = tempStream.toString();
        writer.close();

        StringReader reader = new StringReader(output);

        InputSource inputSource = new InputSource(reader);
        DOMParser parser = new DOMParser();
        parser.setFeature("http://xml.org/sax/features/validation", false);
        parser.setFeature("http://apache.org/xml/features/validation/dynamic",
                false);
        parser.setEntityResolver(new WebFlowDtdResolver());
        parser.parse(inputSource);

        Document doc = parser.getDocument();

        OutputFormat format = new OutputFormat(doc);
        format.setLineWidth(80);
        format.setIndenting(true);
        format.setIndent(4);
        format.setEncoding("UTF-8");
        format.setPreserveEmptyAttributes(false);
        XMLSerializer serializer = new XMLSerializer(stream, format);
        serializer.serialize(doc);

        stream.flush();
        stream.close();
    }

}