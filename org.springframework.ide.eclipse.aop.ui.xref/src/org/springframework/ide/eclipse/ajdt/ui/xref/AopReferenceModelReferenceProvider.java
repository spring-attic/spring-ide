/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.ajdt.ui.xref;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.contribution.xref.core.IXReference;
import org.eclipse.contribution.xref.core.IXReferenceProvider;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.springframework.ide.eclipse.aop.core.model.IAopReferenceModel;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class AopReferenceModelReferenceProvider implements IXReferenceProvider {

	@SuppressWarnings("unchecked")
	private static final Class[] CLASSES = new Class[] { IJavaElement.class };

	@SuppressWarnings("unchecked")
	public Class[] getClasses() {
		return CLASSES;
	}

	public IJavaElement[] getExtraChildren(IJavaElement je) {
		return null;
	}

	@SuppressWarnings("unchecked")
	public void setCheckedFilters(List l) {
		CrossReferenceViewPreferenceUtils.setCheckedFilters(l);
	}

	@SuppressWarnings("unchecked")
	public List getFilterCheckedList() {
		List checked = CrossReferenceViewPreferenceUtils.getFilterCheckedList();
		if (checked != null) {
			return checked;
		}
		// use defaults
		return getFilterDefaultList();
	}

	@SuppressWarnings("unchecked")
	public void setCheckedInplaceFilters(List l) {
		CrossReferenceViewPreferenceUtils.setCheckedInplaceFilters(l);
	}

	@SuppressWarnings("unchecked")
	public List getFilterCheckedInplaceList() {
		List checked = CrossReferenceViewPreferenceUtils
				.getFilterCheckedInplaceList();
		if (checked != null) {
			return checked;
		}
		// use defaults
		return getFilterDefaultList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.contribution.xref.core.IXReferenceProvider#getFilterList()
	 */
	@SuppressWarnings("unchecked")
	public List getFilterList() {
		List<String> populatingList = new ArrayList<String>();
		populatingList.add("advises");
		populatingList.add("advised by");
		populatingList.add("declared on");
		populatingList.add("aspect declarations");
		return populatingList;
	}

	/*
	 * Returns the List of items to be filtered from the view by default.
	 * 
	 * @see org.eclipse.contribution.xref.core.IXReferenceProvider#getFilterDefaultList()
	 */
	@SuppressWarnings("unchecked")
	public List getFilterDefaultList() {
		List defaultFilterList = new ArrayList();
		return defaultFilterList;
	}

	public String getProviderDescription() {
		return "Provides Spring crosscutting structure references";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.contribution.xref.core.IXReferenceProvider#getXReferences(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public Collection<XRef> getXReferences(Object o, List checkedRelNames) {
		if (!(o instanceof IJavaElement))
			return Collections.EMPTY_SET;

		IJavaElement je = (IJavaElement) o;
		List<XRef> xrefs = new ArrayList<XRef>();

		IAopReferenceModel model = org.springframework.ide.eclipse.aop.core.Activator
				.getModel();
		List<IAopReference> references = model.getAllReferences();
		Map<IMember, XRef> refsAdvised = new HashMap<IMember, XRef>();
		Map<IMember, XRef> refsAdvises = new HashMap<IMember, XRef>();
		Map<IMember, XRef> refsDeclaredOn = new HashMap<IMember, XRef>();
		Map<IMember, XRef> refsAspectDeclarations = new HashMap<IMember, XRef>();
		
		for (IAopReference reference : references) {
			if (reference.getAdviceType() != ADVICE_TYPES.DECLARE_PARENTS) {
				if (checkFilter(checkedRelNames, "advises")
						&& reference.getSource() != null
						&& reference.getSource().equals(je)) {
					XRef ref = null;
					if (refsDeclaredOn.containsKey(reference.getSource())) {
						ref = refsDeclaredOn.get(reference.getSource());
					}
					else {
						ref = new XRef("advises",
								new HashSet<AopReferenceModelNode>());
						refsDeclaredOn.put(reference.getSource(), ref);
						xrefs.add(ref);
					}
					AopReferenceModelNode associate = new AopReferenceModelNode(
							AopReferenceModelNode.TYPE.TARGET, reference);
					if (!ref.getAssociatesList().contains(associate)) {
						ref.getAssociatesList().add(associate);
					}
				}
				else if (checkFilter(checkedRelNames, "advised by")
						&& reference.getTarget().equals(je)) {
					XRef ref = null;
					if (refsAspectDeclarations.containsKey(reference
							.getTarget())) {
						ref = refsAspectDeclarations.get(reference.getTarget());
					}
					else {
						ref = new XRef("advised by",
								new HashSet<AopReferenceModelNode>());
						refsAspectDeclarations.put(reference.getTarget(), ref);
						xrefs.add(ref);
					}
					AopReferenceModelNode associate = new AopReferenceModelNode(
							AopReferenceModelNode.TYPE.SOURCE, reference);
					if (!ref.getAssociatesList().contains(associate)) {
						ref.getAssociatesList().add(associate);
					}
				}
			}
			else {
				if (checkFilter(checkedRelNames, "declared on")
						&& reference.getSource() != null
						&& reference.getSource().equals(je)) {
					XRef ref = null;
					if (refsAdvises.containsKey(reference.getSource())) {
						ref = refsAdvises.get(reference.getSource());
					}
					else {
						ref = new XRef("declared on",
								new HashSet<AopReferenceModelNode>());
						refsAdvises.put(reference.getSource(), ref);
						xrefs.add(ref);
					}
					AopReferenceModelNode associate = new AopReferenceModelNode(
							AopReferenceModelNode.TYPE.TARGET, reference);
					if (!ref.getAssociatesList().contains(associate)) {
						ref.getAssociatesList().add(associate);
					}
				}
				else if (checkFilter(checkedRelNames, "aspect declarations")
						&& reference.getTarget().equals(je)
						&& reference.getSource() != null) {
					XRef ref = null;
					if (refsAdvised.containsKey(reference.getTarget())) {
						ref = refsAdvised.get(reference.getTarget());
					}
					else {
						ref = new XRef("aspect declarations",
								new HashSet<AopReferenceModelNode>());
						refsAdvised.put(reference.getTarget(), ref);
						xrefs.add(ref);
					}
					AopReferenceModelNode associate = new AopReferenceModelNode(
							AopReferenceModelNode.TYPE.SOURCE, reference);
					if (!ref.getAssociatesList().contains(associate)) {
						ref.getAssociatesList().add(associate);
					}
				}
			}
		}
		return xrefs;
	}

	@SuppressWarnings("unchecked")
	private boolean checkFilter(List checkedRelNames, String relName) {
		return checkedRelNames == null
				|| (checkedRelNames != null && !checkedRelNames
						.contains(relName));
	}

	private static class XRef implements IXReference {

		private String name;

		private Set<AopReferenceModelNode> associates;

		public XRef(String name, Set<AopReferenceModelNode> associates) {
			this.name = name;
			this.associates = associates;
		}

		public String getName() {
			return name;
		}

		public Iterator<AopReferenceModelNode> getAssociates() {
			return associates.iterator();
		}

		public Set<AopReferenceModelNode> getAssociatesList() {
			return associates;
		}
	}
}
