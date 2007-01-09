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
package org.springframework.ide.eclipse.aop.ui.visualiser;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.contribution.visualiser.core.ProviderManager;
import org.eclipse.contribution.visualiser.core.Stripe;
import org.eclipse.contribution.visualiser.interfaces.IMember;
import org.eclipse.contribution.visualiser.jdtImpl.JDTMember;
import org.eclipse.contribution.visualiser.simpleImpl.SimpleMarkupKind;
import org.eclipse.contribution.visualiser.simpleImpl.SimpleMarkupProvider;
import org.eclipse.contribution.visualiser.utils.JDTUtils;
import org.eclipse.contribution.visualiser.utils.MarkupUtils;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.aop.core.model.IAopModelChangedListener;
import org.springframework.ide.eclipse.aop.core.model.IAopProject;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;
import org.springframework.ide.eclipse.aop.ui.navigator.util.BeansAopNavigatorUtils;

/**
 * The Beans AOP Markup Provider
 */
public class BeansAopMarkupProvider
        extends SimpleMarkupProvider implements IAopModelChangedListener {

    // Cache: IMember -> List(Stripe)
    private static Hashtable<IMember, List<Stripe>> markupCache = 
        new Hashtable<IMember, List<Stripe>>();

    public static void resetCache() {
        markupCache.clear();
    }

    /**
     * Get a List of Stripes for the given member, which are its markups.
     */
    @SuppressWarnings("unchecked")
    public List<Stripe> getMemberMarkups(IMember member) {

        List<Stripe> cachedValue = markupCache.get(member);
        if (cachedValue != null) {
            return cachedValue;
        }
        List<Stripe> markupList = super.getMemberMarkups(member);
        if (markupList != null) {
            return markupList;
        }

        List<Stripe> stripeList = new ArrayList<Stripe>();
        if (ProviderManager.getContentProvider() instanceof BeansAopContentProvider) {
            IJavaProject jp = ((BeansAopContentProvider) ProviderManager
                    .getContentProvider()).getCurrentProject();

            if (jp != null) {
                IAopProject aopProject = org.springframework.ide.eclipse.aop.core.Activator
                        .getModel().getProject(jp.getProject());
                if (aopProject != null) {
                    List<IAopReference> references = aopProject
                            .getAllReferences();
                    if (references != null && references.size() > 0) {
                        for (IAopReference reference : references) {
                            IType advisedType = reference.getTarget()
                                    .getDeclaringType();
                            ICompilationUnit advisedCu = advisedType
                                    .getCompilationUnit();
                            if (member instanceof JDTMember) {
                                IJavaElement je = ((JDTMember) member)
                                        .getResource();
                                if (je.equals(advisedCu)) {
                                    String label = getText(reference);
                                    Stripe stripe = new Stripe(
                                            new SimpleMarkupKind(label),
                                            BeansAopNavigatorUtils
                                                    .getLineNumber(reference
                                                            .getTarget()) + 1);
                                    stripeList.add(stripe);
                                    addMarkup(member.getFullname(), stripe);
                                }
                            }
                        }
                    }
                }
            }
        }
        MarkupUtils.processStripes(stripeList);
        markupCache.put(member, stripeList);
        return stripeList;
    }

    /**
     * Get all the markup kinds - which in this case is the label for the last
     * run search (if it was a java search)
     * 
     * @return a Set of Strings
     */
    public SortedSet<SimpleMarkupKind> getAllMarkupKinds() {
        SortedSet<SimpleMarkupKind> kinds = new TreeSet<SimpleMarkupKind>();
        List<String> advices = new ArrayList<String>();
        if (ProviderManager.getContentProvider() instanceof BeansAopContentProvider) {
            IJavaProject jp = ((BeansAopContentProvider) ProviderManager
                    .getContentProvider()).getCurrentProject();
            if (jp != null) {
                IAopProject aopProject = org.springframework.ide.eclipse.aop.core.Activator
                        .getModel().getProject(jp.getProject());
                if (aopProject != null) {
                    List<IAopReference> references = aopProject
                            .getAllReferences();
                    if (references != null && references.size() > 0) {
                        for (IAopReference reference : references) {
                            String label = getText(reference);
                            if (!advices.contains(label)) {
                                kinds.add(new SimpleMarkupKind(label));
                                advices.add(label);
                            }
                        }
                    }
                }
            }
        }
        if (kinds.size() > 0) {
            return kinds;
        }
        return null;
    }

    /**
     * Process a mouse click on a stripe. This method opens the editor at the
     * line of the stripe clicked.
     * 
     * @see org.eclipse.contribution.visualiser.interfaces.IMarkupProvider#processMouseclick(org.eclipse.contribution.visualiser.interfaces.IMember,
     *      org.eclipse.contribution.visualiser.core.Stripe, int)
     */
    public boolean processMouseclick(IMember member, Stripe stripe,
            int buttonClicked) {
        if (buttonClicked == 1) {
            if (member instanceof JDTMember) {
                IJavaElement jEl = ((JDTMember) member).getResource();
                if (jEl != null) {
                    JDTUtils
                            .openInEditor(jEl.getResource(), stripe.getOffset());
                }
            }
            return false;
        }
        return true;
    }

    private String getText(IAopReference reference) {
        ADVICE_TYPES type = reference.getAdviceType();
        String text = "";
        if (type == ADVICE_TYPES.AFTER) {
            text += "after()";
        }
        else if (type == ADVICE_TYPES.AFTER_RETURNING) {
            text += "after-returning()";
        }
        else if (type == ADVICE_TYPES.AFTER_THROWING) {
            text += "after-throwing()";
        }
        else if (type == ADVICE_TYPES.BEFORE) {
            text += "before()";
        }
        else if (type == ADVICE_TYPES.AROUND) {
            text += "around()";
        }
        text += " <";
        text += reference.getDefinition().getAspectName();
        text += "> [";
        text += reference.getResource().getProjectRelativePath().toString();
        text += "]";
        return text;
    }

    public void changed() {
        resetCache();
    }
    
    /**
     * Activate the provider
     */
    public void activate() {
        Activator.getModel().registerAopModelChangedListener(this);
    }


    /**
     * Deactivate the provider
     */
    public void deactivate() {
        super.deactivate();
        Activator.getModel().unregisterAopModelChangedListener(this);
    }
}
