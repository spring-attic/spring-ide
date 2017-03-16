package org.springframework.ide.eclipse.boot.wizard;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.function.Supplier;

import org.eclipse.jface.internal.text.html.HTML2TextReader;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

@SuppressWarnings("restriction")
public class HtmlTooltip extends ToolTip {
	
	private static final int MIN_WIDTH= 80;

	private static final int MIN_HEIGHT= 50;
	
	private Supplier<String> html;
	private int maxWidth;
	private int maxHeight;
	
	public HtmlTooltip(Control control) {
		super(control);
		setHideOnMouseDown(false);
		this.maxHeight = 700;
		this.maxWidth = 300;
	}

	@Override
	protected Composite createToolTipContentArea(Event event, Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().create());
		Browser browser = new Browser(composite, SWT.NONE);
		
		browser.setJavascriptEnabled(false);
		
		browser.addOpenWindowListener(new OpenWindowListener() {
			@Override
			public void open(WindowEvent event) {
				event.required= true; // Cancel opening of new windows
			}
		});
		
		// Replace browser's built-in context menu with none
		browser.setMenu(new Menu(browser.getShell(), SWT.NONE));

		String htmlContent = html.get();
		
		browser.setText(htmlContent);
		Point size = computeSizeHint(browser, htmlContent);
		browser.setLayoutData(GridDataFactory.swtDefaults().hint(size.x, size.y).create());
		
		// Add after HTML content is set to avoid event fired after the content is set
		browser.addLocationListener(new LocationAdapter() {
			@Override
			public void changing(LocationEvent event) {
				super.changing(event);
				event.doit = false;
				try {
					PlatformUI.getWorkbench().getBrowserSupport().createBrowser(null).openURL(new URL(event.location));
					hide();
				} catch (PartInitException | MalformedURLException e) {
					BootWizardActivator.log(e);
				}
			}
		});
		
		return composite;
		
//		Browser browser = new Browser(parent, SWT.NONE);
//		String htmlContent = html.get();
//		browser.setText(htmlContent);
//		createTextLayout(browser);
//		
//		Point size = computeSizeHint((Shell)parent, htmlContent);
//		
//		System.out.println("Width = " + size.x + " Height = " + size.y);
//		
//		browser.setSize(size);
//		parent.setSize(size);
//		
//		return browser;
	}
	
	public void setHtml(Supplier<String> html) {
		this.html = html;
	}
	
	public void setMaxSize(int minWidth, int minHeight) {
		this.maxWidth = minWidth;
		this.maxHeight = minHeight;
	}

	private Point computeSizeHint(Browser browser, String html) {
		TextLayout fTextLayout= new TextLayout(browser.getDisplay());

		// Initialize fonts
		String symbolicFontName= JFaceResources.DIALOG_FONT;
		Font font= JFaceResources.getFont(symbolicFontName);
		FontData fd = font.getFontData()[0];
		font = new Font(font.getDevice(), new FontData(fd.getName(), fd.getHeight() - 1, fd.getStyle()));
		
		fTextLayout.setFont(font);
		fTextLayout.setWidth(-1);
//		font= JFaceResources.getFontRegistry().getBold(symbolicFontName);
		
		Font boldFont = new Font(font.getDevice(), new FontData(fd.getName(), fd.getHeight() - 1, SWT.BOLD));
		TextStyle fBoldStyle= new TextStyle(boldFont, null, null);

		// Compute and set tab width
		fTextLayout.setText("    "); //$NON-NLS-1$
		int tabWidth= fTextLayout.getBounds().width;
		fTextLayout.setTabs(new int[] { tabWidth });
		fTextLayout.setText(""); //$NON-NLS-1$

		Point sizeConstraints= new Point(maxWidth, maxHeight);
		Rectangle trim= browser.getParent().computeTrim(0, 0, 0, 0);
		trim.width += 12;
		trim.height += 12;
		int height= trim.height;

		//FIXME: The HTML2TextReader does not render <p> like a browser.
		// Instead of inserting an empty line, it just adds a single line break.
		// Furthermore, the indentation of <dl><dd> elements is too small (e.g with a long @see line)
		TextPresentation presentation= new TextPresentation();
		String text;
		try (HTML2TextReader reader= new HTML2TextReader(new StringReader(html), presentation)) {
			text= reader.getString();
		} catch (IOException e) {
			text= ""; //$NON-NLS-1$
		}

		fTextLayout.setText(text);
		fTextLayout.setWidth(sizeConstraints == null ? SWT.DEFAULT : sizeConstraints.x - trim.width);
		Iterator<StyleRange> iter= presentation.getAllStyleRangeIterator();
		while (iter.hasNext()) {
			StyleRange sr= iter.next();
			if (sr.fontStyle == SWT.BOLD)
				fTextLayout.setStyle(fBoldStyle, sr.start, sr.start + sr.length - 1);
		}

		Rectangle bounds= fTextLayout.getBounds(); // does not return minimum width, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=217446
		int lineCount= fTextLayout.getLineCount();
		int textWidth= 0;
		for (int i= 0; i < lineCount; i++) {
			Rectangle rect= fTextLayout.getLineBounds(i);
			int lineWidth= rect.x + rect.width;
//			if (i == 0)
//				lineWidth+= fInput.getLeadingImageWidth();
			textWidth= Math.max(textWidth, lineWidth);
		}
		bounds.width= textWidth;
		fTextLayout.setText(""); //$NON-NLS-1$

		int minWidth= bounds.width;
		height= height + bounds.height;

		// Add some air to accommodate for different browser renderings
		minWidth+= 20;
		height+= 20;


		// Apply max size constraints
		if (sizeConstraints != null) {
			if (sizeConstraints.x != SWT.DEFAULT)
				minWidth= Math.min(sizeConstraints.x, minWidth + trim.width);
			if (sizeConstraints.y != SWT.DEFAULT)
				height= Math.min(sizeConstraints.y, height);
		}

		// Ensure minimal size
		int width= Math.max(MIN_WIDTH, minWidth);
		height= Math.max(MIN_HEIGHT, height);
		
		fTextLayout.dispose();
		font.dispose();
		boldFont.dispose();

		return new Point(width, height);
	}

}
