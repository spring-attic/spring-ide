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
package org.springframework.ide.eclipse.aop.ui.xref;

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
import org.springframework.ide.eclipse.aop.core.model.IAopModel;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;

public class BeansAopReferenceProvider implements IXReferenceProvider {

    private static final Class[] CLASSES = new Class[] { IJavaElement.class };

    public Class[] getClasses() {
        return CLASSES;
    }

    public IJavaElement[] getExtraChildren(IJavaElement je) {
        return null;
    }

    public void setCheckedFilters(List l) {
        BeansAopPreferenceUtils.setCheckedFilters(l);
    }

    public List getFilterCheckedList() {
        List checked = BeansAopPreferenceUtils.getFilterCheckedList();
        if (checked != null) {
            return checked;
        }
        // use defaults
        return getFilterDefaultList();
    }

    public void setCheckedInplaceFilters(List l) {
        BeansAopPreferenceUtils.setCheckedInplaceFilters(l);
    }

    public List getFilterCheckedInplaceList() {
        List checked = BeansAopPreferenceUtils.getFilterCheckedInplaceList();
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

        IAopModel model = org.springframework.ide.eclipse.aop.core.Activator.getModel();
        List<IAopReference> references = model.getAllReferences(je.getJavaProject());
        Map<IMember, XRef> refsAdvised = new HashMap<IMember, XRef>();
        Map<IMember, XRef> refsAdvises = new HashMap<IMember, XRef>();
        Map<IMember, XRef> refsDeclaredOn = new HashMap<IMember, XRef>();
        Map<IMember, XRef> refsAspectDeclarations = new HashMap<IMember, XRef>();
        for (IAopReference reference : references) {
            if (reference.getAdviceType() != ADVICE_TYPES.DECLARE_PARENTS) {
                if (checkFilter(checkedRelNames, "advises") && reference.getSource() != null
                        && reference.getSource().equals(je)) {
                    XRef ref = null;
                    if (refsDeclaredOn.containsKey(reference.getSource())) {
                        ref = refsDeclaredOn.get(reference.getSource());
                    }
                    else {
                        ref = new XRef("advises", new HashSet<BeansAopNode>());
                        refsDeclaredOn.put(reference.getSource(), ref);
                        xrefs.add(ref);
                    }
                    BeansAopNode associate = new BeansAopNode(BeansAopNode.TYPE.TARGET, reference);
                    if (!ref.getAssociatesList().contains(associate)) {
                        ref.getAssociatesList().add(associate);
                    }
                }
                else if (checkFilter(checkedRelNames, "advised by")
                        && reference.getTarget().equals(je)) {
                    XRef ref = null;
                    if (refsAspectDeclarations.containsKey(reference.getTarget())) {
                        ref = refsAspectDeclarations.get(reference.getTarget());
                    }
                    else {
                        ref = new XRef("advised by", new HashSet<BeansAopNode>());
                        refsAspectDeclarations.put(reference.getTarget(), ref);
                        xrefs.add(ref);
                    }
                    BeansAopNode associate = new BeansAopNode(BeansAopNode.TYPE.SOURCE, reference);
                    if (!ref.getAssociatesList().contains(associate)) {
                        ref.getAssociatesList().add(associate);
                    }
                }
            }
            else {
                if (checkFilter(checkedRelNames, "declared on") && reference.getSource() != null
                        && reference.getSource().equals(je)) {
                    XRef ref = null;
                    if (refsAdvises.containsKey(reference.getSource())) {
                        ref = refsAdvises.get(reference.getSource());
                    }
                    else {
                        ref = new XRef("declared on", new HashSet<BeansAopNode>());
                        refsAdvises.put(reference.getSource(), ref);
                        xrefs.add(ref);
                    }
                    BeansAopNode associate = new BeansAopNode(BeansAopNode.TYPE.TARGET, reference);
                    if (!ref.getAssociatesList().contains(associate)) {
                        ref.getAssociatesList().add(associate);
                    }
                }
                else if (checkFilter(checkedRelNames, "aspect declarations")
                        && reference.getTarget().equals(je) && reference.getSource() != null) {
                    XRef ref = null;
                    if (refsAdvised.containsKey(reference.getTarget())) {
                        ref = refsAdvised.get(reference.getTarget());
                    }
                    else {
                        ref = new XRef("aspect declarations", new HashSet<BeansAopNode>());
                        refsAdvised.put(reference.getTarget(), ref);
                        xrefs.add(ref);
                    }
                    BeansAopNode associate = new BeansAopNode(BeansAopNode.TYPE.SOURCE, reference);
                    if (!ref.getAssociatesList().contains(associate)) {
                        ref.getAssociatesList().add(associate);
                    }
                }
            }
        }
        return xrefs;
    }

    private boolean checkFilter(List checkedRelNames, String relName) {
        return checkedRelNames == null
                || (checkedRelNames != null && !checkedRelNames.contains(relName));
    }

    private static class XRef implements IXReference {

        private String name;

        private Set<BeansAopNode> associates;

        public XRef(String name, Set<BeansAopNode> associates) {
            this.name = name;
            this.associates = associates;
        }

        public String getName() {
            return name;
        }

        public Iterator<BeansAopNode> getAssociates() {
            return associates.iterator();
        }

        public Set<BeansAopNode> getAssociatesList() {
            return associates;
        }
    }
}
