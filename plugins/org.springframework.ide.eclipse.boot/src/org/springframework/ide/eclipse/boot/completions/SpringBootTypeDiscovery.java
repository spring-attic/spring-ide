package org.springframework.ide.eclipse.boot.completions;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.codehaus.plexus.util.StringInputStream;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.MavenCoordinates;
import org.springframework.ide.eclipse.boot.core.SpringBootCore;
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

	public class DGraphTypeSouce implements ExternalTypeSource {

		private ExternalType type;

		public DGraphTypeSouce(ExternalType type) {
			this.type = type;
		}

		@Override
		public void addToClassPath(IJavaProject project, IProgressMonitor mon) {
			try {
				//TODO: progress monitor handling
				//TODO only one way to add to classpath out of a potential multiple ways to add the type is implemented.
				ISpringBootProject bootProject = SpringBootCore.create(project);

				Collection<MavenCoordinates> sources = (Collection<MavenCoordinates>) dgraph.get(type);
				for (MavenCoordinates source : sources) {
					bootProject.addMavenDependency(source);
					break;
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
	}

	//TODO: should generate data based on spring boot version of project. For now we just have a sample file that's hard-coded here.
	private static final URI XML_DATA_LOCATION = new File("/home/kdvolder/workspaces-sts/spring-ide/fun-with-maven/boot-completion-data.txt").toURI();
	
	private MultiMap dgraph = new MultiValueMap();
	
	private final class MyHandler extends DefaultHandler {
		Stack<Object> path = new Stack<Object>();

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			Object parentElement = peek();
			Object thisElement = null;
			if (qName.equals("artifact")) {
				MavenCoordinates artifact = MavenCoordinates.parse(attributes.getValue("id"));
				if (artifact!=null) {
					thisElement = artifact;
					if (parentElement!=null) {
						Assert.isLegal(parentElement instanceof MavenCoordinates, "parent of artifact should always be another artififact");
						dgraph.put(thisElement, parentElement);
					}
				}
			} else if (qName.equals("type")) {
				ExternalType type = new ExternalType(attributes.getValue("id"));
				thisElement = type;
				Assert.isLegal(parentElement instanceof MavenCoordinates, "parent of a type should be an artifact (that contains it) but it was: "+parentElement);
				dgraph.put(thisElement, parentElement);
			}
			//else { ... something we don't care about ... }
			path.push(thisElement); //Yes we might push some null's but that makes it easier to ensure pops and pushes are
									// 'balanced' as it keeps the 'when to push and pop' logic very simple (i.e. always push on startElement and
									// always pop on endElement
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
		}
	}


	public SpringBootTypeDiscovery() throws Exception {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		MyHandler handler = new MyHandler();
		saxParser.parse(XML_DATA_LOCATION.toString(), handler);
		
		handler.dispose(); 
	}
	
	@Override
	public void getTypes(Requestor<ExternalTypeEntry> requestor) {
		Set nodes = dgraph.keySet();
		for (Object node : nodes) {
			//Not all nodes in the graph represent types. Fewer of them represent artifacts.
			if (node instanceof ExternalType) {
				ExternalType type = (ExternalType) node;
				requestor.receive(new ExternalTypeEntry(type, new DGraphTypeSouce(type)));
			}
		}
	}
	
	

}
