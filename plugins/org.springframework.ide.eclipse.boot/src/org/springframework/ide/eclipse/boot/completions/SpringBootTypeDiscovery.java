/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.completions;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.Callable;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.MavenCoordinates;
import org.springframework.ide.eclipse.boot.core.SpringBootCore;
import org.springframework.ide.eclipse.boot.ui.ChooseDependencyDialog;
import org.springframework.ide.eclipse.boot.util.RetryUtil;
import org.springsource.ide.eclipse.commons.completions.externaltype.AbstractExternalTypeSource;
import org.springsource.ide.eclipse.commons.completions.externaltype.ExternalType;
import org.springsource.ide.eclipse.commons.completions.externaltype.ExternalTypeDiscovery;
import org.springsource.ide.eclipse.commons.completions.externaltype.ExternalTypeEntry;
import org.springsource.ide.eclipse.commons.completions.util.Requestor;
import org.springsource.ide.eclipse.commons.core.preferences.StsProperties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This example 'discovers' types by reading a large xml file. This xml file is 
 * created 'offline' and contains a dependency graph of maven artifacts and types.
 * 
 * @author Kris De Volder
 */
public class SpringBootTypeDiscovery implements ExternalTypeDiscovery {
	
	private static final long SECOND = 1000;
	private static final long MINUTE = 60 * SECOND;

	private static StsProperties stsProps = StsProperties.getInstance();
	
	/**
	 * When requesting graph data from webservice we may have to retry... because
	 * the webservice may return a 'I am busy' result while it is computing the data.
	 * This constant specifies the 'retry interval'. I.e the time we wait in between
	 * retries.
	 */
	private static final long RETRY_INTERVAL = 15 * SECOND; 
	
	/**
	 *  Retries are not unlimited. When this timelimit is reached we stop retrying.
	 */
	private static final long RETRY_TIMELIMIT = 5 * MINUTE;
	
	private static final boolean DEBUG = false;// (""+Platform.getLocation()).contains("kdvolder");

	/**
	 * If this option is 'true' then when a dependency is added the managedVersion is 
	 * never overridden (i.e. an explicit version is only inserted in the pom
	 * if there is no managed version).
	 * 
	 * If this option is 'false' then a version dependency will be included in the 
	 * pom if it does not match the managed version).
	 */
	private boolean preferManagedVersion = true;
	
	/**
	 * If this option is selected transitive dependencies are considered. If is not
	 * selected then only a jar that directly provides a type will be suggested.
	 * <p>
	 * Note that even when this option deselected, spring-boot-starters will be suggested
	 * for some types because of the graph simplification algorithm that transforms the
	 * graph such that it appears as though those types are provided directly by
	 * the starter. 
	 */
	private boolean transitive = false;
	private String bootVersion;
	
	public class DGraphTypeSource extends AbstractExternalTypeSource {

		private DirectedGraph dgraph;
		private ExternalType type;

