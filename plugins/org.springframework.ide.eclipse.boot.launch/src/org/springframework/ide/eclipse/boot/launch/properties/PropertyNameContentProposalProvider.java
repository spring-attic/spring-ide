/*******************************************************************************
 * Copyright (c) 2015 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.launch.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesCompletionEngine;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

/**
 * @author Kris De Volder
 */
public class PropertyNameContentProposalProvider implements IContentProposalProvider {

	private static final IContentProposal[] NO_PROPOSALS = new IContentProposal[0];

	private SpringPropertiesCompletionEngine _engine;
	final private LiveExpression<IProject> project;

	public PropertyNameContentProposalProvider(LiveExpression<IProject> project) {
		this.project = project;
		project.addListener(new ValueListener<IProject>() {
			public void gotValue(LiveExpression<IProject> exp, IProject value) {
				_engine = null;
			}
		});
	}

	@Override
	public IContentProposal[] getProposals(String contents, int position) {
		SpringPropertiesCompletionEngine engine = getEngine();
		if (engine!=null) {
			return engine.getPropertyFieldProposals(contents, position);
		}
		return NO_PROPOSALS;
	}

	private SpringPropertiesCompletionEngine getEngine() {
		try {
			if (_engine==null) {
				IJavaProject jp = getJavaProject();
				if (jp!=null) {
					_engine = new SpringPropertiesCompletionEngine(jp);
				}
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return _engine;
	}

	private IJavaProject getJavaProject() {
		try {
			IProject p = project.getValue();
			if (p!=null && p.isAccessible() && p.hasNature(JavaCore.NATURE_ID)) {
				return JavaCore.create(p);
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return null;
	}

}
