/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ApplicationManifestHandler;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudApplicationURL;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.DumperOptions.LineBreak;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.composer.Composer;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.parser.ParserImpl;
import org.yaml.snakeyaml.reader.StreamReader;
import org.yaml.snakeyaml.resolver.Resolver;

import com.google.common.base.Objects;

/**
 * Deployment properties based on YAML Graph. Instance of this class has ability
 * to compute text differences between this instance and deployment properties
 * passed as parameter
 *
 * @author Alex Boyko
 *
 */
public class YamlGraphDeploymentProperties implements DeploymentProperties {

	private String content;
	private MappingNode appNode;
	private SequenceNode applicationsValueNode;
	private Yaml yaml;
	private List<CloudDomain> domains;

	public YamlGraphDeploymentProperties(String content, String appName, List<CloudDomain> domains) {
		super();
		this.appNode = null;
		this.applicationsValueNode = null;
		this.domains = domains;
		this.content = content;
		initializeYaml(appName);
	}

	private void initializeYaml(String appName) {
		Composer composer = new Composer(new ParserImpl(new StreamReader(new InputStreamReader(new ByteArrayInputStream(content.getBytes())))), new Resolver());
		Node root = composer.getSingleNode();

		Node apps = YamlGraphDeploymentProperties.findValueNode(root, "applications");
		if (apps instanceof SequenceNode) {
			applicationsValueNode = (SequenceNode) apps;
			appNode = findAppNode(applicationsValueNode, appName);
		} else if (root instanceof MappingNode) {
			appNode = (MappingNode) root;
		}

		this.yaml = new Yaml(createDumperOptions());
	}

	private static MappingNode findAppNode(SequenceNode seq, String name) {
		if (name != null) {
			for (Node n : seq.getValue()) {
				Node nameValue = findValueNode(n, ApplicationManifestHandler.NAME_PROP);
				if (nameValue instanceof ScalarNode && ((ScalarNode)nameValue).getValue().equals(name)) {
					return (MappingNode) n;
				}
			}
		}
		return null;
	}

	private DumperOptions createDumperOptions() {
		DumperOptions options = new DumperOptions();
		options.setExplicitStart(false);
		options.setCanonical(false);
		options.setPrettyFlow(true);
		options.setDefaultFlowStyle(FlowStyle.BLOCK);
		options.setLineBreak(LineBreak.getPlatformLineBreak());
		return options;
	}

	@SuppressWarnings("unchecked")
	static private <T extends Node> T getNode(Node node, String key, Class<T> type) {
		Node n = findValueNode(node, key);
		if (n != null && type.isAssignableFrom(n.getClass())) {
			return (T) n;
		}
		return null;
	}

	@Override
	public String getAppName() {
		ScalarNode n = getNode(appNode, ApplicationManifestHandler.NAME_PROP, ScalarNode.class);
		return n == null ? null : n.getValue();
	}

	@Override
	public int getMemory() {
		ScalarNode n = getNode(appNode, ApplicationManifestHandler.MEMORY_PROP, ScalarNode.class);
		if (n != null) {
			try {
				return ApplicationManifestHandler.convertMemory(((ScalarNode)n).getValue());
			} catch (CoreException e) {
				BootDashActivator.log(e);
			}
		}
		return DeploymentProperties.DEFAULT_MEMORY;
	}

	@Override
	public String getBuildpack() {
		ScalarNode n = getNode(appNode, ApplicationManifestHandler.BUILDPACK_PROP, ScalarNode.class);
		return n == null ? null : n.getValue();
	}

	@Override
	public Map<String, String> getEnvironmentVariables() {
		MappingNode mapping = getNode(appNode, ApplicationManifestHandler.ENV_PROP, MappingNode.class);
		if (mapping != null) {
			Map<String, String> vars = new HashMap<>();
			for (NodeTuple entry : mapping.getValue()) {
				if (entry.getKeyNode() instanceof ScalarNode && entry.getValueNode() instanceof ScalarNode) {
					vars.put(((ScalarNode)entry.getKeyNode()).getValue(), ((ScalarNode)entry.getValueNode()).getValue());
				}
			}
			return vars;
		}
		return Collections.emptyMap();
	}