		public DGraphTypeSource(DirectedGraph dgraph, ExternalType type) {
			this.dgraph = dgraph;
			this.type = type;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void addToClassPath(IJavaProject project, IProgressMonitor mon) {
			try {
				//TODO: progress monitor handling
				ISpringBootProject bootProject = SpringBootCore.create(project);

				Collection<MavenCoordinates> sources;
				sources = getProviders();
				MavenCoordinates source = chooseSource(sources);
				if (source!=null) {
					bootProject.addMavenDependency(source, preferManagedVersion);
				}
			} catch (Exception e) {
				BootActivator.log(e);
			}
		}

		@SuppressWarnings("unchecked")
		public Collection<MavenCoordinates> getProviders() {
			Collection<MavenCoordinates> sources;
			if (transitive) {
				sources = (Collection<MavenCoordinates>) dgraph.getDescendants(type);
			} else {
				sources = dgraph.getSuccessors(type);
			}
			return sources;
		}

		/**
		 * Open a dialog to let user choose one of the several ways a type can get added to
		 * the classpath. 
		 * <p>
		 * If only one choice is available the dialog is skipped and that choice is returned immediately.
		 * <p>
		 * If the collection of choices is empty then the dialog is also skipped and null is returned.
		 */
		private MavenCoordinates chooseSource(Collection<MavenCoordinates> sources) {
			if (sources!=null) {
				if (sources.size()==1) {
					for (MavenCoordinates mavenCoordinates : sources) {
						return mavenCoordinates;
					}
				} else if (sources.isEmpty()) {
					return null;
				}
				//There are at least 2 choices available. Offer them to the user.
				return ChooseDependencyDialog.openOn("Choose a Dependency",
						"How do you want to add <b>"+type.getName()
						+"</b> from <b>"+type.getPackage()+"</b> to your classpath?",
						sources
				);
			}
			return null;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public String getDescription() {
			//The dgraph map actually contains inverted dependency edges so we have to
			// get 'descendants' to actually get the 'ancestors' in the real dgraph.
			Collection<MavenCoordinates> ancestors = getProviders();
			if (!ancestors.isEmpty()) {
				StringBuilder description = new StringBuilder();
				description.append(
						"Add type <b>"+type.getName()+"</b> from<br>"+ 
						"package <b>"+type.getPackage()+"</b><br>"+
						"to the classpath via one of the following:<p>");
				description.append("<ul>");
				for (Object object : ancestors) {
					description.append(toHtml(object));
				}
				description.append("</ul>");
				return description.toString();
			}
			return null;
		}

		private String toHtml(Object object) {
			if (object instanceof MavenCoordinates) {
				MavenCoordinates artifact = (MavenCoordinates) object;
				StringBuilder html = new StringBuilder();
				html.append("<li>");
				html.append("<b>"+artifact.getArtifactId()+"</b><br>");
				html.append("group: "+artifact.getGroupId()+"<br>");
				html.append("version: "+artifact.getVersion());
				html.append("</li>");
				return html.toString();
			}
			return object.toString();
		}
	}

	//TODO: should generate or obtain this data based on spring boot version of project. For now only use rest api call that 
	// retrieves type graph data for default version of spring boot.
	private static URI XML_DATA_LOCATION;
	static {
		try {
			//Use data embedded in this plugin:
			//XML_DATA_LOCATION = new URI("platform:/plugin/org.springframework.ide.eclipse.boot/resources/boot-completion-data.txt");
			XML_DATA_LOCATION = new URI(stsProps.get("spring.boot.typegraph.url"));
		} catch (URISyntaxException e) {
			BootActivator.log(e);
		}
	}

	private static class MyHandler extends DefaultHandler {
		Stack<Object> path = new Stack<Object>();
		
		/**
		 * Map used to 'reuse' strings if they have the same content. We expect a packag name to be used
		 * many times (depending on the number of types in the package). So reusing the Stirng
		 * objects could save memory. 
		 */
		private HashMap<String, String> strings = new HashMap<String, String>();

		private DirectedGraph dgraph;

		public MyHandler(DirectedGraph dgraph) {
			this.dgraph = dgraph;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			Object parentElement = peek();
			Object thisElement = null;
			if (qName.equals("artifact")) {
				MavenCoordinates artifact = MavenCoordinates_parse(attributes.getValue("id"));
				if (artifact!=null) {
					thisElement = artifact;
					if (parentElement!=null) {
						Assert.isLegal(parentElement instanceof MavenCoordinates, "parent of artifact should always be another artififact");
						dgraph.addEdge(thisElement, parentElement);
					}
				}
			} else if (qName.equals("type")) {
				ExternalType type = ExternalType_parse(attributes.getValue("id"));
				thisElement = type;
				Assert.isLegal(parentElement instanceof MavenCoordinates, "parent of a type should be an artifact (that contains it) but it was: "+parentElement);
				dgraph.addEdge(thisElement, parentElement);
			}
			//else { ... something we don't care about ... }
			path.push(thisElement); //Yes we might push some null's but that makes it easier to ensure pops and pushes are
									// 'balanced' as it keeps the 'when to push and pop' logic very simple (i.e. always push on startElement and
									// always pop on endElement
		}

		private ExternalType ExternalType_parse(String fqName) {
			int split = fqName.lastIndexOf('.');
			if (split>0) {
				String name = fqName.substring(split+1);
				String packageName = fqName.substring(0, split);
				return new ExternalType(intern(name),intern(packageName));
			} else {
				throw new IllegalArgumentException("Invalid fqName: "+fqName);
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			path.pop();
		}

		private Object peek() {
			if (path.isEmpty()) {
				return null;
			}
			return path.peek();
		}

		public void dispose() {
			Assert.isLegal(path.isEmpty(), "Bug: pops and pushes are out of whack!");
			strings = null;
		}
		
		/**
		 * Parse from a string like: 		
		 * org.springframework:spring-core:4.0.0.RC1
		 * <p>
		 * We are really only interested in jars. So if the dependency is not a jar
		 * then returns null.
		 */
		public MavenCoordinates MavenCoordinates_parse(String artifact) {
			String[] pieces = artifact.split(":");
			if (pieces.length==3) {
				//e.g: org.springframework:spring-core:4.0.0.RC1
				return new MavenCoordinates(intern(pieces[0]), intern(pieces[1]), intern(pieces[2]));
			} else if (pieces.length==4) {
				//e.g: org.springframework:spring-core:jar:4.0.0.RC1
				String type = pieces[2];
				if ("jar".equals(type)) {
					return new MavenCoordinates(intern(pieces[0]), intern(pieces[1]), intern(pieces[3]));
				}
			}
			throw new IllegalArgumentException("Unsupported artifact string: '"+artifact+"'");
		}

		private String intern(String string) {
			String existing = strings.get(string);
			if (existing==null) {
				strings.put(string, string);
				existing = string;
			}
			return existing;
		}
		
	}

	public SpringBootTypeDiscovery(String bootVersion) {
		Assert.isNotNull(bootVersion);
		this.bootVersion = bootVersion;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void getTypes(Requestor<ExternalTypeEntry> requestor) {
		try {
			DirectedGraph dgraph = createGraph();
			Set nodes = dgraph.getNonLeafNodes();
			//We are only interested in 'type' nodes. These should always have pointer to
			// at least one maven artifact that contains them. Therefore type nodes are
			// never leaf nodes.
			for (Object node : nodes) {
	
				//Not all non-leaf nodes in the graph represent types. Some (fewer) of them represent artifacts
				// that were added to the graph because they are depended on by other artifacts.
				if (node instanceof ExternalType) {
					ExternalType type = (ExternalType) node;
					requestor.receive(new ExternalTypeEntry(type, new DGraphTypeSource(dgraph, type)));
				}
	
				if (DEBUG) {
					if (node instanceof MavenCoordinates) {
						Set ancestors = dgraph.getDescendants(node); 
						if (!ancestors.isEmpty()) {
							System.out.println(node+" has ancestors: ");
							for (Object anc : ancestors) {
								System.out.println("   "+anc);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
	}

	private DirectedGraph createGraph() throws Exception {
		return RetryUtil.retry(RETRY_INTERVAL, RETRY_TIMELIMIT, new Callable<DirectedGraph>() {
			public DirectedGraph call() throws Exception {
				DirectedGraph dgraph = new DirectedGraph();
				
				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser saxParser = factory.newSAXParser();
				MyHandler handler = new MyHandler(dgraph);
				try {
					saxParser.parse(XML_DATA_LOCATION.toString()+"/"+bootVersion, handler);
				} finally {
					handler.dispose();
				}
				return dgraph;
			}
		});
	}

	private DirectedGraph createGraphMaybe() throws Exception {
		//Should parsing be done lazyly?
		// Alternatively the callers of this contructor could do it from a job.
		DirectedGraph dgraph = new DirectedGraph();
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		MyHandler handler = new MyHandler(dgraph);
		try {
			saxParser.parse(XML_DATA_LOCATION.toString(), handler);
		} finally {
			handler.dispose();
		}
		return dgraph;
	}
}
