package org.springframework.ide.eclipse.webflow.mylar.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.mylar.context.core.AbstractContextStructureBridge;
import org.eclipse.mylar.context.core.ContextCorePlugin;
import org.eclipse.mylar.context.core.IMylarElement;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISpringProject;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModel;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;

/**
 * {@link AbstractContextStructureBridge} extension that integrates the
 * {@link IWebflowModel} with Mylar.
 * @author Christian Dupuis
 * @since 2.0
 */
public class WebflowContextStructureBridge extends
		AbstractContextStructureBridge {

	public static final String CONTENT_TYPE = "spring/webflow";

	@Override
	public boolean acceptsObject(Object object) {
		return (object instanceof IModelElement || (object instanceof IResource && WebflowModelUtils
				.isWebflowConfig((IResource) object)));
	}

	@Override
	public boolean canBeLandmark(String handle) {
		return true;
	}

	@Override
	public boolean canFilter(Object obj) {
		if (obj instanceof IModelElement) {
			IModelElement modelElement = (IModelElement) obj;

			IModelElement[] children = modelElement.getElementChildren();
			for (IModelElement child : children) {
				IMylarElement node = ContextCorePlugin.getContextManager()
						.getElement(getHandleIdentifier(child));
				if (node != null && node.getInterest().isInteresting()) {
					return false;
				}
			}

			if (modelElement instanceof ISpringProject) {
				IProject project = ((ISpringProject) modelElement).getProject();
				IWebflowProject webflowProject = Activator.getModel()
						.getProject(project);
				return canFilter(webflowProject);
			}

			IMylarElement node = ContextCorePlugin.getContextManager()
					.getElement(getHandleIdentifier(obj));
			if (node != null && node.getInterest().isInteresting()) {
				return false;
			}
		}
		else if ((obj instanceof IFile && WebflowModelUtils
				.isWebflowConfig((IResource) obj))) {
			return canFilter(WebflowModelUtils.getWebflowConfig((IFile) obj));
		}
		return true;
	}

	@Override
	public List<String> getChildHandles(String handle) {
		Object obj = getObjectForHandle(handle);
		if (obj != null && obj instanceof IModelElement) {
			List<String> childHandles = new ArrayList<String>();
			IModelElement[] children = ((IModelElement) obj)
					.getElementChildren();
			for (IModelElement child : children) {
				childHandles.add(child.getElementID());
			}
			return childHandles;
		}
		return null;
	}

	@Override
	public String getContentType() {
		return CONTENT_TYPE;
	}

	@Override
	public String getContentType(String handle) {
		return CONTENT_TYPE;
	}

	@Override
	public String getHandleIdentifier(Object obj) {
		if (obj instanceof IModelElement) {
			return ((IModelElement) obj).getElementID();
		}
		else if (obj instanceof IFile
				&& WebflowModelUtils.isWebflowConfig((IFile) obj)) {
			return WebflowModelUtils.getWebflowConfig((IFile) obj)
					.getElementID();
		}
		return null;
	}

	@Override
	public String getName(Object obj) {
		if (obj instanceof IModelElement) {
			return ((IModelElement) obj).getElementName();
		}
		else if (obj instanceof IFile
				&& WebflowModelUtils.isWebflowConfig((IFile) obj)) {
			return WebflowModelUtils.getWebflowConfig((IFile) obj)
					.getElementName();
		}
		return null;
	}

	@Override
	public Object getObjectForHandle(String handle) {
		return Activator.getModel().getElement(handle);
	}

	@Override
	public String getParentHandle(String handle) {
		Object obj = getObjectForHandle(handle);
		if (obj != null && obj instanceof IModelElement) {
			IModelElement parent = ((IModelElement) obj).getElementParent();
			if (parent != null) {
				return parent.getElementID();
			}
		}
		return null;
	}

	@Override
	public boolean isDocument(String handle) {
		Object obj = getObjectForHandle(handle);
		if (obj != null && obj instanceof IWebflowConfig) {
			return true;
		}
		return false;
	}

	@Override
	public String getHandleForOffsetInObject(Object arg0, int arg1) {
		return null;
	}

}