	@Override
	public int getInstances() {
		ScalarNode n = getNode(appNode, ApplicationManifestHandler.INSTANCES_PROP, ScalarNode.class);
		if (n != null) {
			try {
				return Integer.valueOf(n.getValue());
			} catch (NumberFormatException e) {
				BootDashActivator.log(e);
			}
		}
		return DeploymentProperties.DEFAULT_INSTANCES;
	}

	@Override
	public List<String> getServices() {
		SequenceNode sequence = getNode(appNode, ApplicationManifestHandler.SERVICES_PROP, SequenceNode.class);
		if (sequence != null) {
			List<String> services = new ArrayList<>();
			for (Node entry : sequence.getValue()) {
				if (entry instanceof ScalarNode) {
					services.add(((ScalarNode)entry).getValue());
				}
			}
			return services;
		}
		return Collections.emptyList();
	}

	public static Node findValueNode(Node node, String key) {
		if (node instanceof MappingNode) {
			MappingNode mapping = (MappingNode) node;
			for (NodeTuple tuple : mapping.getValue()) {
				if (tuple.getKeyNode() instanceof ScalarNode) {
					ScalarNode scalar = (ScalarNode) tuple.getKeyNode();
					if (key.equals(scalar.getValue())) {
						return tuple.getValueNode();
					}
				}
			}
		}
		return null;
	}

	public static NodeTuple findNodeTuple(MappingNode mapping, String key) {
		if (mapping != null) {
			for (NodeTuple tuple : mapping.getValue()) {
				if (tuple.getKeyNode() instanceof ScalarNode) {
					ScalarNode scalar = (ScalarNode) tuple.getKeyNode();
					if (key.equals(scalar.getValue())) {
						return tuple;
					}
				}
			}
		}
		return null;
	}

	private ReplaceEdit addLineBreakIfMissing(int index) {
		int i = index - 1;
		for (; i >= 0 && Character.isWhitespace(content.charAt(i)) && content.charAt(i) != '\n'; i--);
		if (i > 0 && content.charAt(i) != '\n') {
			return new ReplaceEdit(index, 0, System.lineSeparator());
		}
		return null;
	}

	public MultiTextEdit getDifferences(DeploymentProperties props) {
		MultiTextEdit edits = new MultiTextEdit();
		TextEdit edit;

		if (appNode == null) {
			Map<Object, Object> obj = ApplicationManifestHandler.toYaml(props, domains);
			if (applicationsValueNode == null) {
				DumperOptions options = new DumperOptions();
				options.setExplicitStart(true);
				options.setCanonical(false);
				options.setPrettyFlow(true);
				options.setDefaultFlowStyle(FlowStyle.BLOCK);
				options.setLineBreak(LineBreak.getPlatformLineBreak());
				edits.addChild(new ReplaceEdit(0, content.length(), new Yaml(options).dump(obj)));
			} else {
				edit = addLineBreakIfMissing(applicationsValueNode.getEndMark().getIndex());
				if (edit != null) {
					edits.addChild(edit);
				}
				@SuppressWarnings("unchecked")
				/*
				 * Find the appropriate application Object in the list.
				 */
				List<Object> appsObj = (List<Object>) obj.get(ApplicationManifestHandler.APPLICATIONS_PROP);
				Object appObject = appsObj.get(0);
				for (Object entry : appsObj) {
					if (entry instanceof Map<?,?> && Objects.equal(props.getAppName(), ((Map<?,?>)entry).get(ApplicationManifestHandler.NAME_PROP))) {
						appObject = entry;
						break;
					}
				}
				edits.addChild(new ReplaceEdit(applicationsValueNode.getEndMark().getIndex(), 0, serializeListEntry(appObject, applicationsValueNode.getStartMark().getColumn()).toString()));
			}
		} else {
			if (!Objects.equal(getAppName(), props.getAppName())) {
				edit = createEdit(appNode, props.getAppName(), ApplicationManifestHandler.NAME_PROP);
				if (edit != null) {
					edits.addChild(edit);
				}
			}

			/*
			 * Compare value because strings may have 'G', 'M' etc post-fixes
			 */
			if (getMemory() != props.getMemory()) {
				edit = createEdit(appNode, String.valueOf(props.getMemory()) + "M", ApplicationManifestHandler.MEMORY_PROP);
				if (edit != null) {
					edits.addChild(edit);
				}
			}

			if (getInstances() != props.getInstances()) {
				/*
				 * Value == null if number of instances is 1, i.e. remove instances from YAML
				 */
				Integer newValue = props.getInstances() == DeploymentProperties.DEFAULT_INSTANCES ? null : new Integer(props.getInstances());
				edit = createEdit(appNode, newValue, ApplicationManifestHandler.INSTANCES_PROP);
				if (edit != null) {
					edits.addChild(edit);
				}
			}

			if (!Objects.equal(getBuildpack(), props.getBuildpack())) {
				edit = createEdit(appNode, props.getBuildpack(), ApplicationManifestHandler.BUILDPACK_PROP);
				if (edit != null) {
					edits.addChild(edit);
				}
			}

			if (!getServices().equals(props.getServices())) {
				edit = createEdit(appNode, props.getServices(), ApplicationManifestHandler.SERVICES_PROP);
				if (edit != null) {
					edits.addChild(edit);
				}
			}

			if (!getEnvironmentVariables().equals(props.getEnvironmentVariables())) {
				edit = createEdit(appNode, props.getEnvironmentVariables(), ApplicationManifestHandler.ENV_PROP);
				if (edit != null) {
					edits.addChild(edit);
				}
			}

			/*
			 * If any text edits are produced then there are differences in the URIs
			 */
			edit = getDifferenceForUris(props.getUris());
			if (edit != null) {
				edits.addChild(edit);
			}

		}
		return edits.hasChildren() ? edits : null;
	}

