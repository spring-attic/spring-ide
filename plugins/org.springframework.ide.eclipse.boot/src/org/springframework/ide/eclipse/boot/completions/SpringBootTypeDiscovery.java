package org.springframework.ide.eclipse.boot.completions;

import java.beans.DesignMode;
import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.MavenCoordinates;
import org.springframework.ide.eclipse.boot.core.SpringBootCore;
import org.springframework.ide.eclipse.boot.ui.ChooseDependencyDialog;
import org.springsource.ide.eclipse.commons.completions.externaltype.AbstractExternalTypeSource;
import org.springsource.ide.eclipse.commons.completions.externaltype.ExternalType;
import org.springsource.ide.eclipse.commons.completions.externaltype.ExternalTypeDiscovery;
import org.springsource.ide.eclipse.commons.completions.externaltype.ExternalTypeEntry;
import org.springsource.ide.eclipse.commons.completions.externaltype.ExternalTypeSource;
import org.springsource.ide.eclipse.commons.completions.util.Requestor;
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
	
	private static final boolean DEBUG = (""+Platform.getLocation()).contains("kdvolder");

	public static class DGraphTypeSource extends AbstractExternalTypeSource {

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
				//TODO only one way to add to classpath out of a potential multiple ways to add the type is implemented.
				ISpringBootProject bootProject = SpringBootCore.create(project);

				Collection<MavenCoordinates> sources = (Collection<MavenCoordinates>) dgraph.getDescendants(type);
				MavenCoordinates source = chooseSource(sources);
				if (source!=null) {
					bootProject.addMavenDependency(source);
				}
				
//				IFile pomFile = project.getProject().getFile("pom.xml");
//				
//				
//				if (sources!=null && !sources.isEmpty()) {
//					//TODO: replace with proper mechanism to add dependencies to pom file.
//				
//					StringBuilder comments = new StringBuilder();
//					comments.append("\n<!-- ============================ -->\n");
//					for (MavenCoordinates source : sources) {
//						comments.append("<!-- "+source+"-->\n");
//					}
//					
//					pomFile.appendContents(new StringInputStream(comments.toString()), true/*force*/, true/*keepHistory*/, new NullProgressMonitor());  
//				}
			} catch (Exception e) {
				BootActivator.log(e);
			}
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
						"Type "+type.getFullyQualifiedName()+" is not yet on the classpath.\n"+
						"How do you want to add it?",
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
			Set ancestors = dgraph.getDescendants(type);
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

	//TODO: should generate or obtain this data based on spring boot version of project. For now we just have a sample file that's hard-coded here.
	private static final URI XML_DATA_LOCATION = new File("/home/kdvolder/workspaces-sts/spring-ide/fun-with-maven/boot-completion-data.txt").toURI();

	private DirectedGraph dgraph = new DirectedGraph();
	
	private final class MyHandler extends DefaultHandler {
		Stack<Object> path = new Stack<Object>();
		
		/**
		 * Map used to 'reuse' strings if they have the same content. We expect a packag name to be used
		 * many times (depending on the number of types in the package). So reusing the Stirng
		 * objects could save memory. 
		 */
		private HashMap<String, String> strings = new HashMap<String, String>();

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
		 * org.springframework:spring-core:jar:4.0.0.RC1
		 * <p>
		 * We are really only interested in jars. So if the dependency is not a jar
		 * then returns null.
		 */
		public MavenCoordinates MavenCoordinates_parse(String artifact) {
			String[] pieces = artifact.split(":");
			if (pieces.length==4) {
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


	public SpringBootTypeDiscovery() throws Exception {
		//Should parsing be done lazyly?
		// Alternatively the callers of this contructor could do it from a job.
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		MyHandler handler = new MyHandler();
		saxParser.parse(XML_DATA_LOCATION.toString(), handler);
		
		handler.dispose(); 
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void getTypes(Requestor<ExternalTypeEntry> requestor) {
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
		
	}
}
