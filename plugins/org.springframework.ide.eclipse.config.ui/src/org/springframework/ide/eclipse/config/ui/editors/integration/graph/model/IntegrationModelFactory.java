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
package org.springframework.ide.eclipse.config.ui.editors.integration.graph.model;

import java.util.List;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.schemas.IntegrationSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.AbstractConfigGraphDiagram;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.graph.model.IDiagramModelFactory;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class IntegrationModelFactory extends AbstractIntegrationModelFactory implements IDiagramModelFactory {

	public void getChildrenFromXml(List<Activity> list, IDOMElement input, Activity parent) {
		if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_AGGREGATOR)) {
			AggregatorModelElement aggregator = new AggregatorModelElement(input, parent.getDiagram());
			list.add(aggregator);
		}
		else if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_BRIDGE)) {
			BridgeModelElement bridge = new BridgeModelElement(input, parent.getDiagram());
			list.add(bridge);
		}
		else if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_CHAIN)) {
			ChainModelElement chain = new ChainModelElement(input, parent.getDiagram());
			list.add(chain);
		}
		else if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_CHANNEL)) {
			ChannelModelElement channel = new ChannelModelElement(input, parent.getDiagram());
			list.add(channel);
		}
		else if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_CLAIM_CHECK_IN)) {
			ClaimCheckInModelElement claim = new ClaimCheckInModelElement(input, parent.getDiagram());
			list.add(claim);
		}
		else if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_CLAIM_CHECK_OUT)) {
			ClaimCheckOutModelElement claim = new ClaimCheckOutModelElement(input, parent.getDiagram());
			list.add(claim);
		}
		else if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_CONTROL_BUS)) {
			ControlBusModelElement bus = new ControlBusModelElement(input, parent.getDiagram());
			list.add(bus);
		}
		else if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_DELAYER)) {
			DelayerModelElement delayer = new DelayerModelElement(input, parent.getDiagram());
			list.add(delayer);
		}
		else if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_ENRICHER)) {
			EnricherModelElement enricher = new EnricherModelElement(input, parent.getDiagram());
			list.add(enricher);
		}
		else if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_EXCEPTION_TYPE_ROUTER)) {
			ExceptionTypeRouterModelElement router = new ExceptionTypeRouterModelElement(input, parent.getDiagram());
			list.add(router);
		}
		else if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_FILTER)) {
			FilterModelElement filter = new FilterModelElement(input, parent.getDiagram());
			list.add(filter);
		}
		else if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_GATEWAY)) {
			GatewayModelElement gateway = new GatewayModelElement(input, parent.getDiagram());
			list.add(gateway);
		}
		else if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_HEADER_ENRICHER)) {
			HeaderEnricherModelElement enricher = new HeaderEnricherModelElement(input, parent.getDiagram());
			list.add(enricher);
		}
		else if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_HEADER_FILTER)) {
			HeaderFilterModelElement filter = new HeaderFilterModelElement(input, parent.getDiagram());
			list.add(filter);
		}
		else if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_HEADER_VALUE_ROUTER)) {
			HeaderValueRouterModelElement router = new HeaderValueRouterModelElement(input, parent.getDiagram());
			list.add(router);
		}
		else if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_INBOUND_CHANNEL_ADAPTER)) {
			InboundChannelAdapterModelElement adapter = new InboundChannelAdapterModelElement(input,
					parent.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_JSON_TO_OBJECT_TRANSFORMER)) {
			JsonToObjectTransformerModelElement transformer = new JsonToObjectTransformerModelElement(input,
					parent.getDiagram());
			list.add(transformer);
		}
		else if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_LOGGING_CHANNEL_ADAPTER)) {
			LoggingChannelAdapterModelElement adapter = new LoggingChannelAdapterModelElement(input,
					parent.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_MAP_TO_OBJECT_TRANSFORMER)) {
			MapToObjectTransformerModelElement transformer = new MapToObjectTransformerModelElement(input,
					parent.getDiagram());
			list.add(transformer);
		}
		else if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_OBJECT_TO_JSON_TRANSFORMER)) {
			ObjectToJsonTransformerModelElement transformer = new ObjectToJsonTransformerModelElement(input,
					parent.getDiagram());
			list.add(transformer);
		}
		else if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_OBJECT_TO_MAP_TRANSFORMER)) {
			ObjectToMapTransformerModelElement transformer = new ObjectToMapTransformerModelElement(input,
					parent.getDiagram());
			list.add(transformer);
		}
		else if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_OBJECT_TO_STRING_TRANSFORMER)) {
			ObjectToStringTransformerModelElement transformer = new ObjectToStringTransformerModelElement(input,
					parent.getDiagram());
			list.add(transformer);
		}
		else if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_OUTBOUND_CHANNEL_ADAPTER)) {
			OutboundChannelAdapterModelElement adapter = new OutboundChannelAdapterModelElement(input,
					parent.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_PAYLOAD_DESERIALIZING_TRANSFORMER)) {
			PayloadDeserializingTransformerModelElement transformer = new PayloadDeserializingTransformerModelElement(
					input, parent.getDiagram());
			list.add(transformer);
		}
		else if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_PAYLOAD_SERIALIZING_TRANSFORMER)) {
			PayloadSerializingTransformerModelElement transformer = new PayloadSerializingTransformerModelElement(
					input, parent.getDiagram());
			list.add(transformer);
		}
		else if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_PAYLOAD_TYPE_ROUTER)) {
			PayloadTypeRouterModelElement router = new PayloadTypeRouterModelElement(input, parent.getDiagram());
			list.add(router);
		}
		else if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_PUBLISH_SUBSCRIBE_CHANNEL)) {
			PublishSubscribeChannelModelElement channel = new PublishSubscribeChannelModelElement(input,
					parent.getDiagram());
			list.add(channel);
		}
		else if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_RECIPIENT_LIST_ROUTER)) {
			RecipientListRouterModelElement router = new RecipientListRouterModelElement(input, parent.getDiagram());
			list.add(router);
		}
		else if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_RESEQUENCER)) {
			ResequencerModelElement resequencer = new ResequencerModelElement(input, parent.getDiagram());
			list.add(resequencer);
		}
		else if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_RESOURCE_INBOUND_CHANNEL_ADAPTER)) {
			ResourceInboundChannelAdapterModelElement adapter = new ResourceInboundChannelAdapterModelElement(input,
					parent.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_ROUTER)) {
			RouterModelElement router = new RouterModelElement(input, parent.getDiagram());
			list.add(router);
		}
		else if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_SERVICE_ACTIVATOR)) {
			ServiceActivatorModelElement activator = new ServiceActivatorModelElement(input, parent.getDiagram());
			list.add(activator);
		}
		else if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_SPLITTER)) {
			SplitterModelElement splitter = new SplitterModelElement(input, parent.getDiagram());
			list.add(splitter);
		}
		else if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_TRANSFORMER)) {
			TransformerModelElement transformer = new TransformerModelElement(input, parent.getDiagram());
			list.add(transformer);
		}
	}

	public void getGenericChildrenFromXml(List<Activity> list, IDOMElement input, Activity parent) {
		if (parent instanceof ChainContainerElement) {
			PlaceholderModelElement placeholder = new PlaceholderModelElement(input, parent.getDiagram());
			if (!parent.getDiagram().listContainsElement(list, placeholder)) {
				list.add(placeholder);
			}
		}
	}

	@Override
	public void getNestedChildrenFromXml(List<Activity> list, IDOMElement input, AbstractConfigGraphDiagram diagram) {
		super.getNestedChildrenFromXml(list, input, diagram);
		if (input.getLocalName().equals(IntegrationSchemaConstants.ELEM_CHAIN)) {
			ChainContainerElement chain = new ChainContainerElement(input, diagram);
			list.add(chain);
		}
	}

}