	private TextEdit getDifferenceForUris(Collection<String> uris) {
		MultiTextEdit me = new MultiTextEdit();
		SequenceNode sequence;
		ScalarNode n;

		LinkedHashSet<String> otherHosts = new LinkedHashSet<>();
		LinkedHashSet<String> otherDomains = new LinkedHashSet<>();
		ApplicationManifestHandler.extractHostsAndDomains(uris, domains, otherHosts, otherDomains);
		boolean otherNoRoute = otherHosts.isEmpty() && otherDomains.isEmpty();
		boolean otherNoHostname = otherHosts.isEmpty() && !otherDomains.isEmpty();

		LinkedHashSet<String> currentHosts = new LinkedHashSet<>();
		LinkedHashSet<String> currentDomains = new LinkedHashSet<>();

		n = getNode(appNode, ApplicationManifestHandler.SUB_DOMAIN_PROP, ScalarNode.class);
		if (n != null) {
			currentHosts.add(n.getValue());
		}
		sequence = getNode(appNode, ApplicationManifestHandler.SUB_DOMAINS_PROP, SequenceNode.class);
		if (sequence != null) {
			for (Node o : sequence.getValue()) {
				if (o instanceof ScalarNode) {
					currentHosts.add(((ScalarNode) o).getValue());
				}
			}
		}

		/*
		 * Gather domains from app node from 'domain' and 'domains' attributes
		 */
		n = getNode(appNode, ApplicationManifestHandler.DOMAIN_PROP, ScalarNode.class);
		if (n != null) {
			currentDomains.add(n.getValue());
		}
		sequence = getNode(appNode, ApplicationManifestHandler.DOMAINS_PROP, SequenceNode.class);
		if (sequence != null) {
			for (Node o : sequence.getValue()) {
				if (o instanceof ScalarNode) {
					currentDomains.add(((ScalarNode) o).getValue());
				}
			}
		}

		boolean match = false;
		boolean randomRemoved = false;
		ScalarNode noHostNode = getNode(appNode, ApplicationManifestHandler.NO_HOSTNAME_PROP, ScalarNode.class);
		ScalarNode randomRouteNode = getNode(appNode, ApplicationManifestHandler.RANDOM_ROUTE_PROP, ScalarNode.class);
		ScalarNode noRouteNode = getNode(appNode, ApplicationManifestHandler.NO_ROUTE_PROP, ScalarNode.class);

		if (otherNoRoute) {
			if (noRouteNode == null || !String.valueOf(true).equals(noRouteNode.getValue())) {
				me.addChild(createEdit(appNode, Boolean.TRUE, ApplicationManifestHandler.NO_ROUTE_PROP));

				if (noHostNode != null) {
					me.addChild(createEdit(appNode, (String) null, ApplicationManifestHandler.NO_HOSTNAME_PROP));
				}

				if (randomRouteNode != null) {
					randomRemoved = true;
					me.addChild(createEdit(appNode, (String) null, ApplicationManifestHandler.RANDOM_ROUTE_PROP));
				}
			} else {
				match = true;
			}
		} else {
			if (noRouteNode != null && String.valueOf(true).equals(noRouteNode.getValue())) {
				me.addChild(createEdit(appNode, (String) null, ApplicationManifestHandler.NO_ROUTE_PROP));
			}

			if (otherNoHostname) {
				if (noHostNode == null || !String.valueOf(true).equals(noHostNode.getValue())) {
					me.addChild(createEdit(appNode, Boolean.TRUE, ApplicationManifestHandler.NO_HOSTNAME_PROP));
				}
			} else {
				if (noHostNode != null && String.valueOf(true).equals(noHostNode.getValue())) {
					me.addChild(createEdit(appNode, (String) null, ApplicationManifestHandler.NO_HOSTNAME_PROP));
				}
			}

			if (randomRouteNode != null && otherHosts.size() == 1 && otherDomains.size() == 1
					&& String.valueOf(true).equals(randomRouteNode.getValue())) {
				match = true;
			}

			if (currentHosts.isEmpty() && !(noHostNode != null && String.valueOf(true).equals(noHostNode.getValue()))) {
				currentHosts.add(getAppName());
			}

			if (currentDomains.isEmpty() && !domains.isEmpty()) {
				currentDomains.add(domains.get(0).getName());
			}
		}

		if (!match && (!currentHosts.equals(otherHosts) || !currentDomains.equals(otherDomains))) {
			if (!randomRemoved && randomRouteNode != null) {
				me.addChild(createEdit(appNode, (String) null, ApplicationManifestHandler.RANDOM_ROUTE_PROP));
			}

			generateEditForHostsAndDomains(me, currentHosts, currentDomains, otherHosts, otherDomains);
		}

		return me.hasChildren() ? me : null;
	}

