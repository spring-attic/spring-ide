/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.graph.parts;

import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.FanRouter;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.ShortestPathConnectionRouter;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.graph.CompoundDirectedGraph;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.commands.CommandStackListener;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.springframework.ide.eclipse.config.core.preferences.SpringConfigPreferenceConstants;
import org.springframework.ide.eclipse.config.graph.ConfigGraphPlugin;
import org.springframework.ide.eclipse.config.graph.model.AbstractConfigGraphDiagram;
import org.springframework.ide.eclipse.config.graph.model.Activity;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @author Terry Denney
 */
public abstract class ActivityDiagramPart extends StructuredActivityPart implements IPropertyChangeListener {

	private final AbstractConfigGraphDiagram diagram;

	private DelegatingLayoutManager delegatingLayoutManager;

	private final int direction;

	private boolean isFirstManualLayout;

	private boolean isLayoutReset;

	private final IPreferenceStore prefStore;

	CommandStackListener stackListener = new CommandStackListener() {
		public void commandStackChanged(EventObject event) {
			if (delegatingLayoutManager.getActiveLayoutManager() instanceof GraphLayoutManager) {
				if (GraphAnimation.captureLayout(getFigure())) {
					while (GraphAnimation.step()) {
						getFigure().getUpdateManager().performUpdate();
					}
				}
				GraphAnimation.end();
			}
			else {
				getFigure().getUpdateManager().performUpdate();
			}
		}
	};

	public ActivityDiagramPart(AbstractConfigGraphDiagram diagram, int direction) {
		super(diagram);
		this.diagram = diagram;
		prefStore = ConfigGraphPlugin.getDefault().getPreferenceStore();

		if (direction == PositionConstants.EAST) {
			this.direction = direction;
		}
		else {
			this.direction = PositionConstants.SOUTH;
		}
		isFirstManualLayout = true;
	}

	/**
	 * @see org.springframework.ide.eclipse.config.graph.parts.ActivityPart#activate()
	 */
	@Override
	public void activate() {
		super.activate();
		getViewer().getEditDomain().getCommandStack().addCommandStackListener(stackListener);
		prefStore.addPropertyChangeListener(this);
	}

	@Override
	protected void applyOwnResults(CompoundDirectedGraph graph, Map<AbstractGraphicalEditPart, Object> map) {
	}

	/**
	 * @see org.springframework.ide.eclipse.config.graph.parts.ActivityPart#createEditPolicies()
	 */
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());
		// installEditPolicy(EditPolicy.CONTAINER_ROLE, new
		// ActivityContainerEditPolicy());
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, null);
		installEditPolicy(EditPolicy.LAYOUT_ROLE, null);
		installEditPolicy(EditPolicy.NODE_ROLE, null);
		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, null);
	}

	@Override
	protected IFigure createFigure() {
		Figure f = new FreeformLayer() {
			@Override
			public void setBounds(Rectangle rect) {
				int x = bounds.x, y = bounds.y;

				boolean resize = (rect.width != bounds.width) || (rect.height != bounds.height), translate = (rect.x != x)
						|| (rect.y != y);

				if (isVisible() && (resize || translate)) {
					erase();
				}
				if (translate) {
					int dx = rect.x - x;
					int dy = rect.y - y;
					primTranslate(dx, dy);
				}
				bounds.width = rect.width;
				bounds.height = rect.height;
				if (resize || translate) {
					fireFigureMoved();
					fireCoordinateSystemChanged();
					repaint();
				}
			}
		};

		delegatingLayoutManager = new DelegatingLayoutManager(this);
		f.setLayoutManager(delegatingLayoutManager);
		f.setOpaque(true);
		ConnectionLayer cLayer = (ConnectionLayer) getLayer(LayerConstants.CONNECTION_LAYER);
		cLayer.setAntialias(SWT.ON);

		FanRouter fanRouter = new FanRouter();
		fanRouter.setSeparation(20);
		ShortestPathConnectionRouter router = new ShortestPathConnectionRouter(f);
		fanRouter.setNextRouter(router);
		cLayer.setConnectionRouter(fanRouter);
		return f;
	}

	/**
	 * @see org.springframework.ide.eclipse.config.graph.parts.ActivityPart#deactivate()
	 */
	@Override
	public void deactivate() {
		prefStore.removePropertyChangeListener(this);
		getViewer().getEditDomain().getCommandStack().removeCommandStackListener(stackListener);
		super.deactivate();
	}

	public int getDirection() {
		return direction;
	}

	private void handleLayoutChange(PropertyChangeEvent event) {
		getFigure().setLayoutManager(delegatingLayoutManager);
	}

	private void handleLayoutReset(PropertyChangeEvent event) {
		isLayoutReset = true;
		isFirstManualLayout = true;
		diagram.doResetCoordinates();
		handleLayoutChange(event);
	}

	public boolean isFirstManualLayout() {
		return isFirstManualLayout;
	}

	public boolean isManualLayout() {
		return prefStore.getBoolean(SpringConfigPreferenceConstants.PREF_MANUAL_LAYOUT);
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractEditPart#isSelectable()
	 */
	@Override
	public boolean isSelectable() {
		return false;
	}

	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if (SpringConfigPreferenceConstants.PREF_MANUAL_LAYOUT.equals(property)) {
			handleLayoutChange(event);
		}
		else if (SpringConfigPreferenceConstants.PROP_RESET_LAYOUT.equals(property)) {
			Object value = event.getNewValue();
			if (value != null && value.equals(diagram.getNamespaceUri())) {
				handleLayoutReset(event);
			}
		}
	}

	@Override
	public void refresh() {
		((AbstractConfigGraphDiagram) getModelElement()).refreshModelFromXml();
		super.refresh();
	}

	@Override
	protected void refreshVisuals() {
		// no-op
	}

	/**
	 * Updates the bounds of the figure (without invoking any event handling),
	 * and sets the layout constraint data
	 * 
	 * @return
	 */
	public boolean setBoundsOnFigure(boolean updateConstraint) {
		List parts = getChildren();
		for (Iterator iter = parts.iterator(); iter.hasNext();) {
			ActivityPart part = (ActivityPart) iter.next();
			Activity activity = part.getModelElement();

			Rectangle bounds = activity.getBounds();
			if (isFirstManualLayout) {
				Rectangle savedBounds = diagram.doReadCoordinates(activity);
				if (savedBounds != null) {
					bounds = savedBounds;
				}
			}

			IFigure figure = part.getFigure();
			if (bounds != null && figure != null && updateConstraint) {
				delegatingLayoutManager.setXYLayoutConstraint(figure, new Rectangle(bounds.x, bounds.y, bounds.width,
						bounds.height));
				activity.setBounds(bounds);
				activity.setHasManualBounds(true);
			}
		}
		isFirstManualLayout = false;
		return true;
	}

	/**
	 * Updates the bounds in the model so that the same bounds can be restored
	 * after saving
	 * 
	 * @return
	 */
	public boolean setBoundsOnModel() {
		List parts = getChildren();
		for (Iterator iter = parts.iterator(); iter.hasNext();) {
			ActivityPart part = (ActivityPart) iter.next();
			Activity activity = part.getModelElement();
			IFigure figure = part.getFigure();
			if (figure != null && (!activity.hasManualBounds() || isLayoutReset)) {
				Rectangle bounds = figure.getBounds().getCopy();
				activity.setBounds(bounds);
			}
		}
		isLayoutReset = false;
		return true;
	}

}
