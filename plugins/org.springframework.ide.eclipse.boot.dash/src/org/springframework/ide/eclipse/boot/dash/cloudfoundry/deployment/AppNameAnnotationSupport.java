package org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment;

import java.util.Iterator;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension2;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;

public class AppNameAnnotationSupport {

	private static final RGB APP_NAME_COLOR = new RGB(0xBB, 0xBB, 0xBB);
	private static final RGB SELECTED_APP_NAME_COLOR = new RGB(0x00, 0x87, 0x00);
	private static final int APP_NAME_ANNOTATION_ALPHA = 0x17;

	private static final int ANNOTATION_COLUMN_WIDTH = 12;

	private SourceViewer fViewer = null;
	private AppNameRulerColumn fColumn = null;
	private ISharedTextColors fColorsCache;
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

	public AppNameAnnotationSupport(SourceViewer viewer, IAnnotationAccess annotationAccess, ISharedTextColors colorsCache) {
		super();
		fViewer = viewer;
		fViewer.addTextInputListener(textInputListener);

		fColorsCache = colorsCache;
		fColumn = new AppNameRulerColumn(ANNOTATION_COLUMN_WIDTH, annotationAccess);
		fColumn.addAnnotationType(AppNameAnnotation.TYPE);
		fColumn.setHover(new IAnnotationHover() {
			@Override
			public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
				for (Iterator<?> itr = fColumn.getModel().getAnnotationIterator(); itr.hasNext();) {
					Object o = itr.next();
					if (o instanceof AppNameAnnotation) {
						AppNameAnnotation a = (AppNameAnnotation) o;
						Position p = fColumn.getModel().getPosition(a);
						try {
							if (fViewer.getDocument().getLineOfOffset(p.getOffset()) <= lineNumber && fViewer.getDocument().getLineOfOffset(p.getOffset() + p.getLength() - 1) >= lineNumber) {
								String hoverText = "Application '" + a.getText() + "'";
								if (a.isSelected()) {
									hoverText += " is selected";
								}
								return hoverText;
							}
						} catch (BadLocationException e) {
							BootDashActivator.log(e);
						}
					}
				}
				return null;
			}

		});

		AppNameAnnotationsPainter annotationPainter= new AppNameAnnotationsPainter(fViewer, annotationAccess);
		annotationPainter.addDrawingStrategy(AppNameAnnotationModel.APP_NAME_MODEL_KEY, new AppNameDrawingStrategy());
		annotationPainter.addAnnotationType(AppNameAnnotation.TYPE, AppNameAnnotationModel.APP_NAME_MODEL_KEY);
		annotationPainter.setAnnotationTypeColor(AppNameAnnotation.TYPE, fViewer.getControl().getDisplay().getSystemColor(SWT.COLOR_GRAY));
		fViewer.addPainter(annotationPainter);

		fViewer.addVerticalRulerColumn(fColumn);
	}

	public void dispose() {
		fViewer.removeTextInputListener(textInputListener);
	}

	public static AppNameAnnotationModel getAppNameAnnotationModel(ISourceViewer viewer) {
		IAnnotationModel model = viewer instanceof ISourceViewerExtension2
				? ((ISourceViewerExtension2) viewer).getVisualAnnotationModel() : viewer.getAnnotationModel();
		AppNameAnnotationModel appNameModel = null;
		if (model instanceof IAnnotationModelExtension) {
			appNameModel = (AppNameAnnotationModel) ((IAnnotationModelExtension) model)
					.getAnnotationModel(AppNameAnnotationModel.APP_NAME_MODEL_KEY);
		} else if (model instanceof AppNameAnnotationModel) {
			appNameModel = (AppNameAnnotationModel) model;
		}
		return appNameModel;
	}

	private static class AppNameAnnotationsPainter extends AnnotationPainter {

		/**
		 * Creates a new painter indicating the location of collapsed regions.
		 *
		 * @param sourceViewer the source viewer for the painter
		 * @param access the annotation access
		 */
		public AppNameAnnotationsPainter(ISourceViewer sourceViewer, IAnnotationAccess access) {
			super(sourceViewer, access);
		}

		/*
		 * @see org.eclipse.jface.text.source.AnnotationPainter#findAnnotationModel(org.eclipse.jface.text.source.ISourceViewer)
		 */
		protected IAnnotationModel findAnnotationModel(ISourceViewer sourceViewer) {
			return getAppNameAnnotationModel(sourceViewer);
		}

		/*
		 * @see org.eclipse.jface.text.source.AnnotationPainter#skip(org.eclipse.jface.text.source.Annotation)
		 */
		protected boolean skip(Annotation annotation) {
			return !(annotation instanceof AppNameAnnotation);
		}
	}

	private class AppNameDrawingStrategy implements AnnotationPainter.IDrawingStrategy {
		/*
		 * @see org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy#draw(org.eclipse.swt.graphics.GC, org.eclipse.swt.custom.StyledText, int, int, org.eclipse.swt.graphics.Color)
		 */
		public void draw(Annotation annotation, GC gc, StyledText textWidget, int offset, int length, Color color) {
			if (annotation instanceof AppNameAnnotation) {
				AppNameAnnotation a = (AppNameAnnotation) annotation;
				StyledTextContent content = textWidget.getContent();
				final int line = content.getLineAtOffset(offset);
				if (gc == null) {
					textWidget.setLineBackground(line, 1, null);
					textWidget.redrawRange(offset, length, true);
				} else {
					Position p = fColumn.getModel().getPosition(annotation);
					if (p != null && line == content.getLineAtOffset(p.getOffset())) {
						Color lineColor = a.isSelected() ? fColorsCache.getColor(SELECTED_APP_NAME_COLOR)
								: fColorsCache.getColor(APP_NAME_COLOR);
						Color c = gc.getBackground();
						int opacity = gc.getAlpha();
						gc.setBackground(lineColor);
						gc.setAlpha(APP_NAME_ANNOTATION_ALPHA);
						gc.fillRectangle(0, textWidget.getLocationAtOffset(offset).y, textWidget.getClientArea().width,
								textWidget.getLineHeight());
						gc.setAlpha(opacity);
						gc.setBackground(c);
					}
				}
			}
		}
	}

}
