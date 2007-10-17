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
package org.springframework.ide.eclipse.webflow.ui.graph.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelProvider;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.webflow.core.internal.model.Action;
import org.springframework.ide.eclipse.webflow.core.internal.model.BeanAction;
import org.springframework.ide.eclipse.webflow.core.internal.model.EvaluateAction;
import org.springframework.ide.eclipse.webflow.core.internal.model.ExceptionHandler;
import org.springframework.ide.eclipse.webflow.core.internal.model.InputAttribute;
import org.springframework.ide.eclipse.webflow.core.internal.model.OutputAttribute;
import org.springframework.ide.eclipse.webflow.core.internal.model.Set;
import org.springframework.ide.eclipse.webflow.core.model.IActionState;
import org.springframework.ide.eclipse.webflow.core.model.IDecisionState;
import org.springframework.ide.eclipse.webflow.core.model.IEndState;
import org.springframework.ide.eclipse.webflow.core.model.IIf;
import org.springframework.ide.eclipse.webflow.core.model.IInputAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IMapping;
import org.springframework.ide.eclipse.webflow.core.model.IOutputAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IStateTransition;
import org.springframework.ide.eclipse.webflow.core.model.ISubflowState;
import org.springframework.ide.eclipse.webflow.core.model.IViewState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.springframework.ide.eclipse.webflow.ui.editor.outline.webflow.WebflowUIImages;
import org.springframework.ide.eclipse.webflow.ui.graph.Activator;
import org.springframework.ide.eclipse.webflow.ui.graph.WebflowUtils;

/**
 * 
 */
@SuppressWarnings("restriction")
public class DialogUtils {

	public static int openPropertiesDialog(final IWebflowModelElement parent,
			final IWebflowModelElement element, final boolean newMode) {
		return openPropertiesDialog(parent, element, newMode, 0);
	}

