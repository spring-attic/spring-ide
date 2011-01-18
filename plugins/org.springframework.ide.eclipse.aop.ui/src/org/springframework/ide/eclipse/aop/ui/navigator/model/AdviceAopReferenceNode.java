/*******************************************************************************
 * Copyright (c) 2006, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.ui.navigator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;

/**
 * @author Christian Dupuis
 */
public class AdviceAopReferenceNode implements IReferenceNode {

	private List<IAopReference> references;

	public AdviceAopReferenceNode(List<IAopReference> reference) {
		this.references = reference;
	}

	public IReferenceNode[] getChildren() {
		List<IReferenceNode> nodes = new ArrayList<IReferenceNode>();
		Map<String, List<IAopReference>> refs = new HashMap<String, List<IAopReference>>();
		for (IAopReference r : this.references) {
			if (refs.containsKey(r.getTargetBeanId())) {
				refs.get(r.getTargetBeanId()).add(r);
			}
			else {
				List<IAopReference> ref = new ArrayList<IAopReference>();
				ref.add(r);
				refs.put(r.getTargetBeanId(), ref);
			}
		}
		for (Map.Entry<String, List<IAopReference>> entry : refs.entrySet()) {
			nodes.add(new AdviceAopTargetBeanNode(entry.getValue()));
		}

		Collections.sort(nodes, new Comparator<IReferenceNode>() {

			public int compare(IReferenceNode e1, IReferenceNode e2) {
				if (e1 instanceof IRevealableReferenceNode
						&& e2 instanceof IRevealableReferenceNode) {
					IRevealableReferenceNode ref1 = (IRevealableReferenceNode) e1;
					IRevealableReferenceNode ref2 = (IRevealableReferenceNode) e2;
					return ref1.getResource().getFullPath().toString().compareTo(
							ref2.getResource().getFullPath().toString());
				}
				return 0;
			}
		});

		return nodes.toArray(new IReferenceNode[nodes.size()]);
	}

	public Image getImage() {
		return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_REFERENCE);
	}

	public String getText() {
		return "advises";
	}

	public boolean hasChildren() {
		return true;
	}
	
	public Object getReferenceParticipant() {
		return null;
	}

}