	private void generateEditForHostsAndDomains(MultiTextEdit me, Set<String> currentHosts, Set<String> currentDomains, Set<String> otherHosts, Set<String> otherDomains) {
		TextEdit te = null;
		ScalarNode n = null;

		n = getNode(appNode, ApplicationManifestHandler.SUB_DOMAIN_PROP, ScalarNode.class);
		if (otherHosts.size() == 1) {
			String otherHost = otherHosts.iterator().next();
			if (n == null || !otherHost.equals(n.getValue())) {
				me.addChild(createEdit(appNode, otherHost, ApplicationManifestHandler.SUB_DOMAIN_PROP));
			}
			otherHosts.clear();
		} else {
			if (n != null && !otherHosts.remove(n.getValue())) {
				me.addChild(createEdit(appNode, (String) null, ApplicationManifestHandler.SUB_DOMAIN_PROP));
			}
		}
		te = createEdit(appNode, new ArrayList<>(otherHosts), ApplicationManifestHandler.SUB_DOMAINS_PROP);
		if (te != null) {
			me.addChild(te);
		}

		n = getNode(appNode, ApplicationManifestHandler.DOMAIN_PROP, ScalarNode.class);
		if (otherDomains.size() == 1) {
			String otherDomain = otherDomains.iterator().next();
			if (n == null || !otherDomain.equals(n.getValue())) {
				me.addChild(createEdit(appNode, otherDomain,  ApplicationManifestHandler.DOMAIN_PROP));
			}
			otherDomains.clear();
		} else {
			if (n != null && !otherDomains.remove(n.getValue())) {
				me.addChild(createEdit(appNode, (String) null, ApplicationManifestHandler.DOMAIN_PROP));
			}
		}
		te = createEdit(appNode, new ArrayList<>(otherDomains), ApplicationManifestHandler.DOMAINS_PROP);
		if (te != null) {
			me.addChild(te);
		}

	}