	/**
	 * @param element
	 * @param newMode
	 * @param parent
	 * @return
	 */
	public static int openPropertiesDialog(final IWebflowModelElement parent,
			final IWebflowModelElement element, final boolean newMode,
			final int index) {
		final Integer[] result = new Integer[1];
		final Shell shell = getShell();

		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				Dialog dialog = null;
				if (element instanceof IEndState) {
					dialog = new EndStatePropertiesDialog(shell, parent,
							(IEndState) element);
				}
				else if (element instanceof IViewState) {
					dialog = new ViewStatePropertiesDialog(shell, parent,
							(IViewState) element);
				}
				else if (element instanceof ISubflowState) {
					dialog = new SubFlowStatePropertiesDialog(shell, parent,
							(ISubflowState) element, index);
				}
				else if (element instanceof IActionState) {
					dialog = new ActionStatePropertiesDialog(shell, parent,
							(IActionState) element);
				}
				else if (element instanceof Action) {
					dialog = new ActionPropertiesDialog(shell, parent,
							(Action) element);
				}
				else if (element instanceof BeanAction) {
					dialog = new BeanActionPropertiesDialog(shell, parent,
							(BeanAction) element);
				}
				else if (element instanceof EvaluateAction) {
					dialog = new EvaluateActionPropertiesDialog(shell, parent,
							(EvaluateAction) element);
				}
				else if (element instanceof Set) {
					dialog = new SetActionPropertiesDialog(shell, parent,
							(Set) element);
				}
				else if (element instanceof OutputAttribute) {
					dialog = new InputAttributeEditorDialog(shell,
							(IOutputAttribute) element);
				}
				else if (element instanceof InputAttribute) {
					dialog = new InputAttributeEditorDialog(shell,
							(IInputAttribute) element);
				}
				else if (element instanceof IMapping) {
					dialog = new MappingEditorDialog(shell, (IMapping) element);
				}
				else if (element instanceof ExceptionHandler) {
					dialog = new ExceptionHandlerPropertiesDialog(shell,
							parent, (ExceptionHandler) element);
				}
				else if (element instanceof IStateTransition) {
					dialog = new StateTransitionPropertiesDialog(shell, parent,
							(IStateTransition) element);
				}
				else if (element instanceof IDecisionState) {
					dialog = new DecisionStatePropertiesDialog(shell, parent,
							(IDecisionState) element);
				}
				else if (element instanceof IIf) {
					dialog = new IfPropertiesDialog(shell,
							(IDecisionState) parent, (IIf) element, newMode);
				}
				else if (element instanceof IWebflowState) {
					dialog = new WebflowStatePropertiesDialog(shell,
							(IWebflowState) element);
				}

				if (dialog != null) {
					dialog.setBlockOnOpen(true);
					result[0] = dialog.open();
				}
			}
		});
		return result[0];
	}

	private static Shell getShell() {
		Shell shell = Activator.getDefault().getWorkbench()
				.getActiveWorkbenchWindow().getShell();
		return shell;
	}

	public static ElementListSelectionDialog openBeanReferenceDialog(
			String beanId, boolean filter) {
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(
				getShell(), new BeansModelLabelProvider(true));
		dialog.setBlockOnOpen(true);

		dialog.setSize(100, 20);
		if (filter) {
			dialog.setFilter("*" + beanId + "*");
		}
		dialog.setElements(WebflowUtils.getBeansFromEditorInput().toArray());
		dialog.setEmptySelectionMessage("Select a bean reference");
		dialog.setTitle("Bean reference");
		dialog.setMessage("Please select a bean reference");
		dialog.setMultipleSelection(false);
		return dialog;
	}

	public static ElementListSelectionDialog openActionMethodReferenceDialog(
			IDOMNode node) {
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(
				getShell(), new JavaElementLabelProvider());
		dialog.setBlockOnOpen(true);
		dialog.setSize(100, 20);
		dialog.setElements(WebflowUtils.getActionMethods(node).toArray());
		dialog.setEmptySelectionMessage("Select a action method");
		dialog.setTitle("Action method reference");
		dialog.setMessage("Please select a action method");
		dialog.setMultipleSelection(false);
		return dialog;
	}

	public static ElementListSelectionDialog openFlowReferenceDialog() {
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(
				getShell(), new LabelProvider() {
					public Image getImage(Object obj) {
						return WebflowUIImages
								.getImage(WebflowUIImages.IMG_OBJS_WEBFLOW);
					}
				});
		dialog.setBlockOnOpen(true);
		dialog.setSize(100, 20);
		dialog.setElements(WebflowUtils.getWebflowConfigNames());
		dialog.setEmptySelectionMessage("Select a flow reference");
		dialog.setTitle("Flow reference");
		dialog.setMessage("Please select a flow");
		dialog.setMultipleSelection(false);
		return dialog;
	}

	public static void attachContentAssist(Text text, Object[] elements) {
		try {
			char[] autoActivationCharacters = new char[0];
			KeyStroke keyStroke = KeyStroke.getInstance("Ctrl+Space");
			new ContentProposalAdapter(text, new TextContentAdapter(),
					new SimpleContentProposalProvider(elements), keyStroke,
					autoActivationCharacters);
		}
		catch (ParseException e1) {
		}
	}

	private static class SimpleContentProposalProvider implements
			IContentProposalProvider {

		/*
		 * The proposals provided.
		 */
		private String[] proposals;

		/**
		 * Construct a SimpleContentProposalProvider whose content proposals are
		 * always the specified array of Objects.
		 * @param proposals the array of Strings to be returned whenever
		 * proposals are requested.
		 */
		public SimpleContentProposalProvider(String[] proposals) {
			Arrays.sort(proposals);
			this.proposals = proposals;
		}

		/**
		 * Construct a SimpleContentProposalProvider whose content proposals are
		 * always the specified array of Objects.
		 * @param proposals the array of Strings to be returned whenever
		 * proposals are requested.
		 */
		public SimpleContentProposalProvider(Object[] proposals) {

			if (proposals != null && proposals.length > 0) {
				List<String> strings = new ArrayList<String>();
				for (Object obj : proposals) {
					if (obj instanceof IModelElement) {
						strings.add(((IModelElement) obj).getElementName());
					}
					else if (obj instanceof IMethod) {
						strings.add(((IMethod) obj).getElementName());
					}
				}
				this.proposals = strings.toArray(new String[strings.size()]);
				Arrays.sort(this.proposals);
			}
			else {
				this.proposals = new String[0];
			}
		}

		/**
		 * Return an array of Objects representing the valid content proposals
		 * for a field. Ignore the current contents of the field.
		 * @param contents the current contents of the field (ignored)
		 * @param position the current cursor position within the field
		 * (ignored)
		 * @return the array of Objects that represent valid proposals for the
		 * field given its current content.
		 */
		public IContentProposal[] getProposals(String contents,
				final int position) {
			List<IContentProposal> contentProposals = new ArrayList<IContentProposal>();
			for (int i = 0; i < proposals.length; i++) {
				final String proposal = proposals[i];
				if (proposal.startsWith(contents)) {
					contentProposals.add(new IContentProposal() {
						public String getContent() {
							return proposal.substring(position);
						}

						public String getDescription() {
							return null;
						}

						public String getLabel() {
							return proposal;
						}

						public int getCursorPosition() {
							return proposal.length();
						}
					});
				}
			}
			return contentProposals
					.toArray(new IContentProposal[contentProposals.size()]);
		}

		/**
		 * Set the Strings to be used as content proposals.
		 * @param items the array of Strings to be used as proposals.
		 */
		public void setProposals(String[] items) {
			this.proposals = items;
		}
	}
}
