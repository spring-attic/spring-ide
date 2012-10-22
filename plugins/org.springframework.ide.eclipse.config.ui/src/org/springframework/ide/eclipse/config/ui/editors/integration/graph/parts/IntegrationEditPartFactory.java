/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.ui.editors.integration.graph.parts;

import org.eclipse.gef.EditPart;
import org.springframework.ide.eclipse.config.graph.AbstractConfigGraphicalEditor;
import org.springframework.ide.eclipse.config.graph.model.Transition;
import org.springframework.ide.eclipse.config.graph.parts.AbstractConfigEditPartFactory;
import org.springframework.ide.eclipse.config.graph.parts.BorderedActivityPart;
import org.springframework.ide.eclipse.config.graph.parts.TransitionPart;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.AggregatorModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.AlternateTransition;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.BridgeModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.ChainContainerElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.ChainModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.ChannelModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.ClaimCheckInModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.ClaimCheckOutModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.ControlBusModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.DelayerModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.EnricherModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.ExceptionTypeRouterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.FilterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.GatewayModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.HeaderEnricherModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.HeaderFilterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.HeaderValueRouterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.ImplicitChannelModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.ImplicitTransition;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.InboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.IntegrationDiagram;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.JsonToObjectTransformerModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.LoggingChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.MapToObjectTransformerModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.ObjectToJsonTransformerModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.ObjectToMapTransformerModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.ObjectToStringTransformerModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.OutboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.PayloadDeserializingTransformerModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.PayloadSerializingTransformerModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.PayloadTypeRouterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.PlaceholderModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.PublishSubscribeChannelModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.RecipientListRouterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.ResequencerModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.ResourceInboundChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.RouterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.ServiceActivatorModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.SplitterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.SyslogToMapTransformerModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.TransformerModelElement;

/**
 * @author Leo Dos Santos
 */
public class IntegrationEditPartFactory extends AbstractConfigEditPartFactory {

	public IntegrationEditPartFactory(AbstractConfigGraphicalEditor editor) {
		super(editor);
	}

	@Override
	public EditPart createEditPart(EditPart context, Object model) {
		EditPart part = super.createEditPart(context, model);
		if (context instanceof ChainContainerEditPart && part instanceof BorderedActivityPart) {
			BorderedActivityPart borderedPart = (BorderedActivityPart) part;
			borderedPart.setHasAnchors(false);
		}
		return part;
	}