	/**
	 * Creates text edit for mapping node tuples where property and value are
	 * scalars (i.e. value is either string or some primitive type)
	 *
	 * @param parent
	 *            the parent MappingNode
	 * @param otherValue
	 *            the new value for the tuple
	 * @param property
	 *            tuple's key
	 * @return the text edit
	 */
	private TextEdit createEdit(MappingNode parent, Object otherValue, String property) {
		NodeTuple tuple = findNodeTuple(parent, property);
		if (tuple == null) {
			if (otherValue != null) {
				StringBuilder serializedValue = serialize(property, otherValue);
				postIndent(serializedValue, getDefaultOffset());
				int position = positionToAppendAt(parent);
				return new ReplaceEdit(position, 0, serializedValue.toString());
			}
		} else {
			if (otherValue == null) {
				/*
				 * Delete the tuple including the line break if possible
				 */
				int start = tuple.getKeyNode().getStartMark().getIndex();
				int end = tuple.getValueNode().getEndMark().getIndex();
//				int index = parent.getValue().indexOf(tuple);
//				if (!(index > 0 && parent.getValue().get(index - 1).getValueNode() instanceof CollectionNode)) {
//					/*
//					 * If previous tuple is not a map or list then try to remove the preceding line break
//					 */
//					for (; start > 0 && Character.isWhitespace(content.charAt(start - 1)) && content.charAt(start - 1) != '\n'; start--);
//				}
				for (; end > 0 && end < content.length() && Character.isWhitespace(content.charAt(end))/* && content.charAt(end - 1) != '\n'*/; end++);
				/*
				 * HACK!
				 * See if the tuple is first in the application mapping node and application is within application list.
				 * In this case indent for the next value should jump up next to '-', which takes one char from the indent
				 */
//				if ( applicationsValueNode != null && parent.getValue().get(0) == tuple) {
//					end++;
//				}
				return new DeleteEdit(start, end - start);
//				return createDeleteEditIncludingLine(tuple.getKeyNode().getStartMark().getIndex(), tuple.getValueNode().getEndMark().getIndex());
			} else {
				/*
				 * Replace the current value (whether it's a scalr value or anything else without affecting the white space
				 */
				return createReplaceEditWithoutWhiteSpace(tuple.getValueNode().getStartMark().getIndex(), tuple.getValueNode().getEndMark().getIndex() - 1,
						String.valueOf(otherValue));
			}
		}
		return null;
	}

	private int positionToAppendAt(MappingNode m) {
		if (m == appNode) {
			for (NodeTuple tuple : m.getValue()) {
				if (tuple.getKeyNode() instanceof ScalarNode && ApplicationManifestHandler.NAME_PROP.equals(((ScalarNode)tuple.getKeyNode()).getValue())) {
					int index = tuple.getValueNode().getEndMark().getIndex();
					for (; index > 0 && index < content.length() && Character.isWhitespace(content.charAt(index)); index++);
					return index;
				}
			}
		}
		return m.getStartMark().getIndex();
	}

