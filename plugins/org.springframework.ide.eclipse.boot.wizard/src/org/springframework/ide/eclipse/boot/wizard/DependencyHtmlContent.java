package org.springframework.ide.eclipse.boot.wizard;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.internal.text.html.HTMLPrinter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Dependency;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Link;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Links;

@SuppressWarnings("restriction")
public class DependencyHtmlContent {
	
	private static final String UNIT; // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=155993
	static {
		UNIT= Util.isMac() ? "px" : "pt";   //$NON-NLS-1$//$NON-NLS-2$
	}

	public static String generateHtmlTooltip(Dependency dep, Map<String, String> variables) {
		StringBuffer buffer = new StringBuffer();
		HTMLPrinter.insertPageProlog(buffer, 0, String.join("\n", styles()));
		
		buffer.append("<p>");
		buffer.append(HTMLPrinter.convertToHTMLContent(dep.getDescription()));		
		buffer.append("</p>");
		
		if (dep.getLinks() != null) {
			Links links = dep.getLinks();
			if (links.getGuides() != null) {
				String bullets = linkBullets(links.getGuides(), variables);
				if (!bullets.isEmpty()) {
					HTMLPrinter.addSmallHeader(buffer, "Guides");
					HTMLPrinter.startBulletList(buffer);
					buffer.append(bullets);
					HTMLPrinter.endBulletList(buffer);
				}
			}
			if (links.getReferences() != null) {
				String bullets = linkBullets(links.getReferences(), variables);
				if (!bullets.isEmpty()) {
					HTMLPrinter.addSmallHeader(buffer, "References");
					HTMLPrinter.startBulletList(buffer);
					buffer.append(bullets);
					HTMLPrinter.endBulletList(buffer);
				}
			}
		}
				
		HTMLPrinter.addPageEpilog(buffer);
		
		return buffer.toString();
	}
	
	private static String linkBullets(Link[] links, Map<String, String> variables) {
		StringBuffer bullets = new StringBuffer();
		for (Link link : links) {
			String href = link.getHref();
			if (link.isTemplated()) {
				if (href != null) {
					Pattern p = Pattern.compile("\\{(.*?)\\}");
					Matcher matcher = p.matcher(href);
					while (matcher.find()) {
						String variable = matcher.group(1);
						String replacement = variables.get(variable);
						if (replacement == null) {
							BootWizardActivator.getDefault().getLog()
									.log(new Status(IStatus.WARNING, BootWizardActivator.PLUGIN_ID,
											"Initializr dependency link has unknown " + variable + " in link " + href));
							href = null;
							break;
						} else {
							href = href.replaceAll("\\{" + variable + "\\}", replacement);
						}
					}
				}
			}
			if (href != null) {
				StringBuilder sb = new StringBuilder();
				sb.append("<a href=\"");
				sb.append(href);
				sb.append("\">");
				if (link.getTitle() != null) {
					sb.append(HTMLPrinter.convertToHTMLContent(link.getTitle()));
				} else {
//					if (href.contains("docs.spring.io")) {
//						sb.append(HTMLPrinter.convertToHTMLContent("Spring Boot Reference Doc"));
//					} else {
						sb.append(HTMLPrinter.convertToHTMLContent(href));
//					}
				}
				sb.append("</a>");
				HTMLPrinter.addBullet(bullets, sb.toString());
			}
		}
		return bullets.toString();
	}
	
	private static String[] styles() {
		StringBuilder mainStyle = new StringBuilder();		
		FontData fontData = JFaceResources.getDialogFontDescriptor().getFontData()[0];
		boolean bold= (fontData.getStyle() & SWT.BOLD) != 0;
		boolean italic= (fontData.getStyle() & SWT.ITALIC) != 0;
		String size= Integer.toString(Math.max(5, fontData.getHeight() - 1)) + UNIT;
		String family= "'" + fontData.getName() + "',sans-serif"; //$NON-NLS-1$ //$NON-NLS-2$
		mainStyle.append("font-size:" + size + ";");
		mainStyle.append(size);
		mainStyle.append(';');
		mainStyle.append("font-family:");
		mainStyle.append(family);
		mainStyle.append(';');
		mainStyle.append("font-weight:");
		mainStyle.append(bold ? "bold" : "normal");
		mainStyle.append(';');
		mainStyle.append("font-style:");
		mainStyle.append(italic ? "italic" : "normal");
		mainStyle.append(';');
		
		return new String[] {
			"html 		{" + mainStyle + "}",
			"body, h4, h5, h6, p, table, td, caption, th, ul, ol, dl, li, dd, dt { font-size: 1em; }",
			"h5         { margin-top: 0px; margin-bottom: 0px; }",
			"p 			{ margin-top: 1em; margin-bottom: 1em; }",
			"ul	        { margin-top: 0px; margin-bottom: 0em; margin-left: 1em; padding-left: 1em; }",
			"li	        { margin-top: 0px; margin-bottom: 0px; }",
			"li p	    { margin-top: 0px; margin-bottom: 0px; }"
		};		
	}

}
