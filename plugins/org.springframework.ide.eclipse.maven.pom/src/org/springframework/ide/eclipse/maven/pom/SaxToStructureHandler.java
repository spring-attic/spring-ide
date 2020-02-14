package org.springframework.ide.eclipse.maven.pom;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.IDocument;
import org.springframework.ide.eclipse.maven.pom.DomStructureComparable.DomType;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SaxToStructureHandler extends DefaultHandler {
	
//	public enum DOM_TYPES {
//		ELEMENT,
//		ATTRIBUTE,
//		TEXT
//	}
//	
//	private List<DocumentRangeNode> children;
//	
//	private DocumentRangeNode structure;
//	
//	private Locator locator;
	
	private DomStructureComparable.Builder current;
	private Locator locator;
	private DomStructureComparable root;
	private IDocument document;
	private IdProviderRegistry idProviders;
	
	public SaxToStructureHandler(IDocument document, IdProviderRegistry idProviders) {
		this.document = document;
		this.idProviders = idProviders;
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
		System.out.println(locator);
	}

	@Override
	public void startDocument() throws SAXException {
		Assert.isTrue(root == null);
		current = DomStructureComparable.createRoot(document);
		current.start(locator);
	}

	@Override
	public void endDocument() throws SAXException {
		Assert.isTrue(current.type == DomType.ROOT);
		current.end(locator);
		root = current.build("root");
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
//		System.out.println("Start " + localName + " line: " + locator.getLineNumber() + " column: " + locator.getColumnNumber() + " pid = " + locator.getPublicId() + " sysId = " + locator.getSystemId());
		current = DomStructureComparable.createElement(document, current, localName);
		current.start(locator);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		current.end(locator);
		DomStructureComparable child = current.build(idProviders.getId(current));
		current = current.parent;
		current.addChild(child);
	}

//	@Override
//	public void characters(char[] ch, int start, int length) throws SAXException {
//		StringBuilder sb = new StringBuilder();
//		for (int i = 0; i < length; i++) {
//			sb.append(ch[start + i]);
//		}
//		DomStructureComparable.createText(document, current);
//		System.out.println("CHARACTERS: '" + sb + "'");
//	}
	
	public DomStructureComparable getRoot() {
		return root;
	}

}