	private TextEdit createEdit(MappingNode parent, List<String> otherValue, String property) {
		NodeTuple tuple = findNodeTuple(parent, property);
		if (tuple == null) {
			if (otherValue != null && !otherValue.isEmpty()) {
				StringBuilder serializedValue = serialize(property, otherValue);
				postIndent(serializedValue, getDefaultOffset());
				int position = positionToAppendAt(parent);
				return new ReplaceEdit(position, 0, serializedValue.toString());
			}
		} else {
			if (otherValue == null || otherValue.isEmpty()) {
				int start = tuple.getKeyNode().getStartMark().getIndex();
				int end = tuple.getValueNode().getEndMark().getIndex();
//				int index = parent.getValue().indexOf(tuple);
//				if (!(index > 0 && parent.getValue().get(index - 1).getValueNode() instanceof CollectionNode)) {
//					/*
//					 * If previous tuple is not a map or list then try to remove the preceding line break
//					 */
//					for (; start > 0 && Character.isWhitespace(content.charAt(start - 1)) && content.charAt(start - 1) != '\n'; start--);
//				}
				for (; end > 0 && end < content.length() && Character.isWhitespace(content.charAt(end)); end++);

				return new DeleteEdit(start, end - start);
			} else {
				Node sequence = tuple.getKeyNode();
				if (tuple.getValueNode() instanceof SequenceNode) {
					SequenceNode sequenceValue = (SequenceNode) tuple.getValueNode();
					MultiTextEdit me = new MultiTextEdit();
					Set<String> others = new LinkedHashSet<>();
					others.addAll(otherValue);

					/*
					 * Remember the ending position of the last entry that remains in the list
					 */
					int appendIndex = sequenceValue.getEndMark().getIndex();
					for (Node n : sequenceValue.getValue()) {
						if (n instanceof ScalarNode) {
							ScalarNode scalar = (ScalarNode) n;
							if (others.contains(scalar.getValue())) {
								// Entry exists, do nothing, just update the end position to append the missing entries
								others.remove(scalar.getValue());
								appendIndex  = scalar.getEndMark().getIndex();
							} else {
								/*
								 * skip "- " prefix for the start position
								 */
								int start = scalar.getStartMark().getIndex();
								for (; start > 0 && content.charAt(start) != '-' && content.charAt(start) != '\n'; start--);
								int end = scalar.getEndMark().getIndex();
								/*
								 *  "- entry" start=2, end=7, need to include '\n' in the deletion
								 */
								me.addChild(createDeleteEditIncludingLine(start, end));
							}
						}
					}
					/*
					 * Offset appendIndex to leave the line break for the previous entry in place. jump over spacing and line break.
					 */
					for (; appendIndex > 0 && appendIndex < content.length() && Character.isWhitespace(content.charAt(appendIndex)) && content.charAt(appendIndex - 1) != '\n'; appendIndex++);
					/*
					 * Add a line break if append index is not starting right after line break.
					 */
					if (!others.isEmpty() && content.charAt(appendIndex - 1) != '\n') {
						me.addChild(new ReplaceEdit(appendIndex, 0, System.lineSeparator()));
					}
					/*
					 * Add missing entries
					 */
					for (String s : others) {
						me.addChild(new ReplaceEdit(appendIndex, 0, serializeListEntry(s, sequenceValue.getStartMark().getColumn()).toString()));
					}
					return me.hasChildren() ? me : null;
				} else {
					/*
					 * Sequence is expected but was something else. Replace the
					 * whole tuple. Don't touch the whitespace when replacing -
					 * it looks good
					 */
					StringBuilder s = serialize(property, otherValue);
					preIndent(s, sequence.getStartMark().getColumn());
					return createReplaceEditWithoutWhiteSpace(sequence.getStartMark().getIndex(),
							tuple.getValueNode().getEndMark().getIndex() - 1,
							s.toString().trim());
				}
			}
		}
		return null;
	}

	private TextEdit createEdit(MappingNode parent, Map<String, String> otherValue, String property) {
		NodeTuple tuple = findNodeTuple(parent, property);
		if (tuple == null) {
			/*
			 * No tuple found for the key
			 */
			if (otherValue != null && !otherValue.isEmpty()) {
				/*
				 * If other value is something that can be serialized, serialize the key and other value and put in the YAML
				 */
				StringBuilder serializedValue = serialize(property, otherValue);
				postIndent(serializedValue, getDefaultOffset());
				int position = positionToAppendAt(parent);
				return new ReplaceEdit(position, 0, serializedValue.toString());
			}
		} else {
			/*
			 * Tuple with the string key is found
			 */
			if (otherValue == null || otherValue.isEmpty()) {
				/*
				 * Delete the found tuple since other value is null or empty
				 */
				int start = tuple.getKeyNode().getStartMark().getIndex();
				int end = tuple.getValueNode().getEndMark().getIndex();
				return new DeleteEdit(start, end - start);
//				return createDeleteEditIncludingLine(tuple.getKeyNode().getStartMark().getIndex(), tuple.getValueNode().getEndMark().getIndex());
			} else {
				/*
				 * Tuple is found, so the key node is there, check the value node
				 */
				Node map = tuple.getKeyNode();
				if (tuple.getValueNode() instanceof MappingNode) {
					/*
					 * Value node is a map node. Go over every entry in the map to calculate differences
					 */
					MappingNode mapValue = (MappingNode) tuple.getValueNode();
					MultiTextEdit e = new MultiTextEdit();
					Map<String, String> leftOver = new LinkedHashMap<>();
					leftOver.putAll(otherValue);
					int appendIndex = mapValue.getStartMark().getIndex();
					for (NodeTuple t : mapValue.getValue()) {
						if (t.getKeyNode() instanceof ScalarNode && t.getValueNode() instanceof ScalarNode) {
							ScalarNode key = (ScalarNode) t.getKeyNode();
							ScalarNode value = (ScalarNode) t.getValueNode();
							String newValue = leftOver.get(key.getValue());
							if (newValue == null) {
								/*
								 * Delete the tuple if newValue is null. Delete including the line if necessary
								 */
								e.addChild(createDeleteEditIncludingLine(key.getStartMark().getIndex(), value.getEndMark().getIndex()));
							} else if (!value.getValue().equals(newValue)) {
								/*
								 * Key is there but value is different, so edit the value
								 */
								e.addChild(new ReplaceEdit(value.getStartMark().getIndex(), value.getEndMark().getIndex() - value.getStartMark().getIndex(), newValue));
								appendIndex = value.getEndMark().getIndex();
							} else {
								appendIndex = value.getEndMark().getIndex();
							}
							leftOver.remove(key.getValue());
						}
					}
					/*
					 * Offset appendIndex to leave the line break for the previous entry in place. jump over spacing and line break.
					 */
					for (; appendIndex > 0 && appendIndex < content.length() && Character.isWhitespace(content.charAt(appendIndex)) && content.charAt(appendIndex - 1) != '\n'; appendIndex++);
					/*
					 * Add a line break if append index is not starting right after line break.
					 */
					if (!leftOver.isEmpty() && content.charAt(appendIndex - 1) != '\n') {
						e.addChild(new ReplaceEdit(appendIndex, 0, System.lineSeparator()));
					}
					/*
					 * Add remaining unmatched entries
					 */
					for (Map.Entry<String, String> entry : leftOver.entrySet()) {
						StringBuilder serializedValue = serialize(entry.getKey(), entry.getValue());
						preIndent(serializedValue, mapValue.getStartMark().getColumn());
						e.addChild(new ReplaceEdit(appendIndex, 0, serializedValue.toString()));
					}
					return e.hasChildren() ? e : null;
				} else {
					/*
					 * Map is expected but was something else. Replace the
					 * whole tuple. Don't touch the whitespace when replacing -
					 * it looks good
					 */
					StringBuilder serializedValue = serialize(property, otherValue);
					preIndent(serializedValue, map.getStartMark().getColumn());
					return createReplaceEditWithoutWhiteSpace(map.getStartMark().getIndex(), tuple.getValueNode().getEndMark().getIndex() - 1, serializedValue.toString().trim());
				}
			}
		}
		return null;
	}

