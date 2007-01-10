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
import org.springframework.ide.eclipse.aop.core.model.IAopProject;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;

public class BeansAopReferenceProvider implements IXReferenceProvider {

    private static final Class[] CLASSES = new Class[] { IJavaElement.class };

    public Class[] getClasses() {
        return CLASSES;
    }

    public IJavaElement[] getExtraChildren(IJavaElement je) {
        return null;
    }

    public List getFilterCheckedInplaceList() {
        return null;
    }

    public List getFilterCheckedList() {
        return null;
    }

    public List getFilterDefaultList() {
        return null;
    }

    public List getFilterList() {
        return null;
    }

    public String getProviderDescription() {
        return null;
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

        IAopModel model = org.springframework.ide.eclipse.aop.core.Activator
                .getModel();
        IAopProject project = model
                .getProject(je.getJavaProject().getProject());
        if (project != null) {
            List<IAopReference> references = project.getAllReferences();
            Map<IMember, XRef> refs = new HashMap<IMember, XRef>();
            for (IAopReference reference : references) {
                if (reference.getSource() != null
                        && reference.getSource().equals(je)) {
                    XRef ref = null;
                    if (refs.containsKey(reference.getSource())) {
                        ref = refs.get(reference.getSource());
                    }
                    else {
                        ref = new XRef("advises", new HashSet<BeansAopNode>());
                        xrefs.add(ref);
                    }
                    refs.put(reference.getSource(), ref);
                    BeansAopNode associate = new BeansAopNode(
                            BeansAopNode.TYPE.TARGET, reference);
                    if (!ref.getAssociatesList().contains(associate)) {
                        ref.getAssociatesList().add(associate);
                    }
                }
                else if (reference.getTarget().equals(je)) {
                    XRef ref = null;
                    if (refs.containsKey(reference.getTarget())) {
                        ref = refs.get(reference.getTarget());
                    }
                    else {
                        ref = new XRef("advised by",
                                new HashSet<BeansAopNode>());
                        xrefs.add(ref);
                    }
                    refs.put(reference.getTarget(), ref);
                    BeansAopNode associate = new BeansAopNode(
                            BeansAopNode.TYPE.SOURCE, reference);
                    if (!ref.getAssociatesList().contains(associate)) {
                        ref.getAssociatesList().add(associate);
                    }
                }
            }
        }
        return xrefs;
    }

    public void setCheckedFilters(List l) {
    }

    public void setCheckedInplaceFilters(List l) {
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
