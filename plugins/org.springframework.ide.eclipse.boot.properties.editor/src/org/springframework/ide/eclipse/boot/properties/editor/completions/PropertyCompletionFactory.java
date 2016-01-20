/*******************************************************************************
 * Copyright (c) 2014-2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.completions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.springframework.ide.eclipse.boot.properties.editor.DocumentContextFinder;
import org.springframework.ide.eclipse.boot.properties.editor.FuzzyMap.Match;
import org.springframework.ide.eclipse.boot.properties.editor.PropertyInfo;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertyHoverInfo;
import org.springframework.ide.eclipse.boot.properties.editor.util.Type;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeParser;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypedProperty;
import org.springframework.ide.eclipse.editor.support.completions.CompletionFactory;
import org.springframework.ide.eclipse.editor.support.completions.ProposalApplier;
import org.springframework.ide.eclipse.editor.support.hover.HoverInfo;
import org.springframework.ide.eclipse.editor.support.yaml.completions.AbstractPropertyProposal;
import org.springframework.ide.eclipse.editor.support.yaml.schema.YType;

public class PropertyCompletionFactory extends CompletionFactory {

	public ScoreableProposal property(IDocument doc, ProposalApplier applier, Match<PropertyInfo> prop, TypeUtil typeUtil) {
		return new PropertyProposal(doc, applier, prop, typeUtil);
	}

	public ScoreableProposal valueProposal(String value, Type type, int order, ProposalApplier applier) {
		return valueProposal(value, type, -(1.0+order), applier);
	}

	public ScoreableProposal valueProposal(String value, Type type, double score, ProposalApplier applier) {
		return simpleProposal(value, score, applier);
	}

	public ScoreableProposal beanProperty(IDocument doc, final String contextProperty, final Type contextType, final String pattern, final TypedProperty p, final double score, ProposalApplier applier, final TypeUtil typeUtil) {
		return new AbstractPropertyProposal(doc, applier) {

			private HoverInfo hoverInfo;

			@Override
			public HoverInfo getAdditionalProposalInfo(IProgressMonitor monitor) {
				if (hoverInfo==null) {
					hoverInfo = new JavaTypeNavigationHoverInfo(contextProperty+"."+p.getName(), p.getName(), contextType, p.getType(), typeUtil);
				}
				return hoverInfo;
			}

			@Override
			protected String getBaseDisplayString() {
				return p.getName();
			}

			@Override
			protected String getHighlightPattern() {
				return pattern;
			}

			@Override
			protected Type getType() {
				return p.getType();
			}

			@Override
			public double getBaseScore() {
				return score;
			}

			@Override
			protected String niceTypeName(YType type) {
				return typeUtil.niceTypeName((Type) type);
			}
		};
	}

	private DocumentContextFinder documentContextFinder;

	public PropertyCompletionFactory(DocumentContextFinder documentContextFinder) {
		this.documentContextFinder = documentContextFinder;
	}

	private class PropertyProposal extends AbstractPropertyProposal {
		private Match<PropertyInfo> match;
		private Type type;
		private TypeUtil typeUtil;

		public PropertyProposal(IDocument doc, ProposalApplier applier, Match<PropertyInfo> match,
				TypeUtil typeUtil) {
			super(doc, applier);
			this.typeUtil = typeUtil;
			this.match = match;
		}

		@Override
		public HoverInfo getAdditionalProposalInfo(IProgressMonitor monitor) {
			return new SpringPropertyHoverInfo(documentContextFinder.getJavaProject(fDoc), match.data);
		}

		@Override
		protected String getBaseDisplayString() {
			return match.data.getId();
		}

		@Override
		public double getBaseScore() {
			return match.score;
		}

		@Override
		protected Type getType() {
			if (type==null) {
				type = TypeParser.parse(match.data.getType());
			}
			return type;
		}

		@Override
		protected String getHighlightPattern() {
			return match.getPattern();
		}

		@Override
		protected String niceTypeName(YType type) {
			return typeUtil.niceTypeName(((Type)type));
		}
	}

}