	private StringBuilder serialize(String property, Object value) {
		Map<Object, Object> obj = new HashMap<>();
		obj.put(property, value);
		return new StringBuilder(yaml.dump(obj));
	}

	private StringBuilder postIndent(StringBuilder s, int offset) {
		char[] indent = new char[offset];
		for (int i = 0; i < offset; i++) {
			indent[i] = ' ';
		}
		for (int i = 0; i < s.length(); ) {
			if (s.charAt(i) == '\n') {
				s.insert(i + 1, indent);
				i += indent.length;
			}
			i++;
		}
		return s;
	}

//	private StringBuilder serialize(String property, Object value, int offset) {
//		Map<Object, Object> obj = new HashMap<>();
//		obj.put(property, value);
//		StringBuilder s = new StringBuilder(yaml.dump(obj));
//		if (offset > 0) {
////			offsetString(s, offset);
//			for (int i = 0; i < offset; i++) {
//				s.append(' ');
//			}
//		}
//		return s;
//	}

	private StringBuilder serializeListEntry(Object obj, int offset) {
		StringBuilder s = new StringBuilder(yaml.dump(Collections.singletonList(obj)));
		if (offset > 0) {
			preIndent(s, offset);
		}
		return s;
	}

	private StringBuilder preIndent(StringBuilder s, int offset) {
		char[] indent = new char[offset];
		for (int i = 0; i < offset; i++) {
			indent[i] = ' ';
		}
		int lineLength = 0;
		for (int i = 0; i < s.length(); ) {
			if (s.charAt(i) == '\n') {
				if (lineLength > 0) {
					s.insert(i - lineLength, indent);
					i += indent.length;
					lineLength = 0;
				}
			} else {
				lineLength++;
			}
			i++;
		}
		if (lineLength > 0) {
			s.insert(s.length() - lineLength, indent);
			lineLength = 0;
		}
		return s;
	}

	private DeleteEdit createDeleteEditIncludingLine(int start, int end) {
		if (content != null) {
			for (; start > 0 && Character.isWhitespace(content.charAt(start - 1)) && content.charAt(start - 1) != '\n'; start--);
			for (; end > 0 && end < content.length() && Character.isWhitespace(content.charAt(end)) && content.charAt(end - 1) != '\n'; end++);
		}
		return new DeleteEdit(start, end - start);
	}

