package org.springframework.ide.html5.editor;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javafx.application.Platform;
import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.springframework.ide.html5.IdeHtml5Plugin;

public class Html5ContentEditor extends EditorPart {

	private static final String URL = IdeHtml5Plugin.PLUGIN_ID + "/resources/integration";

	private WebView webView;

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		Platform.setImplicitExit(false);
		final FXCanvas fxCanvas = new FXCanvas(parent, SWT.NONE);
		fxCanvas.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).create());
		fxCanvas.setLayout(GridLayoutFactory.fillDefaults().create());
		webView = new WebView();
		webView.setVisible(false);

		BorderPane border = new BorderPane();
		Scene scene = new Scene(border);
		border.setCenter(webView);
		fxCanvas.setScene(scene);
		try {
			File contentInstance = ResourceCopier.getCopy(URL, new NullProgressMonitor());
			String htmlLocation = new File(contentInstance, "index.html").toURI().toString();
			final String fileLocation = new File(contentInstance, "integration.xml").toURI().toString();
			webView.getEngine().load(htmlLocation);
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					JSObject js = (JSObject) webView.getEngine().executeScript("window");
					js.call("loadXml", fileLocation);
				}
			});
		}
		catch (MalformedURLException e) {
			IdeHtml5Plugin.error(null, e);
		}
		catch (IOException e) {
			IdeHtml5Plugin.error(null, e);
		}
		webView.setVisible(true);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
