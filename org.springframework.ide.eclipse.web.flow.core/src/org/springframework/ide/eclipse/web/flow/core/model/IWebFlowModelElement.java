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

package org.springframework.ide.eclipse.web.flow.core.model;

import java.beans.PropertyChangeListener;

import org.eclipse.core.resources.IResource;

/**
 * Common protocol for all elements provided by the Web Flow model.
 */
public interface IWebFlowModelElement {

    /**
     * Constant representing a Web Flow project bean's constructor argument. A
     * Web Flow element with this type can be safely cast to
     * <code>ConstructorArgument</code>.
     */
    int ACTION = 7;

    int ACTION_STATE = 51;

    String ADD_CHILDREN = "add_children";

    int ATTRIBUTEMAPPER = 9;

    /**
     * Constant representing a Web Flow project's config. A Web Flow element with this
     * type can be safely cast to <code>Web FlowConfig</code>.
     */
    int CONFIG = 3;

    /**
     * Constant representing a Web Flow project's config set. A Web Flow element with
     * this type can be safely cast to <code>Web FlowConfigSet</code>.
     */
    int CONFIG_SET = 4;

    int DECISION_STATE = 55;

    int END_STATE = 54;

    int IF = 10;
    
    int INLINE_FLOW = 11;
    
    int INPUT = 12;
    
    int OUTPUT = 13;

    int IF_TRANSITION = 81;

    String INPUTS = "inputs";

    /**
     * Constant representing the Web Flow model (workspace level object). A Beans
     * element with this type can be safely cast to <code>BeansModel</code>.
     */
    int MODEL = 1;

    String MOVE_CHILDREN = "move_children";

    String OUTPUTS = "outputs";

    /**
     * Constant representing a Web Flow project. A Web Flow element with this type can
     * be safely cast to <code>Web FlowProject</code>.
     */
    int PROJECT = 2;

    /**
     * Constant representing a Web Flow project bean's property. A Web Flow element
     * with this type can be safely cast to <code>Property</code>.
     */
    int PROPERTY = 6;

    String PROPS = "properties";

    String REMOVE_CHILDREN = "remove_children";

    /**
     * Constant representing a Web Flow project bean's constructor argument. A
     * Web Flow element with this type can be safely cast to
     * <code>ConstructorArgument</code>.
     */
    int STATE_TRANSITION = 80;

    int SUBFLOW_STATE = 53;

    int VIEW_STATE = 52;

    /**
     * Constant representing a Web Flow project's bean. A Web Flow element with this
     * type can be safely cast to <code>Bean</code>.
     */
    int WEBFLOW_STATE = 50;

    /**
     * Accepts the given visitor. The visitor's <code>visit</code> method is
     * called with this model element. If the visitor returns <code>true</code>,
     * this method visits this element's members.
     * 
     * @param visitor
     *            the visitor
     * @see IModelElementVisitor#visit(IModelElement)
     * @see #accept(IModelElementVisitor)
     */
    void accept(IModelElementVisitor visitor);

    void addPropertyChangeListener(PropertyChangeListener l);

    /**
     * Returns the line number with the logical end of the element's source
     * code, or -1 if it's a one liner.
     * 
     * e.g.
     * 
     * <pre>
     * 
     *  (1) &lt;bean class=&quot;foo&quot; id=&quot;bar&quot;&gt;
     *  (2)    &lt;property name=&quot;fred&quot;&gt;&lt;value&gt;3&lt;/value&gt;&lt;/property&gt;
     *  (3) &lt;/bean&gt; 
     *  would return 2 (line 3 is not counted as it's just closing)
     *   
     *  (1) &lt;bean class=&quot;foo&quot; id=&quot;bar&quot;/&gt;
     *  would return -1 (one liner)
     *  
     *  (1) &lt;bean class=&quot;foo&quot; id=&quot;bar&quot;&gt;
     *  (2)    &lt;!-- comment --&gt;
     *  (3) &lt;/bean&gt;
     *  would also return -1 as there's nothing logically 'interesting' in the tag
     *  
     *  
     * </pre>
     * 
     * @return line number with start of element's source code
     */
    int getElementEndLine();

    /**
     * Returns the name of this element.
     * 
     * @return the element's name
     */
    String getElementName();

    /**
     * Returns the element directly containing this element, or
     * <code>null</code> if this element has no parent.
     * 
     * @return the parent element, or <code>null</code> if this element has no
     *         parent
     */
    IWebFlowModelElement getElementParent();

    /**
     * Returns the resource of the innermost resource enclosing this element.
     * 
     * @return the resource of the innermost resource enclosing this element
     */
    IResource getElementResource();

    /**
     * Returns the line number with the start of the element's source code.
     * 
     * @return line number with start of element's source code
     */
    int getElementStartLine();

    /**
     * Returns this element's kind encoded as an integer. This is a handle-only
     * method.
     * 
     * @return the kind of element; one of the constants declared in
     *         <code>IWebFlowModelElement</code>
     * @see IWebFlowModelElement
     */
    int getElementType();

    void removePropertyChangeListener(PropertyChangeListener l);

    void setElementParent(IWebFlowModelElement parent);
}