	private ReplaceEdit createReplaceEditWithoutWhiteSpace(int start, int end, String text) {
		for (; start < content.length() && Character.isWhitespace(content.charAt(start)); start++);
		for (; end >= start && Character.isWhitespace(content.charAt(end)); end--);
		return new ReplaceEdit(start, end - start + 1, text);
	}

	private int getDefaultOffset() {
		if (appNode == null) {
			if (applicationsValueNode == null) {
				return 0;
			} else {
				return applicationsValueNode.getStartMark().getColumn();
			}
		} else {
			return appNode.getStartMark().getColumn();
		}
	}

	@Override
	public Set<String> getUris() {
		ScalarNode n = getNode(appNode, ApplicationManifestHandler.NO_ROUTE_PROP, ScalarNode.class);
		SequenceNode sequence;
		if (n != null && String.valueOf(true).equals(n.getValue())) {
			return Collections.emptySet();
		}

		LinkedHashSet<String> hostsSet = new LinkedHashSet<>();
		LinkedHashSet<String> domainsSet = new LinkedHashSet<>();

		/*
		 * Gather domains from app node from 'domain' and 'domains' attributes
		 */
		n = getNode(appNode, ApplicationManifestHandler.DOMAIN_PROP, ScalarNode.class);
		if (n != null) {
			domainsSet.add(n.getValue());
		}
		sequence = getNode(appNode, ApplicationManifestHandler.DOMAINS_PROP, SequenceNode.class);
		if (sequence != null) {
			for (Node o : sequence.getValue()) {
				if (o instanceof ScalarNode) {
					domainsSet.add(((ScalarNode)o).getValue());
				}
			}
		}

		/*
		 * Gather hosts from app node from 'host' and 'hosts'
		 * attributes.
		 */
		n = getNode(appNode, ApplicationManifestHandler.SUB_DOMAIN_PROP, ScalarNode.class);
		if (n != null) {
			hostsSet.add(n.getValue());
		}
		sequence = getNode(appNode, ApplicationManifestHandler.SUB_DOMAINS_PROP, SequenceNode.class);
		if (sequence != null) {
			for (Node o : sequence.getValue()) {
				if (o instanceof ScalarNode) {
					hostsSet.add(((ScalarNode)o).getValue());
				}
			}
		}

		/*
		 * If no host names found check for "random-route: true" and
		 * "no-hostname: true" otherwise take app name as the host name
		 */
		if (hostsSet.isEmpty()) {
			n = getNode(appNode, ApplicationManifestHandler.RANDOM_ROUTE_PROP, ScalarNode.class);
			if (n != null && String.valueOf(true).equals(n.getValue())) {
				hostsSet.add(ApplicationManifestHandler.RANDOM_VAR);
				domainsSet.clear();
				domainsSet.add(domains.get(0).getName());
			} else {
				n = getNode(appNode, ApplicationManifestHandler.NO_HOSTNAME_PROP, ScalarNode.class);
				if (n == null || !String.valueOf(true).equals(n.getValue())) {
					hostsSet.add(getAppName());
				}
			}
		}

		/*
		 * Set a domain if they are still empty
		 */
		if (domainsSet.isEmpty()) {
			domainsSet.add(domains.get(0).getName());
		}

		/*
		 * Compose URIs for application based on hosts and domains
		 */
		Set<String> uris = new HashSet<>();
		for (String d : domainsSet) {
			if (hostsSet.isEmpty()) {
				uris.add(new CloudApplicationURL(null, d).getUrl());
			} else {
				for (String h : hostsSet) {
					uris.add(new CloudApplicationURL(h, d).getUrl());
				}
			}
		}

		return uris;
	}

	@Override
	public int getDiskQuota() {
		ScalarNode n = getNode(appNode, ApplicationManifestHandler.DISK_QUOTA_PROP, ScalarNode.class);
		if (n != null) {
			try {
				return ApplicationManifestHandler.convertMemory(((ScalarNode)n).getValue());
			} catch (CoreException e) {
				BootDashActivator.log(e);
			}
		}
		return DeploymentProperties.DEFAULT_MEMORY;
	}

}
