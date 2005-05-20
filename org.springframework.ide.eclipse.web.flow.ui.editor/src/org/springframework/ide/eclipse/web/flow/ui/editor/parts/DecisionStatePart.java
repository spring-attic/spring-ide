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
package org.springframework.ide.eclipse.web.flow.ui.editor.parts;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.graph.CompoundDirectedGraph;
import org.eclipse.draw2d.graph.Edge;
import org.eclipse.draw2d.graph.Node;
import org.springframework.ide.eclipse.web.flow.core.model.IDecisionState;
import org.springframework.ide.eclipse.web.flow.core.model.IIf;

public class DecisionStatePart extends ChildrenStatePart {

    protected void applyChildrenResults(CompoundDirectedGraph graph, Map map) {
        for (int i = 0; i < getChildren().size(); i++) {
            AbstractStatePart part = (AbstractStatePart) getChildren().get(i);
            //part.applyGraphResults(graph, map);
            Node n = (Node) map.get(part);
            Dimension dim = part.getFigure().getPreferredSize();
            part.getFigure().setBounds(
                    new Rectangle(n.x, n.y, n.width, dim.height));
        }
    }

    public void contributeEdgesToGraph(CompoundDirectedGraph graph, Map map) {
        List outgoing = getSourceConnections();
        for (int i = 0; i < outgoing.size(); i++) {
            StateTransitionPart part = (StateTransitionPart) getSourceConnections()
                    .get(i);
            part.contributeToGraph(graph, map);
        }

        Iterator iter = ((IDecisionState) getModel()).getIfs().iterator();
        int j = 0;
        while (iter.hasNext()) {
            IIf theIf = (IIf) iter.next();
            if (theIf.getThenTransition() != null) {
                // insert dummy edges for layouting
                Edge e1 = new Edge((Node) map.get(this), getNode(theIf
                        .getThenTransition().getToState(), map));

                //e1.weight = 5;
                graph.edges.add(e1);
                map.put(this.toString() + (j++), e1);
            }
            if (theIf.getElseTransition() != null) {
                Edge e2 = new Edge((Node) map.get(this), getNode(theIf
                        .getElseTransition().getToState(), map));
                //e2.weight = 5;
                graph.edges.add(e2);
                map.put(this.toString() + (j++), e2);
            }

        }

        for (int i = 0; i < getChildren().size(); i++) {
            AbstractStatePart child = (AbstractStatePart) children.get(i);
            child.contributeEdgesToGraph(graph, map);
        }
    }

    protected List getModelChildren() {
        if (getModel() instanceof IDecisionState) {
            return ((IDecisionState) getState()).getIfs();
        }
        else {
            return Collections.EMPTY_LIST;
        }
    }

    private Node getNode(Object model, Map map) {
        Node node = null;
        Iterator iter = map.values().iterator();
        while (iter.hasNext()) {
            Object obj = (Object) iter.next();
            if (obj instanceof Node) {
                Node tempNode = (Node) obj;
                if (model
                        .equals(((AbstractStatePart) tempNode.data).getModel())) {
                    node = tempNode;
                    break;
                }
            }
        }
        return node;
    }
}