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
package org.springframework.ide.eclipse.ajdt.ui.visualiser;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.contribution.visualiser.core.ProviderManager;
import org.eclipse.contribution.visualiser.core.Stripe;
import org.eclipse.contribution.visualiser.interfaces.IMember;
import org.eclipse.contribution.visualiser.jdtImpl.JDTContentProvider;
import org.eclipse.contribution.visualiser.jdtImpl.JDTMember;
import org.eclipse.contribution.visualiser.simpleImpl.SimpleMarkupKind;
import org.eclipse.contribution.visualiser.simpleImpl.SimpleMarkupProvider;
import org.eclipse.contribution.visualiser.utils.JDTUtils;
import org.eclipse.contribution.visualiser.utils.MarkupUtils;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.aop.core.internal.model.AopReferenceModel;
import org.springframework.ide.eclipse.aop.core.model.IAopModelChangedListener;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IIntroductionDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;
import org.springframework.ide.eclipse.aop.ui.navigator.util.AopReferenceModelNavigatorUtils;

/**
 * Implementation of AJDT's {@link SimpleMarkupProvider} that contributes
 * elements from the {@link AopReferenceModel}.
 * @author Christian Dupuis
 * @since 2.0
 */
public class AopReferenceModelMarkupProvider extends SimpleMarkupProvider
		implements IAopModelChangedListener {

	// Cache: IMember -> List(Stripe)
	private static Hashtable<IMember, List<Stripe>> markupCache = new Hashtable<IMember, List<Stripe>>();

	public static void resetCache() {
		markupCache.clear();
	}

	/**
	 * Get a List of Stripes for the given member, which are its markups.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Stripe> getMemberMarkups(IMember member) {

		List<Stripe> cachedValue = markupCache.get(member);
		if (cachedValue != null) {
			return cachedValue;
		}

		List<Stripe> stripeList = new ArrayList<Stripe>();
		if (ProviderManager.getContentProvider() instanceof JDTContentProvider) {
			List<IAopReference> references = org.springframework.ide.eclipse.aop.core.Activator
					.getModel().getAllReferences();
			if (references != null && references.size() > 0) {
				for (IAopReference reference : references) {
					IType advisedType = null;
					if (reference.getTarget() instanceof IType) {
						advisedType = (IType) reference.getTarget();
					}
					else {
						advisedType = reference.getTarget().getDeclaringType();
					}
					ICompilationUnit advisedCu = advisedType
							.getCompilationUnit();
					if (member instanceof JDTMember) {
						IJavaElement je = ((JDTMember) member).getResource();
						if (je.equals(advisedCu)) {
							String label = getText(reference);
							Stripe stripe = new Stripe(new SimpleMarkupKind(
									label), AopReferenceModelNavigatorUtils
									.getLineNumber(reference.getTarget()) + 1);
							stripeList.add(stripe);
							addMarkup(member.getFullname(), stripe);
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
	 * Get all the markup kinds
	 * @return a Set of Strings
	 */
	@Override
	public SortedSet<SimpleMarkupKind> getAllMarkupKinds() {
		SortedSet<SimpleMarkupKind> kinds = new TreeSet<SimpleMarkupKind>();
		List<String> advices = new ArrayList<String>();
		if (ProviderManager.getContentProvider() instanceof JDTContentProvider) {
			List<IAopReference> references = Activator.getModel()
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
		if (kinds.size() > 0) {
			return kinds;
		}
		return null;
	}

	/**
	 * Process a mouse click on a stripe. This method opens the editor at the
	 * line of the stripe clicked.
	 * @see org.eclipse.contribution.visualiser.interfaces.IMarkupProvider#processMouseclick(org.eclipse.contribution.visualiser.interfaces.IMember,
	 * org.eclipse.contribution.visualiser.core.Stripe, int)
	 */
	@Override
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
		else if (type == ADVICE_TYPES.DECLARE_PARENTS) {
			text += "declare parents:";
			text += " implements "
					+ ((IIntroductionDefinition) reference.getDefinition())
							.getImplInterfaceName();
		}
		text += " <";
		text += reference.getDefinition().getAspectName();
		text += "> [";
		text += reference.getDefinition().getResource()
				.getProjectRelativePath().toString();
		text += "]";
		return text;
	}

	public void changed() {
		resetCache();
	}

	/**
	 * Activate the provider
	 */
	@Override
	public void activate() {
		Activator.getModel().registerAopModelChangedListener(this);
	}

	/**
	 * Deactivate the provider
	 */
	@Override
	public void deactivate() {
		super.deactivate();
		Activator.getModel().unregisterAopModelChangedListener(this);
	}
}