	@Override
	protected EditPart createEditPartFromModel(EditPart context, Object model) {
		EditPart part = null;
		if (model instanceof IntegrationDiagram) {
			part = new IntegrationDiagramEditPart((IntegrationDiagram) model);
		}
		else if (model instanceof AggregatorModelElement) {
			part = new AggregatorGraphicalEditPart((AggregatorModelElement) model);
		}
		else if (model instanceof BridgeModelElement) {
			part = new BridgeGraphicalEditPart((BridgeModelElement) model);
		}
		else if (model instanceof ChainContainerElement) {
			part = new ChainContainerEditPart((ChainContainerElement) model);
		}
		else if (model instanceof ChainModelElement) {
			part = new ChainGraphicalEditPart((ChainModelElement) model);
		}
		else if (model instanceof ChannelModelElement) {
			part = new ChannelGraphicalEditPart((ChannelModelElement) model);
		}
		else if (model instanceof ClaimCheckInModelElement) {
			part = new ClaimCheckInGraphicalEditPart((ClaimCheckInModelElement) model);
		}
		else if (model instanceof ClaimCheckOutModelElement) {
			part = new ClaimCheckOutGraphicalEditPart((ClaimCheckOutModelElement) model);
		}
		else if (model instanceof ControlBusModelElement) {
			part = new ControlBusGraphicalEditPart((ControlBusModelElement) model);
		}
		else if (model instanceof DelayerModelElement) {
			part = new DelayerGraphicalEditPart((DelayerModelElement) model);
		}
		else if (model instanceof EnricherModelElement) {
			part = new EnricherGraphicalEditPart((EnricherModelElement) model);
		}
		else if (model instanceof ExceptionTypeRouterModelElement) {
			part = new ExceptionTypeRouterGraphicalEditPart((ExceptionTypeRouterModelElement) model);
		}
		else if (model instanceof FilterModelElement) {
			part = new FilterGraphicalEditPart((FilterModelElement) model);
		}
		else if (model instanceof GatewayModelElement) {
			part = new GatewayGraphicalEditPart((GatewayModelElement) model);
		}
		else if (model instanceof HeaderEnricherModelElement) {
			part = new HeaderEnricherGraphicalEditPart((HeaderEnricherModelElement) model);
		}
		else if (model instanceof HeaderFilterModelElement) {
			part = new HeaderFilterGraphicalEditPart((HeaderFilterModelElement) model);
		}
		else if (model instanceof HeaderValueRouterModelElement) {
			part = new HeaderValueRouterGraphicalEditPart((HeaderValueRouterModelElement) model);
		}
		else if (model instanceof ImplicitChannelModelElement) {
			part = new ImplicitChannelGraphicalEditPart((ImplicitChannelModelElement) model);
		}
		else if (model instanceof InboundChannelAdapterModelElement) {
			part = new InboundChannelAdapterGraphicalEditPart((InboundChannelAdapterModelElement) model);
		}
		else if (model instanceof JsonToObjectTransformerModelElement) {
			part = new JsonToObjectTransformerGraphicalEditPart((JsonToObjectTransformerModelElement) model);
		}
		else if (model instanceof LoggingChannelAdapterModelElement) {
			part = new LoggingChannelAdapterGraphicalEditPart((LoggingChannelAdapterModelElement) model);
		}
		else if (model instanceof MapToObjectTransformerModelElement) {
			part = new MapToObjectTransformerGraphicalEditPart((MapToObjectTransformerModelElement) model);
		}
		else if (model instanceof ObjectToJsonTransformerModelElement) {
			part = new ObjectToJsonTransformerGraphicalEditPart((ObjectToJsonTransformerModelElement) model);
		}
		else if (model instanceof ObjectToMapTransformerModelElement) {
			part = new ObjectToMapTransformerGraphicalEditPart((ObjectToMapTransformerModelElement) model);
		}
		else if (model instanceof ObjectToStringTransformerModelElement) {
			part = new ObjectToStringTransformerGraphicalEditPart((ObjectToStringTransformerModelElement) model);
		}
		else if (model instanceof OutboundChannelAdapterModelElement) {
			part = new OutboundChannelAdapterGraphicalEditPart((OutboundChannelAdapterModelElement) model);
		}
		else if (model instanceof PayloadDeserializingTransformerModelElement) {
			part = new PayloadDeserializingTransformerGraphicalEditPart(
					(PayloadDeserializingTransformerModelElement) model);
		}
		else if (model instanceof PayloadSerializingTransformerModelElement) {
			part = new PayloadSerializingTransformerGraphicalEditPart((PayloadSerializingTransformerModelElement) model);
		}
		else if (model instanceof PayloadTypeRouterModelElement) {
			part = new PayloadTypeRouterGraphicalEditPart((PayloadTypeRouterModelElement) model);
		}
		else if (model instanceof PlaceholderModelElement) {
			part = new PlaceholderGraphicalEditPart((PlaceholderModelElement) model);
		}
		else if (model instanceof PublishSubscribeChannelModelElement) {
			part = new PublishSubscribeChannelGraphicalEditPart((PublishSubscribeChannelModelElement) model);
		}
		else if (model instanceof RecipientListRouterModelElement) {
			part = new RecipientListRouterGraphicalEditPart((RecipientListRouterModelElement) model);
		}
		else if (model instanceof ResequencerModelElement) {
			part = new ResequencerGraphicalEditPart((ResequencerModelElement) model);
		}
		else if (model instanceof ResourceInboundChannelAdapterModelElement) {
			part = new ResourceInboundChannelAdapterGraphicalEditPart((ResourceInboundChannelAdapterModelElement) model);
		}
		else if (model instanceof RouterModelElement) {
			part = new RouterGraphicalEditPart((RouterModelElement) model);
		}
		else if (model instanceof ServiceActivatorModelElement) {
			part = new ServiceActivatorGraphicalEditPart((ServiceActivatorModelElement) model);
		}
		else if (model instanceof SplitterModelElement) {
			part = new SplitterGraphicalEditPart((SplitterModelElement) model);
		}
		else if (model instanceof SyslogToMapTransformerModelElement) {
			part = new SyslogToMapTransformerGraphicalEditPart((SyslogToMapTransformerModelElement) model);
		}
		else if (model instanceof TransformerModelElement) {
			part = new TransformerGraphicalEditPart((TransformerModelElement) model);
		}
		else if (model instanceof AlternateTransition) {
			part = new AlternateTransitionPart((AlternateTransition) model);
		}
		else if (model instanceof ImplicitTransition) {
			part = new ImplicitTransitionPart((ImplicitTransition) model);
		}
		else if (model instanceof Transition) {
			part = new TransitionPart((Transition) model);
		}
		return part;
	}

}
