package org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment;

import java.util.Iterator;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;

public class AppNameAnnotationModel extends AnnotationModel {

	public static final Object APP_NAME_MODEL_KEY = new Object();

	/**
	 * Creates a new, empty projection annotation model.
	 */
	public AppNameAnnotationModel() {
	}

	/**
	 * Marks the given annotation as selected. An appropriate
	 * annotation model change event is sent out.
	 *
	 * @param annotation the annotation
	 */
	public void markSelected(Annotation annotation) {
		if (annotation instanceof AppNameAnnotation) {
			AppNameAnnotation appName = (AppNameAnnotation) annotation;
			Iterator<?> iterator= getAnnotationIterator();
			while(iterator.hasNext()) {
				Object o = iterator.next();
				if (o instanceof AppNameAnnotation && appName != o) {
					AppNameAnnotation a = (AppNameAnnotation) o;
					if (a.isSelected()) {
						a.markUnselected();
						modifyAnnotation(a, true);
					}
				}
			}
			if (!appName.isSelected()) {
				appName.markSelected();
				modifyAnnotation(appName, true);
			}
		}
	}

	public AppNameAnnotation getSelectedAppAnnotation() {
		Iterator<?> iterator= getAnnotationIterator();
		while(iterator.hasNext()) {
			Object o = iterator.next();
			if (o instanceof AppNameAnnotation) {
				AppNameAnnotation a = (AppNameAnnotation) o;
				if (a.isSelected()) {
					return a;
				}
			}
		}
		return null;
	}

}
