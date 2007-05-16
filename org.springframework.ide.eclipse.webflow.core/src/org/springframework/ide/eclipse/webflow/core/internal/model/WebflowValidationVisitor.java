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
package org.springframework.ide.eclipse.webflow.core.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.support.DefaultConversionService;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElementVisitor;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * {@link IWebflowModelElementVisitor} implementation that collects and stores
 * {@link WebflowValidationProblem} in an internal list.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class WebflowValidationVisitor implements IWebflowModelElementVisitor {

	private static final String EXPRESSION_PREFIX = "${";

	private static final String EXPRESSION_SUFFIX = "}";

	private static final List<String> SCOPE_TYPES;

	static {
		SCOPE_TYPES = new ArrayList<String>();
		SCOPE_TYPES.add("request");
		SCOPE_TYPES.add("flash");
		SCOPE_TYPES.add("flow");
		SCOPE_TYPES.add("conversation");
	}

	private WebflowValidationProblemReporter problemReporter = new WebflowValidationProblemReporter();

	private ConversionService conversionService = null;

	private IWebflowConfig webflowConfig = null;

	private IFile file = null;

	public WebflowValidationVisitor(IWebflowConfig webflowConfig) {
		this.webflowConfig = webflowConfig;
		this.file = webflowConfig.getResource();
	}

	public WebflowValidationProblemReporter getProblemReporter() {
		return problemReporter;
	}

	public boolean visit(IWebflowModelElement element, IProgressMonitor monitor) {
		if (element instanceof WebflowState) {
			validateWebflowState(element);
			return true;
		}
		else if (element instanceof Attribute) {
			validateAttribute(element);
			return true;
		}
		else if (element instanceof Variable) {
			validateVar(element);
			return true;
		}
		else if (element instanceof InputAttribute) {
			validateInputAttribute(element);
			return true;
		}
		else if (element instanceof OutputAttribute) {
			validateOutputAttribute(element);
			return true;
		}
		else if (element instanceof Mapping) {
			validateMapping(element);
			return true;
		}
		else if (element instanceof ExceptionHandler) {
			validateExceptionHandler(element);
			return true;
		}
		else if (element instanceof Import) {
			validateImport(element);
			return true;
		}
		else if (element instanceof InlineFlowState) {
			validateInlineFlow(element);
			return true;
		}
		else if (element instanceof Action) {
			validateAction(element);
			return true;
		}
		else if (element instanceof EvaluateAction) {
			validateEvaluateAction(element);
			return true;
		}
		else if (element instanceof BeanAction) {
			validateBeanAction(element);
			return true;
		}
		else if (element instanceof Set) {
			validateSet(element);
			return true;
		}
		else if (element instanceof EvaluationResult) {
			validateEvaluationResult(element);
			return true;
		}
		else if (element instanceof ActionState) {
			validateActionState(element);
			return true;
		}
		else if (element instanceof ViewState) {
			validateViewState(element);
			return true;
		}
		else if (element instanceof DecisionState) {
			validateDecisionState(element);
			return true;
		}
		else if (element instanceof EndState) {
			validateEndState(element);
			return true;
		}
		else if (element instanceof SubflowState) {
			validateSubflowState(element);
			return true;
		}
		else if (element instanceof StateTransition) {
			validateTransition(element);
			return true;
		}
		else if (element instanceof AttributeMapper) {
			validateAttributeMapper(element);
			return true;
		}
		else if (element instanceof RenderActions) {
			return true;
		}
		else if (element instanceof ExitActions) {
			return true;
		}
		else if (element instanceof EntryActions) {
			return true;
		}
		return false;
	}

	private void validateSubflowState(IWebflowModelElement element) {
		SubflowState state = (SubflowState) element;
		if (!StringUtils.hasText(state.getId())) {
			getProblemReporter().error(
					"Element 'subflow-state' requires unique 'id' attribute",
					element);
		}
		else if (!WebflowModelUtils.isStateIdUnique(state)) {
			getProblemReporter().error(
					"Specified state id \"{0}\" is not unique", element,
					state.getId());
		}
		if (!StringUtils.hasText(state.getFlow())) {
			getProblemReporter().error(
					"Element 'subflow-state' requires unique 'flow' attribute",
					element);
		}
		else if (!WebflowModelUtils.getWebflowConfigNames(
				webflowConfig.getProject()).contains(state.getFlow())
				&& !WebflowModelUtils.getWebflowConfigNames(
						WebflowModelUtils.getWebflowState(state, true))
						.contains(state.getFlow())) {
			getProblemReporter().error(
					"Referenced flow \"{0}\" cannot be found", element,
					state.getFlow());
		}
	}

	private void validateEndState(IWebflowModelElement element) {
		EndState state = (EndState) element;
		if (!StringUtils.hasText(state.getId())) {
			getProblemReporter().error(
					"Element 'end-state' requires unique 'id' attribute",
					element);
		}
		else if (!WebflowModelUtils.isStateIdUnique(state)) {
			getProblemReporter().error(
					"Specified state id \"{0}\" is not unique", element,
					state.getId());
		}
	}

	private void validateDecisionState(IWebflowModelElement element) {
		DecisionState state = (DecisionState) element;
		if (!StringUtils.hasText(state.getId())) {
			getProblemReporter().error(
					"Element 'decision-state' requires unique 'id' attribute",
					element);
		}
		else if (!WebflowModelUtils.isStateIdUnique(state)) {
			getProblemReporter().error(
					"Specified state id \"{0}\" is not unique", element,
					state.getId());
		}
	}

	private void validateViewState(IWebflowModelElement element) {
		ViewState state = (ViewState) element;
		if (!StringUtils.hasText(state.getId())) {
			getProblemReporter().error(
					"Element 'view-state' requires unique 'id' attribute",
					element);
		}
		else if (!WebflowModelUtils.isStateIdUnique(state)) {
			getProblemReporter().error(
					"Specified state id \"{0}\" is not unique", element,
					state.getId());
		}
	}

	private void validateActionState(IWebflowModelElement element) {
		ActionState state = (ActionState) element;
		if (!StringUtils.hasText(state.getId())) {
			getProblemReporter().error(
					"Element 'action-state' requires unique 'id' attribute",
					element);
		}
		else if (!WebflowModelUtils.isStateIdUnique(state)) {
			getProblemReporter().error(
					"Specified state id \"{0}\" is not unique", element,
					state.getId());
		}
		if (state.getActions().size() == 0) {
			getProblemReporter().error(
					"Element 'action-state' requires action sub elements",
					element);
		}
	}

	private void validateAttributeMapper(IWebflowModelElement element) {
		AttributeMapper mapper = (AttributeMapper) element;
		if (StringUtils.hasText(mapper.getBean())
				&& !WebflowModelUtils.isReferencedBeanFound(webflowConfig,
						mapper.getBean())) {
			getProblemReporter().error(
					"Referenced bean \"{0}\" cannot be found", element,
					mapper.getBean());
		}
	}

	private void validateTransition(IWebflowModelElement element) {
		StateTransition trans = (StateTransition) element;
		if (!StringUtils.hasText(trans.getToStateId())) {
			getProblemReporter().error(
					"Element 'transition' requires 'to' attribute", element);
		}
		else if (trans.getToState() == null
				&& (!(trans.getToStateId().startsWith(EXPRESSION_PREFIX) && trans
						.getToStateId().endsWith(EXPRESSION_SUFFIX)))) {
			getProblemReporter()
					.error(
							"Element 'transition' references a non-exiting state \"{0}\"",
							element, trans.getToStateId());
		}
	}

	private void validateSet(IWebflowModelElement element) {
		Set set = (Set) element;
		if (!StringUtils.hasText(set.getAttribute())) {
			getProblemReporter().error(
					"Element 'set' requires 'attribute' attribute", element);
		}
		if (!StringUtils.hasText(set.getValue())) {
			getProblemReporter().error(
					"Element  'set' requires 'value' attribute", element);
		}
		if (StringUtils.hasText(set.getScope())
				&& !SCOPE_TYPES.contains(set.getScope())) {
			getProblemReporter().error("Invalid scope \"{0}\" specified",
					element, set.getScope());
		}
	}

	private void validateEvaluationResult(IWebflowModelElement element) {
		EvaluationResult result = (EvaluationResult) element;
		if (!StringUtils.hasText(result.getName())) {
			getProblemReporter().error(
					"Element  'evaluate-result' requires 'name' attribute",
					element);
		}
		if (StringUtils.hasText(result.getScope())
				&& !SCOPE_TYPES.contains(result.getScope())) {
			getProblemReporter().error("Invalid scope \"{0}\" specified",
					element, result.getScope());
		}
	}

	private void validateEvaluateAction(IWebflowModelElement element) {
		EvaluateAction action = (EvaluateAction) element;
		if (!StringUtils.hasText(action.getExpression())) {
			getProblemReporter()
					.error(
							"Element 'evaluate-action' requires 'expression' attribute",
							element);
		}
	}

	private void validateBeanAction(IWebflowModelElement element) {
		BeanAction action = (BeanAction) element;
		if (!StringUtils.hasText(action.getBean())) {
			getProblemReporter().error(
					"Element 'bean-action' requires bean attribute", element);
		}
		else if (!WebflowModelUtils.isReferencedBeanFound(webflowConfig, action
				.getBean())) {
			getProblemReporter().error(
					"Referenced bean \"{0}\" cannot be found", element,
					action.getBean());
		}
		if (!StringUtils.hasText(action.getMethod())) {
			getProblemReporter().error(
					"Element 'bean-action' requires method attribute", element);
		}
		else if (!Introspector.doesImplement(WebflowModelUtils.getActionType(
				webflowConfig, action.getNode()), FactoryBean.class.getName())) {
			List<IMethod> methods = WebflowModelUtils.getActionMethods(
					webflowConfig, action.getNode());
			boolean found = false;
			for (IMethod method : methods) {
				if (method.getElementName().equals(action.getMethod())) {
					found = true;
					break;
				}
			}
			if (!found) {
				getProblemReporter()
						.error(
								"Referenced action method \"{0}\" cannot be found or is not a valid action method",
								element, action.getMethod());
			}
		}
	}

	private void validateAction(IWebflowModelElement element) {
		Action action = (Action) element;
		if (!StringUtils.hasText(action.getBean())) {
			getProblemReporter().error(
					"Element 'action' requires 'bean' attribute", element);
		}
		else if (!WebflowModelUtils.isReferencedBeanFound(webflowConfig, action
				.getBean())) {
			getProblemReporter().error(
					"Referenced bean \"{0}\" cannot be found", element,
					action.getBean());
		}
		if (StringUtils.hasText(action.getMethod())
				&& !Introspector.doesImplement(WebflowModelUtils.getActionType(
						webflowConfig, action.getNode()), FactoryBean.class
						.getName())) {
			List<IMethod> methods = WebflowModelUtils.getActionMethods(
					webflowConfig, action.getNode());
			boolean found = false;
			for (IMethod method : methods) {
				if (method.getElementName().equals(action.getMethod())) {
					found = true;
					break;
				}
			}
			if (!found) {
				getProblemReporter()
						.error(
								"Referenced action method \"{0}\" cannot be found or is not a valid action method",
								element, action.getMethod());
			}
		}
	}

	private void validateInlineFlow(IWebflowModelElement element) {
		InlineFlowState state = (InlineFlowState) element;
		if (!StringUtils.hasText(state.getId())) {
			getProblemReporter().error(
					"Element 'inline-flow' requires 'id' attribute", element);
		}
	}

	private void validateImport(IWebflowModelElement element) {
		Import impor = (Import) element;
		if (!StringUtils.hasText(impor.getResource())) {
			getProblemReporter().error(
					"Element 'import' requires 'resource' attribute", element);
		}
	}

	private void validateExceptionHandler(IWebflowModelElement element) {
		ExceptionHandler handler = (ExceptionHandler) element;
		if (!StringUtils.hasText(handler.getBean())) {
			getProblemReporter().error(
					"Element 'exception-handler' requires 'bean' attribute",
					element);
		}
		else if (!WebflowModelUtils.isReferencedBeanFound(webflowConfig,
				handler.getBean())) {
			getProblemReporter().error(
					"Referenced bean \"{0}\" cannot be found", element,
					handler.getBean());
		}
	}

	private void validateMapping(IWebflowModelElement element) {
		Mapping mapping = (Mapping) element;
		if (!StringUtils.hasText(mapping.getSource())) {
			getProblemReporter()
					.error(
							"Element 'mapping' element requires 'input-attribute' attribute",
							element);
		}
		if (!StringUtils.hasText(mapping.getTarget())
				&& !StringUtils.hasText(mapping.getTargetCollection())) {
			getProblemReporter()
					.error(
							"Using 'target' and 'target-collection' attributes is not allowed on 'mapping' element",
							element);
		}
		if (StringUtils.hasText(mapping.getTo())
				&& getJavaType(mapping.getTo()) == null) {
			getProblemReporter().error("Class 'to' \"{0}\" cannot be resolved",
					element, mapping.getTo());
		}
		if (StringUtils.hasText(mapping.getFrom())
				&& getJavaType(mapping.getFrom()) == null) {
			getProblemReporter().error(
					"Class 'from' \"{0}\" cannot be resolved", element,
					mapping.getFrom());
		}
	}

	private void validateInputAttribute(IWebflowModelElement element) {
		InputAttribute attribute = (InputAttribute) element;
		if (!StringUtils.hasText(attribute.getName())) {
			getProblemReporter().error(
					"Element 'input-attribute' requires 'name' attribute",
					element);
		}
		if (StringUtils.hasText(attribute.getScope())
				&& !SCOPE_TYPES.contains(attribute.getScope())) {
			getProblemReporter().error("Invalid scope \"{0}\" specified",
					element, attribute.getScope());
		}
	}

	private void validateOutputAttribute(IWebflowModelElement element) {
		OutputAttribute attribute = (OutputAttribute) element;
		if (!StringUtils.hasText(attribute.getName())) {
			getProblemReporter().error(
					"Element 'output-attribute' requires 'name' attribute",
					element);
		}
		if (StringUtils.hasText(attribute.getScope())
				&& !SCOPE_TYPES.contains(attribute.getScope())) {
			getProblemReporter().error("Invalid scope \"{0}\" specified",
					element, attribute.getScope());
		}
	}

	private void validateVar(IWebflowModelElement element) {
		Variable attribute = (Variable) element;
		if (!StringUtils.hasText(attribute.getName())) {
			getProblemReporter().error(
					"Element 'var' requires 'name' attribute", element);
		}
		else {
			if (!StringUtils.hasText(attribute.getBean())
					&& !StringUtils.hasText(attribute.getClazz())
					&& !WebflowModelUtils.isReferencedBeanFound(webflowConfig,
							attribute.getName())) {
				getProblemReporter().error(
						"Referenced bean \"{0}\" cannot be found", element,
						attribute.getName());
			}
		}
		if (StringUtils.hasText(attribute.getScope())
				&& !SCOPE_TYPES.contains(attribute.getScope())) {
			getProblemReporter().error("Invalid scope \"{0}\" specified",
					element, attribute.getScope());
		}
		if (StringUtils.hasText(attribute.getClazz())) {
			IType type = getJavaType(attribute.getClazz());
			if (type == null) {
				getProblemReporter().error(
						"class 'var' \"{0}\" cannot be resolved", element,
						attribute.getClazz());
			}
			else
				try {
					if (type.isInterface() || Flags.isAbstract(type.getFlags())) {
						getProblemReporter()
								.error(
										"class 'var' \"{0}\" is either an Interface or abstract",
										element, attribute.getClazz());
					}
				}
				catch (JavaModelException e) {
				}
		}
		if (StringUtils.hasText(attribute.getBean())
				&& !WebflowModelUtils.isReferencedBeanFound(webflowConfig,
						attribute.getBean())) {
			getProblemReporter().error(
					"Referenced bean \"{0}\" cannot be found", element,
					attribute.getBean());
		}
	}

	private void validateAttribute(IWebflowModelElement element) {
		Attribute attribute = (Attribute) element;
		if (!StringUtils.hasText(attribute.getName())) {
			getProblemReporter().error(
					"Element 'attribute' requires 'name' attribute", element);
		}
		if (StringUtils.hasText(attribute.getType())
				&& getJavaType(attribute.getType()) == null) {
			getProblemReporter().error(
					"Attribute 'type' \"{0}\" cannot be resolved", attribute,
					attribute.getType());
		}
		if (!StringUtils.hasText(attribute.getValue())) {
			getProblemReporter().error(
					"Element 'Attribute' requires a 'value'", element);
		}
	}

	private void validateWebflowState(IWebflowModelElement element) {
		WebflowState state = (WebflowState) element;
		// check if starte state is defined
		if (state.getStartState() == null) {
			Element node = (Element) state.getNode();
			NodeList startStateNodes = node.getElementsByTagName("start-state");
			if (startStateNodes == null || startStateNodes.getLength() == 0) {
				getProblemReporter()
						.error(
								"Start state definition is missing. Add a 'start-state' element",
								state);
			}
			else if (startStateNodes.getLength() == 1) {
				IDOMNode startStateNode = (IDOMNode) startStateNodes.item(0);
				String idref = state.getAttribute(startStateNode, "idref");
				if (idref == null) {
					getProblemReporter().error(
							"Start state definition misses 'idref' attribute",
							state);
				}
				else if (idref != null) {
					getProblemReporter()
							.error(
									"Start state definition references non-existing state \"{0}\"",
									state, idref);
				}
			}
		}
	}

	private IType getJavaType(String className) {
		IType type = BeansModelUtils.getJavaType(file.getProject(), className);
		if (type == null) {
			Class clazz = getConversionService().getClassByAlias(className);
			if (clazz != null) {
				type = BeansModelUtils.getJavaType(file.getProject(), clazz
						.getName());
			}
		}
		return type;
	}

	private ConversionService getConversionService() {
		if (this.conversionService == null) {
			this.conversionService = new DefaultConversionService();
		}
		return this.conversionService;
	}
}
