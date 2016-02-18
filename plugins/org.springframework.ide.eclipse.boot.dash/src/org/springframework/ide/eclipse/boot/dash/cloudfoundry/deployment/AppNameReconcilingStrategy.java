package org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

	private IAnnotationModel getAppNameAnnotationModel() {
		IAnnotationModel model = fViewer instanceof ISourceViewerExtension2 ? ((ISourceViewerExtension2)fViewer).getVisualAnnotationModel() : fViewer.getAnnotationModel();
		if (model instanceof IAnnotationModelExtension) {
			return ((IAnnotationModelExtension) model).getAnnotationModel(AppNameAnnotationModel.APP_NAME_MODEL_KEY);
		}
		return model;
	}

	@Override
	public void reconcile(IRegion region) {
		IAnnotationModel annotationModel = getAppNameAnnotationModel();

		if (annotationModel == null) {
			return;
		}

		List<Annotation> toRemove= new ArrayList<Annotation>();

		@SuppressWarnings("unchecked")
		Iterator<Annotation> iter= annotationModel.getAnnotationIterator();
		while (iter.hasNext()) {
			Annotation annotation= iter.next();
			if (AppNameAnnotation.TYPE.equals(annotation.getType())) {
				toRemove.add(annotation);
			}
		}
		Annotation[] annotationsToRemove= toRemove.toArray(new Annotation[toRemove.size()]);

		Map<Annotation, Position> annotationsToAdd = createAnnotations();

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

	private Map<Annotation, Position> createAnnotations() {
		Map<Annotation, Position> annotationsMap = new HashMap<>();
		try {
			YamlFileAST ast = parser.getAST(fDocument);
			List<Node> rootList = ast.getNodes();
			if (rootList.size() == 1) {
				Node root = rootList.get(0);
				SequenceNode applicationsNode = YamlGraphDeploymentProperties.getNode(root, ApplicationManifestHandler.APPLICATIONS_PROP, SequenceNode.class);
				if (applicationsNode == null) {
					if (YamlGraphDeploymentProperties.getPropertyValue(root, ApplicationManifestHandler.NAME_PROP, ScalarNode.class) != null) {
						annotationsMap.put(new AppNameAnnotation(true), new Position(root.getStartMark().getIndex(), root.getEndMark().getIndex() - root.getStartMark().getIndex()));
					}
				} else {
					for (Node appNode : applicationsNode.getValue()) {
						if (YamlGraphDeploymentProperties.getNode(appNode, ApplicationManifestHandler.NAME_PROP, ScalarNode.class) != null) {
							annotationsMap.put(new AppNameAnnotation(true), new Position(appNode.getStartMark().getIndex(), appNode.getEndMark().getIndex() - appNode.getStartMark().getIndex()));
						}
					}
				}
			}
		} catch (Throwable t) {
			BootDashActivator.log(t);
		}
		return annotationsMap;
	}

}
