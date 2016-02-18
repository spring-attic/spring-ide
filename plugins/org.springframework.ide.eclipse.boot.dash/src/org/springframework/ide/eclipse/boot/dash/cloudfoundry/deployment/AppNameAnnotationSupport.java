package org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.SourceViewer;

public class AppNameAnnotationSupport {

	private SourceViewer fViewer = null;
	private AppNameRulerColumn fColumn = null;
	private ITextInputListener textInputListener = new ITextInputListener() {

		public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
		}

		@Override
		public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
			IAnnotationModel annotationModel = fViewer.getVisualAnnotationModel();
			if (annotationModel instanceof IAnnotationModelExtension) {
				IAnnotationModelExtension extension = (IAnnotationModelExtension) annotationModel;
				if (extension.getAnnotationModel(AppNameAnnotationModel.APP_NAME_MODEL_KEY) == null) {
					extension.addAnnotationModel(AppNameAnnotationModel.APP_NAME_MODEL_KEY, new AppNameAnnotationModel());
				}
			}
			fColumn.setModel(annotationModel);
		}

	};

	public AppNameAnnotationSupport(SourceViewer viewer, IAnnotationAccess annotationAccess) {
		super();
		fViewer = viewer;
		fColumn = new AppNameRulerColumn(9, annotationAccess);
		fColumn.addAnnotationType(AppNameAnnotation.TYPE);
		fViewer.addVerticalRulerColumn(fColumn);
		fViewer.addTextInputListener(textInputListener);
	}

	public void dispose() {
		fViewer.removeTextInputListener(textInputListener);
	}
}
