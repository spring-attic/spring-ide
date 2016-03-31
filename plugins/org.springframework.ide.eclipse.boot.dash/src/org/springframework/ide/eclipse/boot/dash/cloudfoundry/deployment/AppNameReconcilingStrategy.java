package org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension2;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ApplicationManifestHandler;
import org.springframework.ide.eclipse.editor.support.yaml.ast.YamlASTProvider;
import org.springframework.ide.eclipse.editor.support.yaml.ast.YamlFileAST;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;

public class AppNameReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {

	private ISourceViewer fViewer;
	private YamlASTProvider parser;

	private IDocument fDocument;
	private IProgressMonitor fProgressMonitor;

	public AppNameReconcilingStrategy(ISourceViewer viewer, YamlASTProvider parser) {
		this.fViewer = viewer;
		this.parser = parser;
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#initialReconcile()
	 */
	public void initialReconcile() {
		reconcile(new Region(0, fDocument.getLength()));
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#reconcile(org.eclipse.jface.text.reconciler.DirtyRegion,org.eclipse.jface.text.IRegion)
	 */
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		try {
			IRegion startLineInfo= fDocument.getLineInformationOfOffset(subRegion.getOffset());
			IRegion endLineInfo= fDocument.getLineInformationOfOffset(subRegion.getOffset() + Math.max(0, subRegion.getLength() - 1));
			if (startLineInfo.getOffset() == endLineInfo.getOffset())
				subRegion= startLineInfo;
			else
				subRegion= new Region(startLineInfo.getOffset(), endLineInfo.getOffset() + Math.max(0, endLineInfo.getLength() - 1) - startLineInfo.getOffset());

		} catch (BadLocationException e) {
			subRegion= new Region(0, fDocument.getLength());
		}
		reconcile(subRegion);
	}

	private AppNameAnnotationModel getAppNameAnnotationModel() {
		IAnnotationModel model = fViewer instanceof ISourceViewerExtension2 ? ((ISourceViewerExtension2)fViewer).getVisualAnnotationModel() : fViewer.getAnnotationModel();
		if (model instanceof IAnnotationModelExtension) {
			return (AppNameAnnotationModel) ((IAnnotationModelExtension) model).getAnnotationModel(AppNameAnnotationModel.APP_NAME_MODEL_KEY);
		}
		return (AppNameAnnotationModel) model;
	}

	@Override
	public void reconcile(IRegion region) {
		AppNameAnnotationModel annotationModel = getAppNameAnnotationModel();

		if (annotationModel == null) {
			return;
		}

		List<Annotation> toRemove= new ArrayList<Annotation>();

		@SuppressWarnings("unchecked")
		Iterator<? extends Annotation> iter= annotationModel.getAnnotationIterator();
		while (iter.hasNext()) {
			Annotation annotation= iter.next();
			if (AppNameAnnotation.TYPE.equals(annotation.getType())) {
				toRemove.add(annotation);
			}
		}
		Annotation[] annotationsToRemove= toRemove.toArray(new Annotation[toRemove.size()]);

		Map<AppNameAnnotation, Position> annotationsToAdd = createAnnotations(annotationModel);

		if (annotationModel instanceof IAnnotationModelExtension)
			((IAnnotationModelExtension)annotationModel).replaceAnnotations(annotationsToRemove, annotationsToAdd);
		else {
			for (int i= 0; i < annotationsToRemove.length; i++)
				annotationModel.removeAnnotation(annotationsToRemove[i]);
			for (iter= annotationsToAdd.keySet().iterator(); iter.hasNext();) {
				Annotation annotation= iter.next();
				annotationModel.addAnnotation(annotation, annotationsToAdd.get(annotation));
			}
		}

	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#setProgressMonitor(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public final void setProgressMonitor(IProgressMonitor monitor) {
		fProgressMonitor= monitor;
	}

	@Override
	public void setDocument(IDocument document) {
		fDocument= document;
	}

	private Map<AppNameAnnotation, Position> createAnnotations(AppNameAnnotationModel annotationModel) {
		Map<AppNameAnnotation, Position> annotationsMap = new LinkedHashMap<>();
		fProgressMonitor.beginTask("Calculating application names", 100);
		try {
			YamlFileAST ast = parser.getAST(fDocument);
			List<Node> rootList = ast.getNodes();
			fProgressMonitor.worked(70);
			if (rootList.size() == 1) {
				Node root = rootList.get(0);
				SequenceNode applicationsNode = YamlGraphDeploymentProperties.getNode(root, ApplicationManifestHandler.APPLICATIONS_PROP, SequenceNode.class);
				if (applicationsNode == null) {
					ScalarNode node = YamlGraphDeploymentProperties.getPropertyValue(root, ApplicationManifestHandler.NAME_PROP, ScalarNode.class);
					if (node != null) {
						annotationsMap.put(new AppNameAnnotation(node.getValue(), true),
								new Position(root.getStartMark().getIndex(),
										getLastWhiteCharIndex(fDocument.get(), root.getEndMark().getIndex())
												- root.getStartMark().getIndex()));
					}
				} else {
					for (Node appNode : applicationsNode.getValue()) {
						ScalarNode node = YamlGraphDeploymentProperties.getNode(appNode, ApplicationManifestHandler.NAME_PROP, ScalarNode.class);
						if (node != null) {
							annotationsMap.put(new AppNameAnnotation(node.getValue()), new Position(appNode.getStartMark().getIndex(), getLastWhiteCharIndex(fDocument.get(), appNode.getEndMark().getIndex()) - appNode.getStartMark().getIndex()));
						}
					}
				}
				fProgressMonitor.worked(20);
				if (!annotationsMap.isEmpty()) {
					AppNameAnnotation selected = annotationModel.getSelectedAppAnnotation();
					Map.Entry<AppNameAnnotation, Position> newSelected = null;
					if (selected != null) {
						Position selectedPosition = annotationModel.getPosition(selected);
						for (Map.Entry<AppNameAnnotation, Position> entry : annotationsMap.entrySet()) {
							if (entry.getKey().getText().equals(selected.getText())) {
								if (newSelected == null) {
									newSelected = entry;
								} else if (Math.abs(newSelected.getValue().getOffset() - selectedPosition.getOffset()) > Math.abs(entry.getValue().getOffset() - selectedPosition.getOffset())){
									newSelected = entry;
								}
							} else if (entry.getValue().getOffset() == selectedPosition.getOffset() && newSelected == null) {
								newSelected = entry;
							}
						}
					}
					if (newSelected == null) {
						newSelected = annotationsMap.entrySet().iterator().next();
					}
					newSelected.getKey().markSelected();
					fProgressMonitor.worked(10);
				}
			}
		} catch (Throwable t) {
			BootDashActivator.log(t);
		} finally {
			fProgressMonitor.done();
		}
		return annotationsMap;
	}

	private static int getLastWhiteCharIndex(String text, int index) {
		int i = index < text.length() ? index : index - 1;
		for (; i >=  0 && Character.isWhitespace(text.charAt(i)); i--) {
			// Nothing to do
		}
		return i == index ? i : i + 1;
	}

}
